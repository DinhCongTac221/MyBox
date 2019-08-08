package mara.mybox.controller.base;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import mara.mybox.controller.ImageViewerController;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.controller.LoadingController;
import mara.mybox.controller.MainMenuController;
import mara.mybox.controller.OperationController;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.DerbyBase;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import mara.mybox.value.CommonValues;
import static mara.mybox.value.CommonValues.AppDataRoot;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:50:43
 * @Description
 * @License Apache License Version 2.0
 */
public class BaseController implements Initializable {

    protected String TipsLabelKey, LastPathKey, targetPathKey, sourcePathKey, defaultPathKey, SaveAsOptionsKey;
    protected int SourceFileType, SourcePathType, TargetFileType, TargetPathType, AddFileType, AddPathType,
            operationType;
    protected List<FileChooser.ExtensionFilter> sourceExtensionFilter, targetExtensionFilter;
    protected String myFxml, parentFxml, currentStatus, baseTitle, baseName, loadFxml;
    protected Stage myStage;
    protected Scene myScene;
    protected Alert loadingAlert;
    protected Task<Void> task, backgroundTask;
    protected BaseController parentController, myController;
    protected Timer popupTimer, timer;
    protected Popup popup;
    protected ContextMenu popMenu;
    protected MaximizedListener maximizedListener;
    protected FullscreenListener fullscreenListener;

    protected boolean isSettingValues;
    protected File sourceFile, sourcePath, targetPath, targetFile;
    protected SaveAsType saveAsType;

    protected enum SaveAsType {
        Load, Open, None
    }

    @FXML
    protected Pane thisPane, mainMenu, operationBar;
    @FXML
    protected MainMenuController mainMenuController;
    @FXML
    protected TextField sourceFileInput, sourcePathInput,
            targetPathInput, targetPrefixInput, targetFileInput, statusLabel;
    @FXML
    protected OperationController operationBarController;
    @FXML
    protected Button selectSourceButton, createButton, copyButton, pasteButton,
            deleteButton, saveButton, infoButton, metaButton, selectAllButton,
            okButton, startButton, firstButton, lastButton, previousButton, nextButton, goButton, previewButton,
            cropButton, saveAsButton, recoverButton, renameButton, tipsButton, viewButton, popButton, refButton,
            undoButton, redoButton, transparentButton, whiteButton, blackButton;
    @FXML
    protected VBox paraBox;
    @FXML
    protected Label bottomLabel, tipsLabel;
    @FXML
    protected ImageView tipsView, linksView;
    @FXML
    protected ChoiceBox saveAsOptionsBox;

