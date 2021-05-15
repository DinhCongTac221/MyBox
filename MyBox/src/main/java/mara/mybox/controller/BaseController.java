package mara.mybox.controller;

import java.awt.Toolkit;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.FadeTransition;
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
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import mara.mybox.data.BaseTask;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.TableUserConf;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.MyboxDataPath;
import static mara.mybox.value.AppVariables.getPopErrorColor;
import static mara.mybox.value.AppVariables.getPopInfoColor;
import static mara.mybox.value.AppVariables.getPopTextDuration;
import static mara.mybox.value.AppVariables.getPopTextSize;
import static mara.mybox.value.AppVariables.getPopTextbgColor;
import static mara.mybox.value.AppVariables.getPopWarnColor;
import static mara.mybox.value.AppVariables.getUserConfigBoolean;
import static mara.mybox.value.AppVariables.getUserConfigValue;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.setUserConfigValue;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:50:43
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BaseController implements Initializable {

    protected String TipsLabelKey, LastPathKey, targetPathKey, sourcePathKey, defaultPath;
    protected int SourceFileType = -1, SourcePathType, TargetFileType, TargetPathType, AddFileType, AddPathType,
            operationType, dpi;
    protected List<FileChooser.ExtensionFilter> sourceExtensionFilter, targetExtensionFilter;
    protected String myFxml, parentFxml, currentStatus, baseTitle, baseName;
    protected Stage myStage;
    protected Scene myScene;
    protected Alert loadingAlert;
    protected SingletonTask<Void> task, backgroundTask;
    protected BaseController parentController, myController;
    protected Timer popupTimer, timer;
    protected Popup popup;
    protected ContextMenu popMenu;
    protected MaximizedListener maximizedListener;
    protected FullscreenListener fullscreenListener;
    protected String targetFileSuffix, targetNameAppend;
    protected ChangeListener<Number> leftDividerListener, rightDividerListener;
    protected boolean isSettingValues;
    protected File sourceFile, sourcePath, targetPath, targetFile;
    protected SaveAsType saveAsType;
    protected TargetExistType targetExistType;
    protected KeyEvent currentKeyEvent;

    protected enum SaveAsType {
        Load, Open, None
    }

    public static enum TargetExistType {
        Rename, Replace, Skip
    }

    @FXML
    protected Pane thisPane, mainMenu, operationBar;
    @FXML
    protected MainMenuController mainMenuController;
    @FXML
    protected TextField sourceFileInput, sourcePathInput, targetAppendInput,
            targetPathInput, targetPrefixInput, targetFileInput, statusLabel;
    @FXML
    protected OperationController operationBarController;
    @FXML
    protected Button allButton, clearButton, selectFileButton, createButton, copyButton, pasteButton, cancelButton,
            deleteButton, saveButton, infoButton, metaButton, setButton, addButton,
            okButton, startButton, firstButton, lastButton, previousButton, nextButton, goButton, previewButton,
            cropButton, saveAsButton, recoverButton, renameButton, tipsButton, viewButton, popButton, refButton,
            undoButton, redoButton, transparentButton, whiteButton, blackButton, playButton, stopButton,
            selectAllButton, selectNoneButton, withdrawButton,
            pageFirstButton, pageLastButton, pagePreviousButton, pageNextButton;
    @FXML
    protected VBox paraBox;
    @FXML
    protected Label bottomLabel, tipsLabel;
    @FXML
    protected ImageView tipsView, rightTipsView, linksView, leftPaneControl, rightPaneControl;
    @FXML
    protected CheckBox topCheck, saveCloseCheck, closeRightPaneCheck;
    @FXML
    protected ToggleGroup saveAsGroup, targetExistGroup, fileTypeGroup;
    @FXML
    protected RadioButton saveLoadRadio, saveOpenRadio, saveJustRadio,
            targetReplaceRadio, targetRenameRadio, targetSkipRadio;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected ScrollPane leftPane, rightPane;
    @FXML
    protected ComboBox<String> dpiSelector;

    public BaseController() {
        baseTitle = AppVariables.message("AppTitle");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            baseName = FxmlControl.getFxmlName(url);
            myFxml = "/fxml/" + baseName + ".fxml";

            initValues();
            initBaseControls();
            initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initValues() {
        try {
            setFileType();

            myController = this;
            if (mainMenuController != null) {
                mainMenuController.parentFxml = myFxml;
                mainMenuController.parentController = this;
            }
            AppVariables.alarmClockController = null;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setFileType() {
        setFileType(FileType.All);
    }

    public void setFileType(int fileType) {
        SourceFileType = fileType;
        SourcePathType = fileType;
        AddFileType = fileType;
        AddPathType = fileType;
        LastPathKey = VisitHistoryTools.getPathKey(fileType);
        sourcePathKey = LastPathKey;
        sourceExtensionFilter = VisitHistoryTools.getExtensionFilter(fileType);

        TargetPathType = fileType;
        TargetFileType = fileType;
        targetPathKey = LastPathKey;
        defaultPath = null;
        targetExtensionFilter = sourceExtensionFilter;
    }

    public void setFileType(int sourceType, int targetType) {
        SourceFileType = sourceType;
        SourcePathType = sourceType;
        AddFileType = sourceType;
        AddPathType = sourceType;
        LastPathKey = VisitHistoryTools.getPathKey(sourceType);
        sourcePathKey = LastPathKey;
        sourceExtensionFilter = VisitHistoryTools.getExtensionFilter(sourceType);

        TargetPathType = targetType;
        TargetFileType = targetType;
        targetPathKey = VisitHistoryTools.getPathKey(targetType);
        defaultPath = null;
        targetExtensionFilter = VisitHistoryTools.getExtensionFilter(targetType);
    }

    public void initBaseControls() {
        try {
            setInterfaceStyle(AppVariables.getStyle());
            setSceneFontSize(AppVariables.sceneFontSize);
            if (thisPane != null) {
                thisPane.setStyle("-fx-font-size: " + AppVariables.sceneFontSize + "px;");
                thisPane.setOnKeyReleased((KeyEvent event) -> {
                    keyEventsHandler(event);
                });

            }

            if (mainMenuController != null) {
                mainMenuController.SourceFileType = getSourceFileType();
                mainMenuController.sourceExtensionFilter = sourceExtensionFilter;
                mainMenuController.targetExtensionFilter = targetExtensionFilter;
                mainMenuController.sourcePathKey = sourcePathKey;
                mainMenuController.sourcePathKey = sourcePathKey;
                mainMenuController.SourcePathType = SourcePathType;
                mainMenuController.TargetPathType = TargetPathType;
                mainMenuController.TargetFileType = TargetFileType;
                mainMenuController.AddFileType = AddFileType;
                mainMenuController.AddPathType = AddPathType;
                mainMenuController.targetPathKey = targetPathKey;
                mainMenuController.LastPathKey = LastPathKey;
            }

            if (sourceFileInput != null) {
                sourceFileInput.textProperty().addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                            checkSourceFileInput();
                        });
            }

            if (sourcePathInput != null) {
                sourcePathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        checkSourcetPathInput();
                    }
                });
                File sfile = AppVariables.getUserConfigPath(sourcePathKey);
                if (sfile != null) {
                    sourcePathInput.setText(sfile.getAbsolutePath());
                }
            }

            if (targetFileInput != null) {
                targetFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        checkTargetFileInput();
                    }
                });
            }

            if (targetPathInput != null) {
                targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        checkTargetPathInput();
                    }
                });
                File tfile = AppVariables.getUserConfigPath(targetPathKey);
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
            if (rightTipsView != null && TipsLabelKey != null) {
                FxmlControl.setTooltip(rightTipsView, new Tooltip(message(TipsLabelKey)));
            }

            saveAsType = SaveAsType.Open;
            if (saveAsGroup != null && saveOpenRadio != null) {
                String v = AppVariables.getUserConfigValue(baseName + "SaveAsType", SaveAsType.Open.name());
                for (SaveAsType s : SaveAsType.values()) {
                    if (v.equals(s.name())) {
                        saveAsType = s;
                        break;
                    }
                }
                if (saveAsType == null || (saveLoadRadio == null && saveAsType == SaveAsType.Load)) {
                    saveAsType = SaveAsType.Open;
                }
                switch (saveAsType) {
                    case Load:
                        saveLoadRadio.setSelected(true);
                        break;
                    case Open:
                        saveOpenRadio.setSelected(true);
                        break;
                    case None:
                        saveJustRadio.setSelected(true);
                        break;
                    default:
                        break;
                }
                saveAsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        if (saveOpenRadio.isSelected()) {
                            saveAsType = SaveAsType.Open;
                        } else if (saveJustRadio.isSelected()) {
                            saveAsType = SaveAsType.None;
                        } else if (saveLoadRadio != null && saveLoadRadio.isSelected()) {
                            saveAsType = SaveAsType.Load;
                        } else {
                            saveAsType = SaveAsType.Open;
                        }
                        AppVariables.setUserConfigValue(baseName + "SaveAsType", saveAsType.name());
                    }
                });
            }

            if (topCheck != null) {
                topCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Top", true));
                topCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (!isSettingValues) {
                            AppVariables.setUserConfigValue(baseName + "Top", newValue);
                        }
                        checkAlwaysTop();
                    }
                });

            }

            if (saveCloseCheck != null) {
                saveCloseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean oldVal, Boolean newVal) {
                        AppVariables.setUserConfigValue(baseName + "SaveClose", saveCloseCheck.isSelected());
                    }
                });
                saveCloseCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "SaveClose", false));
            }

            if (targetExistGroup != null) {
                targetExistGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                        checkTargetExistType();
                    }
                });
                isSettingValues = true;
                FxmlControl.setRadioSelected(targetExistGroup, getUserConfigValue(baseName + "TargetExistType", message("Replace")));
                if (targetAppendInput != null) {
                    targetAppendInput.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                            checkTargetExistType();
                        }
                    });
                    targetAppendInput.setText(getUserConfigValue(baseName + "TargetExistAppend", "_m"));
                }
                isSettingValues = false;
                checkTargetExistType();
            }

            dpi = AppVariables.getUserConfigInt(baseName + "DPI", 96);
            if (dpiSelector != null) {
                List<String> dpiValues = new ArrayList();
                dpiValues.addAll(Arrays.asList("96", "72", "300", "160", "240", "120", "600", "400"));
                String sValue = Toolkit.getDefaultToolkit().getScreenResolution() + "";
                if (dpiValues.contains(sValue)) {
                    dpiValues.remove(sValue);
                }
                dpiValues.add(0, sValue);
                sValue = (int) Screen.getPrimary().getDpi() + "";
                if (dpiValues.contains(sValue)) {
                    dpiValues.remove(sValue);
                }
                dpiValues.add(sValue);
                dpiSelector.getItems().addAll(dpiValues);
                dpiSelector.setValue(dpi + "");
                dpiSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            try {
                                int v = Integer.parseInt(newValue);
                                if (v > 0) {
                                    dpi = v;
                                    AppVariables.setUserConfigInt(baseName + "DPI", dpi);
                                    dpiSelector.getEditor().setStyle(null);
                                } else {
                                    dpiSelector.getEditor().setStyle(badStyle);
                                }
                            } catch (Exception e) {
                                dpiSelector.getEditor().setStyle(badStyle);
                            }
                        });
            }

            if (splitPane != null && leftPane != null && leftPaneControl != null) {
                leftPaneControl.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        controlLeftPaneOnMouseEnter();
                    }
                });
                leftPaneControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        controlLeftPane();
                    }
                });
                leftPaneControl.setPickOnBounds(getUserConfigBoolean("ControlSplitPanesSensitive", false));
                leftPane.setHvalue(0);
            }

            if (splitPane != null && rightPane != null && rightPaneControl != null) {
                rightPaneControl.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        controlRightPaneOnMouseEnter();
                    }
                });
                rightPaneControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        controlRightPane();
                    }
                });
                rightPaneControl.setPickOnBounds(getUserConfigBoolean("ControlSplitPanesSensitive", false));
                rightPane.setHvalue(0);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initControls() {

    }

    // This is called automatically after window is opened
    public void afterStageShown() {
        getMyStage();
        if (myStage != null) {
            myStage.sizeToScene();
            myStage.centerOnScreen();
        }
    }

    // This is called automatically after TOP scene is loaded.
    // Notice embedded scenes will NOT call this automatically.
    public void afterSceneLoaded() {
        try {
            getMyScene();
            getMyStage();

            String prefix = interfaceKeysPrefix();
            int minSize = 200;
            myStage.setMinWidth(minSize);
            myStage.setMinHeight(minSize);
            myStage.sizeToScene();
            myStage.centerOnScreen();

            if (AppVariables.restoreStagesSize) {
                if (AppVariables.getUserConfigBoolean(prefix + "FullScreen", false)) {
                    myStage.setFullScreen(true);

                } else if (AppVariables.getUserConfigBoolean(prefix + "Maximized", false)) {
                    FxmlControl.setMaximized(myStage, true);

                } else {
                    restoreStageStatus(prefix, minSize);
                }

                fullscreenListener = new FullscreenListener(prefix);
                myStage.fullScreenProperty().addListener(fullscreenListener);
                maximizedListener = new MaximizedListener(prefix);
                myStage.maximizedProperty().addListener(maximizedListener);

            }

            Rectangle2D screen = FxmlControl.getScreen();
            if (myStage.getHeight() > screen.getHeight()) {
                myStage.setHeight(screen.getHeight());
            }
            if (myStage.getWidth() > screen.getWidth()) {
                myStage.setWidth(screen.getWidth());
            }
            if (myStage.getX() < 0) {
                myStage.setX(0);
            }
            if (myStage.getY() < 0) {
                myStage.setY(0);
            }

            refreshStyle();
            initSplitPanes();

            toFront();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String interfaceKeysPrefix() {
        return "Interface_" + baseName;
    }

    public void restoreStageStatus(String prefix, int minSize) {
        int w = AppVariables.getUserConfigInt(prefix + "StageWidth", -1);
        int h = AppVariables.getUserConfigInt(prefix + "StageHeight", -1);
        int x = AppVariables.getUserConfigInt(prefix + "StageX", 0);
        int y = AppVariables.getUserConfigInt(prefix + "StageY", 0);
        if (w > minSize && h > minSize) {
            myStage.setWidth(w);
            myStage.setHeight(h);
        }
        if (x > 0 && y > 0) {
            myStage.setX(x);
            myStage.setY(y);
        }
    }

    public void refreshStyle() {
        if (getMyScene() == null) {
            return;
        }
        Parent root = myScene.getRoot();
        FxmlControl.refreshStyle(root);
    }

    public void refreshCurrentStyle() {
        if (getMyScene() == null) {
            return;
        }
        Parent root = myScene.getRoot();
        root.applyCss();
        root.layout();
    }

    public void toFront() {
        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        getMyStage().setIconified(false);
                        myStage.requestFocus();
                        myStage.toFront();
                        if (selectFileButton != null) {
                            selectFileButton.requestFocus();
                        } else if (tipsView != null) {
                            tipsView.requestFocus();
                        } else {
                            thisPane.requestFocus();
                        }
                        if (mainMenuController != null) {
                            FxmlControl.mouseCenter(myStage);
                        }
                        if (leftPane != null) {
                            leftPane.setHvalue(0);
                        }
                        checkAlwaysTop();
                    });
                }
            }, 500);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkAlwaysTop() {
        if (topCheck == null || !topCheck.isVisible() || topCheck.isDisabled()
                || getMyStage() == null) {
            return;
        }
        myStage.setAlwaysOnTop(topCheck.isSelected());
        if (topCheck.isSelected()) {
            popWarn(message("AlwaysTopWarning"));
            FadeTransition fade = new FadeTransition(Duration.millis(300));
            fade.setFromValue(1.0);
            fade.setToValue(0f);
            fade.setCycleCount(4);
            fade.setAutoReverse(true);
            fade.setNode(topCheck);
            fade.play();
        }
    }

    public void initSplitPanes() {
        try {
            if (splitPane == null || splitPane.getDividers().isEmpty()) {
                return;
            }
            if (closeRightPaneCheck != null) {
                closeRightPaneCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                            if (isSettingValues) {
                                return;
                            }
                            AppVariables.setUserConfigValue(baseName + "CloseRightPane", closeRightPaneCheck.isSelected());
                            checkRightPaneClose();
                        });
                isSettingValues = true;
                closeRightPaneCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "CloseRightPane", false));
                isSettingValues = false;
                checkRightPaneClose();
            }

            if (leftPaneControl != null) {
                FxmlControl.setTooltip(leftPaneControl, new Tooltip("F4"));
            }
            if (rightPaneControl != null) {
                FxmlControl.setTooltip(rightPaneControl, new Tooltip("F5"));
            }

            checkRightPaneHide();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkRightPaneClose() {
        if (isSettingValues || splitPane == null || rightPane == null
                || closeRightPaneCheck == null || rightPaneControl == null) {
            return;
        }
        if (closeRightPaneCheck.isSelected()) {
            hideRightPane();
            rightPaneControl.setVisible(false);
        } else {
            rightPaneControl.setVisible(true);
            showRightPane();
        }
    }

    public void checkRightPaneHide() {
        if (isSettingValues || splitPane == null || rightPane == null
                || rightPaneControl == null || !rightPaneControl.isVisible()
                || !splitPane.getItems().contains(rightPane)
                || splitPane.getItems().size() == 1) {
            return;
        }
        if (!AppVariables.getUserConfigBoolean(baseName + "ShowRightControl", true)) {
            hideRightPane();
        }
        setSplitDividerPositions();
        splitPane.applyCss();
    }

    public void setSplitDividerPositions() {
        try {
            if (isSettingValues || splitPane == null) {
                return;
            }
            int dividersSize = splitPane.getDividers().size();
            if (dividersSize < 1) {
                return;
            }
            isSettingValues = true;
            try {
                splitPane.getDividers().get(0).positionProperty().removeListener(leftDividerListener);
                leftDividerListener = null;
            } catch (Exception e) {
            }
            try {
                splitPane.getDividers().get(dividersSize - 1).positionProperty().removeListener(rightDividerListener);
                rightDividerListener = null;
            } catch (Exception e) {
            }
            if (splitPane.getItems().contains(leftPane)) {
                double defaultv = dividersSize == 1 ? 0.35 : 0.15;
                try {
                    String v = AppVariables.getUserConfigValue(baseName + "LeftPanePosition", defaultv + "");
                    splitPane.setDividerPosition(0, Double.parseDouble(v));
                } catch (Exception e) {
                    splitPane.setDividerPosition(0, defaultv);
                }
                leftDividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!isSettingValues) {
                        AppVariables.setUserConfigValue(baseName + "LeftPanePosition", newValue.doubleValue() + "");
                    }
                };
                splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);
            }
            if (splitPane.getItems().contains(rightPane)) {
                int index = splitPane.getDividers().size() - 1;
                double defaultv = index > 0 ? 0.85 : 0.65;
                try {
                    String v = AppVariables.getUserConfigValue(baseName + "RightPanePosition", defaultv + "");
                    splitPane.setDividerPosition(index, Double.parseDouble(v));
                } catch (Exception e) {
                    splitPane.setDividerPosition(index, defaultv);
                }
                rightDividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!isSettingValues) {
                        AppVariables.setUserConfigValue(baseName + "RightPanePosition", newValue.doubleValue() + "");
                    }
                };
                splitPane.getDividers().get(index).positionProperty().addListener(rightDividerListener);
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void controlLeftPaneOnMouseEnter() {
        if (getUserConfigBoolean("MousePassControlPanes", true)) {
            controlLeftPane();
        }
    }

    @FXML
    public void controlLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || leftPaneControl == null || !leftPaneControl.isVisible()) {
            return;
        }
        if (splitPane.getItems().contains(leftPane)) {
            hideLeftPane();
        } else {
            showLeftPane();
        }
    }

    public void hideLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || leftPaneControl == null || !leftPaneControl.isVisible()
                || !splitPane.getItems().contains(leftPane)
                || splitPane.getItems().size() == 1) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().remove(leftPane);
        isSettingValues = false;
        ControlStyle.setIconName(leftPaneControl, "iconDoubleRight.png");
        setSplitDividerPositions();
        FxmlControl.refreshStyle(splitPane);
        AppVariables.setUserConfigValue(baseName + "ShowLeftControl", false);
    }

    public void showLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || leftPaneControl == null || !leftPaneControl.isVisible()
                || splitPane.getItems().contains(leftPane)) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().add(0, leftPane);
        isSettingValues = false;
        ControlStyle.setIconName(leftPaneControl, "iconDoubleLeft.png");
        setSplitDividerPositions();
        FxmlControl.refreshStyle(splitPane);
        AppVariables.setUserConfigValue(baseName + "ShowLeftControl", true);
    }

    public void controlRightPaneOnMouseEnter() {
        if (getUserConfigBoolean("MousePassControlPanes", true)) {
            controlRightPane();
        }
    }

    @FXML
    public void controlRightPane() {
        if (isSettingValues || splitPane == null || rightPane == null
                || rightPaneControl == null || !rightPaneControl.isVisible()) {
            return;
        }
        if (splitPane.getItems().contains(rightPane)) {
            hideRightPane();
        } else {
            showRightPane();
        }
    }

    public void hideRightPane() {
        if (isSettingValues || splitPane == null || rightPane == null
                || rightPaneControl == null || !rightPaneControl.isVisible()
                || !splitPane.getItems().contains(rightPane)
                || splitPane.getItems().size() == 1) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().remove(rightPane);
        isSettingValues = false;
        ControlStyle.setIconName(rightPaneControl, "iconDoubleLeft.png");
        setSplitDividerPositions();
        FxmlControl.refreshStyle(splitPane);
        AppVariables.setUserConfigValue(baseName + "ShowRightControl", false);
    }

    public void showRightPane() {
        if (isSettingValues || splitPane == null || rightPane == null
                || rightPaneControl == null || !rightPaneControl.isVisible()
                || splitPane.getItems().contains(rightPane)) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().add(rightPane);
        isSettingValues = false;
        ControlStyle.setIconName(rightPaneControl, "iconDoubleRight.png");
        setSplitDividerPositions();
        FxmlControl.refreshStyle(splitPane);
        AppVariables.setUserConfigValue(baseName + "ShowRightControl", true);
    }

    public class FullscreenListener implements ChangeListener<Boolean> {

        private final String prefix;

        public FullscreenListener(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
            AppVariables.setUserConfigValue(prefix + "FullScreen", getMyStage().isFullScreen());
        }
    }

    public class MaximizedListener implements ChangeListener<Boolean> {

        private final String prefix;

        public MaximizedListener(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
            AppVariables.setUserConfigValue(prefix + "Maximized", getMyStage().isMaximized());
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
            AppVariables.setUserConfigValue(sourcePathKey, file.getPath());
        } else {
            AppVariables.setUserConfigValue(sourcePathKey, file.getParent());
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
            AppVariables.setUserConfigValue(targetPathKey, file.getPath());
            recordFileWritten(file);
        } catch (Exception e) {
        }
    }

    public void checkTargetFileInput() {
        try {
            String input = targetFileInput.getText();
            targetFile = new File(input);
            targetFileInput.setStyle(null);
            AppVariables.setUserConfigValue(targetPathKey, targetFile.getParent());
        } catch (Exception e) {
            targetFile = null;
            targetFileInput.setStyle(badStyle);
        }
    }

    public void checkTargetExistType() {
        if (isSettingValues) {
            return;
        }
        if (targetAppendInput != null) {
            targetAppendInput.setStyle(null);
        }

        RadioButton selected = (RadioButton) targetExistGroup.getSelectedToggle();
        if (selected.equals(targetReplaceRadio)) {
            targetExistType = TargetExistType.Replace;

        } else if (selected.equals(targetRenameRadio)) {
            targetExistType = TargetExistType.Rename;
            if (targetAppendInput != null) {
                if (targetAppendInput.getText() == null || targetAppendInput.getText().trim().isEmpty()) {
                    targetAppendInput.setStyle(badStyle);
                } else {
                    setUserConfigValue(baseName + "TargetExistAppend", targetAppendInput.getText().trim());
                }
            }

        } else if (selected.equals(targetSkipRadio)) {
            targetExistType = TargetExistType.Skip;
        }
        setUserConfigValue(baseName + "TargetExistType", selected.getText());
    }

    // Shortcuts like PageDown/PageUp/Home/End/Ctrl-c/v/x/z/y/a may work for text editing
    public void keyEventsHandler(KeyEvent event) {
//        MyBoxLog.debug(this.getClass().getName() + " text:" + event.getText() + " code:" + event.getCode());
        currentKeyEvent = event;
//        MyBoxLog.debug(currentKeyEvent.getSource().getClass());
        keyEventsHandlerDo(event);
    }

    public void keyEventsHandlerDo(KeyEvent event) {
        if (event.isControlDown()) {
            controlHandler(event);

        } else if (event.isAltDown()) {
            altHandler(event);

        } else if (event.getCode() != null) {
            keyHandler(event);

        }
    }

    public void controlHandler(KeyEvent event) {
        if (!event.isControlDown() || event.getCode() == null) {
            return;
        }
        controlAltHandler(event);
    }

    public void altHandler(KeyEvent event) {
        if (!event.isAltDown() || event.getCode() == null) {
            return;
        }
        switch (event.getCode()) {
            case HOME:
                if (firstButton != null && !firstButton.isDisabled() && firstButton.isVisible()) {
                    firstAction();
                } else if (pageFirstButton != null && !pageFirstButton.isDisabled() && pageFirstButton.isVisible()) {
                    pageFirstAction();
                }
                return;
            case END:
                if (lastButton != null && !lastButton.isDisabled() && lastButton.isVisible()) {
                    lastAction();
                } else if (pageLastButton != null && !pageLastButton.isDisabled() && pageLastButton.isVisible()) {
                    pageLastAction();
                }
                return;
            case PAGE_UP:
                if (previousButton != null && !previousButton.isDisabled() && previousButton.isVisible()) {
                    previousAction();
                } else if (pagePreviousButton != null && !pagePreviousButton.isDisabled() && pagePreviousButton.isVisible()) {
                    pagePreviousAction();
                }
                return;
            case PAGE_DOWN:
                if (nextButton != null && !nextButton.isDisabled() && nextButton.isVisible()) {
                    nextAction();
                } else if (pageNextButton != null && !pageNextButton.isDisabled() && pageNextButton.isVisible()) {
                    pageNextAction();
                }
                return;
        }
        controlAltHandler(event);
    }

    public void controlAltHandler(KeyEvent event) {
        if (event.getCode() == null) {
            return;
        }
        switch (event.getCode()) {
            case E:
                if (startButton != null && !startButton.isDisabled() && startButton.isVisible()) {
                    startAction();
                } else if (okButton != null && !okButton.isDisabled() && okButton.isVisible()) {
                    okAction();
                }
                return;
            case N:
                if (createButton != null && !createButton.isDisabled() && createButton.isVisible()) {
                    createAction();
                } else if (addButton != null && !addButton.isDisabled() && addButton.isVisible()) {
                    addAction(null);
                }
                return;
            case C:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (copyButton != null && !copyButton.isDisabled() && copyButton.isVisible()) {
                    copyAction();
                }
                return;
            case V:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (pasteButton != null && !pasteButton.isDisabled() && pasteButton.isVisible()) {
                    pasteAction();
                }
                return;
            case S:
                if (saveButton != null && !saveButton.isDisabled() && saveButton.isVisible()) {
                    saveAction();
                }
                return;
            case B:
                if (saveAsButton != null && !saveAsButton.isDisabled() && saveAsButton.isVisible()) {
                    saveAsAction();
                }
                return;
            case I:
                if (infoButton != null && !infoButton.isDisabled() && infoButton.isVisible()) {
                    infoAction();
                }
                return;
            case D:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (deleteButton != null && !deleteButton.isDisabled() && deleteButton.isVisible()) {
                    deleteAction();
                }
                return;
            case A:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (allButton != null && !allButton.isDisabled() && allButton.isVisible()) {
                    allAction();
                } else if (selectAllButton != null && !selectAllButton.isDisabled() && selectAllButton.isVisible()) {
                    selectAllAction();
                }
                return;
            case O:
                if (selectNoneButton != null && !selectNoneButton.isDisabled() && selectNoneButton.isVisible()) {
                    selectNoneAction();
                }
                return;
            case X:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (cropButton != null && !cropButton.isDisabled() && cropButton.isVisible()) {
                    cropAction();
                }
                return;
            case G:
                if (clearButton != null && !clearButton.isDisabled() && clearButton.isVisible()) {
                    clearAction();
                }
                return;
            case R:
                if (recoverButton != null && !recoverButton.isDisabled() && recoverButton.isVisible()) {
                    recoverAction();
                }
                return;
            case Z:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (undoButton != null && !undoButton.isDisabled() && undoButton.isVisible()) {
                    undoAction();
                }
                return;
            case Y:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (redoButton != null && !redoButton.isDisabled() && redoButton.isVisible()) {
                    redoAction();
                }
                return;
            case P:
                if (popButton != null && !popButton.isDisabled() && popButton.isVisible()) {
                    popAction();
                }
                return;
            case W:
                if (cancelButton != null && !cancelButton.isDisabled() && cancelButton.isVisible()) {
                    cancelAction();
                } else if (withdrawButton != null && !withdrawButton.isDisabled()) {
                    withdrawAction();
                }
                return;
            case MINUS:
                setSceneFontSize(AppVariables.sceneFontSize - 1);
                return;
            case EQUALS:
                setSceneFontSize(AppVariables.sceneFontSize + 1);
        }

    }

    public void keyHandler(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == null) {
            return;
        }
        switch (code) {
            case DELETE:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (deleteButton != null && !deleteButton.isDisabled() && deleteButton.isVisible()) {
                    deleteAction();
                }
                return;
            case HOME:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (firstButton != null && !firstButton.isDisabled() && firstButton.isVisible()) {
                    firstAction();
                } else if (pageFirstButton != null && !pageFirstButton.isDisabled() && pageFirstButton.isVisible()) {
                    pageFirstAction();
                }
                return;
            case END:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (lastButton != null && !lastButton.isDisabled() && lastButton.isVisible()) {
                    lastAction();
                } else if (pageLastButton != null && !pageLastButton.isDisabled() && pageLastButton.isVisible()) {
                    pageLastAction();
                }
                return;
            case PAGE_UP:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (previousButton != null && !previousButton.isDisabled() && previousButton.isVisible()) {
                    previousAction();
                } else if (pagePreviousButton != null && !pagePreviousButton.isDisabled() && pagePreviousButton.isVisible()) {
                    pagePreviousAction();
                }
                return;
            case PAGE_DOWN:
                if (FxmlControl.textInputFocus(this)) {
                    return;
                }
                if (nextButton != null && !nextButton.isDisabled() && nextButton.isVisible()) {
                    nextAction();
                } else if (pageNextButton != null && !pageNextButton.isDisabled() && pageNextButton.isVisible()) {
                    pageNextAction();
                }
                return;
            case F1:
                if (startButton != null && !startButton.isDisabled() && startButton.isVisible()) {
                    startAction();
                } else if (okButton != null && !okButton.isDisabled() && okButton.isVisible()) {
                    okAction();
                } else if (setButton != null && !setButton.isDisabled() && setButton.isVisible()) {
                    setAction();
                } else if (playButton != null && !playButton.isDisabled() && playButton.isVisible()) {
                    playAction();
                } else if (goButton != null && !goButton.isDisabled() && goButton.isVisible()) {
                    goAction();
                }
                return;
            case F2:
                if (saveButton != null && !saveButton.isDisabled() && saveButton.isVisible()) {
                    saveAction();
                }
                return;
            case F3:
                if (recoverButton != null && !recoverButton.isDisabled() && recoverButton.isVisible()) {
                    recoverAction();
                }
                return;
            case F4:
                if (leftPaneControl != null && leftPaneControl.isVisible()) {
                    controlLeftPane();
                }
                return;
            case F5:
                if (rightPaneControl != null && rightPaneControl.isVisible()) {
                    controlRightPane();
                }
                return;
            case F6:
                closePopup(event);
                return;
            case F9:
                closeStage();
                return;
            case F10:
                refresh();
                return;
            case F11:
                if (saveAsButton != null && !saveAsButton.isDisabled() && saveAsButton.isVisible()) {
                    saveAsAction();
                }
                return;
            case ESCAPE:
                if (cancelButton != null && !cancelButton.isDisabled() && cancelButton.isVisible()) {
                    cancelAction();
                } else if (withdrawButton != null && !withdrawButton.isDisabled() && withdrawButton.isVisible()) {
                    withdrawAction();
                }
                closePopup(event);
//                else if (stopButton != null && !stopButton.isDisabled()) {
//                    stopAction();
//                }
        }

        if (!FxmlControl.textInputFocus(this)) {
            controlAltHandler(event);
        }

    }

    public void setInterfaceStyle(Scene scene, String style) {
        try {
            if (scene != null && style != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(BaseController.class.getResource(style).toExternalForm());
            }
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    public void setInterfaceStyle(String style) {
        try {
            if (thisPane != null && style != null) {
                thisPane.getStylesheets().clear();
                if (!CommonValues.MyBoxStyle.equals(style)) {
                    thisPane.getStylesheets().add(BaseController.class.getResource(style).toExternalForm());
                }
                thisPane.getStylesheets().add(BaseController.class.getResource(CommonValues.MyBoxStyle).toExternalForm());
            }
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    public boolean setSceneFontSize(int size) {
        if (thisPane == null) {
            return false;
        }
        AppVariables.setSceneFontSize(size);
        thisPane.setStyle("-fx-font-size: " + size + "px;");
        if (parentController != null) {
            parentController.setSceneFontSize(size);
        }
        return true;
    }

    public boolean setIconSize(int size) {
        if (thisPane == null) {
            return false;
        }
        AppVariables.setIconSize(size);
        if (parentController != null) {
            parentController.setIconSize(size);
        }
        refreshBase();
        return true;
    }

    public BaseController refresh() {
        return refreshBase();
    }

    public BaseController refreshBase() {
        try {
            if (getMyStage() == null || myFxml == null) {
                return null;
            }
            String title = myStage.getTitle();
            BaseController c, p = parentController;
            c = loadScene(myFxml);
            if (c == null) {
                return null;
            }
            if (p != null) {
                c.parentFxml = p.myFxml;
                c.parentController = p;
                p.refresh();
            }
            if (c.getMyStage() != null) {
                c.getMyStage().requestFocus();
                c.getMyStage().setTitle(title);
            }
            return c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public List<MenuItem> getRecentMenu() {
        List<MenuItem> menus = new ArrayList();
        List<VisitHistory> his = VisitHistoryTools.getRecentMenu();
        if (his == null || his.isEmpty()) {
            return menus;
        }
        List<String> valid = new ArrayList();
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            final String fxml = h.getDataMore();
            if (valid.contains(fxml)) {
                continue;
            }
            valid.add(fxml);
            MenuItem menu = new MenuItem(message(fname));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    loadScene(fxml);
                }
            });
            menus.add(menu);
        }
        return menus;

    }

    @FXML
    public void selectSourceFile() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            File file = FxmlControl.selectFile(this);
            if (file == null) {
                return;
            }
            selectSourceFileDo(file);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
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
        recordFileOpened(file);
        selectSourceFileDo(file);
    }

    public void selectSourceFileDo(File file) {
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
        recordFileOpened(file, getSourcePathKey(), SourcePathType, SourceFileType);
    }

    public void recordFileOpened(final File file, int fileType) {
        recordFileOpened(file, VisitHistoryTools.getPathKey(fileType), fileType, fileType);
    }

    private void recordFileOpened(final File file, String sourcePathKey, int pathType, int fileType) {
        if (file == null) {
            return;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            if (file.isDirectory()) {
                String path = file.getPath();
                AppVariables.setUserConfigValue(conn, LastPathKey, path);
                VisitHistoryTools.readPath(conn, pathType, path);
            } else {
                String path = file.getParent();
                String fname = file.getAbsolutePath();
                AppVariables.setUserConfigValue(conn, sourcePathKey, path);
                AppVariables.setUserConfigValue(conn, LastPathKey, path);
                VisitHistoryTools.readPath(conn, pathType, path);
                VisitHistoryTools.readFile(conn, fileType, fname);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void recordFileWritten(String file) {
        recordFileWritten(new File(file));
    }

    public void recordFileWritten(final File file) {
        recordFileWritten(file, targetPathKey, TargetPathType, TargetFileType);
    }

    public void recordFileWritten(final File file, int fileType) {
        recordFileWritten(file, VisitHistoryTools.getPathKey(fileType), fileType, fileType);
    }

    private void recordFileWritten(final File file, String targetPathKey, int TargetPathType, int TargetFileType) {
        if (file == null) {
            return;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            if (file.isDirectory()) {
                String path = file.getPath();
                AppVariables.setUserConfigValue(conn, targetPathKey, path);
                AppVariables.setUserConfigValue(conn, LastPathKey, path);
                VisitHistoryTools.writePath(conn, TargetPathType, path);
            } else {
                String path = file.getParent();
                String fname = file.getAbsolutePath();
                AppVariables.setUserConfigValue(conn, targetPathKey, path);
                AppVariables.setUserConfigValue(conn, LastPathKey, path);
                VisitHistoryTools.writePath(conn, TargetPathType, path);
                VisitHistoryTools.writeFile(conn, TargetFileType, fname);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void recordFileAdded(String file) {
        recordFileOpened(new File(file));
    }

    public void recordFileAdded(final File file) {
        if (file == null) {
            return;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            if (file.isDirectory()) {
                String path = file.getPath();
                AppVariables.setUserConfigValue(conn, sourcePathKey, path);
                AppVariables.setUserConfigValue(conn, LastPathKey, path);
                VisitHistoryTools.readPath(conn, SourcePathType, path);
            } else {
                String path = file.getParent();
                String fname = file.getAbsolutePath();
                AppVariables.setUserConfigValue(conn, sourcePathKey, path);
                AppVariables.setUserConfigValue(conn, LastPathKey, path);
                VisitHistoryTools.readPath(conn, SourcePathType, path);
                VisitHistoryTools.readFile(conn, AddFileType, fname);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void recordFileAdded(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String path = file.getPath();
                    AppVariables.setUserConfigValue(conn, sourcePathKey, path);
                    AppVariables.setUserConfigValue(conn, LastPathKey, path);
                    VisitHistoryTools.readPath(conn, SourcePathType, path);
                } else {
                    String path = file.getParent();
                    String fname = file.getAbsolutePath();
                    AppVariables.setUserConfigValue(conn, sourcePathKey, path);
                    AppVariables.setUserConfigValue(conn, LastPathKey, path);
                    VisitHistoryTools.readPath(conn, SourcePathType, path);
                    VisitHistoryTools.readFile(conn, AddFileType, fname);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void selectTargetPath() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVariables.getUserConfigPath(targetPathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            selectTargetPath(directory);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void selectTargetPath(File directory) {
        if (targetPathInput != null) {
            targetPathInput.setText(directory.getPath());
        }

        recordFileWritten(directory);
        targetPathChanged();
    }

    public void targetPathChanged() {

    }

    @FXML
    public void selectTargetFile() {
        File path = AppVariables.getUserConfigPath(targetPathKey);
        selectTargetFileFromPath(path);
    }

    public void selectTargetFileFromPath(File path) {
        try {
            String name = null;
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(path, name, targetExtensionFilter);
            if (file == null) {
                return;
            }
            selectTargetFile(file);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
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
//            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void selectSourcePath() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVariables.getUserConfigPath(sourcePathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            selectSourcePath(directory);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void selectSourcePath(File directory) {
        if (sourcePathInput != null) {
            sourcePathInput.setText(directory.getPath());
        }
        recordFileOpened(directory);
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
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        RecentVisitMenu menu = makeSourceFileRecentVisitMenu(event);
        if (menu != null) {
            menu.pop();
        }
    }

    public RecentVisitMenu makeSourceFileRecentVisitMenu(MouseEvent event) {
        RecentVisitMenu menu = new RecentVisitMenu(this, event) {

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

        };
        return menu;
    }

    @FXML
    public void popFileAdd(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentAddFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 4 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.AddPathType = controller.SourcePathType;
                }
                return VisitHistoryTools.getRecentPath(controller.getAddPathType(), pathNumber);
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

        }.pop();
    }

    @FXML
    public void popFileInsert(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentAddFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 4 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.AddPathType = controller.SourcePathType;
                }
                return VisitHistoryTools.getRecentPath(controller.getAddPathType(), pathNumber);
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

        }.pop();
    }

    @FXML
    public void popDirectoryAdd(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 4 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.AddPathType = controller.SourcePathType;
                }
                return VisitHistoryTools.getRecentPath(controller.getAddPathType(), pathNumber);
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
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 4 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.AddPathType = controller.SourcePathType;
                }
                return VisitHistoryTools.getRecentPath(controller.getAddPathType(), pathNumber);
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
        if (AppVariables.fileRecentNumber <= 0) {
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
        if (AppVariables.fileRecentNumber <= 0) {
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
        if (AppVariables.fileRecentNumber <= 0) {
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
        if (AppVariables.fileRecentNumber <= 0) {
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

    public File makeTargetFile(File sourceFile, File targetPath) {
        if (sourceFile.isDirectory()) {
            return makeTargetFile(sourceFile.getName(), "", targetPath);
        } else {
            return makeTargetFile(sourceFile.getName(), targetPath);
        }
    }

    //
    public File makeTargetFile(String fileName, File targetPath) {
        try {
            if (fileName == null || targetPath == null) {
                return null;
            }
            String namePrefix = FileTools.namePrefix(fileName);
            String nameSuffix;
            if (targetFileSuffix != null) {
                nameSuffix = "." + targetFileSuffix;
            } else {
                nameSuffix = FileTools.getFileSuffix(fileName);
                if (nameSuffix != null && !nameSuffix.isEmpty()) {
                    nameSuffix = "." + nameSuffix;
                } else {
                    nameSuffix = "";
                }
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

    public File makeTargetFile(String namePrefix, String nameSuffix, File targetPath) {
        try {
            String targetPrefix = targetPath.getAbsolutePath() + File.separator + FileTools.filenameFilter(namePrefix);
            String targetSuffix = FileTools.filenameFilter(nameSuffix);
            File target = new File(targetPrefix + targetSuffix);
            if (target.exists()) {
                if (targetExistType == TargetExistType.Skip) {
                    target = null;
                } else if (targetExistType == TargetExistType.Rename) {
                    if (targetAppendInput != null) {
                        targetNameAppend = targetAppendInput.getText().trim();
                    }
                    if (targetNameAppend == null || targetNameAppend.isEmpty()) {
                        targetNameAppend = "_m";
                    }
                    while (true) {
                        targetPrefix = targetPrefix + targetNameAppend;
                        target = new File(targetPrefix + targetSuffix);
                        if (!target.exists()) {
                            break;
                        }
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @FXML
    public void link(ActionEvent event) {
        try {
            Hyperlink link = (Hyperlink) event.getSource();
            openLink(link.getText());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void openLink(String address) {
        if (address == null || address.isBlank()) {
            return;
        }
        WebBrowserController.oneOpen(address);
    }

    public void openLink(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        WebBrowserController.oneOpen(file);
    }

    @FXML
    public void regexHelp() {
        try {
            String link;
            switch (AppVariables.getLanguage()) {
                case "zh":
                    link = "https://baike.baidu.com/item/%E6%AD%A3%E5%88%99%E8%A1%A8%E8%BE%BE%E5%BC%8F/1700215";
                    break;
                default:
                    link = "https://en.wikipedia.org/wiki/Regular_expression";
            }
            openLink(link);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void derbyHelp() {
        openLink("http://db.apache.org/derby/docs/10.15/ref/index.html");
    }

    @FXML
    public void okAction() {

    }

    @FXML
    public void startAction() {

    }

    @FXML
    public void playAction() {

    }

    @FXML
    public void goAction() {

    }

    @FXML
    public void stopAction() {

    }

    @FXML
    public void createAction() {

    }

    @FXML
    public void addAction(ActionEvent event) {

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
    public void redoAction() {

    }

    @FXML
    public void undoAction() {

    }

    @FXML
    public void allAction() {

    }

    @FXML
    public void clearAction() {

    }

    @FXML
    public void cancelAction() {

    }

    @FXML
    public void closeAction() {
        closeStage();
    }

    @FXML
    public void infoAction() {

    }

    @FXML
    public void setAction() {

    }

    @FXML
    public void selectAllAction() {

    }

    @FXML
    public void selectNoneAction() {

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
    public void pageNextAction() {

    }

    @FXML
    public void pagePreviousAction() {

    }

    @FXML
    public void pageFirstAction() {

    }

    @FXML
    public void pageLastAction() {

    }

    @FXML
    public void popAction() {

    }

    @FXML
    public void withdrawAction() {

    }

    @FXML
    public void closePopup(KeyEvent event) {
        if (popMenu != null) {
            popMenu.hide();
        }
        if (popup != null) {
            popup.hide();
        }
    }

    @FXML
    public void mybox(ActionEvent event) {
        openStage(CommonValues.MyboxFxml);
    }

    public void clearUserSettings() {
        if (!FxmlControl.askSure(getBaseTitle(), message("ClearPersonalSettings"), message("SureClear"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        new TableUserConf().clear();
                        AppVariables.initAppVaribles();
                        return true;
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    refresh();
                    popSuccessful();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void cleanAppPath() {
        try {
            File userPath = new File(MyboxDataPath);
            if (userPath.exists()) {
                File[] files = userPath.listFiles();
                if (files == null) {
                    return;
                }
                for (File f : files) {
                    if (f.isDirectory() && !AppVariables.MyBoxReservePaths.contains(f)) {
                        FileTools.deleteDir(f);
                    } else if (!f.equals(AppVariables.MyboxConfigFile)) {
                        FileTools.delete(f);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void view(File file) {
        FxmlStage.openTarget(null, file.getAbsolutePath());
    }

    public void view(String file) {
        FxmlStage.openTarget(null, file);
    }

    public void browse(String url) {
        try {
            browseURI(new URI(url));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void browseURI(URI uri) {
        FxmlStage.browseURI(getMyStage(), uri);
    }

    @FXML
    public void openDataPath(ActionEvent event) {
        try {
            browseURI(new File(MyboxDataPath).toURI());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public BaseController loadScene(String newFxml) {
        try {
            if (!leavingScene()) {
                return null;
            }
            return FxmlStage.openScene(getMyStage(), newFxml);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public BaseController openStage(String newFxml) {
        return openStage(newFxml, false);
    }

    public BaseController openStage(String newFxml, boolean isOwned) {
        return FxmlStage.openStage(getMyStage(), newFxml, isOwned);
    }

    public boolean closeStage() {
        if (leavingScene()) {
            FxmlStage.closeStage(getMyStage());
            return true;
        } else {
            return false;
        }
    }

    public void recordStageStatus() {
        if (getMyStage() == null) {
            return;
        }
        final String prefix = interfaceKeysPrefix();
        AppVariables.setUserConfigInt(prefix + "StageX", (int) myStage.getX());
        AppVariables.setUserConfigInt(prefix + "StageY", (int) myStage.getY());
        AppVariables.setUserConfigInt(prefix + "StageWidth", (int) myStage.getWidth());
        AppVariables.setUserConfigInt(prefix + "StageHeight", (int) myStage.getHeight());
    }

    public boolean leavingScene() {
        try {
            if (!checkBeforeNextAction()) {
                return false;
            }

            if (mainMenuController != null) {
                mainMenuController.stopMemoryMonitorTimer();
                mainMenuController.stopCpuMonitorTimer();
                mainMenuController = null;
            }

            if (maximizedListener != null) {
                getMyStage().maximizedProperty().removeListener(maximizedListener);
                maximizedListener = null;
            }
            if (fullscreenListener != null) {
                getMyStage().fullScreenProperty().removeListener(fullscreenListener);
                fullscreenListener = null;
            }

            recordStageStatus();

            hidePopup();
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (task != null && !task.isQuit()) {
                if (!FxmlControl.askSure(getMyStage().getTitle(), message("TaskRunning"))) {
                    return false;
                }
                if (task != null) {
                    task.cancel();
                    task = null;
                }
            }

            if (backgroundTask != null && !backgroundTask.isQuit()) {
                backgroundTask.cancel();
                backgroundTask = null;
            }

            parentController = null;
            myController = null;
            popupTimer = null;
            timer = null;
            popup = null;
            popMenu = null;
            leftDividerListener = null;
            rightDividerListener = null;
            currentKeyEvent = null;
            myScene = null;
            myStage = null;

            System.gc();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    public boolean checkBeforeNextAction() {
        return true;
    }

    public File chooseSaveFile(int type, String defaultName) {
        return chooseSaveFile(VisitHistoryTools.getSavedPath(type), defaultName,
                VisitHistoryTools.getExtensionFilter(type));
    }

    public File chooseSaveFile(File defaultPath, String defaultName,
            List<FileChooser.ExtensionFilter> filters) {
        return chooseSaveFile(null, defaultPath, defaultName, filters);
    }

    public File chooseSaveFile(String title, File defaultPath, String defaultName,
            List<FileChooser.ExtensionFilter> filters) {
        try {
            FileChooser fileChooser = new FileChooser();
            if (title != null) {
                fileChooser.setTitle(title);
            }
            if (defaultPath != null && defaultPath.exists()) {
                fileChooser.setInitialDirectory(defaultPath);
            }
            String name = defaultName;
            String suffix = null;
            if (filters != null) {
                suffix = FileTools.getFileSuffix(filters.get(0).getExtensions().get(0));
                fileChooser.getExtensionFilters().addAll(filters);
            }
            if ("*".equals(suffix)) {
                suffix = null;
            }
            if (suffix != null) {
                if (name == null) {
                    name = "." + suffix;
                } else {
                    if (FileTools.getFileSuffix(name).isEmpty()) {
                        name += "." + suffix;
                    }
                }
            }
            if (name != null) {
                name = FileTools.filenameFilter(name);
                fileChooser.setInitialFileName(name);
            }

            File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return null;
            }

            // https://stackoverflow.com/questions/20637865/javafx-2-2-get-selected-file-extension
            // This is a pretty annoying thing in JavaFX - they will automatically append the extension on Windows, but not on Linux or Mac.
            if (FileTools.getFileSuffix(file.getName()).isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                if (myStage != null) {
                    alert.setTitle(myStage.getTitle());
                }
                alert.setHeaderText(null);
                alert.setContentText(message("SureNoFileExtension"));
                ButtonType buttonSure = new ButtonType(message("Sure"));
                ButtonType buttonNo = new ButtonType(message("No"));
                ButtonType buttonCancel = new ButtonType(message("Cancel"));
                alert.getButtonTypes().setAll(buttonCancel, buttonNo, buttonSure);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() != buttonSure) {
                    if (result.get() == buttonNo) {
                        return chooseSaveFile(title, defaultPath, defaultName, filters);
                    } else {
                        return null;
                    }
                }
            }
            return file;

        } catch (Exception e) {
            return null;
        }

    }

    public void alertError(String information) {
        FxmlStage.alertError(getMyStage(), information);
    }

    public void alertWarning(String information) {
        FxmlStage.alertError(getMyStage(), information);
    }

    public void alertInformation(String information) {
        FxmlStage.alertInformation(getMyStage(), information);
    }

    public Popup getPopup() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
        popup = new Popup();
        popup.setAutoHide(true);
        return popup;
    }

    public void popText(String text, int duration, String bgcolor, String color, String size, Region attach) {
        try {
            if (popup != null) {
                popup.hide();
            }
            popup = getPopup();
            popup.setAutoFix(true);
            Label popupLabel = new Label(text);
            popupLabel.setStyle("-fx-background-color:" + bgcolor + ";"
                    + " -fx-text-fill: " + color + ";"
                    + " -fx-font-size: " + size + ";"
                    + " -fx-padding: 10px;"
                    + " -fx-background-radius: 6;");
            popup.setAutoFix(true);
            popup.getContent().add(popupLabel);
            popupLabel.setWrapText(true);
            popupLabel.setMinHeight(Region.USE_PREF_SIZE);
            popupLabel.applyCss();

            if (duration > 0) {
                if (popupTimer != null) {
                    popupTimer.cancel();
                }
                popupTimer = getPopupTimer();
                popupTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            hidePopup();
                        });
                    }
                }, duration);
            }

            if (attach != null) {
                FxmlControl.locateUp(attach, popup);
            } else {
                popup.show(getMyStage());
            }
        } catch (Exception e) {

        }
    }

    public void popInformation(String text, int duration, String size) {
        popText(text, duration, getPopTextbgColor(), getPopInfoColor(), size, null);
    }

    public void popInformation(String text, int duration) {
        popInformation(text, duration, getPopTextSize());
    }

    public void popInformation(String text, Region attach) {
        popText(text, getPopTextDuration(), getPopTextbgColor(), getPopInfoColor(), getPopTextSize(), attach);
    }

    public void popInformation(String text) {
        popInformation(text, getPopTextDuration(), getPopTextSize());
    }

    public void popSuccessful() {
        popInformation(message("Successful"));
    }

    public void popError(String text, int duration, String size) {
        popText(text, duration, getPopTextbgColor(), getPopErrorColor(), size, null);
    }

    public void popError(String text) {
        popError(text, getPopTextDuration(), getPopTextSize());
    }

    public void popFailed() {
        popError(message("Failed"));
    }

    public void popWarn(String text, int duration, String size) {
        popText(text, duration, getPopTextbgColor(), getPopWarnColor(), size, null);
    }

    public void popWarn(String text, int duration) {
        popWarn(text, duration, getPopTextSize());
    }

    public void popWarn(String text) {
        popWarn(text, getPopTextDuration(), getPopTextSize());
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
                    if (myStage.getUserData() == null) {
                        myStage.setUserData(this);
                    }
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
                    = FxmlStage.openLoadingStage(getMyStage(), block, info);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public LoadingController openHandlingStage(final Task<?> task, Modality block) {
        return openHandlingStage(task, block, null);
    }

    public LoadingController openHandlingStage(final Task<?> task, Modality block, String info) {
        try {
            final LoadingController controller
                    = FxmlStage.openLoadingStage(getMyStage(), block, task, info);
            controller.parentController = myController;

            task.setOnSucceeded((WorkerStateEvent event) -> {
                controller.closeStage();
            });
            task.setOnCancelled((WorkerStateEvent event) -> {
                popInformation(AppVariables.message("Canceled"));
                controller.closeStage();
            });
            task.setOnFailed((WorkerStateEvent event) -> {
                popError(AppVariables.message("Error"));
                controller.closeStage();
            });
            return controller;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public void taskCanceled(Task task) {

    }

    public String getBaseTitle() {
        if (baseTitle == null && myStage != null) {
            baseTitle = myStage.getTitle();
            if (baseTitle == null) {
                baseTitle = AppVariables.message("AppTitle");
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

    public void multipleFilesGenerated(final List<String> fileNames) {
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return;
            }
            String path = new File(fileNames.get(0)).getParent();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(getMyStage().getTitle());
            String info = MessageFormat.format(AppVariables.message("GeneratedFilesResult"),
                    fileNames.size(), "\"" + path + "\"");
            int num = fileNames.size();
            if (num > 10) {
                num = 10;
            }
            for (int i = 0; i < num; ++i) {
                info += "\n    " + fileNames.get(i);
            }
            if (fileNames.size() > num) {
                info += "\n    ......";
            }
            alert.setContentText(info);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonOpen = new ButtonType(AppVariables.message("OpenTargetPath"));
            ButtonType buttonBrowse = new ButtonType(AppVariables.message("Browse"));
            ButtonType buttonBrowseNew = new ButtonType(AppVariables.message("BrowseInNew"));
            ButtonType buttonClose = new ButtonType(AppVariables.message("Close"));
            alert.getButtonTypes().setAll(buttonBrowseNew, buttonBrowse, buttonOpen, buttonClose);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonOpen) {
                browseURI(new File(path).toURI());
                recordFileOpened(path);
            } else if (result.get() == buttonBrowse) {
                final ImagesBrowserController controller = FxmlStage.openImagesBrowser(getMyStage());
                if (controller != null) {
                    controller.loadFiles(fileNames);
                }
            } else if (result.get() == buttonBrowseNew) {
                final ImagesBrowserController controller = FxmlStage.openImagesBrowser(null);
                if (controller != null) {
                    controller.loadFiles(fileNames);
                }
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void dataChanged() {

    }

    // pick coordinate from outside
    public void setCoordinate(double longitude, double latitude) {
    }

    // pick GeographyCode from outside
    public void setGeographyCode(GeographyCode code) {
    }

    @FXML
    public void myboxInternetDataPath() {
        browseURI(new File(CommonValues.MyBoxInternetDataPath).toURI());
    }

    /*
        Task
     */
    public class SingletonTask<Void> extends BaseTask<Void> {

        @Override
        protected void whenSucceeded() {
            popSuccessful();
        }

        @Override
        protected void whenFailed() {
            if (error != null) {
                popError(error);
                MyBoxLog.debug(error);
            } else {
                popFailed();
            }
        }

    };

    /*
        get/set
     */
    public void setMyStage(Stage myStage) {
        this.myStage = myStage;
    }

    public void setMyScene(Scene myScene) {
        this.myScene = myScene;
    }

    public Pane getThisPane() {
        return thisPane;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getMyFxml() {
        return myFxml;
    }

    public String getLastPathKey() {
        return LastPathKey;
    }

    public String getTargetPathKey() {
        return targetPathKey;
    }

    public String getSourcePathKey() {
        if (sourcePathKey == null) {
            setFileType();
        }
        return sourcePathKey;
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public int getSourceFileType() {
        if (SourceFileType < 0) {
            setFileType();
        }
        return SourceFileType;
    }

    public int getSourcePathType() {
        return SourcePathType;
    }

    public int getTargetFileType() {
        return TargetFileType;
    }

    public int getTargetPathType() {
        return TargetPathType;
    }

    public int getAddFileType() {
        return AddFileType;
    }

    public int getAddPathType() {
        return AddPathType;
    }

    public List<FileChooser.ExtensionFilter> getSourceExtensionFilter() {
        if (sourceExtensionFilter == null) {
            setFileType();
        }
        return sourceExtensionFilter;
    }

    public void setSourceExtensionFilter(List<FileChooser.ExtensionFilter> sourceExtensionFilter) {
        this.sourceExtensionFilter = sourceExtensionFilter;
    }

    public void setParentFxml(String parentFxml) {
        this.parentFxml = parentFxml;
    }

    public BaseController getParentController() {
        return parentController;
    }

    public void setParentController(BaseController parentController) {
        this.parentController = parentController;
    }

    public MainMenuController getMainMenuController() {
        return mainMenuController;
    }

    public ContextMenu getPopMenu() {
        return popMenu;
    }

    public void setPopMenu(ContextMenu popMenu) {
        this.popMenu = popMenu;
    }

    public SingletonTask<Void> getTask() {
        return task;
    }

    public void setTask(SingletonTask<Void> task) {
        this.task = task;
    }

}
