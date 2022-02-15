package mara.mybox.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DCell;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DCell;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class DataMatrix extends Data2D {

    protected TableData2DCell tableData2DCell;

    public DataMatrix() {
        type = Type.Matrix;
        tableData2DCell = new TableData2DCell();
    }

    public int type() {
        return type(Type.Matrix);
    }

    @Override
    public boolean checkForLoad() {
        hasHeader = false;
        return true;
    }

    @Override
    public boolean checkForSave() {
        if (dataName == null || dataName.isBlank()) {
            dataName = rowsNumber + "x" + colsNumber;
        }
        return true;
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryID(conn, d2did);
    }

    @Override
    public void applyOptions() {
    }

    @Override
    public long readTotal() {
        return dataSize;
    }

    @Override
    public List<List<String>> readPageData() {
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        List<List<String>> rows = new ArrayList<>();
        if (d2did >= 0 && rowsNumber > 0 && colsNumber > 0) {
            double[][] matrix = new double[(int) rowsNumber][(int) colsNumber];
            try ( Connection conn = DerbyBase.getConnection();
                     PreparedStatement query = conn.prepareStatement(TableData2DCell.QueryData)) {
                query.setLong(1, d2did);
                ResultSet results = query.executeQuery();
                while (results.next()) {
                    Data2DCell cell = tableData2DCell.readData(results);
                    if (cell.getCol() < colsNumber && cell.getRow() < rowsNumber) {
                        matrix[(int) cell.getRow()][(int) cell.getCol()] = toDouble(cell.getValue());
                    }
                }
                rows = toTableData(matrix);
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.console(e);
            }
        }
        rowsNumber = rows.size();
        dataSize = rowsNumber;
        endRowOfCurrentPage = startRowOfCurrentPage + rowsNumber;
        return rows;
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !targetData.isMatrix()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            targetData.saveDefinition(conn);
            long did = targetData.getD2did();
            if (did < 0) {
                return false;
            }
            try ( PreparedStatement clear = conn.prepareStatement(TableData2DCell.ClearData)) {
                clear.setLong(1, did);
                clear.executeUpdate();
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.commit();
            conn.setAutoCommit(false);
            for (int r = 0; r < tableRowsNumber(); r++) {
                List<String> row = tableRowWithoutNumber(r);
                for (int c = 0; c < row.size(); c++) {
                    double d = toDouble(row.get(c));
                    if (d == 0 || d == AppValues.InvalidDouble) {
                        continue;
                    }
                    Data2DCell cell = Data2DCell.create().setD2did(did)
                            .setRow(r).setCol(c).setValue(d + "");
                    tableData2DCell.insertData(conn, cell);
                }
            }
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean export(ControlDataConvert convertController, List<Integer> colIndices) {
        return false;
    }

    public boolean isSquare() {
        return isValid() && tableColsNumber() == tableRowsNumber();
    }

    public String toString(double d) {
        if (d == AppValues.InvalidDouble) {
            return "0";
        } else {
            return DoubleTools.format(d, scale);
        }
    }

    public double toDouble(String d) {
        try {
            return Double.valueOf(d);
        } catch (Exception e) {
            return 0;
        }
    }

    public double[][] toArray() {
        rowsNumber = tableRowsNumber();
        colsNumber = tableColsNumber();
        if (rowsNumber <= 0 || colsNumber <= 0) {
            return null;
        }
        double[][] data = new double[(int) rowsNumber][(int) colsNumber];
        for (int r = 0; r < rowsNumber; r++) {
            List<String> row = tableRowWithoutNumber(r);
            for (int c = 0; c < row.size(); c++) {
                data[r][c] = toDouble(row.get(c));
            }
        }
        return data;
    }

    public List<List<String>> toTableData(double[][] data) {
        if (data == null) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        for (int r = 0; r < data.length; r++) {
            List<String> row = new ArrayList<>();
            row.add(("" + (r + 1)));
            for (int c = 0; c < data[r].length; c++) {
                row.add(toString(data[r][c]));
            }
            rows.add(row);
        }
        return rows;
    }

    @Override
     public long clearData() {
        long count = -1;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement clear = conn.prepareStatement(TableData2DCell.ClearData)) {
            clear.setLong(1, d2did);
            count = clear.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

}
