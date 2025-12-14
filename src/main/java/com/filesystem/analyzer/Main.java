package com.filesystem.analyzer;

import com.filesystem.analyzer.gui.MainFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;

/**
 * Главный класс приложения "Анализатор файловой системы".
 * <p>
 * Содержит точку входа в приложение и отвечает за его инициализацию,
 * настройку графического интерфейса, логирование системной информации
 * и корректное завершение работы.
 * </p>
 *
 * <p><b>Основные функции:</b></p>
 * <ul>
 * <li>Инициализация и настройка логгера Log4j2</li>
 * <li>Настройка системного Look and Feel для графического интерфейса</li>
 * <li>Проверка доступности файловой системы и дисков</li>
 * <li>Сбор и логирование информации о системе</li>
 * <li>Запуск главного окна приложения в потоке обработки событий (EDT)</li>
 * <li>Обработка критических ошибок запуска</li>
 * </ul>
 *
 * @author Максим
 * @version 1.0
 * @since 2024
 * @see MainFrame
 * @see Logger
 * @see SwingUtilities
 */
public class Main {
    /**
     * Логгер для главного класса приложения.
     * Используется для записи событий запуска, информации о системе и ошибок.
     */
    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * Точка входа в приложение "Анализатор файловой системы".
     * <p>
     * Выполняет следующие задачи:
     * </p>
     * <ol>
     * <li>Инициализация логирования</li>
     * <li>Сбор и логирование информации о системе</li>
     * <li>Настройка системного внешнего вида графического интерфейса</li>
     * <li>Проверка доступности файловой системы (дисков)</li>
     * <li>Запуск главного окна приложения в потоке обработки событий</li>
     * <li>Настройка обработчика корректного завершения приложения</li>
     * </ol>
     *
     * @param args аргументы командной строки. В текущей реализации не используются,
     *             но логируются для отладки.
     *
     * <p><b>Последовательность выполнения:</b></p>
     * <pre>
     * 1. Логирование начала работы
     * 2. Логирование системной информации
     * 3. Настройка Look and Feel
     * 4. Проверка доступных дисков
     * 5. Запуск графического интерфейса
     * 6. Настройка shutdown hook
     * </pre>
     *
     * <p><b>Пример запуска:</b></p>
     * <pre>
     * java -jar FileSystemAnalyzer.jar
     * </pre>
     *
     * <p><b>Обработка исключений:</b></p>
     * <ul>
     * <li>Ошибки настройки Look and Feel логируются, но не прерывают работу приложения</li>
     * <li>Критические ошибки при создании GUI показывают диалоговое окно и завершают приложение с кодом 1</li>
     * </ul>
     */
    public static void main(String[] args) {
        logger.info("Запуск приложения Анализатор файловой системы");
        logger.debug("Аргументы командной строки: {}", (Object) args);

        // Логирование информации о системе
        logSystemInfo();

        // Установка современного Look and Feel
        try {
            logger.info("Установка системного Look and Feel");
            String lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lookAndFeelClassName);
            logger.debug("Look and Feel успешно установлен: {}",
                    UIManager.getLookAndFeel().getClass().getName());
        } catch (ClassNotFoundException e) {
            logger.error("Класс Look and Feel не найден", e);
        } catch (InstantiationException e) {
            logger.error("Ошибка создания экземпляра Look and Feel", e);
        } catch (IllegalAccessException e) {
            logger.error("Нет доступа к Look and Feel", e);
        } catch (UnsupportedLookAndFeelException e) {
            logger.error("Неподдерживаемый Look and Feel", e);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка при установке Look and Feel", e);
        }

        // Проверка доступности файловой системы
        logger.info("Проверка доступности файловой системы");
        File[] roots = File.listRoots();

        if (roots == null || roots.length == 0) {
            logger.warn("Не удалось получить список корневых директорий");
        } else {
            logger.info("Найдено {} доступных дисков:", roots.length);
            for (File root : roots) {
                long totalSpace = root.getTotalSpace();
                long freeSpace = root.getFreeSpace();
                long usedSpace = totalSpace - freeSpace;
                double freePercentage = totalSpace > 0 ?
                        (double) freeSpace / totalSpace * 100 : 0;

                logger.info("  Диск: {}", root.getPath());
                logger.info("    Всего места: {} GB",
                        formatBytesToGB(totalSpace));
                logger.info("    Свободно: {} GB ({}%)",
                        formatBytesToGB(freeSpace),
                        String.format("%.1f", freePercentage));
                logger.info("    Использовано: {} GB",
                        formatBytesToGB(usedSpace));

                // Дополнительная информация о правах доступа
                logger.debug("    Читаемый: {}, Записываемый: {}",
                        root.canRead(), root.canWrite());
            }
        }

