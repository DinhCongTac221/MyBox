package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.data.FileInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;

import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-10-21
 * @License Apache License Version 2.0
 */
public class HtmlMergeAsHtmlController extends BaseBatchFileController {

    protected FileWriter writer;

    @FXML
    protected TextArea headArea;
    @FXML
    protected TextField titleInput;
    @FXML
    protected CheckBox deleteCheck;
    @FXML
    protected ControlFileSelecter targetFileController;

    public HtmlMergeAsHtmlController() {
        baseTitle = Languages.message("HtmlMergeAsHtml");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            targetFileController.label(Languages.message("TargetFile"))
                    .isDirectory(false).isSource(false).mustExist(false).permitNull(false)
                    .defaultValue("_" + Languages.message("Merge"))
                    .name(baseName + "TargetFile", false).type(VisitHistory.FileType.Html);
            targetFileInput = targetFileController.fileInput;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            String head
                    = "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
                    + "        <title>####title####</title>\n"
                    + "    </head>";
            headArea.setText(head);

            targetFileInput.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        String prefix = FileNameTools.namePrefix(newValue);
                        if (prefix != null) {
                            titleInput.setText(prefix);
                        }
                    });
//            titleInput.textProperty().addListener(
//                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
//                        if (newValue == null) {
//                            return;
//                        }
//                        headArea.setText(headArea.getText().replace("####title####", newValue));
//                    });

            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public boolean makeMoreParameters() {
        try {
            targetFile = targetFileController.file;
            if (targetFile == null) {
                return false;
            }
            writer = new FileWriter(targetFile, Charset.forName("utf-8"));
            writer.write("<!DOCTYPE html><html>\n"
                    + headArea.getText().replace("####title####", titleInput.getText()) + "\n"
                    + "    <body>\n");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileNameTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "html".equals(suffix) || "htm".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            String html = TextFileTools.readTexts(srcFile);
            String body = HtmlReadTools.body(html, false);
            writer.write(body + "\n");
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
        }
    }

    @Override
    public void afterHandleFiles() {
        try {
            writer.write("    </body>\n</html>\n");
            writer.flush();
            writer.close();
            targetFileGenerated(targetFile);
            if (deleteCheck.isSelected()) {
                List<FileInformation> sources = new ArrayList<>();
                sources.addAll(tableData);
                for (int i = sources.size() - 1; i >= 0; --i) {
                    try {
                        FileInformation source = sources.get(i);
                        FileDeleteTools.delete(source.getFile());
                        tableData.remove(i);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
