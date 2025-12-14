package com.filesystem.analyzer.utils;

import com.filesystem.analyzer.model.FileInfo;
import com.filesystem.analyzer.exceptions.DirectoryAccessException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Основной класс для сканирования файловой системы и получения информации о файлах и директориях.
 * <p>
 * Предоставляет методы для синхронного и асинхронного сканирования директорий,
 * получения статистики, а также работы с отдельными файлами. Использует многопоточность
 * для повышения производительности при работе с большими директориями.
 * </p>
 *
 * <p><b>Особенности реализации:</b></p>
 * <ul>
 * <li>Использует NIO.2 API для работы с файловой системой</li>
 * <li>Поддерживает рекурсивное сканирование директорий</li>
 * <li>Предоставляет асинхронные методы для длительных операций</li>
 * <li>Логирует все операции через Log4j2</li>
 * <li>Корректно обрабатывает ошибки доступа и исключительные ситуации</li>
 * </ul>
 *
 * @author Максим
 * @version 1.0
 * @since 2024
 * @see java.nio.file.Files
 * @see java.nio.file.Path
 * @see com.filesystem.analyzer.model.FileInfo
 * @see com.filesystem.analyzer.exceptions.DirectoryAccessException
 */
public class FileSystemScanner {

    /** Логгер для класса FileSystemScanner */
    private static final Logger logger = LogManager.getLogger(FileSystemScanner.class);

    /** Пул потоков для выполнения асинхронных задач */
    private final ExecutorService executorService;

    /**
     * Создает новый экземпляр FileSystemScanner.
     * <p>
     * Инициализирует пул потоков для асинхронных операций. Количество потоков
     * определяется количеством доступных процессоров в системе.
     * </p>
     *
     * <p><b>Пример использования:</b></p>
     * <pre>{@code
     * FileSystemScanner scanner = new FileSystemScanner();
     * List<FileInfo> files = scanner.scanDirectory("/path/to/dir");
     * scanner.shutdown();
     * }</pre>
     */
    public FileSystemScanner() {
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
        logger.debug("FileSystemScanner инициализирован с пулом из {} потоков",
                Runtime.getRuntime().availableProcessors());
    }

    /**
     * Сканирует содержимое указанной директории.
     * <p>
     * Рекурсивно получает информацию о всех файлах и поддиректориях в указанной директории.
     * Метод работает синхронно и может блокировать выполнение на время сканирования.
     * </p>
     *
     * @param directoryPath путь к директории для сканирования
     * @return список объектов {@link FileInfo}, представляющих содержимое директории
     * @throws DirectoryAccessException если директория не существует, недоступна
     *         или произошла ошибка ввода-вывода при доступе к файлам
     *
     * <p><b>Особенности работы:</b></p>
     * <ul>
     * <li>Проверяет существование и доступность директории</li>
     * <li>Обрабатывает каждый файл/директорию отдельно</li>
     * <li>Логирует процесс сканирования и статистику</li>
     * <li>Продолжает работу при ошибках доступа к отдельным файлам</li>
     * <li>Бросает исключение при критических ошибках доступа к директории</li>
     * </ul>
     *
     * <p><b>Пример использования:</b></p>
     * <pre>{@code
     * try {
     *     List<FileInfo> files = scanner.scanDirectory("/home/user/documents");
     *     files.forEach(file -> System.out.println(file.getName()));
     * } catch (DirectoryAccessException e) {
     *     System.err.println("Ошибка доступа: " + e.getMessage());
     * }
     * }</pre>
     */
    public List<FileInfo> scanDirectory(String directoryPath) throws DirectoryAccessException {
        logger.info("Начало сканирования директории: {}", directoryPath);
        List<FileInfo> result = new ArrayList<>();
        Path path = Paths.get(directoryPath);

        if (!Files.exists(path) || !Files.isDirectory(path)) {
            String errorMsg = "Директория не существует или не является папкой: " + directoryPath;
            logger.error(errorMsg);
            throw new DirectoryAccessException(errorMsg);
        }

        logger.debug("Директория {} существует, начинаем сканирование", directoryPath);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            int processedCount = 0;
            int errorCount = 0;

            for (Path entry : stream) {
                try {
                    FileInfo fileInfo = scanFile(entry);
                    result.add(fileInfo);
                    processedCount++;

                    if (logger.isTraceEnabled()) {
                        logger.trace("Обработан элемент: {} (тип: {}, размер: {})",
                                entry.getFileName(),
                                Files.isDirectory(entry) ? "папка" : "файл",
                                fileInfo.getSize());
                    }
                } catch (IOException e) {
                    errorCount++;
                    logger.error("Ошибка при сканировании файла: {} - {}", entry, e.getMessage(), e);
                    // Сообщение в System.err оставляем для обратной совместимости
                    System.err.println("Ошибка при сканировании файла: " + entry + " - " + e.getMessage());
                }
            }

            logger.info("Сканирование завершено. Успешно обработано: {}, с ошибками: {}, всего: {}",
                    processedCount, errorCount, processedCount + errorCount);

        } catch (IOException e) {
            String errorMsg = "Ошибка доступа к директории: " + directoryPath;
            logger.error(errorMsg, e);
            throw new DirectoryAccessException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Неожиданная ошибка при сканировании директории: " + directoryPath;
            logger.error(errorMsg, e);
            throw new DirectoryAccessException(errorMsg, e);
        }

