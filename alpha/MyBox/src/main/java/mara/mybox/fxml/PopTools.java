package mara.mybox.fxml;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseLogs;
import mara.mybox.controller.ControlWebView;
import mara.mybox.controller.HtmlStyleInputController;
import mara.mybox.controller.MenuController;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.TimeFormats;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class PopTools {

    /*
        common
     */
    public static void browseURI(BaseController controller, URI uri) {
        if (uri == null) {
            return;
        }
        if (SystemTools.isLinux()) {
            // On my CentOS 7, system hangs when both Desktop.isDesktopSupported() and
            // desktop.isSupported(Desktop.Action.BROWSE) are true.
            // https://stackoverflow.com/questions/27879854/desktop-getdesktop-browse-hangs
            // Below workaround for Linux because "Desktop.getDesktop().browse()" doesn't work on some Linux implementations
            try {
                if (Runtime.getRuntime().exec(new String[]{"which", "xdg-open"}).getInputStream().read() > 0) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", uri.toString()});
                    return;
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }

        } else if (SystemTools.isMac()) {
            // https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java/28807079#28807079
            try {
                Runtime.getRuntime().exec(new String[]{"open", uri.toString()});
                return;
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
        } else if (Desktop.isDesktopSupported()) {
            // https://stackoverflow.com/questions/23176624/javafx-freeze-on-desktop-openfile-desktop-browseuri?r=SearchResults
            // interface are blocked after system explorer is opened. Happened again after javafx 17.0.2
//            new Thread(() -> {
//                try {
//                    Desktop.getDesktop().browse(uri);
//                } catch (Exception e) {
//                    MyBoxLog.debug(e);
//                }
//            }).start();
            try {
                Runtime.getRuntime().exec(new String[]{"explorer.exe", uri.toString()});
                return;
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            return;
        }
        if (!uri.getScheme().equals("file") || new File(uri.getPath()).isFile()) {
            ControllerTools.openTarget(uri.toString());
        } else {
            alertError(controller, message("DesktopNotSupportBrowse"));
        }
    }

    public static Alert alert(BaseController controller, Alert.AlertType type, String information) {
        try {
            Alert alert = new Alert(type);
            if (controller != null) {
                alert.setTitle(controller.getTitle());
            }
            alert.setHeaderText(null);
            alert.setContentText(information);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().applyCss();
            // https://stackoverflow.com/questions/38799220/javafx-how-to-bring-dialog-alert-to-the-front-of-the-screen?r=SearchResults
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            stage.sizeToScene();
            alert.showAndWait();
            return alert;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Alert alertInformation(BaseController controller, String information) {
        return alert(controller, Alert.AlertType.INFORMATION, information);
    }

    public static Alert alertWarning(BaseController controller, String information) {
        return alert(controller, Alert.AlertType.WARNING, information);
    }

    public static Alert alertError(BaseController controller, String information) {
        return alert(controller, Alert.AlertType.ERROR, information);
    }

    public static String askValue(String title, String header, String name, String initValue) {
        return askValue(title, header, name, initValue, 400);
    }

    public static String askValue(String title, String header, String name, String initValue, int minWidth) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(name);
        dialog.getEditor().setText(initValue);
        dialog.getEditor().setPrefWidth(initValue == null ? minWidth : Math.min(minWidth, initValue.length() * AppVariables.sceneFontSize));
        dialog.getEditor().selectEnd();
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        stage.getScene().getRoot().requestFocus();
        Optional<String> result = dialog.showAndWait();
        if (result == null || !result.isPresent()) {
            return null;
        }
        String value = result.get();
        return value;
    }

    public static boolean askSure(String title, String sureString) {
        return askSure(title, null, sureString);
    }

    // https://openjfx.io/javadoc/17/javafx.controls/javafx/scene/control/Dialog.html
    public static boolean askSure(String title, String header, String sureString) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        if (header != null) {
            alert.setHeaderText(header);
        }
        alert.setContentText(sureString);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(message("Sure"));
        ButtonType buttonCancel = new ButtonType(message("Cancel"), ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<ButtonType> result = alert.showAndWait();
        return result != null && result.isPresent() && result.get() == buttonSure;
    }

    public static Popup makePopWindow(BaseController parent, String fxml) {
        try {
            BaseController controller = WindowTools.loadFxml(fxml);
            if (controller == null) {
                return null;
            }
            Popup popup = new Popup();
            popup.setAutoHide(true);
            popup.getContent().add(controller.getMyScene().getRoot());
            popup.setUserData(controller);
            popup.setOnHiding((WindowEvent event) -> {
                WindowTools.closeWindow(popup);
            });
            controller.setParentController(parent);
            controller.setMyWindow(popup);
            if (parent != null) {
                parent.closePopup();
                parent.setPopup(popup);
            }
            return popup;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Popup popWindow(BaseController parent, String fxml, Node owner, double x, double y) {
        try {
            Popup popup = makePopWindow(parent, fxml);
            if (popup == null) {
                return null;
            }
            popup.show(owner, x, y);
            return popup;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void showError(BaseController controller, String error) {
        if (controller != null) {
            if (controller instanceof BaseLogs) {
                ((BaseLogs) controller).updateLogs(error, true, true);
            } else {
                Platform.runLater(() -> {
                    controller.alertError(error);
                });
            }
        } else {
            MyBoxLog.error(error);
        }
    }

    /*
        buttons
     */
    public static void addButtonsPane(MenuController controller, TextInputControl input,
            List<String> values, String menuName) {
        addButtonsPane(controller, input, values, -1, menuName);
    }

    public static void addButtonsPane(MenuController controller, TextInputControl input,
            List<String> values, int index, String menuName) {
        try {
            List<Node> buttons = new ArrayList<>();
            for (String value : values) {
                if (value == null) {
                    continue;
                }
                Button button = new Button(value.length() > 200 ? value.substring(0, 200) + " ..." : value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.replaceText(input.getSelection(), value);
                        if (UserConfig.getBoolean(menuName + "ValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
                        }
                        input.requestFocus();
                    }
                });
                buttons.add(button);
            }
            controller.addFlowPane(buttons);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        style
     */
    public static ContextMenu popHtmlStyle(Event event, ControlWebView controller) {
        try {
            if (event == null || controller == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();

            String baseName = controller.getBaseName();

            MenuItem menu = new MenuItem(message("HtmlStyle"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            ToggleGroup sgroup = new ToggleGroup();
            String prefix = UserConfig.getBoolean(baseName + "ShareHtmlStyle", true) ? "AllInterface" : baseName;
            String currentStyle = UserConfig.getString(prefix + "HtmlStyle", null);

            RadioMenuItem rmenu = new RadioMenuItem(message("None"));
            rmenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    controller.setStyle(null);
                }
            });
            rmenu.setSelected(currentStyle == null);
            rmenu.setToggleGroup(sgroup);
            items.add(rmenu);

            boolean predefinedValue = false;
            for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
                rmenu = new RadioMenuItem(message(style.name()));
                String styleValue = HtmlStyles.styleValue(style);
                rmenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        controller.setStyle(styleValue);
                    }
                });
                boolean isCurrent = currentStyle != null && currentStyle.equals(styleValue);
                rmenu.setSelected(isCurrent);
                rmenu.setToggleGroup(sgroup);
                items.add(rmenu);
                if (isCurrent) {
                    predefinedValue = true;
                }
            }

            rmenu = new RadioMenuItem(message("Input") + "...");
            rmenu.setOnAction(new EventHandler<ActionEvent>() {
                ChangeListener<Boolean> getListener;

                @Override
                public void handle(ActionEvent event) {
                    HtmlStyleInputController inputController = HtmlStyleInputController.open(controller,
                            message("Style"), UserConfig.getString(prefix + "HtmlStyle", null));
                    getListener = new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            String value = inputController.getInputString();
                            if (value == null || value.isBlank()) {
                                value = null;
                            }
                            inputController.getNotify().removeListener(getListener);
                            controller.setStyle(value);
                            inputController.closeStage();
                        }
                    };
                    inputController.getNotify().addListener(getListener);
                }
            });
            rmenu.setSelected(currentStyle != null && !predefinedValue);
            rmenu.setToggleGroup(sgroup);
            items.add(rmenu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("ShareAllInterface"));
            checkMenu.setSelected(UserConfig.getBoolean(baseName + "ShareHtmlStyle", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ShareHtmlStyle", checkMenu.isSelected());
                }
            });
            items.add(checkMenu);

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("HtmlStylesPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("HtmlStylesPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            controller.popEventMenu(event, items);
            return controller.getPopMenu();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ContextMenu popWindowStyles(BaseController parent, String baseStyle, Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();

            String baseName = parent.getBaseName();
            MenuItem menu = new MenuItem(message("WindowStyle"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            Map<String, String> styles = new LinkedHashMap<>();
            styles.put("None", "");
            styles.put("Transparent", "; -fx-text-fill: black; -fx-background-color: transparent;");
            styles.put("Console", "; -fx-text-fill: #CCFF99; -fx-background-color: black;");
            styles.put("Blackboard", "; -fx-text-fill: white; -fx-background-color: #336633;");
            styles.put("Ago", "; -fx-text-fill: white; -fx-background-color: darkblue;");
            styles.put("Book", "; -fx-text-fill: black; -fx-background-color: #F6F1EB;");
            ToggleGroup sgroup = new ToggleGroup();
            String prefix = UserConfig.getBoolean(baseName + "ShareWindowStyle", true) ? "AllInterface" : baseName;
            String currentStyle = UserConfig.getString(prefix + "WindowStyle", "");

            for (String name : styles.keySet()) {
                RadioMenuItem rmenu = new RadioMenuItem(message(name));
                String style = styles.get(name);
                rmenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setString(prefix + "WindowStyle", style);
                        parent.getThisPane().setStyle(baseStyle + style);
                        setMenuLabelsStyle(parent.getThisPane(), baseStyle + style);
                    }
                });
                rmenu.setSelected(currentStyle != null && currentStyle.equals(style));
                rmenu.setToggleGroup(sgroup);
                items.add(rmenu);
            }
            items.add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("ShareAllInterface"));
            checkMenu.setSelected(UserConfig.getBoolean(baseName + "ShareWindowStyle", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ShareWindowStyle", checkMenu.isSelected());
                }
            });
            items.add(checkMenu);

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("WindowStylesPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("WindowStylesPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            parent.popEventMenu(event, items);
            return parent.getPopMenu();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void setMenuLabelsStyle(Node node, String style) {
        if (node instanceof Label) {
            node.setStyle(style);
        } else if (node instanceof Parent && !(node instanceof TableView)) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                setMenuLabelsStyle(child, style);
            }
        }
    }

    public static void setWindowStyle(Pane pane, String baseName, String baseStyle) {
        String prefix = UserConfig.getBoolean(baseName + "ShareWindowStyle", true) ? "AllInterface" : baseName;
        String style = UserConfig.getString(prefix + "WindowStyle", "");
        pane.setStyle(baseStyle + style);
        setMenuLabelsStyle(pane, baseStyle + style);
    }

    /*
        saved values
     */
    public static void popStringValues(BaseController parent, TextInputControl input, Event event,
            String menuName, boolean alwaysClear, boolean checkPop) {
        try {
            int max = UserConfig.getInt(menuName + "MaxSaved", 20);
            MenuController controller = MenuController.open(parent, input, event);

            List<Node> setButtons = new ArrayList<>();
            Button clearInputButton = new Button(message("ClearInputArea"));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });
            setButtons.add(clearInputButton);

            Button clearValuesButton = new Button(message("ClearValues"));
            clearValuesButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent aevent) {
                    TableStringValues.clear(menuName);
                    controller.close();
                    popStringValues(parent, input, aevent, menuName, alwaysClear, checkPop);
                }
            });
            setButtons.add(clearValuesButton);

            Button maxButton = new Button(message("MaxSaved"));
            maxButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent aevent) {
                    String value = PopTools.askValue(parent.getTitle(), null, message("MaxSaved"), max + "");
                    if (value == null) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(value);
                        UserConfig.setInt(menuName + "MaxSaved", v);
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                    }
                }
            });
            setButtons.add(maxButton);

            if (alwaysClear) {
                UserConfig.setBoolean(menuName + "ValuesClearAndSet", true);
            } else {
                CheckBox clearCheck = new CheckBox();
                clearCheck.setGraphic(StyleTools.getIconImageView("iconClear.png"));
                NodeStyleTools.setTooltip(clearCheck, new Tooltip(message("ClearAndPaste")));
                clearCheck.setSelected(UserConfig.getBoolean(menuName + "ValuesClearAndSet", false));
                clearCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(menuName + "ValuesClearAndSet", clearCheck.isSelected());
                    }
                });
                setButtons.add(clearCheck);
            }

            CheckBox closeCheck = new CheckBox();
            closeCheck.setGraphic(StyleTools.getIconImageView("iconClose.png"));
            NodeStyleTools.setTooltip(closeCheck, new Tooltip(message("CloseAfterPaste")));
            closeCheck.setSelected(UserConfig.getBoolean(menuName + "ValuesCloseAfterPaste", true));
            closeCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent aevent) {
                    UserConfig.setBoolean(menuName + "ValuesCloseAfterPaste", closeCheck.isSelected());
                }
            });
            setButtons.add(closeCheck);

            if (checkPop) {
                CheckBox popCheck = new CheckBox();
                popCheck.setGraphic(StyleTools.getIconImageView("iconPop.png"));
                NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWindowWhenMouseHovering")));
                popCheck.setSelected(UserConfig.getBoolean(menuName + "PopWhenMouseHovering", false));
                popCheck.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent aevent) {
                        UserConfig.setBoolean(menuName + "PopWhenMouseHovering", popCheck.isSelected());
                    }
                });
                setButtons.add(popCheck);
            }

            controller.addFlowPane(setButtons);
            controller.addNode(new Separator());
            controller.addNode(new Label(message("PopValuesComments")));

            List<String> values = TableStringValues.max(menuName, max);
            List<Node> buttons = new ArrayList<>();
            for (String value : values) {
                Button button = new Button(StringTools.start(value, 200));
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent aevent) {
                        if (UserConfig.getBoolean(menuName + "ValuesClearAndSet", true)) {
                            input.setText(value);
                        } else {
                            input.replaceText(input.getSelection(), value);
                        }
                        if (UserConfig.getBoolean(menuName + "ValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
                        }
                        input.requestFocus();
                    }
                });
                button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent aevent) {
                        if (aevent.getButton() == MouseButton.SECONDARY) {
                            TableStringValues.delete(menuName, value);
                            controller.close();
                            popStringValues(parent, input, event, menuName, alwaysClear, checkPop);
                        }
                    }
                });
                buttons.add(button);
            }
            controller.addFlowPane(buttons);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static void popStringValues(BaseController parent, TextInputControl input, Event event, String menuName) {
        popStringValues(parent, input, event, menuName, false, true);
    }

    /*
        examples
     */
    public static ContextMenu popDatetimeExamples(BaseController parent, ContextMenu inPopMenu,
            TextField input, MouseEvent mouseEvent) {
        try {
            List<String> values = new ArrayList<>();
            Date d = new Date();
            values.add(DateTools.datetimeToString(d, TimeFormats.Datetime));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMs));
            values.add(DateTools.datetimeToString(d, TimeFormats.Date));
            values.add(DateTools.datetimeToString(d, TimeFormats.Month));
            values.add(DateTools.datetimeToString(d, TimeFormats.Year));
            values.add(DateTools.datetimeToString(d, TimeFormats.TimeMs));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeZone));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeC));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMsC));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateC));
            values.add(DateTools.datetimeToString(d, TimeFormats.MonthC));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeZoneC));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeE));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMsE));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateE));
            values.add(DateTools.datetimeToString(d, TimeFormats.MonthE));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeZoneE));
            values.addAll(Arrays.asList(
                    "2020-07-15T36:55:09", "2020-07-10T10:10:10.532 +0800"
            ));
            return popDateMenu(parent, inPopMenu, input, mouseEvent, values);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ContextMenu popDateExamples(BaseController parent, ContextMenu inPopMenu,
            TextField input, MouseEvent mouseEvent) {
        try {
            List<String> values = new ArrayList<>();
            Date d = new Date();
            values.add(DateTools.datetimeToString(d, TimeFormats.Date));
            values.add(DateTools.datetimeToString(d, TimeFormats.Month));
            values.add(DateTools.datetimeToString(d, TimeFormats.Year));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateC));
            values.add(DateTools.datetimeToString(d, TimeFormats.MonthC));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateE));
            values.add(DateTools.datetimeToString(d, TimeFormats.MonthE));
            return popDateMenu(parent, inPopMenu, input, mouseEvent, values);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void popEraExamples(BaseController parent, TextField input, MouseEvent mouseEvent) {
        try {
            List<String> values = new ArrayList<>();
            Date d = new Date();
            values.add(DateTools.datetimeToString(d, TimeFormats.Datetime + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMs + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, TimeFormats.Date + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, TimeFormats.Month + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, "G" + TimeFormats.DateA, Locale.ENGLISH, null));

            Date bc = DateTools.encodeDate("770-3-9 12:56:33.498 BC");
            values.add(DateTools.datetimeToString(bc, TimeFormats.DatetimeA + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(bc, TimeFormats.Date + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(bc, "G" + TimeFormats.MonthA, Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(bc, "G" + TimeFormats.YearA, Locale.ENGLISH, null));

            if (Languages.isChinese()) {
                values.add(DateTools.datetimeToString(d, TimeFormats.Datetime + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMs + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(d, TimeFormats.Date + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(d, TimeFormats.Month + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(d, "G" + TimeFormats.DateA, Locale.CHINESE, null));

                values.add(DateTools.datetimeToString(bc, TimeFormats.DatetimeA + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(bc, TimeFormats.DateA + " G", Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(bc, "G" + TimeFormats.MonthA, Locale.CHINESE, null));
                values.add(DateTools.datetimeToString(bc, "G" + TimeFormats.YearA, Locale.CHINESE, null));
            }

            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMsC + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateC + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, "G" + TimeFormats.MonthC, Locale.ENGLISH, null));

            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeMsB + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, TimeFormats.DateB + " G", Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, "G" + TimeFormats.DatetimeB, Locale.ENGLISH, null));
            values.add(DateTools.datetimeToString(d, "G" + TimeFormats.MonthB, Locale.ENGLISH, null));

            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeZone));
            values.add(DateTools.datetimeToString(d, TimeFormats.DatetimeZoneE));
            values.addAll(Arrays.asList(
                    "2020-07-15T36:55:09", "2020-07-10T10:10:10.532 +0800"
            ));

            MenuController controller = MenuController.open(parent, input, mouseEvent.getScreenX(), mouseEvent.getScreenY());

            List<Node> nodes = new ArrayList<>();
            for (String value : values) {
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.setText(value);
                        controller.close();
                    }
                });
                nodes.add(button);
            }
            controller.addFlowPane(nodes);

            Hyperlink link = new Hyperlink("DateFormat");
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    parent.openLink(HelpTools.simpleDateFormatLink());
                }
            });
            controller.addNode(link);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static ContextMenu popDateMenu(BaseController parent, ContextMenu inPopMenu,
            TextField input, MouseEvent mouseEvent, List<String> values) {
        try {
            if (inPopMenu != null && inPopMenu.isShowing()) {
                inPopMenu.hide();
            }
            if (values == null || values.isEmpty()) {
                return inPopMenu;
            }
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu;
            for (String value : values) {
                menu = new MenuItem(value);
                menu.setOnAction((ActionEvent event) -> {
                    input.setText(value);
                    input.requestFocus();
                });
                items.add(menu);
            }
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("DateFormat"));
            menu.setStyle("-fx-text-fill: blue;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    parent.openLink(HelpTools.simpleDateFormatLink());
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            parent.popEventMenu(mouseEvent, items);
            return parent.getPopMenu();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void popRegexExamples(BaseController parent, TextInputControl input, Event event) {
        try {
            MenuController controller = MenuController.open(parent, input, event);

            List<Node> topButtons = new ArrayList<>();
            Button clearButton = new Button(message("Clear"));
            NodeStyleTools.setTooltip(clearButton, new Tooltip(message("ClearInputArea")));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                    controller.getThisPane().requestFocus();
                    input.requestFocus();
                }
            });
            topButtons.add(clearButton);

            CheckBox closeCheck = new CheckBox();
            closeCheck.setGraphic(StyleTools.getIconImageView("iconClose.png"));
            NodeStyleTools.setTooltip(closeCheck, new Tooltip(message("CloseAfterPaste")));
            closeCheck.setSelected(UserConfig.getBoolean("RegexExamplesValuesCloseAfterPaste", true));
            closeCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent aevent) {
                    UserConfig.setBoolean("RegexExamplesValuesCloseAfterPaste", closeCheck.isSelected());
                }
            });
            topButtons.add(closeCheck);

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImageView("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWindowWhenMouseHovering")));
            popCheck.setSelected(UserConfig.getBoolean("RegexExamplesPopWhenMouseHovering", false));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("RegexExamplesPopWhenMouseHovering", popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);
            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            List<String> values = Arrays.asList("^      " + message("StartLocation"),
                    "$      " + message("EndLocation"),
                    "*      " + message("ZeroOrNTimes"),
                    "+      " + message("OneOrNTimes"),
                    "?      " + message("ZeroOrOneTimes"),
                    "{n}      " + message("NTimes"),
                    "{n,}      " + message("N+Times"),
                    "{n,m}      " + message("NMTimes"),
                    "|      " + message("Or"),
                    "[abc]      " + message("MatchOneCharacters"),
                    "[A-Z]      " + message("A-Z"),
                    "\\x20      " + message("Blank"),
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
                    "\\d+\\.\\d+\\.\\d+\\.\\d+      " + message("IP"),
                    "line1\\s*\\nline2      " + message("MultipleLines"));
            List<Node> nodes = new ArrayList<>();
            for (String value : values) {
                String[] vv = value.split("      ");
                Button button = new Button(vv[1].trim());
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.replaceText(input.getSelection(), vv[0]);
                        if (UserConfig.getBoolean("RegexExamplesValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
                        }
                        input.requestFocus();
                    }
                });
                NodeStyleTools.setTooltip(button, new Tooltip(vv[0]));
                nodes.add(button);
            }
            controller.addFlowPane(nodes);

            Hyperlink link = new Hyperlink(message("AboutRegularExpression"));
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (Languages.isChinese()) {
                        parent.openLink("https://baike.baidu.com/item/%E6%AD%A3%E5%88%99%E8%A1%A8%E8%BE%BE%E5%BC%8F/1700215");
                    } else {
                        parent.openLink("https://en.wikipedia.org/wiki/Regular_expression");
                    }
                }
            });
            controller.addNode(link);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void popColorExamples(BaseController parent, TextInputControl input, Event event) {
        try {
            MenuController controller = MenuController.open(parent, input, event);

            List<Node> topButtons = new ArrayList<>();

            Button clearButton = new Button(message("ClearInputArea"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });
            topButtons.add(clearButton);

            CheckBox closeCheck = new CheckBox();
            closeCheck.setGraphic(StyleTools.getIconImageView("iconClose.png"));
            NodeStyleTools.setTooltip(closeCheck, new Tooltip(message("CloseAfterPaste")));
            closeCheck.setSelected(UserConfig.getBoolean("ColorExamplesValuesCloseAfterPaste", true));
            closeCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent aevent) {
                    UserConfig.setBoolean("ColorExamplesValuesCloseAfterPaste", closeCheck.isSelected());
                }
            });
            topButtons.add(closeCheck);

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImageView("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWindowWhenMouseHovering")));
            popCheck.setSelected(UserConfig.getBoolean("ColorExamplesPopWhenMouseHovering", false));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("ColorExamplesPopWhenMouseHovering", popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);
            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(
                    "orange", "pink", "lightblue", "wheat",
                    "0xff668840", "0x5f86df", "#226688", "#68f",
                    "rgb(255,102,136)", "rgb(100%,60%,50%)",
                    "rgba(102,166,136,0.25)", "rgba(155,20%,70%,0.25)",
                    "hsl(240,70%,80%)", "hsla(60,50%,60%,0.25)"
            ));

            boolean isTextArea = input instanceof TextArea;
            List<Node> nodes = new ArrayList<>();
            for (String value : values) {
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (isTextArea) {
                            input.appendText(value + "\n");
                        } else {
                            input.setText(value);
                        }
                        if (UserConfig.getBoolean("ColorExamplesValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
                        }
                        input.requestFocus();
                    }
                });
                nodes.add(button);
            }
            controller.addFlowPane(nodes);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void popSqlExamples(BaseController parent, TextInputControl input,
            String tableName, boolean onlyQuery, Event event) {
        try {
            MenuController controller = MenuController.open(parent, input, event);
            String menuName = "SqlExamples";
            boolean isTextArea = input instanceof TextArea;

            List<Node> topButtons = new ArrayList<>();
            if (isTextArea) {
                Button newLineButton = new Button();
                newLineButton.setGraphic(StyleTools.getIconImageView("iconTurnOver.png"));
                NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
                newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.replaceText(input.getSelection(), "\n");
                        controller.getThisPane().requestFocus();
                        input.requestFocus();
                    }
                });
                topButtons.add(newLineButton);
            }

            Button clearButton = new Button();
            clearButton.setGraphic(StyleTools.getIconImageView("iconClear.png"));
            NodeStyleTools.setTooltip(clearButton, new Tooltip(message("ClearInputArea")));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                    controller.getThisPane().requestFocus();
                    input.requestFocus();
                }
            });
            topButtons.add(clearButton);

            CheckBox closeCheck = new CheckBox();
            closeCheck.setGraphic(StyleTools.getIconImageView("iconClose.png"));
            NodeStyleTools.setTooltip(closeCheck, new Tooltip(message("CloseAfterPaste")));
            closeCheck.setSelected(UserConfig.getBoolean(menuName + "ValuesCloseAfterPaste", true));
            closeCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent aevent) {
                    UserConfig.setBoolean(menuName + "ValuesCloseAfterPaste", closeCheck.isSelected());
                }
            });
            topButtons.add(closeCheck);

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImageView("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWindowWhenMouseHovering")));
            popCheck.setSelected(UserConfig.getBoolean(menuName + "PopWhenMouseHovering", false));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(menuName + "PopWhenMouseHovering", popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            String tname = tableName == null ? "<table>" : tableName;
            addButtonsPane(controller, input, Arrays.asList(
                    "SELECT * FROM " + tname,
                    " WHERE ", " ORDER BY ", " DESC ", " ASC ",
                    " FETCH FIRST ROW ONLY", " FETCH FIRST <size> ROWS ONLY",
                    " OFFSET <start> ROWS FETCH NEXT <size> ROWS ONLY"
            ), menuName);
            addButtonsPane(controller, input, Arrays.asList(
                    " , ", " (   ) ", " = ", " '' ", " >= ", " > ", " <= ", " < ", " != "
            ), menuName);
            addButtonsPane(controller, input, Arrays.asList(
                    " AND ", " OR ", " NOT ", " IS NULL ", " IS NOT NULL "
            ), menuName);
            addButtonsPane(controller, input, Arrays.asList(
                    " LIKE 'a%' ", " LIKE 'a_' ", " BETWEEN <value1> AND <value2>"
            ), menuName);
            addButtonsPane(controller, input, Arrays.asList(
                    " IN ( <value1>, <value2> ) ", " IN (SELECT FROM " + tname + " WHERE <condition>) "
            ), menuName);
            addButtonsPane(controller, input, Arrays.asList(
                    " EXISTS (SELECT FROM " + tname + " WHERE <condition>) "
            ), menuName);
            addButtonsPane(controller, input, Arrays.asList(
                    " DATE('1998-02-26') ", " TIMESTAMP('1962-09-23 03:23:34.234') "
            ), menuName);
            addButtonsPane(controller, input, Arrays.asList(
                    " COUNT(*) ", " AVG() ", " MAX() ", " MIN() ", " SUM() ", " GROUP BY ", " HAVING "
            ), menuName);
            addButtonsPane(controller, input, Arrays.asList(
                    " JOIN ", " INNER JOIN ", " LEFT OUTER JOIN ", " RIGHT OUTER JOIN ", " CROSS JOIN "
            ), menuName);
            if (!onlyQuery) {
                addButtonsPane(controller, input, Arrays.asList(
                        "INSERT INTO " + tname + " (column1, column2) VALUES (value1, value2)",
                        "UPDATE " + tname + " SET <column1>=<value1>, <column2>=<value2> WHERE <condition>",
                        "DELETE FROM " + tname + " WHERE <condition>", "TRUNCATE TABLE <table>",
                        "ALTER TABLE " + tname + " ALTER COLUMN id RESTART WITH 100"
                ), menuName);
            }

            Hyperlink link = new Hyperlink("Derby Reference Manual");
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    parent.openLink("https://db.apache.org/derby/docs/10.15/ref/index.html");
                }
            });
            controller.addNode(link);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void popJShellSuggesions(BaseController parent, JShell jShell, TextInputControl scriptInput) {
        try {
            List<SourceCodeAnalysis.Suggestion> suggestions = jShell.sourceCodeAnalysis().completionSuggestions(
                    scriptInput.getText(), scriptInput.getCaretPosition(), new int[1]);
            if (suggestions == null || suggestions.isEmpty()) {
                return;
            }
            ContextMenu ePopMenu = parent.getPopMenu();
            if (ePopMenu != null && ePopMenu.isShowing()) {
                ePopMenu.hide();
            }
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu;

            for (SourceCodeAnalysis.Suggestion suggestion : suggestions) {
                String c = suggestion.continuation();
                menu = new MenuItem(StringTools.abbreviate(c, 100));
                menu.setOnAction((ActionEvent event) -> {
                    scriptInput.replaceText(scriptInput.getSelection(), c);
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());
            parent.popNodeMenu(scriptInput, items);
        } catch (Exception e) {
        }
    }

    public static MenuController popJavaScriptExamples(BaseController parent, Event event,
            TextInputControl scriptInput, String menuName) {
        try {
            MenuController controller = MenuController.open(parent, scriptInput, event);
            controller.setTitleLabel(message("Examples"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button();
            newLineButton.setGraphic(StyleTools.getIconImageView("iconTurnOver.png"));
            NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    scriptInput.replaceText(scriptInput.getSelection(), "\n");
                    scriptInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);

            Button clearInputButton = new Button();
            clearInputButton.setGraphic(StyleTools.getIconImageView("iconClear.png"));
            NodeStyleTools.setTooltip(clearInputButton, new Tooltip(message("ClearInputArea")));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    scriptInput.clear();
                }
            });
            topButtons.add(clearInputButton);

            CheckBox closeCheck = new CheckBox();
            closeCheck.setGraphic(StyleTools.getIconImageView("iconClose.png"));
            NodeStyleTools.setTooltip(closeCheck, new Tooltip(message("CloseAfterPaste")));
            closeCheck.setSelected(UserConfig.getBoolean(menuName + "ValuesCloseAfterPaste", true));
            closeCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent aevent) {
                    UserConfig.setBoolean(menuName + "ValuesCloseAfterPaste", closeCheck.isSelected());
                }
            });
            topButtons.add(closeCheck);

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImageView("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWindowWhenMouseHovering")));
            popCheck.setSelected(UserConfig.getBoolean(menuName + "PopWhenMouseHovering", false));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(menuName + "PopWhenMouseHovering", popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    " + ", " - ", " * ", " / ", " % ",
                    "''", "( )", ";", " = ", " += ", " -= ", " *= ", " /= ", " %= ",
                    "++ ", "-- ", " , ", " { } ", "[ ]", "\" \"", ".",
                    " var ", " this"
            ), menuName);

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    " >= ", " > ", " <= ", " < ", " != ", " && ", " || ", " !",
                    " '' == ", " == ", " '' != ", " === ", " !== ",
                    " true ", " false ", " null ", " undefined "
            ), menuName);

            return controller;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void rowExpressionButtons(MenuController controller,
            TextInputControl scriptInput, String colName, String menuName) {
        try {
            if (controller == null) {
                return;
            }
            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "#{" + message("DataRowNumber") + "} % 2 == 0",
                    "#{" + message("DataRowNumber") + "} % 2 == 1",
                    "#{" + message("DataRowNumber") + "} >= 9",
                    "#{" + message("TableRowNumber") + "} % 2 == 0",
                    "#{" + message("TableRowNumber") + "} % 2 == 1",
                    "#{" + message("TableRowNumber") + "} == 1"
            ), 2, menuName);

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "#{" + colName + "} == 0",
                    "Math.abs(#{" + colName + "}) >= 3",
                    "#{" + colName + "} < 0 || #{" + colName + "} != -6 "
            ), 3, menuName);

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "new Date('#{" + message("Time") + "}'.replace(/-/g,'/')).getTime()  > new Date('2016/05/19 09:23:12').getTime()",
                    "'#{" + message("Time") + "}' == '2016-05-19 11:34:28'",
                    "'#{" + message("Time") + "}'.startsWith('2016-05-19 11')"
            ), 4, menuName);

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "'#{" + colName + "}' == ''",
                    "'#{" + colName + "}'.length > 0",
                    "'#{" + colName + "}'.indexOf('Hello') == 3",
                    "'#{" + colName + "}'.endsWith('Hello')",
                    "'#{" + colName + "}'.search(/Hello/ig) >= 0",
                    "var array = [ 'A', 'B', 'C', 'D' ];\n"
                    + "array.includes('#{" + colName + "})')"
            ), 5, menuName);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static List<String> htmlTags() {
        return Arrays.asList(
                "p", "img", "a", "div", "li", "ul", "ol", "h1", "h2", "h3", "h4",
                "button", "input", "label", "form", "table", "tr", "th", "td",
                "font", "span", "b", "hr", "br", "frame", "pre",
                "meta", "script", "style"
        );
    }

    public static void popHtmlTagExamples(BaseController parent, TextInputControl input, Event event) {
        try {
            MenuController controller = MenuController.open(parent, input, event);
            Button clearButton = new Button(message("ClearInputArea"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                    controller.getThisPane().requestFocus();
                    input.requestFocus();
                }
            });
            controller.addNode(clearButton);

            List< Node> nodes = new ArrayList<>();
            for (String value : htmlTags()) {
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.setText(value);
                        controller.getThisPane().requestFocus();
                        input.requestFocus();
                    }
                });
                nodes.add(button);
            }
            controller.addFlowPane(nodes);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        values
     */
    public static void popValues(BaseController parent, TextInputControl input,
            String menuName, LinkedHashMap<String, String> values, Event event) {
        try {
            MenuController controller = MenuController.open(parent, input, event);

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button();
            newLineButton.setGraphic(StyleTools.getIconImageView("iconTurnOver.png"));
            NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.replaceText(input.getSelection(), "\n");
                    input.requestFocus();
                }
            });
            topButtons.add(newLineButton);

            Button clearButton = new Button(message("ClearInputArea"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });
            topButtons.add(clearButton);

            CheckBox clearCheck = new CheckBox();
            clearCheck.setGraphic(StyleTools.getIconImageView("iconClear.png"));
            NodeStyleTools.setTooltip(clearCheck, new Tooltip(message("ClearAndPaste")));
            clearCheck.setSelected(UserConfig.getBoolean(menuName + "ValuesClearAndSet", false));
            clearCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(menuName + "ValuesClearAndSet", clearCheck.isSelected());
                }
            });
            topButtons.add(clearCheck);

            CheckBox closeCheck = new CheckBox();
            closeCheck.setGraphic(StyleTools.getIconImageView("iconClose.png"));
            NodeStyleTools.setTooltip(closeCheck, new Tooltip(message("CloseAfterPaste")));
            closeCheck.setSelected(UserConfig.getBoolean(menuName + "ValuesCloseAfterPaste", true));
            closeCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent aevent) {
                    UserConfig.setBoolean(menuName + "ValuesCloseAfterPaste", closeCheck.isSelected());
                }
            });
            topButtons.add(closeCheck);

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImageView("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWindowWhenMouseHovering")));
            popCheck.setSelected(UserConfig.getBoolean(menuName + "PopWhenMouseHovering", false));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(menuName + "PopWhenMouseHovering", popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);
            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            List<Node> nodes = new ArrayList<>();
            for (String name : values.keySet()) {
                String value = values.get(name);
                Button button = new Button(value + "    " + name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (UserConfig.getBoolean(menuName + "ValuesClearAndSet", true)) {
                            input.setText(value);
                        } else {
                            input.replaceText(input.getSelection(), value);
                        }
                        if (UserConfig.getBoolean(menuName + "ValuesCloseAfterPaste", true)) {
                            controller.close();
                        } else {
                            controller.getThisPane().requestFocus();
                        }
                        input.requestFocus();
                    }
                });
                nodes.add(button);
            }
            controller.addFlowPane(nodes);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
