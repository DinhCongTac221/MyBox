package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.PixelsOperation.ColorActionType;
import mara.mybox.image.PixelsOperation.OperationType;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-22
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchColorController extends ImageManufactureBatchController {

    private int colorValue;
    private OperationType colorOperationType;
    private ColorActionType colorActionType;

    @FXML
    protected ToggleGroup colorGroup, opGroup;
    @FXML
    protected RadioButton setRadio, invertRadio, increaseRadio, decreaseRadio, filterRadio;
    @FXML
    protected Slider colorSlider;
    @FXML
    protected TextField colorInput;
    @FXML
    protected Label colorUnit;
    @FXML
    protected CheckBox preAlphaCheck, ignoreTransparentCheck;
    @FXML
    protected ImageView preAlphaTipsView;

    public ImageManufactureBatchColorController() {
        baseTitle = AppVariables.message("ImageManufactureBatchColor");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(colorInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });
            checkOperationType();

            colorSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    colorValue = newValue.intValue();
                    colorInput.setText(colorValue + "");
                }
            });

            colorInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorInput();
                }
            });
            checkColorInput();

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorActionType();
                }
            });
            checkColorActionType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkOperationType() {

        setRadio.setDisable(false);
        invertRadio.setDisable(false);
        filterRadio.setDisable(false);
        increaseRadio.setDisable(false);
        decreaseRadio.setDisable(false);
        setRadio.setSelected(true);
        preAlphaCheck.setVisible(false);
        preAlphaTipsView.setVisible(false);
        RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
        if (message("Brightness").equals(selected.getText())) {
            colorOperationType = OperationType.Brightness;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterRadio.setDisable(true);
            invertRadio.setDisable(true);
        } else if (message("Saturation").equals(selected.getText())) {
            colorOperationType = OperationType.Saturation;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterRadio.setDisable(true);
            invertRadio.setDisable(true);
        } else if (message("Hue").equals(selected.getText())) {
            colorOperationType = OperationType.Hue;
            colorSlider.setMax(360);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText(message("Degree"));
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterRadio.setDisable(true);
            invertRadio.setDisable(true);
        } else if (message("Red").equals(selected.getText())) {
            colorOperationType = OperationType.Red;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (message("Green").equals(selected.getText())) {
            colorOperationType = OperationType.Green;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (message("Blue").equals(selected.getText())) {
            colorOperationType = OperationType.Blue;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (message("Yellow").equals(selected.getText())) {
            colorOperationType = OperationType.Yellow;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (message("Cyan").equals(selected.getText())) {
            colorOperationType = OperationType.Cyan;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (message("Magenta").equals(selected.getText())) {
            colorOperationType = OperationType.Magenta;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (message("Opacity").equals(selected.getText())) {
            colorOperationType = OperationType.Opacity;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            invertRadio.setDisable(true);
            filterRadio.setDisable(true);
            preAlphaCheck.setVisible(true);
            preAlphaTipsView.setVisible(true);
        } else if (message("RGB").equals(selected.getText())) {
            colorOperationType = OperationType.RGB;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }
            setRadio.setDisable(true);
            filterRadio.setDisable(true);
            invertRadio.setSelected(true);
        }
    }

    private void checkColorInput() {
        try {
            colorValue = Integer.valueOf(colorInput.getText());
            if (colorValue >= 0 && colorValue <= colorSlider.getMax()) {
                colorInput.setStyle(null);
                colorSlider.setValue(colorValue);
            } else {
                colorInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            colorInput.setStyle(badStyle);
        }
    }

    private void checkColorActionType() {
        RadioButton selected = (RadioButton) opGroup.getSelectedToggle();
        if (message("Set").equals(selected.getText())) {
            colorActionType = ColorActionType.Set;
        } else if (message("Increase").equals(selected.getText())) {
            colorActionType = ColorActionType.Increase;
        } else if (message("Decrease").equals(selected.getText())) {
            colorActionType = ColorActionType.Decrease;
        } else if (message("Filter").equals(selected.getText())) {
            colorActionType = ColorActionType.Filter;
        } else if (message("Invert").equals(selected.getText())) {
            colorActionType = ColorActionType.Invert;
        } else {
            colorActionType = null;
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        if (null == colorOperationType || colorActionType == null) {
            return null;
        }
        try {
            if (colorOperationType == OperationType.Opacity && preAlphaCheck.isSelected()) {
                colorOperationType = OperationType.PreOpacity;
            }
            PixelsOperation pixelsOperation = PixelsOperation.create(source, null,
                    colorOperationType, colorActionType);
            pixelsOperation.setSkipTransparent(ignoreTransparentCheck.isSelected());
            switch (colorOperationType) {
                case Hue:
                    pixelsOperation.setFloatPara1(colorValue / 360.0f);
                    break;
                case Brightness:
                case Saturation:
                    pixelsOperation.setFloatPara1(colorValue / 100.0f);
                    break;
                case Red:
                case Green:
                case Blue:
                case Yellow:
                case Cyan:
                case Magenta:
                case RGB:
                    pixelsOperation.setIntPara1(colorValue);
                    break;
                case Opacity:
                case PreOpacity:
                    pixelsOperation.setIntPara1(colorValue * 255 / 100);
                    break;
            }
            BufferedImage target = pixelsOperation.operate();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

}
