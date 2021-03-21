package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.data.EpidemicReport;
import static mara.mybox.db.data.EpidemicReport.create;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeLevel;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.NetworkTools.trustAllManager;
import static mara.mybox.tools.NetworkTools.trustAllVerifier;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-16
 * @License Apache License Version 2.0
 */
public class EpidemicReportsImportBaiduController extends BaseTaskController {

    protected EpidemicReportsController parent;
    protected Date reportTime;
    protected List<EpidemicReport> reports;
    protected String address;
    protected String Address = "https://voice.baidu.com/act/newpneumonia/newpneumonia/?from=osari_pc_3";
    protected String Dataset = "COVID-19_Baidu";

    @FXML
    protected TextField addressInput;
    @FXML
    protected CheckBox replaceCheck, statisticCheck;

    public EpidemicReportsImportBaiduController() {
        baseTitle = AppVariables.message("ImportEpidemicReportBaidu");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            address = Address;
            addressInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        String v = addressInput.getText().trim();
                        if (v.isBlank()) {
                            return;
                        }
                        address = v;
                    });
            addressInput.setText(Address);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    protected boolean doTask() {
        try {
            if (TableGeographyCode.China() == null) {
                updateLogs(message("LoadingPredefinedGeographyCodes"));
                GeographyCodeTools.importPredefined();
            }
            reports = new ArrayList();
            updateLogs(address);
            URL url = new URL(address);
            File pageFile = FileTools.getTempFile(".txt");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            SSLContext sc = SSLContext.getInstance(CommonValues.HttpsProtocal);
            sc.init(null, trustAllManager(), new SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier(trustAllVerifier());
            connection.connect();
            try ( BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(pageFile))) {
                byte[] buf = new byte[CommonValues.IOBufferLength];
                int len;
                while ((len = inStream.read(buf)) > 0) {
                    if (task == null || task.isCancelled()) {
                        return false;
                    }
                    outputStream.write(buf, 0, len);
                }
            } catch (Exception e) {
                updateLogs(e.toString());
            }
            updateLogs(pageFile.getAbsolutePath());

            readData(pageFile);

            if (task == null || task.isCancelled()) {
                return false;
            }
            updateLogs(message("Importing") + " " + message("DataSize") + ":" + reports.size());
            return TableEpidemicReport.write(reports, replaceCheck.isSelected()) > 0;

        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }

    }

    protected void readData(File file) {
        try {
            String flagStart = "\"mapLastUpdatedTime\"";
            String flagEnd = ",\"dataSource\":";
            boolean started = false;
            StringBuilder s = new StringBuilder();
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("utf-8"))) {
                char[] buf = new char[1024];
                String bufStr;
                int len;
                while ((len = reader.read(buf)) > 0) {
                    if (task == null || task.isCancelled()) {
                        return;
                    }
                    bufStr = new String(buf, 0, len);
                    int pos = bufStr.indexOf(flagEnd);
                    if (pos >= 0) {
                        s.append(bufStr);
                        break;
                    }
                    pos = bufStr.indexOf(flagStart);
                    if (pos >= 0) {
                        s.append(bufStr.substring(pos));
                        started = true;
                    } else if (started) {
                        s.append(bufStr);
                    }
                }
            }

            String jsonString = StringTools.decodeUnicode(s.toString());
            File tempFile = FileTools.getTempFile("json");
            FileTools.writeFile(tempFile, jsonString);
            int pos = jsonString.indexOf("2020");
            String dateString = jsonString.substring(pos, pos + 10) + EpidemicReport.COVID19TIME;
            reportTime = DateTools.stringToDatetime(dateString, CommonValues.DatetimeFormat6);
            updateLogs(dateString);

            try ( Connection conn = DerbyBase.getConnection()) {
                while (true) {
                    if (task == null || task.isCancelled()) {
                        return;
                    }
                    int startPos = jsonString.indexOf("{\"confirmed\":\"");
                    if (startPos < 0) {
                        break;
                    }
                    jsonString = jsonString.substring(startPos);
                    int endPos = jsonString.indexOf("]},{");
                    if (endPos < 0) {
                        endPos = jsonString.indexOf("]}]");
                        if (endPos < 0) {
                            break;
                        }
                    }
                    String area = jsonString.substring(0, endPos + 2);
                    readArea(conn, area);
                    jsonString = jsonString.substring(endPos + 3);
                }

            } catch (Exception e) {
                MyBoxLog.error(e);
                MyBoxLog.debug(e.toString());
                updateLogs(e.toString());
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            updateLogs(e.toString());
        }
    }

    protected void readArea(Connection conn, String areaValues) {
        try {
            int confirmed = 0, crued = 0, died = 0, startPos, endPos;
            startPos = areaValues.indexOf("\"confirmed\":\"");
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"confirmed\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    confirmed = 0;
                } else {
                    confirmed = Integer.valueOf(field);
                }
            }
            startPos = areaValues.indexOf("\"died\":\"");
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"died\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    died = 0;
                } else {
                    died = Integer.valueOf(field);
                }
            }
            startPos = areaValues.indexOf("\"crued\":\"");
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"crued\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    crued = 0;
                } else {
                    crued = Integer.valueOf(field);
                }
            }

            startPos = areaValues.indexOf("\"area\":\"");
            String area = null;
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"area\":\"".length());
                endPos = field.indexOf("\"");
                area = field.substring(0, endPos);
            }

            String sql = "SELECT * FROM Geography_Code WHERE "
                    + " (level=3 OR level=4) AND ( " + TableGeographyCode.nameEqual(area) + " )";
            GeographyCode code = TableGeographyCode.queryCode(conn, sql, false);
            if (code == null) {
                code = new GeographyCode();
                code.setChineseName(area);
                code.setLevelCode(new GeographyCodeLevel((short) 3));
                TableGeographyCode.insert(conn, code);
            }
            EpidemicReport areaReport = create().
                    setDataSet(Dataset).setSource((short) 2)
                    .setTime(reportTime.getTime())
                    .setLocationid(code.getId()).setLocation(code)
                    .setConfirmed(confirmed).setHealed(crued).setDead(died);

            if (confirmed > 0 || died > 0 || crued > 0) {
                reports.add(areaReport);
                updateLogs(Dataset + " " + code.getName() + " " + confirmed + " " + crued + " " + died);
            }

            startPos = areaValues.indexOf("\"subList\":[");
            String cities;
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"subList\":[".length());
                endPos = field.indexOf("]");
                cities = field.substring(0, endPos);
                if (!cities.isBlank()) {
                    readCities(conn, areaReport, cities);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            updateLogs(e.toString());
        }
    }

    protected void readCities(Connection conn, EpidemicReport areaReport, String citiesValues) {
        try {
            String data = citiesValues;
            while (true) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                int startPos = data.indexOf("{");
                if (startPos < 0) {
                    break;
                }
                data = data.substring(startPos);
                int endPos = data.indexOf("}");
                if (endPos < 0) {
                    break;
                }
                String city = data.substring(0, endPos + 1);
                EpidemicReport cityReport = readCity(conn, areaReport, city);
                if (cityReport != null) {
                    reports.add(cityReport);
                }
                if (endPos >= data.length()) {
                    break;
                }
                data = data.substring(endPos + 1);
            }

        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected EpidemicReport readCity(Connection conn,
            EpidemicReport areaReport, String cityValues) {
        try {
            int confirmed = 0, crued = 0, died = 0, startPos, endPos;
            startPos = cityValues.indexOf("\"confirmed\":\"");
            if (startPos >= 0) {
                String field = cityValues.substring(startPos + "\"confirmed\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    confirmed = 0;
                } else {
                    confirmed = Integer.valueOf(field);
                }
            }
            startPos = cityValues.indexOf("\"died\":\"");
            if (startPos >= 0) {
                String field = cityValues.substring(startPos + "\"died\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    died = 0;
                } else {
                    died = Integer.valueOf(field);
                }
            }
            startPos = cityValues.indexOf("\"crued\":\"");
            if (startPos >= 0) {
                String field = cityValues.substring(startPos + "\"crued\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    crued = 0;
                } else {
                    crued = Integer.valueOf(field);
                }
            }
            startPos = cityValues.indexOf("\"city\":\"");
            String city = null;
            if (startPos >= 0) {
                String field = cityValues.substring(startPos + "\"city\":\"".length());
                endPos = field.indexOf("\"");
                city = field.substring(0, endPos);
            }

            if (confirmed <= 0 && died <= 0 && crued <= 0) {
                return null;
            }
            String sql = "SELECT * FROM Geography_Code WHERE ";
            GeographyCode area = areaReport.getLocation();
            if (area != null) {
                if (area.getCountry() > 0) {
                    sql += " country=" + area.getCountry() + " AND ";
                }
                if (area.getProvince() > 0) {
                    sql += " province=" + area.getProvince() + " AND ";
                }
            }
            sql += " (level=5 OR level=6) AND ( " + TableGeographyCode.nameEqual(city) + " )";
            GeographyCode code = TableGeographyCode.queryCode(conn, sql, false);
            if (code == null) {
                code = new GeographyCode();
                code.setChineseName(city);
                code.setLevelCode(new GeographyCodeLevel((short) 5));
                code.setCountry(area.getCountry());
                code.setProvince(area.getProvince());
                TableGeographyCode.insert(conn, code);
            }
            EpidemicReport cityReport = create().
                    setDataSet(Dataset).setSource((short) 2)
                    .setTime(reportTime.getTime())
                    .setLocationid(code.getId()).setLocation(code)
                    .setConfirmed(confirmed).setHealed(crued).setDead(died);
            updateLogs(Dataset + " " + code.getName() + " " + confirmed + " " + crued + " " + died);

            return cityReport;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            updateLogs(e.toString());

        }
        return null;
    }

    @Override
    protected void afterSuccess() {
        if (statisticCheck.isSelected()) {
            startStatistic();
        } else {
            if (FxmlControl.askSure(getBaseTitle(), message("EpidemicReportStatistic"))) {
                startStatistic();
            }
        }
    }

    protected void startStatistic() {
        EpidemicReportsController savedP = (EpidemicReportsController) parent;
        this.parent = null;
        EpidemicReportsStatisticController controller
                = (EpidemicReportsStatisticController) openStage(CommonValues.EpidemicReportsStatisticFxml);
        if (savedP != null) {
            controller.parent = savedP;
        }
        controller.start(Dataset);
        controller.getMyStage().requestFocus();
    }

}
