package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-5-17
 * @License Apache License Version 2.0
 */
public class WordToHtmlController extends BaseBatchFileController {

    protected Charset charset;

    public WordToHtmlController() {
        baseTitle = AppVariables.message("WordToHtml");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Word, VisitHistory.FileType.Html);
        targetFileSuffix = "htm";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            charset = Charset.defaultCharset();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return AppVariables.message("Skip");
        }
        try {
            String html = MicrosoftDocumentTools.word2html(srcFile, charset);
            FileTools.writeFile(target, html, charset);
            targetFileGenerated(target);
            return message("Successful");
        } catch (Exception e) {
            updateLogs(e.toString());
            return e.toString();
        }
    }

}
