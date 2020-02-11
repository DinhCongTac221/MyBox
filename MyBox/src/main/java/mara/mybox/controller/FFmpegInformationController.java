package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.TableBooleanCell;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-12-4
 * @License Apache License Version 2.0
 */
public class FFmpegInformationController extends FFmpegBaseController {

    protected ObservableList<FFmpegFormat> formatsData;
    protected ObservableList<FFmpegCodec> codecsData;
    protected ObservableList<FFmpegFilter> filtersData;
    protected SingletonTask formatsTask, codecsTask, filtersTask, queryTask;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab formatsTab;
    @FXML
    protected TableView<FFmpegFormat> formatsView;
    @FXML
    protected TableColumn<FFmpegFormat, String> formatColumn, formatDescColumn;
    @FXML
    protected TableColumn<FFmpegFormat, Boolean> muxColumn, demuxColumn;
    @FXML
    protected Label fromatsLabel, codecLabel, filtersLabel;
    @FXML
    protected TextArea versionArea, queryArea;
    @FXML
    protected TableView<FFmpegCodec> codecsView;
    @FXML
    protected TableColumn<FFmpegCodec, String> codecColumn, codecTypeColumn, codecDescColumn;
    @FXML
    protected TableColumn<FFmpegCodec, Boolean> codecDecodeColumn, codecEncodeColumn,
            codecLossyColumn, codecLosslessColumn, codecFrameColumn;
    @FXML
    protected TableView<FFmpegFilter> filtersView;
    @FXML
    protected TableColumn<FFmpegFilter, String> filterColumn, filterDirectionColumn, filterDescColumn;
    @FXML
    protected TableColumn<FFmpegFilter, Boolean> filterTimelineColumn, filterSliceColumn,
            filterCommandColumn;
    @FXML
    protected TextField queryInput;
    @FXML
    protected ComboBox<String> querySelector;

