package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.controller.base.PdfBatchController;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import static mara.mybox.value.AppVaribles.setUserConfigValue;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-16
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertImagesBatchController extends PdfBatchController {

    protected final String PdfConverterAppendColorKey, PdfConverterAppendCompressionKey,
            PdfConverterAppendQualityKey, PdfConverterAppendDensityKey;
    protected ImageAttributes attributes;
    protected PDFRenderer renderer;

    @FXML
    protected ImageConverterOptionsController optionsController;
    @FXML
    protected CheckBox appendColorCheck, appendCompressionCheck, appendQualityCheck, appendDensityCheck;

    public PdfConvertImagesBatchController() {
        baseTitle = AppVaribles.message("PdfConvertImagesBatch");
        browseTargets = true;

        PdfConverterAppendColorKey = "PdfConverterDitherKey";
        PdfConverterAppendCompressionKey = "PdfConverterAppendCompressionKey";
        PdfConverterAppendQualityKey = "PdfConverterAppendQualityKey";
        PdfConverterAppendDensityKey = "PdfConverterAppendDensityKey";
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            optionsController.initDpiBox(true);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.qualitySelector.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(optionsController.dpiSelector.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(optionsController.profileInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.thresholdInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initTargetSection() {
        try {
            super.initTargetSection();

            appendColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    setUserConfigValue(PdfConverterAppendColorKey, appendColorCheck.isSelected());
                }
            });
            appendColorCheck.setSelected(AppVaribles.getUserConfigBoolean(PdfConverterAppendColorKey));

            appendCompressionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    setUserConfigValue(PdfConverterAppendCompressionKey, appendCompressionCheck.isSelected());
                }
            });
            appendCompressionCheck.setSelected(AppVaribles.getUserConfigBoolean(PdfConverterAppendCompressionKey));

            appendQualityCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    setUserConfigValue(PdfConverterAppendQualityKey, appendQualityCheck.isSelected());
                }
            });
            appendQualityCheck.setSelected(AppVaribles.getUserConfigBoolean(PdfConverterAppendQualityKey));

            appendDensityCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    setUserConfigValue(PdfConverterAppendDensityKey, appendDensityCheck.isSelected());
                }
            });
            appendDensityCheck.setSelected(AppVaribles.getUserConfigBoolean(PdfConverterAppendDensityKey));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }

        attributes = optionsController.attributes;

        return true;
    }

    @Override
    public boolean preHandlePages() {
        try {
            renderer = new PDFRenderer(doc);
        } catch (Exception e) {
            logger.error(e.toString());
            renderer = null;
        }
        return renderer != null;
    }

    @Override
    public int handleCurrentPage() {
        try {
            BufferedImage pageImage = renderer.renderImageWithDPI(currentParameters.currentPage,
                    attributes.getDensity(), ImageType.ARGB);
            BufferedImage targetImage = ImageConvert.convertColorSpace(pageImage, attributes);
            if (targetImage == null) {
                return 0;
            }
            File tFile = makeTargetFile();
            if (!ImageFileWriters.writeImageFile(targetImage, attributes, tFile.getAbsolutePath())) {
                return 0;
            }
            actualParameters.finalTargetName = tFile.getAbsolutePath();
            targetFiles.add(tFile);
            return 1;
        } catch (Exception e) {
            logger.error(e.toString());
            return 0;
        }
    }

    public File makeTargetFile() {
        try {
            String namePrefix = FileTools.getFilePrefix(currentParameters.currentSourceFile.getName())
                    + "_page" + currentParameters.currentPage;
            if (appendColorCheck.isSelected()) {
                if (message("IccProfile").equals(attributes.getColorSpaceName())) {
                    namePrefix += "_" + attributes.getProfileName();
                } else {
                    namePrefix += "_" + attributes.getColorSpaceName();
                }
            }
            if (attributes.getCompressionType() != null) {
                if (appendCompressionCheck.isSelected()) {
                    namePrefix += "_" + attributes.getCompressionType();
                }
                if (appendQualityCheck.isSelected()) {
                    namePrefix += "_quality-" + attributes.getQuality() + "%";
                }
            }
            if (appendDensityCheck.isSelected()) {
                namePrefix += "_" + attributes.getDensity();
            }
            namePrefix = namePrefix.replace(" ", "_");
            String nameSuffix = "." + attributes.getImageFormat();

            return makeTargetFile(namePrefix, nameSuffix, currentParameters.currentTargetPath);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    @Override
    public void postHandlePages() {
        renderer = null;
    }

}