        return result;
    }

    /**
     * Сканирует отдельный файл и возвращает подробную информацию о нем.
     * <p>
     * Получает все доступные атрибуты файла, включая размер, время создания,
     * время модификации, владельца и расширение.
     * </p>
     *
     * @param path путь к файлу для сканирования
     * @return объект {@link FileInfo} с полной информацией о файле
     * @throws IOException если произошла ошибка при чтении атрибутов файла
     *
     * <p><b>Получаемые атрибуты:</b></p>
     * <ul>
     * <li>Имя файла</li>
     * <li>Относительный и абсолютный путь</li>
     * <li>Размер в байтах</li>
     * <li>Тип (файл/директория)</li>
     * <li>Время создания</li>
     * <li>Время последнего изменения</li>
     * <li>Владелец файла</li>
     * <li>Расширение файла (только для файлов)</li>
     * </ul>
     *
     * <p><b>Особенности работы:</b></p>
     * <ul>
     * <li>Использует {@link BasicFileAttributes} для получения атрибутов</li>
     * <li>Обрабатывает отсутствие информации о владельце (устанавливает "Неизвестно")</li>
     * <li>Автоматически определяет расширение файла</li>
     * <li>Логирует процесс сканирования на разных уровнях</li>
     * </ul>
     */
    public FileInfo scanFile(Path path) throws IOException {
        logger.debug("Начало сканирования файла: {}", path);

        try {
            BasicFileAttributes attrs = Files.readAttributes(
                    path,
                    BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS
            );

            FileInfo info = new FileInfo();
            info.setName(path.getFileName().toString());
            info.setPath(path.toString());
            info.setAbsolutePath(path.toAbsolutePath().toString());
            info.setSize(attrs.size());
            info.setDirectory(attrs.isDirectory());

            logger.trace("Основные атрибуты получены: размер={}, директория={}",
                    attrs.size(), attrs.isDirectory());

            // Время создания и модификации
            if (attrs.creationTime() != null) {
                info.setCreationTime(LocalDateTime.ofInstant(
                        attrs.creationTime().toInstant(),
                        ZoneId.systemDefault()
                ));
                logger.trace("Время создания установлено");
            }

            if (attrs.lastModifiedTime() != null) {
                info.setLastModifiedTime(LocalDateTime.ofInstant(
                        attrs.lastModifiedTime().toInstant(),
                        ZoneId.systemDefault()
                ));
                logger.trace("Время модификации установлено");
            }

            // Владелец файла
            try {
                UserPrincipal owner = Files.getOwner(path);
                info.setOwner(owner.getName());
                logger.trace("Владелец файла: {}", owner.getName());
            } catch (IOException e) {
                info.setOwner("Неизвестно");
                logger.warn("Не удалось получить владельца файла: {} - {}", path, e.getMessage());
            }

            // Расширение файла
            if (!attrs.isDirectory()) {
                String fileName = path.getFileName().toString();
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                    String extension = fileName.substring(dotIndex + 1).toLowerCase();
                    info.setExtension(extension);
                    logger.trace("Расширение файла: {}", extension);
                } else {
                    logger.trace("Файл без расширения или скрытый");
                }
            } else {
                logger.trace("Элемент является директорией");
            }

            logger.debug("Сканирование файла завершено: {}", path);
            return info;

        } catch (IOException e) {
            logger.error("Ошибка при сканировании файла {}: {}", path, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Получает расширенную статистику для указанной директории.
     * <p>
     * Рекурсивно обходит все файлы и поддиректории, собирая следующую статистику:
     * </p>
     * <ul>
     * <li>Общий размер всех файлов в байтах</li>
     * <li>Количество файлов</li>
     * <li>Количество поддиректорий</li>
     * </ul>
     *
     * @param directoryPath путь к директории для анализа
     * @return объект {@link DirectoryStats} со статистикой
     * @throws IOException если директория не существует, недоступна
     *         или произошла ошибка при чтении файлов
     *
     * <p><b>Особенности работы:</b></p>
     * <ul>
     * <li>Использует {@link Files walk(Path)} для рекурсивного обхода</li>
     * <li>Не учитывает саму целевую директорию в подсчете поддиректорий</li>
     * <li>Пропускает файлы, к которым нет доступа (с увеличением счетчика inaccessibleCount)</li>
     * <li>Логирует процесс сбора статистики и результаты</li>
     * </ul>
     *
     * <p><b>Производительность:</b></p>
     * Метод может быть медленным для больших директорий с тысячами файлов.
     * Для таких случаев рекомендуется использовать {@link #getDirectoryStatsAsync(Path)}.
     *
     * @see #getDirectoryStatsAsync(Path)
     * @see DirectoryStats
     */
    public DirectoryStats getDirectoryStats(Path directoryPath) throws IOException {
        logger.info("Получение статистики для директории: {}", directoryPath);

        long totalSize = 0;
        int fileCount = 0;
        int dirCount = 0;
        int inaccessibleCount = 0;

        try {
            if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
                String errorMsg = "Директория не существует: " + directoryPath;
                logger.error(errorMsg);
                throw new IOException(errorMsg);
            }

            logger.debug("Рекурсивный обход директории {}", directoryPath);

            // Используем Files.walk для рекурсивного обхода
            try (var stream = Files.walk(directoryPath)) {
                var iterator = stream.iterator();
                while (iterator.hasNext()) {
                    Path path = iterator.next();
                    try {
                        if (Files.isDirectory(path)) {
                            if (!path.equals(directoryPath)) { // не считаем саму директорию
                                dirCount++;
                            }
                        } else {
                            fileCount++;
                            long fileSize = Files.size(path);
                            totalSize += fileSize;

                            if (logger.isTraceEnabled()) {
                                logger.trace("Файл: {}, размер: {} байт", path.getFileName(), fileSize);
                            }
                        }
                    } catch (IOException e) {
                        inaccessibleCount++;
                        logger.warn("Нет доступа к элементу: {} - {}", path, e.getMessage());
                        // Пропускаем файлы, к которым нет доступа
                        continue;
                    }
                }
            }

            logger.info("Статистика собрана. Файлов: {}, Папок: {}, Общий размер: {} байт, Недоступно: {}",
                    fileCount, dirCount, totalSize, inaccessibleCount);

        } catch (IOException e) {
            String errorMsg = "Не удалось получить статистику для директории: " + directoryPath;
            logger.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        }

        return new DirectoryStats(totalSize, fileCount, dirCount);
    }

    /**
     * Вспомогательный класс для хранения статистики директории.
     * <p>
     * Предоставляет неизменяемые (immutable) данные о характеристиках директории.
     * Используется для возврата результатов методов {@link #getDirectoryStats(Path)}
     * и {@link #getDirectoryStatsAsync(Path)}.
     * </p>
     *
     * @see #getDirectoryStats(Path)
     * @see #getDirectoryStatsAsync(Path)
     */
    public static class DirectoryStats {
        /** Общий размер всех файлов в директории в байтах */
        private final long totalSize;

        /** Количество файлов в директории */
        private final int fileCount;

        /** Количество поддиректорий в директории */
        private final int directoryCount;

        /**
         * Создает новый объект статистики директории.
         *
         * @param totalSize общий размер всех файлов в байтах
         * @param fileCount количество файлов
         * @param directoryCount количество поддиректорий
         */
        public DirectoryStats(long totalSize, int fileCount, int directoryCount) {
            this.totalSize = totalSize;
            this.fileCount = fileCount;
            this.directoryCount = directoryCount;
            logger.debug("DirectoryStats создан: размер={}, файлы={}, папки={}",
                    totalSize, fileCount, directoryCount);
        }

        /**
         * Возвращает общий размер всех файлов в директории.
         *
         * @return размер в байтах
         */
        public long getTotalSize() { return totalSize; }

        /**
         * Возвращает количество файлов в директории.
         *
         * @return количество файлов
         */
        public int getFileCount() { return fileCount; }

        /**
         * Возвращает количество поддиректорий в директории.
         *
         * @return количество поддиректорий
         */
        public int getDirectoryCount() { return directoryCount; }

        /**
         * Возвращает строковое представление статистики.
         *
         * @return строка в формате "DirectoryStats{files=X, dirs=Y, size=Z}"
         */
        @Override
        public String toString() {
            return String.format("DirectoryStats{files=%d, dirs=%d, size=%d}",
                    fileCount, directoryCount, totalSize);
        }
    }

    /**
     * Асинхронно получает статистику для указанной директории.
     * <p>
     * Возвращает {@link CompletableFuture}, который будет выполнен с результатом
     * {@link DirectoryStats} после завершения сбора статистики.
     * </p>
     *
     * @param directoryPath путь к директории для анализа
     * @return CompletableFuture с результатом DirectoryStats
     *
     * <p><b>Особенности работы:</b></p>
     * <ul>
     * <li>Выполняет сбор статистики в отдельном потоке из пула</li>
     * <li>Не блокирует основной поток выполнения</li>
     * <li>Позволяет обрабатывать результат асинхронно с помощью thenApply, thenAccept</li>
     * <li>Логирует начало и завершение асинхронной задачи</li>
     * </ul>
     *
     * <p><b>Пример использования:</b></p>
     * <pre>{@code
     * CompletableFuture<DirectoryStats> future = scanner.getDirectoryStatsAsync(Paths.get("/data"));
     * future.thenAccept(stats -> {
     *     System.out.println("Файлов: " + stats.getFileCount());
     *     System.out.println("Общий размер: " + stats.getTotalSize());
     * }).exceptionally(ex -> {
     *     System.err.println("Ошибка: " + ex.getMessage());
     *     return null;
     * });
     * }</pre>
     *
     * @see CompletableFuture
     * @see #getDirectoryStats(Path)
     */
    public CompletableFuture<DirectoryStats> getDirectoryStatsAsync(Path directoryPath) {
        logger.debug("Асинхронный запрос статистики для: {}", directoryPath);

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.trace("Запущена асинхронная задача для директории: {}", directoryPath);
                DirectoryStats stats = getDirectoryStats(directoryPath);
                logger.debug("Асинхронная задача завершена: {}", directoryPath);
                return stats;
            } catch (IOException e) {
                logger.error("Ошибка в асинхронной задаче для {}: {}", directoryPath, e.getMessage(), e);
                throw new CompletionException(e);
            }
        }, executorService);
    }

    /**
     * Корректно завершает работу FileSystemScanner.
     * <p>
     * Останавливает все активные задачи в пуле потоков и освобождает ресурсы.
     * Метод должен вызываться перед завершением работы приложения.
     * </p>
     *
     * <p><b>Последовательность действий:</b></p>
     * <ol>
     * <li>Инициирует плавную остановку пула потоков</li>
     * <li>Ожидает завершения выполняющихся задач (до 60 секунд)</li>
     * <li>Принудительно останавливает задачи, если время ожидания истекло</li>
     * <li>Обрабатывает прерывание потока во время ожидания</li>
     * <li>Логирует процесс завершения</li>
     * </ol>
     *
     * <p><b>Важно:</b> После вызова этого метода объект FileSystemScanner
     * не может быть использован повторно.</p>
     *
     * @see ExecutorService#shutdown()
     * @see ExecutorService#awaitTermination(long, TimeUnit)
     * @see ExecutorService#shutdownNow()
     */
    public void shutdown() {
        logger.info("Завершение работы FileSystemScanner");

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Потоки не завершились за 60 секунд, принудительное завершение");
                executorService.shutdownNow();
            } else {
                logger.debug("Все потоки успешно завершены");
            }
        } catch (InterruptedException e) {
            logger.error("Прерывание при завершении пула потоков", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("FileSystemScanner остановлен");
    }

    /**
     * Логирует информацию о системе.
     * <p>
     * Выводит в лог информацию о доступных ресурсах системы, включая:
     * </p>
     * <ul>
     * <li>Количество доступных процессоров</li>
     * <li>Объем свободной памяти</li>
     * <li>Общий объем выделенной памяти</li>
     * <li>Максимальный объем памяти</li>
     * </ul>
     *
     * <p><b>Использование:</b> Метод полезен для диагностики и мониторинга
     * производительности приложения.</p>
     *
     * @see Runtime#availableProcessors()
     * @see Runtime#freeMemory()
     * @see Runtime#totalMemory()
     * @see Runtime#maxMemory()
     */
    public void logSystemInfo() {
        if (logger.isInfoEnabled()) {
            Runtime runtime = Runtime.getRuntime();
            logger.info("Информация о системе:");
            logger.info("  Доступно процессоров: {}", runtime.availableProcessors());
            logger.info("  Свободная память: {} MB", runtime.freeMemory() / (1024 * 1024));
            logger.info("  Всего памяти: {} MB", runtime.totalMemory() / (1024 * 1024));
            logger.info("  Максимум памяти: {} MB", runtime.maxMemory() / (1024 * 1024));
        }
    }
}