    public BaseController() {
        baseTitle = AppVaribles.message("AppTitle");

        SourceFileType = 0;
        SourcePathType = 0;
        TargetPathType = 0;
        TargetFileType = 0;
        AddFileType = 0;
        AddPathType = 0;
        operationType = 0;

        LastPathKey = "LastPathKey";
        targetPathKey = "targetPath";
        sourcePathKey = "sourcePath";
        defaultPathKey = null;
        SaveAsOptionsKey = "SaveAsOptionsKey";

        sourceExtensionFilter = CommonValues.AllExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            baseName = FxmlControl.getFxmlName(url.getPath());
            myFxml = "/fxml/" + baseName + ".fxml";
            myController = this;
            if (mainMenuController != null) {
                mainMenuController.parentFxml = myFxml;
                mainMenuController.parentController = this;
            }
            AppVaribles.alarmClockController = null;

            setInterfaceStyle(AppVaribles.getStyle());
            setSceneFontSize(AppVaribles.sceneFontSize);
            if (thisPane != null) {
                thisPane.setStyle("-fx-font-size: " + AppVaribles.sceneFontSize + "px;");
                thisPane.setOnKeyReleased(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        keyEventsHandler(event);
                    }
                });
            }

            initControls();
            initializeNext();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void afterStageShown() {
        getMyStage();
    }

    public void afterSceneLoaded() {
        try {
            getMyScene();
            getMyStage();

            final String prefix;
            if (baseName.startsWith("ImageManufacture") && !baseName.startsWith("ImageManufactureBatch")) {
                prefix = "Interface_" + "ImageManufacture";
            } else {
                prefix = "Interface_" + baseName;
            }

            if (AppVaribles.restoreStagesSize) {

                final int minSize = 400;

                if (AppVaribles.getUserConfigBoolean(prefix + "FullScreen", false)) {
                    myStage.setFullScreen(true);

                } else if (AppVaribles.getUserConfigBoolean(prefix + "Maximized", false)) {
                    FxmlControl.setMaximized(myStage, AppVaribles.getUserConfigBoolean(prefix + "Maximized", false));

                } else {

                    int v = AppVaribles.getUserConfigInt(prefix + "StageWidth", -1);
                    if (v > 0) {
                        if (v < Math.min(minSize, myStage.getWidth())) {
                            v = 400;
                        }
                        myStage.setWidth(v);
                    }
                    v = AppVaribles.getUserConfigInt(prefix + "StageHeight", -1);
                    if (v > 0) {
                        if (v < Math.min(minSize, myStage.getHeight())) {
                            v = 400;
                        }
                        myStage.setHeight(v);
                    }
                    myStage.centerOnScreen();
                }

                fullscreenListener = new FullscreenListener(prefix);
                myStage.fullScreenProperty().addListener(fullscreenListener);
                maximizedListener = new MaximizedListener(prefix);
                myStage.maximizedProperty().addListener(maximizedListener);

                myScene.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        if (!myStage.isMaximized() && !myStage.isFullScreen() && myStage.getWidth() > minSize) {
                            AppVaribles.setUserConfigInt(prefix + "StageWidth", (int) myStage.getWidth());
                        }
                    }
                });
                myScene.heightProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        if (!myStage.isMaximized() && !myStage.isFullScreen() && myStage.getHeight() > minSize) {
                            AppVaribles.setUserConfigInt(prefix + "StageHeight", (int) myStage.getHeight());
                        }
                    }
                });

            } else {
//                myStage.sizeToScene();
                myStage.centerOnScreen();
            }

            Parent root = myScene.getRoot();
            root.requestFocus();
            refreshStyle(root);

            myStage.toFront();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public class FullscreenListener implements ChangeListener<Boolean> {

        private final String prefix;

        public FullscreenListener(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
            AppVaribles.setUserConfigValue(prefix + "FullScreen", myStage.isFullScreen());
        }
    }

    public class MaximizedListener implements ChangeListener<Boolean> {

        private final String prefix;

        public MaximizedListener(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
            AppVaribles.setUserConfigValue(prefix + "Maximized", myStage.isMaximized());
        }
    }

    public void refreshStyle(Parent node) {
        node.applyCss();
        node.layout();
        applyStyle(node);
    }

    public void applyStyle(Node node) {
        if (node == null) {
            return;
        }
        ControlStyle.setStyle(node);
        if (node instanceof Parent) {
            for (Node c : ((Parent) node).getChildrenUnmodifiable()) {
                applyStyle(c);
            }
        }
    }

    public void initControls() {
        try {

            if (mainMenuController != null) {
                mainMenuController.sourceExtensionFilter = sourceExtensionFilter;
                mainMenuController.targetExtensionFilter = targetExtensionFilter;
                mainMenuController.sourcePathKey = sourcePathKey;
                mainMenuController.sourcePathKey = sourcePathKey;
                mainMenuController.SourceFileType = SourceFileType;
                mainMenuController.SourcePathType = SourcePathType;
                mainMenuController.TargetPathType = TargetPathType;
                mainMenuController.TargetFileType = TargetFileType;
                mainMenuController.AddFileType = AddFileType;
                mainMenuController.AddPathType = AddPathType;
                mainMenuController.targetPathKey = targetPathKey;
                mainMenuController.LastPathKey = LastPathKey;
            }

            if (sourceFileInput != null) {
                sourceFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkSourceFileInput();
                    }
                });
            }

            if (sourcePathInput != null) {
                sourcePathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        checkSourcetPathInput();
                    }
                });
                File sfile = AppVaribles.getUserConfigPath(sourcePathKey);
                if (sfile != null) {
                    sourcePathInput.setText(sfile.getAbsolutePath());
                }
            }

            if (targetFileInput != null) {
                targetFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkTargetFileInput();
                    }
                });
            }

            if (targetPathInput != null) {
                targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkTargetPathInput();
                    }
                });
                File tfile = AppVaribles.getUserConfigPath(targetPathKey);
                if (tfile != null) {
                    targetPathInput.setText(tfile.getAbsolutePath());
                }
            }

            if (operationBarController != null) {
                operationBarController.parentController = this;
                if (operationBarController.openTargetButton != null) {
                    if (targetFileInput != null) {
                        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetFileInput.textProperty())
                                .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                        );
                    } else if (targetPathInput != null) {
                        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                                .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                        );
                    }
                }
            }

            if (tipsLabel != null && TipsLabelKey != null) {
                FxmlControl.setTooltip(tipsLabel, new Tooltip(message(TipsLabelKey)));
            }

            if (tipsView != null && TipsLabelKey != null) {
                FxmlControl.setTooltip(tipsView, new Tooltip(message(TipsLabelKey)));
            }

            try {
                String vv = AppVaribles.getUserConfigValue(SaveAsOptionsKey, SaveAsType.Load + "");
                if ((SaveAsType.Load + "").equals(vv)) {
                    saveAsType = SaveAsType.Load;

                } else if ((SaveAsType.Open + "").equals(vv)) {
                    saveAsType = SaveAsType.Open;

                } else if ((SaveAsType.None + "").equals(vv)) {
                    saveAsType = SaveAsType.None;
                }

            } catch (Exception e) {
//                logger.error(e.toString());
                saveAsType = SaveAsType.Load;
            }

            if (saveAsOptionsBox != null) {
                List<String> optionsList = Arrays.asList(message("LoadAfterSaveAs"),
                        message("OpenAfterSaveAs"), message("JustSaveAs"));
                saveAsOptionsBox.getItems().addAll(optionsList);
                saveAsOptionsBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                        checkSaveAsOption();
                    }
                });
                if (null != saveAsType) {
                    switch (saveAsType) {
                        case Load:
                            saveAsOptionsBox.getSelectionModel().select(0);
                            break;
                        case Open:
                            saveAsOptionsBox.getSelectionModel().select(1);
                            break;
                        case None:
                            saveAsOptionsBox.getSelectionModel().select(2);
                            break;
                        default:
                            break;
                    }
                }

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkSaveAsOption() {
        switch (saveAsOptionsBox.getSelectionModel().getSelectedIndex()) {
            case 0:
                AppVaribles.setUserConfigValue(SaveAsOptionsKey, SaveAsType.Load + "");
                saveAsType = SaveAsType.Load;
                break;
            case 1:
                AppVaribles.setUserConfigValue(SaveAsOptionsKey, SaveAsType.Open + "");
                saveAsType = SaveAsType.Open;
                break;
            case 2:
                AppVaribles.setUserConfigValue(SaveAsOptionsKey, SaveAsType.None + "");
                saveAsType = SaveAsType.None;
                break;
            default:
                break;
        }
    }

    public void checkSourcetPathInput() {
        try {
            final File file = new File(sourcePathInput.getText());
            if (!file.exists() || !file.isDirectory()) {
                sourcePathInput.setStyle(badStyle);
                return;
            }
            sourcePath = file;
            sourcePathInput.setStyle(null);
            recordFileOpened(file);
        } catch (Exception e) {
        }
    }

    public void checkSourceFileInput() {
        String v = sourceFileInput.getText();
        if (v == null || v.isEmpty()) {
            sourceFileInput.setStyle(badStyle);
            return;
        }
        final File file = new File(v);
        if (!file.exists()) {
            sourceFileInput.setStyle(badStyle);
            return;
        }
        sourceFileInput.setStyle(null);
        sourceFileChanged(file);
        if (parentController != null) {
            parentController.sourceFileChanged(file);
        }
        if (file.isDirectory()) {
            AppVaribles.setUserConfigValue(sourcePathKey, file.getPath());
        } else {
            AppVaribles.setUserConfigValue(sourcePathKey, file.getParent());
            if (targetPrefixInput != null) {
                targetPrefixInput.setText(FileTools.getFilePrefix(file.getName()));
            }
        }
    }

    public void checkTargetPathInput() {
        try {
            final File file = new File(targetPathInput.getText());
            if (!file.exists() || !file.isDirectory()) {
                targetPathInput.setStyle(badStyle);
                return;
            }
            targetPath = file;
            targetPathInput.setStyle(null);
            AppVaribles.setUserConfigValue(targetPathKey, file.getPath());
            recordFileWritten(file);
        } catch (Exception e) {
        }
    }

    public void checkTargetFileInput() {
        try {
            String input = targetFileInput.getText();
            targetFile = new File(input);
            targetFileInput.setStyle(null);
            AppVaribles.setUserConfigValue(targetPathKey, targetFile.getParent());
        } catch (Exception e) {
            targetFile = null;
            targetFileInput.setStyle(badStyle);
        }
    }

    public void keyEventsHandler(KeyEvent event) {
        if (event.isControlDown()) {
            switch (event.getCode()) {
                case HOME:
                    if (firstButton != null && !firstButton.isDisabled()) {
                        firstAction();
                    }
                    return;
                case END:
                    if (lastButton != null && !lastButton.isDisabled()) {
                        lastAction();
                    }
                    return;
            }
            String key = event.getText();
            if (key != null) {
                switch (key) {
                    case "n":
                    case "N":
                        if (createButton != null && !createButton.isDisabled()) {
                            createAction();
                        }
                        break;
                    case "g":
                    case "G":
                        if (okButton != null && !okButton.isDisabled()) {
                            okAction();
                        }
                        break;
                    case "c":
                    case "C":
                        if (copyButton != null && !copyButton.isDisabled()) {
                            copyAction();
                        }
                        break;
                    case "v":
                    case "V":
                        if (pasteButton != null && !pasteButton.isDisabled()) {
                            pasteAction();
                        }
                        break;
                    case "s":
                    case "S":
                        if (saveButton != null && !saveButton.isDisabled()) {
                            saveAction();
                        }
                        break;
                    case "i":
                    case "I":
                        if (infoButton != null && !infoButton.isDisabled()) {
                            infoAction();
                        }
                        break;
                    case "d":
                    case "D":
                        if (deleteButton != null && !deleteButton.isDisabled()) {
                            deleteAction();
                        }
                        break;
                    case "a":
                    case "A":
                        if (selectAllButton != null && !selectAllButton.isDisabled()) {
                            selectAllAction();
                        }
                        break;
                    case "x":
                    case "X":
                        if (cropButton != null && !cropButton.isDisabled()) {
                            cropAction();
                        }
                        break;
                    case "r":
                    case "R":
                        if (recoverButton != null && !recoverButton.isDisabled()) {
                            recoverAction();
                        }
                        break;
                    case "-":
                        setSceneFontSize(AppVaribles.sceneFontSize - 1);
                        break;
                    case "=":
                        setSceneFontSize(AppVaribles.sceneFontSize + 1);
                        break;
                }

            }

        } else {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case DELETE:
                        if (deleteButton != null && !deleteButton.isDisabled()) {
                            deleteAction();
                        }
                        break;

                    case PAGE_UP:
                        if (previousButton != null && !previousButton.isDisabled()) {
                            previousAction();
                        }
                        break;
                    case PAGE_DOWN:
                        if (nextButton != null && !nextButton.isDisabled()) {
                            nextAction();
                        }
                        break;
                    case F1:
                        if (startButton != null && !startButton.isDisabled()) {
                            startAction();
                        } else if (okButton != null && !okButton.isDisabled()) {
                            okAction();
                        }
                        break;
                    case F2:
                        if (saveButton != null && !saveButton.isDisabled()) {
                            saveAction();
                        }
                        break;
                    case F4:
                        closeStage();
                        break;
                    case F5:
                        refresh();
                        break;
                }
            }
        }
    }

    public void initializeNext() {

    }

    public void setInterfaceStyle(Scene scene, String style) {
        try {
            if (scene != null && style != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(BaseController.class.getResource(style).toExternalForm());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void setInterfaceStyle(String style) {
        try {
            if (thisPane != null && style != null) {
                thisPane.getStylesheets().clear();
                thisPane.getStylesheets().add(BaseController.class.getResource(style).toExternalForm());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public boolean setSceneFontSize(int size) {
        if (thisPane == null) {
            return false;
        }
        AppVaribles.setSceneFontSize(size);
        thisPane.setStyle("-fx-font-size: " + size + "px;");
        if (parentController != null) {
            parentController.setSceneFontSize(size);
        }
        return true;
    }

    public BaseController refresh() {
        return refreshBase();
    }

    public BaseController refreshBase() {
        try {
            String title = myStage.getTitle();
            BaseController c, p = parentController;
            c = loadScene(myFxml);
            if (c == null) {
                return null;
            }
            myStage.setTitle(title);
            if (p != null) {
                c.parentFxml = myFxml;
                c.parentController = p;
                p.refresh();
            }
            c.getMyStage().toFront();
            return c;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public ContextMenu getRecentMenu() {
        final ContextMenu recentMenu = new ContextMenu();
        List<VisitHistory> his = VisitHistory.getRecentMenu();
        if (his == null || his.isEmpty()) {
            return recentMenu;
        }

        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            final String fxml = h.getDataMore();
            MenuItem menu = new MenuItem(message(fname));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    loadScene(fxml);
                }
            });
            recentMenu.getItems().add(menu);
        }

        return recentMenu;

    }

    @FXML
    public void selectSourceFile() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            File file = fileChooser.showOpenDialog(myStage);
            if (file == null || !file.exists()) {
                return;
            }

            selectSourceFileDo(file);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void selectSourceFile(File file) {
        if (file == null || !file.exists()) {
            selectSourceFile();
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        selectSourceFileDo(file);
    }

    public void selectSourceFileDo(File file) {
        recordFileOpened(file);
        if (sourceFileInput != null) {
            sourceFileInput.setText(file.getAbsolutePath());
        } else {
            sourceFileChanged(file);
        }
    }

    public void sourceFileChanged(final File file) {
        sourceFile = file;

    }

    public void recordFileOpened(String file) {
        recordFileOpened(new File(file));
    }

    public void recordFileOpened(final File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            String path = file.getPath();
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(SourcePathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(SourcePathType, path);
            VisitHistory.readFile(SourceFileType, fname);
        }

    }

    public void recordFileOpened(final File file, int pathType, int fileType) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            String path = file.getPath();
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(pathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(pathType, path);
            VisitHistory.readFile(fileType, fname);
        }

    }

    public void recordFileWritten(String file) {
        recordFileWritten(new File(file));
    }

    public void recordFileWritten(final File file) {
        recordFileWritten(file, targetPathKey, TargetPathType, TargetFileType);
    }

    public void recordFileWritten(final File file,
            String targetPathKey, int TargetPathType, int TargetFileType) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            String path = file.getPath();
            AppVaribles.setUserConfigValue(targetPathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.writePath(TargetPathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVaribles.setUserConfigValue(targetPathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.writePath(TargetPathType, path);
            VisitHistory.writeFile(TargetFileType, fname);
        }
    }

    public void recordFileAdded(String file) {
        recordFileOpened(new File(file));
    }

    public void recordFileAdded(final File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            String path = file.getPath();
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(SourcePathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(SourcePathType, path);
            VisitHistory.readFile(AddFileType, fname);
        }

    }

    @FXML
    public void selectTargetPath() {
        if (targetPathInput == null) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVaribles.getUserConfigPath(targetPathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(myStage);
            if (directory == null) {
                return;
            }
            selectTargetPath(directory);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void selectTargetPath(File directory) {
        targetPathInput.setText(directory.getPath());

        recordFileWritten(directory);
        targetPathChanged();
    }

    public void targetPathChanged() {

    }

    @FXML
    public void selectTargetFile() {
        File path = AppVaribles.getUserConfigPath(targetPathKey);
        selectTargetFileFromPath(path);
    }

    public void selectTargetFileFromPath(File path) {
        try {
            String name = null;
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(path, name, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            selectTargetFile(file);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void selectTargetFile(File file) {
        try {
            if (file == null) {
                return;
            }
            targetFile = file;
            recordFileWritten(file);

            if (targetFileInput != null) {
                targetFileInput.setText(targetFile.getAbsolutePath());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    public void selectSourcePath() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            selectSourcePath(directory);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void selectSourcePath(File directory) {
        if (sourcePathInput != null) {
            sourcePathInput.setText(directory.getPath());
        }
        recordFileWritten(directory);
    }

    public void openTarget(ActionEvent event) {

    }

    @FXML
    public void addFilesAction() {

    }

    public void addFile(File file) {

    }

    @FXML
    public void insertFilesAction() {

    }

    public void insertFile(File file) {

    }

    @FXML
    public void addDirectoryAction() {

    }

    public void addDirectory(File directory) {

    }

    @FXML
    public void insertDirectoryAction() {

    }

    public void insertDirectory(File directory) {

    }

    @FXML
    public void saveAsAction() {

    }

    @FXML
    public void popSourceFile(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentSourceFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectSourceFile();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectSourceFile();
                    return;
                }
                selectSourceFile(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    public void popFileAdd(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentAddFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.setAddPathType(controller.getSourcePathType());
                }
                return VisitHistory.getRecentPath(controller.getAddPathType(), pathNumber);
            }

            @Override
            public void handleSelect() {
                addFilesAction();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectSourceFile();
                    return;
                }
                addFile(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    public void popFileInsert(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentAddFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.setAddPathType(controller.getSourcePathType());
                }
                return VisitHistory.getRecentPath(controller.getAddPathType(), pathNumber);
            }

            @Override
            public void handleSelect() {
                insertFilesAction();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectSourceFile();
                    return;
                }
                insertFile(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    public void popDirectoryAdd(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.setAddPathType(controller.getSourcePathType());
                }
                return VisitHistory.getRecentPath(controller.getAddPathType(), pathNumber);
            }

            @Override
            public void handleSelect() {
                addDirectoryAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                addDirectory(file);
            }

        }.pop();
    }

    @FXML
    public void popDirectoryInsert(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.setAddPathType(controller.getSourcePathType());
                }
                return VisitHistory.getRecentPath(controller.getAddPathType(), pathNumber);
            }

            @Override
            public void handleSelect() {
                insertDirectoryAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                insertDirectory(file);
            }

        }.pop();
    }

    @FXML
    public void popSourcePath(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePaths();
            }

            @Override
            public void handleSelect() {
                selectSourcePath();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                selectSourcePath(file);
            }

        }.pop();
    }

    @FXML
    public void popTargetPath(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentTargetPaths();
            }

            @Override
            public void handleSelect() {
                selectTargetPath();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                selectTargetPath(file);
            }

        }.pop();
    }

    @FXML
    public void popTargetFile(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentTargetPaths();
            }

            @Override
            public void handleSelect() {
                selectTargetFile();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                selectTargetFileFromPath(file);
            }

        }.pop();
    }

    @FXML
    public void popSaveAs(MouseEvent event) { //
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentTargetPaths();
            }

            @Override
            public void handleSelect() {
                saveAsAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void link(ActionEvent event) {
        try {
            Hyperlink link = (Hyperlink) event.getSource();
            browseURI(new URI(link.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void okAction() {

    }

    @FXML
    public void startAction() {

    }

    @FXML
    public void createAction() {

    }

    @FXML
    public void copyAction() {

    }

    @FXML
    public void pasteAction() {

    }

    @FXML
    public void saveAction() {

    }

    @FXML
    public void deleteAction() {

    }

    @FXML
    public void cropAction() {

    }

    @FXML
    public void recoverAction() {

    }

    @FXML
    public void infoAction() {

    }

    @FXML
    public void selectAllAction() {

    }

    @FXML
    public void nextAction() {

    }

    @FXML
    public void previousAction() {

    }

    @FXML
    public void firstAction() {

    }

    @FXML
    public void lastAction() {

    }

    @FXML
    public void clearSettings(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVaribles.message("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        DerbyBase.clearData();
        cleanAppPath();
        AppVaribles.initAppVaribles();
        popInformation(AppVaribles.message("Successful"));
    }

    public void cleanAppPath() {
        try {
            File userPath = new File(AppDataRoot);
            if (userPath.exists()) {
                File[] files = userPath.listFiles();
                for (File f : files) {
                    if (f.isFile()) {
                        f.delete();
                    } else if (f.isDirectory() && !CommonValues.AppDataPaths.contains(f)) {
                        FileTools.deleteDir(f);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void clearUserTempFiles() {
        try {
            File tempPath = AppVaribles.getUserTempPath();
            if (tempPath.exists()) {
                FileTools.deleteDir(tempPath);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void clearSystemTempFiles() {
        try {
            if (CommonValues.AppTempPath.exists()) {
                FileTools.deleteDir(CommonValues.AppTempPath);
            } else {
                CommonValues.AppTempPath.mkdirs();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void view(File file) {
        FxmlStage.openTarget(null, file.getAbsolutePath());
    }

    public void view(String file) {
        FxmlStage.openTarget(null, file);
    }

    public boolean browseURI(URI uri) {
        if (uri == null) {
            return false;
        }
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                    return true;
                } catch (Exception ioe) {
                }
            } else {
                popError(message("DesktopNotSupportBrowse"), 6000);
            }
        } else {
            popError(message("DesktopNotSupportBrowse"), 6000);
        }
        view(uri.toString());
        return true;
    }

    @FXML
    public void openUserPath(ActionEvent event) {
        try {
            browseURI(new File(AppDataRoot).toURI());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public BaseController loadScene(String newFxml) {
        try {
            if (!leavingScene()) {
                return null;
            }
            return FxmlStage.openScene(myStage, newFxml);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public BaseController openStage(String newFxml) {
        return openStage(newFxml, false);
    }

    public BaseController openStage(String newFxml, boolean isOwned) {
        return FxmlStage.openStage(myStage, newFxml, isOwned);
    }

    public boolean closeStage() {
        if (leavingScene()) {
            FxmlStage.closeStage(myStage);
            return true;
        } else {
            return false;
        }
    }

    public boolean leavingScene() {
        try {
            if (!checkBeforeNextAction()) {
                return false;
            }

            if (mainMenuController != null) {
                mainMenuController.stopMemoryMonitorTimer();
                mainMenuController.stopCpuMonitorTimer();
            }

            if (maximizedListener != null) {
                myStage.maximizedProperty().removeListener(maximizedListener);
            }
            if (fullscreenListener != null) {
                myStage.fullScreenProperty().removeListener(fullscreenListener);
            }

            hidePopup();
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (task != null && task.isRunning()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(myStage.getTitle());
                alert.setContentText(AppVaribles.message("TaskRunning"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK && task != null) {
                    task.cancel();
                    task = null;
                } else {
                    return false;
                }
            }

            if (backgroundTask != null && backgroundTask.isRunning()) {
                backgroundTask.cancel();
                backgroundTask = null;
            }

//            System.gc();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

    public boolean checkBeforeNextAction() {
        return true;
    }

    public File chooseSaveFile(File defaultPath, String defaultName,
            List<FileChooser.ExtensionFilter> filters) {
        return chooseSaveFile(null, defaultPath, defaultName, filters, true);
    }

    public File chooseSaveFile(File defaultPath, String defaultName,
            List<FileChooser.ExtensionFilter> filters, boolean mustHaveExtension) {
        return chooseSaveFile(null, defaultPath, defaultName, filters, mustHaveExtension);
    }

    public File chooseSaveFile(String title, File defaultPath, String defaultName,
            List<FileChooser.ExtensionFilter> filters, boolean mustHaveExtension) {
        try {
            FileChooser fileChooser = new FileChooser();
            if (title != null) {
                fileChooser.setTitle(title);
            }
            if (defaultPath != null && defaultPath.exists()) {
                fileChooser.setInitialDirectory(defaultPath);
            }
            if (defaultName != null) {
                fileChooser.setInitialFileName(defaultName);
            }
            if (filters != null) {
                fileChooser.getExtensionFilters().addAll(filters);
            }

            File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return null;
            }
            String s = FileTools.getFileSuffix(fileChooser.getSelectedExtensionFilter().getExtensions().get(0));
            // https://stackoverflow.com/questions/20637865/javafx-2-2-get-selected-file-extension
            // This is a pretty annoying thing in JavaFX - they will automatically append the extension on Windows, but not on Linux or Mac.
            if (mustHaveExtension && FileTools.getFileSuffix(file.getName()).isEmpty()) {
                String suffix = null;
                if (filters != null) {
                    try {
                        suffix = FileTools.getFileSuffix(fileChooser.getSelectedExtensionFilter().getExtensions().get(0));
                    } catch (Exception e) {
                        suffix = FileTools.getFileSuffix(filters.get(0).getExtensions().get(0));
                    }
                }
                if (suffix == null) {
                    popError(message("NoFileExtension"), 3000);
                    return null;
                }
                file = new File(file.getAbsolutePath() + "." + suffix);
            }
            return file;

        } catch (Exception e) {
            return null;
        }

    }

    public void alertError(String information) {
        FxmlStage.alertError(myStage, information);
    }

    public void alertWarning(String information) {
        FxmlStage.alertError(myStage, information);
    }

    public void alertInformation(String information) {
        FxmlStage.alertInformation(myStage, information);
    }

    public Popup getPopup() {
        if (popup != null) {
            popup.hide();
        }
        popup = new Popup();
        popup.setAutoHide(true);
        return popup;
    }

    public void popText(String text, int delay, String color) {
        try {
            if (popup != null) {
                popup.hide();
            }
            popup = getPopup();
            Label popupLabel = new Label(text);
            popupLabel.setStyle("-fx-background-color:black;"
                    + " -fx-text-fill: " + color + ";"
                    + " -fx-font-size: 1em;"
                    + " -fx-padding: 10px;"
                    + " -fx-background-radius: 6;");
            popup.setAutoFix(true);
            popup.getContent().add(popupLabel);

            if (delay > 0) {
                if (popupTimer != null) {
                    popupTimer.cancel();
                }
                popupTimer = getPopupTimer();
                popupTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                hidePopup();
                            }
                        });
                    }
                }, delay);
            }
            popup.show(myStage);
        } catch (Exception e) {

        }
    }

    public void popInformation(String text) {
        popInformation(text, AppVaribles.getCommentsDelay());
    }

    public void popInformation(String text, int delay) {
        popText(text, delay, "white");
    }

    public void popError(String text) {
        popError(text, AppVaribles.getCommentsDelay());
    }

    public void popError(String text, int delay) {
        popText(text, delay, "red");
    }

    public void popWarn(String text) {
        popError(text, AppVaribles.getCommentsDelay());
    }

    public void popWarn(String text, int delay) {
        popText(text, delay, "orange");
    }

    public void hidePopup() {
        if (popup != null) {
            popup.hide();
        }
        if (popupTimer != null) {
            popupTimer.cancel();
        }
        popup = null;
        popupTimer = null;
    }

    public Stage getMyStage() {
        if (myStage == null) {
            if (thisPane != null) {
                myScene = thisPane.getScene();
                if (myScene != null) {
                    myStage = (Stage) myScene.getWindow();
                }
            }
        }
        return myStage;
    }

    public LoadingController openHandlingStage(Modality block) {
        return openHandlingStage(block, null);
    }

    public LoadingController openHandlingStage(Modality block, String info) {
        try {
            final LoadingController controller
                    = FxmlStage.openLoadingStage(myStage, block, info);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public LoadingController openHandlingStage(final Task<?> task, Modality block) {
        return openHandlingStage(task, block, null);
    }

    public LoadingController openHandlingStage(final Task<?> task, Modality block, String info) {
        try {
            final LoadingController controller
                    = FxmlStage.openLoadingStage(myStage, block, info);

            controller.init(task);
            controller.parentController = myController;

            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    controller.closeStage();
                }
            });
            task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    popInformation(AppVaribles.message("Canceled"));
                    controller.closeStage();
                }
            });
            task.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    popError(AppVaribles.message("Error"));
                    controller.closeStage();
                }
            });
            return controller;

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public void taskCanceled(Task task) {

    }

    public String getBaseTitle() {
        if (baseTitle == null && myStage != null) {
            baseTitle = myStage.getTitle();
            if (baseTitle == null) {
                baseTitle = AppVaribles.message("AppTitle");
            }
        }
        return baseTitle;
    }

    public Timer getPopupTimer() {
        if (popupTimer != null) {
            popupTimer.cancel();

        }
        popupTimer = new Timer();
        return popupTimer;
    }

    public Scene getMyScene() {
        if (myScene == null) {
            if (thisPane != null) {
                myScene = thisPane.getScene();
            } else if (myStage != null) {
                myScene = myStage.getScene();
            }
        }
        return myScene;
    }

    public void setMaskStroke() {

    }

    public void drawMaskRulerX() {

    }

    public void drawMaskRulerY() {

    }

    public void multipleFilesGenerated(final List<String> fileNames) {
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return;
            }
            String path = new File(fileNames.get(0)).getParent();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(getMyStage().getTitle());
            String info = MessageFormat.format(AppVaribles.message("GeneratedFilesResult"),
                    fileNames.size(), "\"" + path + "\"");
            int num = fileNames.size();
            if (num > 10) {
                num = 10;
            }
            for (int i = 0; i < num; i++) {
                info += "\n    " + fileNames.get(i);
            }
            if (fileNames.size() > num) {
                info += "\n    ......";
            }
            alert.setContentText(info);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonOpen = new ButtonType(AppVaribles.message("OpenTargetPath"));
            ButtonType buttonBrowse = new ButtonType(AppVaribles.message("Browse"));
            ButtonType buttonBrowseNew = new ButtonType(AppVaribles.message("BrowseInNew"));
            ButtonType buttonClose = new ButtonType(AppVaribles.message("Close"));
            alert.getButtonTypes().setAll(buttonBrowseNew, buttonBrowse, buttonOpen, buttonClose);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonOpen) {
                browseURI(new File(path).toURI());
                recordFileOpened(path);
            } else if (result.get() == buttonBrowse) {
                final ImagesBrowserController controller = FxmlStage.openImagesBrowser(getMyStage());
                if (controller != null && sourceFile != null) {
                    controller.loadFiles(fileNames);
                }
            } else if (result.get() == buttonBrowseNew) {
                final ImagesBrowserController controller = FxmlStage.openImagesBrowser(null);
                if (controller != null && sourceFile != null) {
                    controller.loadFiles(fileNames);
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void dataChanged() {

    }

    /*
        Static methods
     */
    public static void openImageViewer(File file) {
        FxmlStage.openImageViewer(null, file);
    }

    public static void openImageViewer(String file) {
        FxmlStage.openImageViewer(null, new File(file));
    }

    public static void openImageViewer(Image image) {
        try {
            final ImageViewerController controller = FxmlStage.openImageViewer(null);
            if (controller != null) {
                controller.loadImage(image);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void openImageViewer(ImageInformation info) {
        try {
            final ImageViewerController controller = FxmlStage.openImageViewer(null);
            if (controller != null) {
                controller.loadImage(info);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void openImageManufacture(String filename) {
        FxmlStage.openImageManufacture(null, new File(filename));
    }

    public static void showImageInformation(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlStage.openImageInformation(null, info);
    }

    public static void showImageMetaData(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlStage.openImageMetaData(null, info);
    }

    public static void showImageStatistic(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlStage.openImageStatistic(null, info);
    }

    public static void showImageStatistic(Image image) {
        if (image == null) {
            return;
        }
        FxmlStage.openImageStatistic(null, image);
    }

    /*
        get/set
     */
    public String getTipsLabelKey() {
        return TipsLabelKey;
    }

    public void setTipsLabelKey(String TipsLabelKey) {
        this.TipsLabelKey = TipsLabelKey;
    }

    public String getLastPathKey() {
        return LastPathKey;
    }

    public void setLastPathKey(String LastPathKey) {
        this.LastPathKey = LastPathKey;
    }

    public String getTargetPathKey() {
        return targetPathKey;
    }

    public void setTargetPathKey(String targetPathKey) {
        this.targetPathKey = targetPathKey;
    }

    public String getSourcePathKey() {
        return sourcePathKey;
    }

    public void setSourcePathKey(String sourcePathKey) {
        this.sourcePathKey = sourcePathKey;
    }

    public String getDefaultPathKey() {
        return defaultPathKey;
    }

    public void setDefaultPathKey(String defaultPathKey) {
        this.defaultPathKey = defaultPathKey;
    }

    public String getSaveAsOptionsKey() {
        return SaveAsOptionsKey;
    }

    public void setSaveAsOptionsKey(String SaveAsOptionsKey) {
        this.SaveAsOptionsKey = SaveAsOptionsKey;
    }

    public int getSourceFileType() {
        return SourceFileType;
    }

    public void setSourceFileType(int SourceFileType) {
        this.SourceFileType = SourceFileType;
    }

    public int getSourcePathType() {
        return SourcePathType;
    }

    public void setSourcePathType(int SourcePathType) {
        this.SourcePathType = SourcePathType;
    }

    public int getTargetFileType() {
        return TargetFileType;
    }

    public void setTargetFileType(int TargetFileType) {
        this.TargetFileType = TargetFileType;
    }

    public int getTargetPathType() {
        return TargetPathType;
    }

    public void setTargetPathType(int TargetPathType) {
        this.TargetPathType = TargetPathType;
    }

    public int getAddFileType() {
        return AddFileType;
    }

    public void setAddFileType(int AddFileType) {
        this.AddFileType = AddFileType;
    }

    public int getAddPathType() {
        return AddPathType;
    }

    public void setAddPathType(int AddPathType) {
        this.AddPathType = AddPathType;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public List<FileChooser.ExtensionFilter> getFileExtensionFilter() {
        return sourceExtensionFilter;
    }

    public void setFileExtensionFilter(List<FileChooser.ExtensionFilter> fileExtensionFilter) {
        this.sourceExtensionFilter = fileExtensionFilter;
    }

    public String getMyFxml() {
        return myFxml;
    }

    public void setMyFxml(String myFxml) {
        this.myFxml = myFxml;
    }

    public String getParentFxml() {
        return parentFxml;
    }

    public void setParentFxml(String parentFxml) {
        this.parentFxml = parentFxml;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getLoadFxml() {
        return loadFxml;
    }

    public void setLoadFxml(String loadFxml) {
        this.loadFxml = loadFxml;
    }

    public Alert getLoadingAlert() {
        return loadingAlert;
    }

    public void setLoadingAlert(Alert loadingAlert) {
        this.loadingAlert = loadingAlert;
    }

    public Task<Void> getTask() {
        return task;
    }

    public void setTask(Task<Void> task) {
        this.task = task;
    }

    public Task<Void> getBackgroundTask() {
        return backgroundTask;
    }

    public void setBackgroundTask(Task<Void> backgroundTask) {
        this.backgroundTask = backgroundTask;
    }

    public BaseController getParentController() {
        return parentController;
    }

    public void setParentController(BaseController parentController) {
        this.parentController = parentController;
    }

    public BaseController getMyController() {
        return myController;
    }

    public void setMyController(BaseController myController) {
        this.myController = myController;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public ContextMenu getPopMenu() {
        return popMenu;
    }

    public void setPopMenu(ContextMenu popMenu) {
        this.popMenu = popMenu;
    }

    public boolean isIsSettingValues() {
        return isSettingValues;
    }

    public void setIsSettingValues(boolean isSettingValues) {
        this.isSettingValues = isSettingValues;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public File getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(File targetPath) {
        this.targetPath = targetPath;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    public SaveAsType getSaveAsType() {
        return saveAsType;
    }

    public void setSaveAsType(SaveAsType saveAsType) {
        this.saveAsType = saveAsType;
    }

    public Pane getThisPane() {
        return thisPane;
    }

    public void setThisPane(Pane thisPane) {
        this.thisPane = thisPane;
    }

    public Pane getMainMenu() {
        return mainMenu;
    }

    public void setMainMenu(Pane mainMenu) {
        this.mainMenu = mainMenu;
    }

    public Pane getOperationBar() {
        return operationBar;
    }

    public void setOperationBar(Pane operationBar) {
        this.operationBar = operationBar;
    }

    public MainMenuController getMainMenuController() {
        return mainMenuController;
    }

    public void setMainMenuController(MainMenuController mainMenuController) {
        this.mainMenuController = mainMenuController;
    }

    public TextField getSourceFileInput() {
        return sourceFileInput;
    }

    public void setSourceFileInput(TextField sourceFileInput) {
        this.sourceFileInput = sourceFileInput;
    }

    public TextField getSourcePathInput() {
        return sourcePathInput;
    }

    public void setSourcePathInput(TextField sourcePathInput) {
        this.sourcePathInput = sourcePathInput;
    }

    public TextField getTargetPathInput() {
        return targetPathInput;
    }

    public void setTargetPathInput(TextField targetPathInput) {
        this.targetPathInput = targetPathInput;
    }

    public TextField getTargetPrefixInput() {
        return targetPrefixInput;
    }

    public void setTargetPrefixInput(TextField targetPrefixInput) {
        this.targetPrefixInput = targetPrefixInput;
    }

    public TextField getTargetFileInput() {
        return targetFileInput;
    }

    public void setTargetFileInput(TextField targetFileInput) {
        this.targetFileInput = targetFileInput;
    }

    public TextField getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(TextField statusLabel) {
        this.statusLabel = statusLabel;
    }

    public OperationController getOperationBarController() {
        return operationBarController;
    }

    public void setOperationBarController(OperationController operationBarController) {
        this.operationBarController = operationBarController;
    }

    public Button getSelectSourceButton() {
        return selectSourceButton;
    }

    public void setSelectSourceButton(Button selectSourceButton) {
        this.selectSourceButton = selectSourceButton;
    }

    public Button getCreateButton() {
        return createButton;
    }

    public void setCreateButton(Button createButton) {
        this.createButton = createButton;
    }

    public Button getCopyButton() {
        return copyButton;
    }

    public void setCopyButton(Button copyButton) {
        this.copyButton = copyButton;
    }

    public Button getPasteButton() {
        return pasteButton;
    }

    public void setPasteButton(Button pasteButton) {
        this.pasteButton = pasteButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(Button deleteButton) {
        this.deleteButton = deleteButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public void setSaveButton(Button saveButton) {
        this.saveButton = saveButton;
    }

    public Button getInfoButton() {
        return infoButton;
    }

    public void setInfoButton(Button infoButton) {
        this.infoButton = infoButton;
    }

    public Button getMetaButton() {
        return metaButton;
    }

    public void setMetaButton(Button metaButton) {
        this.metaButton = metaButton;
    }

    public Button getSelectAllButton() {
        return selectAllButton;
    }

    public void setSelectAllButton(Button selectAllButton) {
        this.selectAllButton = selectAllButton;
    }

    public Button getOkButton() {
        return okButton;
    }

    public void setOkButton(Button okButton) {
        this.okButton = okButton;
    }

    public Button getStartButton() {
        return startButton;
    }

    public void setStartButton(Button startButton) {
        this.startButton = startButton;
    }

    public Button getFirstButton() {
        return firstButton;
    }

    public void setFirstButton(Button firstButton) {
        this.firstButton = firstButton;
    }

    public Button getLastButton() {
        return lastButton;
    }

    public void setLastButton(Button lastButton) {
        this.lastButton = lastButton;
    }

    public Button getPreviousButton() {
        return previousButton;
    }

    public void setPreviousButton(Button previousButton) {
        this.previousButton = previousButton;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public void setNextButton(Button nextButton) {
        this.nextButton = nextButton;
    }

    public Button getGoButton() {
        return goButton;
    }

    public void setGoButton(Button goButton) {
        this.goButton = goButton;
    }

    public Button getPreviewButton() {
        return previewButton;
    }

    public void setPreviewButton(Button previewButton) {
        this.previewButton = previewButton;
    }

    public Button getCropButton() {
        return cropButton;
    }

    public void setCropButton(Button cropButton) {
        this.cropButton = cropButton;
    }

    public Button getSaveAsButton() {
        return saveAsButton;
    }

    public void setSaveAsButton(Button saveAsButton) {
        this.saveAsButton = saveAsButton;
    }

    public Button getRecoverButton() {
        return recoverButton;
    }

    public void setRecoverButton(Button recoverButton) {
        this.recoverButton = recoverButton;
    }

    public Button getRenameButton() {
        return renameButton;
    }

    public void setRenameButton(Button renameButton) {
        this.renameButton = renameButton;
    }

    public Button getTipsButton() {
        return tipsButton;
    }

    public void setTipsButton(Button tipsButton) {
        this.tipsButton = tipsButton;
    }

    public Button getViewButton() {
        return viewButton;
    }

    public void setViewButton(Button viewButton) {
        this.viewButton = viewButton;
    }

    public Button getPopButton() {
        return popButton;
    }

    public void setPopButton(Button popButton) {
        this.popButton = popButton;
    }

    public Button getRefButton() {
        return refButton;
    }

    public void setRefButton(Button refButton) {
        this.refButton = refButton;
    }

    public Button getUndoButton() {
        return undoButton;
    }

    public void setUndoButton(Button undoButton) {
        this.undoButton = undoButton;
    }

    public Button getRedoButton() {
        return redoButton;
    }

    public void setRedoButton(Button redoButton) {
        this.redoButton = redoButton;
    }

    public Button getTransparentButton() {
        return transparentButton;
    }

    public void setTransparentButton(Button transparentButton) {
        this.transparentButton = transparentButton;
    }

    public Button getWhiteButton() {
        return whiteButton;
    }

    public void setWhiteButton(Button whiteButton) {
        this.whiteButton = whiteButton;
    }

    public Button getBlackButton() {
        return blackButton;
    }

    public void setBlackButton(Button blackButton) {
        this.blackButton = blackButton;
    }

    public VBox getParaBox() {
        return paraBox;
    }

    public void setParaBox(VBox paraBox) {
        this.paraBox = paraBox;
    }

    public Label getBottomLabel() {
        return bottomLabel;
    }

    public void setBottomLabel(Label bottomLabel) {
        this.bottomLabel = bottomLabel;
    }

    public Label getTipsLabel() {
        return tipsLabel;
    }

    public void setTipsLabel(Label tipsLabel) {
        this.tipsLabel = tipsLabel;
    }

    public ImageView getTipsView() {
        return tipsView;
    }

    public void setTipsView(ImageView tipsView) {
        this.tipsView = tipsView;
    }

    public ImageView getLinksView() {
        return linksView;
    }

    public void setLinksView(ImageView linksView) {
        this.linksView = linksView;
    }

    public ChoiceBox getSaveAsOptionsBox() {
        return saveAsOptionsBox;
    }

    public void setSaveAsOptionsBox(ChoiceBox saveAsOptionsBox) {
        this.saveAsOptionsBox = saveAsOptionsBox;
    }

    public void setBaseTitle(String baseTitle) {
        this.baseTitle = baseTitle;
    }

    public void setMyStage(Stage myStage) {
        this.myStage = myStage;
    }

    public void setMyScene(Scene myScene) {
        this.myScene = myScene;
    }

    public void setPopupTimer(Timer popupTimer) {
        this.popupTimer = popupTimer;
    }

    public void setPopup(Popup popup) {
        this.popup = popup;
    }

    public File getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(File sourcePath) {
        this.sourcePath = sourcePath;
    }

    public List<FileChooser.ExtensionFilter> getSourceExtensionFilter() {
        return sourceExtensionFilter;
    }

    public void setSourceExtensionFilter(List<FileChooser.ExtensionFilter> sourceExtensionFilter) {
        this.sourceExtensionFilter = sourceExtensionFilter;
    }

    public List<FileChooser.ExtensionFilter> getTargetExtensionFilter() {
        return targetExtensionFilter;
    }

    public void setTargetExtensionFilter(List<FileChooser.ExtensionFilter> targetExtensionFilter) {
        this.targetExtensionFilter = targetExtensionFilter;
    }

    public MaximizedListener getMaximizedListener() {
        return maximizedListener;
    }

    public void setMaximizedListener(MaximizedListener maximizedListener) {
        this.maximizedListener = maximizedListener;
    }

    public FullscreenListener getFullscreenListener() {
        return fullscreenListener;
    }

    public void setFullscreenListener(FullscreenListener fullscreenListener) {
        this.fullscreenListener = fullscreenListener;
    }

}
