package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlWindow;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageScope;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FileTools.FileSortMode;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends BaseImageShapesController {

    protected ImageScope scope;
    protected int currentAngle = 0, rotateAngle = 90;
    protected File nextFile, previousFile;
    protected FileSortMode sortMode;

    @FXML
    protected TitledPane filePane, framePane, viewPane, saveAsPane, editPane, browsePane;
    @FXML
    protected VBox panesBox, contentBox, fileBox, saveAsBox;
    @FXML
    protected HBox operationBox;
    @FXML
    protected FlowPane saveFramesPane;
    @FXML
    protected CheckBox selectAreaCheck, deleteConfirmCheck, saveConfirmCheck;
    @FXML
    protected ToggleGroup sortGroup, framesSaveGroup;
    @FXML
    protected ComboBox<String> loadWidthBox, frameSelector;
    @FXML
    protected Label framesLabel;
    @FXML
    protected RadioButton saveAllFramesRadio;
    @FXML
    protected ControlImageFormat formatController;
    @FXML
    protected Button loadContentInSystemClipboardButton;
    @FXML
    protected ControlFileBackup backupController;

    public ImageViewerController() {
        baseTitle = message("ImageViewer");
        TipsLabelKey = "ImageViewerTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initFilePane();
            initFramePane();
            initViewPane();
            initSaveAsPane();
            initEditPane();
            initBrowsePane();
            initOperationBox();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initFilePane() {
        try {
            if (fileBox != null && imageView != null) {
                fileBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (saveButton != null && imageView != null) {
                saveButton.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }

            loadWidth = defaultLoadWidth;
            if (loadWidthBox != null) {
                List<String> values = Arrays.asList(message("OriginalSize"),
                        "512", "1024", "256", "128", "2048", "100", "80", "4096");
                loadWidthBox.getItems().addAll(values);
                int v = AppVariables.getUserConfigInt(baseName + "LoadWidth", defaultLoadWidth);
                if (v <= 0) {
                    loadWidth = -1;
                    loadWidthBox.getSelectionModel().select(0);
                } else {
                    loadWidth = v;
                    loadWidthBox.setValue(v + "");
                }
                loadWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (message("OriginalSize").equals(newValue)) {
                            loadWidth = -1;
                        } else {
                            try {
                                loadWidth = Integer.valueOf(newValue);
                                FxmlControl.setEditorNormal(loadWidthBox);
                            } catch (Exception e) {
                                FxmlControl.setEditorBadStyle(loadWidthBox);
                                return;
                            }
                        }
                        AppVariables.setUserConfigInt(baseName + "LoadWidth", loadWidth);
                        setLoadWidth();
                    }
                });
                FxmlControl.setTooltip(loadWidthBox, new Tooltip(AppVariables.message("ImageLoadWidthCommnets")));
            }

            if (deleteConfirmCheck != null) {
                deleteConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "ConfirmDelete", deleteConfirmCheck.isSelected());
                    }
                });
                deleteConfirmCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ConfirmDelete", true));
            }

            if (saveConfirmCheck != null) {
                saveConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "ConfirmSave", saveConfirmCheck.isSelected());
                    }
                });
                saveConfirmCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ConfirmSave", true));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initFramePane() {
        try {
            if (framePane == null) {
                return;
            }
            if (imageView != null) {
                framePane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (frameSelector != null) {
                frameSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            if (isSettingValues) {
                                return;
                            }
                            int v = Integer.parseInt(frameSelector.getValue());
                            if (v < 1 || v > framesNumber) {
                                frameSelector.getEditor().setStyle(badStyle);
                            } else {
                                frameSelector.getEditor().setStyle(null);
                                loadFrame(v - 1);
                            }
                        } catch (Exception e) {
                            frameSelector.getEditor().setStyle(badStyle);
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initViewPane() {
        try {
            if (viewPane != null) {
                if (imageView != null) {
                    viewPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                viewPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "ViewPane", viewPane.isExpanded());
                });
                viewPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "ViewPane", false));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected List<MenuItem> makeImageContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            List<MenuItem> subItems = new ArrayList<>();
            menu = new MenuItem(message("Pop")
                    + (popButton != null && popButton.isVisible() && !popButton.isDisabled() ? "  CTRL+p" : ""));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                popAction();
            });
            subItems.add(menu);

            if (pickColorCheck != null && pickColorCheck.isVisible() && !pickColorCheck.isDisabled()) {
                CheckMenuItem checkMenu = new CheckMenuItem(message("PickColor"));
                checkMenu.setSelected(pickColorCheck.isSelected());
                checkMenu.setOnAction((ActionEvent menuItemEvent) -> {
                    pickColorCheck.setSelected(checkMenu.isSelected());
                });
                subItems.add(checkMenu);
            }

            if (!needNotCoordinates) {
                CheckMenuItem checkMenu = new CheckMenuItem(message("Coordinate"));
                checkMenu.setSelected(coordinateCheck != null ? coordinateCheck.isSelected()
                        : AppVariables.getUserConfigBoolean(baseName + "PopCooridnate", false));
                checkMenu.setOnAction((ActionEvent menuItemEvent) -> {
                    if (coordinateCheck != null) {
                        coordinateCheck.setSelected(checkMenu.isSelected());
                    } else {
                        AppVariables.setUserConfigValue(baseName + "PopCooridnate", checkMenu.isSelected());
                        checkCoordinate();
                    }
                });
                subItems.add(checkMenu);
            }

            if (!needNotRulers) {
                CheckMenuItem checkMenuX = new CheckMenuItem(message("RulerX"));
                checkMenuX.setSelected(rulerXCheck != null ? rulerXCheck.isSelected()
                        : AppVariables.getUserConfigBoolean(baseName + "RulerX", false));
                checkMenuX.setOnAction((ActionEvent menuItemEvent) -> {
                    if (rulerXCheck != null) {
                        rulerXCheck.setSelected(checkMenuX.isSelected());
                    } else {
                        AppVariables.setUserConfigValue(baseName + "RulerX", checkMenuX.isSelected());
                        checkRulerX();
                    }
                });
                subItems.add(checkMenuX);

                CheckMenuItem checkMenuY = new CheckMenuItem(message("RulerY"));
                checkMenuY.setSelected(rulerYCheck != null ? rulerYCheck.isSelected()
                        : AppVariables.getUserConfigBoolean(baseName + "RulerY", false));
                checkMenuY.setOnAction((ActionEvent menuItemEvent) -> {
                    if (rulerYCheck != null) {
                        rulerYCheck.setSelected(checkMenuY.isSelected());
                    } else {
                        AppVariables.setUserConfigValue(baseName + "RulerY", checkMenuY.isSelected());
                        checkRulerY();
                    }
                });
                subItems.add(checkMenuY);
            }

            if (!subItems.isEmpty()) {
                items.addAll(subItems);
                items.add(new SeparatorMenuItem());
            }

            subItems = new ArrayList<>();

            if (selectAllButton != null && selectAllButton.isVisible() && !selectAllButton.isDisabled()) {
                menu = new MenuItem(message("SelectAll") + "  CTRL+a");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    selectAllAction();
                });
                subItems.add(menu);
            }

            if (selectAreaCheck != null && selectAreaCheck.isVisible() && !selectAreaCheck.isDisabled()) {
                CheckMenuItem checkMenu = new CheckMenuItem(message("SelectArea"));
                checkMenu.setOnAction((ActionEvent menuItemEvent) -> {
                    if (isSettingValues) {
                        return;
                    }
                    checkMenu.setSelected(!selectAreaCheck.isSelected());
                    selectAreaCheck.setSelected(!selectAreaCheck.isSelected());
                });
                isSettingValues = true;
                checkMenu.setSelected(selectAreaCheck.isSelected());
                isSettingValues = false;
                subItems.add(checkMenu);
            }

            if (copyButton == null || (copyButton.isVisible() && !copyButton.isDisabled())) {
                menu = new MenuItem(message("Copy") + "  CTRL+c");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    copyAction();
                });
                subItems.add(menu);
            }

            if (pasteButton != null && pasteButton.isVisible() && !pasteButton.isDisabled()) {
                menu = new MenuItem(message("Paste") + "  CTRL+v");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pasteAction();
                });
                subItems.add(menu);
            }

            if (cropButton != null && cropButton.isVisible() && !cropButton.isDisabled()) {
                menu = new MenuItem(message("Crop") + "  CTRL+x");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    cropAction();
                });
                subItems.add(menu);
            }

            if (rotateLeftButton != null && rotateLeftButton.isVisible() && !rotateLeftButton.isDisabled()) {
                menu = new MenuItem(message("RotateLeft"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    rotateLeft();
                });
                subItems.add(menu);
            }

            if (rotateRightButton != null && rotateRightButton.isVisible() && !rotateRightButton.isDisabled()) {
                menu = new MenuItem(message("RotateRight"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    rotateRight();
                });
                subItems.add(menu);
            }

            if (undoButton != null && undoButton.isVisible() && !undoButton.isDisabled()) {
                menu = new MenuItem(message("Undo") + "  CTRL+z");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    undoAction();
                });
                subItems.add(menu);
            }

            if (redoButton != null && redoButton.isVisible() && !redoButton.isDisabled()) {
                menu = new MenuItem(message("Redo") + "  F3CTRL+y");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    redoAction();
                });
                subItems.add(menu);
            }

            if (recoverButton != null && recoverButton.isVisible() && !recoverButton.isDisabled()) {
                menu = new MenuItem(message("Recover") + "  F3 / CTRL+r");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    recoverAction();
                });
                subItems.add(menu);
            }

            if (saveButton != null && saveButton.isVisible() && !saveButton.isDisabled()) {
                menu = new MenuItem(message("Save") + "  F2 / CTRL+s");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    saveAction();
                });
                subItems.add(menu);
            }

            if (saveAsButton != null && saveAsButton.isVisible() && !saveAsButton.isDisabled()) {
                menu = new MenuItem(message("SaveAs") + "  F11");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    saveAsAction();
                });
                subItems.add(menu);
            }

            if (renameButton != null && renameButton.isVisible() && !renameButton.isDisabled()) {
                menu = new MenuItem(message("Rename"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    renameAction();
                });
                subItems.add(menu);
            }

            if (deleteButton != null && deleteButton.isVisible() && !deleteButton.isDisabled()) {
                menu = new MenuItem(message("Delete"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteAction();
                });
                subItems.add(menu);
            }

            if (!subItems.isEmpty()) {
                if (subItems.size() > 2) {
                    Menu subMenu = new Menu(message("Edit"));
                    subMenu.getItems().addAll(subItems);
                    items.add(subMenu);
                } else {
                    items.addAll(subItems);
                }
                items.add(new SeparatorMenuItem());
            }

            if (imageInformation != null) {
                menu = new MenuItem(message("Information"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                items.add(menu);

                menu = new MenuItem(message("MetaData"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    popMetaData();
                });
                items.add(menu);

                items.add(new SeparatorMenuItem());
            }

            subItems = new ArrayList<>();
            if (previousButton != null && previousButton.isVisible() && !previousButton.isDisabled()) {
                menu = new MenuItem(message("Previous") + "  PAGE UP");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    previousAction();
                });
                subItems.add(menu);
            }
            if (nextButton != null && nextButton.isVisible() && !nextButton.isDisabled()) {
                menu = new MenuItem(message("Next") + "  PAGE DOWN");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    nextAction();
                });
                subItems.add(menu);
            }

            if (firstButton != null && firstButton.isVisible() && !firstButton.isDisabled()) {
                menu = new MenuItem(message("First") + "  ALT+HOME");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    firstAction();
                });
                subItems.add(menu);
            }

            if (lastButton != null && lastButton.isVisible() && !lastButton.isDisabled()) {
                menu = new MenuItem(message("Last") + "  ALT+END");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    lastAction();
                });
                subItems.add(menu);
            }

            if (!subItems.isEmpty()) {
                if (subItems.size() > 2) {
                    Menu subMenu = new Menu(message("Navigate"));
                    subMenu.getItems().addAll(subItems);
                    items.add(subMenu);
                } else {
                    items.addAll(subItems);
                }

            }

            List<MenuItem> superItems = super.makeImageContextMenu();
            if (!superItems.isEmpty()) {
                superItems.addAll(items);
            }

            return superItems;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected void initSaveAsPane() {
        try {
            if (saveAsPane != null) {
                if (imageView != null) {
                    saveAsPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                saveAsPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "SaveAsPane", saveAsPane.isExpanded());
                });
                saveAsPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "SaveAsPane", false));
            }

            if (formatController != null) {
                formatController.setParameters(this, false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initBrowsePane() {
        try {
            if (browsePane != null) {
                if (imageView != null) {
                    browsePane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                browsePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "BrowsePane", browsePane.isExpanded());
                });
                browsePane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "BrowsePane", false));
            }

            if (previousButton != null) {
                previousButton.setDisable(imageFile() == null);
            }
            if (nextButton != null) {
                nextButton.setDisable(imageFile() == null);
            }

            String saveMode = AppVariables.getUserConfigValue(baseName + "SortMode",
                    FileTools.FileSortMode.NameAsc.name());
            sortMode = FileTools.sortMode(saveMode);
            if (sortGroup != null) {
                sortGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldValue, Toggle newValue) -> {
                            if (newValue == null || isSettingValues) {
                                return;
                            }
                            String selected = ((RadioButton) newValue).getText();
                            for (FileSortMode mode : FileSortMode.values()) {
                                if (message(mode.name()).equals(selected)) {
                                    sortMode = mode;
                                    break;
                                }
                            }
                            AppVariables.setUserConfigValue(baseName + "SortMode", sortMode.name());
                            makeImageNevigator();
                        });
                for (Toggle toggle : sortGroup.getToggles()) {
                    RadioButton button = (RadioButton) toggle;
                    if (button.getText().equals(message(saveMode))) {
                        isSettingValues = true;
                        button.fire();
                        isSettingValues = false;
                    }
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initEditPane() {
        try {
            if (editPane != null) {
                editPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                editPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "EditPane", editPane.isExpanded());
                });
                editPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "EditPane", false));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initOperationBox() {
        try {
            if (imageView != null) {
                if (operationBox != null) {
                    operationBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                if (leftPaneControl != null) {
                    leftPaneControl.visibleProperty().bind(Bindings.isNotNull(imageView.imageProperty()));
                }
                if (rightPaneControl != null) {
                    rightPaneControl.visibleProperty().bind(Bindings.isNotNull(imageView.imageProperty()));
                }
            }

            if (selectAreaCheck != null) {
                selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "SelectArea", selectAreaCheck.isSelected());
                        checkSelect();
                    }
                });
                selectAreaCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "SelectArea", false));
                checkSelect();
                FxmlControl.setTooltip(selectAreaCheck, new Tooltip("CTRL+t"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void controlAltHandler(KeyEvent event) {
        if (event.getCode() == null) {
            return;
        }
        switch (event.getCode()) {
            case T:
                if (selectAreaCheck != null) {
                    selectAreaCheck.setSelected(!selectAreaCheck.isSelected());
                }
                return;
            case V:
                if (pasteButton != null && !pasteButton.isDisabled() && pasteButton.isVisible()) {
                    pasteAction();
                } else if (loadContentInSystemClipboardButton != null) {
                    loadSystemClipboardImage();
                }
                return;
        }
        super.controlAltHandler(event);
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            if (pasteButton == null) {
                FxmlControl.setTooltip(loadContentInSystemClipboardButton, message("LoadContentInSystemClipboard") + "\nCTRL+v / ALT+v");
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkSaveAs() {

    }

    protected void checkSelect() {
        if (cropButton != null) {
            cropButton.setDisable(!selectAreaCheck.isSelected());
        }
        if (selectAllButton != null) {
            selectAllButton.setDisable(!selectAreaCheck.isSelected());
        }

        if (selectAreaCheck != null) {
            initMaskRectangleLine(selectAreaCheck.isSelected());
        }
        updateLabelsTitle();
    }

    protected void setLoadWidth() {
        if (isSettingValues) {
            return;
        }
        if (imageFile() != null) {
            loadImageFile(imageFile(), loadWidth);
        } else if (imageView.getImage() != null) {
            loadImage(imageView.getImage(), loadWidth);
        } else if (image != null) {
            loadImage(image, loadWidth);
        }
        if (imageInformation != null) {
            setImageChanged(imageInformation.isIsScaled());
        } else {
            setImageChanged(false);
        }
    }

    @Override
    public void afterInfoLoaded() {
        if (infoButton != null) {
            infoButton.setDisable(imageInformation == null);
        }
        if (metaButton != null) {
            metaButton.setDisable(imageInformation == null);
        }
        if (deleteButton != null) {
            deleteButton.setDisable(imageFile() == null);
        }
        if (renameButton != null) {
            renameButton.setDisable(imageFile() == null);
        }
        if (deleteConfirmCheck != null) {
            deleteConfirmCheck.setDisable(imageFile() == null);
        }
        if (previousButton != null) {
            previousButton.setDisable(imageFile() == null);
        }
        if (nextButton != null) {
            nextButton.setDisable(imageFile() == null);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            afterInfoLoaded();
            if (imageView == null) {
                return true;
            }
            imageView.setPreserveRatio(true);
            imageView.setImage(image);
            if (image == null) {
                return true;
            }

            if (sampledView != null) {
                if (imageInformation != null && imageInformation.isIsSampled()) {
                    FxmlControl.setTooltip(sampledView, imageInformation.sampleInformation(image));
                    sampledView.setVisible(true);
                } else {
                    sampledView.setVisible(false);
                }
            }

            if (saveAsBox != null && saveFramesPane != null) {
                if (framesNumber <= 1) {
                    if (saveAsBox.getChildren().contains(saveFramesPane)) {
                        saveAsBox.getChildren().remove(saveFramesPane);
                    }

                } else {
                    if (!saveAsBox.getChildren().contains(saveFramesPane)) {
                        saveAsBox.getChildren().add(0, saveFramesPane);
                    }
                }
            }
            if (saveAllFramesRadio != null) {
                saveAllFramesRadio.fire();
                saveAllFramesSelected();
            }
            if (framesLabel != null) {
                framesLabel.setText("/" + framesNumber);
            }
            if (frameSelector != null) {
                List<String> frames = new ArrayList<>();
                for (int i = 1; i <= framesNumber; i++) {
                    frames.add(i + "");
                }
                isSettingValues = true;
                frameSelector.getItems().setAll(frames);
                frameSelector.setValue((frameIndex + 1) + "");
                isSettingValues = false;
            }
            if (panesBox != null && framePane != null) {
                if (framesNumber <= 1) {
                    if (panesBox.getChildren().contains(framePane)) {
                        panesBox.getChildren().remove(framePane);
                    }
                } else {
                    if (!panesBox.getChildren().contains(framePane)) {
                        panesBox.getChildren().add(1, framePane);
                        framePane.setExpanded(true);
                    }
                }
            }

            if (imageFile() != null && nextButton != null) {
                makeImageNevigator();
            }
            fitSize();
            setMaskStroke();

            if (selectAreaCheck != null) {
                checkSelect();
            }

            if (imageInformation == null) {
                setImageChanged(true);
            } else {
                setImageChanged(imageInformation.isIsScaled());
            }
            refinePane();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            if (imageView != null) {
                imageView.setImage(null);
            }
            alertInformation(AppVariables.message("NotSupported"));
            return false;
        }
    }

    public void makeImageNevigator() {
        makeImageNevigator(imageFile());
    }

    public void makeImageNevigator(File currentfile) {
        try {
            if (currentfile == null) {
                previousFile = null;
                previousButton.setDisable(true);
                nextFile = null;
                nextButton.setDisable(true);
                return;
            }
            File path = currentfile.getParentFile();
            List<File> pathFiles = new ArrayList<>();
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        pathFiles.add(file);
                    }
                }
                FileTools.sortFiles(pathFiles, sortMode);

                for (int i = 0; i < pathFiles.size(); ++i) {
                    if (pathFiles.get(i).getAbsoluteFile().equals(currentfile.getAbsoluteFile())) {
                        if (i < pathFiles.size() - 1) {
                            nextFile = pathFiles.get(i + 1);
                            nextButton.setDisable(false);
                        } else {
                            nextFile = null;
                            nextButton.setDisable(true);
                        }
                        if (i > 0) {
                            previousFile = pathFiles.get(i - 1);
                            previousButton.setDisable(false);
                        } else {
                            previousFile = null;
                            previousButton.setDisable(true);
                        }
                        return;
                    }
                }
            }
            previousFile = null;
            previousButton.setDisable(true);
            nextFile = null;
            nextButton.setDisable(true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void playAction() {
        try {
            ImagesPlayController controller
                    = (ImagesPlayController) openStage(CommonValues.ImagesPlayFxml);
            controller.sourceFileChanged(sourceFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void nextFrame() {
        loadFrame(frameIndex + 1);
    }

    @FXML
    public void previousFrame() {
        loadFrame(frameIndex - 1);
    }

    @FXML
    public void saveAllFramesSelected() {
        if (imageFile() != null && framesNumber > 1) {
            formatController.formatPane.getChildren().setAll(formatController.tifRadio, formatController.gifRadio);
            if ("gif".equalsIgnoreCase(FileTools.getFileSuffix(imageFile()))) {
                formatController.gifRadio.fire();
            } else {
                formatController.tifRadio.fire();
            }
        } else {
            formatController.formatPane.getChildren().setAll(formatController.pngRadio, formatController.jpgRadio,
                    formatController.tifRadio, formatController.gifRadio,
                    formatController.pcxRadio, formatController.pnmRadio,
                    formatController.bmpRadio, formatController.wbmpRadio, formatController.icoRadio);
            formatController.pngRadio.fire();
        }
    }

    @FXML
    public void saveCurrentFramesSelected() {
        formatController.formatPane.getChildren().setAll(formatController.pngRadio, formatController.jpgRadio,
                formatController.tifRadio, formatController.gifRadio,
                formatController.pcxRadio, formatController.pnmRadio,
                formatController.bmpRadio, formatController.wbmpRadio, formatController.icoRadio);
    }

    @FXML
    @Override
    public void infoAction() {
        if (imageInformation == null) {
            return;
        }
        FxmlWindow.showImageInformation(imageInformation);
    }

    @FXML
    public void loadSystemClipboardImage() {
        if (!checkBeforeNextAction()) {
            return;
        }
        Image clip = SystemTools.fetchImageInClipboard(false);
        if (clip == null) {
            popError(message("NoImageInClipboard"));
            return;
        }
        loadImage(clip);
    }

    @FXML
    @Override
    public void nextAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (nextFile != null) {
            loadImageFile(nextFile.getAbsoluteFile(), loadWidth, 0);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (previousFile != null) {
            loadImageFile(previousFile.getAbsoluteFile(), loadWidth, 0);
        }
    }

    @FXML
    public void viewImageAction() {
        FxmlWindow.openImageViewer(null, imageFile());
    }

    @FXML
    public void popMetaData() {
        FxmlWindow.showImageMetaData(imageInformation);
    }

    @FXML
    public void moveRight() {
        FxmlControl.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
    }

    @FXML
    public void moveLeft() {
        FxmlControl.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
    }

    @FXML
    public void moveUp() {
        FxmlControl.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
    }

    @FXML
    public void moveDown() {
        FxmlControl.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
    }

    @FXML
    public void rotateRight() {
        rotate(90);
    }

    @FXML
    public void rotateLeft() {
        rotate(270);
    }

    @FXML
    public void turnOver() {
        rotate(180);
    }

    public void rotate(final int rotateAngle) {
        if (imageView.getImage() == null) {
            return;
        }
        currentAngle = rotateAngle;
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.rotateImage(imageView.getImage(), rotateAngle);
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    imageView.setImage(newImage);
                    checkSelect();
                    setImageChanged(true);
                    refinePane();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (imageView.getImage() == null
                || maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            return;
        }
        maskRectangleData = new DoubleRectangle(0, 0,
                getImageWidth() - 1, getImageHeight() - 1);

        drawMaskRectangleLineAsData();
    }

    @FXML
    @Override
    public void cropAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        try {
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private Image areaImage;

                    @Override
                    protected boolean handle() {
                        areaImage = cropImage();
                        if (areaImage == null) {
                            areaImage = imageView.getImage();
                        }
                        return areaImage != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        imageView.setImage(areaImage);
                        setImageChanged(true);
                        resetMaskControls();
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected Image cropImage() {
        Image inImage = imageView.getImage();

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            if (maskRectangleData.getSmallX() == 0
                    && maskRectangleData.getSmallY() == 0
                    && maskRectangleData.getBigX() == (int) inImage.getWidth() - 1
                    && maskRectangleData.getBigY() == (int) inImage.getHeight() - 1) {
                return null;
            }
            return FxmlImageManufacture.cropOutsideFx(inImage, maskRectangleData, Color.WHITE);

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskCircleData, Color.WHITE);

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskEllipseData, Color.WHITE);

        } else if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskPolygonData, Color.WHITE);

        } else {
            return null;
        }

    }

    @FXML
    @Override
    public void recoverAction() {
        if (imageView == null) {
            return;
        }
        boolean sizeChanged = getImageWidth() != image.getWidth()
                || getImageHeight() != image.getHeight();
        imageView.setImage(image);
        if (sizeChanged) {
            resetMaskControls();
        }
        setImageChanged(false);
        popInformation(message("Recovered"));
    }

    @FXML
    @Override
    public void copyAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image areaImage;

                @Override
                protected boolean handle() {
                    areaImage = cropImage();
                    if (areaImage == null) {
                        areaImage = imageView.getImage();
                    }
                    return ImageClipboard.add(areaImage, ImageClipboard.ImageSource.Copy) != null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(AppVariables.message("ImageSelectionInClipBoard"));
                    ControlImagesClipboard.updateClipboards();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (imageView == null || imageView.getImage() == null
                || (saveButton != null && saveButton.isDisabled())) {
            return;
        }
        File imageFile = imageFile();
        if (imageFile == null) {
            saveAsAction();
            return;
        }
        try {
            String ask = null;
            if (imageInformation != null && imageInformation.isIsScaled()) {
                ask = message("SureSaveScaled");
            } else if (saveConfirmCheck != null && saveConfirmCheck.isSelected()) {
                ask = message("SureOverrideFile");
            }
            if (ask != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(ask);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
                ButtonType buttonSaveAs = new ButtonType(AppVariables.message("SaveAs"));
                ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
                alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonCancel) {
                    return;
                } else if (result.get() == buttonSaveAs) {
                    saveAsAction();
                    return;
                }
            }

            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private Image targetImage;

                    @Override
                    protected boolean handle() {
                        Object imageToSave = imageToSave();
                        if (imageToSave == null) {
                            return false;
                        }
                        BufferedImage bufferedImage;
                        if (imageToSave instanceof Image) {
                            targetImage = (Image) imageToSave;
                            bufferedImage = SwingFXUtils.fromFXImage(targetImage, null);
                        } else if (imageToSave instanceof BufferedImage) {
                            bufferedImage = (BufferedImage) imageToSave;
                            targetImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        } else {
                            return false;
                        }
                        if (bufferedImage == null || task == null || isCancelled()) {
                            return false;
                        }
                        if (backupController != null && backupController.isBack()) {
                            backupController.addBackup(imageFile);
                        }
                        String format = FileTools.getFileSuffix(imageFile.getName());
                        if (framesNumber > 1) {
                            error = ImageFileWriters.writeFrame(imageFile, frameIndex, bufferedImage, imageFile, null);
                            ok = error == null;
                        } else {
                            ok = ImageFileWriters.writeImageFile(bufferedImage, format, imageFile.getAbsolutePath());
                        }
                        if (!ok || task == null || isCancelled()) {
                            return false;
                        }
                        ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(imageFile);
                        if (finfo == null || finfo.getImageInformation() == null) {
                            return false;
                        }
                        imageInformation = finfo.getImageInformation();
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        image = targetImage;
                        imageView.setImage(image);
                        popInformation(imageFile + "   " + AppVariables.message("Saved"));
                        setImageChanged(false);
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public String saveAsPrefix() {
        String name;
        if (imageFile() != null) {
            name = FileTools.getFilePrefix(imageFile().getName())
                    + (framesNumber > 1 && (saveAllFramesRadio == null || !saveAllFramesRadio.isSelected())
                    ? "-" + message("Frame") + (frameIndex + 1) : "")
                    + "-" + new Date().getTime();
        } else {
            name = new Date().getTime() + "";
        }
        if (formatController != null) {
            name += "." + formatController.attributes.getImageFormat();
        } else if (fileTypeGroup != null) {
            name += "." + ((RadioButton) fileTypeGroup.getSelectedToggle()).getText();
        }
        return name;
    }

    public Object imageToSave() {
        Image selected = cropImage();
        if (selected == null) {
            selected = imageView.getImage();
        }
        return selected;
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageView == null || imageView.getImage() == null
                || (saveAsButton != null && saveAsButton.isDisabled())) {
            return;
        }
        targetFile = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                saveAsPrefix(), CommonFxValues.ImageExtensionFilter);
        if (targetFile == null) {
            return;
        }
        File imageFile = imageFile();
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    Object imageToSave = imageToSave();
                    if (imageToSave == null) {
                        return false;
                    }
                    BufferedImage bufferedImage;
                    if (imageToSave instanceof Image) {
                        bufferedImage = SwingFXUtils.fromFXImage((Image) imageToSave, null);
                    } else if (imageToSave instanceof BufferedImage) {
                        bufferedImage = (BufferedImage) imageToSave;
                    } else {
                        return false;
                    }
                    if (bufferedImage == null || task == null || isCancelled()) {
                        return false;
                    }
                    boolean multipleFrames = imageFile != null && framesNumber > 1 && saveAllFramesRadio != null && saveAllFramesRadio.isSelected();
                    if (formatController != null) {
                        if (multipleFrames) {
                            error = ImageFileWriters.writeFrame(imageFile, frameIndex, bufferedImage, targetFile, formatController.attributes);
                            return error == null;
                        } else {
                            BufferedImage converted = ImageConvert.convertColorSpace(bufferedImage, formatController.attributes);
                            return ImageFileWriters.writeImageFile(converted, formatController.attributes, targetFile.getAbsolutePath());
                        }
                    } else {
                        if (multipleFrames) {
                            error = ImageFileWriters.writeFrame(imageFile, frameIndex, bufferedImage, targetFile, null);
                            return error == null;
                        } else {
                            return ImageFileWriters.writeImageFile(bufferedImage, targetFile);
                        }
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(AppVariables.message("Saved"));
                    recordFileWritten(targetFile);

                    if (saveAsType == SaveAsType.Load) {
                        sourceFileChanged(targetFile);

                    } else if (saveAsType == SaveAsType.Open) {
                        FxmlWindow.openImageViewer(targetFile);

                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    @FXML
    @Override
    public void deleteAction() {
        if (deleteFile(sourceFile)) {
            sourceFile = null;
            image = null;
            imageView.setImage(null);
            if (nextFile != null) {
                nextAction();
            } else if (previousFile != null) {
                previousAction();
            } else {
                if (previousButton != null) {
                    previousButton.setDisable(true);
                }
                if (nextButton != null) {
                    nextButton.setDisable(true);
                }
            }
        }
    }

    public boolean deleteFile(File sfile) {
        if (sfile == null) {
            return false;
        }
        if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
            if (!FxmlControl.askSure(getMyStage().getTitle(), message("SureDelete"))) {
                return false;
            }
        }
        if (FileTools.delete(sfile)) {
            popSuccessful();
            return true;
        } else {
            popFailed();
            return false;
        }
    }

    public void changeFile(ImageInformation info, File file) {
        if (info == null || file == null) {
            return;
        }
        ImageFileInformation finfo = info.getImageFileInformation();
        if (finfo != null) {
            finfo.setFile(file);
            finfo.setFileName(file.getAbsolutePath());
        }
        info.setFileName(file.getAbsolutePath());
        info.setFile(file);
    }

    @FXML
    public void renameAction() {
        try {
            if (imageChanged) {
                saveAction();
            }
            if (sourceFile == null) {
                return;
            }
            FileRenameController controller = (FileRenameController) FxmlWindow.openStage(CommonValues.FileRenameFxml);
            controller.getMyStage().setOnHiding((WindowEvent event) -> {
                File newFile = controller.getNewFile();
                Platform.runLater(() -> {
                    fileRenamed(newFile);
                });
            });
            controller.set(sourceFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    public void fileRenamed(File newFile) {
        try {
            if (newFile == null) {
                return;
            }
            popSuccessful();
            sourceFile = newFile;
            recordFileOpened(sourceFile);
            changeFile(imageInformation, newFile);
            updateLabelsTitle();
            makeImageNevigator();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    @FXML
    protected void manufactureAction() {
        ImageManufactureController controller = (ImageManufactureController) FxmlWindow.openStage(CommonValues.ImageManufactureFxml);
        operation(controller);
    }

    @FXML
    public void browseAction() {
        ImagesBrowserController controller = FxmlWindow.openImagesBrowser(null);
        File file = imageFile();
        if (file != null) {
            controller.loadImages(file.getParentFile(), 9);
        }
    }

    @FXML
    @Override
    public void popAction() {
        if (imageView.getImage() == null) {
            return;
        }
        ImageViewerController controller = (ImageViewerController) FxmlWindow.openStage(CommonValues.ImagePopupFxml);
        operation(controller);
    }

    @FXML
    public void statisticAction() {
        ImageAnalyseController controller = (ImageAnalyseController) FxmlWindow.openStage(CommonValues.ImageAnalyseFxml);
        operation(controller);
    }

    @FXML
    public void ocrAction() {
        ImageOCRController controller = (ImageOCRController) FxmlWindow.openStage(CommonValues.ImageOCRFxml);
        operation(controller);
    }

    @FXML
    public void splitAction() {
        ImageSplitController controller = (ImageSplitController) FxmlWindow.openStage(CommonValues.ImageSplitFxml);
        operation(controller);
    }

    public void sampleAction() {
        ImageSampleController controller = (ImageSampleController) FxmlWindow.openStage(CommonValues.ImageSampleFxml);
        operation(controller);
    }

    public File imageFile() {
        return sourceFile;
    }

    public void operation(BaseImageController controller) {
        if (imageView == null || imageView.getImage() == null || controller == null) {
            return;
        }
        controller.toFront();
        if (maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            if (imageChanged) {
                controller.loadImage(imageView.getImage());
            } else {
                if (controller instanceof ImageSampleController || controller instanceof ImageSplitController) {
                    controller.loadImage(imageFile(), imageInformation, imageView.getImage());
                } else if (imageInformation != null && imageInformation.isIsScaled()) {
                    controller.loadImage(imageView.getImage());
                } else {
                    controller.loadImage(imageFile(), imageInformation, imageView.getImage());
                }
            }
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image areaImage;

                @Override
                protected boolean handle() {
                    areaImage = cropImage();
                    if (areaImage == null) {
                        areaImage = imageView.getImage();
                    }
                    return areaImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    controller.loadImage(areaImage);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void convertAction() {
        ImageConverterBatchController controller = (ImageConverterBatchController) FxmlWindow.openStage(CommonValues.ImageConverterBatchFxml);
        File file = imageFile();
        if (file != null) {
            controller.tableController.addFile(file);
        }
    }

    @FXML
    public void settings() {
        SettingsController controller = (SettingsController) openStage(CommonValues.SettingsFxml);
        controller.setParentController(this);
        controller.parentFxml = myFxml;
        controller.tabPane.getSelectionModel().select(controller.imageTab);
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("Copy"));
            menu.setOnAction((ActionEvent event) -> {
                copyAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("View"));
            menu.setOnAction((ActionEvent event) -> {
                ImageViewerController controller = (ImageViewerController) FxmlWindow.openStage(CommonValues.ImageViewerFxml);
                operation(controller);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Manufacture"));
            menu.setOnAction((ActionEvent event) -> {
                manufactureAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Statistic"));
            menu.setOnAction((ActionEvent event) -> {
                statisticAction();

            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("OCR"));
            menu.setOnAction((ActionEvent event) -> {
                ocrAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Split"));
            menu.setOnAction((ActionEvent event) -> {
                splitAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Sample"));
            menu.setOnAction((ActionEvent event) -> {
                sampleAction();

            });
            popMenu.getItems().add(menu);

            if (imageFile() != null) {
                menu = new MenuItem(message("Convert"));
                menu.setOnAction((ActionEvent event) -> {
                    convertAction();
                });
                popMenu.getItems().add(menu);

                menu = new MenuItem(message("Browse"));
                menu.setOnAction((ActionEvent event) -> {
                    browseAction();
                });
                popMenu.getItems().add(menu);
            }

            if (imageInformation != null) {
                popMenu.getItems().add(new SeparatorMenuItem());

                menu = new MenuItem(message("Information"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                popMenu.getItems().add(menu);

                menu = new MenuItem(message("MetaData"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    popMetaData();
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("ImagesInMyBoxClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                ImagesInMyBoxClipboardController.oneOpen();

            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImagesInSystemClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                ImagesInSystemClipboardController.oneOpen();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Settings"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                settings();

            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
