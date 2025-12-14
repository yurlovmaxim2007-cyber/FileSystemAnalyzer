package com.filesystem.analyzer.gui;

import com.filesystem.analyzer.gui.components.ModernTreeCellRenderer;
import com.filesystem.analyzer.model.FileInfo;
import com.filesystem.analyzer.utils.FileSystemScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * Главное окно приложения "Анализатор файловой системы".
 * <p>
 * Предоставляет графический интерфейс пользователя для навигации по файловой системе,
 * отображения информации о файлах и директориях, а также выполнения операций сканирования.
 * </p>
 *
 * <p>Окно разделено на несколько областей:</p>
 * <ul>
 * <li>Верхняя панель с полем пути и кнопками управления</li>
 * <li>Центральная область с разделителем: слева - дерево файлов, справа - детальная информация</li>
 * <li>Нижняя панель статуса</li>
 * </ul>
 *
 * @author Максим
 * @version 1.0
 * @since 2024
 * @see javax.swing.JFrame
 * @see com.filesystem.analyzer.model.FileInfo
 * @see com.filesystem.analyzer.utils.FileSystemScanner
 */
public class MainFrame extends JFrame {

    /** Логгер для класса MainFrame */
    private static final Logger logger = LogManager.getLogger(MainFrame.class);

    /** Дерево для отображения файловой структуры */
    private JTree fileTree;

    /** Текстовая область для отображения детальной информации о выбранном файле */
    private JTextArea infoArea;

    /** Поле ввода пути для сканирования */
    private JTextField pathField;

    /** Кнопка для запуска сканирования */
    private JButton scanButton;

    /** Кнопка для обновления дерева файлов */
    private JButton refreshButton;

    /** Сканер файловой системы */
    private FileSystemScanner scanner;

    /** Модель данных для дерева файлов */
    private DefaultTreeModel treeModel;

    /**
     * Конструктор главного окна приложения.
     * <p>
     * Инициализирует все компоненты интерфейса, настраивает обработчики событий
     * и загружает корневые директории файловой системы.
     * </p>
     *
     * <p><b>Последовательность инициализации:</b></p>
     * <ol>
     * <li>Инициализация FileSystemScanner</li>
     * <li>Создание и настройка компонентов интерфейса (initComponents)</li>
     * <li>Настройка компоновки (setupLayout)</li>
     * <li>Настройка обработчиков событий (setupListeners)</li>
     * <li>Загрузка корневых директорий (loadRoots)</li>
     * </ol>
     */
    public MainFrame() {
        logger.info("Инициализация главного окна приложения");
        scanner = new FileSystemScanner();
        initComponents();
        setupLayout();
        setupListeners();
        loadRoots();
        logger.info("Главное окно успешно создано");
    }

    /**
     * Инициализирует компоненты графического интерфейса.
     * <p>
     * Создает все необходимые элементы управления:
     * </p>
     * <ul>
     * <li>Поле ввода пути (устанавливает значение по умолчанию - домашняя директория пользователя)</li>
     * <li>Кнопки "Сканировать" и "Обновить"</li>
     * <li>Дерево файлов с кастомным рендерером</li>
     * <li>Текстовую область для отображения информации</li>
     * </ul>
     *
     * <p><b>Логирование:</b></p>
     * Метод логирует процесс инициализации каждого компонента для отладки.
     */
    private void initComponents() {
        logger.debug("Инициализация компонентов интерфейса");

        // Поле пути
        pathField = new JTextField(30);
        String userHome = System.getProperty("user.home");
        pathField.setText(userHome);
        logger.debug("Поле пути установлено на: {}", userHome);

        // Кнопки
        scanButton = new JButton("Сканировать");
        refreshButton = new JButton("Обновить");

        // Дерево файлов
        fileTree = new JTree();
        fileTree.setCellRenderer(new ModernTreeCellRenderer());
        fileTree.setRootVisible(false);
        fileTree.setShowsRootHandles(true);
        logger.debug("Дерево файлов инициализировано");

        // Область информации
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logger.debug("Область информации инициализирована");
    }

