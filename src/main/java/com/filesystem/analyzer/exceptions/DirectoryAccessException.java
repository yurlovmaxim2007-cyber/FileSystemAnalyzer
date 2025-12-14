package com.filesystem.analyzer.exceptions;
/**
 * Исключение, выбрасываемое при ошибках доступа к директориям.
 * Наследуется от RuntimeException для удобства использования.
 *
 * @author [Максим]
 * @version 1.0
 * @since 2024
 */
public class DirectoryAccessException extends Exception {
    /**
     * Создает исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public DirectoryAccessException(String message) {
        super(message);
    }
    /**
     * Создает исключение с указанным сообщением и причиной.
     *
     * @param message описание ошибки
     * @param cause причина исключения
     */
    public DirectoryAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}