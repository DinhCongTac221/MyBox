package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.BufferedImageTools.Direction;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageGray;
import mara.mybox.bufferedimage.ImageMosaic;
import mara.mybox.bufferedimage.ImageQuantization;
import mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.bufferedimage.ImageQuantizationFactory;
import mara.mybox.bufferedimage.ImageQuantizationFactory.KMeansClusteringQuantization;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperation.OperationType;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureEffectsController extends ImageManufactureOperationController {

    protected List<Color> quantizationColors;
    protected StringTable quanTable;

    @FXML
    protected ImageManufactureEffectsOptionsController optionsController;
    @FXML
    protected Button demoButton, paletteAddButton, htmlButton;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(paletteAddButton, message("AddInColorPalette"));
            NodeStyleTools.setTooltip(htmlButton, message("ShowData"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initPane() {
        try {
            super.initPane();

            optionsController.setValues(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        optionsController.checkEffectType();
        paletteAddButton.setVisible(false);
        htmlButton.setVisible(false);

    }

    @FXML
    @Override
    public void okAction() {
        quantizationColors = null;
        paletteAddButton.setVisible(false);
        htmlButton.setVisible(false);
        quanTable = null;
        optionsController.actualLoopLabel.setText("");
        if (imageController == null || optionsController.effectType == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private Image newImage;
            private String value = null;
            private ImageQuantization quantization;
            private int actualLoop = -1;

            @Override
            protected boolean handle() {
                try {
                    PixelsOperation pixelsOperation;
                    ImageConvolution imageConvolution;
                    switch (optionsController.effectType) {
                        case EdgeDetect:
                            if (optionsController.eightLaplaceRadio.isSelected()) {
                                optionsController.kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace();
                            } else if (optionsController.eightLaplaceExcludedRadio.isSelected()) {
                                optionsController.kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert();
                            } else if (optionsController.fourLaplaceRadio.isSelected()) {
                                optionsController.kernel = ConvolutionKernel.makeEdgeDetectionFourNeighborLaplace();
                            } else if (optionsController.fourLaplaceExcludedRadio.isSelected()) {
                                optionsController.kernel = ConvolutionKernel.makeEdgeDetectionFourNeighborLaplaceInvert();
                            } else {
                                return false;
                            }
                            optionsController.kernel.setGray(optionsController.valueCheck.isSelected());
                            imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setScope(scopeController.scope).
                                    setKernel(optionsController.kernel);
                            newImage = imageConvolution.operateFxImage();
                            break;
                        case Emboss:
                            optionsController.kernel = ConvolutionKernel.makeEmbossKernel(
                                    optionsController.intPara1, optionsController.intPara2, optionsController.valueCheck.isSelected());
                            imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setScope(scopeController.scope).
                                    setKernel(optionsController.kernel);
                            newImage = imageConvolution.operateFxImage();
                            break;
                        case Quantization:
                            quantization = ImageQuantizationFactory.create(imageView.getImage(),
                                    scopeController.scope, optionsController.quantizationAlgorithm,
                                    optionsController.quanColors, optionsController.regionSize,
                                    optionsController.weight1, optionsController.weight2, optionsController.weight3,
                                    true, optionsController.quanDitherCheck.isSelected(),
                                    optionsController.firstColorCheck.isSelected());
                            if (optionsController.quantizationAlgorithm == QuantizationAlgorithm.KMeansClustering) {
                                KMeansClusteringQuantization q = (KMeansClusteringQuantization) quantization;
                                q.getKmeans().setMaxIteration(optionsController.kmeansLoop);
                                newImage = q.operateFxImage();
                                actualLoop = q.getKmeans().getLoopCount();
                            } else {
                                newImage = quantization.operateFxImage();
                            }
                            value = optionsController.intPara1 + "";
                            break;
                        case Thresholding:
                            pixelsOperation = PixelsOperationFactory.create(imageView.getImage(),
                                    scopeController.scope, optionsController.effectType);
                            pixelsOperation.setIntPara1(optionsController.intPara1);
                            pixelsOperation.setIntPara2(optionsController.intPara2);
                            pixelsOperation.setIntPara3(optionsController.intPara3);
                            pixelsOperation.setIsDithering(false);
                            newImage = pixelsOperation.operateFxImage();
                            break;
                        case BlackOrWhite:
                            int threshold = optionsController.binaryController.threshold();
                            value = threshold + "";
                            ImageBinary imageBinary = new ImageBinary(imageView.getImage(), scopeController.scope, threshold);
                            imageBinary.setIsDithering(optionsController.binaryController.dither());
                            newImage = imageBinary.operateFxImage();
                            break;
                        case Gray:
                            ImageGray imageGray = new ImageGray(imageView.getImage(), scopeController.scope);
                            newImage = imageGray.operateFxImage();
                            break;
                        case Sepia:
                            pixelsOperation = PixelsOperationFactory.create(imageView.getImage(), scopeController.scope, optionsController.effectType);
                            pixelsOperation.setIntPara1(optionsController.intPara1);
                            newImage = pixelsOperation.operateFxImage();
                            value = optionsController.intPara1 + "";
                            break;
                        case Mosaic: {
                            ImageMosaic mosaic
                                    = ImageMosaic.create(imageView.getImage(), scopeController.scope, ImageMosaic.MosaicType.Mosaic, optionsController.intPara1);
                            newImage = mosaic.operateFxImage();
                            value = optionsController.intPara1 + "";
                        }
                        break;
                        case FrostedGlass: {
                            ImageMosaic mosaic
                                    = ImageMosaic.create(imageView.getImage(), scopeController.scope, ImageMosaic.MosaicType.FrostedGlass, optionsController.intPara1);
                            newImage = mosaic.operateFxImage();
                            value = optionsController.intPara1 + "";
                        }
                        break;
                        default:
                            return false;
                    }
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return newImage != null;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                imageController.popSuccessful();
                imageController.updateImage(ImageOperation.Effects, optionsController.effectType.name(), value, newImage, cost);
                if (quantization != null) {
                    String name = null;
                    if (imageController.sourceFile != null) {
                        name = imageController.sourceFile.getName();
                    }
                    quanTable = quantization.countTable(name);
                    if (quanTable != null) {
                        htmlButton.setVisible(true);
                        if (optionsController.quanDataCheck.isSelected()) {
                            htmlAction();
                        }
                    }
                    if (actualLoop >= 0) {
                        optionsController.actualLoopLabel.setText(message("ActualLoop") + ":" + actualLoop);
                    }
                    List<ImageQuantization.ColorCount> sortedCounts = quantization.getSortedCounts();
                    if (sortedCounts != null && !sortedCounts.isEmpty()) {
                        quantizationColors = new ArrayList<>();
                        for (int i = 0; i < sortedCounts.size(); ++i) {
                            ImageQuantization.ColorCount count = sortedCounts.get(i);
                            Color color = ColorConvertTools.converColor(count.color);
                            quantizationColors.add(color);
                        }
                        paletteAddButton.setVisible(true);
                    }
                }
            }
        };
        start(task);
    }

    @FXML
    public void htmlAction() {
        if (quanTable == null) {
            return;
        }
        HtmlTableController controller
                = (HtmlTableController) WindowTools.openStage(Fxmls.HtmlTableFxml);
        controller.loadTable(quanTable);
    }

    @FXML
    protected void demo() {
        if (imageView.getImage() == null) {
            return;
        }
        imageController.popInformation(message("WaitAndHandling"));
        demoButton.setDisable(true);
        Task demoTask = new Task<Void>() {
            private List<String> files;

            @Override
            protected Void call() {

                try {
                    files = new ArrayList<>();
                    BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                    image = ScaleTools.scaleImageLess(image, 1000000);

                    PixelsOperation pixelsOperation;
                    ImageConvolution imageConvolution;
                    ConvolutionKernel kernel;
                    BufferedImage bufferedImage;
                    String tmpFile;

                    BufferedImage outlineSource = SwingFXUtils.fromFXImage(
                            new Image("img/cover" + AppValues.AppYear + "g4.png"), null);
                    ImageScope scope = new ImageScope(SwingFXUtils.toFXImage(image, null));
                    scope.setScopeType(ImageScope.ScopeType.Outline);
                    if (sourceFile != null) {
                        scope.setFile(sourceFile.getAbsolutePath());
                    }
                    scope.setRectangle(new DoubleRectangle(0, 0, image.getWidth() - 1, image.getHeight() - 1));
                    BufferedImage[] outline = AlphaTools.outline(outlineSource,
                            scope.getRectangle(), image.getWidth(), image.getHeight(),
                            false, ColorConvertTools.converColor(Color.WHITE), false);
                    scope.setOutlineSource(outlineSource);
                    scope.setOutline(outline[1]);

                    ImageQuantization quantization
                            = ImageQuantizationFactory.create(image, scope,
                                    QuantizationAlgorithm.PopularityQuantization, 16, 256, 2, 4, 3, false, true, true);
                    bufferedImage = quantization.operateImage();
                    tmpFile = FileTmpTools.generateFile(message("Posterizing"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(
                            image, scope, OperationType.Thresholding);
                    pixelsOperation.setIntPara1(128);
                    pixelsOperation.setIntPara2(0);
                    pixelsOperation.setIntPara3(255);
                    pixelsOperation.setIsDithering(false);
                    bufferedImage = pixelsOperation.operateImage();
                    tmpFile = FileTmpTools.generateFile(message("Thresholding"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    ImageGray imageGray = new ImageGray(image, scope);
                    bufferedImage = imageGray.operate();
                    tmpFile = FileTmpTools.generateFile(message("Gray"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(
                            image, scope, OperationType.Sepia);
                    pixelsOperation.setIntPara1(60);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = FileTmpTools.generateFile(message("Sepia"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    ImageBinary imageBinary = new ImageBinary(imageView.getImage(), scope, -1);
                    imageBinary.setIsDithering(true);
                    bufferedImage = imageBinary.operate();
                    tmpFile = FileTmpTools.generateFile(message("BlackOrWhite"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace();
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setScope(scope).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = FileTmpTools.generateFile(message("EdgeDetection"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeEmbossKernel(Direction.Top, 3, true);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setScope(scope).setKernel(kernel);
                    bufferedImage = imageConvolution.operate();
                    tmpFile = FileTmpTools.generateFile(message("Emboss"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    ImageMosaic mosaic = ImageMosaic.create(
                            image, scope, ImageMosaic.MosaicType.Mosaic, 30);
                    bufferedImage = mosaic.operate();
                    tmpFile = FileTmpTools.generateFile(message("Mosaic"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    mosaic = ImageMosaic.create(image, scope,
                            ImageMosaic.MosaicType.FrostedGlass, 20);
                    bufferedImage = mosaic.operate();
                    tmpFile = FileTmpTools.generateFile(message("FrostedGlass"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                } catch (Exception e) {

                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                demoButton.setDisable(false);
                if (files.isEmpty()) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ImagesBrowserController controller
                                    = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
                            controller.loadFiles(files);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                });

            }

        };
        start(demoTask, false);
    }

    public void addColors() {
        ColorsManageController.addColors(quantizationColors);
    }

}
