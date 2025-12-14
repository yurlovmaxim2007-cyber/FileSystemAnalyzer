package com.filesystem.analyzer.model;

import java.time.LocalDateTime;

/**
 * Класс, представляющий информацию о файле или директории в файловой системе.
 * Содержит основные атрибуты, такие как имя, путь, размер, время создания и другие метаданные.
 * <p>
 * Этот класс используется для передачи информации между слоями приложения и
 * отображения в пользовательском интерфейсе. Поддерживает как файлы, так и директории.
 * </p>
 *
 * <p><b>Пример использования:</b></p>
 * <pre>{@code
 * FileInfo file = new FileInfo("document.pdf", "/path/to/document.pdf", 2048, false);
 * file.setOwner("user");
 * file.setCreationTime(LocalDateTime.now());
 * System.out.println(file.getFormattedSize()); // Выводит "2.0 KB"
 * }</pre>
 *
 * @author Максим
 * @version 1.0
 * @since 2024
 * @see java.time.LocalDateTime
 * @see com.filesystem.analyzer.utils.FileSystemScanner
 */
public class FileInfo {

    /** Имя файла или директории (без пути) */
    private String name;

    /** Относительный путь к файлу или директории */
    private String path;

    /** Абсолютный путь к файлу или директории */
    private String absolutePath;

    /** Размер в байтах */
    private long size;

    /** Флаг, указывающий, является ли объект директорией */
    private boolean isDirectory;

    /** Время создания файла или директории */
    private LocalDateTime creationTime;

    /** Время последнего изменения файла или директории */
    private LocalDateTime lastModifiedTime;

    /** Владелец файла или директории */
    private String owner;

    /** Расширение файла (только для файлов) */
    private String extension;

    /** Количество файлов в директории (только для директорий) */
    private int fileCount;

    /** Количество поддиректорий в директории (только для директорий) */
    private int directoryCount;

    /**
     * Конструктор по умолчанию.
     * <p>
     * Создает пустой объект FileInfo. Все поля инициализируются значениями по умолчанию:
     * строки - null, числа - 0, boolean - false.
     * </p>
     */
    public FileInfo() {}

    /**
     * Конструктор с основными параметрами.
     * <p>
     * Создает объект FileInfo с указанными основными атрибутами.
     * Остальные поля должны быть установлены с помощью сеттеров.
     * </p>
     *
     * @param name имя файла или директории
     * @param path путь к файлу или директории
     * @param size размер в байтах
     * @param isDirectory true если это директория, false если файл
     */
    public FileInfo(String name, String path, long size, boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.isDirectory = isDirectory;
    }

    /**
     * Возвращает имя файла или директории.
     *
     * @return имя файла/директории (без пути)
     */
    public String getName() { return name; }

    /**
     * Устанавливает имя файла или директории.
     *
     * @param name новое имя файла/директории
     * @throws NullPointerException если передано null (если это не разрешено логикой)
     */
    public void setName(String name) { this.name = name; }

    /**
     * Возвращает путь к файлу или директории.
     * <p>
     * Путь может быть относительным или абсолютным, в зависимости от контекста использования.
     * </p>
     *
     * @return путь к файлу/директории
     */
    public String getPath() { return path; }

    /**
     * Устанавливает путь к файлу или директории.
     *
     * @param path новый путь
     */
    public void setPath(String path) { this.path = path; }

    /**
     * Возвращает абсолютный путь к файлу или директории.
     * <p>
     * Абсолютный путь содержит полную информацию о местоположении в файловой системе.
     * Например, "C:\Users\Username\Documents\file.txt" на Windows.
     * </p>
     *
     * @return абсолютный путь к файлу/директории
     */
    public String getAbsolutePath() { return absolutePath; }

    /**
     * Устанавливает абсолютный путь к файлу или директории.
     *
     * @param absolutePath новый абсолютный путь
     */
    public void setAbsolutePath(String absolutePath) { this.absolutePath = absolutePath; }

    /**
     * Возвращает размер файла или директории в байтах.
     * <p>
     * Для директорий может быть:
     * </p>
     * <ul>
     * <li>0 - если размер не вычислен</li>
     * <li>Суммарный размер всех файлов в директории - после вычисления статистики</li>
     * </ul>
     *
     * @return размер в байтах
     */
    public long getSize() { return size; }

    /**
     * Устанавливает размер файла или директории в байтах.
     *
     * @param size новый размер в байтах
     * @throws IllegalArgumentException если size отрицательный
     */
    public void setSize(long size) { this.size = size; }

    /**
     * Проверяет, является ли объект директорией.
     *
     * @return true если это директория, false если файл
     */
    public boolean isDirectory() { return isDirectory; }

