package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.fxml.FXML;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class Data2DSetValuesController extends BaseData2DHandleController {

    @FXML
    protected ControlData2DSetValue valueController;

    public Data2DSetValuesController() {
        baseTitle = message("SetValues");
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

            idExclude(true);
            valueController.setParameter(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceChanged() {
        super.sourceChanged();
        valueController.setData2D(data2D);
    }

    @Override
    public void refreshControls() {
        try {
            if (data2D == null) {
                return;
            }
            if (data2D.isMutiplePages()) {
                allPagesRadio.setDisable(false);
            } else {
                if (allPagesRadio.isSelected()) {
                    currentPageRadio.fire();
                }
                allPagesRadio.setDisable(true);
            }
            showPaginationPane(false);
            restoreSelections();
            checkOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        ok = valueController.checkSelection() && ok;
        okButton.setDisable(!ok);
        return ok;
    }

    @Override
    public boolean initData() {
        if (isAllPages() && !tableController.checkBeforeNextAction()) {
            return false;
        }
        return PopTools.askSure(this, baseTitle, message("SureOverwriteColumns") + "\n" + checkedColsNames);
    }

    @Override
    public void preprocessStatistic() {
        List<String> scripts = new ArrayList<>();
        String filterScript = data2D.filterScipt();
        if (filterScript != null && !filterScript.isBlank()) {
            scripts.add(filterScript);
        }
        if (valueController.expressionRadio.isSelected()) {
            String expression = valueController.expression();
            if (expression == null || expression.isBlank()) {
                popError(message("Invalid") + ": " + message("RowExpression"));
                return;
            }
            scripts.add(expression);
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
                try {
                    data2D.setTask(task);
                    List<String> filled = data2D.calculateScriptsStatistic(scripts);
                    if (filled == null) {
                        return false;
                    }
                    String filledExp = null;
                    if (filterScript != null && !filterScript.isBlank()) {
                        String filledScript = filled.get(0);
                        if (filledScript == null || filledScript.isBlank()) {
                            error = message("Invalid") + ": " + message("RowFilter");
                            return false;
                        }
                        data2D.filter.setFilledScript(filledScript);
                        if (valueController.expressionRadio.isSelected()) {
                            filledExp = filled.get(1);
                        }
                    } else if (valueController.expressionRadio.isSelected()) {
                        filledExp = filled.get(0);
                    }
                    if (valueController.expressionRadio.isSelected()) {
                        if (filledExp == null || filledExp.isBlank()) {
                            error = message("Invalid") + ": " + message("RowExpression");
                            return false;
                        }
                        valueController.setExpression(filledExp);
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setTask(null);
                task = null;
                if (ok) {
                    startOperation();
                }
            }

        };
        start(task);
    }

    @Override
    public void handleAllTask() {
        task = new SingletonTask<Void>(this) {

            private long count;

            @Override
            protected boolean handle() {
                try {
                    if (!data2D.isTmpData() && tableController.dataController.backupController != null
                            && tableController.dataController.backupController.isBack()) {
                        tableController.dataController.backupController.addBackup(task, data2D.getFile());
                    }
                    data2D.startTask(task, filterController.filter);
                    count = data2D.setValue(checkedColsIndices, valueController.value, valueController.errorContinueCheck.isSelected());
                    data2D.stopFilter();
                    return count >= 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                tableController.dataController.goPage();
                tableController.requestMouse();
                tableController.popDone();
                tabPane.getSelectionModel().select(dataTab);
                alertInformation(message("ChangedRowsNumber") + ": " + count);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
                valueController.expressionController.calculator.stop();
            }

        };
        start(task);
    }

    @Override
    public void ouputRows() {
        try {
            tableController.isSettingValues = true;
            if (valueController.gaussianDistributionRadio.isSelected()) {
                gaussianDistribution();
            } else if (valueController.identifyRadio.isSelected()) {
                identifyMatrix();
            } else if (valueController.upperTriangleRadio.isSelected()) {
                upperTriangleMatrix();
            } else if (valueController.lowerTriangleRadio.isSelected()) {
                lowerTriangleMatrix();
            } else {
                setValue();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
        tableController.isSettingValues = false;
    }

    @Override
    public boolean updateTable() {
        tableController.tableView.refresh();
        tableController.isSettingValues = false;
        tableController.tableChanged(true);
        tableController.requestMouse();
        tabPane.getSelectionModel().select(dataTab);
        alertInformation(message("ChangedRowsNumber") + ": " + filteredRowsIndices.size());
        return true;
    }

    public void setValue() {
        try {
            Random random = new Random();
            String script = valueController.expression();
            for (int row : filteredRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                String v = valueController.value;
                if (valueController.blankRadio.isSelected()) {
                    v = "";
                } else if (valueController.blankRadio.isSelected()) {
                    v = "";
                } else if (valueController.expressionRadio.isSelected()) {
                    if (!data2D.calculateTableRowExpression(script, values, row)) {
                        if (valueController.errorContinueCheck.isSelected()) {
                            continue;
                        } else {
                            if (data2D.getError() != null) {
                                popError(data2D.getError());
                            }
                            return;
                        }
                    }
                    v = data2D.expressionResult();
                }
                for (int col : checkedColsIndices) {
                    if (valueController.randomRadio.isSelected()) {
                        v = tableController.data2D.random(random, col, false);
                    } else if (valueController.randomNnRadio.isSelected()) {
                        v = tableController.data2D.random(random, col, true);
                    }
                    values.set(col + 1, v);
                }
                tableController.tableData.set(row, values);
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    public void gaussianDistribution() {
        try {
            if (filteredRowsIndices.size() != checkedColsIndices.size()) {
                popError(message("MatricesCannotCalculateShouldSqure"));
                return;
            }
            if (filteredRowsIndices.size() % 2 == 0) {
                popError(message("MatricesCannotCalculateShouldOdd"));
                return;
            }
            float[][] m = ConvolutionKernel.makeGaussMatrix((int) filteredRowsIndices.size() / 2);
            int rowIndex = 0, colIndex;
            for (int row : filteredRowsIndices) {
                List<String> tableRow = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    try {
                        tableRow.set(col + 1, DoubleTools.format(m[rowIndex][colIndex], scale));
                    } catch (Exception e) {
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, tableRow);
                rowIndex++;
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    public void identifyMatrix() {
        try {
            if (filteredRowsIndices.size() != checkedColsIndices.size()) {
                popError(message("MatricesCannotCalculateShouldSqure"));
                return;
            }
            int rowIndex = 0, colIndex;
            for (int row : filteredRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex == colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    public void upperTriangleMatrix() {
        try {
            if (filteredRowsIndices.size() != checkedColsIndices.size()) {
                popError(message("MatricesCannotCalculateShouldSqure"));
                return;
            }
            int rowIndex = 0, colIndex;
            for (int row : filteredRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex <= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    public void lowerTriangleMatrix() {
        try {
            if (filteredRowsIndices.size() != checkedColsIndices.size()) {
                popError(message("MatricesCannotCalculateShouldSqure"));
                return;
            }
            int rowIndex = 0, colIndex;
            for (int row : filteredRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex >= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    /*
        static
     */
    public static Data2DSetValuesController open(ControlData2DEditTable tableController) {
        try {
            Data2DSetValuesController controller = (Data2DSetValuesController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSetValuesFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
