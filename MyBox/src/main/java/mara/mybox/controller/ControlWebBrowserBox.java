package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javax.imageio.ImageIO;
import mara.mybox.data.BrowserHistory;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.TableBrowserHistory;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.MyboxDataPath;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class ControlWebBrowserBox extends ControlWebview {

    protected Tab tab;
    protected String status;
    protected boolean bypassSSL, fetchIcon;
    protected BrowserHistory his;

    @FXML
    protected ComboBox<String> urlBox;
    @FXML
    protected HBox addressBox;
    @FXML
    protected Button settingsButton;

    public ControlWebBrowserBox() {
        baseTitle = AppVariables.message("WebBrowser");
        fetchIcon = false;

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initURLBox();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void initURLBox() {
        try {
            List<String> urls = TableBrowserHistory.recentBrowse();
            if (!urls.isEmpty()) {
                isSettingValues = true;
                urlBox.getItems().addAll(urls);
                isSettingValues = false;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void setBrowser(BaseController parent, Tab tab) {
        setValues(parent, true, true);
        addressBox.getChildren().remove(settingsButton);
        this.tab = tab;
        fetchIcon = true;
    }

    @Override
    public void sourceFileChanged(final File file) {
        loadFile(file);
    }

    @Override
    public void setAddress(String value) {
        super.setAddress(value);
        urlBox.setValue(address);
    }

    public void setFile(File file) {
        if (file == null) {
            return;
        }
        sourceFile = file;
        setAddress(file.toURI().toString());
    }

    @FXML
    @Override
    public void goAction() {
        try {
            address = urlBox.getValue();
            if (address == null || address.isBlank()) {
                return;
            }
            URL url;
            try {
                url = new URL(address);
                urlBox.getEditor().setStyle(null);
            } catch (Exception e) {
                urlBox.getEditor().setStyle(badStyle);
                return;
            }
            setAddress(address);
            webEngine.getLoadWorker().cancel();
            bottomLabel.setText(AppVariables.message("Loading..."));
            webEngine.load(address);

            his = new BrowserHistory();
            his.setAddress(address);
            his.setVisitTime(new Date().getTime());
            his.setTitle(address);
            if (fetchIcon) {
                File iconFile = new File(MyboxDataPath + File.separator + "icons" + File.separator + url.getHost() + ".png");
                if (iconFile.exists()) {
                    his.setIcon(iconFile.getAbsolutePath());
                    if (tab != null) {
                        BufferedImage image = ImageIO.read(iconFile);
                        if (image != null) {
                            ImageView tabImage = new ImageView(SwingFXUtils.toFXImage(image, null));
                            tabImage.setFitWidth(20);
                            tabImage.setFitHeight(20);
                            tab.setGraphic(tabImage);
                        }
                    }
                }
            }
            TableBrowserHistory.write(his);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void afterPageLoaded() {
        try {
            super.afterPageLoaded();
            if (address == null) {
                return;
            }
            isSettingValues = true;
            urlBox.getItems().clear();
            List<String> urls = TableBrowserHistory.recentBrowse();
            if (!urls.isEmpty()) {
                urlBox.getItems().addAll(urls);
                urlBox.getEditor().setText(address);
            }
            isSettingValues = false;

            if (his == null) {
                his = new BrowserHistory();
                his.setAddress(address);
            }
            his.setVisitTime(new Date().getTime());
            String title = webEngine.getTitle();
            if (title != null) {
                his.setTitle(title);
                if (tab != null) {
                    tab.setText(title.substring(0, Math.min(10, title.length())));
                }
            } else {
                his.setTitle("");
                if (tab != null) {
                    tab.setText("");
                }
            }
            if (fetchIcon) {
                File path = new File(MyboxDataPath + File.separator + "icons");
                if (!path.exists()) {
                    path.mkdirs();
                }
                URL url = new URL(address);
                File file = new File(MyboxDataPath + File.separator + "icons" + File.separator + url.getHost() + ".png");
                if (!file.exists()) {
                    HtmlTools.readIcon(address, file);
                }
                if (file.exists()) {
                    his.setIcon(file.getAbsolutePath());
                    if (tab != null) {
                        BufferedImage image = ImageIO.read(file);
                        if (image != null) {
                            ImageView tabImage = new ImageView(SwingFXUtils.toFXImage(image, null));
                            tabImage.setFitWidth(20);
                            tabImage.setFitHeight(20);
                            tab.setGraphic(tabImage);
                        }
                    }
                } else {
                    his.setIcon("");
                    if (tab != null) {
                        ImageView tabImage = new ImageView("img/MyBox.png");
                        tabImage.setFitWidth(20);
                        tabImage.setFitHeight(20);
                        tab.setGraphic(tabImage);
                    }
                }
            }
            TableBrowserHistory.write(his);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void setting() {
        WebBrowserController controller = (WebBrowserController) openStage(CommonValues.WebBrowserFxml);
        controller.manageHistories();
    }

}