    public FFmpegInformationController() {
        baseTitle = AppVariables.message("MediaInformation");

    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            executableName = "FFmpegExecutable";
            executableDefault = "D:\\Programs\\ffmpeg\\bin\\ffmpeg.exe";

            formatsData = FXCollections.observableArrayList();
            codecsData = FXCollections.observableArrayList();
            filtersData = FXCollections.observableArrayList();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            initColumns();

            querySelector.getItems().addAll(Arrays.asList(
                    message("BasicOptions"), message("MoreOptions"), message("AllOptions"),
                    message("Licence"), message("Protocols"), message("Devices"),
                    message("Formats"), message("Muxers"), message("Demuxers"),
                    message("Codecs"), message("Decoders"), message("Encoders"),
                    message("BitStreamFilters"), message("ChannelLayouts"), message("AudioSampleFormats"),
                    message("ColorNames"), message("HardwareAccelerationMethods")
            ));
            querySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkQuery();
                }
            });
            querySelector.getSelectionModel().select(0);

            queryArea.setStyle("-fx-font-family: monospace");

            startButton.disableProperty().bind(executableInput.styleProperty().isEqualTo(badStyle));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkQuery() {
        String selected = querySelector.getSelectionModel().getSelectedItem();
        if (message("BasicOptions").equals(selected)) {
            queryInput.setText("-h");
        } else if (message("MoreOptions").equals(selected)) {
            queryInput.setText("-h long");
        } else if (message("AllOptions").equals(selected)) {
            queryInput.setText("-h full");
        } else if (message("Licence").equals(selected)) {
            queryInput.setText("-L");
        } else if (message("Protocols").equals(selected)) {
            queryInput.setText("-protocols");
        } else if (message("Devices").equals(selected)) {
            queryInput.setText("-devices");
        } else if (message("Formats").equals(selected)) {
            queryInput.setText("-formats");
        } else if (message("Muxers").equals(selected)) {
            queryInput.setText("-muxers");
        } else if (message("Demuxers").equals(selected)) {
            queryInput.setText("-demuxers");
        } else if (message("Codecs").equals(selected)) {
            queryInput.setText("-codecs");
        } else if (message("Decoders").equals(selected)) {
            queryInput.setText("-decoders");
        } else if (message("Encoders").equals(selected)) {
            queryInput.setText("-encoders");
        } else if (message("BitStreamFilters").equals(selected)) {
            queryInput.setText("-bsfs");
        } else if (message("ChannelLayouts").equals(selected)) {
            queryInput.setText("-layouts");
        } else if (message("AudioSampleFormats").equals(selected)) {
            queryInput.setText("-sample_fmts");
        } else if (message("ColorNames").equals(selected)) {
            queryInput.setText("-colors");
        } else if (message("HardwareAccelerationMethods").equals(selected)) {
            queryInput.setText("-hwaccels");
        }

    }

    protected void initColumns() {
        try {
            muxColumn.setCellValueFactory(new PropertyValueFactory<>("mux"));
            muxColumn.setCellFactory(new TableBooleanCell());
            demuxColumn.setCellValueFactory(new PropertyValueFactory<>("demux"));
            demuxColumn.setCellFactory(new TableBooleanCell());
            formatColumn.setCellValueFactory(new PropertyValueFactory<>("format"));
            formatDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            codecColumn.setCellValueFactory(new PropertyValueFactory<>("codec"));
            codecTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            codecDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            codecDecodeColumn.setCellValueFactory(new PropertyValueFactory<>("decode"));
            codecDecodeColumn.setCellFactory(new TableBooleanCell());
            codecEncodeColumn.setCellValueFactory(new PropertyValueFactory<>("encode"));
            codecEncodeColumn.setCellFactory(new TableBooleanCell());
            codecLossyColumn.setCellValueFactory(new PropertyValueFactory<>("lossyCompress"));
            codecLossyColumn.setCellFactory(new TableBooleanCell());
            codecLosslessColumn.setCellValueFactory(new PropertyValueFactory<>("losslessCompress"));
            codecLosslessColumn.setCellFactory(new TableBooleanCell());
            codecFrameColumn.setCellValueFactory(new PropertyValueFactory<>("frame"));
            codecFrameColumn.setCellFactory(new TableBooleanCell());

            filterColumn.setCellValueFactory(new PropertyValueFactory<>("filter"));
            filterDirectionColumn.setCellValueFactory(new PropertyValueFactory<>("direction"));
            filterDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            filterTimelineColumn.setCellValueFactory(new PropertyValueFactory<>("timeline"));
            filterTimelineColumn.setCellFactory(new TableBooleanCell());
            filterSliceColumn.setCellValueFactory(new PropertyValueFactory<>("slice"));
            filterSliceColumn.setCellFactory(new TableBooleanCell());
            filterCommandColumn.setCellValueFactory(new PropertyValueFactory<>("command"));
            filterCommandColumn.setCellFactory(new TableBooleanCell());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (executable == null) {
            return;
        }
        readFormats();
        readFilters();
    }

    public void readFormats() {
        formatsData.clear();
        fromatsLabel.setText("");
        versionArea.clear();
        if (executable == null) {
            return;
        }
        synchronized (this) {
            if (formatsTask != null) {
                return;
            }
            formatsTask = new SingletonTask<Void>() {
                private StringBuilder version;

                @Override
                protected boolean handle() {
                    try {
                        error = null;
                        List<String> command = new ArrayList<>();
                        command.add(executable.getAbsolutePath());
                        command.add("-formats");
                        ProcessBuilder pb = new ProcessBuilder(command)
                                .redirectErrorStream(true);
                        pb.redirectErrorStream(true);
                        final Process process = pb.start();

                        try ( BufferedReader inReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {
                            String line;
                            int count = 0;
                            boolean versionEnd = false;
                            version = new StringBuilder();
                            while ((line = inReader.readLine()) != null) {
                                if (line.contains("File formats:")) {
                                    versionEnd = true;
                                }
                                if (!versionEnd) {
                                    version.append(line).append("\n");
                                    continue;
                                }
                                count++;
                                if (count < 5 || line.length() < 21) {
                                    continue;
                                }
                                String type = line.substring(0, 4);
                                boolean mux = type.contains("E");
                                boolean demux = type.contains("D");
                                String[] v = StringTools.separatedBySpace(line.substring(4));;
                                String name = v[0];
                                String desc = v[1];
                                FFmpegFormat f = FFmpegFormat.create().
                                        setMux(mux).setDemux(demux)
                                        .setFormat(name).setDescription(desc);
                                formatsData.add(f);
                            }
                        } catch (Exception e) {
                            error = e.toString();
                        }

                        process.waitFor();

                    } catch (Exception e) {
                        error = e.toString();;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (error != null) {
                        popError(error);
                    }
                    if (version != null) {
                        versionArea.setText(version.toString());
                    }
                    formatsView.setItems(formatsData);
                    fromatsLabel.setText(AppVariables.message("Total") + ": " + formatsData.size());
                    readCodecs();

                }

                protected void taskQuit() {
                    formatsTask = null;
                }
            };

            openHandlingStage(formatsTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(formatsTask);
            thread.setDaemon(true);
            thread.start();

        }
    }

    public void readCodecs() {
        codecsData.clear();
        codecLabel.setText("");
        if (executable == null) {
            return;
        }
        synchronized (this) {
            if (codecsTask != null) {
                return;
            }
            codecsTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        error = null;
                        List<String> command = new ArrayList<>();
                        command.add(executable.getAbsolutePath());
                        command.add("-hide_banner");
                        command.add("-codecs");
                        ProcessBuilder pb = new ProcessBuilder(command)
                                .redirectErrorStream(true);
                        pb.redirectErrorStream(true);
                        final Process process = pb.start();
                        try ( BufferedReader inReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {
                            String line;
                            int count = 0;
                            while ((line = inReader.readLine()) != null) {
                                count++;
                                if (count < 10 || line.length() < 30) {
                                    continue;
                                }
                                String flags = line.substring(0, 8);
                                boolean decode = flags.contains("D");
                                boolean encode = flags.contains("E");
                                boolean frame = flags.contains("I");
                                boolean lossy = flags.contains("L");
                                boolean lossless = flags.contains("S");
                                String type = "";
                                if (flags.contains("V")) {
                                    type = AppVariables.message("Video");
                                } else if (flags.contains("A")) {
                                    type = AppVariables.message("Audio");
                                } else if (flags.contains("S")) {
                                    type = AppVariables.message("Subtitle");
                                }
                                String[] v = StringTools.separatedBySpace(line.substring(8));;
                                String codec = v[0];
                                String desc = v[1];
                                FFmpegCodec c = FFmpegCodec.create().
                                        setCodec(codec).setType(type)
                                        .setDecode(decode).setEncode(encode)
                                        .setLossyCompress(lossy).setLosslessCompress(lossless)
                                        .setFrame(frame).setDescription(desc);
                                codecsData.add(c);
                            }
                        } catch (Exception e) {
                            error = e.toString();
                        }

                        process.waitFor();
                    } catch (Exception e) {
                        error = e.toString();
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (error != null) {
                        popError(error);
                    }
                    codecsView.setItems(codecsData);
                    codecLabel.setText(AppVariables.message("Total") + ": " + codecsData.size());

                }

                protected void taskQuit() {
                    codecsTask = null;
                }
            };

            openHandlingStage(codecsTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(codecsTask);
            thread.setDaemon(true);
            thread.start();

        }
    }

    public void readFilters() {
        filtersData.clear();
        filtersLabel.setText("");
        if (executable == null) {
            return;
        }
        synchronized (this) {
            if (filtersTask != null) {
                return;
            }
            filtersTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        error = null;

                        List<String> command = new ArrayList<>();
                        command.add(executable.getAbsolutePath());
                        command.add("-hide_banner");
                        command.add("-filters");
                        ProcessBuilder pb = new ProcessBuilder(command)
                                .redirectErrorStream(true);
                        pb.redirectErrorStream(true);
                        final Process process = pb.start();

                        try ( BufferedReader inReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {
                            String line;
                            int count = 0;
                            while ((line = inReader.readLine()) != null) {
                                count++;
                                if (count < 8 || line.length() < 35) {
                                    continue;
                                }
                                String flags = line.substring(0, 5);
                                boolean timeline = flags.contains("T");
                                boolean slice = flags.contains("S");
                                boolean com = flags.contains("C");
                                String[] v = StringTools.separatedBySpace(line.substring(5));
                                String filter = v[0];
                                String[] vv = StringTools.separatedBySpace(v[1]);
                                String direction = vv[0];
                                String desc = vv[1];
                                FFmpegFilter f = FFmpegFilter.create().
                                        setTimeline(timeline).setSlice(slice)
                                        .setCommand(com).setFilter(filter)
                                        .setDirection(direction)
                                        .setDescription(desc);
                                filtersData.add(f);
                            }
                        } catch (Exception e) {
                            error = e.toString();
                        }
                        process.waitFor();

                    } catch (Exception e) {
                        error = e.toString();
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (error != null) {
                        popError(error);
                    }
                    filtersView.setItems(filtersData);
                    filtersLabel.setText(AppVariables.message("Total") + ": " + filtersData.size()
                            + "   " + AppVariables.message("ffmpegFilterComments"));
                }

                protected void taskQuit() {
                    filtersTask = null;
                }
            };

            openHandlingStage(filtersTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(filtersTask);
            thread.setDaemon(true);
            thread.start();

        }
    }

    @FXML
    protected void queryAction() {
        String[] args = StringTools.splitBySpace(queryInput.getText());
        if (args.length == 0) {
            return;
        }
        queryArea.clear();
        if (executable == null) {
            return;
        }
        synchronized (this) {
            if (queryTask != null) {
                return;
            }
            queryTask = new SingletonTask<Void>() {
                private StringBuilder output;

                @Override
                protected boolean handle() {
                    try {
                        error = null;
                        List<String> command = new ArrayList<>();
                        command.add(executable.getAbsolutePath());
                        command.add("-hide_banner");
                        for (String arg : args) {
                            command.add(arg);
                        }
                        ProcessBuilder pb = new ProcessBuilder(command)
                                .redirectErrorStream(true);
                        pb.redirectErrorStream(true);
                        final Process process = pb.start();

                        try ( BufferedReader inReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {
                            String line;
                            output = new StringBuilder();
                            while ((line = inReader.readLine()) != null) {
                                output.append(line).append("\n");
                            }
                        } catch (Exception e) {
                            error = e.toString();
                        }

                        process.waitFor();

                    } catch (Exception e) {
                        error = e.toString();
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (error != null) {
                        popError(error);
                    }
                    if (output != null) {
                        queryArea.setText(output.toString());
                    }
                }

                protected void taskQuit() {
                    queryTask = null;
                }
            };

            openHandlingStage(queryTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(queryTask);
            thread.setDaemon(true);
            thread.start();

        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if ((formatsTask != null && formatsTask.isRunning())
                || (codecsTask != null && codecsTask.isRunning())
                || (queryTask != null && queryTask.isRunning())
                || (filtersTask != null && filtersTask.isRunning())) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("TaskRunning"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSure) {
                if (formatsTask != null) {
                    formatsTask.cancel();
                    formatsTask = null;
                }
                if (codecsTask != null) {
                    codecsTask.cancel();
                    codecsTask = null;
                }
                if (filtersTask != null) {
                    filtersTask.cancel();
                    filtersTask = null;
                }
                if (queryTask != null) {
                    queryTask.cancel();
                    queryTask = null;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /*
        classes
     */
    public static class FFmpegFormat {

        private boolean mux, demux;
        private String format, description;

        public FFmpegFormat() {
        }

        public static FFmpegFormat create() {
            return new FFmpegFormat();
        }

        public boolean isMux() {
            return mux;
        }

        public FFmpegFormat setMux(boolean mux) {
            this.mux = mux;
            return this;
        }

        public boolean isDemux() {
            return demux;
        }

        public FFmpegFormat setDemux(boolean demux) {
            this.demux = demux;
            return this;
        }

        public String getFormat() {
            return format;
        }

        public FFmpegFormat setFormat(String format) {
            this.format = format;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public FFmpegFormat setDescription(String description) {
            this.description = description;
            return this;
        }

    }

    public static class FFmpegCodec {

        private boolean decode, encode, lossyCompress, losslessCompress, frame;
        private String type, codec, description;

        public FFmpegCodec() {
        }

        public static FFmpegCodec create() {
            return new FFmpegCodec();
        }

        public String getCodec() {
            return codec;
        }

        public FFmpegCodec setCodec(String codec) {
            this.codec = codec;
            return this;
        }

        public boolean isDecode() {
            return decode;
        }

        public FFmpegCodec setDecode(boolean decode) {
            this.decode = decode;
            return this;
        }

        public boolean isEncode() {
            return encode;
        }

        public FFmpegCodec setEncode(boolean encode) {
            this.encode = encode;
            return this;
        }

        public boolean isLossyCompress() {
            return lossyCompress;
        }

        public FFmpegCodec setLossyCompress(boolean lossyCompress) {
            this.lossyCompress = lossyCompress;
            return this;
        }

        public boolean isLosslessCompress() {
            return losslessCompress;
        }

        public FFmpegCodec setLosslessCompress(boolean losslessCompress) {
            this.losslessCompress = losslessCompress;
            return this;
        }

        public boolean isFrame() {
            return frame;
        }

        public FFmpegCodec setFrame(boolean frame) {
            this.frame = frame;
            return this;
        }

        public String getType() {
            return type;
        }

        public FFmpegCodec setType(String type) {
            this.type = type;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public FFmpegCodec setDescription(String description) {
            this.description = description;
            return this;
        }

    }

    public static class FFmpegFilter {

        private String filter, direction, description;
        private boolean timeline, slice, command;

        public FFmpegFilter() {
        }

        public static FFmpegFilter create() {
            return new FFmpegFilter();
        }

        public String getFilter() {
            return filter;
        }

        public FFmpegFilter setFilter(String filter) {
            this.filter = filter;
            return this;
        }

        public boolean isTimeline() {
            return timeline;
        }

        public FFmpegFilter setTimeline(boolean timeline) {
            this.timeline = timeline;
            return this;
        }

        public boolean isSlice() {
            return slice;
        }

        public FFmpegFilter setSlice(boolean slice) {
            this.slice = slice;
            return this;
        }

        public boolean isCommand() {
            return command;
        }

        public FFmpegFilter setCommand(boolean command) {
            this.command = command;
            return this;
        }

        public String getDirection() {
            return direction;
        }

        public FFmpegFilter setDirection(String direction) {
            this.direction = direction;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public FFmpegFilter setDescription(String description) {
            this.description = description;
            return this;
        }

    }

}