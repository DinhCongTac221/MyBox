package mara.mybox.controller;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.MessageFormat;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ScrollEvent;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.data.PdfInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;
import org.fit.pdfdom.PDFDomTreeConfig;
import thridparty.PDFResourceToDirHandler;

/**
 * @Author Mara
 * @CreateDate 2019-8-31
 * @License Apache License Version 2.0
 */
public class PdfHtmlViewerController extends PdfViewController {

    protected final String checkBottomScript, checkTopScript;

    protected File htmlFile, subPath;
    protected PDFDomTreeConfig domConfig;
    protected WebEngine webEngine;
    protected float zoomScale;
    protected boolean atTop, atBottom, setScroll;

    @FXML
    protected WebView webView;
    @FXML
    protected HTMLEditor htmlEditor;
    @FXML
    protected TextArea textArea;

    public PdfHtmlViewerController() {
        baseTitle = AppVariables.message("PdfHtmlViewer");

        checkBottomScript
                = " function checkBottom() { "
                + "     var scrollTop = document.documentElement.scrollTop||document.body.scrollTop;  "
                + "     var windowHeight = document.documentElement.clientHeight || document.body.clientHeight; "
                + "     var scrollHeight = document.documentElement.scrollHeight||document.body.scrollHeight;"
                //                + "     alert(scrollTop + ' ' + windowHeight + ' ' + scrollHeight);  "
                + "     if ( scrollTop + windowHeight - scrollHeight < 50 || windowHeight > scrollHeight) {  "
                + "          alert('AtBottom');  "
                + "     };"
                + " }; "
                + "checkBottom(); ";

        checkTopScript
                = " function checkTop() { "
                + "     var scrollTop = document.documentElement.scrollTop||document.body.scrollTop;  "
                + "     if ( scrollTop == 0 ) {  "
                + "          alert('AtTop');  "
                + "     };"
                + " }; "
                + "checkTop(); ";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PDF, VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (tipsView != null) {
                FxmlControl.setTooltip(tipsView,
                        new Tooltip(message("PDFComments") + "\n\n" + message("PdfHtmlViewerTips")));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initOperationBox() {
        try {
            super.initOperationBox();

            operationBox.disableProperty().bind(Bindings.not(infoLoaded));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initViewPane() {
        try {
            super.initViewPane();

            mainPane.disableProperty().bind(Bindings.not(infoLoaded));

            domConfig = PDFDomTreeConfig.createDefaultConfig();
            zoomScale = 1.0f;

            webView.setContextMenuEnabled(true);
            // https://stackoverflow.com/questions/51048312/javafx-webview-scrollevent-listener-zooms-in-and-scrolls-only-want-it-to-zoom-i?r=SearchResults
            webView.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent event) {
                    double deltaY = event.getDeltaY();
                    if (deltaY > 0) {
                        webEngine.executeScript(checkTopScript);

                        if (event.isControlDown()) {
                            webView.setZoom(webView.getZoom() * 1.1);
                        }
                    } else {
                        webEngine.executeScript(checkBottomScript);

                        if (event.isControlDown()) {
                            webView.setZoom(webView.getZoom() / 1.1);
                        }
                    }
                    event.consume();
                }
            });

            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);
            webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    try {
                        if ("AtBottom".equals(ev.getData())) {
                            if (setScroll) {
                                setScroll = false;
                                return;
                            }
                            if (atBottom) {  // Go next at second time
                                atBottom = false;
                                pageNextAction();
                            } else {         // buffering at first time
                                atBottom = true;
                                setScroll = true;
                                int h = (Integer) webEngine.executeScript("document.documentElement.scrollHeight || document.body.scrollHeight;");
                                webEngine.executeScript("window.scrollTo(0," + (h - 200) + ");");
                            }
                        } else if ("AtTop".equals(ev.getData())) {
                            if (setScroll) {
                                setScroll = false;
                                return;
                            }
                            if (atTop) {  // Go previous at second time
                                atTop = false;
                                pagePreviousAction();
                            } else {         // buffering at first time
                                atTop = true;
                                setScroll = true;
                                webEngine.executeScript("window.scrollTo(0, 200);");
                            }
                        }

                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                }
            });

            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState, State newState) {
                    try {
                        if (newState == State.SUCCEEDED) {
                            String contents = FxmlControl.getHtml(webEngine);
                            htmlEditor.setHtmlText(contents);
                            textArea.setText(contents);
                        }
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void loadFile(File file, PdfInformation pdfInfo, int page) {
        try {
            webEngine.load(null);
            currentPage = page;
            infoLoaded.set(false);
            pageSelector.setValue(null);
            pageLabel.setText("");
            thumbBox.getChildren().clear();
            outlineTree.setRoot(null);
            if (file == null) {
                return;
            }
            sourceFile = file;

            if (pdfInfo != null) {
                pdfInformation = pdfInfo;
                loadPage();
            } else {
                pdfInformation = new PdfInformation(sourceFile);
                loadInformation(null);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected void loadPage() {
        if (pdfInformation == null) {
            return;
        }
        if (currentPage < 0) {
            currentPage = 0;
        } else if (infoLoaded.get() && currentPage >= pdfInformation.getNumberOfPages()) {
            currentPage = pdfInformation.getNumberOfPages() - 1;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                protected String title;

                @Override
                protected boolean handle() {
                    title = sourceFile.getAbsolutePath() + " " + MessageFormat.format(message("PageNumber3"), (currentPage + 1) + "");
                    htmlFile = FileTools.getTempFile(".html");
                    subPath = new File(htmlFile.getParent() + File.separator
                            + htmlFile.getName().substring(0, htmlFile.getName().length() - 5));
                    subPath.mkdirs();
                    domConfig.setFontHandler(new PDFResourceToDirHandler(subPath));
                    domConfig.setImageHandler(new PDFResourceToDirHandler(subPath));
                    try ( PDDocument doc = PDDocument.load(sourceFile, password, AppVariables.pdfMemUsage)) {
                        PDFDomTree parser = new PDFDomTree(domConfig);
                        parser.setStartPage(currentPage + 1);
                        parser.setEndPage(currentPage + 1);
                        parser.setPageStart(title);
//                    MyBoxLog.debug(parser.getSpacingTolerance());
//                    parser.setSpacingTolerance(0f);
                        try ( Writer output = new PrintWriter(htmlFile, "utf-8")) {
                            try {
                                parser.writeText(doc, output);
                            } catch (Exception e) {
//                                MyBoxLog.debug(error);
                            }
                            doc.close();
                            ok = true;
                        } catch (Exception e) {
                            error = e.toString();
//                            MyBoxLog.debug(error);
                        }
                    } catch (Exception e) {
                        error = e.toString();
//                        MyBoxLog.debug(error);
                    }
                    return htmlFile.exists();
                }

                @Override
                protected void whenSucceeded() {
                    String name = htmlFile.getAbsolutePath();
                    name = name.replace("\\\\", "/");
                    webEngine.load("file:///" + name);
                    webView.requestFocus();
                    atBottom = false;

                    getMyStage().setTitle(getBaseTitle() + " " + title);
                    isSettingValues = true;
                    pageSelector.setValue((currentPage + 1) + "");
                    isSettingValues = false;
                    pagePreviousButton.setDisable(currentPage <= 0);
                    pageNextButton.setDisable(!infoLoaded.get() || currentPage >= (pdfInformation.getNumberOfPages() - 1));
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL,
                    MessageFormat.format(message("LoadingPageNumber"), (currentPage + 1) + ""));
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void zoomIn() {
        zoomScale += 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    @Override
    public void zoomOut() {
        zoomScale -= 0.1f;
        webView.setZoom(zoomScale);
    }

    @Override
    protected void setPrecent(int percent) {
        zoomScale = percent / 100f;
        webView.setZoom(zoomScale);
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            if (htmlFile == null || subPath == null) {
                return;
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    saveAsPrefix(), targetExtensionFilter);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            FileTools.copyFile(htmlFile, file);
            String path = file.getParent() + File.separator
                    + htmlFile.getName().substring(0, htmlFile.getName().length() - 5);
            FileTools.copyWholeDirectory(subPath, new File(path));

            if (file.exists()) {
                FxmlStage.openHtmlEditor(null, file);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
