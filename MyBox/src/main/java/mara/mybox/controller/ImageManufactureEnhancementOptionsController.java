package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.table.TableConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageContrast.ContrastAlgorithm;
import mara.mybox.image.ImageConvolution.SharpenAlgorithm;
import mara.mybox.image.ImageConvolution.SmoothAlgorithm;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation.OperationType;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-29
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureEnhancementOptionsController extends ImageManufactureOperationController {

    protected OperationType enhanceType;
    protected int intPara1, intPara2, intPara3;
    protected List<ConvolutionKernel> kernels;
    protected ConvolutionKernel kernel, loadedKernel;
    protected ContrastAlgorithm contrastAlgorithm;
    protected SmoothAlgorithm smoothAlgorithm;
    protected SharpenAlgorithm sharpenAlgorithm;
    protected ChangeListener<String> intBoxListener, stringBoxListener,
            intInput1Listener, intInput2Listener;
    protected ChangeListener<Number> numberBoxListener;
    protected ImageView manageView;

    @FXML
    protected ToggleGroup enhancementGroup, blurGroup, sharpenGroup, contrastGroup;
    @FXML
    protected RadioButton ContrastRadio, smoothRadio, SharpenRadio, ConvolutionRadio;
    @FXML
    protected TextField intInput1, intInput2;
    @FXML
    protected VBox setBox, blurABox, sharpenABox, contrastABox;
    @FXML
    protected FlowPane stringSelectorPane, intSelectorPane, intInput1Pane, intInput2Pane;
    @FXML
    protected ComboBox<String> intSelector, stringSelector;
    @FXML
    protected CheckBox valueCheck;
    @FXML
    protected Label intListLabel, stringLabel, intLabel1, intLabel2, commentsLabel;
    @FXML
    protected Button button;

    @Override
    public void initControls() {
        try {
            super.initControls();

            enhancementGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkEnhanceType();
                }
            });

            blurGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkSmoothAlgorithm();
                }
            });

            sharpenGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkSharpenAlgorithm();
                }
            });

            contrastGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkContrastAlgorithm();
                }
            });

            manageView = new ImageView();

            checkEnhanceType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void setValues(BaseController parent) {
        parentController = parent;
        if (parentController == null) {
            return;
        }
        if (parentController instanceof ImageManufactureEnhancementController) {
            ImageManufactureEnhancementController pController = (ImageManufactureEnhancementController) parentController;
            imageController = pController.imageController;
            commentsLabel = pController.commentsLabel;
            okButton = pController.okButton;
        }
    }

    protected void checkEnhanceType() {
        try {
            if (imageController != null) {
                imageController.resetImagePane();
                if (scopeController != null && scopeController.scope != null
                        && scopeController.scope.getScopeType() != ImageScope.ScopeType.All) {
                    imageController.hideImagePane();
                    imageController.showScopePane();
                } else {
                    imageController.hideScopePane();
                    imageController.showImagePane();
                }
            }

            clearValues();
            if (okButton != null && enhancementGroup.getSelectedToggle() == null) {
                okButton.setDisable(true);
                return;
            }
            RadioButton selected = (RadioButton) enhancementGroup.getSelectedToggle();
            if (ContrastRadio.equals(selected)) {
                if (imageController != null) {
                    imageController.showImagePane();
                    imageController.hideScopePane();
                    commentsLabel.setText(message("ManufactureWholeImage"));
                }
                enhanceType = OperationType.Contrast;
                makeContrastBox();

            } else {
                if (imageController != null) {
                    imageController.showScopePane();
                    imageController.hideImagePane();
                    commentsLabel.setText(message("DefineScopeAndManufacture"));
                }

                if (smoothRadio.equals(selected)) {
                    enhanceType = OperationType.Smooth;
                    makeSmoothBox();

                } else if (SharpenRadio.equals(selected)) {
                    enhanceType = OperationType.Sharpen;
                    makeSharpenBox();

                } else if (ConvolutionRadio.equals(selected)) {
                    enhanceType = OperationType.Convolution;
                    makeConvolutionBox();

                }
            }

            FxmlControl.refreshStyle(setBox);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void clearValues() {
        setBox.getChildren().clear();
        if (stringBoxListener != null) {
            stringSelector.getSelectionModel().selectedItemProperty().removeListener(stringBoxListener);
        }
        if (numberBoxListener != null) {
            stringSelector.getSelectionModel().selectedIndexProperty().removeListener(numberBoxListener);
        }
        if (intBoxListener != null) {
            intSelector.getSelectionModel().selectedItemProperty().removeListener(intBoxListener);
        }
        if (intInput1Listener != null) {
            intInput1.textProperty().removeListener(intInput1Listener);
        }
        if (intInput2Listener != null) {
            intInput2.textProperty().removeListener(intInput2Listener);
        }
        valueCheck.setDisable(false);
        button.setOnAction(null);
        button.disableProperty().unbind();
        button.setDisable(false);
        stringSelector.getItems().clear();
        stringSelector.getEditor().setStyle(null);
        intSelector.getItems().clear();
        intSelector.getEditor().setStyle(null);
        intInput1.setStyle(null);
        intInput2.setStyle(null);
        stringSelector.setEditable(false);
        intSelector.setEditable(false);
        intSelector.setDisable(false);
        kernel = null;
        if (okButton != null) {
            okButton.disableProperty().unbind();
            okButton.setDisable(false);
            commentsLabel.setText("");
        }
    }

    protected void makeSmoothBox() {
        try {
            intPara1 = 10;
            intListLabel.setText(message("Intensity"));
            intSelector.setEditable(true);
            intBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            FxmlControl.setEditorNormal(intSelector);
                        } else {
                            FxmlControl.setEditorBadStyle(intSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intSelector);
                    }
                }
            };
            intSelector.getSelectionModel().selectedItemProperty().addListener(intBoxListener);
            intSelector.getItems().addAll(Arrays.asList("3", "5", "10", "2", "1", "8", "15", "20", "30"));
            intSelector.getSelectionModel().select(0);

            setBox.getChildren().addAll(blurABox, intSelectorPane);

            if (okButton != null) {
                okButton.disableProperty().bind(intSelector.getEditor().styleProperty().isEqualTo(badStyle));
            }

            checkSmoothAlgorithm();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void makeSharpenBox() {
        try {
            intPara1 = 2;
            intListLabel.setText(message("Intensity"));
            intSelector.setEditable(true);
            intBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            FxmlControl.setEditorNormal(intSelector);
                        } else {
                            FxmlControl.setEditorBadStyle(intSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intSelector);
                    }
                }
            };
            intSelector.getSelectionModel().selectedItemProperty().addListener(intBoxListener);
            intSelector.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5"));
            intSelector.getSelectionModel().select(0);

            setBox.getChildren().addAll(sharpenABox, intSelectorPane);
            if (okButton != null) {
                okButton.disableProperty().bind(intSelector.getEditor().styleProperty().isEqualTo(badStyle));
            }

            checkSharpenAlgorithm();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void makeContrastBox() {
        try {
            contrastAlgorithm = ContrastAlgorithm.HSB_Histogram_Equalization;
            setBox.getChildren().addAll(contrastABox);

            checkContrastAlgorithm();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void makeConvolutionBox() {
        stringLabel.setText(message("ConvolutionKernel"));
        kernel = null;
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    if (kernels == null) {
                        kernels = TableConvolutionKernel.read();
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    try {
                        loadKernelsList(kernels);
                        numberBoxListener = new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                                int index = newValue.intValue();
                                if (index < 0 || index >= kernels.size()) {
                                    kernel = null;
                                    FxmlControl.setEditorBadStyle(stringSelector);
                                    return;
                                }
                                kernel = kernels.get(index);
                                FxmlControl.setEditorNormal(stringSelector);
                            }
                        };
                        stringSelector.getSelectionModel().selectedIndexProperty().addListener(numberBoxListener);

                        manageView.setImage(new Image(ControlStyle.getIcon("iconSetting.png")));
                        manageView.setFitWidth(20);
                        manageView.setFitHeight(20);
                        button.setGraphic(manageView);
                        button.setText("");
                        FxmlControl.setTooltip(button, new Tooltip(message("ManageDot")));
                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                BaseController c = openStage(CommonValues.ConvolutionKernelManagerFxml);
                                c.setParentController(myController);
                                c.setParentFxml(myFxml);
                            }
                        });

                        setBox.getChildren().addAll(stringSelectorPane, button);
                        if (okButton != null) {
                            okButton.disableProperty().bind(stringSelector.getEditor().styleProperty().isEqualTo(badStyle));
                        }

                        if (loadedKernel != null) {
                            kernel = loadedKernel;
                            stringSelector.getSelectionModel().select(kernel.getName());
                            if (okButton != null) {
                                parentController.okAction();
                            }
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                }
            };
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void checkSmoothAlgorithm() {
        try {
            RadioButton selected = (RadioButton) blurGroup.getSelectedToggle();
            String name = selected.getText();
            for (SmoothAlgorithm a : SmoothAlgorithm.values()) {
                if (message(a.name()).equals(name)) {
                    smoothAlgorithm = a;
                    break;
                }
            }
        } catch (Exception e) {
            smoothAlgorithm = SmoothAlgorithm.AverageBlur;
        }

    }

    protected void checkSharpenAlgorithm() {
        try {
            RadioButton selected = (RadioButton) sharpenGroup.getSelectedToggle();
            String name = selected.getText();
            for (SharpenAlgorithm a : SharpenAlgorithm.values()) {
                if (message(a.name()).equals(name)) {
                    sharpenAlgorithm = a;
                    break;
                }
            }
        } catch (Exception e) {
            sharpenAlgorithm = SharpenAlgorithm.UnsharpMasking;
        }
        intSelector.setDisable(sharpenAlgorithm != SharpenAlgorithm.UnsharpMasking);
    }

    protected void checkContrastAlgorithm() {
        try {
            RadioButton selected = (RadioButton) contrastGroup.getSelectedToggle();
            String name = selected.getText();
            if (setBox.getChildren() != null) {
                if (setBox.getChildren().contains(intInput1Pane)) {
                    setBox.getChildren().removeAll(intInput1Pane);
                }
                if (setBox.getChildren().contains(intInput2Pane)) {
                    setBox.getChildren().removeAll(intInput2Pane);
                }
            }
            if (okButton != null) {
                okButton.disableProperty().unbind();
            }
            if (message("GrayHistogramEqualization").equals(name)) {
                contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Equalization;

            } else if (message("GrayHistogramStretching").equals(name)) {
                contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Stretching;
                intPara1 = 100;
                intLabel1.setText(message("LeftThreshold"));
                intInput1.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        try {
                            int v = Integer.valueOf(intInput1.getText());
                            if (v >= 0) {
                                intPara1 = v;
                                intInput1.setStyle(null);
                            } else {
                                intInput1.setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            intInput1.setStyle(badStyle);
                        }
                    }
                });
                intInput1.setText("100");

                intPara2 = 100;
                intLabel2.setText(message("RightThreshold"));
                intInput2.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        try {
                            int v = Integer.valueOf(intInput2.getText());
                            if (v >= 0) {
                                intPara2 = v;
                                intInput2.setStyle(null);
                            } else {
                                intInput2.setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            intInput2.setStyle(badStyle);
                        }
                    }
                });
                intInput2.setPrefWidth(100);
                intInput2.setText("100");

                setBox.getChildren().addAll(intInput1Pane, intInput2Pane);
                if (okButton != null) {
                    okButton.disableProperty().bind(intInput1.styleProperty().isEqualTo(badStyle)
                            .or(intInput2.styleProperty().isEqualTo(badStyle))
                    );
                }

            } else if (message("GrayHistogramShifting").equals(name)) {
                contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Shifting;
                intPara1 = 80;
                intLabel1.setText(message("Offset"));
                intInput1.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        try {
                            int v = Integer.valueOf(intInput1.getText());
                            if (v >= -255 && v <= 255) {
                                intPara1 = v;
                                intInput1.setStyle(null);
                            } else {
                                intInput1.setStyle(badStyle);
                                popError("-255 ~ 255");
                            }
                        } catch (Exception e) {
                            intInput1.setStyle(badStyle);
                            popError("-255 ~ 255");
                        }
                    }
                });
                intInput1.setText("10");
                FxmlControl.setTooltip(intInput1, new Tooltip("-255 ~ 255"));
                setBox.getChildren().addAll(intInput1Pane);
                if (okButton != null) {
                    okButton.disableProperty().bind(intInput1.styleProperty().isEqualTo(badStyle));
                }

            } else if (message("HSBHistogramEqualization").equals(name)) {
                contrastAlgorithm = ContrastAlgorithm.HSB_Histogram_Equalization;

            }

        } catch (Exception e) {
            smoothAlgorithm = SmoothAlgorithm.AverageBlur;
        }

    }

    public void applyKernel(ConvolutionKernel kernel) {
        loadedKernel = kernel;
        ConvolutionRadio.fire();
        checkEnhanceType();
    }

    public void loadKernelsList(List<ConvolutionKernel> records) {
        if (enhanceType != OperationType.Convolution || stringSelector == null) {
            return;
        }
        kernels = records;
        stringSelector.getItems().clear();
        if (kernels != null && !kernels.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (ConvolutionKernel k : kernels) {
                names.add(k.getName());
            }
            stringSelector.getItems().addAll(names);
            stringSelector.getSelectionModel().select(0);
            FxmlControl.setEditorNormal(stringSelector);
        } else {
            FxmlControl.setEditorBadStyle(stringSelector);
        }
    }

}
