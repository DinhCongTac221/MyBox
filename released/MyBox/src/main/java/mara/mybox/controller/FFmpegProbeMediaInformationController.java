package mara.mybox.controller;

import com.github.kokorin.jaffree.Rational;
import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.Format;
import com.github.kokorin.jaffree.ffprobe.Frame;
import com.github.kokorin.jaffree.ffprobe.Packet;
import com.github.kokorin.jaffree.ffprobe.PixelFormat;
import com.github.kokorin.jaffree.ffprobe.PixelFormatComponent;
import com.github.kokorin.jaffree.ffprobe.PixelFormatFlags;
import com.github.kokorin.jaffree.ffprobe.Stream;
import com.github.kokorin.jaffree.ffprobe.Subtitle;
import com.github.kokorin.jaffree.ffprobe.Tag;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;

import mara.mybox.tools.FFmpegTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegProbeMediaInformationController extends ControlFFmpegOptions {

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab formatTab, videoTab, audioTab, streamsTab, subtitlesTab,
            framesTab, packetsTab, pixelFormatsTab;
    @FXML
    protected WebView formatView, videoView, audioView, streamsView, subtitlesView,
            framesView, packetsView, pixelFormatsView;
    @FXML
    protected TextField framesStreamsInput, framesIntervalInput,
            packetsStreamsInput, packetsIntervalInput;

    public FFmpegProbeMediaInformationController() {
        baseTitle = Languages.message("FFmpegProbeMediaInformation");

    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            executableName = "FFprobeExecutable";
            executableDefault = "win".equals(SystemTools.os()) ? "D:\\Programs\\ffmpeg\\bin\\ffprobe.exe" : "/home/ffprobe";

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tabPane.getTabs().clear();

            functionBox.disableProperty().bind(executableInput.styleProperty().isEqualTo(NodeStyleTools.badStyle));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(framesStreamsInput, Languages.message("FFmpegStreamsSpecifierComments"));
            NodeStyleTools.setTooltip(framesIntervalInput, Languages.message("FFmpegIntervalComments"));
            NodeStyleTools.setTooltip(packetsStreamsInput, Languages.message("FFmpegStreamsSpecifierComments"));
            NodeStyleTools.setTooltip(packetsIntervalInput, Languages.message("FFmpegIntervalComments"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void checkSourceFileInput() {
        String v = sourceFileInput.getText();
        if (v == null || v.isEmpty()) {
            sourceFileInput.setStyle(NodeStyleTools.badStyle);
            return;
        }
        sourceFileInput.setStyle(null);
        final File file = new File(v);
        if (file.exists()) {
            sourceFile = file;
            recordFileOpened(file);
        } else {
            VisitHistoryTools.visitStreamMedia(v);
        }
    }

    protected void mediaSelected(String name) {
        sourceFileInput.setText(name);
        File file = new File(name);
        if (file.exists()) {
            sourceFile = file;
            recordFileOpened(file);
        } else {
            sourceFile = null;
            VisitHistoryTools.visitStreamMedia(name);
        }
    }

    @FXML
    public void popMedia(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                List<VisitHistory> recent = recentSourceFiles();
                List<VisitHistory> recentMedia = VisitHistoryTools.getRecentStreamMedia();
                recent.addAll(recentMedia);
                return recent;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectSourceFile();
            }

            @Override
            public void handleFile(String fname) {
                mediaSelected(fname);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

            @Override
            public void pop() {
                if (controller == null || event == null) {
                    return;
                }
                ContextMenu popMenu = controller.popMenu;
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                popMenu = new ContextMenu();
                popMenu.setAutoHide(true);

                MenuItem menu = new MenuItem(Languages.message("Select..."));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        handleSelect();
                    }
                });
                popMenu.getItems().add(menu);

                List<VisitHistory> his = recentFiles();
                if (his != null && !his.isEmpty()) {
                    popMenu.getItems().add(new SeparatorMenuItem());
                    menu = new MenuItem(Languages.message("RecentAccessedFiles"));
                    menu.setStyle("-fx-text-fill: #2e598a;");
                    popMenu.getItems().add(menu);
                    for (VisitHistory h : his) {
                        final String fname = h.getResourceValue();
                        menu = new MenuItem(fname);
                        menu.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                mediaSelected(fname);
                            }
                        });
                        popMenu.getItems().add(menu);
                    }
                }

                menu = new MenuItem(Languages.message("Examples"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                popMenu.getItems().add(menu);
                menu = new MenuItem("http://download.oracle.com/otndocs/products/javafx/JavaRap/prog_index.m3u8");
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mediaSelected("http://download.oracle.com/otndocs/products/javafx/JavaRap/prog_index.m3u8");
                    }
                });
                popMenu.getItems().add(menu);
                menu = new MenuItem("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mediaSelected("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
                    }
                });
                popMenu.getItems().add(menu);

                List<String> paths = paths();
                if (paths != null && !paths.isEmpty()) {
                    popMenu.getItems().add(new SeparatorMenuItem());
                    menu = new MenuItem(Languages.message("RecentAccessedDirectories"));
                    menu.setStyle("-fx-text-fill: #2e598a;");
                    popMenu.getItems().add(menu);
                    for (String path : paths) {
                        menu = new MenuItem(path);
                        menu.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                handlePath(path);
                            }
                        });
                        popMenu.getItems().add(menu);
                    }
                }

                controller.popMenu = popMenu;
                popMenu.getItems().add(new SeparatorMenuItem());
                menu = new MenuItem(Languages.message("PopupClose"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        controller.popMenu.hide();
                        controller.popMenu = null;
                    }
                });
                popMenu.getItems().add(menu);

                LocateTools.locateBelow((Region) event.getSource(), popMenu);

            }

        }.pop();
    }

    @FXML
    @Override
    public void startAction() {
        try {
            if (executable == null) {
                return;
            }
            final String address = sourceFileInput.getText();
            if (address == null || address.isEmpty()) {
                sourceFileInput.setStyle(NodeStyleTools.badStyle);
                return;
            }
            formatView.getEngine().loadContent​("");
            videoView.getEngine().loadContent​("");
            audioView.getEngine().loadContent​("");
            streamsView.getEngine().loadContent​("");
            subtitlesView.getEngine().loadContent​("");
            pixelFormatsView.getEngine().loadContent​("");
            framesView.getEngine().loadContent​("");
            packetsView.getEngine().loadContent​("");
            probeResult = null;

            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        error = null;
                        try {
                            probeResult = FFprobe.atPath(executable.toPath().getParent())
                                    .setShowFormat(true).setShowStreams(true)
                                    .setShowPixelFormats(true)
                                    .setInput(address)
                                    .execute();
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
                        showResults();
                    }
                };
                start(task);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void showResults() {
        tabPane.getTabs().clear();
        if (probeResult == null) {
            return;
        }
        showFormat();
        showVideoStream();
        showAudioStream();
        showOtherStreams();
        showSubtitles();
        tabPane.getTabs().addAll(framesTab, packetsTab);
        showPixelFormats();

        if (probeResult.getError() != null) {
            popError(probeResult.getError().getString());
        }
    }

    protected void showFormat() {
        if (isSettingValues || probeResult == null) {
            return;
        }
        Format format = probeResult.getFormat();
        if (format == null) {
            popError(Languages.message("InvalidData"));
            return;
        }
        tabPane.getTabs().add(formatTab);
        StringTable table = new StringTable(null, Languages.message("Format"));
        table.add(Arrays.asList(Languages.message("Format"), format.getFormatName()));
        table.add(Arrays.asList(Languages.message("FormatLongName"), format.getFormatLongName()));
        if (format.getDuration() != null) {
            table.add(Arrays.asList(Languages.message("Duration"), DateTools.timeDuration(Math.round(format.getDuration() * 1000))));
        }
        if (format.getSize() != null) {
            table.add(Arrays.asList(Languages.message("Size"), FileTools.showFileSize(format.getSize())));
        }
        if (format.getBitRate() != null) {
            table.add(Arrays.asList(Languages.message("BitRate"), (format.getBitRate() / 1024) + " kb/s"));
        }
        table.add(Arrays.asList(Languages.message("NumberOfStreams"), format.getNbStreams() + ""));
        table.add(Arrays.asList(Languages.message("NumberOfPrograms"), format.getNbPrograms() + ""));
        if (format.getProbeScore() != null) {
            table.add(Arrays.asList(Languages.message("ProbeScore"), format.getProbeScore() + ""));
        }
        if (format.getStartTime() != null) {
            table.add(Arrays.asList(Languages.message("StartTime"), format.getStartTime() + ""));
        }
        if (format.getTag() != null) {
            for (Tag tag : format.getTag()) {
                table.add(Arrays.asList(tag.getKey(), tag.getValue()));
            }
        }
        String html = StringTable.tableHtml(table);
        formatView.getEngine().loadContent​(html);
    }

    protected void showVideoStream() {
        if (isSettingValues || probeResult == null || probeResult.getStreams() == null) {
            return;
        }
        List<Stream> streams = probeResult.getStreams();
        StringBuilder s = new StringBuilder();
        Stream videoStream = null;
        for (int i = 0; i < streams.size(); ++i) {
            Stream stream = streams.get(i);
            if (stream.getCodecType() == StreamType.VIDEO) {
                videoStream = stream;
                break;
            }
        }
        if (videoStream == null) {
            tabPane.getTabs().remove(videoTab);
            return;
        }
        if (!tabPane.getTabs().contains(videoTab)) {
            tabPane.getTabs().add(videoTab);
        }
        String html = HtmlWriteTools.html(Languages.message("VideoStream"),
                streamTable(videoStream, Languages.message("VideoStream")));
        videoView.getEngine().loadContent​(html);
    }

    protected void showAudioStream() {
        if (isSettingValues || probeResult == null || probeResult.getStreams() == null) {
            return;
        }
        List<Stream> streams = probeResult.getStreams();
        StringBuilder s = new StringBuilder();
        Stream audioStream = null;
        for (int i = 0; i < streams.size(); ++i) {
            Stream stream = streams.get(i);
            if (stream.getCodecType() == StreamType.AUDIO) {
                audioStream = stream;
                break;
            }
        }
        if (audioStream == null) {
            tabPane.getTabs().remove(audioTab);
            return;
        }
        if (!tabPane.getTabs().contains(audioTab)) {
            tabPane.getTabs().add(audioTab);
        }
        String html = HtmlWriteTools.html(Languages.message("AudioStream"),
                streamTable(audioStream, Languages.message("AudioStream")));
        audioView.getEngine().loadContent​(html);
    }

    protected void showOtherStreams() {
        if (isSettingValues || probeResult == null || probeResult.getStreams() == null) {
            return;
        }
        List<Stream> streams = probeResult.getStreams();
        StringBuilder s = new StringBuilder();
        List<Stream> otherStreams = new ArrayList();
        for (int i = 0; i < streams.size(); ++i) {
            Stream stream = streams.get(i);
            if (stream.getCodecType() != StreamType.AUDIO
                    && stream.getCodecType() != StreamType.VIDEO) {
                otherStreams.add(stream);
            }
        }
        if (otherStreams.isEmpty()) {
            tabPane.getTabs().remove(streamsTab);
            return;
        }
        if (!tabPane.getTabs().contains(streamsTab)) {
            tabPane.getTabs().add(streamsTab);
        }

        for (int i = 0; i < otherStreams.size(); ++i) {
            Stream stream = otherStreams.get(i);
            s.append(streamTable(stream, Languages.message("Stream") + " " + stream.getIndex())).append("</hr>");
        }

        String html = HtmlWriteTools.html(null, s.toString());
        streamsView.getEngine().loadContent​(html);
    }

    protected String streamTable(Stream stream, String name) {
        if (isSettingValues || stream == null) {
            return null;
        }
        StringBuilder s = new StringBuilder();
        StringTable table = new StringTable(null, name);
        if (stream.getId() != null) {
            table.add(Arrays.asList(Languages.message("ID"), stream.getId()));
        }
        if (stream.getCodecType() != null) {
            table.add(Arrays.asList(Languages.message("CodecType"), stream.getCodecType().name()));
        }
        if (stream.getCodecName() != null) {
            table.add(Arrays.asList(Languages.message("CodecName"), stream.getCodecName()));
        }
        if (stream.getCodecLongName() != null) {
            table.add(Arrays.asList(Languages.message("CodecLongName"), stream.getCodecLongName()));
        }
        if (stream.getProfile() != null) {
            table.add(Arrays.asList(Languages.message("Profile"), stream.getProfile()));
        }
        if (stream.getLevel() != null) {
            table.add(Arrays.asList(Languages.message("Level"), stream.getLevel() + ""));
        }
        if (stream.getCodecTimeBase() != null) {
            Rational r = stream.getCodecTimeBase();
            table.add(Arrays.asList(Languages.message("CodecTimeBase"), r.denominator + "." + r.numerator));
        }
        if (stream.getDuration() != null) {
            table.add(Arrays.asList(Languages.message("Duration"), DateTools.timeDuration(Math.round(stream.getDuration() * 1000))));
        }
        if (stream.getStartTime() != null) {
            table.add(Arrays.asList(Languages.message("StartTime"), stream.getStartTime() + ""));
        }
        if (stream.getCodecTag() != null) {
            table.add(Arrays.asList(Languages.message("CodecTag"), stream.getCodecTag() + ""));
        }
        if (stream.getCodecTagString() != null) {
            table.add(Arrays.asList(Languages.message("CodecTagString"), stream.getCodecTagString() + ""));
        }
        if (stream.getWidth() != null) {
            table.add(Arrays.asList(Languages.message("Width"), stream.getWidth() + ""));
        }
        if (stream.getHeight() != null) {
            table.add(Arrays.asList(Languages.message("Height"), stream.getHeight() + ""));
        }
        if (stream.getCodedWidth() != null) {
            table.add(Arrays.asList(Languages.message("CodedWidth"), stream.getCodedWidth() + ""));
        }
        if (stream.getCodedHeight() != null) {
            table.add(Arrays.asList(Languages.message("CodedHeight"), stream.getCodedHeight() + ""));
        }
        if (stream.getBitRate() != null) {
            table.add(Arrays.asList(Languages.message("BitRate"), (stream.getBitRate() / 1024) + " kb/s"));
        }
        if (stream.getMaxBitRate() != null) {
            table.add(Arrays.asList(Languages.message("MaxBitRate"), (stream.getMaxBitRate() / 1024) + " kb/s"));
        }
        if (stream.getSampleRate() != null) {
            table.add(Arrays.asList(Languages.message("SampleRate"), stream.getSampleRate() + ""));
        }
        if (stream.getSampleFmt() != null) {
            table.add(Arrays.asList(Languages.message("SampleFmt"), stream.getSampleFmt() + ""));
        }
        if (stream.getSampleAspectRatio() != null) {
            table.add(Arrays.asList(Languages.message("SampleAspectRatio"), stream.getSampleAspectRatio() + ""));
        }
        if (stream.getDisplayAspectRatio() != null) {
            table.add(Arrays.asList(Languages.message("DisplayAspectRatio"), stream.getDisplayAspectRatio() + ""));
        }
        if (stream.getBitsPerSample() != null) {
            table.add(Arrays.asList(Languages.message("BitsPerSample"), stream.getBitsPerSample() + ""));
        }
        if (stream.getBitsPerRawSample() != null) {
            table.add(Arrays.asList(Languages.message("MaxBitRate"), stream.getBitsPerRawSample() + ""));
        }
        if (stream.getRFrameRate() != null) {
            table.add(Arrays.asList(Languages.message("FrameRate"), stream.getRFrameRate() + ""));
        }
        if (stream.getAvgFrameRate() != null) {
            table.add(Arrays.asList(Languages.message("AvgFrameRate"), stream.getAvgFrameRate() + ""));
        }
        if (stream.getNbFrames() != null) {
            table.add(Arrays.asList(Languages.message("NumberOfFrames"), stream.getNbFrames() + ""));
        }
        if (stream.getNbReadFrames() != null) {
            table.add(Arrays.asList(Languages.message("NumberOfReadFrames"), stream.getNbReadFrames() + ""));
        }
        if (stream.getNbReadPackets() != null) {
            table.add(Arrays.asList(Languages.message("NumberOfReadPackets"), stream.getNbReadPackets() + ""));
        }
        if (stream.getChannels() != null) {
            table.add(Arrays.asList(Languages.message("Channels"), stream.getChannels() + ""));
        }
        if (stream.getChannelLayout() != null) {
            table.add(Arrays.asList(Languages.message("ChannelLayout"), stream.getChannelLayout() + ""));
        }
        if (stream.getPixFmt() != null) {
            table.add(Arrays.asList(Languages.message("PixFmt"), stream.getPixFmt() + ""));
        }
        if (stream.getColorSpace() != null) {
            table.add(Arrays.asList(Languages.message("ColorSpace"), stream.getColorSpace() + ""));
        }
        if (stream.getColorPrimaries() != null) {
            table.add(Arrays.asList(Languages.message("ColorPrimaries"), stream.getColorPrimaries() + ""));
        }
        if (stream.getColorRange() != null) {
            table.add(Arrays.asList(Languages.message("ColorRange"), stream.getColorRange() + ""));
        }
        if (stream.getColorTransfer() != null) {
            table.add(Arrays.asList(Languages.message("ColorTransfer"), stream.getColorTransfer() + ""));
        }
        if (stream.getFieldOrder() != null) {
            table.add(Arrays.asList(Languages.message("FieldOrder"), stream.getFieldOrder() + ""));
        }
        if (stream.getChromaLocation() != null) {
            table.add(Arrays.asList(Languages.message("ChromaLocation"), stream.getChromaLocation() + ""));
        }
        s.append(StringTable.tableDiv(table)).append("</hr>");

        return s.toString();
    }

    protected void showSubtitles() {
        if (isSettingValues || probeResult == null) {
            return;
        }
        List<Subtitle> subtitles = probeResult.getSubtitles();
        if (subtitles == null || subtitles.isEmpty()) {
            tabPane.getTabs().remove(subtitlesTab);
            return;
        }
        if (!tabPane.getTabs().contains(subtitlesTab)) {
            tabPane.getTabs().add(subtitlesTab);
        }
        StringTable table = new StringTable(null, Languages.message("Subtitles"));
        for (Subtitle subtitle : subtitles) {
            table.add(Arrays.asList(Languages.message("MediaType"), subtitle.getMediaType().name()));
            table.add(Arrays.asList(Languages.message("Format"), subtitle.getFormat() + ""));
            table.add(Arrays.asList(Languages.message("pts"), subtitle.getPts() + ""));
            table.add(Arrays.asList(Languages.message("pts_time"), subtitle.getPtsTime() + ""));
            table.add(Arrays.asList(Languages.message("start_display_time"), subtitle.getStartDisplayTime() + ""));
            table.add(Arrays.asList(Languages.message("end_display_time"), subtitle.getEndDisplayTime() + ""));
            table.add(Arrays.asList(Languages.message("num_rects"), subtitle.getNumRects() + ""));
        }

        String html = StringTable.tableHtml(table);
        subtitlesView.getEngine().loadContent​(html);
    }

    protected void showPixelFormats() {
        if (isSettingValues || probeResult == null) {
            return;
        }
        List<PixelFormat> pixelFormats = probeResult.getPixelFormats();
        if (pixelFormats == null || pixelFormats.isEmpty()) {
            tabPane.getTabs().remove(pixelFormatsTab);
            return;
        }
        if (!tabPane.getTabs().contains(pixelFormatsTab)) {
            tabPane.getTabs().add(pixelFormatsTab);
        }
        StringBuilder s = new StringBuilder();
        for (PixelFormat pixelFormat : pixelFormats) {
            StringTable table = new StringTable(null, Languages.message("Format") + ": " + pixelFormat.getName());
            table.add(Arrays.asList(Languages.message("BitsPerPixel"), pixelFormat.getBitsPerPixel() + ""));
            table.add(Arrays.asList(Languages.message("NumberOfComponents"), pixelFormat.getNbComponents() + ""));
            table.add(Arrays.asList(Languages.message("log2_chroma_h"), pixelFormat.getLog2ChromaH() + ""));
            table.add(Arrays.asList(Languages.message("log2_chroma_w"), pixelFormat.getLog2ChromaW() + ""));
            List<PixelFormatComponent> components = pixelFormat.getComponents();
            if (components != null && !components.isEmpty()) {
                String b = "";
                for (PixelFormatComponent c : components) {
                    b += c.getBitDepth() + " ";
                }
                table.add(Arrays.asList(Languages.message("BitDepth"), b));
            }
            PixelFormatFlags flags = pixelFormat.getFlags();
            if (flags != null) {
                table.add(Arrays.asList(Languages.message("rgb"), flags.getRgb() + ""));
                table.add(Arrays.asList(Languages.message("alpha"), flags.getAlpha() + ""));
                table.add(Arrays.asList(Languages.message("big_endian"), flags.getBigEndian() + ""));
                table.add(Arrays.asList(Languages.message("palette"), flags.getPalette() + ""));
                table.add(Arrays.asList(Languages.message("bitstream"), flags.getBitstream() + ""));
                table.add(Arrays.asList(Languages.message("hwaccel"), flags.getHwaccel() + ""));
                table.add(Arrays.asList(Languages.message("planar"), flags.getPlanar() + ""));
                table.add(Arrays.asList(Languages.message("pseudopal"), flags.getPseudopal() + ""));
            }
            s.append(StringTable.tableDiv(table)).append("</hr>");
        }
        String html = HtmlWriteTools.html(Languages.message("Format"), s.toString());
        pixelFormatsView.getEngine().loadContent​(html);
    }

    @FXML
    protected void framesAction() {
        try {
            framesView.getEngine().loadContent​("");
            probeResult = null;
            if (executable == null || sourceFile == null) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        error = null;
                        try {
                            probeResult = FFmpegTools.FFprobleFrames(executable, sourceFile,
                                    framesStreamsInput.getText(), framesIntervalInput.getText());
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
                        if (probeResult == null) {
                            return;
                        }
                        showFrames(probeResult.getFrames());
                    }
                };
                start(task);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void showFrames(List<Frame> frames) {
        if (isSettingValues) {
            return;
        }
        if (frames == null || frames.isEmpty()) {
            popInformation(Languages.message("NoData"));
            return;
        }
        StringBuilder s = new StringBuilder();
        int size = frames.size();
        s.append(Languages.message("Found")).append(": ").append(size);
        for (int i = 0; i < size; ++i) {
            Frame frame = frames.get(i);
            StringTable table = new StringTable(null, Languages.message("Frame") + " " + i);
            if (frame.getMediaType() != null) {
                table.add(Arrays.asList(Languages.message("MediaType"), frame.getMediaType().name()));
            }
            if (frame.getStreamIndex() != null) {
                table.add(Arrays.asList(Languages.message("StreamIndex"), frame.getStreamIndex() + ""));
            }
            table.add(Arrays.asList(Languages.message("KeyFrame"), frame.getKeyFrame() + ""));
            if (frame.getPktDts() != null) {
                table.add(Arrays.asList(Languages.message("PktDts"), frame.getPktDts() + ""));
            }
            if (frame.getPktDtsTime() != null) {
                table.add(Arrays.asList(Languages.message("PktDtsTime"), frame.getPktDtsTime() + ""));
            }
            if (frame.getPktPts() != null) {
                table.add(Arrays.asList(Languages.message("PktPts"), frame.getPktPts() + ""));
            }
            if (frame.getPktPtsTime() != null) {
                table.add(Arrays.asList(Languages.message("PktPtsTime"), frame.getPktPtsTime() + ""));
            }
            if (frame.getBestEffortTimestamp() != null) {
                table.add(Arrays.asList(Languages.message("BestEffortTimestamp"), frame.getBestEffortTimestamp() + ""));
            }
            if (frame.getBestEffortTimestampTime() != null) {
                table.add(Arrays.asList(Languages.message("BestEffortTimestampTime"), frame.getBestEffortTimestampTime() + ""));
            }
            if (frame.getPktDuration() != null) {
                table.add(Arrays.asList(Languages.message("PktDuration"), frame.getPktDuration() + ""));
            }
            if (frame.getPktDurationTime() != null) {
                table.add(Arrays.asList(Languages.message("PktDurationTime"), frame.getPktDurationTime() + ""));
            }
            if (frame.getPktPos() != null) {
                table.add(Arrays.asList(Languages.message("PktPos"), frame.getPktPos() + ""));
            }
            if (frame.getPktSize() != null) {
                table.add(Arrays.asList(Languages.message("PktSize"), frame.getPktSize() + ""));
            }
            if (frame.getPts() != null) {
                table.add(Arrays.asList(Languages.message("Pts"), frame.getPts() + ""));
            }
            if (frame.getPtsTime() != null) {
                table.add(Arrays.asList(Languages.message("PtsTime"), frame.getPtsTime() + ""));
            }
            if (frame.getSampleFmt() != null) {
                table.add(Arrays.asList(Languages.message("SampleFmt"), frame.getSampleFmt() + ""));
            }
            if (frame.getSampleAspectRatio() != null) {
                table.add(Arrays.asList(Languages.message("SampleAspectRatio"), frame.getSampleAspectRatio() + ""));
            }
            if (frame.getNbSamples() != null) {
                table.add(Arrays.asList(Languages.message("NumberOfSamples"), frame.getNbSamples() + ""));
            }
            if (frame.getChannels() != null) {
                table.add(Arrays.asList(Languages.message("Channels"), frame.getChannels() + ""));
            }
            if (frame.getChannelLayout() != null) {
                table.add(Arrays.asList(Languages.message("ChannelLayout"), frame.getChannelLayout() + ""));
            }
            if (frame.getWidth() != null) {
                table.add(Arrays.asList(Languages.message("Width"), frame.getWidth() + ""));
            }
            if (frame.getHeight() != null) {
                table.add(Arrays.asList(Languages.message("Height"), frame.getHeight() + ""));
            }
            if (frame.getPictType() != null) {
                table.add(Arrays.asList(Languages.message("PictType"), frame.getPictType() + ""));
            }
            if (frame.getPixFmt() != null) {
                table.add(Arrays.asList(Languages.message("PixFmt"), frame.getPixFmt() + ""));
            }
            if (frame.getCodedPictureNumber() != null) {
                table.add(Arrays.asList(Languages.message("CodedPictureNumber"), frame.getCodedPictureNumber() + ""));
            }
            if (frame.getDisplayPictureNumber() != null) {
                table.add(Arrays.asList(Languages.message("DisplayPictureNumber"), frame.getDisplayPictureNumber() + ""));
            }
            if (frame.getRepeatPict() != null) {
                table.add(Arrays.asList(Languages.message("RepeatPict"), frame.getRepeatPict() + ""));
            }
            if (frame.getInterlacedFrame() != null) {
                table.add(Arrays.asList(Languages.message("InterlacedFrame"), frame.getInterlacedFrame() + ""));
            }

            s.append(StringTable.tableDiv(table)).append("</hr>");
        }

        String html = HtmlWriteTools.html(Languages.message("Frames"), s.toString());
        framesView.getEngine().loadContent​(html);
    }

    @FXML
    protected void packetsAction() {
        try {
            packetsView.getEngine().loadContent​("");
            probeResult = null;
            if (executable == null || sourceFile == null) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        error = null;
                        try {
                            probeResult = FFmpegTools.FFproblePackets(executable, sourceFile,
                                    framesStreamsInput.getText(), framesIntervalInput.getText());
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
                        if (probeResult == null) {
                            return;
                        }
                        showPackets(probeResult.getPackets());
                    }
                };
                start(task);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void showPackets(List<Packet> packets) {
        if (isSettingValues) {
            return;
        }
        if (packets == null || packets.isEmpty()) {
            popInformation(Languages.message("NoData"));
            return;
        }
        StringBuilder s = new StringBuilder();
        int size = packets.size();
        s.append(Languages.message("Found")).append(": ").append(size);
        for (int i = 0; i < size; ++i) {
            Packet packet = packets.get(i);
            StringTable table = new StringTable(null, Languages.message("Frame") + " " + i);
            if (packet.getCodecType() != null) {
                table.add(Arrays.asList(Languages.message("MediaType"), packet.getCodecType().name()));
            }
            table.add(Arrays.asList(Languages.message("StreamIndex"), packet.getStreamIndex() + ""));
            if (packet.getPts() != null) {
                table.add(Arrays.asList(Languages.message("Pts"), packet.getPts() + ""));
            }
            if (packet.getPtsTime() != null) {
                table.add(Arrays.asList(Languages.message("PtsTime"), packet.getPtsTime() + ""));
            }
            if (packet.getDts() != null) {
                table.add(Arrays.asList(Languages.message("Dts"), packet.getDts() + ""));
            }
            if (packet.getDtsTime() != null) {
                table.add(Arrays.asList(Languages.message("DtsTime"), packet.getDtsTime() + ""));
            }
            if (packet.getDuration() != null) {
                table.add(Arrays.asList(Languages.message("Duration"), packet.getDuration() + ""));
            }
            if (packet.getDurationTime() != null) {
                table.add(Arrays.asList(Languages.message("DurationTime"), packet.getDurationTime() + ""));
            }
            if (packet.getConvergenceDuration() != null) {
                table.add(Arrays.asList(Languages.message("ConvergenceDuration"), packet.getConvergenceDuration() + ""));
            }
            if (packet.getConvergenceDurationTime() != null) {
                table.add(Arrays.asList(Languages.message("ConvergenceDurationTime"), packet.getConvergenceDurationTime() + ""));
            }
            table.add(Arrays.asList(Languages.message("Size"), packet.getSize() + ""));
            if (packet.getPos() != null) {
                table.add(Arrays.asList(Languages.message("Position"), packet.getPos() + ""));
            }
            if (packet.getFlags() != null) {
                table.add(Arrays.asList(Languages.message("Flags"), packet.getFlags() + ""));
            }
            s.append(StringTable.tableDiv(table)).append("</hr>");
        }

        String html = HtmlWriteTools.html(Languages.message("Packets"), s.toString());
        packetsView.getEngine().loadContent​(html);
    }

}
