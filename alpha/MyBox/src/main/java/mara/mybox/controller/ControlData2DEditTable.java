package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.input.Clipboard;
import mara.mybox.data.DataClipboard;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DEditTable extends ControlData2DLoad {

    protected final SimpleBooleanProperty selectNotify;
    protected List<Integer> checkedRowsIndices, checkedColsIndices;

    @FXML
    protected CheckBox columnsCheck;

    public ControlData2DEditTable() {
        forDisplay = false;
        selectNotify = new SimpleBooleanProperty(false);
    }

    protected void setParameters(ControlData2DEdit editController) {
        try {
            dataController = editController.dataController;

            paginationPane = dataController.paginationPane;
            pageSizeSelector = dataController.pageSizeSelector;
            pageSelector = dataController.pageSelector;
            pageLabel = dataController.pageLabel;
            dataSizeLabel = dataController.dataSizeLabel;
            selectedLabel = dataController.selectedLabel;
            pagePreviousButton = dataController.pagePreviousButton;
            pageNextButton = dataController.pageNextButton;
            pageFirstButton = dataController.pageFirstButton;
            pageLastButton = dataController.pageLastButton;
            saveButton = dataController.saveButton;

            tableView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener() {
                @Override
                public void onChanged(ListChangeListener.Change c) {
                    notifySelect();
                }
            });

            columnsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (columnsCheck.isSelected()) {
                        selectAllCols();
                    } else {
                        selectNoneCols();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        table
     */
    @Override
    public synchronized void tableChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        data2D.setTableChanged(changed);
        validateData();
        notifyStatus();

        dataController.textController.loadData();
        dataController.viewController.loadData();
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        return dataController.checkBeforeNextAction();
    }

    @Override
    public List<String> newData() {
        return data2D.newRow();
    }

    @Override
    public List<String> dataCopy(List<String> data) {
        return data2D.copyRow(data);
    }

    /*
        selection
     */
    @Override
    public void makeColumns() {
        try {
            if (!validateData()) {
                return;
            }
            super.makeColumns();
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = new CheckBox(tableColumn.getText());
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        notifySelect();
                    }
                });
                tableColumn.setGraphic(cb);
                tableColumn.setText(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        restoreSelections();
    }

    public void notifySelect() {
        if (isSettingValues) {
            return;
        }
        selectNotify.set(!selectNotify.get());
    }

    @FXML
    public void selectAllCols() {
        try {
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(true);
            }
            isSettingValues = false;
            notifySelect();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectNoneCols() {
        try {
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(false);
            }
            isSettingValues = false;
            notifySelect();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public List<Integer> checkedRowsIndices(boolean allRows) {
        try {
            checkedRowsIndices = new ArrayList<>();
            if (allRows) {
                for (int i = 0; i < tableData.size(); i++) {
                    checkedRowsIndices.add(i);
                }
            } else {
                List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
                for (int i : selected) {
                    checkedRowsIndices.add(i);
                }
            }
            return checkedRowsIndices;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<Integer> checkedColsIndices() {
        try {
            checkedColsIndices = new ArrayList<>();
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                if (cb.isSelected()) {
                    int col = data2D.colOrder(cb.getText());
                    if (col >= 0) {
                        checkedColsIndices.add(col);
                    }
                }
            }
            return checkedColsIndices;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<String> checkedColsNames() {
        try {
            List<String> names = new ArrayList<>();
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                if (cb.isSelected()) {
                    names.add(cb.getText());
                }
            }
            return names;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<Data2DColumn> checkedCols() {
        try {
            List<Data2DColumn> cols = new ArrayList<>();
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                if (cb.isSelected()) {
                    Data2DColumn col = data2D.col(cb.getText());
                    if (col != null) {
                        cols.add(col.cloneAll());
                    }
                }
            }
            return cols;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<List<String>> selectedData(boolean all, boolean rowNumber, boolean colName) {
        try {
            if (!checkSelections(all)) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            if (colName) {
                List<String> names = checkedColsNames();
                if (rowNumber) {
                    names.add(0, message("RowNumber"));
                }
                data.add(names);
            }
            int size = tableData.size();
            for (int row : checkedRowsIndices) {
                if (row < 0 || row >= size) {
                    continue;
                }
                List<String> tableRow = tableData.get(row);
                List<String> newRow = new ArrayList<>();
                if (rowNumber) {
                    newRow.add((row + 1) + "");
                }
                for (int col : checkedColsIndices) {
                    int index = col + 1;
                    if (index < 0 || index >= tableRow.size()) {
                        continue;
                    }
                    newRow.add(tableRow.get(index));
                }
                data.add(newRow);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public boolean checkSelections(boolean allRows) {
        checkedRowsIndices = checkedRowsIndices(allRows);
        checkedColsIndices = checkedColsIndices();
        return checkedRowsIndices != null && !checkedRowsIndices.isEmpty()
                && checkedColsIndices != null && !checkedColsIndices.isEmpty();
    }

    public boolean isSquare(boolean allRows) {
        checkedRowsIndices = checkedRowsIndices(allRows);
        checkedColsIndices = checkedColsIndices();
        return checkedRowsIndices != null && checkedColsIndices != null
                && !checkedRowsIndices.isEmpty()
                && checkedRowsIndices.size() == checkedColsIndices.size();
    }

    public void selectRows(List<Integer> rows) {
        try {
            isSettingValues = true;
            if (rows != null && !rows.isEmpty()) {
                for (int i = 0; i < tableData.size(); i++) {
                    if (rows.contains(i)) {
                        tableView.getSelectionModel().select(i);
                    } else {
                        tableView.getSelectionModel().clearSelection(i);
                    }
                }
            } else {
                tableView.getSelectionModel().clearSelection();
            }
            isSettingValues = false;
            notifySelect();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void selectCols(List<Integer> cols) {
        try {
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                if (cols != null && !cols.isEmpty()) {
                    int col = data2D.colOrder(cb.getText());
                    cb.setSelected(col >= 0 && cols.contains(col));
                } else {
                    cb.setSelected(false);
                }
            }
            isSettingValues = false;
            notifySelect();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void restoreSelections() {
        selectRows(checkedRowsIndices);
        selectCols(checkedColsIndices);
    }

    /*
        action
     */
    @FXML
    @Override
    public void addAction() {
        if (!validateData()) {
            return;
        }
        addRowsAction();
    }

    @FXML
    @Override
    public void deleteAction() {
        deleteRowsAction();
    }

    @FXML
    @Override
    public void copyAction() {
        if (!validateData()) {
            return;
        }
        Data2DCopyController.open(this);
    }

    public void copyToSystemClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            String text = TextTools.dataText(data, ",", names, null);
            TextClipboardTools.copyToSystemClipboard(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void copyToMyBoxClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            copyToMyBoxClipboard2(data2D.toColumns(names), data);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void copyToMyBoxClipboard2(List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            SingletonTask copyTask = new SingletonTask<Void>(this) {

                private DataClipboard clip;

                @Override
                protected boolean handle() {
                    clip = DataClipboard.create(task, cols, data);
                    return clip != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataClipboardController controller = DataClipboardController.oneOpen();
                    controller.load(clip);
                    popDone();
                }

            };
            start(copyTask, false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void pasteContentInSystemClipboard() {
        try {
            if (data2D == null) {
                return;
            }
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            Data2DPasteController.open(this, text, true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void pasteContentInMyboxClipboard() {
        try {
            if (data2D == null) {
                return;
            }
            DataClipboardPopController.open(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void statistic() {
        if (!validateData()) {
            return;
        }
        Data2DOperateController.open(this, "statistic");
    }

    public void percentage() {
        if (!validateData()) {
            return;
        }
        Data2DOperateController.open(this, "percentage");
    }

}
