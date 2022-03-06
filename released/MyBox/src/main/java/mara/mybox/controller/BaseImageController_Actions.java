package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.CropTools;
import mara.mybox.fximage.TransformTools;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_Actions extends BaseImageController_Image {

    protected int currentAngle = 0, rotateAngle = 90;

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
                selectAreaCheck.setSelected(UserConfig.getBoolean(baseName + "SelectArea", false));
                selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "SelectArea", selectAreaCheck.isSelected());
                        checkSelect();
                    }
                });
                checkSelect();
            }

            if (handleSelectCheck != null) {
                handleSelectCheck.setSelected(UserConfig.getBoolean(baseName + "HandleSelectArea", true));
                handleSelectCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "HandleSelectArea", handleSelectCheck.isSelected());
                    }
                });
            }

            if (pickColorCheck != null) {
                pickColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        isPickingColor = pickColorCheck.isSelected();
                        checkPickingColor();
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected Image scopeImage() {
        Image inImage = imageView.getImage();

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            if (maskRectangleData.getSmallX() == 0
                    && maskRectangleData.getSmallY() == 0
                    && maskRectangleData.getBigX() == (int) inImage.getWidth() - 1
                    && maskRectangleData.getBigY() == (int) inImage.getHeight() - 1) {
                return null;
            }
            return CropTools.cropOutsideFx(inImage, maskRectangleData, Color.WHITE);

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            return CropTools.cropOutsideFx(inImage, maskCircleData, Color.WHITE);

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            return CropTools.cropOutsideFx(inImage, maskEllipseData, Color.WHITE);

        } else if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
            return CropTools.cropOutsideFx(inImage, maskPolygonData, Color.WHITE);

        } else {
            return null;
        }
    }

    public Image imageToHandle() {
        Image selected = null;
        if (handleSelectCheck == null || handleSelectCheck.isSelected()) {
            selected = scopeImage();
        }
        if (selected == null) {
            selected = imageView.getImage();
        }
        return selected;
    }

    public Object imageToSaveAs() {
        return imageToHandle();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        if (!checkBeforeNextAction()) {
            return;
        }
        Image clip = ImageClipboardTools.fetchImageInClipboard(false);
        if (clip == null) {
            popError(message("NoImageInClipboard"));
            return;
        }
        loadImage(clip);
    }

    @FXML
    public void viewAction() {
        ImageViewerController controller = (ImageViewerController) openStage(Fxmls.ImageViewerFxml);
        checkImage(controller);
    }

    @FXML
    @Override
    public boolean popAction() {
        if (handleSelectCheck == null || handleSelectCheck.isSelected()) {
            ImagePopController.openImage(this, scopeImage());
        } else {
            ImagePopController.openView(this, imageView);
        }
        return true;
    }

    @FXML
    protected void manufactureAction() {
        ImageManufactureController controller = (ImageManufactureController) openStage(Fxmls.ImageManufactureFxml);
        checkImage(controller);
    }

    @FXML
    public void browseAction() {
        ImagesBrowserController controller = (ImagesBrowserController) openStage(Fxmls.ImagesBrowserFxml);
        File file = imageFile();
        if (file != null) {
            controller.loadImages(file.getParentFile(), 9);
        }
    }

    @FXML
    public void statisticAction() {
        ImageAnalyseController controller = (ImageAnalyseController) openStage(Fxmls.ImageAnalyseFxml);
        checkImage(controller);
    }

    @FXML
    public void ocrAction() {
        ImageOCRController controller = (ImageOCRController) openStage(Fxmls.ImageOCRFxml);
        checkImage(controller);
    }

    @FXML
    public void splitAction() {
        ImageSplitController controller = (ImageSplitController) openStage(Fxmls.ImageSplitFxml);
        checkImage(controller);
    }

    @FXML
    public void sampleAction() {
        ImageSampleController controller = (ImageSampleController) openStage(Fxmls.ImageSampleFxml);
        checkImage(controller);
    }

    @FXML
    public void convertAction() {
        ImageConverterBatchController controller = (ImageConverterBatchController) openStage(Fxmls.ImageConverterBatchFxml);
        File file = imageFile();
        if (file != null) {
            controller.tableController.addFile(file);
        }
    }

    @FXML
    public void settings() {
        SettingsController controller = SettingsController.oneOpen(this);
        controller.tabPane.getSelectionModel().select(controller.imageTab);
    }

    @FXML
    @Override
    public void copyAction() {
        copyToSystemClipboard();
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private Image areaImage;

                @Override
                protected boolean handle() {
                    areaImage = imageToHandle();
                    return areaImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    ImageClipboardTools.copyToSystemClipboard(myController, areaImage);
                }

            };
            start(task);
        }
    }

    @Override
    public void copyToMyBoxClipboard() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private Image areaImage;

                @Override
                protected boolean handle() {
                    areaImage = imageToHandle();
                    return ImageClipboard.add(areaImage, ImageClipboard.ImageSource.Copy) != null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("CopiedInMyBoxClipBoard"));
                }

            };
            start(task);
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (imageView.getImage() == null || maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            return;
        }
        maskRectangleData = new DoubleRectangle(0, 0, getImageWidth() - 1, getImageHeight() - 1);
        drawMaskRectangleLineAsData();
    }

    @FXML
    @Override
    public void infoAction() {
        if (imageInformation == null) {
            return;
        }
        ControllerTools.showImageInformation(imageInformation);
    }

    @FXML
    public void metaAction() {
        if (imageInformation == null) {
            return;
        }
        ControllerTools.showImageMetaData(imageInformation);
    }

    public void checkImage(BaseImageController controller) {
        if (imageView == null || imageView.getImage() == null || controller == null) {
            return;
        }
        controller.toFront();
        if (maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            if (imageChanged) {
                controller.loadImage(imageView.getImage());

            } else {
                if (imageInformation != null && imageInformation.getRegion() != null) {
                    controller.loadRegion(imageInformation);
                } else if (controller instanceof ImageSampleController || controller instanceof ImageSplitController) {
                    controller.loadImage(imageFile(), imageInformation, imageView.getImage(), imageChanged);
                } else if (imageInformation != null && imageInformation.isIsScaled()) {
                    controller.loadImage(imageView.getImage());
                } else {
                    controller.loadImage(imageFile(), imageInformation, imageView.getImage(), imageChanged);
                }
            }
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private Image areaImage;

                @Override
                protected boolean handle() {
                    areaImage = imageToHandle();
                    return areaImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    controller.loadImage(areaImage);
                }
            };
            start(task);
        }
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

            menu = new MenuItem(message("CopyToSystemClipboard"), StyleTools.getIconImage("iconCopySystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                copyToSystemClipboard();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("CopyToMyBoxClipboard"), StyleTools.getIconImage("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                copyToMyBoxClipboard();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Pop"), StyleTools.getIconImage("iconPop.png"));
            menu.setOnAction((ActionEvent event) -> {
                popAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("View"), StyleTools.getIconImage("iconView.png"));
            menu.setOnAction((ActionEvent event) -> {
                viewAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Manufacture"), StyleTools.getIconImage("iconEdit.png"));
            menu.setOnAction((ActionEvent event) -> {
                manufactureAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Statistic"), StyleTools.getIconImage("iconStatistic.png"));
            menu.setOnAction((ActionEvent event) -> {
                statisticAction();

            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("OCR"), StyleTools.getIconImage("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                ocrAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Split"), StyleTools.getIconImage("iconSplit.png"));
            menu.setOnAction((ActionEvent event) -> {
                splitAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Sample"), StyleTools.getIconImage("iconSample.png"));
            menu.setOnAction((ActionEvent event) -> {
                sampleAction();

            });
            popMenu.getItems().add(menu);

            if (imageFile() != null) {
                menu = new MenuItem(message("Convert"), StyleTools.getIconImage("iconDelimiter.png"));
                menu.setOnAction((ActionEvent event) -> {
                    convertAction();
                });
                popMenu.getItems().add(menu);

                menu = new MenuItem(message("Browse"), StyleTools.getIconImage("iconBrowse.png"));
                menu.setOnAction((ActionEvent event) -> {
                    browseAction();
                });
                popMenu.getItems().add(menu);
            }

            if (imageInformation != null) {
                popMenu.getItems().add(new SeparatorMenuItem());

                menu = new MenuItem(message("Information"), StyleTools.getIconImage("iconInfo.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                popMenu.getItems().add(menu);

                menu = new MenuItem(message("MetaData"), StyleTools.getIconImage("iconMeta.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    metaAction();
                });
                popMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("ImagesInSystemClipboard"), StyleTools.getIconImage("iconSystemClipboard.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageInSystemClipboardController.oneOpen();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImagesInMyBoxClipboard"), StyleTools.getIconImage("iconClipboard.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageInMyBoxClipboardController.oneOpen();

            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("Settings"), StyleTools.getIconImage("iconSetting.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                settings();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        Point2D localToScreen = scrollPane.localToScreen(scrollPane.getWidth() - 80, 80);
        MenuImageBaseController.open((BaseImageController) this, localToScreen.getX(), localToScreen.getY());
        return true;
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
            task = new SingletonTask<Void>(this) {

                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = TransformTools.rotateImage(imageView.getImage(), rotateAngle);
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
            start(task);
        }
    }

    @FXML
    @Override
    public void playAction() {
        try {
            ImagesPlayController controller = (ImagesPlayController) openStage(Fxmls.ImagesPlayFxml);
            controller.sourceFileChanged(sourceFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        saveImage(imageFile(), file, imageToSaveAs());
    }

    public void saveImage(File srcFile, File newfile, Object imageToSave) {
        if (newfile == null || imageToSave == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
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
                    boolean multipleFrames = srcFile != null && framesNumber > 1;
                    if (multipleFrames) {
                        error = ImageFileWriters.writeFrame(srcFile, frameIndex, bufferedImage, newfile, null);
                        return error == null;
                    } else {
                        return ImageFileWriters.writeImageFile(bufferedImage, newfile);
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    recordFileWritten(newfile);

                    if (saveAsType == SaveAsType.Load) {
                        sourceFileChanged(newfile);

                    } else if (saveAsType == SaveAsType.Open) {
                        ControllerTools.openImageViewer(newfile);

                    }
                }
            };
            start(task);
        }
    }

}
