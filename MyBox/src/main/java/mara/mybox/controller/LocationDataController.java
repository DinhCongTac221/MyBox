package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.data.Era;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.BaseDataTools;
import mara.mybox.db.data.Dataset;
import mara.mybox.db.data.Location;
import mara.mybox.db.table.TableDataset;
import mara.mybox.db.table.TableLocationData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.fxml.FxmlStage.openScene;
import mara.mybox.fxml.TableCoordinateSystemCell;
import mara.mybox.fxml.TableDoubleCell;
import mara.mybox.fxml.TableLatitudeCell;
import mara.mybox.fxml.TableLongitudeCell;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.tableMessage;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationDataController extends BaseDataManageController<Location> {

    protected TableLocationData tableLocationData;
    protected TableDataset tableDataset;
    protected List<Dataset> datasets;

    @FXML
    protected LocationDataSourceController sourceController;
    @FXML
    protected LocationDataMapController mapController;
    @FXML
    protected ControlTimeTree timeController;
    @FXML
    protected TableColumn<Location, Long> dataidColumn;
    @FXML
    protected TableColumn<Location, String> datasetColumn, labelColumn, addressColumn,
            commentsColumn, imageColumn, startTimeColumn, endTimeColumn, durationColumn;
    @FXML
    protected TableColumn<Location, Double> longitudeColumn, latitudeColumn, altitudeColumn,
            valueColumn, sizeColumn, precisionColumn, speedColumn;
    @FXML
    protected TableColumn<Location, CoordinateSystem> coordinateSystemColumn;
    @FXML
    protected TableColumn<Location, Integer> directionColumn;
    @FXML
    protected Button datesetButton;
    @FXML
    protected Tab mapTab;

    public LocationDataController() {
        baseTitle = AppVariables.message("LocationData");
        TipsLabelKey = "LocationDataTips";
    }

    @Override
    public void setTableDefinition() {
        tableLocationData = new TableLocationData();
        tableDefinition = tableLocationData;
        tableDataset = new TableDataset();
        viewDefinition = new TableLocationData(false).readDefinitionFromDB("Location_Data_View");
    }

    @Override
    public void setTableValues() {
        queryPrefix = TableLocationData.ViewSelect;
        sizePrefix = TableLocationData.SizeSelectPrefix;
        clearPrefix = TableLocationData.ClearPrefix;
        String html = viewDefinition.columnsTable() + "</BR><HR>"
                + tableDefinition.createTableStatement().replaceAll("\n", "</BR>") + "</BR></BR>"
                + tableDataset.createTableStatement().replaceAll("\n", "</BR>") + "</BR></BR>"
                + TableLocationData.CreateView.replaceAll("\n", "</BR>");
        tableDefinitionString = HtmlTools.html(tableName, html);
    }

    @Override
    protected void initColumns() {
        try {
            dataidColumn.setCellValueFactory(new PropertyValueFactory<>("ldid"));
            datasetColumn.setCellValueFactory(new PropertyValueFactory<>("datasetName"));
            labelColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
            addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            longitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
            longitudeColumn.setCellFactory(new TableLongitudeCell());
            latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
            latitudeColumn.setCellFactory(new TableLatitudeCell());
            altitudeColumn.setCellValueFactory(new PropertyValueFactory<>("altitude"));
            altitudeColumn.setCellFactory(new TableDoubleCell());
            coordinateSystemColumn.setCellValueFactory(new PropertyValueFactory<>("coordinateSystem"));
            coordinateSystemColumn.setCellFactory(new TableCoordinateSystemCell());
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("dataValue"));
            valueColumn.setCellFactory(new TableDoubleCell());
            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("dataSize"));
            sizeColumn.setCellFactory(new TableDoubleCell());
            precisionColumn.setCellValueFactory(new PropertyValueFactory<>("precision"));
            precisionColumn.setCellFactory(new TableDoubleCell());
            speedColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));
            speedColumn.setCellFactory(new TableDoubleCell());
            startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTimeText"));
            endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTimeText"));
            durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationText"));
            directionColumn.setCellValueFactory(new PropertyValueFactory<>("direction"));
            commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
            imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            mapController.initMap(this);

            FxmlControl.setTooltip(datesetButton, message("Dataset"));

            loadTrees(false);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected String checkWhere() {
        String sourceConditions = sourceController.check();
        String timeConditions = timeController.check();
        if (sourceConditions == null) {
            popError(message("MissDataset") + "\n" + message("SetConditionsComments"));
            return null;
        }
        if (timeConditions == null) {
            popError(message("MissTime") + "\n" + message("SetConditionsComments"));
            return null;
        }
        sourceConditions = sourceConditions.trim();
        timeConditions = timeConditions.trim();
        String condition;
        if (sourceConditions.isBlank()) {
            condition = timeConditions.isBlank() ? "" : timeConditions;
        } else {
            condition = timeConditions.isBlank() ? sourceConditions
                    : "( " + sourceConditions + ") AND (" + timeConditions + " )";
        }
        return condition;
    }

    @Override
    protected String checkTitle() {
        return sourceController.getFinalTitle() + "\n"
                + timeController.getFinalTitle();
    }

    public void loadTrees(boolean load) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        sourceController.clearTree();
        timeController.clearTree();
        mapController.initFrames();
        if (load) {
            tableData.clear();
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private List<Date> times;

                @Override
                protected boolean handle() {
                    datasets = tableLocationData.datasets();
                    times = TableLocationData.times();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (datasets == null || datasets.isEmpty()) {
                        addButton.setDisable(true);
                        askImportPredefined();
                    } else {
                        addButton.setDisable(false);
                        sourceController.loadTree(datasets);
                        timeController.loadTree("start_time", times, true);
                        if (load) {
                            queryData();
                        }
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void askImportPredefined() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(baseTitle);
        alert.setHeaderText(message("ImportExamples") + " ?");
        alert.setContentText("1. " + message("ChineseHistoricalCapitals") + "\n"
                + "2. " + message("AutumnMovementPatternsOfEuropeanGadwalls") + "\n"
                + "3. " + message("SpermWhalesGulfOfMexico"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType button1 = new ButtonType("1");
        ButtonType button2 = new ButtonType("2");
        ButtonType button3 = new ButtonType("3");
        ButtonType buttonNo = new ButtonType(AppVariables.message("No"));
        alert.getButtonTypes().setAll(button1, button2, button3, buttonNo);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == button1) {
            ChineseHistoricalCapitals();
        } else if (result.get() == button2) {
            EuropeanGadwalls();
        } else if (result.get() == button3) {
            SpermWhales();
        } else {
            try {
                DatasetEditController controller = (DatasetEditController) FxmlStage.openStage(CommonValues.DatasetEditFxml);
                controller.initEditor(this, null);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        }
    }

    @FXML
    @Override
    public void queryData() {
        if (isSettingValues) {
            return;
        }
        super.queryData();
        if (!mapCurrentPage()) {
            queryMap();
        }
    }

    @Override
    public boolean preLoadingTableData() {
        if (super.preLoadingTableData()) {
            if (mapCurrentPage()) {
                mapController.clearAction();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void postLoadedTableData() {
        if (queryCondition == null) {
            return;
        }
        super.postLoadedTableData();
        if (mapCurrentPage()) {
            mapController.drawLocationData(tableData, finalTitle);
        }
    }

    public boolean mapCurrentPage() {
        return paginate && mapController.mapOptionsController.currentPageRadio.isSelected();
    }

    @Override
    protected String loadMoreInfo() {
        String s = "<SPAN class=\"boldText\">" + message("MapQuery") + ": </SPAN></br>";
        if (mapCurrentPage()) {
            s += "<SPAN class=\"valueText\">" + message("CurrentPage") + "</SPAN></br>"
                    + "<SPAN class=\"boldText\">" + message("DataNumber") + ": </SPAN>"
                    + "<SPAN class=\"valueText\">" + (tableData != null ? tableData.size() : "") + "</SPAN></br></br>";
        } else {
            s += "<SPAN class=\"valueText\">" + message("CurrentQuery") + "</SPAN></br>"
                    + "<SPAN class=\"boldText\">" + message("DataNumber") + ": </SPAN>"
                    + "<SPAN class=\"valueText\">"
                    + (mapController.locations != null ? mapController.locations.size() : "")
                    + "</SPAN></br></br>";
        }
        return s;
    }

    @Override
    public void reloadChart() {
        if (isSettingValues) {
            return;
        }
        mapController.clearAction();
        if (mapCurrentPage()) {
            mapController.drawLocationData(tableData, finalTitle);
        } else {
            queryMap();
        }
    }

    public void queryMap() {
        if (isSettingValues || queryCondition == null || dataQuerySQL == null) {
            return;
        }
        synchronized (this) {
            if (backgroundTask != null && !backgroundTask.isQuit()) {
                return;
            }
            backgroundTask = new SingletonTask<Void>() {
                private List<Location> mapData;

                @Override
                protected boolean handle() {
                    mapData = tableLocationData.readData(dataQuerySQL,
                            mapController.mapOptionsController.dataMax);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    mapController.drawLocationData(mapData, queryCondition.getTitle());
                    loadInfo();
                }

                @Override
                protected void taskQuit() {
                    super.taskQuit();
                    backgroundTask = null;
                }
            };
            openHandlingStage(backgroundTask, Modality.WINDOW_MODAL);
            backgroundTask.setSelf(backgroundTask);
            Thread thread = new Thread(backgroundTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTrees(true);
    }

    @FXML
    @Override
    public void addAction(ActionEvent event) {
        try {
            LocationDataEditController controller
                    = (LocationDataEditController) openScene(null, CommonValues.LocationDataEditFxml);
            controller.initEditor(this, null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void editAction(ActionEvent event) {
        Location selected = (Location) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            LocationDataEditController controller
                    = (LocationDataEditController) openScene(null, CommonValues.LocationDataEditFxml);
            controller.initEditor(this, selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void viewAction() {
        Location selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        HtmlTools.viewHtml(message("LocationData"), BaseDataTools.displayData(tableDefinition, selected, null, true));
    }

    @FXML
    public void locationAction(ActionEvent event) {
        try {
            Location selected = (Location) tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            LocationInMapController controller
                    = (LocationInMapController) openScene(null, CommonValues.LocationInMapFxml);
            controller.loadCoordinate(null, selected.getLongitude(), selected.getLatitude());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void datasetAction(ActionEvent event) {
        DatasetController controller = (DatasetController) openStage(CommonValues.DatasetFxml);
        controller.load(this, tableMessage("Location_data"));
    }

    @FXML
    @Override
    protected void popImportMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("ImportLocationDataCVS"));
            menu.setOnAction((ActionEvent event) -> {
                LocationDataImportCSVController controller
                        = (LocationDataImportCSVController) openStage(CommonValues.LocationDataImportCSVFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImportLocationDataMovebank"));
            menu.setOnAction((ActionEvent event) -> {
                LocationDataImportMovebankController controller
                        = (LocationDataImportMovebankController) openStage(CommonValues.LocationDataImportMovebankFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ImportLocationDataChineseCapitals"));
            menu.setOnAction((ActionEvent event) -> {
                ChineseHistoricalCapitals();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImportLocationDataEuropeanGadwalls"));
            menu.setOnAction((ActionEvent event) -> {
                EuropeanGadwalls();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImportLocationDataSpermWhales"));
            menu.setOnAction((ActionEvent event) -> {
                SpermWhales();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void ChineseHistoricalCapitals() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            LocationDataController currentController = this;
            task = new SingletonTask<Void>() {
                private File file;
                String datasetName;

                @Override
                protected boolean handle() {
                    File image = FxmlControl.getInternalFile("/img/jade.png", "image", "jade.png", false);
                    datasetName = message("ChineseHistoricalCapitals");
                    try ( Connection conn = DerbyBase.getConnection()) {
                        conn.setAutoCommit(true);
                        tableLocationData.delete(conn, datasetName, true);
                        Dataset dataset = Dataset.create()
                                .setDataCategory("Location_Data")
                                .setDataSet(datasetName)
                                .setTimeFormat(Era.Format.Year)
                                .setOmitAD(false)
                                .setTextColor(Color.web("#622A1D"))
                                .setImage(image);
                        tableDataset.insertData(conn, dataset);
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                    String lang = AppVariables.isChinese() ? "zh" : "en";
                    file = FxmlControl.getInternalFile("/data/db/Location_Data_ChineseHistoricalCapitals_" + lang + ".csv",
                            "data", "Location_Data_ChineseHistoricalCapitals_" + lang + ".csv", true);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    sourceController.select(datasetName);
                    timeController.selectAllAction();
                    mapController.isSettingValues = true;
                    mapController.sequenceRadio.fire();
                    mapController.accumulateCheck.setSelected(true);
                    mapController.overlayCheck.setSelected(true);
                    mapController.centerCheck.setSelected(false);
                    mapController.linkCheck.setSelected(false);
                    mapController.intervalSelector.getSelectionModel().select("50");
                    mapController.mapOptionsController.isSettingValues = true;
                    mapController.mapOptionsController.currentQueryRadio.fire();
                    mapController.mapOptionsController.dataMaximumSelector.getSelectionModel().select("300");
                    mapController.mapOptionsController.markerDatasetRadio.fire();
                    mapController.mapOptionsController.markerLabelCheck.setSelected(true);
                    mapController.mapOptionsController.markerStartCheck.setSelected(true);
                    mapController.mapOptionsController.markerEndCheck.setSelected(true);
                    mapController.mapOptionsController.markerAddressCheck.setSelected(false);
                    mapController.mapOptionsController.markerCoordinateCheck.setSelected(false);
                    mapController.mapOptionsController.markerDatasetCheck.setSelected(false);
                    mapController.mapOptionsController.markerValueCheck.setSelected(false);
                    mapController.mapOptionsController.markerSizeCheck.setSelected(false);
                    mapController.mapOptionsController.markerDurationCheck.setSelected(false);
                    mapController.mapOptionsController.markerSpeedCheck.setSelected(false);
                    mapController.mapOptionsController.markerDirectionCheck.setSelected(false);
                    mapController.mapOptionsController.dataColorRadio.fire();
                    mapController.mapOptionsController.isSettingValues = false;
                    mapController.mapOptionsController.setMapSize(5, false, true);
                    mapController.isSettingValues = false;
                    tabsPane.getSelectionModel().select(mapTab);
                    LocationDataImportCSVController controller
                            = (LocationDataImportCSVController) openStage(CommonValues.LocationDataImportCSVFxml);
                    controller.parent = currentController;
                    controller.startFile(file, true, true);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    protected void EuropeanGadwalls() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            LocationDataController currentController = this;
            task = new SingletonTask<Void>() {
                private File file;
                private String datasetName;

                @Override
                protected boolean handle() {
                    File image = FxmlControl.getInternalFile("/img/Gadwalls.png", "image", "Gadwalls.png", false);
                    datasetName = message("AutumnMovementPatternsOfEuropeanGadwalls");
                    try ( Connection conn = DerbyBase.getConnection()) {
                        conn.setAutoCommit(true);
                        tableLocationData.delete(conn, datasetName, true);
                        Dataset dataset = Dataset.create()
                                .setDataCategory("Location_Data")
                                .setDataSet(datasetName)
                                .setTimeFormat(Era.Format.Datetime)
                                .setOmitAD(true)
                                .setTextColor(Color.web("#432918"))
                                .setImage(image)
                                .setComments("https://www.datarepository.movebank.org/handle/10255/move.346");
                        tableDataset.insertData(conn, dataset);
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                    String lang = AppVariables.isChinese() ? "zh" : "en";
                    file = FxmlControl.getInternalFile("/data/db/Location_Data_EuropeanGadwalls_" + lang + ".csv",
                            "data", "Location_Data_EuropeanGadwalls_" + lang + ".csv", true);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    sourceController.select(datasetName);
                    timeController.selectAllAction();
                    mapController.isSettingValues = true;
                    mapController.distributionRadio.fire();
                    mapController.mapOptionsController.isSettingValues = true;
                    mapController.mapOptionsController.currentQueryRadio.fire();
                    mapController.mapOptionsController.dataMaximumSelector.getSelectionModel().select("3000");
                    mapController.mapOptionsController.markerDatasetRadio.fire();
                    mapController.mapOptionsController.markerLabelCheck.setSelected(false);
                    mapController.mapOptionsController.markerStartCheck.setSelected(false);
                    mapController.mapOptionsController.markerEndCheck.setSelected(false);
                    mapController.mapOptionsController.markerAddressCheck.setSelected(false);
                    mapController.mapOptionsController.markerCoordinateCheck.setSelected(false);
                    mapController.mapOptionsController.markerDatasetCheck.setSelected(false);
                    mapController.mapOptionsController.markerValueCheck.setSelected(false);
                    mapController.mapOptionsController.markerSizeCheck.setSelected(false);
                    mapController.mapOptionsController.markerDurationCheck.setSelected(false);
                    mapController.mapOptionsController.markerSpeedCheck.setSelected(false);
                    mapController.mapOptionsController.markerDirectionCheck.setSelected(false);
                    mapController.mapOptionsController.dataColorRadio.fire();
                    mapController.mapOptionsController.isSettingValues = false;
                    mapController.mapOptionsController.setMapSize(6, false, true);
                    mapController.isSettingValues = false;
                    tabsPane.getSelectionModel().select(mapTab);
                    LocationDataImportCSVController controller
                            = (LocationDataImportCSVController) openStage(CommonValues.LocationDataImportCSVFxml);
                    controller.parent = currentController;
                    controller.startFile(file, true, true);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    protected void SpermWhales() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            LocationDataController currentController = this;
            task = new SingletonTask<Void>() {
                private File file;
                private String datasetName;

                @Override
                protected boolean handle() {
                    File image = FxmlControl.getInternalFile("/img/SpermWhale.png", "image", "SpermWhale.png", false);
                    datasetName = message("SpermWhalesGulfOfMexico");
                    try ( Connection conn = DerbyBase.getConnection()) {
                        conn.setAutoCommit(true);
                        tableLocationData.delete(conn, datasetName, true);
                        Dataset dataset = Dataset.create()
                                .setDataCategory("Location_Data")
                                .setDataSet(datasetName)
                                .setTimeFormat(Era.Format.Datetime)
                                .setOmitAD(true)
                                .setTextColor(Color.web("#432918"))
                                .setImage(image)
                                .setComments("https://www.datarepository.movebank.org/handle/10255/move.1059");
                        tableDataset.insertData(conn, dataset);
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                    String lang = AppVariables.isChinese() ? "zh" : "en";
                    file = FxmlControl.getInternalFile("/data/db/Location_Data_SpermWhales_" + lang + ".csv",
                            "data", "Location_Data_SpermWhales_" + lang + ".csv", true);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    sourceController.select(datasetName);
                    timeController.selectAllAction();
                    mapController.isSettingValues = true;
                    mapController.distributionRadio.fire();
                    mapController.mapOptionsController.isSettingValues = true;
                    mapController.mapOptionsController.currentQueryRadio.fire();
                    mapController.mapOptionsController.dataMaximumSelector.getSelectionModel().select("8000");
                    mapController.mapOptionsController.markerDatasetRadio.fire();
                    mapController.mapOptionsController.markerLabelCheck.setSelected(false);
                    mapController.mapOptionsController.markerStartCheck.setSelected(false);
                    mapController.mapOptionsController.markerEndCheck.setSelected(false);
                    mapController.mapOptionsController.markerAddressCheck.setSelected(false);
                    mapController.mapOptionsController.markerCoordinateCheck.setSelected(false);
                    mapController.mapOptionsController.markerDatasetCheck.setSelected(false);
                    mapController.mapOptionsController.markerValueCheck.setSelected(false);
                    mapController.mapOptionsController.markerSizeCheck.setSelected(false);
                    mapController.mapOptionsController.markerDurationCheck.setSelected(false);
                    mapController.mapOptionsController.markerSpeedCheck.setSelected(false);
                    mapController.mapOptionsController.markerDirectionCheck.setSelected(false);
                    mapController.mapOptionsController.dataColorRadio.fire();
                    mapController.mapOptionsController.isSettingValues = false;
                    mapController.mapOptionsController.setMapSize(5, false, true);
                    mapController.isSettingValues = false;
                    tabsPane.getSelectionModel().select(mapTab);
                    LocationDataImportCSVController controller
                            = (LocationDataImportCSVController) openStage(CommonValues.LocationDataImportCSVFxml);
                    controller.parent = currentController;
                    controller.startFile(file, true, true);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @Override
    public boolean leavingScene() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            sourceController.leavingScene();
            timeController.leavingScene();
            mapController.leavingScene();
        } catch (Exception e) {
        }
        return super.leavingScene();

    }

}
