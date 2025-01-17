package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class RemotePathPutController extends BaseBatchFileController {

    protected RemotePathManageController manageController;
    protected String targetPathName;
    protected int permissions;

    @FXML
    protected TextField targetPathInput;
    @FXML
    protected Label hostLabel;
    @FXML
    protected CheckBox copyMtimeCheck, permissionCheck;
    @FXML
    protected TextField permissionInput;

    public RemotePathPutController() {
        baseTitle = message("RemotePathPut");
    }

    public void setParameters(RemotePathManageController manageController) {
        try {
            this.manageController = manageController;
            logsTextArea = manageController.logsTextArea;
            logsMaxChars = manageController.logsMaxChars;
            verboseCheck = manageController.verboseCheck;

            TreeItem<FileNode> item = manageController.filesTreeView.getSelectionModel().getSelectedItem();
            if (item == null) {
                item = manageController.filesTreeView.getRoot();
            }
            if (item != null && item.getValue() != null) {
                targetPathName = item.getValue().path(false);
                targetPathInput.setText(targetPathName);
                targetPathInput.selectEnd();
            }

            hostLabel.setText(message("Host") + ": " + manageController.remoteController.host());

            copyMtimeCheck.setSelected(UserConfig.getBoolean(baseName + "CopyMtime", true));
            copyMtimeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "CopyMtime", nv);
                }
            });

            permissionCheck.setSelected(UserConfig.getBoolean(baseName + "SetPermissions", false));
            permissionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "SetPermissions", nv);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        targetPathName = targetPathInput.getText();
        if (targetPathName == null || targetPathName.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("TargetPath"));
            return false;
        }
        permissions = -1;
        if (permissionCheck.isSelected()) {
            try {
                permissions = Integer.parseInt(permissionInput.getText(), 8);
                UserConfig.setString(baseName + "Permissions", permissionInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("Permissions"));
                return false;
            }
        }
        if (manageController.task != null) {
            manageController.task.cancel();
        }
        manageController.tabPane.getSelectionModel().select(manageController.logsTab);
        manageController.requestMouse();
        return super.makeMoreParameters();
    }

    @FXML
    @Override
    public void startAction() {
        targetFilesCount = 0;
        targetFiles = new LinkedHashMap<>();
        runTask();
    }

    @Override
    public void startTask() {
        super.startAction();
    }

    @Override
    public boolean beforeHandleFiles() {
        manageController.task = task;
        manageController.remoteController.task = task;
        return manageController.checkConnection()
                && checkDirectory(null, targetPathName);
    }

    @Override
    public String handleFile(File file) {
        try {
            if (task == null || task.isCancelled()) {
                return message("Canceled");
            }
            if (file == null || !file.isFile() || !match(file)) {
                return message("Skip" + ": " + file);
            }
            return handleFileWithName(file, targetPathName);
        } catch (Exception e) {
            return file + " " + e.toString();
        }
    }

    @Override
    public String handleFileWithName(File srcFile, String targetPath) {
        try {
            String targetName = makeTargetFilename(srcFile, targetPath);
            if (targetName == null) {
                return message("Skip");
            }
            targetName = manageController.remoteController.fixFilename(targetName);
            showLogs("put " + srcFile.getAbsolutePath() + " " + targetName);
            if (manageController.remoteController.put(srcFile, targetName,
                    copyMtimeCheck.isSelected(), permissions)) {
                showLogs(MessageFormat.format(message("FilesGenerated"), targetName));
                return message("Successful");
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            showLogs(e.toString());
            return null;
        }
    }

    @Override
    public String handleDirectory(File dir) {
        try {
            dirFilesNumber = dirFilesHandled = 0;
            String targetDir = targetPathName;
            if (createDirectories) {
                targetDir += "/" + dir.getName();
                if (!checkDirectory(dir, targetDir)) {
                    return message("Failed");
                }
            }
            handleDirectory(dir, targetDir);
            return MessageFormat.format(message("DirHandledSummary"), dirFilesNumber, dirFilesHandled);
        } catch (Exception e) {
            showLogs(e.toString());
            return message("Failed");
        }
    }

    @Override
    public boolean checkDirectory(File srcFile, String pathname) {
        try {
            if (pathname == null) {
                return false;
            }
            return manageController.remoteController.mkdirs(pathname,
                    copyMtimeCheck.isSelected() && srcFile != null ? (int) (srcFile.lastModified() / 1000) : -1,
                    permissions);
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    protected void taskCanceled() {
        super.taskCanceled();
        if (manageController != null) {
            manageController.disconnect();
        }
    }

    @Override
    public void afterTask() {
        tableView.refresh();
        if (miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
        if (manageController != null) {
            manageController.loadPath();
        }
    }

    /*
        static methods
     */
    public static RemotePathPutController open(RemotePathManageController manageController) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathPutController controller = (RemotePathPutController) WindowTools.openChildStage(
                    manageController.getMyWindow(), Fxmls.RemotePathPutFxml, false);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
