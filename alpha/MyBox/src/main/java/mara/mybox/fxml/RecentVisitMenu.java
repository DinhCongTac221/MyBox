package mara.mybox.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.FileChooser;
import mara.mybox.controller.BaseController_Files;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @License Apache License Version 2.0
 */
public abstract class RecentVisitMenu {

    protected final BaseController_Files controller;
    protected final Event event;
    protected List<String> examples;
    protected int SourceFileType, SourcePathType, AddFileType, AddPathType, TargetFileType, TargetPathType;
    protected List<FileChooser.ExtensionFilter> sourceExtensionFilter;
    protected String baseName, defaultPath;

    public RecentVisitMenu(BaseController_Files controller, Event event) {
        this.controller = controller;
        this.event = event;
        this.baseName = controller.getBaseName();
        SourceFileType = controller.getSourceFileType();
        SourcePathType = controller.getSourcePathType();
        AddFileType = controller.getAddFileType();
        AddPathType = controller.getAddPathType();

        TargetFileType = controller.getTargetFileType();
        TargetPathType = controller.getTargetPathType();

        if (AddFileType <= 0) {
            AddFileType = SourceFileType;
        }
    }

    public RecentVisitMenu setFileType(int fileType) {
        SourceFileType = fileType;
        SourcePathType = fileType;
        TargetFileType = fileType;
        TargetPathType = fileType;
        AddFileType = fileType;
        AddPathType = fileType;
        return this;
    }