    /**
     * Настраивает компоновку элементов интерфейса.
     * <p>
     * Использует BorderLayout для размещения компонентов:
     * </p>
     * <ul>
     * <li>NORTH: верхняя панель с полем пути и кнопками</li>
     * <li>CENTER: разделитель с деревом файлов (слева) и областью информации (справа)</li>
     * <li>SOUTH: нижняя панель статуса</li>
     * </ul>
     *
     * <p><b>Параметры разделителя:</b></p>
     * Разделитель установлен в позиции 400 пикселей от левого края.
     *
     * <p><b>Размер окна:</b></p>
     * Устанавливает предпочтительный размер окна 1200x800 пикселей.
     */
    private void setupLayout() {
        logger.debug("Настройка компоновки интерфейса");

        setLayout(new BorderLayout());

        // Верхняя панель
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Путь:"));
        topPanel.add(pathField);
        topPanel.add(scanButton);
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);
        logger.debug("Верхняя панель с кнопками создана");

        // Центральная панель с разделителем
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(fileTree));
        splitPane.setRightComponent(new JScrollPane(infoArea));
        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);
        logger.debug("Центральная панель с разделителем создана");

        // Нижняя панель статуса
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(new JLabel(" Готово", SwingConstants.LEFT), BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1200, 800));
        logger.debug("Размер окна установлен: 1200x800");
    }

    /**
     * Настраивает обработчики событий для компонентов интерфейса.
     * <p>
     * Добавляет слушатели для следующих событий:
     * </p>
     * <ul>
     * <li>Кнопка "Сканировать" - запускает сканирование указанного пути</li>
     * <li>Кнопка "Обновить" - обновляет дерево файлов</li>
     * <li>Выбор элемента в дереве - отображает информацию о выбранном файле</li>
     * <li>Раскрытие узла дерева - загружает содержимое директории</li>
     * <li>Сворачивание узла дерева - логирует событие</li>
     * </ul>
     *
     * <p><b>Особенности реализации:</b></p>
     * Для раскрытия узлов используется ленивая загрузка (lazy loading) - содержимое
     * директории загружается только при первом раскрытии узла.
     */
    private void setupListeners() {
        logger.debug("Настройка обработчиков событий");

        scanButton.addActionListener(e -> {
            logger.info("Нажата кнопка 'Сканировать'");
            scanDirectory();
        });

        refreshButton.addActionListener(e -> {
            logger.info("Нажата кнопка 'Обновить'");
            refreshTree();
        });

        fileTree.addTreeSelectionListener(e -> {
            TreePath path = fileTree.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObject = node.getUserObject();
                if (userObject instanceof FileInfo) {
                    logger.debug("Пользователь выбрал элемент: {}", ((FileInfo) userObject).getName());
                    displayFileInfo((FileInfo) userObject);
                } else {
                    logger.trace("Выбран системный узел: {}", userObject);
                }
            }
        });

        fileTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObject = node.getUserObject();
                if (userObject instanceof FileInfo) {
                    logger.debug("Раскрытие папки: {}", ((FileInfo) userObject).getName());
                }
                loadChildren(node);
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObject = node.getUserObject();
                if (userObject instanceof FileInfo) {
                    logger.debug("Сворачивание папки: {}", ((FileInfo) userObject).getName());
                }
            }
        });

        logger.debug("Обработчики событий успешно настроены");
    }

    /**
     * Загружает содержимое директории для указанного узла дерева.
     * <p>
     * Выполняет ленивую загрузку содержимого директории при первом раскрытии узла.
     * Если узел уже был загружен ранее, метод не выполняет повторную загрузку.
     * </p>
     *
     * @param parentNode узел дерева, представляющий директорию
     *
     * <p><b>Алгоритм работы:</b></p>
     * <ol>
     * <li>Проверяет, является ли объект узла экземпляром FileInfo</li>
     * <li>Проверяет, является ли FileInfo директорией</li>
     * <li>Проверяет, не загружены ли уже дочерние элементы (проверка на наличие узла "Загрузка...")</li>
     * <li>Запускает фоновую задачу SwingWorker для сканирования директории</li>
     * <li>После загрузки обновляет дерево, заменяя узел "Загрузка..." на реальное содержимое</li>
     * </ol>
     *
     * @see SwingWorker
     * @see FileSystemScanner#scanDirectory(String)
     */
    private void loadChildren(DefaultMutableTreeNode parentNode) {
        logger.trace("Загрузка содержимого узла дерева");
        Object userObject = parentNode.getUserObject();

        if (userObject instanceof FileInfo) {
            FileInfo fileInfo = (FileInfo) userObject;

            if (fileInfo.isDirectory()) {
                logger.debug("Загрузка содержимого папки: {}", fileInfo.getName());

                // Проверяем, не загружены ли уже дети
                if (parentNode.getChildCount() == 1) {
                    DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) parentNode.getChildAt(0);
                    if (firstChild.getUserObject() instanceof String &&
                            firstChild.getUserObject().equals("Загрузка...")) {

                        SwingWorker<List<FileInfo>, Void> worker = new SwingWorker<>() {
                            @Override
                            protected List<FileInfo> doInBackground() throws Exception {
                                logger.trace("Фоновая загрузка содержимого папки: {}", fileInfo.getPath());
                                return scanner.scanDirectory(fileInfo.getPath());
                            }

                            @Override
                            protected void done() {
                                try {
                                    List<FileInfo> children = get();
                                    logger.debug("Загружено {} элементов из папки {}", children.size(), fileInfo.getName());

                                    parentNode.removeAllChildren();

                                    for (FileInfo child : children) {
                                        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
                                        if (child.isDirectory()) {
                                            childNode.add(new DefaultMutableTreeNode("Загрузка..."));
                                        }
                                        parentNode.add(childNode);
                                        logger.trace("Добавлен элемент: {}", child.getName());
                                    }

                                    treeModel.reload(parentNode);
                                    logger.info("Содержимое папки '{}' успешно загружено ({} элементов)",
                                            fileInfo.getName(), children.size());

                                } catch (Exception e) {
                                    logger.error("Ошибка при загрузке содержимого папки {}: {}",
                                            fileInfo.getName(), e.getMessage(), e);
                                    parentNode.removeAllChildren();
                                    parentNode.add(new DefaultMutableTreeNode("Ошибка загрузки: " + e.getMessage()));
                                    treeModel.reload(parentNode);
                                }
                            }
                        };

                        worker.execute();
                    } else {
                        logger.trace("Содержимое папки уже загружено");
                    }
                }
            }
        }
    }

    /**
     * Загружает корневые директории (диски) файловой системы.
     * <p>
     * Создает корневой узел "Компьютер" и добавляет в него все доступные диски.
     * Для каждого диска создается узел с информацией и заглушкой "Загрузка...".
     * </p>
     *
     * <p><b>Структура создаваемого дерева:</b></p>
     * <pre>
     * Компьютер
     * ├── C:\ (Локальный диск)
     * │   └── Загрузка...
     * ├── D:\ (Локальный диск)
     * │   └── Загрузка...
     * └── E:\ (Диск)
     *     └── Загрузка...
     * </pre>
     *
     * <p><b>Логирование:</b></p>
     * Метод логирует количество найденных дисков и информацию о каждом из них.
     */
    private void loadRoots() {
        logger.info("Загрузка списка корневых дисков");

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Компьютер");

        File[] roots = File.listRoots();
        if (roots == null || roots.length == 0) {
            logger.warn("Не найдено доступных дисков");
        } else {
            logger.info("Найдено {} дисков", roots.length);

            for (File file : roots) {
                FileInfo fileInfo = new FileInfo();
                String driveName = file.getPath() + " (" + getDriveName(file) + ")";
                fileInfo.setName(driveName);
                fileInfo.setPath(file.getPath());
                fileInfo.setAbsolutePath(file.getAbsolutePath());
                fileInfo.setDirectory(true);
                fileInfo.setSize(0); // Размер будет вычислен при выборе

                DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileInfo);
                node.add(new DefaultMutableTreeNode("Загрузка..."));
                root.add(node);

                logger.debug("Добавлен диск: {} ({})", file.getPath(), getDriveName(file));
            }
        }

        treeModel = new DefaultTreeModel(root);
        fileTree.setModel(treeModel);
        logger.info("Модель дерева дисков создана");
    }

    /**
     * Определяет отображаемое имя для диска на основе его пути.
     * <p>
     * Для Windows-дисков (например, C:\) возвращает "Локальный диск",
     * для остальных - "Диск".
     * </p>
     *
     * @param drive объект File, представляющий диск
     * @return отображаемое имя диска
     *
     * <p><b>Примеры:</b></p>
     * <ul>
     * <li>C:\ → "Локальный диск"</li>
     * <li>D:\ → "Локальный диск"</li>
     * <li>/mnt/data → "Диск"</li>
     * </ul>
     */
    private String getDriveName(File drive) {
        try {
            String path = drive.getPath();
            if (path.length() >= 2 && path.charAt(1) == ':') {
                return "Локальный диск";
            }
        } catch (Exception e) {
            logger.warn("Ошибка при определении типа диска {}: {}", drive.getPath(), e.getMessage());
        }
        return "Диск";
    }

    /**
     * Выполняет сканирование директории по указанному пути.
     * <p>
     * Запускает сканирование в фоновом потоке для предотвращения блокировки интерфейса.
     * Во время сканирования показывает курсор ожидания и отключает кнопку сканирования.
     * </p>
     *
     * <p><b>Валидация:</b></p>
     * <ul>
     * <li>Проверяет, что путь не пустой</li>
     * <li>При пустом пути показывает предупреждение</li>
     * </ul>
     *
     * <p><b>Обработка результатов:</b></p>
     * <ul>
     * <li>При успешном сканировании обновляет дерево файлов</li>
     * <li>При ошибке показывает диалоговое окно с сообщением об ошибке</li>
     * <li>В любом случае восстанавливает состояние интерфейса</li>
     * </ul>
     *
     * @see SwingWorker
     * @see #displayFilesInTree(List, String)
     */
    private void scanDirectory() {
        String path = pathField.getText().trim();
        if (path.isEmpty()) {
            logger.warn("Попытка сканирования с пустым путем");
            JOptionPane.showMessageDialog(this,
                    "Введите путь для сканирования",
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        logger.info("Начало сканирования директории: {}", path);

        // Показываем индикатор загрузки
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        scanButton.setEnabled(false);
        logger.debug("Установлен курсор ожидания, кнопка сканирования отключена");

        SwingWorker<List<FileInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<FileInfo> doInBackground() throws Exception {
                logger.debug("Фоновое сканирование директории: {}", path);
                return scanner.scanDirectory(path);
            }

            @Override
            protected void done() {
                try {
                    List<FileInfo> files = get();
                    logger.info("Сканирование завершено. Найдено {} элементов в {}", files.size(), path);
                    displayFilesInTree(files, path);
                } catch (Exception e) {
                    logger.error("Ошибка при сканировании пути {}: {}", path, e.getMessage(), e);
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Ошибка при сканировании: " + e.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                    scanButton.setEnabled(true);
                    logger.debug("Курсор восстановлен, кнопка сканирования включена");
                }
            }
        };

        worker.execute();
    }

    /**
     * Отображает список файлов в дереве файловой системы.
     * <p>
     * Создает структуру дерева для отображения результатов сканирования:
     * </p>
     * <ul>
     * <li>Родительский узел "Результаты сканирования"</li>
     * <li>Узел с информацией о сканируемом пути</li>
     * <li>Узлы для каждого найденного файла и директории</li>
     * <li>Узел со статистикой сканирования</li>
     * </ul>
     *
     * @param files список объектов FileInfo для отображения
     * @param parentPath путь сканируемой директории
     *
     * <p><b>Особенности форматирования:</b></p>
     * <ul>
     * <li>Для файлов добавляет информацию о размере в скобках</li>
     * <li>Для директорий добавляет заглушку "Загрузка..."</li>
     * <li>Вычисляет статистику: количество папок, файлов и общий размер</li>
     * </ul>
     *
     * @see #formatSize(long)
     */
    private void displayFilesInTree(List<FileInfo> files, String parentPath) {
        logger.debug("Отображение {} элементов в дереве для пути: {}", files.size(), parentPath);

        // Создаем родительский узел
        DefaultMutableTreeNode parent = new DefaultMutableTreeNode("Результаты сканирования");

        // Добавляем информацию о родительской папке
        FileInfo parentInfo = new FileInfo();
        parentInfo.setName("Путь: " + parentPath);
        parentInfo.setPath(parentPath);
        parentInfo.setDirectory(true);
        parentInfo.setSize(files.stream().mapToLong(FileInfo::getSize).sum());

        DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode(parentInfo);
        parent.add(parentNode);

        int folderCount = 0;
        int fileCount = 0;
        long totalSize = 0;

        for (FileInfo file : files) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
            if (file.isDirectory()) {
                node.add(new DefaultMutableTreeNode("Загрузка..."));
                folderCount++;
            } else {
                fileCount++;
                totalSize += file.getSize();
            }
            parentNode.add(node);
        }

        // Добавляем узел со статистикой
        FileInfo statsInfo = new FileInfo();
        statsInfo.setName(String.format("Статистика: %d папок, %d файлов, общий размер: %s",
                folderCount, fileCount, formatSize(totalSize)));
        parent.add(new DefaultMutableTreeNode(statsInfo));

        treeModel = new DefaultTreeModel(parent);
        fileTree.setModel(treeModel);

        // Раскрываем первый уровень
        fileTree.expandRow(0);
        if (parentNode.getChildCount() > 0) {
            fileTree.expandRow(1);
        }

        logger.info("Дерево обновлено. Папок: {}, Файлов: {}, Общий размер: {} байт",
                folderCount, fileCount, totalSize);
    }

    /**
     * Форматирует размер в байтах в читаемую строку.
     * <p>
     * Автоматически выбирает подходящую единицу измерения:
     * </p>
     * <ul>
     * <li>Менее 1 KB: отображается в байтах</li>
     * <li>1 KB - 1 MB: отображается в килобайтах с одним десятичным знаком</li>
     * <li>1 MB - 1 GB: отображается в мегабайтах с одним десятичным знаком</li>
     * <li>Более 1 GB: отображается в гигабайтах с одним десятичным знаком</li>
     * </ul>
     *
     * @param bytes размер в байтах
     * @return отформатированная строка размера
     *
     * <p><b>Примеры:</b></p>
     * <ul>
     * <li>500 → "500 B"</li>
     * <li>1536 → "1.5 KB"</li>
     * <li>1572864 → "1.5 MB"</li>
     * <li>1610612736 → "1.5 GB"</li>
     * </ul>
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * Отображает детальную информацию о выбранном файле в правой панели.
     * <p>
     * Выводит следующую информацию:
     * </p>
     * <ul>
     * <li>Имя файла</li>
     * <li>Путь</li>
     * <li>Полный путь (если доступен)</li>
     * <li>Тип (файл или папка)</li>
     * <li>Размер (для папок запускает асинхронный расчет статистики)</li>
     * <li>Владелец (если известен)</li>
     * <li>Время создания</li>
     * <li>Время последнего изменения</li>
     * <li>Расширение (для файлов)</li>
     * </ul>
     *
     * @param fileInfo объект с информацией о файле
     *
     * @see #updateDirectoryStats(FileInfo)
     */
    private void displayFileInfo(FileInfo fileInfo) {
        logger.debug("Отображение информации о файле: {}", fileInfo.getName());

        StringBuilder info = new StringBuilder();
        info.append("=== Информация о файле ===\n\n");
        info.append("Имя: ").append(fileInfo.getName()).append("\n");
        info.append("Путь: ").append(fileInfo.getPath()).append("\n");
        if (fileInfo.getAbsolutePath() != null) {
            info.append("Полный путь: ").append(fileInfo.getAbsolutePath()).append("\n");
        }
        info.append("Тип: ").append(fileInfo.isDirectory() ? "Папка" : "Файл").append("\n");

        if (fileInfo.isDirectory()) {
            // Для директории показываем, что размер вычисляется
            info.append("Размер: вычисляется...\n");
            info.append("Файлов: вычисляется...\n");
            info.append("Папок: вычисляется...\n");

            // Запускаем вычисление статистики в фоне
            updateDirectoryStats(fileInfo);
        } else {
            // Для файла сразу показываем размер
            info.append("Размер: ").append(fileInfo.getFormattedSize()).append("\n");
        }

        if (fileInfo.getOwner() != null && !fileInfo.getOwner().equals("Неизвестно")) {
            info.append("Владелец: ").append(fileInfo.getOwner()).append("\n");
        }

        if (fileInfo.getCreationTime() != null) {
            info.append("Создан: ").append(fileInfo.getCreationTime()).append("\n");
        }

        if (fileInfo.getLastModifiedTime() != null) {
            info.append("Изменен: ").append(fileInfo.getLastModifiedTime()).append("\n");
        }

        if (fileInfo.getExtension() != null) {
            info.append("Расширение: .").append(fileInfo.getExtension()).append("\n");
        }

        infoArea.setText(info.toString());
        logger.trace("Информация о файле отображена в правой панели");
    }

    /**
     * Асинхронно обновляет статистику для выбранной директории.
     * <p>
     * Запускает расчет статистики (размер, количество файлов и поддиректорий)
     * в фоновом потоке. После завершения обновляет отображаемую информацию.
     * </p>
     *
     * @param fileInfo объект FileInfo, представляющий директорию
     *
     * @see FileSystemScanner#getDirectoryStats(java.nio.file.Path)
     * @see SwingWorker
     */
    private void updateDirectoryStats(FileInfo fileInfo) {
        if (!fileInfo.isDirectory()) return;

        logger.debug("Запуск расчета статистики для папки: {}", fileInfo.getName());

        SwingWorker<FileSystemScanner.DirectoryStats, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected FileSystemScanner.DirectoryStats doInBackground() throws Exception {
                        logger.trace("Фоновый расчет статистики для: {}", fileInfo.getPath());
                        return scanner.getDirectoryStats(Paths.get(fileInfo.getPath()));
                    }

                    @Override
                    protected void done() {
                        try {
                            FileSystemScanner.DirectoryStats stats = get();
                            logger.debug("Статистика рассчитана: файлов={}, папок={}, размер={} байт",
                                    stats.getFileCount(), stats.getDirectoryCount(), stats.getTotalSize());

                            // Обновляем информацию в объекте FileInfo
                            fileInfo.setSize(stats.getTotalSize());
                            fileInfo.setFileCount(stats.getFileCount());
                            fileInfo.setDirectoryCount(stats.getDirectoryCount());

                            // Обновляем отображение, если этот файл все еще выбран
                            TreePath currentPath = fileTree.getSelectionPath();
                            if (currentPath != null) {
                                DefaultMutableTreeNode currentNode =
                                        (DefaultMutableTreeNode) currentPath.getLastPathComponent();
                                if (currentNode.getUserObject() == fileInfo) {
                                    displayUpdatedFileInfo(fileInfo);
                                    logger.debug("Информация о папке обновлена в интерфейсе");
                                }
                            }

                        } catch (Exception e) {
                            logger.error("Ошибка при расчете статистики папки {}: {}",
                                    fileInfo.getName(), e.getMessage(), e);
                            // В случае ошибки показываем сообщение
                            infoArea.append("\n\nОшибка расчета статистики: " + e.getMessage());
                        }
                    }
                };

        worker.execute();
    }

    /**
     * Обновляет отображение информации о файле после расчета статистики.
     * <p>
     * Используется для обновления правой панели после получения статистики
     * для директории. Заменяет сообщения "вычисляется..." на реальные данные.
     * </p>
     *
     * @param fileInfo объект FileInfo с обновленной статистикой
     *
     * @see #displayFileInfo(FileInfo)
     */
    private void displayUpdatedFileInfo(FileInfo fileInfo) {
        logger.trace("Обновление отображения информации о файле");

        StringBuilder info = new StringBuilder();
        info.append("=== Информация о файле ===\n\n");
        info.append("Имя: ").append(fileInfo.getName()).append("\n");
        info.append("Путь: ").append(fileInfo.getPath()).append("\n");
        if (fileInfo.getAbsolutePath() != null) {
            info.append("Полный путь: ").append(fileInfo.getAbsolutePath()).append("\n");
        }
        info.append("Тип: ").append(fileInfo.isDirectory() ? "Папка" : "Файл").append("\n");

        if (fileInfo.isDirectory()) {
            info.append("Размер: ").append(fileInfo.getFormattedSize()).append("\n");
            info.append("Файлов: ").append(fileInfo.getFileCount()).append("\n");
            info.append("Папок: ").append(fileInfo.getDirectoryCount()).append("\n");
        } else {
            info.append("Размер: ").append(fileInfo.getFormattedSize()).append("\n");
        }

        if (fileInfo.getOwner() != null && !fileInfo.getOwner().equals("Неизвестно")) {
            info.append("Владелец: ").append(fileInfo.getOwner()).append("\n");
        }

        if (fileInfo.getCreationTime() != null) {
            info.append("Создан: ").append(fileInfo.getCreationTime()).append("\n");
        }

        if (fileInfo.getLastModifiedTime() != null) {
            info.append("Изменен: ").append(fileInfo.getLastModifiedTime()).append("\n");
        }

        if (fileInfo.getExtension() != null) {
            info.append("Расширение: .").append(fileInfo.getExtension()).append("\n");
        }

        infoArea.setText(info.toString());
    }

    /**
     * Обновляет дерево файлов.
     * <p>
     * Выполняет следующие действия:
     * </p>
     * <ol>
     * <li>Сохраняет текущий путь из поля ввода</li>
     * <li>Очищает область информации</li>
     * <li>Перезагружает список корневых директорий</li>
     * <li>Восстанавливает сохраненный путь в поле ввода</li>
     * <li>Показывает информационное сообщение об успешном обновлении</li>
     * </ol>
     *
     * <p><b>Использование:</b></p>
     * Метод вызывается при нажатии кнопки "Обновить" или может быть
     * использован для программного обновления дерева.
     */
    private void refreshTree() {
        logger.info("Обновление дерева файлов");

        // Сохраняем текущий путь
        String currentPath = pathField.getText();

        // Очищаем дерево и информацию
        infoArea.setText("");
        logger.debug("Область информации очищена");

        // Перезагружаем список дисков
        loadRoots();

        // Восстанавливаем путь в поле ввода
        pathField.setText(currentPath);

        logger.info("Дерево файлов успешно обновлено. Текущий путь: {}", currentPath);

        // Показываем сообщение о успешном обновлении
        JOptionPane.showMessageDialog(this,
                "Дерево файлов успешно обновлено!",
                "Обновление завершено",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Корректно завершает работу окна и освобождает ресурсы.
     * <p>
     * Переопределяет метод dispose() для обеспечения корректного завершения
     * работы сканера файловой системы перед закрытием окна.
     * </p>
     *
     * <p><b>Последовательность действий:</b></p>
     * <ol>
     * <li>Логирует начало закрытия окна</li>
     * <li>Останавливает FileSystemScanner (если он существует)</li>
     * <li>Вызывает dispose() родительского класса</li>
     * <li>Логирует успешное закрытие окна</li>
     * </ol>
     *
     * @see FileSystemScanner#shutdown()
     */
    @Override
    public void dispose() {
        logger.info("Закрытие главного окна приложения");

        // Корректно завершаем сканер
        if (scanner != null) {
            logger.debug("Завершение работы FileSystemScanner");
            scanner.shutdown();
        }

        super.dispose();
        logger.info("Главное окно закрыто");
    }
}