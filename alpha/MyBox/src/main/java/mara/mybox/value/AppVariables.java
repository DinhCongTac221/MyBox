package mara.mybox.value;

import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import javafx.beans.property.SimpleBooleanProperty;
import mara.mybox.controller.AlarmClockController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ImageClipboardMonitor;
import mara.mybox.fxml.StyleData;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.TextClipboardMonitor;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.value.Languages.getBundle;
import static mara.mybox.value.Languages.getTableBundle;
import static mara.mybox.value.UserConfig.getPdfMem;
import org.apache.pdfbox.io.MemoryUsageSetting;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class AppVariables {

    public static String[] appArgs;
    public static File MyboxConfigFile, MyBoxLogsPath;
    public static String MyboxDataPath, AlarmClocksFile;
    public static File MyBoxTempPath, MyBoxDerbyPath, MyBoxLanguagesPath;
    public static List<File> MyBoxReservePaths;
    public static ResourceBundle currentBundle, currentTableBundle;
    public static Map<String, String> userConfigValues = new HashMap<>();
    public static Map<String, String> systemConfigValues = new HashMap<>();
    public static ScheduledExecutorService executorService;
    public static Map<Long, ScheduledFuture<?>> scheduledTasks;
    public static AlarmClockController alarmClockController;
    public static MemoryUsageSetting pdfMemUsage;
    public static int sceneFontSize, fileRecentNumber, iconSize, thumbnailWidth;
    public static boolean closeCurrentWhenOpenTool, recordWindowsSizeLocation, controlDisplayText,
            hidpiIcons, ignoreDbUnavailable, popErrorLogs, saveDebugLogs, detailedDebugLogs,
            isTesting, handlingExit;
    public static StyleData.StyleColor ControlColor;
    public static TextClipboardMonitor textClipboardMonitor;
    public static ImageClipboardMonitor imageClipboardMonitor;
    public static Timer exitTimer;
    public static SimpleBooleanProperty errorNotify;
    public static Map<RenderingHints.Key, Object> imageRenderHints;

    public static void initAppVaribles() {
        try {
            userConfigValues.clear();
            systemConfigValues.clear();
            getBundle();
            getTableBundle();
            getPdfMem();
            closeCurrentWhenOpenTool = UserConfig.getBoolean("CloseCurrentWhenOpenTool", true);
            recordWindowsSizeLocation = UserConfig.getBoolean("RecordWindowsSizeLocation", true);
            sceneFontSize = UserConfig.getInt("SceneFontSize", 15);
            fileRecentNumber = UserConfig.getInt("FileRecentNumber", 16);
            iconSize = UserConfig.getInt("IconSize", 20);
            thumbnailWidth = UserConfig.getInt("ThumbnailWidth", 100);
            ControlColor = StyleTools.getConfigStyleColor();
            controlDisplayText = UserConfig.getBoolean("ControlDisplayText", false);
            hidpiIcons = UserConfig.getBoolean("HidpiIcons", Toolkit.getDefaultToolkit().getScreenResolution() > 120);
            saveDebugLogs = UserConfig.getBoolean("SaveDebugLogs", false);
            detailedDebugLogs = UserConfig.getBoolean("DetailedDebugLogs", false);
            ignoreDbUnavailable = false;
            popErrorLogs = UserConfig.getBoolean("PopErrorLogs", true);
            errorNotify = new SimpleBooleanProperty(false);
            isTesting = false;
            ImageRenderHints.loadImageRenderHints();

            exitTimer = new Timer();
            exitTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (handlingExit) {
                        return;
                    }
                    WindowTools.checkExit();
                }
            }, 3000);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
