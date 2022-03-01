package mara.mybox.data;

import java.io.File;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-6-23 6:44:22
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FileInformation {

    protected File file;
    protected long tableIndex, fileSize = -1, createTime, modifyTime, filesNumber = 0;
    protected String fileName, data, fileSuffix, handled, fileType;
    protected BooleanProperty selectedProperty;
    protected boolean selected;
    protected long sizeWithSubdir = -1, sizeWithoutSubdir = -1,
            filesWithSubdir = -1, filesWithoutSubdir = -1;
    protected long duration = -1;  // milliseconds

    public enum FileSelectorType {
        All, ExtensionEuqalAny, ExtensionNotEqualAny,
        NameIncludeAny, NameIncludeAll, NameNotIncludeAny, NameNotIncludeAll,
        NameMatchRegularExpression, NameNotMatchRegularExpression,
        NameIncludeRegularExpression, NameNotIncludeRegularExpression,
        FileSizeLargerThan, FileSizeSmallerThan, ModifiedTimeEarlierThan,
        ModifiedTimeLaterThan
    }

    public FileInformation() {
        this.selectedProperty = new SimpleBooleanProperty(false);
    }

    public FileInformation(File file) {
        setFileAttributes(file);
    }

    public final void setFileAttributes(File file) {
        this.file = file;
        this.selectedProperty = new SimpleBooleanProperty(false);
        if (duration < 0) {
            duration = 3000;
        }
        if (file == null) {
            return;
        }
        this.handled = "";
        this.fileName = file.getAbsolutePath();
        this.data = "";
        this.fileSuffix = "";
        if (!file.exists()) {
            this.fileType = Languages.message("NotExist");
            this.fileSuffix = this.fileType;
            return;
        }
        if (file.isFile()) {
            this.filesNumber = 1;
            this.fileSize = file.length();
            this.fileType = Languages.message("File");
            this.fileSuffix = FileNameTools.suffix(file.getName());
            if (this.fileSuffix == null || this.fileSuffix.isEmpty()) {
                this.fileSuffix = Languages.message("Unknown");
            }
        } else if (file.isDirectory()) {
//            long[] size = FileTools.countDirectorySize(file);
//            this.filesNumber = size[0];
//            this.fileSize = size[1];
            this.filesNumber = -1;
            this.fileSize = -1;
            sizeWithSubdir = sizeWithoutSubdir = -1;
            filesWithSubdir = filesWithoutSubdir = -1;
            this.fileType = Languages.message("Directory");
            this.fileSuffix = this.fileType;
        } else {
            this.fileSuffix = Languages.message("Others");
        }
        this.createTime = FileTools.createTime(fileName);
        this.modifyTime = file.lastModified();
    }

    public void setDirectorySize(boolean countSubdir) {
        if (file == null || !file.isDirectory()) {
            return;
        }
        if (countSubdir) {
            fileSize = sizeWithSubdir;
            filesNumber = filesWithSubdir;
        } else {
            fileSize = sizeWithoutSubdir;
            filesNumber = filesWithoutSubdir;
        }
    }

    public void countDirectorySize(Task task, boolean countSubdir) {
        if (file == null || !file.isDirectory()) {
            return;
        }
        if (countSubdir) {
            if (sizeWithSubdir < 0 || filesWithSubdir < 0) {
                long[] size = FileTools.countDirectorySize(file, countSubdir);
                if (task == null || task.isCancelled()) {
                    return;
                }
                filesWithSubdir = size[0];
                sizeWithSubdir = size[1];
            }
            fileSize = sizeWithSubdir;
            filesNumber = filesWithSubdir;
        } else {
            if (sizeWithoutSubdir < 0 || filesWithoutSubdir < 0) {
                long[] size = FileTools.countDirectorySize(file, countSubdir);
                if (task == null || task.isCancelled()) {
                    return;
                }
                filesWithoutSubdir = size[0];
                sizeWithoutSubdir = size[1];
            }
            fileSize = sizeWithoutSubdir;
            filesNumber = filesWithoutSubdir;
        }
    }

    /*
        customized get/set
     */
    public String getFileName() {
        if (fileName == null && file != null) {
            fileName = file.getAbsolutePath();
        }
        return fileName;
    }

    public File getFile() {
        if (file == null && fileName != null) {
            file = new File(fileName);
        }
        return file;
    }


    /*
        get/set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String getHandled() {
        return handled;
    }

    public void setHandled(String handled) {
        this.handled = handled;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFile(File file) {
        setFileAttributes(file);
    }

    public long getFilesNumber() {
        return filesNumber;
    }

    public void setFilesNumber(long filesNumber) {
        this.filesNumber = filesNumber;
    }

    public BooleanProperty getSelectedProperty() {
        if (selectedProperty == null) {
            selectedProperty = new SimpleBooleanProperty(false);
        }
        return selectedProperty;
    }

    public void setSelectedProperty(BooleanProperty selectedProperty) {
        if (selectedProperty == null) {
            this.selectedProperty = new SimpleBooleanProperty(false);
        } else {
            this.selectedProperty = selectedProperty;
        }
    }

    public boolean isSelected() {
        return selectedProperty.get();
    }

    public void setSelected(boolean selected) {
        selectedProperty.set(selected);
    }

    public long getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(long tableIndex) {
        this.tableIndex = tableIndex;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

}
