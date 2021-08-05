package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.CropTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileReaders;
import static mara.mybox.tools.DoubleTools.scale;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-12-03
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageSampleController extends ImageViewerController {

    private int widthScale, heightScale;
    private double x1, y1, x2, y2;

    @FXML
    protected Label infoLabel;
    @FXML
    protected TextField rectLeftTopXInput, rectLeftTopYInput, rightBottomXInput, rightBottomYInput;
    @FXML
    protected ComboBox<String> widthScaleSelector, heightScaleSelector;

    public ImageSampleController() {
        baseTitle = Languages.message("ImageSample");
        TipsLabelKey = "ImageSampleTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            operateOriginalSize = true;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            rightPane.disableProperty().bind(imageView.imageProperty().isNull());

            List<String> values = Arrays.asList("1", "2", "3", "4", "5", "6", "8", "9", "10", "15", "20",
                    "25", "30", "50", "80", "100", "200", "500", "800", "1000");
            widthScaleSelector.getItems().addAll(values);
            widthScaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkScales();
                }
            });
            widthScaleSelector.getSelectionModel().select("1");

            heightScaleSelector.getItems().addAll(values);
            heightScaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkScales();
                }
            });
            heightScaleSelector.getSelectionModel().select("1");

            rectLeftTopXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRegion();
                }
            });
            rectLeftTopYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRegion();
                }
            });
            rightBottomXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRegion();
                }
            });
            rightBottomYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRegion();
                }
            });

            initMaskControls(false);

            okButton.disableProperty().bind(
                    widthScaleSelector.getEditor().styleProperty().isEqualTo(NodeStyleTools.badStyle)
                            .or(heightScaleSelector.getEditor().styleProperty().isEqualTo(NodeStyleTools.badStyle))
                            .or(Bindings.isEmpty(heightScaleSelector.getEditor().textProperty()))
                            .or(Bindings.isEmpty(widthScaleSelector.getEditor().textProperty()))
                            .or(Bindings.isEmpty(rectLeftTopXInput.textProperty()))
                            .or(rectLeftTopXInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                            .or(Bindings.isEmpty(rectLeftTopYInput.textProperty()))
                            .or(rectLeftTopYInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                            .or(Bindings.isEmpty(rightBottomXInput.textProperty()))
                            .or(rightBottomXInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                            .or(Bindings.isEmpty(rightBottomYInput.textProperty()))
                            .or(rightBottomYInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
            );
            saveButton.disableProperty().bind(okButton.disabledProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkScales() {
        if (isSettingValues || image == null) {
            return;
        }
        try {
            int v = Integer.valueOf(widthScaleSelector.getSelectionModel().getSelectedItem());
            if (v > 0) {
                widthScale = v;
                ValidationTools.setEditorNormal(widthScaleSelector);
            } else {
                ValidationTools.setEditorBadStyle(widthScaleSelector);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(widthScaleSelector);
        }
        try {
            int v = Integer.valueOf(heightScaleSelector.getSelectionModel().getSelectedItem());
            if (v > 0) {
                heightScale = v;
                ValidationTools.setEditorNormal(heightScaleSelector);
            } else {
                ValidationTools.setEditorBadStyle(heightScaleSelector);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(heightScaleSelector);
        }
        updateLabel();
    }

    public DoubleRectangle checkRegion() {
        if (isSettingValues) {
            return null;
        }
        try {

            try {
                x1 = Double.parseDouble(rectLeftTopXInput.getText());
                rectLeftTopXInput.setStyle(null);
            } catch (Exception e) {
                rectLeftTopXInput.setStyle(NodeStyleTools.badStyle);
                return null;
            }
            try {
                y1 = Double.parseDouble(rectLeftTopYInput.getText());
                rectLeftTopYInput.setStyle(null);
            } catch (Exception e) {
                rectLeftTopYInput.setStyle(NodeStyleTools.badStyle);
                return null;
            }
            try {
                x2 = Double.parseDouble(rightBottomXInput.getText());
                rightBottomXInput.setStyle(null);
            } catch (Exception e) {
                rightBottomXInput.setStyle(NodeStyleTools.badStyle);
                return null;
            }
            try {
                y2 = Double.parseDouble(rightBottomYInput.getText());
                rightBottomYInput.setStyle(null);
            } catch (Exception e) {
                rightBottomYInput.setStyle(NodeStyleTools.badStyle);
                return null;
            }
            DoubleRectangle rect = new DoubleRectangle(
                    x1 * widthRatio(), y1 * heightRatio(),
                    x2 * widthRatio(), y2 * heightRatio());
            if (!rect.isValid()) {
                popError(Languages.message("InvalidData"));
                return null;
            }
            return rect;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    private void updateLabel() {
        if (imageView.getImage() == null) {
            return;
        }
        if (widthScale < 1 || heightScale < 1) {
            infoLabel.setText(Languages.message("InvalidParameters"));
            return;
        }
        infoLabel.setText(Languages.message("ImageSize") + ": "
                + getOperationWidth() + "x" + getOperationHeight()
                + "\n" + Languages.message("SamplingSize") + ": "
                + (int) (maskRectangleData.getWidth() / (widthRatio() * widthScale))
                + "x" + (int) (maskRectangleData.getHeight() / (heightRatio() * heightScale)));

    }

    @Override
    public boolean drawMaskRectangleLineAsData() {
        if (maskRectangleLine == null || !maskPane.getChildren().contains(maskRectangleLine)
                || maskRectangleData == null
                || imageView == null || imageView.getImage() == null) {
            return false;
        }
        if (!super.drawMaskRectangleLineAsData()) {
            return false;
        }
        rectLeftTopXInput.setText(scale(maskRectangleData.getSmallX() / widthRatio(), 2) + "");
        rectLeftTopYInput.setText(scale(maskRectangleData.getSmallY() / heightRatio(), 2) + "");
        rightBottomXInput.setText(scale(maskRectangleData.getBigX() / widthRatio(), 2) + "");
        rightBottomYInput.setText(scale(maskRectangleData.getBigY() / heightRatio(), 2) + "");
        updateLabel();
        return true;
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            if (imageView.getImage() == null) {
                return false;
            }
            fitSize();
            initMaskRectangleLine(true);
            checkScales();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    @FXML
    @Override
    public void okAction() {
        try {
            DoubleRectangle rect = checkRegion();
            if (rect == null) {
                return;
            }
            maskRectangleData = rect;
            isSettingValues = true;
            drawMaskRectangleLineAsData();
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public BufferedImage imageToSave() {
        if (sourceFile != null && imageInformation != null) {
            return ImageFileReaders.readFrame(imageInformation.getImageFormat(),
                    sourceFile.getAbsolutePath(), imageInformation.getIndex(),
                    (int) x1, (int) y1, (int) x2, (int) y2, widthScale, heightScale);
        } else if (image != null) {
            return CropTools.sample(SwingFXUtils.fromFXImage(image, null),
                    (int) x1, (int) y1, (int) x2, (int) y2, widthScale, heightScale);
        } else {
            return null;
        }
    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (image == null || widthScale < 1 || heightScale < 1) {
            popError(Languages.message("InvalidParameters"));
            return;
        }
        super.saveAsAction();
    }

}
