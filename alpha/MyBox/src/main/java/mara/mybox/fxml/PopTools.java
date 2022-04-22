package mara.mybox.fxml;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
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
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ControlWebView;
import mara.mybox.controller.HtmlPopController;
import mara.mybox.controller.MenuController;
import mara.mybox.controller.TextInputController;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
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
            ControllerTools.openTarget(null, uri.toString());
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
        if (result == null || !result.isPresent()) {
            return null;
        }
        String value = result.get();
        return value;
    }

    public static boolean askSure(BaseController controller, String title, String sureString) {
        return askSure(controller, title, null, sureString);
    }

    // https://openjfx.io/javadoc/17/javafx.controls/javafx/scene/control/Dialog.html
    public static boolean askSure(BaseController controller, String title, String header, String sureString) {
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

    /*
        style
     */
    public static ContextMenu popHtmlStyle(MouseEvent mouseEvent, ControlWebView controller) {
        try {
            if (mouseEvent == null || controller == null) {
                return null;
            }
            ContextMenu cMenu = controller.getPopMenu();
            if (cMenu != null && cMenu.isShowing()) {
                cMenu.hide();
            }
            final ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            String baseName = controller.getBaseName();

            MenuItem menu = new MenuItem(message("HtmlStyle"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

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
            popMenu.getItems().add(rmenu);

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
                popMenu.getItems().add(rmenu);
                if (isCurrent) {
                    predefinedValue = true;
                }
            }

            rmenu = new RadioMenuItem(message("Input") + "...");
            rmenu.setOnAction(new EventHandler<ActionEvent>() {
                ChangeListener<Boolean> getListener;

                @Override
                public void handle(ActionEvent event) {
                    TextInputController inputController = TextInputController.open(controller,
                            message("Style"), UserConfig.getString(prefix + "HtmlStyle", null));
                    getListener = new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            String value = inputController.getText();
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
            popMenu.getItems().add(rmenu);

            popMenu.getItems().add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("ShareAllInterface"));
            checkMenu.setSelected(UserConfig.getBoolean(baseName + "ShareHtmlStyle", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ShareHtmlStyle", checkMenu.isSelected());
                }
            });
            popMenu.getItems().add(checkMenu);

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
            controller.setPopMenu(popMenu);
            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);
            return popMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ContextMenu popWindowStyles(BaseController parent, String baseStyle, MouseEvent mouseEvent) {
        try {
            ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            String baseName = parent.getBaseName();
            MenuItem menu = new MenuItem(message("WindowStyle"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

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
                popMenu.getItems().add(rmenu);
            }
            popMenu.getItems().add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("ShareAllInterface"));
            checkMenu.setSelected(UserConfig.getBoolean(baseName + "ShareWindowStyle", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ShareWindowStyle", checkMenu.isSelected());
                }
            });
            popMenu.getItems().add(checkMenu);

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

            parent.closePopup();
            parent.setPopMenu(popMenu);

            LocateTools.locateMouse(mouseEvent, popMenu);
            return popMenu;
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
        examples
     */
    public static ContextMenu popEraExample(ContextMenu inPopMenu, TextField input, MouseEvent mouseEvent) {
        try {
            if (inPopMenu != null && inPopMenu.isShowing()) {
                inPopMenu.hide();
            }
            final ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            List<String> values = new ArrayList<>();
            values.add(DateTools.nowString());
            values.add(DateTools.datetimeToString(new Date(), TimeFormats.DatetimeMs, TimeZone.getDefault()));
            values.add(DateTools.datetimeToString(new Date(), TimeFormats.TimeMs, TimeZone.getDefault()));
            values.add(DateTools.datetimeToString(new Date(), TimeFormats.DatetimeMs + " Z", TimeZone.getDefault()));
            values.addAll(Arrays.asList(
                    "2020-07-15T36:55:09", "960-01-23", "581",
                    "-2020-07-10 10:10:10.532 +0800", "-960-01-23", "-581"
            ));
            if (Languages.isChinese()) {
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
                    popMenu.requestFocus();
                    input.requestFocus();
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
            LocateTools.locateMouse(mouseEvent, popMenu);
            return popMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void popRegexExample(BaseController parent, TextInputControl input, MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(parent, input, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            Button clearButton = new Button(message("Clear"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                    controller.getThisPane().requestFocus();
                    input.requestFocus();
                }
            });
            controller.addNode(clearButton);
            List<String> values = Arrays.asList("^      "
                    + message("StartLocation"), "$      "
                    + message("EndLocation"), "*      "
                    + message("ZeroOrNTimes"), "+      "
                    + message("OneOrNTimes"), "?      "
                    + message("ZeroOrOneTimes"), "{n}      "
                    + message("NTimes"), "{n,}      "
                    + message("N+Times"), "{n,m}      "
                    + message("NMTimes"), "|      "
                    + message("Or"), "[abc]      "
                    + message("MatchOneCharacters"), "[A-Z]      "
                    + message("A-Z"), "\\x20      "
                    + message("Blank"), "\\s      "
                    + message("NonprintableCharacter"), "\\S      "
                    + message("PrintableCharacter"), "\\n      "
                    + message("LineBreak"), "\\r      "
                    + message("CarriageReturn"), "\\t      "
                    + message("Tab"), "[0-9]{n}      "
                    + message("NNumber"), "[A-Z]{n}      "
                    + message("NUppercase"), "[a-z]{n}      "
                    + message("NLowercase"), "[\\u4e00-\\u9fa5]      "
                    + message("Chinese"), "[^\\x00-\\xff]      "
                    + message("DoubleByteCharacter"), "[A-Za-z0-9]+      "
                    + message("EnglishAndNumber"), "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*      "
                    + message("Email"), "(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}      "
                    + message("PhoneNumber"), "[a-zA-z]+://[^\\s]*       "
                    + message("URL"), "^(\\s*)\\n       "
                    + message("BlankLine"), "\\d+\\.\\d+\\.\\d+\\.\\d+      "
                    + message("IP"));
            List<Node> nodes = new ArrayList<>();
            for (String value : values) {
                String[] vv = value.split("      ");
                Button button = new Button(vv[1].trim());
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.replaceText(input.getSelection(), vv[0]);
                        controller.getThisPane().requestFocus();
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

    public static void popColorExamples(BaseController parent, TextInputControl input, MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(parent, input, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            Button clearButton = new Button(message("Clear"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });
            controller.addNode(clearButton);
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

    public static void popStringValues(BaseController parent, TextInputControl input, MouseEvent mouseEvent, String name) {
        popStringValues(parent, input, mouseEvent, name, false);
    }

    public static void popStringValues(BaseController parent, TextInputControl input, MouseEvent mouseEvent, String name, boolean clearAndSet) {
        try {
            int max = UserConfig.getInt(name + "MaxSaved", 20);

            MenuController controller = MenuController.open(parent, input, mouseEvent.getScreenX(), mouseEvent.getScreenY());

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
                public void handle(ActionEvent event) {
                    TableStringValues.clear(name);
                    controller.close();
                    popStringValues(parent, input, mouseEvent, name, clearAndSet);
                }
            });
            setButtons.add(clearValuesButton);

            Button maxButton = new Button(message("MaxSaved"));
            maxButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String value = PopTools.askValue(parent.getTitle(), null, message("MaxSaved"), max + "");
                    if (value == null) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(value);
                        UserConfig.setInt(name + "MaxSaved", v);
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                    }
                }
            });
            setButtons.add(maxButton);

            setButtons.add(new Label(message("RightClickToDelete")));
            controller.addFlowPane(setButtons);
            controller.addNode(new Separator());

            List<String> values = TableStringValues.max(name, max);
            List<Node> buttons = new ArrayList<>();
            for (String value : values) {
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (clearAndSet) {
                            input.setText(value);
                        } else {
                            input.replaceText(input.getSelection(), value);
                        }
                        controller.getThisPane().requestFocus();
                        input.requestFocus();
                    }
                });
                button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getButton() == MouseButton.SECONDARY) {
                            TableStringValues.delete(name, value);
                            controller.close();
                            popStringValues(parent, input, mouseEvent, name, clearAndSet);
                        }
                    }
                });
                buttons.add(button);
            }
            controller.addFlowPane(buttons);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void popSqlExamples(BaseController parent, TextInputControl input,
            String tableName, boolean onlyQuery, MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(parent, input, mouseEvent.getScreenX(), mouseEvent.getScreenY());

            boolean isTextArea = input instanceof TextArea;

            List<Node> topButtons = new ArrayList<>();
            if (isTextArea) {
                Button newLineButton = new Button(message("Newline"));
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
            Button clearButton = new Button(message("ClearInputArea"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                    controller.getThisPane().requestFocus();
                    input.requestFocus();
                }
            });
            topButtons.add(clearButton);
            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            String tname = tableName == null ? "<table>" : tableName;
            addButtonsPane(controller, input, Arrays.asList(
                    "SELECT * FROM " + tname,
                    " WHERE ", " ORDER BY ", " DESC ", " ASC ",
                    " FETCH FIRST ROW ONLY", " FETCH FIRST <size> ROWS ONLY",
                    " OFFSET <start> ROWS FETCH NEXT <size> ROWS ONLY"
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " , ", " (   ) ", " = ", " '' ", " >= ", " > ", " <= ", " < ", " != "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " AND ", " OR ", " NOT ", " IS NULL ", " IS NOT NULL "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " LIKE 'a%' ", " LIKE 'a_' ", " BETWEEN <value1> AND <value2>"
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " IN ( <value1>, <value2> ) ", " IN (SELECT FROM " + tname + " WHERE <condition>) "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " EXISTS (SELECT FROM " + tname + " WHERE <condition>) "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " DATE('1998-02-26') ", " TIMESTAMP('1962-09-23 03:23:34.234') "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " COUNT() ", " AVG() ", " MAX() ", " MIN() ", " SUM() ", " GROUP BY ", " HAVING "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " JOIN ", " INNER JOIN ", " LEFT OUTER JOIN ", " RIGHT OUTER JOIN ", " CROSS JOIN "
            ));
            if (!onlyQuery) {
                addButtonsPane(controller, input, Arrays.asList(
                        "INSERT INTO " + tname + " (column1, column2) VALUES (value1, value2)",
                        "UPDATE " + tname + " SET <column1>=<value1>, <column2>=<value2> WHERE <condition>",
                        "DELETE FROM " + tname + " WHERE <condition>", "TRUNCATE TABLE <table>",
                        "ALTER TABLE " + tname + " ALTER COLUMN id RESTART WITH 100"
                ));
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

    public static void popStringValues(BaseController parent, TextInputControl input, MouseEvent mouseEvent, List<String> values) {
        try {
            if (parent == null || input == null || values == null) {
                return;
            }
            MenuController controller = MenuController.open(parent, input, mouseEvent.getScreenX(), mouseEvent.getScreenY());

            boolean isTextArea = input instanceof TextArea;

            List<Node> topButtons = new ArrayList<>();
            if (isTextArea) {
                Button newLineButton = new Button(message("Newline"));
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
            Button clearButton = new Button(message("ClearInputArea"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                    controller.getThisPane().requestFocus();
                    input.requestFocus();
                }
            });
            topButtons.add(clearButton);
            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            addButtonsPane(controller, input, values);

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

    public static void addButtonsPane(MenuController controller, TextInputControl input, List<String> values) {
        try {
            List<Node> buttons = new ArrayList<>();
            for (String value : values) {
                if (value == null) {
                    continue;
                }
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.replaceText(input.getSelection(), value);
                        controller.getThisPane().requestFocus();
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

    public static void addButtonsPane(MenuController controller, TextInputControl input, Map<String, String> values) {
        try {
            List<Node> buttons = new ArrayList<>();
            for (String name : values.keySet()) {
                String value = values.get(name);
                if (value == null) {
                    continue;
                }
                Button button = new Button(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.replaceText(input.getSelection(), value);
                        controller.getThisPane().requestFocus();
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

    public static void popTableNames(BaseController parent, TextInputControl input,
            MouseEvent mouseEvent, boolean internal) {
        try {
            MenuController controller = MenuController.open(parent, input, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            controller.addNode(new Label(message("TableName")));

            List<String> names;
            if (internal) {
                names = DataInternalTable.InternalTables;
            } else {
                names = DataTable.userTables();
            }
            List<Node> valueButtons = new ArrayList<>();
            for (String name : names) {
                Button button = new Button(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        input.replaceText(input.getSelection(), name);
                        controller.getThisPane().requestFocus();
                        input.requestFocus();
                    }
                });
                valueButtons.add(button);
            }
            controller.addFlowPane(valueButtons);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void popTableDefinition(BaseController parent, Node node,
            MouseEvent mouseEvent, boolean internal) {
        try {
            MenuController controller = MenuController.open(parent, node, mouseEvent.getScreenX(), mouseEvent.getScreenY());

            controller.addNode(new Label(message("TableDefinition")));

            List<String> names;
            if (internal) {
                names = DataInternalTable.InternalTables;
            } else {
                names = DataTable.userTables();
            }
            List<Node> valueButtons = new ArrayList<>();
            for (String name : names) {
                Button button = new Button(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String html = TableData2D.tableDefinition(name);
                        if (html != null) {
                            HtmlPopController.openHtml(parent, html);
                        } else {
                            parent.popError(message("NotFound"));
                        }
                    }
                });
                valueButtons.add(button);
            }
            controller.addFlowPane(valueButtons);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
