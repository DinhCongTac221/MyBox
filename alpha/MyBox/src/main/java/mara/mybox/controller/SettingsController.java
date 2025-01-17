package mara.mybox.controller;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import mara.mybox.MyBox;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.DerbyBase.DerbyStatus;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.db.table.TableVisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.fxml.WindowTools.refreshInterfaceAll;
import static mara.mybox.fxml.WindowTools.reloadAll;
import static mara.mybox.fxml.WindowTools.styleAll;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-10-14
 * @Description
 * @License Apache License Version 2.0
 */
public class SettingsController extends BaseController {

    protected int recentFileNumber, newJVM;

    @FXML
    protected Tab interfaceTab, baseTab, pdfTab, imageTab, dataTab, mapTab;
    @FXML
    protected ToggleGroup langGroup, pdfMemGroup, controlColorGroup, derbyGroup, splitPanesGroup;
    @FXML
    protected CheckBox closeCurrentCheck, recordWindowsSizeLocationCheck, clearExpiredCheck,
            anchorSolidCheck, controlsTextCheck, shortcutsCanNotOmitCheck, icons40pxCheck,
            commitLoseFocusCheck, copyCurrentDataPathCheck, clearCurrentRootCheck, splitPaneSensitiveCheck,
            mousePassControlPanesCheck, popColorSetCheck, stopAlarmCheck;
    @FXML
    protected TextField jvmInput, dataDirInput, batchInput, fileRecentInput, thumbnailWidthInput,
            tiandituWebKeyInput, gaodeWebKeyInput, gaodeServiceKeyInput,
            webConnectTimeoutInput, webReadTimeoutInput;
    @FXML
    protected VBox localBox, dataBox;
    @FXML
    protected ComboBox<String> fontSizeBox, iconSizeBox, scrollSizeSelector,
            strokeWidthBox, anchorWidthBox, gridWidthSelector, gridIntervalSelector, gridOpacitySelector,
            popSizeSelector, popDurationSelector;
    @FXML
    protected HBox pdfMemBox, imageHisBox, derbyBox;
    @FXML
    protected Button settingsRecentOKButton, settingsChangeRootButton,
            settingsDataPathButton, settingsJVMButton;
    @FXML
    protected RadioButton chineseRadio, englishRadio, embeddedRadio, networkRadio,
            pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio,
            redRadio, orangeRadio, pinkRadio, lightBlueRadio, blueRadio, greenRadio, colorCustomizeRadio;
    @FXML
    protected Rectangle colorCustomizeRect;
    @FXML
    protected ColorSetController strokeColorSetController, anchorColorSetController, gridColorSetController, alphaColorSetController,
            popBgColorController, popInfoColorController, popErrorColorController, popWarnColorController;
    @FXML
    protected ListView languageList;
    @FXML
    protected Label alphaLabel, currentJvmLabel, currentDataPathLabel, currentTempPathLabel,
            derbyStatus;
    @FXML
    protected ControlImageRender renderController;