        // Запуск GUI в потоке обработки событий
        logger.info("Запуск графического интерфейса в потоке EDT");
        SwingUtilities.invokeLater(() -> {
            try {
                logger.debug("Создание главного окна приложения");
                MainFrame frame = new MainFrame();

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setTitle("Анализатор файловой системы");
                frame.pack();
                frame.setLocationRelativeTo(null); // Центрирование окна
                frame.setVisible(true);

                logger.info("Главное окно успешно создано и отображено");
                logger.debug("Размер окна: {}x{}, Позиция: {},{}",
                        frame.getWidth(), frame.getHeight(),
                        frame.getX(), frame.getY());

            } catch (Exception e) {
                logger.error("Критическая ошибка при создании GUI", e);
                JOptionPane.showMessageDialog(
                        null,
                        "Не удалось запустить приложение:\n" + e.getMessage(),
                        "Ошибка запуска",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });

        // Добавляем shutdown hook для корректного завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Приложение завершает работу");
        }));

        logger.info("Инициализация приложения завершена, GUI запущен");
    }

    /**
     * Собирает и логирует информацию о системе.
     * <p>
     * Метод собирает следующие сведения о системе:
     * </p>
     * <ul>
     * <li>Информация о Java (версия, вендор, директория установки)</li>
     * <li>Информация об операционной системе (имя, версия, архитектура)</li>
     * <li>Информация о пользователе и рабочих директориях</li>
     * <li>Информация о памяти (доступные процессоры, свободная/вся/максимальная память)</li>
     * <li>Информация о кодировках файлов</li>
     * </ul>
     * <p>
     * Все данные логируются с уровнем INFO для последующего анализа проблем
     * и мониторинга окружения выполнения.
     * </p>
     *
     * <p><b>Пример вывода в логах:</b></p>
     * <pre>
     * === Информация о системе ===
     * Версия Java: 11.0.12
     * Вендор Java: Oracle Corporation
     * Директория Java: C:\Program Files\Java\jdk-11
     * ОС: Windows 10 10.0
     * Архитектура: amd64
     * Имя пользователя: User
     * Рабочая директория: C:\projects\FileSystemAnalyzer
     * Домашняя директория: C:\Users\User
     * Доступно процессоров: 8
     * Максимальная память: 2048 MB
     * Всего памяти: 512 MB
     * Свободная память: 256 MB
     * Кодировка файлов: UTF-8
     * === Конец информации о системе ===
     * </pre>
     */
    private static void logSystemInfo() {
        logger.info("=== Информация о системе ===");

        // Информация о Java
        logger.info("Версия Java: {}", System.getProperty("java.version"));
        logger.info("Вендор Java: {}", System.getProperty("java.vendor"));
        logger.info("Директория Java: {}", System.getProperty("java.home"));

        // Информация об ОС
        logger.info("ОС: {} {}",
                System.getProperty("os.name"),
                System.getProperty("os.version"));
        logger.info("Архитектура: {}", System.getProperty("os.arch"));
        logger.info("Имя пользователя: {}", System.getProperty("user.name"));
        logger.info("Рабочая директория: {}", System.getProperty("user.dir"));
        logger.info("Домашняя директория: {}", System.getProperty("user.home"));

        // Информация о памяти
        Runtime runtime = Runtime.getRuntime();
        logger.info("Доступно процессоров: {}", runtime.availableProcessors());
        logger.info("Максимальная память: {} MB",
                runtime.maxMemory() / (1024 * 1024));
        logger.info("Всего памяти: {} MB",
                runtime.totalMemory() / (1024 * 1024));
        logger.info("Свободная память: {} MB",
                runtime.freeMemory() / (1024 * 1024));

        // Информация о кодировках
        logger.info("Кодировка файлов: {}", System.getProperty("file.encoding"));

        logger.info("=== Конец информации о системе ===");
    }

    /**
     * Преобразует количество байтов в гигабайты с двумя знаками после запятой.
     * <p>
     * Используется для форматирования информации о размере дисков.
     * При отрицательном или нулевом значении возвращает "0".
     * </p>
     *
     * @param bytes количество байт для преобразования
     * @return строка в формате "X.XX GB" или "0", если bytes ≤ 0
     *
     * <p><b>Примеры использования:</b></p>
     * <pre>
     * formatBytesToGB(1073741824) → "1.00 GB" (1 гигабайт)
     * formatBytesToGB(2147483648) → "2.00 GB" (2 гигабайта)
     * formatBytesToGB(5368709120) → "5.00 GB" (5 гигабайт)
     * formatBytesToGB(0) → "0"
     * formatBytesToGB(-1024) → "0"
     * </pre>
     */
    private static String formatBytesToGB(long bytes) {
        if (bytes <= 0) return "0";
        double gb = (double) bytes / (1024 * 1024 * 1024);
        return String.format("%.2f", gb);
    }

    /**
     * Тестовый метод для проверки всех уровней логирования.
     * <p>
     * Выводит тестовые сообщения на всех уровнях логирования (от TRACE до ERROR)
     * и создает тестовое исключение для демонстрации логирования ошибок.
     * Рекомендуется удалить этот метод после проверки конфигурации логирования.
     * </p>
     *
     * <p><b>Пример вывода:</b></p>
     * <pre>
     * TRACE - Это сообщение уровня TRACE
     * DEBUG - Это сообщение уровня DEBUG
     * INFO  - Это сообщение уровня INFO
     * WARN  - Это сообщение уровня WARN
     * ERROR - Это сообщение уровня ERROR
     * ERROR - Поймано исключение: Тестовая ошибка для логирования
     * </pre>
     *
     * <p><b>Применение:</b></p>
     * Используется для проверки правильности настройки log4j2.xml
     * и уровней логирования в различных средах выполнения.
     */
    private static void testLoggingLevels() {
        logger.trace("Это сообщение уровня TRACE");
        logger.debug("Это сообщение уровня DEBUG");
        logger.info("Это сообщение уровня INFO");
        logger.warn("Это сообщение уровня WARN");
        logger.error("Это сообщение уровня ERROR");

        try {
            // Искусственная ошибка для демонстрации
            throw new RuntimeException("Тестовая ошибка для логирования");
        } catch (RuntimeException e) {
            logger.error("Поймано исключение: {}", e.getMessage(), e);
        }
    }
}