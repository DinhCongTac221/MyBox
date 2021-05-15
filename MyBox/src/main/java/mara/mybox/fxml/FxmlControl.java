package mara.mybox.fxml;

import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import mara.mybox.controller.BaseController;
import mara.mybox.data.BaseTask;
import mara.mybox.data.DoublePoint;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.MediaTools;
import mara.mybox.tools.SoundTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.MyboxDataPath;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.w3c.dom.Document;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 11:19:42
 * @License Apache License Version 2.0
 */
public class FxmlControl {

    public static String blueText = "-fx-text-fill: #2e598a;";
    public static String darkBlueText = "-fx-text-fill: #2e598a;  -fx-font-weight: bolder;";
    public static String redText = "-fx-text-fill: #961c1c;";
    public static String darkRedText = "-fx-text-fill: #961c1c;  -fx-font-weight: bolder;";
    public static String badStyle = "-fx-text-box-border: blue;   -fx-text-fill: blue;";
    public static String warnStyle = "-fx-text-box-border: orange;   -fx-text-fill: orange;";
    public static String errorData = "-fx-background-color: #e5fbe5";
    public static String selectedData = "-fx-background-color:  #0096C9; -fx-text-background-color: white";

    public enum LabelType {
        NotDisplay, NameAndValue, Value, Name, Pop
    }

    public enum ChartCoordinate {
        Cartesian, LogarithmicE, Logarithmic10, SquareRoot
    }

    public static double realValue(ChartCoordinate chartCoordinate, double coordinateValue) {
        if (chartCoordinate == null) {
            return coordinateValue;
        }
        switch (chartCoordinate) {
            case LogarithmicE:
                return Math.pow(Math.E, coordinateValue);
            case Logarithmic10:
                return Math.pow(10, coordinateValue);
            case SquareRoot:
                return coordinateValue * coordinateValue;
        }
        return coordinateValue;
    }

    public static double coordinateValue(ChartCoordinate chartCoordinate, double value) {
        if (chartCoordinate == null || value <= 0) {
            return value;
        }
        switch (chartCoordinate) {
            case LogarithmicE:
                return Math.log(value);
            case Logarithmic10:
                return Math.log10(value);
            case SquareRoot:
                return Math.sqrt(value);
        }
        return value;
    }

    public static void miao2() {
        playClip("/sound/guaiMiao2.mp3", "guaiMiao2.mp3");
    }

    public static void miao3() {
        playClip("/sound/guaiMiao3.mp3", "guaiMiao3.mp3");
    }

    public static void miao5() {
        playClip("/sound/guaiMiao5.mp3", "guaiMiao5.mp3");
    }

    public static void miao6() {
        playClip("/sound/guaiMiao6.mp3", "guaiMiao6.mp3");
    }

    public static void miao7() {
        playClip("/sound/guaiMiao7.mp3", "guaiMiao7.mp3");
    }

    public static void BenWu() {
        playClip("/sound/BenWu.mp3", "BenWu.mp3");
    }

    public static void GuaiAO() {
        playClip("/sound/GuaiAO.mp3", "GuaiAO.mp3");
    }

    public static void BenWu2() {
        playClip("/sound/BenWu2.mp3", "BenWu2.mp3");
    }

    public static void mp3(File file) {
        playClip(file);
    }

