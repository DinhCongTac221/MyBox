package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.calculation.Normalization;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-28
 * @License Apache License Version 2.0
 */
public class Data2DNormalizeController extends BaseData2DHandleController {

    @FXML
    protected ControlData2DNormalize normalizeController;

    public Data2DNormalizeController() {
        baseTitle = message("Normalize");
    }

    @Override
    public boolean checkOptions() {
        targetController.setNotInTable(isAllPages());
        return super.checkOptions();
    }

    @Override
    public boolean handleRows() {
        try {
            filteredRowsIndices = filteredRowsIndices();
            if (filteredRowsIndices == null || filteredRowsIndices.isEmpty()
                    || checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                if (task != null) {
                    task.setError(message("NoData"));
                }
                return false;
            }
            int rowsNumber = filteredRowsIndices.size();
            int colsNumber = checkedColsIndices.size();
            double[][] matrix = new double[rowsNumber][colsNumber];
            for (int r = 0; r < rowsNumber; r++) {
                int row = filteredRowsIndices.get(r);
                List<String> tableRow = tableController.tableData.get(row);
                for (int c = 0; c < colsNumber; c++) {
                    int col = checkedColsIndices.get(c);
                    matrix[r][c] = DoubleTools.toDouble(tableRow.get(col + 1), invalidAs);
                }
            }
            matrix = normalizeController.calculate(matrix, invalidAs);
            if (matrix == null) {
                return false;
            }
            outputData = new ArrayList<>();
            for (int r = 0; r < rowsNumber; r++) {
                List<String> row = new ArrayList<>();
                if (showRowNumber()) {
                    row.add(message("Row") + (filteredRowsIndices.get(r) + 1) + "");
                }
                for (int c = 0; c < colsNumber; c++) {
                    row.add(DoubleTools.format(matrix[r][c], scale));
                }
                outputData.add(row);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    @Override
    public DataFileCSV generatedFile() {
        if (normalizeController.rowsRadio.isSelected()) {
            Normalization.Algorithm a;
            if (normalizeController.sumRadio.isSelected()) {
                a = Normalization.Algorithm.Sum;
            } else if (normalizeController.zscoreRadio.isSelected()) {
                a = Normalization.Algorithm.ZScore;
            } else {
                a = Normalization.Algorithm.MinMax;
            }
            return data2D.normalizeRows(a, checkedColsIndices,
                    normalizeController.from, normalizeController.to,
                    rowNumberCheck.isSelected(), colNameCheck.isSelected(), scale, invalidAs);

        } else if (normalizeController.allRadio.isSelected()) {
            if (normalizeController.minmaxRadio.isSelected()) {
                return data2D.normalizeMinMaxAll(checkedColsIndices,
                        normalizeController.from, normalizeController.to,
                        rowNumberCheck.isSelected(), colNameCheck.isSelected(), scale, invalidAs);

            } else if (normalizeController.sumRadio.isSelected()) {
                return data2D.normalizeSumAll(checkedColsIndices,
                        rowNumberCheck.isSelected(), colNameCheck.isSelected(), scale, invalidAs);

            } else if (normalizeController.zscoreRadio.isSelected()) {
                return data2D.normalizeZscoreAll(checkedColsIndices,
                        rowNumberCheck.isSelected(), colNameCheck.isSelected(), scale, invalidAs);
            }

        } else {
            if (normalizeController.minmaxRadio.isSelected()) {
                return data2D.normalizeMinMaxColumns(checkedColsIndices,
                        normalizeController.from, normalizeController.to,
                        rowNumberCheck.isSelected(), colNameCheck.isSelected(), scale, invalidAs);

            } else if (normalizeController.sumRadio.isSelected()) {
                return data2D.normalizeSumColumns(checkedColsIndices,
                        rowNumberCheck.isSelected(), colNameCheck.isSelected(), scale, invalidAs);

            } else if (normalizeController.zscoreRadio.isSelected()) {
                return data2D.normalizeZscoreColumns(checkedColsIndices,
                        rowNumberCheck.isSelected(), colNameCheck.isSelected(), scale, invalidAs);
            }
        }
        return null;
    }

    /*
        static
     */
    public static Data2DNormalizeController open(ControlData2DEditTable tableController) {
        try {
            Data2DNormalizeController controller = (Data2DNormalizeController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DNormalizeFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
