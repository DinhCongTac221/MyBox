package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-05-31
 * @License Apache License Version 2.0
 */
public class FFmpegScreenRecorderController extends BaseTaskController {

    protected String os;
    protected Process process;
    protected SimpleBooleanProperty stopping;

    @FXML
    protected FFmpegScreenRecorderOptionsController optionsController;
    @FXML
    protected CheckBox openCheck;
    @FXML
    protected TextField commandInput;

    public FFmpegScreenRecorderController() {
        baseTitle = message("FFmpegScreenRecorder");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            stopping = new SimpleBooleanProperty(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Media);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            os = SystemTools.os();

            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Open", newValue);
                }
            });
            openCheck.setSelected(UserConfig.getBoolean(baseName + "Open", true));

            optionsController.extensionInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkExt();
                    });
            checkExt();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    targetFileController.valid.not()
                            .or(optionsController.executableInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(optionsController.titleInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(optionsController.xInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(optionsController.yInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(optionsController.widthInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(optionsController.heightInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(stopping)
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkExt() {
        String ext = optionsController.extensionInput.getText();
        if (ext == null || ext.isBlank() || message("OriginalFormat").equals(ext)) {
            return;
        }
        String v = targetFileController.text();
        if (v == null || v.isBlank()) {
            targetFileController.input(AppPaths.getGeneratedPath() + File.separator + DateTools.nowFileString() + "." + ext);
        } else if (!v.endsWith("." + ext)) {
            targetFileController.input(FileNameTools.replaceSuffix(v, ext));
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!optionsController.audioCheck.isSelected() && !optionsController.videoCheck.isSelected()) {
                popError(message("NothingHandled"));
                return false;
            }
            targetFile = targetFileController.file;
            if (targetFile == null) {
                popError(message("InvalidParameters"));
                return false;
            }
            targetFile.getParentFile().mkdirs();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        initLogs();
    }

    @Override
    public void startTask() {
        if (optionsController.delayController.value > 0) {
            showLogs(message("Delay") + ": " + optionsController.delayController.value + " " + message("Seconds"));
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    superStartTask();
                }
            }, optionsController.delayController.value * 1000);
        } else {
            superStartTask();
        }
    }

    public void superStartTask() {
        super.startTask();
    }

    @Override
    public boolean doTask() {
        try {
            if (optionsController.miaoCheck.isSelected()) {
                SoundTools.BenWu();
            }
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            List<String> parameters = optionsController.makeParameters(null);
            process = optionsController.startProcess(this, parameters, targetFile);
            if (process == null) {
                return false;
            }
            showLogs(message("Started"));
            if (optionsController.durationController.value > 0) {
                showLogs(message("Duration") + ": "
                        + optionsController.durationController.value + " " + message("Seconds"));
            }
            boolean started = false, recording;
            try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                String line;
                long start = new Date().getTime();
                while ((line = inReader.readLine()) != null) {
                    recording = line.contains(" bitrate=");
                    if (recording) {
                        started = true;
                        if ((timer == null) && (optionsController.durationController.value > 0)) {
                            timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    cancelAction();
                                }
                            }, optionsController.durationController.value * 1000);
                        }
                    }
                    if (verboseCheck.isSelected() || !recording) {
                        updateLogs(line + "\n");
                    }
                    if (!started && (new Date().getTime() - start > 15000)) {  // terminal process if too long blocking
                        process.destroyForcibly();
                        break;
                    }
                }
            }
            process.waitFor();
            if (process != null) {
                process.destroy();
                process = null;
            }
            stopping.set(false);
            if (optionsController.miaoCheck.isSelected()) {
                SoundTools.miao7();
            }
            showLogs(message("Exit"));
            if (started) {
                openTarget(null);
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showLogs(message("FFmpegScreenRecorderAbnormal"));
                        alertError(message("FFmpegScreenRecorderAbnormal"));
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return true;
    }

    @Override
    public void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        cancelAction();
    }

    @Override
    public void cancelAction() {
        if (process == null) {
            stopping.set(false);
            return;
        }
        if (stopping.get()) {
            return;
        }
        stopping.set(true);
        if (process != null) {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), Charset.forName("UTF-8")));) {
                writer.append('q');
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        }
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (targetFile != null && targetFile.exists()) {
                    recordFileOpened(targetFile);
                    if (openCheck.isSelected()) {
                        ControllerTools.openTarget(targetFile.getAbsolutePath());
                    } else {
                        browseURI(targetFile.getParentFile().toURI());
                    }
                } else {
                    popInformation(message("NoFileGenerated"));
                }
            }
        });
    }

    @Override
    public void cleanPane() {
        try {
            cancelTask();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
