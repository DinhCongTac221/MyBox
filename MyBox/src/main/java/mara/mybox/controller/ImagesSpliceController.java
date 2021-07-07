package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlWindow;
import mara.mybox.image.ImageCombine;
import mara.mybox.image.ImageCombine.ArrayType;
import mara.mybox.image.ImageCombine.CombineSizeType;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-8-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesSpliceController extends ImageViewerController {

    protected ObservableList<ImageInformation> tableData;
    protected TableView<ImageInformation> tableView;
    protected ImageCombine imageCombine;

    @FXML
    protected ControlImagesTable tableController;
    @FXML
    protected ToggleGroup sizeGroup, arrayGroup;
    @FXML
    protected RadioButton arrayColumnRadio, arrayRowRadio, arrayColumnsRadio, keepSizeRadio, sizeBiggerRadio,
            sizeSmallerRadio, eachWidthRadio, eachHeightRadio, totalWidthRadio, totalHeightRadio;
    @FXML
    protected TextField totalWidthInput, totalHeightInput, eachWidthInput, eachHeightInput;
    @FXML
    protected ComboBox<String> columnsBox, intervalBox, MarginsBox;
    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected Button newWindowButton;
    @FXML
    protected HBox opBox;
    @FXML
    protected CheckBox openCheck;

    public ImagesSpliceController() {
        baseTitle = AppVariables.message("ImagesSplice");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            imageCombine = new ImageCombine();

            tableController.parentController = this;
            tableController.parentFxml = myFxml;

            tableData = tableController.tableData;
            tableView = tableController.tableView;

            initArraySection();
            initSizeSection();
            initTargetSection();

            saveButton.disableProperty().bind(Bindings.isEmpty(tableData));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void initArraySection() {
        try {
            columnsBox.getItems().addAll(Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10"));
            columnsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        int columnsValue = Integer.valueOf(newValue);
                        if (columnsValue > 0) {
                            imageCombine.setColumnsValue(columnsValue);
                            AppVariables.setUserConfigValue(baseName + "Columns", columnsValue + "");
                            combineImages();
                            FxmlControl.setEditorNormal(columnsBox);
                        } else {
                            imageCombine.setColumnsValue(-1);
                            FxmlControl.setEditorBadStyle(columnsBox);
                        }

                    } catch (Exception e) {
                        imageCombine.setColumnsValue(-1);
                        FxmlControl.setEditorBadStyle(columnsBox);
                    }
                }
            });
            columnsBox.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Columns", "2"));

            intervalBox.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            intervalBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int intervalValue = Integer.valueOf(newValue);
                        if (intervalValue >= 0) {
                            imageCombine.setIntervalValue(intervalValue);
                            AppVariables.setUserConfigValue(baseName + "Interval", intervalValue + "");
                            FxmlControl.setEditorNormal(intervalBox);
                            combineImages();
                        } else {
                            FxmlControl.setEditorBadStyle(intervalBox);
                        }

                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intervalBox);
                    }
                }
            });
            intervalBox.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Interval", "5"));

            MarginsBox.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            MarginsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int MarginsValue = Integer.valueOf(newValue);
                        if (MarginsValue >= 0) {
                            imageCombine.setMarginsValue(MarginsValue);
                            AppVariables.setUserConfigValue(baseName + "Margin", MarginsValue + "");
                            FxmlControl.setEditorNormal(MarginsBox);
                            combineImages();
                        } else {
                            FxmlControl.setEditorBadStyle(MarginsBox);
                        }

                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(MarginsBox);
                    }
                }
            });
            MarginsBox.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Margin", "5"));

            colorSetController.init(this, baseName + "Color");
            imageCombine.setBgColor(colorSetController.color());
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    imageCombine.setBgColor((Color) newValue);
                    combineImages();
                }
            });

            arrayGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) arrayGroup.getSelectedToggle();
                    if (AppVariables.message("SingleColumn").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.SingleColumn);
                        columnsBox.setDisable(true);
                        AppVariables.setUserConfigValue(baseName + "ArrayType", "SingleColumn");
                    } else if (AppVariables.message("SingleRow").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.SingleRow);
                        columnsBox.setDisable(true);
                        AppVariables.setUserConfigValue(baseName + "ArrayType", "SingleRow");
                    } else if (AppVariables.message("ColumnsNumber").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.ColumnsNumber);
                        columnsBox.setDisable(false);
                        AppVariables.setUserConfigValue(baseName + "ArrayType", "ColumnsNumber");
                    }
                    combineImages();
                }
            });
            String arraySelect = AppVariables.getUserConfigValue(baseName + "ArrayType", "SingleColumn");
            switch (arraySelect) {
                case "SingleColumn":
                    arrayColumnRadio.setSelected(true);
                    break;
                case "SingleRow":
                    arrayRowRadio.setSelected(true);
                    break;
                case "ColumnsNumber":
                    arrayColumnsRadio.setSelected(true);
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void initSizeSection() {
        try {
            eachWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkEachWidthValue();
                }
            });
            eachWidthInput.setText(AppVariables.getUserConfigValue(baseName + "EachWidth", ""));

            eachHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkEachHeightValue();
                }
            });
            eachHeightInput.setText(AppVariables.getUserConfigValue(baseName + "EachHeight", ""));

            totalWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTotalWidthValue();
                }
            });
            totalWidthInput.setText(AppVariables.getUserConfigValue(baseName + "TotalWidth", ""));

            totalHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTotalHeightValue();
                }
            });
            totalHeightInput.setText(AppVariables.getUserConfigValue(baseName + "TotalHeight", ""));

            sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    totalWidthInput.setDisable(true);
                    totalWidthInput.setStyle(null);
                    totalHeightInput.setDisable(true);
                    totalHeightInput.setStyle(null);
                    eachWidthInput.setDisable(true);
                    eachWidthInput.setStyle(null);
                    eachHeightInput.setDisable(true);
                    eachHeightInput.setStyle(null);
                    RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
                    if (AppVariables.message("KeepSize").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.KeepSize);
                        AppVariables.setUserConfigValue(baseName + "SizeType", "KeepSize");
                        combineImages();
                    } else if (AppVariables.message("AlignAsBigger").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.AlignAsBigger);
                        AppVariables.setUserConfigValue(baseName + "SizeType", "AlignAsBigger");
                        combineImages();
                    } else if (AppVariables.message("AlignAsSmaller").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.AlignAsSmaller);
                        AppVariables.setUserConfigValue(baseName + "SizeType", "AlignAsSmaller");
                        combineImages();
                    } else if (AppVariables.message("EachWidth").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.EachWidth);
                        eachWidthInput.setDisable(false);
                        checkEachWidthValue();
                        AppVariables.setUserConfigValue(baseName + "SizeType", "EachWidth");
                    } else if (AppVariables.message("EachHeight").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.EachHeight);
                        eachHeightInput.setDisable(false);
                        checkEachHeightValue();
                        AppVariables.setUserConfigValue(baseName + "SizeType", "EachHeight");
                    } else if (AppVariables.message("TotalWidth").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.TotalWidth);
                        totalWidthInput.setDisable(false);
                        checkTotalWidthValue();
                        AppVariables.setUserConfigValue(baseName + "SizeType", "TotalWidth");
                    } else if (AppVariables.message("TotalHeight").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.TotalHeight);
                        totalHeightInput.setDisable(false);
                        checkTotalHeightValue();
                        AppVariables.setUserConfigValue(baseName + "SizeType", "TotalHeight");
                    }
                }
            });
            String arraySelect = AppVariables.getUserConfigValue(baseName + "SizeType", "KeepSize");
            switch (arraySelect) {
                case "KeepSize":
                    keepSizeRadio.setSelected(true);
                    break;
                case "AlignAsBigger":
                    sizeBiggerRadio.setSelected(true);
                    break;
                case "AlignAsSmaller":
                    sizeSmallerRadio.setSelected(true);
                    break;
                case "EachWidth":
                    eachWidthRadio.setSelected(true);
                    break;
                case "EachHeight":
                    eachHeightRadio.setSelected(true);
                    break;
                case "TotalWidth":
                    totalWidthRadio.setSelected(true);
                    break;
                case "TotalHeight":
                    totalHeightRadio.setSelected(true);
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkEachWidthValue() {
        try {
            int eachWidthValue = Integer.valueOf(eachWidthInput.getText());
            if (eachWidthValue > 0) {
                imageCombine.setEachWidthValue(eachWidthValue);
                eachWidthInput.setStyle(null);
                AppVariables.setUserConfigValue(baseName + "EachWidth", eachWidthValue + "");
                combineImages();
            } else {
                imageCombine.setEachWidthValue(-1);
                eachWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            imageCombine.setEachWidthValue(-1);
            eachWidthInput.setStyle(badStyle);
        }
    }

    private void checkEachHeightValue() {
        try {
            int eachHeightValue = Integer.valueOf(eachHeightInput.getText());
            if (eachHeightValue > 0) {
                imageCombine.setEachHeightValue(eachHeightValue);
                eachHeightInput.setStyle(null);
                AppVariables.setUserConfigValue(baseName + "EachHeight", eachHeightValue + "");
                combineImages();
            } else {
                imageCombine.setEachHeightValue(-1);
                eachHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            imageCombine.setEachHeightValue(-1);
            eachHeightInput.setStyle(badStyle);
        }
    }

    private void checkTotalWidthValue() {
        try {
            int totalWidthValue = Integer.valueOf(totalWidthInput.getText());
            if (totalWidthValue > 0) {
                imageCombine.setTotalWidthValue(totalWidthValue);
                totalWidthInput.setStyle(null);
                AppVariables.setUserConfigValue(baseName + "TotalWidth", totalWidthValue + "");
                combineImages();
            } else {
                imageCombine.setTotalWidthValue(-1);
                totalWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            imageCombine.setTotalWidthValue(-1);
            totalWidthInput.setStyle(badStyle);
        }
    }

    private void checkTotalHeightValue() {
        try {
            int totalHeightValue = Integer.valueOf(totalHeightInput.getText());
            if (totalHeightValue > 0) {
                imageCombine.setTotalHeightValue(totalHeightValue);
                totalHeightInput.setStyle(null);
                AppVariables.setUserConfigValue(baseName + "TotalHeight", totalHeightValue + "");
                combineImages();
            } else {
                imageCombine.setTotalHeightValue(-1);
                totalHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            imageCombine.setTotalHeightValue(-1);
            totalHeightInput.setStyle(badStyle);
        }
    }

    public void initTargetSection() {
        try {
            opBox.disableProperty().bind(Bindings.isEmpty(tableData).
                    or(tableController.hasSampled)
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void dataChanged() {
        super.dataChanged();
        if (!tableController.hasSampled()) {
            combineImages();
        }
    }

    @FXML
    protected void newWindow(ActionEvent event) {
        FxmlWindow.openImageViewer(image);
    }

    private void combineImages() {
        if (tableData == null || tableData.isEmpty()
                || totalWidthInput.getStyle().equals(badStyle)
                || totalHeightInput.getStyle().equals(badStyle)
                || eachWidthInput.getStyle().equals(badStyle)
                || eachHeightInput.getStyle().equals(badStyle)) {
            image = null;
            imageView.setImage(null);
            imageLabel.setText("");
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    if (imageCombine.getArrayType() == ArrayType.SingleColumn) {
                        image = ImageManufacture.combineSingleColumn(imageCombine, tableData, false, true);
                    } else if (imageCombine.getArrayType() == ArrayType.SingleRow) {
                        image = ImageManufacture.combineSingleRow(imageCombine, tableData, false, true);
                    } else if (imageCombine.getArrayType() == ArrayType.ColumnsNumber) {
                        image = combineImagesColumns(tableData);
                    } else {
                        image = null;
                    }
                    return image != null;
                }

                @Override
                protected void whenSucceeded() {
                    imageView.setImage(image);
                    setZoomStep(image);
                    fitSize();
                    imageLabel.setText(AppVariables.message("CombinedSize") + ": "
                            + (int) image.getWidth() + "x" + (int) image.getHeight());
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    private Image combineImagesColumns(List<ImageInformation> imageInfos) {
        if (imageInfos == null || imageInfos.isEmpty() || imageCombine.getColumnsValue() <= 0) {
            return null;
        }
        try {
            List<ImageInformation> rowImages = new ArrayList<>();
            List<ImageInformation> rows = new ArrayList<>();
            for (ImageInformation imageInfo : imageInfos) {
                rowImages.add(imageInfo);
                if (rowImages.size() == imageCombine.getColumnsValue()) {
                    Image rowImage = ImageManufacture.combineSingleRow(imageCombine, rowImages, true, false);
                    rows.add(new ImageInformation(rowImage));
                    rowImages = new ArrayList<>();
                }
            }
            if (!rowImages.isEmpty()) {
                Image rowImage = ImageManufacture.combineSingleRow(imageCombine, rowImages, true, false);
                rows.add(new ImageInformation(rowImage));
            }
            Image newImage = ImageManufacture.combineSingleColumn(imageCombine, rows, true, true);
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (image == null) {
            return;
        }
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                null, targetExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        targetFile = file;

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String filename;

                @Override
                protected boolean handle() {
                    filename = targetFile.getAbsolutePath();
                    String format = FileTools.getFileSuffix(filename);
                    final BufferedImage bufferedImage = FxmlImageManufacture.bufferedImage(image);
                    return ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    FxmlWindow.openImageViewer(targetFile);
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
        saveAsAction();
    }

}
