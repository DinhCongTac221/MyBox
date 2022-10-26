package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.Data2D_Operations.ObjectType;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFilter;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.reader.DataTableGroup;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DHandleController extends BaseData2DSourceController {

    protected List<List<String>> outputData;
    protected List<Data2DColumn> outputColumns;
    protected int scale, defaultScale = 2, maxData = -1;
    protected ObjectType objectType;
    protected InvalidAs invalidAs = InvalidAs.Skip;
    protected List<String> orders;

    @FXML
    protected ControlData2DGroup groupController;
    @FXML
    protected ControlSelection sortController;
    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected CheckBox rowNumberCheck, colNameCheck;
    @FXML
    protected Label infoLabel, dataSelectionLabel;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected ToggleGroup objectGroup;
    @FXML
    protected TextField maxInput;
    @FXML
    protected RadioButton columnsRadio, rowsRadio, allRadio,
            skipNonnumericRadio, zeroNonnumericRadio, blankNonnumericRadio;
    @FXML
    protected ImageView tableTipsView;
    @FXML
    protected ComboBox<String> colSelector;

    public BaseData2DHandleController() {
        baseTitle = message("Handle");
    }

    @Override
    public void setStageStatus() {
        setAsNormal();
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            notSelectColumnsInTable(true);

            objectType = ObjectType.Columns;
            if (objectGroup != null) {
                objectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        objectChanged();
                    }
                });
                objectChanged();
            }

            scale = (short) UserConfig.getInt(baseName + "Scale", defaultScale);
            if (scale < 0) {
                scale = defaultScale;
            }
            if (scaleSelector != null) {
                scaleSelector.getItems().addAll(
                        Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
                );
                scaleSelector.setValue(scale + "");
                scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        scaleChanged();
                    }
                });
            }

            if (colSelector != null) {
                colSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkOptions();
                    }
                });
            }

            if (groupController != null) {
                groupController.setParameters(this);
            }

            if (sortController != null) {
                sortController.setParameters(this, message("Sort"), message("DataSortLabel"));
            }

            maxData = UserConfig.getInt(baseName + "MaxDataNumber", -1);
            if (maxInput != null) {
                if (maxData > 0) {
                    maxInput.setText(maxData + "");
                }
                maxInput.setStyle(null);
                maxInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        String maxs = maxInput.getText();
                        if (maxs == null || maxs.isBlank()) {
                            maxData = -1;
                            maxInput.setStyle(null);
                            UserConfig.setLong(baseName + "MaxDataNumber", -1);
                        } else {
                            try {
                                maxData = Integer.valueOf(maxs);
                                maxInput.setStyle(null);
                                UserConfig.setLong(baseName + "MaxDataNumber", maxData);
                            } catch (Exception e) {
                                maxInput.setStyle(UserConfig.badStyle());
                            }
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean scaleChanged() {
        try {
            int v = Integer.parseInt(scaleSelector.getValue());
            if (v >= 0 && v <= 15) {
                scale = (short) v;
                UserConfig.setInt(baseName + "Scale", v);
                scaleSelector.getEditor().setStyle(null);
                return true;
            } else {
                scaleSelector.getEditor().setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
        }
        return false;
    }

    public void setParameters(ControlData2DLoad tableController) {
        try {
            setParameters(this, tableController);

            if (targetController != null) {
                targetController.setParameters(this, tableController);
            }

            if (rowNumberCheck != null) {
                rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "CopyRowNumber", false));
                rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        rowNumberCheckChanged();
                    }
                });
            }
            if (colNameCheck != null) {
                colNameCheck.setSelected(UserConfig.getBoolean(baseName + "CopyColNames", true));
                colNameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "CopyColNames", colNameCheck.isSelected());
                    }
                });
            }

            loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();

            if (colSelector != null) {
                List<String> names = data2D.columnNames();
                if (names == null || names.isEmpty()) {
                    colSelector.getItems().clear();
                    return;
                }
                String selectedCol = colSelector.getSelectionModel().getSelectedItem();
                isSettingValues = true;
                colSelector.getItems().setAll(names);
                if (selectedCol != null && names.contains(selectedCol)) {
                    colSelector.setValue(selectedCol);
                } else {
                    colSelector.getSelectionModel().select(0);
                }
                isSettingValues = false;
            }

            if (groupController != null) {
                groupController.refreshControls();
            }

            makeSortList();

            checkOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeSortList() {
        try {
            if (sortController == null) {
                return;
            }
            if (!data2D.isValid()) {
                sortController.loadNames(null);
                return;
            }
            List<String> names = new ArrayList<>();
            for (String name : data2D.columnNames()) {
                names.add(name + "-" + message("Descending"));
                names.add(name + "-" + message("Ascending"));
            }
            sortController.loadNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceChanged() {
        if (tableController == null) {
            return;
        }
        super.sourceChanged();
        getMyStage().setTitle(baseTitle + (data2D == null ? "" : " - " + data2D.displayName()));
    }

    public void objectChanged() {
        checkObject();
    }

    public void checkObject() {
        if (rowsRadio == null) {
            return;
        }
        if (rowsRadio.isSelected()) {
            objectType = ObjectType.Rows;
        } else if (allRadio != null && allRadio.isSelected()) {
            objectType = ObjectType.All;
        } else {
            objectType = ObjectType.Columns;
        }
    }

    public void checkInvalidAs() {
        if (zeroNonnumericRadio != null && zeroNonnumericRadio.isSelected()) {
            invalidAs = InvalidAs.Zero;
        } else if (blankNonnumericRadio != null && blankNonnumericRadio.isSelected()) {
            invalidAs = InvalidAs.Blank;
        } else if (skipNonnumericRadio != null && skipNonnumericRadio.isSelected()) {
            invalidAs = InvalidAs.Skip;
        } else {
            invalidAs = InvalidAs.Blank;
        }
    }

    public void rowNumberCheckChanged() {
        UserConfig.setBoolean(baseName + "CopyRowNumber", rowNumberCheck.isSelected());
    }

    // Check when selections are changed
    public boolean checkOptions() {
        try {
            if (isSettingValues) {
                return true;
            }
            outOptionsError(null);
            if (data2D == null || !data2D.hasData()) {
                outOptionsError(message("NoData"));
                return false;
            }
            if (!checkSelections()) {
                return false;
            }
            if (targetController != null) {
                targetController.setNotInTable(isAllPages());
                if (targetController.checkTarget() == null) {
                    outOptionsError(message("SelectToHandle") + ": " + message("Target"));
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void outOptionsError(String error) {
        if (error != null && !error.isBlank()) {
            popError(error);
        }
    }

    // Check when "OK"/"Start" button is clicked
    public boolean initData() {
        try {
            if (groupController != null && !groupController.pickValues()) {
                return false;
            }

            checkObject();
            checkInvalidAs();

            if (sortController != null) {
                orders = sortController.selectedNames();
            } else {
                orders = null;
            }

            outputColumns = data2D.targetColumns(checkedColsIndices, otherColsIndices, showRowNumber(), null);

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions() || !initData()) {
            return;
        }
        showRightPane();
        preprocessStatistic();
    }

    public void preprocessStatistic() {
        List<String> scripts = new ArrayList<>();
        String filterScript = data2D.filterScipt();
        boolean hasFilterScript = filterScript != null && !filterScript.isBlank();
        if (hasFilterScript) {
            scripts.add(filterScript);
        }
        boolean hasGroupScripts = groupController != null
                && groupController.byConditions() && groupController.groupConditions != null;
        if (hasGroupScripts) {
            for (DataFilter filter : groupController.groupConditions) {
                String groupScript = filter.getSourceScript();
                if (groupScript != null && !groupScript.isBlank()) {
                    scripts.add(groupScript);
                }
            }
        }
        if (scripts.isEmpty()) {
            startOperation();
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                data2D.setTask(task);
                List<String> filledScripts = data2D.calculateScriptsStatistic(scripts);
                if (filledScripts == null || filledScripts.size() != scripts.size()) {
                    return true;
                }
                int index = 0;
                if (hasFilterScript) {
                    data2D.filter.setFilledScript(filledScripts.get(0));
                    index = 1;
                }
                if (hasGroupScripts) {
                    for (DataFilter filter : groupController.groupConditions) {
                        String groupScript = filter.getSourceScript();
                        if (groupScript != null && !groupScript.isBlank()) {
                            filter.setFilledScript(filledScripts.get(index++));
                        }
                    }
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
                if (ok) {
                    startOperation();
                }
            }

        };
        start(task);
    }

    public DataTable filteredData(List<Integer> colIndices, boolean showRowNumber) {
        try {
            if (colIndices == null) {
                return null;
            }
            Data2D tmp2D = data2D.cloneAll();
            if (groupController != null) {
                List<Data2DColumn> tmpColumns = new ArrayList<>();
                for (Data2DColumn column : data2D.columns) {
                    Data2DColumn tmpColumn = column.cloneAll();
                    String name = tmpColumn.getColumnName();
                    if (groupController.groupName != null && groupController.groupName.equals(name)) {
                        tmpColumn.setType(ColumnDefinition.ColumnType.Double);
                    }
                    tmpColumns.add(tmpColumn);
                }
                tmp2D.setColumns(tmpColumns);
            }
            tmp2D.startTask(task, filterController.filter);
            DataTable tmpTable;
            if (isAllPages()) {
                tmpTable = tmp2D.toTmpTable(task, colIndices, showRowNumber, false, invalidAs);
            } else {
                outputData = filtered(colIndices, showRowNumber);
                if (outputData == null || outputData.isEmpty()) {
                    return null;
                }
                tmpTable = tmp2D.toTmpTable(task, colIndices, outputData, showRowNumber, false, invalidAs);
                outputData = null;
            }
            tmp2D.stopFilter();
            return tmpTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<String> sortNames() {
        try {
            if (orders == null || orders.isEmpty()) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (String order : orders) {
                String name;
                if (order.endsWith("-" + message("Ascending"))) {
                    name = order.substring(0, order.length() - ("-" + message("Ascending")).length());
                } else {
                    name = order.substring(0, order.length() - ("-" + message("Descending")).length());
                }
                if (!names.contains(name)) {
                    names.add(name);
                }
            }
            return names;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataFileCSV sortedData(String dname, List<Integer> colIndices, boolean showRowNumber) {
        try {
            DataTable tmpTable = filteredData(colIndices, showRowNumber);
            if (tmpTable == null) {
                return null;
            }
            DataFileCSV csvData = tmpTable.sort(dname, task, orders, maxData, showColNames());
            tmpTable.drop();
            return csvData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataTableGroup groupData(DataTableGroup.TargetType targetType,
            List<String> copyNames, List<String> sorts, long max, int dscale) {
        try {
            if (groupController == null) {
                return null;
            }
            DataTable tmpTable = filteredData(data2D.columnIndices(), showRowNumber());
            List<String> tnames = new ArrayList<>();
            if (groupController.groupName != null) {
                tnames.add(groupController.groupName);
            } else if (groupController.groupNames != null) {
                tnames.addAll(groupController.groupNames);
            }
            for (String name : copyNames) {
                if (!tnames.contains(name)) {
                    tnames.add(name);
                }
            }
            DataTableGroup group = new DataTableGroup(data2D, tmpTable)
                    .setType(groupController.groupType())
                    .setGroupNames(groupController.groupNames)
                    .setGroupName(groupController.groupName)
                    .setSplitInterval(groupController.splitInterval())
                    .setSplitNumber(groupController.splitNumber())
                    .setSplitList(groupController.splitList())
                    .setConditions(groupController.groupConditions)
                    .setSorts(sorts).setMax(max)
                    .setScale(dscale).setInvalidAs(invalidAs).setTask(task)
                    .setTargetType(targetType)
                    .setTargetNames(tnames);
            return group;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    protected void startOperation() {
        try {
            if (isAllPages()) {
                handleAllTask();
            } else {
                handleRowsTask();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public List<List<String>> scaledData(List<Integer> cols, boolean needRowNumber) {
        try {
            List<List<String>> data;
            if (isAllPages()) {
                data = data2D.allRows(cols, needRowNumber);
            } else {
                data = filtered(cols, needRowNumber);
            }
            if (data == null || scaleSelector == null || scale < 0) {
                return data;
            }
            boolean needScale = false;
            for (int c : cols) {
                if (data2D.column(c).needScale()) {
                    needScale = true;
                    break;
                }
            }
            if (!needScale) {
                return data;
            }
            List<List<String>> scaled = new ArrayList<>();
            for (List<String> row : data) {
                List<String> srow = new ArrayList<>();
                if (needRowNumber) {
                    srow.add(row.get(0));
                }
                for (int i = 0; i < cols.size(); i++) {
                    String s = row.get(needRowNumber ? i + 1 : i);
                    if (s == null || !data2D.column(cols.get(i)).needScale() || scale < 0) {
                        srow.add(s);
                    } else {
                        srow.add(DoubleTools.scaleString(s, invalidAs, scale));
                    }
                }
                scaled.add(srow);
            }
            return scaled;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void handleAllTask() {
        if (targetController == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private DataFileCSV csvFile;

            @Override
            protected boolean handle() {
                data2D.startTask(task, filterController.filter);
                csvFile = generatedFile();
                data2D.stopFilter();
                return csvFile != null;
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                DataFileCSV.openCSV(myController, csvFile, targetController.target);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
            }

        };
        start(task);
    }

    public DataFileCSV generatedFile() {
        return null;
    }

    public void handleRowsTask() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    ok = handleRows();
                    data2D.stopFilter();
                    return ok;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ouputRows();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    public boolean showColNames() {
        return colNameCheck != null && colNameCheck.isSelected();
    }

    public boolean showRowNumber() {
        return rowNumberCheck != null && rowNumberCheck.isSelected();
    }

    public boolean handleRows() {
        try {
            outputData = filtered(showRowNumber());
            if (outputData == null) {
                return false;
            }
            if (showColNames()) {
                List<String> names = new ArrayList<>();
                for (Data2DColumn column : outputColumns) {
                    names.add(column.getColumnName());
                }
                outputData.add(0, names);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public void ouputRows() {
        if (targetController == null || targetController.inTable()) {
            updateTable();
        } else {
            outputExternal();
        }
    }

    public boolean updateTable() {
        try {
            if (targetController == null || !targetController.inTable() || outputData == null) {
                return false;
            }
            int row = targetController.row();
            int col = targetController.col();
            int rowsNumber = tableController.data2D.tableRowsNumber();
            int colsNumber = tableController.data2D.tableColsNumber();
            if (row < 0 || row >= rowsNumber || col < 0 || col >= colsNumber) {
                popError(message("InvalidParameters"));
                return false;
            }
            tableController.isSettingValues = true;
            if (targetController.replaceRadio.isSelected()) {
                for (int r = row; r < Math.min(row + outputData.size(), rowsNumber); r++) {
                    List<String> tableRow = tableController.tableData.get(r);
                    List<String> dataRow = outputData.get(r - row);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    tableController.tableData.set(r, tableRow);
                }
            } else {
                List<List<String>> newRows = new ArrayList<>();
                for (int r = 0; r < outputData.size(); r++) {
                    List<String> newRow = tableController.data2D.newRow();
                    List<String> dataRow = outputData.get(r);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        newRow.set(c + 1, dataRow.get(c - col));
                    }
                    newRows.add(newRow);
                }
                int index = targetController.insertRadio.isSelected() ? row : row + 1;
                tableController.tableData.addAll(index, newRows);
            }
            tableController.tableView.refresh();
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            tableController.requestMouse();
            popDone();
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean outputExternal() {
        if (targetController == null || targetController.target == null
                || outputData == null || outputData.isEmpty()) {
            popError(message("NoData"));
            return false;
        }
        String name = targetController.name();
        switch (targetController.target) {
            case "systemClipboard":
                tableController.copyToSystemClipboard(null, outputData);
                break;
            case "myBoxClipboard":
                tableController.toMyBoxClipboard(name, outputColumns, outputData);
                break;
            case "csv":
                DataFileCSVController.open(name, outputColumns, outputData);
                break;
            case "excel":
                DataFileExcelController.open(name, outputColumns, outputData);
                break;
            case "texts":
                DataFileTextController.open(name, outputColumns, outputData);
                break;
            case "matrix":
                MatricesManageController.open(name, outputColumns, outputData);
                break;
            case "table":
                DataTablesController.open(name, outputColumns, outputData);
                break;
        }
        popDone();
        return true;
    }

    public void cloneOptions(BaseData2DHandleController sourceController) {
        if (sourceController.allPagesRadio.isSelected()) {
            allPagesRadio.setSelected(true);
        } else if (sourceController.currentPageRadio.isSelected()) {
            currentPageRadio.setSelected(true);
        } else {
            selectedRadio.setSelected(true);
        }
        filterController.load(sourceController.filterController.scriptInput.getText(),
                sourceController.filterController.trueRadio.isSelected());
        filterController.maxInput.setText(sourceController.filterController.maxFilteredNumber + "");
        scaleSelector.getSelectionModel().select(sourceController.scale + "");
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    @Override
    public boolean keyF6() {
        close();
        return false;
    }

    @Override
    public void cleanPane() {
        try {
            super.cleanPane();
            tableController = null;
            data2D = null;
            outputData = null;
            outputColumns = null;
        } catch (Exception e) {
        }
    }

}
