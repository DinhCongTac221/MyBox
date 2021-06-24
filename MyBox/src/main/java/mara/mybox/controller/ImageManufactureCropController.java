package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageScope;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureCropController extends ImageManufactureOperationController {

    protected int centerX, centerY, radius;

    @FXML
    protected ToggleGroup copyGroup;
    @FXML
    protected RadioButton includeRadio, excludeRadio;
    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected CheckBox clipboardCheck, clipMarginsCheck, imageMarginsCheck;

    @Override
    public void initPane() {
        try {
            colorSetController.init(this, baseName + "CropColor");

            clipboardCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "CropPutClipboard", false));
            clipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue(baseName + "CropPutClipboard", clipboardCheck.isSelected());
                }
            });

            imageMarginsCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "CropCutImageMargins", true));
            imageMarginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "CropCutImageMargins", imageMarginsCheck.isSelected());
                }
            });

            clipMarginsCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "CropCutClipMargins", true));
            clipMarginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "CropCutClipMargins", clipMarginsCheck.isSelected());
                }
            });

            clipMarginsCheck.disableProperty().bind(clipboardCheck.selectedProperty().not());

            okButton.disableProperty().bind(imageController.cropButton.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.resetImagePane();
        imageController.hideImagePane();
        imageController.showScopePane();
        if (scopeController.scope == null
                || scopeController.scope.getScopeType() == ImageScope.ScopeType.All
                || scopeController.scope.getScopeType() == ImageScope.ScopeType.Operate) {
            scopeController.scopeRectangleRadio.fire();
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (scopeController.scope == null
                || scopeController.scope.getScopeType() == ImageScope.ScopeType.All
                || scopeController.scope.getScopeType() == ImageScope.ScopeType.Operate) {
            popError(message("InvalidScope"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage, cuttedClip;

                @Override
                protected boolean handle() {
                    Color bgColor = colorSetController.color();

                    if (includeRadio.isSelected()) {
                        newImage = FxmlImageManufacture.scopeExcludeImage(imageView.getImage(),
                                scopeController.scope, bgColor, imageMarginsCheck.isSelected());

                    } else if (excludeRadio.isSelected()) {
                        newImage = FxmlImageManufacture.scopeImage(imageView.getImage(),
                                scopeController.scope, bgColor, imageMarginsCheck.isSelected());
                    } else {
                        return false;
                    }
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    if (AppVariables.getUserConfigBoolean(baseName + "CropPutClipboard", false)) {
                        if (includeRadio.isSelected()) {
                            cuttedClip = FxmlImageManufacture.scopeImage(imageView.getImage(),
                                    scopeController.scope, bgColor, clipMarginsCheck.isSelected());

                        } else if (excludeRadio.isSelected()) {
                            cuttedClip = FxmlImageManufacture.scopeExcludeImage(imageView.getImage(),
                                    scopeController.scope, bgColor, clipMarginsCheck.isSelected());
                        }
                        ImageClipboard.add(cuttedClip, ImageClipboard.ImageSource.Crop);
                    }
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    imageController.popSuccessful();
                    if (excludeRadio.isSelected() && imageMarginsCheck.isSelected()) {
                        scopeController.scopeAllRadio.fire();
                    }
                    imageController.updateImage(ImageOperation.Crop, newImage, cost);
                    if (cuttedClip != null) {
                        if (operationsController.clipboardController != null) {
                            operationsController.clipboardController.loadClipboard();
                        }
                        operationsController.clipboardPane.setExpanded(true);
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

}