    public void pop() {
        try {
            if (controller == null || event == null) {
                return;
            }
            ContextMenu popMenu = controller.getPopMenu();
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu = new MenuItem(message("Select..."));
            menu.setOnAction((ActionEvent event1) -> {
                handleSelect();
            });
            popMenu.getItems().add(menu);

            List<VisitHistory> opened = recentFiles();
            if (opened != null && !opened.isEmpty()) {
                List<String> files = new ArrayList<>();
                for (VisitHistory h : opened) {
                    String fname = h.getResourceValue();
                    if (!files.contains(fname)) {
                        files.add(fname);
                    }
                }
                if (!files.isEmpty()) {
                    popMenu.getItems().add(new SeparatorMenuItem());
                    menu = new MenuItem(message("RecentOpenedFiles"));
                    menu.setStyle("-fx-text-fill: #2e598a;");
                    popMenu.getItems().add(menu);
                    for (String fname : files) {
                        menu = new MenuItem(StringTools.menuSuffix(fname));
                        menu.setOnAction((ActionEvent event1) -> {
                            handleFile(fname);
                        });
                        popMenu.getItems().add(menu);
                    }
                }

                List<VisitHistory> written = recentWrittenFiles();
                if (written != null && !written.isEmpty()) {
                    files = new ArrayList<>();
                    for (VisitHistory h : written) {
                        String fname = h.getResourceValue();
                        if (!files.contains(fname)) {
                            files.add(fname);
                        }
                    }
                    if (!files.isEmpty()) {
                        popMenu.getItems().add(new SeparatorMenuItem());
                        menu = new MenuItem(message("RecentWrittenFiles"));
                        menu.setStyle("-fx-text-fill: #2e598a;");
                        popMenu.getItems().add(menu);
                        for (String fname : files) {
                            menu = new MenuItem(StringTools.menuSuffix(fname));
                            menu.setOnAction((ActionEvent event1) -> {
                                handleFile(fname);
                            });
                            popMenu.getItems().add(menu);
                        }
                    }
                }

            }

            if (examples != null && !examples.isEmpty()) {
                popMenu.getItems().add(new SeparatorMenuItem());
                menu = new MenuItem(message("Examples"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                popMenu.getItems().add(menu);
                for (String example : examples) {
                    menu = new MenuItem(StringTools.menuSuffix(example));
                    menu.setOnAction((ActionEvent event1) -> {
                        handleFile(example);
                    });
                    popMenu.getItems().add(menu);
                }
            }
            List<String> paths = paths();
            if (paths != null && !paths.isEmpty()) {
                popMenu.getItems().add(new SeparatorMenuItem());
                menu = new MenuItem(message("RecentAccessedDirectories"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                popMenu.getItems().add(menu);
                for (String path : paths) {
                    menu = new MenuItem(StringTools.menuSuffix(path));
                    menu.setOnAction((ActionEvent event1) -> {
                        handlePath(path);
                    });
                    popMenu.getItems().add(menu);
                }
            }

            popMenu.getItems().add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"));
            hoverMenu.setSelected(UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("RecentVisitMenuPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            popMenu.getItems().add(hoverMenu);

            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event1) -> {
                controller.getPopMenu().hide();
                controller.setPopMenu(null);
            });
            popMenu.getItems().add(menu);

            controller.setPopMenu(popMenu);
            LocateTools.locateEvent(event, popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public abstract void handleSelect();

    public abstract void handleFile(String fname);

    public List<VisitHistory> recentFiles() {
        return recentOpenedFiles();
    }

    public List<VisitHistory> recentPaths() {
        return recentSourcePathsBesidesFiles();
    }

    public void handlePath(String fname) {
        handleSourcePath(fname);
    }

    public List<String> paths() {
        List<VisitHistory> his = recentPaths();
        List<String> paths = new ArrayList<>();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (defaultPath != null && !paths.contains(defaultPath)) {
            paths.add(defaultPath);
        }
        File lastPath = UserConfig.getPath("LastPath");
        if (lastPath != null) {
            String lastPathString = lastPath.getAbsolutePath();
            if (!paths.contains(lastPathString)) {
                paths.add(lastPathString);
            }
        }
        return paths;
    }

    public List<VisitHistory> recentOpenedFiles() {
        int fileNumber = AppVariables.fileRecentNumber / 2;
        if (fileNumber == 0) {
            fileNumber = 1;
        }
        return VisitHistoryTools.getRecentFileRead(SourceFileType, fileNumber);
    }

    public List<VisitHistory> recentWrittenFiles() {
        int fileNumber = AppVariables.fileRecentNumber / 4;
        if (fileNumber == 0) {
            fileNumber = 1;
        }
        return VisitHistoryTools.getRecentFileWrite(SourceFileType, fileNumber);
    }

    public List<VisitHistory> recentTargetFiles() {
        int fileNumber = AppVariables.fileRecentNumber * 3 / 4;
        if (fileNumber == 0) {
            fileNumber = 1;
        }
        return VisitHistoryTools.getRecentFileWrite(TargetFileType, fileNumber);
    }

    public List<VisitHistory> recentAddFiles() {
        int fileNumber = AppVariables.fileRecentNumber / 2;
        if (fileNumber == 0) {
            fileNumber = 1;
        }
        return VisitHistoryTools.getRecentFileRead(AddFileType, fileNumber);
    }

    public List<VisitHistory> recentSourcePathsBesidesFiles() {
        int pathNumber = AppVariables.fileRecentNumber / 4 + 1;
        return VisitHistoryTools.getRecentPath(SourcePathType, pathNumber);
    }

    public List<VisitHistory> recentTargetPathsBesidesFiles() {
        int pathNumber = AppVariables.fileRecentNumber / 4 + 1;
        if (pathNumber == 0) {
            pathNumber = 1;
        }
        return VisitHistoryTools.getRecentPath(TargetPathType, pathNumber);
    }

    public List<VisitHistory> recentSourcePaths() {
        return VisitHistoryTools.getRecentPath(SourcePathType);
    }

    public List<VisitHistory> recentTargetPaths() {
        return VisitHistoryTools.getRecentPath(TargetPathType);
    }

    public void handleSourcePath(String fname) {
        File file = new File(fname);
        if (!file.exists()) {
            handleSelect();
            return;
        }
        UserConfig.setString(baseName + "SourcePath", fname);
        handleSelect();
    }

    public void handleTargetPath(String fname) {
        File file = new File(fname);
        if (!file.exists()) {
            handleSelect();
            return;
        }
        UserConfig.setString(baseName + "TargetPath", fname);
        handleSelect();
    }

    /*
        get/set
     */
    public List<String> getExamples() {
        return examples;
    }

    public RecentVisitMenu setExamples(List<String> examples) {
        this.examples = examples;
        return this;
    }

    public int getSourceFileType() {
        return SourceFileType;
    }

    public RecentVisitMenu setSourceFileType(int SourceFileType) {
        this.SourceFileType = SourceFileType;
        return this;
    }

    public int getSourcePathType() {
        return SourcePathType;
    }

    public RecentVisitMenu setSourcePathType(int SourcePathType) {
        this.SourcePathType = SourcePathType;
        return this;
    }

    public int getAddFileType() {
        return AddFileType;
    }

    public RecentVisitMenu setAddFileType(int AddFileType) {
        this.AddFileType = AddFileType;
        return this;
    }

    public int getAddPathType() {
        return AddPathType;
    }

    public RecentVisitMenu setAddPathType(int AddPathType) {
        this.AddPathType = AddPathType;
        return this;
    }

    public int getTargetPathType() {
        return TargetPathType;
    }

    public RecentVisitMenu setTargetPathType(int TargetPathType) {
        this.TargetPathType = TargetPathType;
        return this;
    }

    public List<FileChooser.ExtensionFilter> getSourceExtensionFilter() {
        return sourceExtensionFilter;
    }

    public RecentVisitMenu setSourceExtensionFilter(List<FileChooser.ExtensionFilter> sourceExtensionFilter) {
        this.sourceExtensionFilter = sourceExtensionFilter;
        return this;
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public RecentVisitMenu setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
        return this;
    }

}
