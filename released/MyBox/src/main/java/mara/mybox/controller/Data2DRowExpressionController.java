package mara.mybox.controller;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-7-1
 * @License Apache License Version 2.0
 */
public class Data2DRowExpressionController extends BaseData2DHandleController {

    protected String value;

    @FXML
    protected TextField nameInput;
    @FXML
    protected ControlData2DRowExpression expressionController;
    @FXML
    protected CheckBox errorContinueCheck;

    public Data2DRowExpressionController() {
        baseTitle = message("RowExpression");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            nameInput.setText(UserConfig.getString(interfaceName + "Name", message("Value")));

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
        expressionController.setData2D(data2D);
    }

    @Override
    public boolean checkOptions() {
        targetController.setNotInTable(isAllPages());
        boolean ok = expressionController.checkExpression(isAllPages());
        if (!ok && data2D.getError() != null) {
            infoLabel.setText(message("Invalid") + ": " + message("RowExpression") + "\n"
                    + data2D.getError());
        }
        String name = nameInput.getText();
        if (name == null || name.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("Name"));
            ok = false;
        } else {
            name = name.trim();
            UserConfig.setString(interfaceName + "Name", name);
            TableStringValues.add(interfaceName + "NameHistories", name);
        }
        String script = expressionController.scriptInput.getText();
        if (script == null || script.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("RowExpression"));
            ok = false;
        }
        ok = super.checkOptions() && ok;
        okButton.setDisable(!ok);
        return ok;
    }

    @Override
    public void handleRowsTask() {
        try {
            boolean showRowNumber = showRowNumber();
            outputData = selectedData(showRowNumber);
            if (outputData == null) {
                popError(message("SelectToHandle"));
                return;
            }
            String script = expressionController.scriptInput.getText();
            for (int i = 0; i < checkedRowsIndices.size(); i++) {
                int rowIndex = checkedRowsIndices.get(i);
                List<String> checkedRow = outputData.get(i);
                if (expressionController.calculator.calculateTableRowExpression(data2D,
                        script, tableController.tableData.get(rowIndex), rowIndex)) {
                    checkedRow.add(expressionController.calculator.getResult());
                } else {
                    if (errorContinueCheck.isSelected()) {
                        checkedRow.add(null);
                    } else {
                        if (data2D.getError() != null) {
                            popError(data2D.getError());
                        }
                        return;
                    }
                }
                outputData.set(i, checkedRow);
            }
            String name = nameInput.getText().trim();
            if (showColNames()) {
                List<String> names = checkedColsNames;
                if (showRowNumber) {
                    names.add(0, message("SourceRowNumber"));
                }
                names.add(name);
                outputData.add(0, names);
            }
            outputColumns.add(new Data2DColumn(name, ColumnDefinition.ColumnType.String));
            if (targetController == null || targetController.inTable()) {
                updateTable();
            } else {
                outputExternal();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    @Override
    public DataFileCSV generatedFile() {
        return data2D.rowExpression(expressionController.calculator,
                expressionController.scriptInput.getText().trim(),
                nameInput.getText().trim(), errorContinueCheck.isSelected(),
                checkedColsIndices, rowNumberCheck.isSelected(), colNameCheck.isSelected());
    }

    @FXML
    protected void popNameHistories(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "NameHistoriesPopWhenMouseHovering", true)) {
            PopTools.popStringValues(this, nameInput, event, interfaceName + "NameHistories", true, true);
        }
    }

    @FXML
    protected void showNameHistories(ActionEvent event) {
        PopTools.popStringValues(this, nameInput, event, interfaceName + "NameHistories", true, true);
    }

    /*
        static
     */
    public static Data2DRowExpressionController open(ControlData2DEditTable tableController) {
        try {
            Data2DRowExpressionController controller = (Data2DRowExpressionController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DRowExpressionFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}