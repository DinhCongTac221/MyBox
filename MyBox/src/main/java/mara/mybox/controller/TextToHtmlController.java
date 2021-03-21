package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mara.mybox.data.FindReplaceString;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2020-10-24
 * @License Apache License Version 2.0
 */
public class TextToHtmlController extends BaseBatchFileController {

    @FXML
    protected TextArea headArea;

    public TextToHtmlController() {
        baseTitle = AppVariables.message("TextToHtml");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text, VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            String head
                    = "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
                    + "         <style type=\"text/css\">\n"
                    + "		  .article {width: 900px; margin:0 auto;font-size:20px;font-weight:normal;}\n"
                    + "        </style>\n"
                    + "        <title>####title####</title>\n"
                    + "    </head>";
            headArea.setText(head);

            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            String text = FileTools.readTexts(srcFile);
            String body = "" + FindReplaceString.replaceAll(text, "\n", "</br>");
            String filePrefix = FileTools.getFilePrefix(target);
            String html = "<!DOCTYPE html><html>\n"
                    + headArea.getText().replace("####title####", filePrefix) + "\n"
                    + "    <body class=\"article\">\n"
                    + body
                    + "    </body>\n"
                    + "</html>\n";
            FileTools.writeFile(target, html, Charset.forName("utf-8"));
            targetFileGenerated(target);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(sourceFile.getName());
            String nameSuffix = "";
            if (sourceFile.isFile()) {
                nameSuffix = ".html";
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

}
