package mara.mybox.controller;

import com.ibm.icu.text.MessageFormat;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-10-29
 * @License Apache License Version 2.0
 */
public class MarkdownToHtmlController extends BaseBatchFileController {

    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;
    protected MutableDataHolder htmlOptions;
    protected int indentSize = 4;

    @FXML
    protected ComboBox<String> emulationSelector, indentSelector, styleSelector;
    @FXML
    protected CheckBox trimCheck, appendCheck, discardCheck, linesCheck;
    @FXML
    protected TextField titleInput;

    public MarkdownToHtmlController() {
        baseTitle = AppVariables.message("MarkdownToHtml");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Markdown, VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            emulationSelector.getItems().addAll(Arrays.asList(
                    "GITHUB", "MARKDOWN", "GITHUB_DOC", "COMMONMARK", "KRAMDOWN", "PEGDOWN",
                    "FIXED_INDENT", "MULTI_MARKDOWN", "PEGDOWN_STRICT"
            ));
            emulationSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Emulation", "GITHUB"));
            emulationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "Emulation", newValue);
                }
            });

            indentSelector.getItems().addAll(Arrays.asList(
                    "4", "2", "0", "6", "8"
            ));
            indentSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Indent", "4"));
            indentSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            indentSize = v;
                            AppVariables.setUserConfigValue(baseName + "Indent", newValue);
                        }
                    } catch (Exception e) {
                    }
                }
            });

            trimCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Trim", false));
            trimCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Indent", trimCheck.isSelected());
                }
            });

            appendCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Append", false));
            appendCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Append", appendCheck.isSelected());
                }
            });

            discardCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Discard", false));
            discardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Discard", discardCheck.isSelected());
                }
            });

            linesCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Trim", false));
            linesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Trim", linesCheck.isSelected());
                }
            });

            List<String> styles = new ArrayList<>();
            for (HtmlTools.HtmlStyle style : HtmlTools.HtmlStyle.values()) {
                styles.add(message(style.name()));
            }
            styleSelector.getItems().addAll(styles);
            styleSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "HtmlStyle", message("Default")));
            styleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "HtmlStyle", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            htmlOptions = new MutableDataSet();
            htmlOptions.setFrom(ParserEmulationProfile.valueOf(emulationSelector.getValue()));
            htmlOptions.set(Parser.EXTENSIONS, Arrays.asList(
                    //                    AbbreviationExtension.create(),
                    //                    DefinitionExtension.create(),
                    //                    FootnoteExtension.create(),
                    //                    TypographicExtension.create(),
                    TablesExtension.create()
            ));
            htmlOptions.set(HtmlRenderer.INDENT_SIZE, indentSize)
                    //                    .set(HtmlRenderer.PERCENT_ENCODE_URLS, true)
                    //                    .set(TablesExtension.COLUMN_SPANS, false)
                    .set(TablesExtension.TRIM_CELL_WHITESPACE, trimCheck.isSelected())
                    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, discardCheck.isSelected())
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, appendCheck.isSelected());

            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }

        return super.makeMoreParameters();
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "md".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            Node document = htmlParser.parse(FileTools.readTexts(srcFile));
            String html = htmlRender.render(document);
            String style = AppVariables.getUserConfigValue(baseName + "HtmlStyle", message("Default"));
            html = HtmlTools.html(titleInput.getText(), style, html);

            FileTools.writeFile(target, html, Charset.forName("utf-8"));
            updateLogs(MessageFormat.format(message("ConvertSuccessfully"),
                    srcFile.getAbsolutePath(), target.getAbsolutePath()));
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
