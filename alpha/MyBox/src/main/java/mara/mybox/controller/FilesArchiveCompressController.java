package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

/**
 * @Author Mara
 * @CreateDate 2019-11-2
 * @License Apache License Version 2.0
 */
// http://commons.apache.org/proper/commons-compress/examples.html
public class FilesArchiveCompressController extends BaseBatchFileController {

    protected String archiver, compressor, rootName, extension;
    protected ArchiveOutputStream archiveOut;
    protected SevenZOutputFile sevenZOutput;
    protected SevenZMethod sevenCompress;
    protected File archiveFile;
    protected List<ArchiveEntry> archive;
    protected long totalSize;

    @FXML
    protected ToggleGroup archiverGroup, compressGroup, sevenCompressGroup;
    @FXML
    protected TextField rootInput;
    @FXML
    protected ComboBox<String> encodeBox;
    @FXML
    protected VBox archiveVBox, compressVBox;
    @FXML
    protected Label archiverLabel;
    @FXML
    protected FlowPane sevenZCompressPane, commonCompressPane;
    @FXML
    protected RadioButton pack200Radio, gzRadio;
    @FXML
    protected CheckBox resultCheck;

    public FilesArchiveCompressController() {
        baseTitle = message("FilesArchiveCompress");
    }

    @Override
    public void initOptionsSection() {
        try {
            archiverGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldv, Toggle newv) {
                    checkArchiver();
                }
            });
            checkArchiver();

            sevenCompressGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldv, Toggle newv) {
                    checkSevenCompress();
                }
            });
            checkSevenCompress();

            compressGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldv, Toggle newv) {
                    checkCompressor();
                }
            });
            checkCompressor();

            rootName = "";
            rootInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    rootName = rootInput.getText().trim();
                }
            });

            List<String> setNames = TextTools.getCharsetNames();
            encodeBox.getItems().addAll(setNames);
            encodeBox.getSelectionModel().select(UserConfig.getString("FilesUnarchiveEncoding", Charset.defaultCharset().name()));

        } catch (Exception e) {

        }

    }

    protected void checkArchiver() {
        archiver = ((RadioButton) archiverGroup.getSelectedToggle()).getText();
        archiverLabel.setText("");
        sevenZCompressPane.setVisible(archiver.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z));
        if (archiver.equalsIgnoreCase(ArchiveStreamFactory.AR)) {
            archiverLabel.setText(message("ARArchivesLimitation"));
        }
        pack200Radio.setDisable(!archiver.equalsIgnoreCase(ArchiveStreamFactory.ZIP)
                && !archiver.equalsIgnoreCase(ArchiveStreamFactory.JAR));
        if (pack200Radio.isDisabled() && pack200Radio.isSelected()) {
            gzRadio.setSelected(true);
        }
        checkExtension();
    }

    protected void checkSevenCompress() {
        String selected = ((RadioButton) sevenCompressGroup.getSelectedToggle()).getText();
        switch (selected) {
            case "LZMA2":
                sevenCompress = SevenZMethod.LZMA2;
                break;
            case "COPY":
                sevenCompress = SevenZMethod.COPY;
                break;
            case "DEFLATE":
                sevenCompress = SevenZMethod.DEFLATE;
                break;
            case "BZIP2":
                sevenCompress = SevenZMethod.BZIP2;
                break;
        }
    }

    protected void checkCompressor() {
        compressor = ((RadioButton) compressGroup.getSelectedToggle()).getText();
        checkExtension();
    }

    protected void checkExtension() {
        extension = archiver;
        if (compressor != null && !message("None").equals(compressor)) {
            switch (compressor) {
                case "bzip2":
                    extension = archiver + ".bz2";
                    break;
                case "pack200":
                    extension = archiver + ".pack";
                    break;
                case "lz4-block":
                case "lz4-framed":
                    extension = archiver + ".lz4";
                    break;
                case "snappy-framed":
                    extension = archiver + ".sz";
                    break;
                default:
                    extension = archiver + "." + compressor;
                    break;
            }

        }

        if (targetFile == null) {
            return;
        }
        String name = targetFile.getName();
        int pos = name.indexOf('.');
        if (pos >= 0) {
            name = name.substring(0, pos);
        }
        targetFileController.input(targetFile.getParent() + File.separator + name + "." + extension);
    }

    @Override
    public void initTargetSection() {
        super.initTargetSection();

        targetFileController.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                try {
                    targetFile = targetFileController.file;
                    if (rootInput.getText().trim().isEmpty()) {
                        String name = targetFile.getName();
                        int pos = name.indexOf('.');
                        if (pos >= 0) {
                            name = name.substring(0, pos);
                        }
                        rootInput.setText(name);
                    }
                } catch (Exception e) {
                }
            }
        });

        openTargetButton.disableProperty().unbind();
        openTargetButton.disableProperty().bind(targetFileController.valid.not());

        startButton.disableProperty().unbind();
        startButton.disableProperty().bind(targetFileController.valid.not()
                .or(Bindings.isEmpty(tableData))
        );

    }

    @Override
    public void disableControls(boolean disable) {
        super.disableControls(disable);
        archiveVBox.setDisable(disable);
        compressVBox.setDisable(disable);
    }

    @Override
    public boolean beforeHandleFiles() {
        try {
            targetFile = makeTargetFile(FileNameTools.prefix(targetFile.getName()),
                    "." + FileNameTools.suffix(targetFile.getName()),
                    targetFile.getParentFile());
            if (targetFile == null) {
                return false;
            }
            archiveFile = TmpFileTools.getTempFile();
            if (archiver.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z)) {
                sevenZOutput = new SevenZOutputFile(archiveFile);
                sevenZOutput.setContentCompression(sevenCompress);
            } else {
                ArchiveStreamFactory f = new ArchiveStreamFactory(
                        encodeBox.getSelectionModel().getSelectedItem());
                archiveOut = f.createArchiveOutputStream(
                        archiver, new FileOutputStream(archiveFile));
            }
            if (resultCheck.isSelected()) {
                archive = new ArrayList();
            } else {
                archive = null;
            }
            totalSize = 0;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @Override
    public String handleFile(File file) {
        try {
            if (!match(file)) {
                return message("Skip");
            }
            return addEntry(file, rootName);
        } catch (Exception e) {
            return message("Failed");
        }
    }

    public String addEntry(File file, String entryPath) {
        try {
            String name;
            if (archiver.equalsIgnoreCase(ArchiveStreamFactory.AR)) {
                name = file.getName();
                if (name.length() > 16) {
                    return message("Skip");
                }
            } else if (entryPath == null || entryPath.trim().isEmpty()) {
                name = file.getName();
            } else {
                name = entryPath + "/" + file.getName();
            }
            if (archiver.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z)) {
                SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(file, name);
                sevenZOutput.putArchiveEntry(entry);
                if (file.isFile()) {
                    try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                        int len;
                        byte[] buf = new byte[AppValues.IOBufferLength];
                        while ((len = inputStream.read(buf)) > 0) {
                            sevenZOutput.write(buf, 0, len);
                        }
                    }
                    totalSize += file.length();
                }
                sevenZOutput.closeArchiveEntry();
                if (archive != null) {
                    archive.add(entry);

                }
            } else {
                ArchiveEntry entry = archiveOut.createArchiveEntry(file, name);
                archiveOut.putArchiveEntry(entry);
                if (file.isFile()) {
                    try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                        IOUtils.copy(inputStream, archiveOut);
                    }
                    totalSize += file.length();
                }
                archiveOut.closeArchiveEntry();
                if (archive != null) {
                    archive.add(entry);
                }
            }
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return message("Failed");
        }

    }

    @Override
    public String handleDirectory(File dir) {
        try {
            if (archiver.equalsIgnoreCase(ArchiveStreamFactory.AR)) {
                return message("Skip");
            }
            dirFilesNumber = dirFilesHandled = 0;
            addEntry(dir, rootName);
            if (rootName == null || rootName.trim().isEmpty()) {
                handleDirectory(dir, dir.getName());
            } else {
                handleDirectory(dir, rootName + "/" + dir.getName());
            }
            return MessageFormat.format(message("DirHandledSummary"),
                    dirFilesNumber, dirFilesHandled);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return message("Failed");
        }
    }

    @Override
    protected boolean handleDirectory(File sourcePath, String entryPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()
                || (isPreview && dirFilesHandled > 0)) {
            return false;
        }
        try {
            File[] files = sourcePath.listFiles();
            if (files == null) {
                return false;
            }
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                if (srcFile.isFile()) {
                    dirFilesNumber++;
                    if (isPreview && dirFilesHandled > 0) {
                        return false;
                    }
                    if (!match(srcFile)) {
                        continue;
                    }
                    String result = addEntry(srcFile, entryPath);
                    if (!message("Failed").equals(result)
                            && !message("Skip").equals(result)) {
                        dirFilesHandled++;
                    }
                } else if (srcFile.isDirectory() && sourceCheckSubdir) {
                    handleDirectory(srcFile, entryPath + "/" + srcFile.getName());
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void afterHandleFiles() {
        try {
            if (archiver.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z)) {
                sevenZOutput.finish();
                sevenZOutput.close();
            } else {
                archiveOut.finish();
                archiveOut.close();
            }
            if (targetFile.exists()) {
                FileDeleteTools.delete(targetFile);
            }
            if (!message("None").equals(compressor)) {
                File tmpFile = TmpFileTools.getTempFile();
                try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(archiveFile));
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
                        CompressorOutputStream compressOut = new CompressorStreamFactory().
                                createCompressorOutputStream(compressor, out)) {
                    if (inputStream != null) {
                        IOUtils.copy(inputStream, compressOut);
                    }
                }
                FileTools.rename(tmpFile, targetFile);
            } else {
                FileTools.rename(archiveFile, targetFile);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void donePost() {
        tableView.refresh();
        if (miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
        if (openCheck.isSelected()) {
            File path = targetFile.getParentFile();
            browseURI(path.toURI());
            recordFileOpened(path);
        }

        if (archive == null) {
            return;
        }

        StringBuilder s = new StringBuilder();
        s.append("<h1  class=\"center\">").append(targetFile).append("</h1>\n");
        s.append("<hr>\n");
        int ratio;
        if (totalSize > 0) {
            ratio = (int) (100 - targetFile.length() * 100 / totalSize);
        } else {
            ratio = 0;
        }
        String compressInfo = message("TotalSize") + ":"
                + FileTools.showFileSize(totalSize) + "&nbsp;&nbsp;&nbsp;"
                + message("SizeAfterArchivedCompressed") + ":"
                + FileTools.showFileSize(targetFile.length()) + "&nbsp;&nbsp;&nbsp;"
                + message("CompressedRatio") + ":" + ratio + "%";
        s.append("<P>").append(compressInfo).append("</P>\n");

        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(message("ID"),
                message("Directory"), message("File"),
                message("Size"), message("ModifiedTime")
        ));
        StringTable table = new StringTable(names, message("ArchiveContents"));
        int id = 1;
        String dir, file, size;
        for (ArchiveEntry entry : archive) {
            List<String> row = new ArrayList<>();
            if (entry.isDirectory()) {
                dir = entry.getName();
                file = "";
            } else {
                int pos = entry.getName().lastIndexOf('/');
                if (pos < 0) {
                    dir = "";
                    file = entry.getName();
                } else {
                    dir = entry.getName().substring(0, pos);
                    file = entry.getName().substring(pos + 1, entry.getName().length());
                }
            }
            if (entry.getSize() > 0) {
                size = FileTools.showFileSize(entry.getSize());
            } else {
                size = "";
            }
            row.addAll(Arrays.asList((id++) + "", dir, file, size,
                    DateTools.datetimeToString(entry.getLastModifiedDate())
            ));
            table.add(row);
        }
        s.append(StringTable.tableDiv(table));

        HtmlWriteTools.editHtml(message("ArchiveContents"), s.toString());
    }

}
