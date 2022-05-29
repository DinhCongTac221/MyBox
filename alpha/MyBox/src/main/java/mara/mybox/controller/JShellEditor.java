package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import mara.mybox.data.JShellSnippet;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class JShellEditor extends TreeNodeEditor {
    
    protected JShellController jShellController;
    protected String outputs = "";
    protected JShell jShell;
    protected SingletonTask resetTask;
    
    @FXML
    protected Button clearCodesButton, suggestionsButton;
    
    public JShellEditor() {
        defaultExt = "java";
    }
    
    protected void setParameters(JShellController jShellController) {
        this.jShellController = jShellController;
        resetJShell();
    }
    
    public String expValue(String exp) {
        try {
            return new JShellSnippet(jShell, jShell.eval(exp).get(0).snippet()).getValue();
        } catch (Exception e) {
            MyBoxLog.error(e, exp);
            return null;
        }
    }
    
    @FXML
    public synchronized void resetJShell() {
        reset();
        resetTask = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    jShell = JShell.create();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }
            
            @Override
            protected void whenSucceeded() {
                jShellController.pathsController.resetPaths();
                jShellController.snippetsController.refreshSnippets();
            }
            
        };
        start(resetTask, true);
    }
    
    @Override
    protected void showEditorPane() {
    }
    
    @FXML
    @Override
    public void startAction() {
        String codes = valueInput.getText();
        if (codes == null || codes.isBlank()) {
            popError(message("NoInput"));
            return;
        }
        if (startButton.getUserData() != null) {
            cancelAction();
            return;
        }
        StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
        startButton.applyCss();
        startButton.setUserData("started");
        jShellController.rightPaneCheck.setSelected(true);
        task = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                return handleCodes(codes);
            }
            
            @Override
            protected void finalAction() {
                cancelAction();
                editNode(null);
            }
        };
        start(task);
    }
    
    protected boolean handleCodes(String codes) {
        TableStringValues.add("JShellHistories", codes.trim());
        return runCodes(codes);
    }
    
    protected boolean runCodes(String codes) {
        try {
            if (codes == null || codes.isBlank()) {
                return false;
            }
            String leftCodes = codes;
            while (leftCodes != null && !leftCodes.isBlank()) {
                SourceCodeAnalysis.CompletionInfo info = jShell.sourceCodeAnalysis().analyzeCompletion(leftCodes);
                if (!runSnippet(info.source())) {
                    return false;
                }
                leftCodes = info.remaining();
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }
    
    protected boolean runSnippet(String source) {
        return runSnippet(source, source);
    }
    
    protected boolean runSnippet(String orignalSource, String source) {
        try {
            if (source == null || source.isBlank()) {
                return false;
            }
            String snippet = source.trim();
            List<SnippetEvent> events = jShell.eval(snippet);
            String snippetOutputs = DateTools.nowString()
                    + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(orignalSource == null ? snippet : orignalSource)
                    + "</div>";
            String results = "";
            for (int i = 0; i < events.size(); i++) {
                if (task == null || task.isCancelled()) {
                    output(message("Canceled"));
                    return true;
                }
                SnippetEvent e = events.get(i);
                JShellSnippet jShellSnippet = new JShellSnippet(jShell, e.snippet());
                if (i > 0) {
                    results += "\n";
                }
                results += "id: " + jShellSnippet.getId() + "\n";
                if (jShellSnippet.getStatus() != null) {
                    results += message("Status") + ": " + jShellSnippet.getStatus() + "\n";
                }
                if (jShellSnippet.getType() != null) {
                    results += message("Type") + ": " + jShellSnippet.getType() + "\n";
                }
                if (jShellSnippet.getName() != null) {
                    results += message("Name") + ": " + jShellSnippet.getName() + "\n";
                }
                if (jShellSnippet.getValue() != null) {
                    results += message("Value") + ": " + jShellSnippet.getValue() + "\n";
                }
            }
            snippetOutputs += "<div class=\"valueBox\">"
                    + HtmlWriteTools.stringToHtml(results) + "</div>";
            output(snippetOutputs);
            
        } catch (Exception e) {
            output(e.toString());
        }
        return true;
    }
    
    @Override
    public void cancelAction() {
        if (task != null) {
            task.cancel();
        }
        reset();
    }
    
    public void reset() {
        if (resetTask != null) {
            resetTask.cancel();
        }
        if (jShell != null) {
            jShell.stop();
        }
        jShellController.snippetsController.refreshSnippets();
        StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
        startButton.applyCss();
        startButton.setUserData(null);
    }
    
    protected void output(String msg) {
        Platform.runLater(() -> {
            outputs += msg + "<br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            jShellController.webViewController.loadContents(html);
        });
        
    }

    // https://stackoverflow.com/questions/53867043/what-are-the-limits-to-jshell?r=SearchResults
    @FXML
    protected void popSyntaxMenu(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(jShellController, valueInput,
                    mouseEvent.getScreenX(), mouseEvent.getScreenY() + 20);
            controller.setTitleLabel(message("Syntax"));
            
            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button(message("Newline"));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.replaceText(valueInput.getSelection(), "\n");
                    valueInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);
            Button clearInputButton = new Button(message("ClearInputArea"));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.clear();
                }
            });
            topButtons.add(clearInputButton);
            controller.addFlowPane(topButtons);
            
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "int maxInt = Integer.MAX_VALUE, minInt = Integer.MIN_VALUE;",
                    "double maxDouble = Double.MAX_VALUE, minDouble = Double.MIN_VALUE;",
                    "float maxFloat = Float.MAX_VALUE, minFloat = Float.MIN_VALUE;",
                    "long maxLong = Long.MAX_VALUE, minLong = Long.MIN_VALUE;",
                    "short maxShort = Short.MAX_VALUE, minShort = Short.MIN_VALUE;",
                    "String s1 =\"Hello\";",
                    "String[] sArray = new String[3]; "
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    ";", " , ", "( )", " = ", " { } ", "[ ]", "\"", " + ", " - ", " * ", " / ",
                    " == ", " != ", " >= ", " > ", " <= ", " < "
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "if (3 > 2) {\n"
                    + "   int a = 1;\n"
                    + "}",
                    "for (int i = 0; i < 5; ++i) {\n"
                    + "    double d = i / 2d - 1;\n"
                    + "}",
                    "while (true) {\n"
                    + "    double d = Math.PI;\n"
                    + "    if ( d > 3 ) break;\n"
                    + "}"
            ));
            
            Hyperlink alink = new Hyperlink("Learning the Java Language");
            alink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://docs.oracle.com/javase/tutorial/java/index.html");
                }
            });
            controller.addNode(alink);
            
            Hyperlink jlink = new Hyperlink("Java Development Kit (JDK) APIs");
            jlink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://docs.oracle.com/en/java/javase/17/docs/api/index.html");
                }
            });
            controller.addNode(jlink);
            
            Hyperlink blink = new Hyperlink("Full list of Math functions");
            blink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Math.html");
                }
            });
            controller.addNode(blink);
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    @FXML
    protected void popHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, valueInput, mouseEvent, "JShellHistories");
    }
    
    @FXML
    public void popSuggesions() {
        try {
            List<SourceCodeAnalysis.Suggestion> suggestions = jShell.sourceCodeAnalysis().completionSuggestions(
                    valueInput.getText(), valueInput.getCaretPosition(), new int[1]);
            if (suggestions == null || suggestions.isEmpty()) {
                return;
            }
            
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.setAutoFix(true);
            
            MenuItem menu;
            
            for (SourceCodeAnalysis.Suggestion suggestion : suggestions) {
                String c = suggestion.continuation();
                menu = new MenuItem(c);
                menu.setOnAction((ActionEvent event) -> {
                    valueInput.replaceText(valueInput.getSelection(), c);
                });
                popMenu.getItems().add(menu);
            }
            
            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);
            
            LocateTools.locateMouse(valueInput, popMenu);
        } catch (Exception e) {
        }
    }
    
    @Override
    public boolean controlAlt1() {
        popSuggesions();
        return true;
    }
    
    @Override
    public void cleanPane() {
        try {
            cancelAction();
            resetTask = null;
            jShell = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }
    
}