    public static Node findNode(Pane pane, String nodeId) {
        try {
            Node node = pane.lookup("#" + nodeId);
            return node;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean setStyle(Pane pane, String nodeId, String style) {
        try {
            Node node = pane.lookup("#" + nodeId);
            return setStyle(node, style);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean setStyle(Node node, String style) {
        try {
            if (node == null) {
                return false;
            }
            if (node instanceof ComboBox) {
                ComboBox c = (ComboBox) node;
                c.getEditor().setStyle(style);
            } else {
                node.setStyle(style);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void refreshStyle(Parent node) {
        node.applyCss();
        node.layout();
        applyStyle(node);
    }

    public static void applyStyle(Node node) {
        if (node == null) {
            return;
        }
        ControlStyle.setStyle(node);
        if (node instanceof Parent) {
            for (Node c : ((Parent) node).getChildrenUnmodifiable()) {
                applyStyle(c);
            }
        }
    }

    public static void playClip(final String file, final String userFile) {
        BaseTask miaoTask = new BaseTask<Void>() {
            @Override
            protected boolean handle() {
                try {
                    File sound = FxmlControl.getInternalFile(file, "sound", userFile);
                    FloatControl control = SoundTools.getControl(sound);
                    Clip player = SoundTools.playback(sound, control.getMaximum() * 0.6f);
                    player.start();
                } catch (Exception e) {
                }
                return true;
            }
        };
        Thread thread = new Thread(miaoTask);
        thread.setDaemon(true);
        thread.start();
    }

    public static void playClip(final File file) {
        BaseTask miaoTask = new BaseTask<Void>() {
            @Override
            protected boolean handle() {
                try {
                    FloatControl control = SoundTools.getControl(file);
                    Clip player = SoundTools.playback(file, control.getMaximum() * 0.6f);
                    player.start();
                } catch (Exception e) {
                }
                return true;
            }
        };
        Thread thread = new Thread(miaoTask);
        thread.setDaemon(true);
        thread.start();
    }

    public static void playSound(final String file, final String userFile) {
        File miao = FxmlControl.getInternalFile(file, "sound", userFile);
        MediaTools.play(miao, 1, 1);
    }

    public static void setScrollPane(ScrollPane scrollPane, double xOffset,
            double yOffset) {
        final Bounds visibleBounds = scrollPane.getViewportBounds();
        double scrollWidth = scrollPane.getContent().getBoundsInParent().getWidth() - visibleBounds.getWidth();
        double scrollHeight = scrollPane.getContent().getBoundsInParent().getHeight() - visibleBounds.getHeight();

        scrollPane.setHvalue(scrollPane.getHvalue() + xOffset / scrollWidth);
        scrollPane.setVvalue(scrollPane.getVvalue() + yOffset / scrollHeight);
    }

    public static boolean setRadioFirstSelected(ToggleGroup group) {
        if (group == null) {
            return false;
        }
        ObservableList<Toggle> buttons = group.getToggles();
        for (Toggle button : buttons) {
            RadioButton radioButton = (RadioButton) button;
            radioButton.setSelected(true);
            return true;
        }
        return false;
    }

    public static boolean setRadioSelected(ToggleGroup group, String text) {
        if (group == null || text == null) {
            return false;
        }
        ObservableList<Toggle> buttons = group.getToggles();
        for (Toggle button : buttons) {
            RadioButton radioButton = (RadioButton) button;
            if (text.equals(radioButton.getText())) {
                button.setSelected(true);
                return true;
            }
        }
        return false;
    }

    public static boolean setItemSelected(ComboBox<String> box, String text) {
        if (box == null || text == null) {
            return false;
        }
        ObservableList<String> items = box.getItems();
        for (String item : items) {
            if (text.equals(item)) {
                box.getSelectionModel().select(item);
                return true;
            }
        }
        return false;
    }

    public static void setTooltip(final Node node, Node tip) {
        if (node instanceof Control) {
            removeTooltip((Control) node);
        }
        Tooltip tooltip = new Tooltip();
        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        tooltip.setGraphic(tip);
        tooltip.setShowDelay(Duration.millis(10));
        tooltip.setShowDuration(Duration.millis(360000));
        tooltip.setHideDelay(Duration.millis(10));
        Tooltip.install(node, tooltip);
    }

    public static void setTooltip(final Node node, String tips) {
        setTooltip(node, new Tooltip(tips));
    }

    public static void setTooltip(final Node node, final Tooltip tooltip) {
        if (node instanceof Control) {
            removeTooltip((Control) node);
        }
        tooltip.setFont(new Font(AppVariables.sceneFontSize));
        tooltip.setShowDelay(Duration.millis(10));
        tooltip.setShowDuration(Duration.millis(360000));
        tooltip.setHideDelay(Duration.millis(10));
        Tooltip.install(node, tooltip);
    }

    public static void removeTooltip(final Control node) {
        Tooltip.uninstall(node, node.getTooltip());
    }

    public static void removeTooltip(final Node node, final Tooltip tooltip) {
        Tooltip.uninstall(node, tooltip);
    }

    public static String getFxmlName(URL url) {
        if (url == null) {
            return null;
        }
        try {
            String fullPath = url.getPath();
            if (!fullPath.endsWith(".fxml")) {
                return null;
            }
            String fname;
            int pos = fullPath.lastIndexOf('/');
            if (pos < 0) {
                fname = fullPath;
            } else {
                fname = fullPath.substring(pos + 1);
            }
            return fname.substring(0, fname.length() - 5);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getFxmlFile(URL url) {
        return "/fxml/" + getFxmlName(url) + ".fxml";
    }

    public static int getInputInt(TextField input) {
        try {
            return Integer.parseInt(input.getText());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "InvalidData");
        }
    }

    public static void setNonnegativeValidation(final TextField input) {
        setNonnegativeValidation(input, Integer.MAX_VALUE);
    }

    public static void setNonnegativeValidation(final TextField input,
            final int max) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v >= 0 && v <= max) {
                        input.setStyle(null);
                    } else {
                        input.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static void setPositiveValidation(final TextField input) {
        setPositiveValidation(input, Integer.MAX_VALUE);
    }

    public static void setPositiveValidation(final TextField input,
            final int max) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0 && v <= max) {
                        input.setStyle(null);
                    } else {
                        input.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static int positiveValue(final TextField input) {
        return positiveValue(input, Integer.MAX_VALUE);
    }

    public static int positiveValue(final TextField input, final int max) {
        try {
            int v = Integer.parseInt(input.getText());
            if (v > 0 && v <= max) {
                input.setStyle(null);
                return v;
            } else {
                input.setStyle(badStyle);
                return -1;
            }
        } catch (Exception e) {
            input.setStyle(badStyle);
            return -1;
        }
    }

    public static void setFloatValidation(final TextField input) {
        input.textProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    try {
                        float v = Float.valueOf(newValue);
                        input.setStyle(null);
                    } catch (Exception e) {
                        input.setStyle(badStyle);
                    }
                });
    }

    public static void setFileValidation(final TextField input, String key) {
        if (input == null) {
            return;
        }
        input.setStyle(badStyle);
        input.textProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    String v = input.getText();
                    if (v == null || v.isEmpty()) {
                        input.setStyle(badStyle);
                        return;
                    }
                    final File file = new File(newValue);
                    if (!file.exists() || !file.isFile()) {
                        input.setStyle(badStyle);
                        return;
                    }
                    input.setStyle(null);
                    AppVariables.setUserConfigValue(key, file.getParent());
                });
    }

    public static void setPathValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.isDirectory()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static void setPathExistedValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.exists() || !file.isDirectory()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static File getInternalFile(String resourceFile, String subPath,
            String userFile) {
        return getInternalFile(resourceFile, subPath, userFile, false);
    }

    // Solution from https://stackoverflow.com/questions/941754/how-to-get-a-path-to-a-resource-in-a-java-jar-file
    public static File getInternalFile(String resourceFile, String subPath, String userFile, boolean deleteExisted) {
        if (resourceFile == null || userFile == null) {
            return null;
        }
        try {
            File path = new File(MyboxDataPath + File.separator + subPath + File.separator);
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(MyboxDataPath + File.separator + subPath + File.separator + userFile);
            if (file.exists() && !deleteExisted) {
                return file;
            }
            File tmpFile = getInternalFile(resourceFile);
            if (tmpFile == null) {
                return null;
            }
            FileTools.rename(tmpFile, file);
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static File getInternalFile(String resourceFile) {
        if (resourceFile == null) {
            return null;
        }
        File file = FileTools.getTempFile();
        try ( InputStream input = FxmlControl.class.getResourceAsStream(resourceFile);
                 OutputStream out = new FileOutputStream(file)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = input.read(bytes)) > 0) {
                out.write(bytes, 0, read);
            }
            file.deleteOnExit();
            return file;
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void setEditorStyle(final ComboBox box, final String style) {
        box.getEditor().setStyle(style);
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                box.getEditor().setStyle(style);
//            }
//        });
    }

    public static void setEditorBadStyle(final ComboBox box) {
        setEditorStyle(box, badStyle);
    }

    public static void setEditorWarnStyle(final ComboBox box) {
        setEditorStyle(box, warnStyle);
    }

    public static void setEditorNormal(final ComboBox box) {
        setEditorStyle(box, null);
    }

    public static Node getFocus(BaseController c) {
        try {
            return c.getMyScene().getFocusOwner();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean textInputFocus(BaseController c) {
        Node focused = getFocus(c);
        if (focused == null) {
            return false;
        }
        if (focused instanceof TextInputControl) {
            return true;
        }
        if (focused instanceof ComboBox) {
            ComboBox cb = (ComboBox) focused;
            return cb.isEditable();
        }
        if (focused instanceof WebView) {
            WebView wb = (WebView) focused;
            Parent p = wb.getParent();
            while (p != null) {
                if (p instanceof HTMLEditor) {
                    return true;
                }
                p = p.getParent();
            }
        }
        return false;
    }

    public static double getX(Node node) {
        return node.getScene().getWindow().getX() + node.getScene().getX()
                + node.localToScene(0, 0).getX();
    }

    public static double getY(Node node) {
        return node.getScene().getWindow().getY() + node.getScene().getY()
                + node.localToScene(0, 0).getY();
    }

    public static double getWidth(Control control) {
        return control.getBoundsInParent().getWidth();
    }

    public static double getHeight(Control control) {
        return control.getBoundsInParent().getHeight();
    }

    public static void moveXCenter(Region pNnode, Node node) {
        if (node == null || pNnode == null) {
            return;
        }
        double xOffset = pNnode.getBoundsInParent().getWidth() - node.getBoundsInParent().getWidth();
        if (xOffset > 0) {
            node.setLayoutX(xOffset / 2);
        } else {
            node.setLayoutX(0);
        }
    }

    public static void moveYCenter(Region pNnode, Node node) {
        if (node == null || pNnode == null) {
            return;
        }
        double yOffset = pNnode.getBoundsInParent().getHeight() - node.getBoundsInParent().getHeight();
        if (yOffset > 0) {
            node.setLayoutY(yOffset / 2);
        } else {
            node.setLayoutY(0);
        }
    }

    public static void moveCenter(Region pNnode, Node node) {
        moveXCenter(pNnode, node);
        moveYCenter(pNnode, node);
    }

    public static void moveXCenter(Node node) {
        if (node == null) {
            return;
        }
        double xOffset = node.getBoundsInParent().getWidth() - node.getBoundsInParent().getWidth();
        if (xOffset > 0) {
            node.setLayoutX(xOffset / 2);
        } else {
            node.setLayoutX(0);
        }
    }

    public static void moveYCenter(Node node) {
        if (node == null) {
            return;
        }
        double yOffset = node.getBoundsInParent().getHeight() - node.getBoundsInParent().getHeight();
        if (yOffset > 0) {
            node.setLayoutY(yOffset / 2);
        } else {
            node.setLayoutY(0);
        }
    }

    public static void moveCenter(Node node) {
        moveXCenter(node);
        moveYCenter(node);
    }

    public static void paneSize(ScrollPane sPane, ImageView iView) {
        try {
            if (iView == null || iView.getImage() == null
                    || sPane == null) {
                return;
            }
            iView.setFitWidth(sPane.getBoundsInLocal().getWidth() - 40);
            iView.setFitHeight(sPane.getBoundsInLocal().getHeight() - 40);
            FxmlControl.moveCenter(sPane, iView);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    public static void imageSize(ScrollPane sPane, ImageView iView) {
        try {
            if (iView == null || iView.getImage() == null
                    || sPane == null) {
                return;
            }
            iView.setFitWidth(iView.getImage().getWidth());
            iView.setFitHeight(iView.getImage().getHeight());
            FxmlControl.moveCenter(sPane, iView);
//            MyBoxLog.console(iView.getImage().getWidth() + " " + iView.getImage().getHeight());
//            iView.setLayoutY(10);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    public static void zoomIn(ScrollPane sPane, ImageView iView, int xZoomStep, int yZoomStep) {
        double currentWidth = iView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = iView.getImage().getWidth();
        }
        iView.setFitWidth(currentWidth + xZoomStep);
        double currentHeight = iView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = iView.getImage().getHeight();
        }
        iView.setFitHeight(currentHeight + yZoomStep);
        FxmlControl.moveCenter(sPane, iView);
    }

    public static void zoomOut(ScrollPane sPane, ImageView iView, int xZoomStep, int yZoomStep) {
        double currentWidth = iView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = iView.getImage().getWidth();
        }
        if (currentWidth <= xZoomStep) {
            return;
        }
        iView.setFitWidth(currentWidth - xZoomStep);
        double currentHeight = iView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = iView.getImage().getHeight();
        }
        if (currentHeight <= yZoomStep) {
            return;
        }
        iView.setFitHeight(currentHeight - yZoomStep);
        FxmlControl.moveCenter(sPane, iView);
    }

    // https://stackoverflow.com/questions/38599588/javafx-stage-setmaximized-only-works-once-on-mac-osx-10-9-5
    public static void setMaximized(Stage stage, boolean max) {
        stage.setMaximized(max);
        if (max) {
            Rectangle2D primaryScreenBounds = getScreen();
            stage.setX(primaryScreenBounds.getMinX());
            stage.setY(primaryScreenBounds.getMinY());
            stage.setWidth(primaryScreenBounds.getWidth());
            stage.setHeight(primaryScreenBounds.getHeight());
        }
    }

    public static Rectangle2D getScreen() {
        return Screen.getPrimary().getVisualBounds();
    }

    public static void mouseCenter() {
        Rectangle2D screen = FxmlControl.getScreen();
        try {
            Robot robot = new Robot();
            robot.mouseMove((int) screen.getWidth() / 2, (int) screen.getHeight() / 2);
        } catch (Exception e) {
        }
    }

    public static void mouseCenter(Stage stage) {
        try {
            Robot robot = new Robot();
            robot.mouseMove((int) (stage.getX() + stage.getWidth() / 2), (int) (stage.getY() + stage.getHeight() / 2));
        } catch (Exception e) {
        }
    }

    public static void locateCenter(Stage stage, Node node) {
        if (stage == null || node == null) {
            return;
        }
        Rectangle2D screen = FxmlControl.getScreen();
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        double centerX = bounds.getMinX() - stage.getWidth() / 2;
        centerX = Math.min(screen.getWidth(), Math.max(0, centerX));
        stage.setX(centerX);

        double centerY = bounds.getMinY() - stage.getHeight() / 2;
        centerY = Math.min(screen.getHeight(), Math.max(0, centerY));
        stage.setY(centerY);
    }

    public static void locateCenter(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
    }

    public static void locateBelow(Node node, PopupWindow window) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        window.show(node, bounds.getMinX() + 2, bounds.getMinY() + bounds.getHeight());
    }

    public static void locateBelow(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + 2, bounds.getMinY() + bounds.getHeight());
    }

    public static void locateRightTop(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMaxX() - window.getWidth() - 20, bounds.getMinY() + 50);
    }

    public static void locateUp(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + 2, bounds.getMinY() - 50);
    }

    public static void locateRight(Stage stage) {
        Rectangle2D screen = getScreen();
        stage.setX(screen.getWidth() - stage.getWidth());
    }

    public static List<Node> traverseNode(Node node, List<Node> children) {
        if (node == null) {
            return children;
        }
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(node);
        if (node instanceof Parent) {
            for (Node c : ((Parent) node).getChildrenUnmodifiable()) {
                traverseNode(c, children);
            }
        }
        return children;
    }

    // https://stackoverflow.com/questions/11552176/generating-a-mouseevent-in-javafx/11567122?r=SearchResults#11567122
    public static void fireMouseClicked(Node node) {
        try {
            Event.fireEvent(node, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                    0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true,
                    true, true, true, true, true, true, null));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static void popText(String text, String color, String size, Stage stage) {
        try {
            Popup popup = new Popup();
            popup.setAutoHide(true);
            popup.setAutoFix(true);
            Label popupLabel = new Label(text);
            popupLabel.setStyle("-fx-background-color:black;"
                    + " -fx-text-fill: " + color + ";"
                    + " -fx-font-size: " + size + ";"
                    + " -fx-padding: 10px;"
                    + " -fx-background-radius: 6;");
            popup.getContent().add(popupLabel);

            popup.show(stage);

        } catch (Exception e) {

        }
    }

    // https://stackoverflow.com/questions/31264847/how-to-set-remember-scrollbar-thumb-position-in-javafx-8-webview?r=SearchResults
    public static ScrollBar getVScrollBar(WebView webView) {
        try {
            Set<Node> scrolls = webView.lookupAll(".scroll-bar");
            for (Node scrollNode : scrolls) {
                if (ScrollBar.class.isInstance(scrollNode)) {
                    ScrollBar scroll = (ScrollBar) scrollNode;
                    if (scroll.getOrientation() == Orientation.VERTICAL) {
                        return scroll;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static double dpiScale() {
        try {
            double scale = Toolkit.getDefaultToolkit().getScreenResolution() / Screen.getPrimary().getDpi();
            return scale > 1 ? scale : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    public static Image snap(Node node) {
        try {
            final Bounds bounds = node.getLayoutBounds();
            double scale = dpiScale();
            int imageWidth = (int) Math.round(bounds.getWidth() * scale);
            int imageHeight = (int) Math.round(bounds.getHeight() * scale);
            final SnapshotParameters snapPara = new SnapshotParameters();
            snapPara.setFill(Color.TRANSPARENT);
            snapPara.setTransform(javafx.scene.transform.Transform.scale(scale, scale));
            WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
            snapshot = node.snapshot(snapPara, snapshot);
            return snapshot;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

    // This can set more than 8 colors. javafx only supports 8 colors defined in css
    // This should be called after data have been assigned to pie
    public static void setPieColors(PieChart pie, boolean showLegend) {
        List<String> palette = FxmlColor.randomRGB(pie.getData().size());
        setPieColors(pie, palette, showLegend);
    }

    public static void setPieColors(PieChart pie, List<String> palette, boolean showLegend) {
        if (pie == null || palette == null
                || pie.getData() == null
                || pie.getData().size() > palette.size()) {
            return;
        }
        for (int i = 0; i < pie.getData().size(); i++) {
            PieChart.Data data = pie.getData().get(i);
            data.getNode().setStyle("-fx-pie-color: " + palette.get(i) + ";");
        }
        pie.setLegendVisible(showLegend);
        if (showLegend) {
            Set<Node> legendItems = pie.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    for (int i = 0; i < pie.getData().size(); i++) {
                        String name = pie.getData().get(i).getName();
                        if (name.equals(legendLabel.getText())) {
                            legend.setStyle("-fx-background-color: " + palette.get(i));
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void setLineChartColors(LineChart chart, Map<String, String> locationColors, boolean showLegend) {
        if (chart == null || locationColors == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null
                || seriesList.size() > locationColors.size()) {
            return;
        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYChart.Series series = seriesList.get(i);
            Node node = series.getNode().lookup(".chart-series-line");
            if (node != null) {
                String name = series.getName();
                String color = locationColors.get(name);
                if (color == null) {
                    MyBoxLog.debug(name);
                } else {
                    node.setStyle("-fx-stroke: " + color + ";");
                }
            }
        }
        chart.setLegendVisible(showLegend);
        if (showLegend) {
            Set<Node> legendItems = chart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    for (int i = 0; i < seriesList.size(); i++) {
                        String name = seriesList.get(i).getName();
                        if (name.equals(legendLabel.getText())) {
                            String color = locationColors.get(name);
                            if (color == null) {
                                MyBoxLog.debug(name);
                            } else {
                                legend.setStyle("-fx-background-color: " + color);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void setBarChartColors(BarChart chart, boolean showLegend) {
        List<String> palette = FxmlColor.randomRGB(chart.getData().size());
        setBarChartColors(chart, palette, showLegend);
    }

    public static void setBarChartColors(BarChart chart, List<String> palette, boolean showLegend) {
        if (chart == null || palette == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null
                || seriesList.size() > palette.size()) {
            return;
        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYChart.Series series = seriesList.get(i);
            if (series.getData() == null) {
                continue;
            }
            for (int j = 0; j < series.getData().size(); j++) {
                XYChart.Data item = (XYChart.Data) series.getData().get(j);
                if (item.getNode() != null) {
                    String color = palette.get(i);
                    item.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        }
        chart.setLegendVisible(showLegend);
        if (showLegend) {
            Set<Node> legendItems = chart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    for (int i = 0; i < seriesList.size(); i++) {
                        if (seriesList.get(i).getName().equals(legendLabel.getText())) {
                            legend.setStyle("-fx-background-color: " + palette.get(i));
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void setBarChartColors(BarChart chart, Map<String, String> palette, boolean showLegend) {
        if (chart == null || palette == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null) {
            return;
        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYChart.Series series = seriesList.get(i);
            if (series.getData() == null) {
                continue;
            }
            for (int j = 0; j < series.getData().size(); j++) {
                XYChart.Data item = (XYChart.Data) series.getData().get(j);
                if (item.getNode() != null) {
                    String color = palette.get(series.getName());
                    item.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        }
        chart.setLegendVisible(showLegend);
        if (showLegend) {
            Set<Node> legendItems = chart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    for (int i = 0; i < seriesList.size(); i++) {
                        String name = seriesList.get(i).getName();
                        String color = palette.get(name);
                        if (color != null && name.equals(legendLabel.getText())) {
                            legend.setStyle("-fx-background-color: " + color);
                        }
                    }
                }
            }
        }
    }

    public static ContextMenu popEraExample(ContextMenu inPopMenu, TextField input, MouseEvent mouseEvent) {
        try {
            if (inPopMenu != null && inPopMenu.isShowing()) {
                inPopMenu.hide();
            }
            final ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            List<String> values = new ArrayList<>();
            values.add(DateTools.nowString());
            values.add(DateTools.datetimeToString(new Date(), CommonValues.DatetimeMs, TimeZone.getDefault()));
            values.add(DateTools.datetimeToString(new Date(), CommonValues.TimeMs, TimeZone.getDefault()));
            values.add(DateTools.datetimeToString(new Date(), CommonValues.DatetimeMs + " Z", TimeZone.getDefault()));
            values.addAll(Arrays.asList(
                    "2020-07-15T36:55:09", "960-01-23", "581",
                    "-2020-07-10 10:10:10.532 +0800", "-960-01-23", "-581"
            ));
            if (AppVariables.isChinese()) {
                values.addAll(Arrays.asList(
                        "公元960", "公元960-01-23", "公元2020-07-10 10:10:10",
                        "公元前202", "公元前770-12-11", "公元前1046-03-10 10:10:10"
                ));
            }
            values.addAll(Arrays.asList(
                    "202 BC", "770-12-11 BC", "1046-03-10 10:10:10 BC",
                    "581 AD", "960-01-23 AD", "2020-07-10 10:10:10 AD"
            ));

            MenuItem menu;
            for (String value : values) {
                menu = new MenuItem(value);
                menu.setOnAction((ActionEvent event) -> {
                    input.setText(value);
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

            return popMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ContextMenu popRegexExample(BaseController parent, ContextMenu inPopMenu,
            TextInputControl input, MouseEvent mouseEvent) {
        try {
            if (inPopMenu != null && inPopMenu.isShowing()) {
                inPopMenu.hide();
            }
            final ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            List<String> values = Arrays.asList(
                    "^      " + message("StartLocation"),
                    "$      " + message("EndLocation"),
                    "*      " + message("ZeroOrNTimes"),
                    "+      " + message("OneOrNTimes"),
                    "?      " + message("ZeroOrOneTimes"),
                    "{n}      " + message("NTimes"),
                    "{n,}      " + message("N+Times"),
                    "{n,m}      " + message("NMTimes"),
                    "[abc]      " + message("MatchTheseCharacters"),
                    "[A-Z]      " + message("A-Z"),
                    "|      " + message("Or"),
                    "\\s      " + message("NonprintableCharacter"),
                    "\\S      " + message("PrintableCharacter"),
                    "\\n      " + message("LineBreak"),
                    "\\r      " + message("CarriageReturn"),
                    "\\t      " + message("Tab"),
                    "[0-9]{n}      " + message("NNumber"),
                    "[A-Z]{n}      " + message("NUppercase"),
                    "[a-z]{n}      " + message("NLowercase"),
                    "[\\u4e00-\\u9fa5]      " + message("Chinese"),
                    "[^\\x00-\\xff]      " + message("DoubleByteCharacter"),
                    "[A-Za-z0-9]+      " + message("EnglishAndNumber"),
                    "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*      " + message("Email"),
                    "(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}      " + message("PhoneNumber"),
                    "[a-zA-z]+://[^\\s]*       " + message("URL"),
                    "^(\\s*)\\n       " + message("BlankLine"),
                    "\\d+\\.\\d+\\.\\d+\\.\\d+      " + message("IP")
            );

            MenuItem menu;
            for (String value : values) {
                menu = new MenuItem(value);
                menu.setOnAction((ActionEvent event) -> {
                    String[] vv = value.split("   ");
                    input.appendText(vv[0]);
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("AboutRegularExpression"));
            menu.setOnAction((ActionEvent event) -> {
                parent.regexHelp();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

            return popMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static DoublePoint getImageXY(MouseEvent event, ImageView view) {
        if (event == null || view.getImage() == null) {
            return null;
        }
        double offsetX = event.getX() - view.getLayoutX() - view.getX();
        double offsetY = event.getY() - view.getLayoutY() - view.getY();
        if (offsetX < 0 || offsetX >= view.getBoundsInParent().getWidth()
                || offsetY < 0 || offsetY >= view.getBoundsInParent().getHeight()) {
            return null;
        }
        double x = offsetX * view.getImage().getWidth() / view.getBoundsInParent().getWidth();
        double y = offsetY * view.getImage().getHeight() / view.getBoundsInParent().getHeight();
        return new DoublePoint(x, y);
    }

    public static Color imagePixel(MouseEvent event, ImageView view) {
        DoublePoint p = getImageXY(event, view);
        if (p == null) {
            return null;
        }
        return imagePixel(p, view);
    }

    public static Color imagePixel(DoublePoint p, ImageView view) {
        if (p == null || view == null) {
            return null;
        }
        PixelReader pixelReader = view.getImage().getPixelReader();
        return pixelReader.getColor((int) p.getX(), (int) p.getY());
    }

    public static ContextMenu popHtmlStyle(MouseEvent mouseEvent,
            BaseController controller, ContextMenu inPopMenu, WebEngine webEngine) {
        try {
            if (inPopMenu != null && inPopMenu.isShowing()) {
                inPopMenu.hide();
            }
            final ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            String baseName = controller == null ? "" : controller.getBaseName();
            MenuItem menu;
            for (HtmlTools.HtmlStyle style : HtmlTools.HtmlStyle.values()) {
                menu = new MenuItem(message(style.name()));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            AppVariables.setUserConfigValue(baseName + "HtmlStyle", style.name());
                            if (webEngine == null) {
                                return;
                            }
                            Object c = webEngine.executeScript("document.documentElement.outerHTML");
                            if (c == null) {
                                return;
                            }
                            String html = (String) c;
                            html = HtmlTools.setStyle(html, style);
                            webEngine.loadContent(html);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateCenter((Region) mouseEvent.getSource(), popMenu);
            return popMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static boolean copyToSystemClipboard(String string) {
        try {
            if (string == null || string.isBlank()) {
                return false;
            }
            ClipboardContent cc = new ClipboardContent();
            cc.putString(string);
            Clipboard.getSystemClipboard().setContent(cc);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static String getSystemClipboardString() {
        return Clipboard.getSystemClipboard().getString();
    }

    public static String askValue(String title, String header, String name, String initValue) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(name);
        dialog.getEditor().setText(initValue);
        dialog.getEditor().setPrefWidth(initValue == null ? 200 : Math.min(600, initValue.length() * AppVariables.sceneFontSize));
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) {
            return null;
        }
        String value = result.get();
        return value;
    }

    public static boolean askSure(String title, String sureString) {
        return askSure(title, null, sureString);
    }

    public static boolean askSure(String title, String header, String sureString) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        if (header != null) {
            alert.setHeaderText(header);
        }
        alert.setContentText(sureString);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == buttonSure;
    }

    public static String getHtml(WebView webView) {
        if (webView == null) {
            return "";
        }
        return getHtml(webView.getEngine());
    }

    public static String getHtml(WebEngine engine) {
        try {
            if (engine == null) {
                return "";
            }
            Object c = engine.executeScript("document.documentElement.outerHTML");
            if (c == null) {
                return "";
            }
            return (String) c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    public static String getFrame(WebEngine engine, int index) {
        try {
            if (engine == null || index < 0) {
                return "";
            }
            Object c = engine.executeScript("window.frames[" + index + "].document.documentElement.outerHTML");
            if (c == null) {
                return "";
            }
            return (String) c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    public static String getFrame(WebEngine engine, String frameName) {
        try {
            if (engine == null || frameName == null) {
                return "";
            }
            Object c = engine.executeScript("window.frames." + frameName + ".document.documentElement.outerHTML");
            if (c == null) {
                return "";
            }
            return (String) c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    public static Document getFrameDocument(WebEngine engine, String frameName) {
        try {
            if (engine == null) {
                return null;
            }
            Object c = engine.executeScript("window.frames." + frameName + ".document");
            if (c == null) {
                return null;
            }
            return (Document) c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static File selectFile(BaseController controller) {
        return selectFile(controller,
                AppVariables.getUserConfigPath(controller.getSourcePathKey()),
                controller.getSourceExtensionFilter());
    }

    public static File selectFile(BaseController controller, int fileType) {
        return selectFile(controller,
                AppVariables.getUserConfigPath(VisitHistoryTools.getPathKey(fileType)),
                VisitHistoryTools.getExtensionFilter(fileType));
    }

    public static File selectFile(BaseController controller, File path,
            List<FileChooser.ExtensionFilter> filter) {
        try {
            FileChooser fileChooser = new FileChooser();
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(filter);
            File file = fileChooser.showOpenDialog(controller.getMyStage());
            if (file == null || !file.exists()) {
                return null;
            }
            controller.recordFileOpened(file);
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public static String gaodeMap() {
        try {
            File map = FxmlControl.getInternalFile("/js/GaoDeMap.html", "js", "GaoDeMap.html", false);
            String html = FileTools.readTexts(map);
            html = html.replace("06b9e078a51325a843dfefd57ffd876c", AppVariables.getUserConfigValue("GaoDeMapWebKey", CommonValues.GaoDeMapWebKey));
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    public static File tiandituFile(boolean geodetic) {
        try {
            File map = FxmlControl.getInternalFile("/js/tianditu.html", "js", "tianditu.html", false);
            String html = FileTools.readTexts(map);
            html = html.replace("0ddeb917def62b4691500526cc30a9b1", AppVariables.getUserConfigValue("TianDiTuWebKey", CommonValues.TianDiTuWebKey));
            if (geodetic) {
                html = html.replace("'EPSG:900913", "EPSG:4326");
            }
            FileTools.writeFile(map, html);
            return map;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