    /**
     * Устанавливает тип объекта (файл или директория).
     *
     * @param directory true для директории, false для файла
     */
    public void setDirectory(boolean directory) { isDirectory = directory; }

    /**
     * Возвращает время создания файла или директории.
     *
     * @return время создания, или null если информация недоступна
     */
    public LocalDateTime getCreationTime() { return creationTime; }

    /**
     * Устанавливает время создания файла или директории.
     *
     * @param creationTime время создания
     */
    public void setCreationTime(LocalDateTime creationTime) { this.creationTime = creationTime; }

    /**
     * Возвращает время последнего изменения файла или директории.
     *
     * @return время последнего изменения, или null если информация недоступна
     */
    public LocalDateTime getLastModifiedTime() { return lastModifiedTime; }

    /**
     * Устанавливает время последнего изменения файла или директории.
     *
     * @param lastModifiedTime время последнего изменения
     */
    public void setLastModifiedTime(LocalDateTime lastModifiedTime) { this.lastModifiedTime = lastModifiedTime; }

    /**
     * Возвращает владельца файла или директории.
     * <p>
     * Обычно это имя пользователя операционной системы, которому принадлежит файл.
     * </p>
     *
     * @return имя владельца, или null если информация недоступна
     */
    public String getOwner() { return owner; }

    /**
     * Устанавливает владельца файла или директории.
     *
     * @param owner имя владельца
     */
    public void setOwner(String owner) { this.owner = owner; }

    /**
     * Возвращает расширение файла.
     * <p>
     * Для директорий всегда возвращает null.
     * Для файлов возвращает расширение без точки (например, "txt", "pdf", "jpg").
     * </p>
     *
     * @return расширение файла, или null для директорий
     */
    public String getExtension() { return extension; }

    /**
     * Устанавливает расширение файла.
     * <p>
     * Расширение должно указываться без точки.
     * </p>
     *
     * @param extension расширение файла (без точки)
     */
    public void setExtension(String extension) { this.extension = extension; }

    /**
     * Возвращает количество файлов в директории.
     * <p>
     * Актуально только для объектов-директорий. Для файлов значение игнорируется.
     * </p>
     *
     * @return количество файлов в директории
     */
    public int getFileCount() { return fileCount; }

    /**
     * Устанавливает количество файлов в директории.
     *
     * @param fileCount количество файлов
     */
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }

    /**
     * Возвращает количество поддиректорий в директории.
     * <p>
     * Актуально только для объектов-директорий. Для файлов значение игнорируется.
     * </p>
     *
     * @return количество поддиректорий в директории
     */
    public int getDirectoryCount() { return directoryCount; }

    /**
     * Устанавливает количество поддиректорий в директории.
     *
     * @param directoryCount количество поддиректорий
     */
    public void setDirectoryCount(int directoryCount) { this.directoryCount = directoryCount; }

    /**
     * Возвращает отформатированное представление размера файла или директории.
     * <p>
     * Использует статический метод {@link #formatSize(long)} для форматирования.
     * Автоматически выбирает наиболее подходящую единицу измерения (B, KB, MB, GB и т.д.).
     * </p>
     *
     * @return отформатированная строка размера
     * @see #formatSize(long)
     */
    public String getFormattedSize() {
        return formatSize(size);
    }

    /**
     * Статический метод для форматирования размера в байтах в читаемую строку.
     * <p>
     * Преобразует байты в более крупные единицы измерения с одним десятичным знаком.
     * Поддерживает единицы от байтов до эксабайтов.
     * </p>
     *
     * @param bytes размер в байтах
     * @return отформатированная строка размера
     *
     * <p><b>Примеры:</b></p>
     * <ul>
     * <li>0 → "0 B"</li>
     * <li>500 → "500 B"</li>
     * <li>1536 → "1.5 KB"</li>
     * <li>1048576 → "1.0 MB"</li>
     * <li>1073741824 → "1.0 GB"</li>
     * <li>1099511627776L → "1.0 TB"</li>
     * </ul>
     *
     * @throws IllegalArgumentException если bytes отрицательный
     */
    public static String formatSize(long bytes) {
        if (bytes == 0) return "0 B";
        if (bytes < 0) return "N/A";

        final String[] units = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Возвращает строковое представление объекта FileInfo.
     * <p>
     * Включает основную информацию: имя, путь, размер и тип (файл/директория).
     * Используется в основном для отладки и логирования.
     * </p>
     *
     * @return строковое представление в формате:
     *         "FileInfo{name='имя', path='путь', size=размер, isDirectory=тип}"
     */
    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", isDirectory=" + isDirectory +
                '}';
    }
}