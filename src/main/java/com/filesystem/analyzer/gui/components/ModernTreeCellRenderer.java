package com.filesystem.analyzer.gui.components;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Пользовательский рендерер для отображения элементов дерева файловой системы.
 * <p>
 * Расширяет {@link DefaultTreeCellRenderer} для предоставления кастомного внешнего вида
 * элементов дерева с использованием системных иконок и дополнительной информации.
 * </p>
 * <p>
 * Класс автоматически определяет тип файлового объекта (диск, директория, файл)
 * и устанавливает соответствующие иконки и подсказки.
 * </p>
 *
 * @author [Максим]
 * @version 1.0
 * @since 2024
 * @see javax.swing.tree.DefaultTreeCellRenderer
 * @see javax.swing.JTree
 * @see com.filesystem.analyzer.model.FileInfo
 */
public class ModernTreeCellRenderer extends DefaultTreeCellRenderer {

    /** Иконка для папок/директорий */
    private final Icon folderIcon;

    /** Иконка для файлов по умолчанию */
    private final Icon fileIcon;

    /** Иконка для дисков/корневых директорий */
    private final Icon driveIcon;

    /**
     * Создает новый рендерер с настройкой системных иконок.
     * <p>
     * Инициализирует иконки из системы, используя UIManager для получения
     * стандартных иконок файлов и папок. При отсутствии системной иконки диска
     * создает простую иконку самостоятельно.
     * </p>
     *
     * <p><b>Настройки внешнего вида:</b></p>
     * <ul>
     * <li>Цвет текста в невыбранном состоянии: черный</li>
     * <li>Цвет текста в выбранном состоянии: белый</li>
     * <li>Цвет фона выбранного элемента: синий (#0078D7)</li>
     * <li>Цвет границы выбранного элемента: синий (#0078D7)</li>
     * </ul>
     */
    public ModernTreeCellRenderer() {
        // Используем системные иконки
        folderIcon = UIManager.getIcon("FileView.directoryIcon");
        fileIcon = UIManager.getIcon("FileView.fileIcon");

        // Получаем иконку диска и проверяем ее
        Icon systemDriveIcon = UIManager.getIcon("FileChooser.hardDriveIcon");

        // Используем системную иконку если она есть, иначе создаем свою
        if (systemDriveIcon != null) {
            driveIcon = systemDriveIcon;
        } else {
            driveIcon = createSimpleDriveIcon();
        }

        // Настройка отступов
        setTextNonSelectionColor(Color.BLACK);
        setTextSelectionColor(Color.WHITE);
        setBackgroundSelectionColor(new Color(0, 120, 215));
        setBorderSelectionColor(new Color(0, 120, 215));
        setBackgroundNonSelectionColor(null);
    }

    /**
     * Возвращает компонент для отрисовки ячейки дерева.
     * <p>
     * Переопределяет метод родительского класса для предоставления
     * кастомного отображения элементов дерева в зависимости от типа данных.
     * </p>
     *
     * @param tree дерево, для которого отрисовывается ячейка
     * @param value значение ячейки (обычно {@link DefaultMutableTreeNode})
     * @param selected {@code true} если ячейка выбрана
     * @param expanded {@code true} если узел развернут
     * @param leaf {@code true} если узел является листом
     * @param row номер строки в дереве
     * @param hasFocus {@code true} если ячейка имеет фокус
     * @return компонент для отрисовки ячейки
     *
     * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean)
     *
     * <p><b>Логика отображения:</b></p>
     * <ul>
     * <li>Для строковых объектов: отображает иконку компьютера</li>
     * <li>Для объектов FileInfo:
     *   <ul>
     *   <li>Диски: иконка диска</li>
     *   <li>Папки: иконка папки</li>
     *   <li>Файлы: иконка файла или специальная иконка для определенных расширений</li>
     *   </ul>
     * </li>
     * </ul>
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            // Определяем иконку в зависимости от типа объекта
            if (userObject instanceof String) {
                String text = (String) userObject;
                if (text.equals("Компьютер") || text.contains("Computer")) {
                    setIcon(UIManager.getIcon("FileChooser.computerIcon"));
                }
            } else if (userObject instanceof com.filesystem.analyzer.model.FileInfo) {
                com.filesystem.analyzer.model.FileInfo fileInfo =
                        (com.filesystem.analyzer.model.FileInfo) userObject;

                if (fileInfo.isDirectory()) {
                    // Для корневых директорий (дисков) используем иконку диска
                    if (fileInfo.getPath().matches("[A-Z]:\\\\?")) {
                        setIcon(driveIcon);
                    } else {
                        setIcon(folderIcon);
                    }
                } else {
                    setIcon(fileIcon);
                    // Можно добавить иконки для разных типов файлов
                    if (fileInfo.getExtension() != null) {
                        setIcon(getIconForExtension(fileInfo.getExtension()));
                    }
                }

                // Добавляем информацию о размере для файлов
                if (!fileInfo.isDirectory() && fileInfo.getSize() > 0) {
                    setText(fileInfo.getName() + " (" + fileInfo.getFormattedSize() + ")");
                } else {
                    setText(fileInfo.getName());
                }

                // Всплывающая подсказка с полным путем
                setToolTipText(fileInfo.getAbsolutePath());
            }
        }

        return this;
    }

    /**
     * Возвращает иконку для конкретного расширения файла.
     * <p>
     * Метод определяет наиболее подходящую иконку на основе расширения файла.
     * В текущей реализации используется базовая логика, которую можно расширить.
     * </p>
     *
     * @param extension расширение файла (без точки)
     * @return иконка для данного типа файла или стандартная иконка файла по умолчанию
     *
     * <p><b>Поддерживаемые расширения:</b></p>
     * <ul>
     * <li>Изображения (jpg, jpeg, png, gif, bmp): временно использует иконку дискеты</li>
     * <li>PDF файлы: временно использует иконку новой папки</li>
     * <li>Остальные: стандартная иконка файла</li>
     * </ul>
     *
     * @see #fileIcon
     */
    private Icon getIconForExtension(String extension) {
        // Простая реализация - можно расширить
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return UIManager.getIcon("FileView.floppyDriveIcon"); // Временно
            case "pdf":
                return UIManager.getIcon("FileChooser.newFolderIcon"); // Временно
            default:
                return fileIcon;
        }
    }

    /**
     * Создает простую пользовательскую иконку для диска.
     * <p>
     * Используется в случае, если системная иконка диска недоступна.
     * Рисует синий квадрат с белой буквой "D" внутри.
     * </p>
     *
     * @return простая иконка диска размером 16x16 пикселей
     *
     * <p><b>Визуальное представление:</b></p>
     * <pre>
     * ┌────────────────┐
     * │                │
     * │      ████      │
     * │     ██████     │
     * │    ██ D ██     │
     * │     ██████     │
     * │      ████      │
     * │                │
     * └────────────────┘
     * </pre>
     */
    private Icon createSimpleDriveIcon() {
        // Создаем простую иконку диска
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(new Color(0, 120, 215));
                g.fillRect(x, y, 16, 16);
                g.setColor(Color.WHITE);
                g.drawString("D", x + 5, y + 12);
            }

            /**
             * Возвращает ширину иконки.
             *
             * @return ширина иконки в пикселях
             */
            @Override
            public int getIconWidth() { return 16; }

            /**
             * Возвращает высоту иконки.
             *
             * @return высота иконки в пикселях
             */
            @Override
            public int getIconHeight() { return 16; }
        };
    }
}