    public SettingsController() {
        baseTitle = message("Settings");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initInterfaceTab();
            initBaseTab();
            initDataTab();
            initPdfTab();
            initImageTab();
            initMapTab();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            isSettingValues = true;
            initSettingValues();
            isSettingValues = false;

            NodeStyleTools.setTooltip(redRadio, new Tooltip(message("MyBoxColorRedDark")));
            NodeStyleTools.setTooltip(pinkRadio, new Tooltip(message("MyBoxColorPinkDark")));
            NodeStyleTools.setTooltip(orangeRadio, new Tooltip(message("MyBoxColorOrangeDark")));
            NodeStyleTools.setTooltip(lightBlueRadio, new Tooltip(message("MyBoxColorLightBlueDark")));
            NodeStyleTools.setTooltip(blueRadio, new Tooltip(message("MyBoxColorBlueDark")));
            NodeStyleTools.setTooltip(greenRadio, new Tooltip(message("MyBoxColorGreenDark")));
            NodeStyleTools.setTooltip(colorCustomizeRadio, new Tooltip(message("Customize")));
            NodeStyleTools.setTooltip(imageHisBox, new Tooltip(message("ImageHisComments")));

            colorCustomizeRect.setFill(Colors.customizeColorDark());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void initSettingValues() {
        try {
            clearExpiredCheck.setSelected(UserConfig.getBoolean("ClearExpiredDataBeforeExit", true));
            stopAlarmCheck.setSelected(UserConfig.getBoolean("StopAlarmsWhenExit"));
            closeCurrentCheck.setSelected(AppVariables.closeCurrentWhenOpenTool);

            thumbnailWidthInput.setText(AppVariables.thumbnailWidth + "");

            recentFileNumber = UserConfig.getInt("FileRecentNumber", 20);
            fileRecentInput.setText(recentFileNumber + "");

            switch (AppVariables.ControlColor) {
                case Pink:
                    pinkRadio.setSelected(true);
                    break;
                case Blue:
                    blueRadio.setSelected(true);
                    break;
                case LightBlue:
                    lightBlueRadio.setSelected(true);
                    break;
                case Orange:
                    orangeRadio.setSelected(true);
                    break;
                case Green:
                    greenRadio.setSelected(true);
                    break;
                case Customize:
                    colorCustomizeRadio.setSelected(true);
                    break;
                default:
                    redRadio.setSelected(true);

            }

            controlsTextCheck.setSelected(AppVariables.controlDisplayText);
            icons40pxCheck.setSelected(AppVariables.icons40px);
            splitPaneSensitiveCheck.setSelected(UserConfig.getBoolean("ControlSplitPanesSensitive", false));
            mousePassControlPanesCheck.setSelected(UserConfig.getBoolean("MousePassControlPanes", true));
            popColorSetCheck.setSelected(UserConfig.getBoolean("PopColorSetWhenMouseHovering", true));
            shortcutsCanNotOmitCheck.setSelected(AppVariables.ShortcutsCanNotOmitCtrlAlt);
            commitLoseFocusCheck.setSelected(AppVariables.commitModificationWhenDataCellLoseFocus);

            checkLanguage();
            checkPdfMem();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }


    /*
        Interface settings
     */
    public void initInterfaceTab() {
        try {

            langGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkLanguage();
                }
            });

            fontSizeBox.getItems().addAll(Arrays.asList(
                    "9", "10", "12", "14", "15", "16", "17", "18", "19", "20", "21", "22")
            );
            fontSizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                setSceneFontSize(v);
                                ValidationTools.setEditorNormal(fontSizeBox);
                            } else {
                                ValidationTools.setEditorBadStyle(fontSizeBox);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(fontSizeBox);
                        }
                    }
                }
            });
            fontSizeBox.getSelectionModel().select(AppVariables.sceneFontSize + "");

            iconSizeBox.getItems().addAll(Arrays.asList(
                    "20", "15", "25", "18", "22", "12", "10")
            );
            iconSizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                setIconSize(v);
                                ValidationTools.setEditorNormal(iconSizeBox);
                            } else {
                                ValidationTools.setEditorBadStyle(iconSizeBox);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(iconSizeBox);
                        }
                    }
                }
            });
            iconSizeBox.getSelectionModel().select(AppVariables.iconSize + "");

            closeCurrentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    UserConfig.setBoolean("CloseCurrentWhenOpenTool", closeCurrentCheck.isSelected());
                    AppVariables.closeCurrentWhenOpenTool = closeCurrentCheck.isSelected();
                }
            });

            recordWindowsSizeLocationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    UserConfig.setBoolean("RecordWindowsSizeLocation", recordWindowsSizeLocationCheck.isSelected());
                    AppVariables.recordWindowsSizeLocation = recordWindowsSizeLocationCheck.isSelected();
                }
            });

            controlColorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkControlsColor();
                }
            });

            controlsTextCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.controlDisplayText = controlsTextCheck.isSelected();
                    UserConfig.setBoolean("ControlDisplayText", AppVariables.controlDisplayText);
                    refreshInterfaceAll();
                }
            });

            commitLoseFocusCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.commitModificationWhenDataCellLoseFocus = commitLoseFocusCheck.isSelected();
                    UserConfig.setBoolean("CommitModificationWhenDataCellLoseFocus", AppVariables.commitModificationWhenDataCellLoseFocus);
                }
            });

            icons40pxCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.icons40px = icons40pxCheck.isSelected();
                    UserConfig.setBoolean("Icons40px", AppVariables.icons40px);
                    refreshInterfaceAll();
                }
            });

            popSizeSelector.getItems().addAll(Arrays.asList(
                    "1.5", "1", "1.2", "2", "2.5", "0.8")
            );
            popSizeSelector.setValue(UserConfig.getString("PopTextSize", "1.5"));
            popSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            float f = Float.parseFloat(newValue);
                            if (f > 0) {
                                UserConfig.setString("PopTextSize", newValue);
                                ValidationTools.setEditorNormal(popSizeSelector);
                                popSuccessful();
                            } else {
                                ValidationTools.setEditorBadStyle(popSizeSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(popSizeSelector);
                        }
                    }
                }
            });

            popDurationSelector.getItems().addAll(Arrays.asList(
                    "3000", "5000", "2000", "1500", "1000", "4000", "2500")
            );
            popDurationSelector.setValue(UserConfig.getInt("PopTextDuration", 3000) + "");
            popDurationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                UserConfig.setInt("PopTextDuration", v);
                                ValidationTools.setEditorNormal(popDurationSelector);
                                popSuccessful();
                            } else {
                                ValidationTools.setEditorBadStyle(popDurationSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(popDurationSelector);
                        }
                    }
                }
            });

            popBgColorController.init(this, "PopTextBgColor", Color.BLACK);
            popBgColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    UserConfig.setString("PopTextBgColor", popBgColorController.rgb());
                    popSuccessful();
                }
            });

            popInfoColorController.init(this, "PopInfoColor", Color.WHITE);
            popInfoColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    UserConfig.setString("PopInfoColor", popInfoColorController.rgb());
                    popSuccessful();
                }
            });

            popErrorColorController.init(this, "PopErrorColor", Color.AQUA);
            popErrorColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    UserConfig.setString("PopErrorColor", popErrorColorController.rgb());
                    popSuccessful();
                }
            });

            popWarnColorController.init(this, "PopWarnColor", Color.ORANGE);
            popWarnColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    UserConfig.setString("PopWarnColor", popWarnColorController.rgb());
                    popSuccessful();
                }
            });

            scrollSizeSelector.getItems().addAll(Arrays.asList(
                    "100", "500", "1000", "20", "50", "200", Integer.MAX_VALUE + "")
            );
            scrollSizeSelector.setValue(UserConfig.selectorScrollSize() + "");
            scrollSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> vv, String ov, String nv) {
                    if (nv != null && !nv.isEmpty()) {
                        try {
                            int v = Integer.parseInt(nv);
                            if (v > 0) {
                                UserConfig.setInt("SelectorScrollSize", v);
                                ValidationTools.setEditorNormal(scrollSizeSelector);
                            } else {
                                ValidationTools.setEditorBadStyle(scrollSizeSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(scrollSizeSelector);
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkLanguage() {
        if (AppVariables.currentBundle == Languages.BundleZhCN) {
            chineseRadio.setSelected(true);
        } else {
            englishRadio.setSelected(true);
        }
    }

    protected void checkControlsColor() {
        try {
            if (isSettingValues) {
                return;
            }
            if (pinkRadio.isSelected()) {
                StyleTools.setConfigStyleColor(this, "Pink");
            } else if (lightBlueRadio.isSelected()) {
                StyleTools.setConfigStyleColor(this, "LightBlue");
            } else if (blueRadio.isSelected()) {
                StyleTools.setConfigStyleColor(this, "Blue");
            } else if (orangeRadio.isSelected()) {
                StyleTools.setConfigStyleColor(this, "Orange");
            } else if (greenRadio.isSelected()) {
                StyleTools.setConfigStyleColor(this, "Green");
            } else if (colorCustomizeRadio.isSelected()) {
                StyleTools.setConfigStyleColor(this, "Customize");
            } else {
                StyleTools.setConfigStyleColor(this, "Red");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void setStyle(String style) {
        try {
            UserConfig.setString("InterfaceStyle", style);
            styleAll(style);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void setChinese(ActionEvent event) {
        Languages.setLanguage("zh");
        reloadAll();
    }

    @FXML
    protected void setEnglish(ActionEvent event) {
        Languages.setLanguage("en");
        reloadAll();
    }

    @FXML
    protected void mousePassControlPanes() {
        UserConfig.setBoolean("MousePassControlPanes", mousePassControlPanesCheck.isSelected());
    }

    @FXML
    protected void popColorSet() {
        UserConfig.setBoolean("PopColorSetWhenMouseHovering", popColorSetCheck.isSelected());
    }

    @FXML
    protected void shortcutsCanNotOmit() {
        AppVariables.ShortcutsCanNotOmitCtrlAlt = shortcutsCanNotOmitCheck.isSelected();
        UserConfig.setBoolean("ShortcutsCanNotOmitCtrlAlt", AppVariables.ShortcutsCanNotOmitCtrlAlt);
    }

    @FXML
    protected void inputColorAction() {
        SettingCustomColorsController.open(this);
    }

    @FXML
    protected void splitPaneSensitive() {
        UserConfig.setBoolean("ControlSplitPanesSensitive", splitPaneSensitiveCheck.isSelected());
    }

    /*
        Base settings
     */
    public void initBaseTab() {
        try {
            int mb = 1024 * 1024;
            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            final long totalM = osmxb.getTotalMemorySize() / mb;
            String m = message("PhysicalMemory") + ": " + totalM + "MB";
            Runtime r = Runtime.getRuntime();
            final long jvmM = r.maxMemory() / mb;
            m += "    " + message("JvmXmx") + ": " + jvmM + "MB";
            currentJvmLabel.setText(m);
            jvmInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(jvmInput.getText());
                        if (v > 50 && v <= totalM - 50) {
                            jvmInput.setStyle(null);
                            if (jvmM == v) {
                                settingsJVMButton.setDisable(true);
                                return;
                            }
                            newJVM = v;
                            settingsJVMButton.setDisable(false);
                        } else {
                            jvmInput.setStyle(UserConfig.badStyle());
                            settingsJVMButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        jvmInput.setStyle(UserConfig.badStyle());
                        settingsJVMButton.setDisable(true);
                    }
                }
            });
            isSettingValues = true;
            jvmInput.setText(jvmM + "");
            settingsJVMButton.setDisable(true);
            isSettingValues = false;

            webConnectTimeoutInput.setText(UserConfig.getInt("WebConnectTimeout", 10000) + "");
            webReadTimeoutInput.setText(UserConfig.getInt("WebReadTimeout", 10000) + "");

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void setJVM() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    long defaultJVM = Runtime.getRuntime().maxMemory() / (1024 * 1024);
                    if (newJVM == defaultJVM) {
                        ConfigTools.writeConfigValue("JVMmemory", null);
                    } else {
                        ConfigTools.writeConfigValue("JVMmemory", "-Xms" + newJVM + "m");
                    }
                    MyBox.restart();
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
        });
    }

    @FXML
    protected void defaultJVM() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long defaultJVM = osmxb.getTotalMemorySize() / (4 * 1024 * 1024);
        jvmInput.setText(defaultJVM + "");
    }

    @FXML
    protected void okWebConnectTimeout() {
        try {
            int v = Integer.parseInt(webConnectTimeoutInput.getText());
            if (v > 0) {
                UserConfig.setInt("WebConnectTimeout", v);
                webConnectTimeoutInput.setStyle(null);
                popSuccessful();
            } else {
                webConnectTimeoutInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            webConnectTimeoutInput.setStyle(UserConfig.badStyle());
        }
    }

    @FXML
    protected void okWebReadTimeout() {
        try {
            int v = Integer.parseInt(webReadTimeoutInput.getText());
            if (v > 0) {
                UserConfig.setInt("WebReadTimeout", v);
                webReadTimeoutInput.setStyle(null);
                popSuccessful();
            } else {
                webReadTimeoutInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            webReadTimeoutInput.setStyle(UserConfig.badStyle());
        }
    }


    /*
        Data settings
     */
    public void initDataTab() {
        try {
            fileRecentInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkRecentFile();
                }
            });

            dataDirInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    String p = dataDirInput.getText();
                    if (isSettingValues || p == null || p.trim().isEmpty()
                            || p.trim().equals(AppVariables.MyboxDataPath)) {
                        settingsChangeRootButton.setDisable(true);
                        return;
                    }
                    settingsChangeRootButton.setDisable(false);
                }
            });
            dataDirInput.setText(AppVariables.MyboxDataPath);
            currentDataPathLabel.setText(MessageFormat.format(message("CurrentValue"), AppVariables.MyboxDataPath));
            clearCurrentRootCheck.setText(MessageFormat.format(message("ClearPathWhenChange"), AppVariables.MyboxDataPath));

            setDerbyMode();
            derbyGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle old_val, Toggle new_val) {
                    checkDerbyMode();
                }
            });

            batchInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        long v = Long.parseLong(batchInput.getText());
                        if (v > 0) {
                            batchInput.setStyle(null);
                            Database.BatchSize = v;
                            UserConfig.setLong("DatabaseBatchSize", v);
                        } else {
                            batchInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        batchInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            batchInput.setText(Database.BatchSize + "");

            clearExpiredCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    UserConfig.setBoolean("ClearExpiredDataBeforeExit", clearExpiredCheck.isSelected());
                }
            });

            stopAlarmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    UserConfig.setBoolean("StopAlarmsWhenExit", stopAlarmCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void selectDataPath(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(new File(AppVariables.MyboxDataPath));
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            recordFileWritten(directory);
            dataDirInput.setText(directory.getPath());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void changeDataPath(ActionEvent event) {
        try {
            String newPath = dataDirInput.getText();
            if (isSettingValues || newPath == null || newPath.trim().isEmpty()
                    || newPath.trim().equals(AppVariables.MyboxDataPath)) {
                return;
            }
            String oldPath = AppVariables.MyboxDataPath;
            if (copyCurrentDataPathCheck.isSelected()) {
                if (!PopTools.askSure(getTitle(), message("ChangeDataPathConfirm"))) {
                    return;
                }
                popInformation(message("CopyingFilesFromTo"));
                if (FileCopyTools.copyWholeDirectory(new File(oldPath), new File(newPath), null, false)) {
                    File lckFile = new File(newPath + File.separator
                            + "mybox_derby" + File.separator + "db.lck");
                    if (lckFile.exists()) {
                        try {
                            FileDeleteTools.delete(lckFile);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }

                } else {
                    popFailed();
                    dataDirInput.setStyle(UserConfig.badStyle());
                }
            }
            AppVariables.MyboxDataPath = newPath;
            ConfigTools.writeConfigValue("MyBoxDataPath", newPath);
            dataDirInput.setStyle(null);
            if (clearCurrentRootCheck.isSelected()) {
                ConfigTools.writeConfigValue("MyBoxOldDataPath", oldPath);
            }
            MyBox.restart();

        } catch (Exception e) {
            popFailed();
            dataDirInput.setStyle(UserConfig.badStyle());
        }
    }

    @FXML
    protected void setFileRecentAction(ActionEvent event) {
        UserConfig.setInt("FileRecentNumber", recentFileNumber);
        AppVariables.fileRecentNumber = recentFileNumber;
        popSuccessful();
    }

    @FXML
    protected void clearFileHistories(ActionEvent event) {
        if (!PopTools.askSure(getTitle(), message("SureClear"))) {
            return;
        }
        new TableVisitHistory().clear();
        popSuccessful();
    }

    public void setDerbyMode() {
        isSettingValues = true;
        if (DerbyStatus.Nerwork == DerbyBase.status) {
            networkRadio.setSelected(true);
            derbyStatus.setText(MessageFormat.format(message("DerbyServerListening"), DerbyBase.port + ""));
        } else if (DerbyStatus.Embedded == DerbyBase.status) {
            embeddedRadio.setSelected(true);
            derbyStatus.setText(message("DerbyEmbeddedMode"));
        } else {
            networkRadio.setSelected(false);
            embeddedRadio.setSelected(false);
            derbyStatus.setText(MessageFormat.format(message("DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath));
        }
        isSettingValues = false;
    }

    public void checkDerbyMode() {
        if (isSettingValues) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        DerbyBase.mode = networkRadio.isSelected() ? "client" : "embedded";
        ConfigTools.writeConfigValue("DerbyMode", DerbyBase.mode);
        derbyBox.setDisable(true);
        task = new SingletonCurrentTask<Void>(this) {
            private String ret;

            @Override
            protected boolean handle() {
                ret = DerbyBase.startDerby();
                return ret != null;
            }

            @Override
            protected void whenSucceeded() {
                popInformation(ret, 6000);
                setDerbyMode();
            }

            @Override
            protected void finalAction() {
                derbyBox.setDisable(false);
                task = null;
            }

        };
        start(task);
    }

    /*
        PDF settings
     */
    public void initPdfTab() {
        try {

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkPdfMem() {
        String pm = UserConfig.getString("PdfMemDefault", "1GB");
        switch (pm) {
            case "1GB":
                pdfMem1GRadio.setSelected(true);
                break;
            case "2GB":
                pdfMem2GRadio.setSelected(true);
                break;
            case "Unlimit":
                pdfMemUnlimitRadio.setSelected(true);
                break;
            case "500MB":
            default:
                pdfMem500MRadio.setSelected(true);
        }
    }

    @FXML
    protected void PdfMem500MB(ActionEvent event) {
        UserConfig.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        UserConfig.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        UserConfig.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        UserConfig.setPdfMem("Unlimit");
    }

    /*
        Image settings
     */
    public void initImageTab() {
        try {
            strokeWidthBox.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5", "6", "7", "8", "9", "10"));
            strokeWidthBox.getSelectionModel().select(UserConfig.getInt("StrokeWidth", 2) + "");
            strokeWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                UserConfig.setInt("StrokeWidth", v);
                                ValidationTools.setEditorNormal(strokeWidthBox);
                                BaseImageController.updateMaskStroke();
                            } else {
                                ValidationTools.setEditorBadStyle(strokeWidthBox);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(strokeWidthBox);
                        }
                    }
                }
            });

            strokeColorSetController.init(this, "StrokeColor", Color.web(BaseImageController.DefaultStrokeColor));
            strokeColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    BaseImageController.updateMaskStroke();
                    popSuccessful();
                }
            });

            anchorWidthBox.getItems().addAll(Arrays.asList("10", "15", "20", "25", "30", "40", "50"));
            anchorWidthBox.getSelectionModel().select(UserConfig.getInt("AnchorWidth", 10) + "");
            anchorWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                UserConfig.setInt("AnchorWidth", v);
                                ValidationTools.setEditorNormal(anchorWidthBox);
                                BaseImageController.updateMaskStroke();
                            } else {
                                ValidationTools.setEditorBadStyle(anchorWidthBox);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(anchorWidthBox);
                        }
                    }
                }
            });

            anchorColorSetController.init(this, "AnchorColor", Color.web(BaseImageController.DefaultAnchorColor));
            anchorColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    BaseImageController.updateMaskStroke();
                    popSuccessful();
                }
            });

            anchorSolidCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    UserConfig.setBoolean("AnchorSolid", new_toggle);
                    if (parentController instanceof BaseImageController) {
                        ((BaseImageController) parentController).setMaskStroke();
                    }
                }
            });
            anchorSolidCheck.setSelected(UserConfig.getBoolean("AnchorSolid", true));

            gridColorSetController.init(this, "GridLinesColor", Color.LIGHTGRAY);
            gridColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    BaseImageController.updateMaskGrid();
                }
            });

            gridWidthSelector.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5", "6", "7", "8", "9", "10"));
            gridWidthSelector.getSelectionModel().select(UserConfig.getInt("GridLinesWidth", 1) + "");
            gridWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                UserConfig.setInt("GridLinesWidth", v);
                                ValidationTools.setEditorNormal(gridWidthSelector);
                                BaseImageController.updateMaskGrid();
                            } else {
                                ValidationTools.setEditorBadStyle(gridWidthSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(gridWidthSelector);
                        }
                    }
                }
            });

            gridIntervalSelector.getItems().addAll(Arrays.asList(message("Automatic"), "10", "20", "25", "50", "100", "5", "1", "2", "200", "500"));
            int gi = UserConfig.getInt("GridLinesInterval", -1);
            gridIntervalSelector.getSelectionModel().select(gi <= 0 ? message("Automatic") : gi + "");
            gridIntervalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    int v = -1;
                    try {
                        if (!message("Automatic").equals(newValue)) {
                            v = Integer.parseInt(newValue);
                        }
                    } catch (Exception e) {
                    }
                    UserConfig.setInt("GridLinesInterval", v);
                    BaseImageController.updateMaskGrid();
                }
            });

            gridOpacitySelector.getItems().addAll(Arrays.asList("0.5", "0.2", "1.0", "0.7", "0.1", "0.3", "0.8", "0.9", "0.6", "0.4"));
            gridOpacitySelector.getSelectionModel().select(UserConfig.getString("GridLinesOpacity", "0.1"));
            gridOpacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    float v = 0.1f;
                    try {
                        v = Float.parseFloat(newValue);
                    } catch (Exception e) {
                    }
                    UserConfig.setString("GridLinesOpacity", v + "");
                    BaseImageController.updateMaskGrid();
                }
            });

            alphaColorSetController.init(this, "AlphaAsColor", Color.WHITE);
            alphaColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    if (!Color.WHITE.equals((Color) newValue)) {
                        alphaLabel.setText(message("AlphaReplaceComments"));
                        alphaLabel.setStyle(NodeStyleTools.darkRedTextStyle());
                    } else {
                        alphaLabel.setText("");
                        popSuccessful();
                    }
                }
            });

            thumbnailWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(thumbnailWidthInput.getText());
                        if (v > 0) {
                            UserConfig.setInt("ThumbnailWidth", v);
                            AppVariables.thumbnailWidth = v;
                            thumbnailWidthInput.setStyle(null);
                            popSuccessful();
                        } else {
                            thumbnailWidthInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        thumbnailWidthInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            renderController.setParentController(this);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private void checkRecentFile() {
        try {
            int v = Integer.parseInt(fileRecentInput.getText());
            if (v >= 0) {
                recentFileNumber = v;
                fileRecentInput.setStyle(null);
                settingsRecentOKButton.setDisable(false);
            } else {
                fileRecentInput.setStyle(UserConfig.badStyle());
                settingsRecentOKButton.setDisable(true);
            }
        } catch (Exception e) {
            fileRecentInput.setStyle(UserConfig.badStyle());
            settingsRecentOKButton.setDisable(true);
        }
    }

    @FXML
    protected void clearImageHistories(ActionEvent event) {
        if (!PopTools.askSure(getTitle(), message("SureClear"))) {
            return;
        }
        new TableImageEditHistory().clearAll();
        popSuccessful();
    }

    /*
        Map settings
     */
    public void initMapTab() {
        try {
            tiandituWebKeyInput.setText(UserConfig.getString("TianDiTuWebKey", AppValues.TianDiTuWebKey));
            gaodeWebKeyInput.setText(UserConfig.getString("GaoDeMapWebKey", AppValues.GaoDeMapWebKey));
            gaodeServiceKeyInput.setText(UserConfig.getString("GaoDeMapServiceKey", AppValues.GaoDeMapServiceKey));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void setMapKeysAction() {
        String tiandituKey = tiandituWebKeyInput.getText();
        String daodeWeb = gaodeWebKeyInput.getText();
        String gaoServiceKey = gaodeServiceKeyInput.getText();
        if (tiandituKey == null || tiandituKey.trim().isBlank()
                || daodeWeb == null || daodeWeb.trim().isBlank()
                || gaoServiceKey == null || gaoServiceKey.trim().isBlank()) {
            popError(message("InvalidData"));
            return;
        }
        UserConfig.setString("TianDiTuWebKey", tiandituKey);
        UserConfig.setString("GaoDeMapWebKey", daodeWeb);
        UserConfig.setString("GaoDeMapServiceKey", gaoServiceKey);
    }

    @FXML
    public void defaultMapAction() {
        tiandituWebKeyInput.setText(AppValues.TianDiTuWebKey);
        gaodeWebKeyInput.setText(AppValues.GaoDeMapWebKey);
        gaodeServiceKeyInput.setText(AppValues.GaoDeMapServiceKey);
        setMapKeysAction();
    }

    /*
        others
     */
    @FXML
    public void clearSettings(ActionEvent event) {
        clearUserSettings();
    }

    @FXML
    public void closeAction(ActionEvent event) {
        closeStage();
    }

    /*
        static methods
     */
    public static SettingsController oneOpen(BaseController parent) {
        SettingsController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof SettingsController) {
                try {
                    controller = (SettingsController) object;
                    controller.requestMouse();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (SettingsController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.SettingsFxml, false);
        }
        return controller;
    }

}
