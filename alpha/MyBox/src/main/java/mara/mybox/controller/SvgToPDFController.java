package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SvgTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-17
 * @License Apache License Version 2.0
 */
public class SvgToPDFController extends BaseBatchFileController {

    @FXML
    protected ControlXmlOptions xmlOptionsController;
    @FXML
    protected ControlSvgTranscode svgOptionsController;

    public SvgToPDFController() {
        baseTitle = message("SvgToPDF");
        targetFileSuffix = "pdf";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.SVG, VisitHistory.FileType.PDF);
    }

    @Override
    public boolean makeActualParameters() {
        svgOptionsController.pickValues();
        return super.makeActualParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return message("Skip");
        }
        File tmpFile = SvgTools.fileToPDF(this, srcFile,
                svgOptionsController.width,
                svgOptionsController.height,
                svgOptionsController.area);
        if (tmpFile == null || !tmpFile.exists()) {
            return message("Failed");
        }

        if (FileTools.rename(tmpFile, target, true)) {
            targetFileGenerated(target);
            return message("Successful");
        } else {
            return message("Failed");
        }
    }

}
