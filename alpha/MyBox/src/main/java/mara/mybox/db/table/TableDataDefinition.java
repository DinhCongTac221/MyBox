package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-1-23
 * @License Apache License Version 2.0
 */
public class TableDataDefinition extends BaseTable<DataDefinition> {

    public TableDataDefinition() {
        tableName = "Data_Definition";
        defineColumns();
    }

    public TableDataDefinition(boolean defineColumns) {
        tableName = "Data_Definition";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableDataDefinition defineColumns() {
        addColumn(new ColumnDefinition("dfid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("data_type", ColumnType.Short, true));
        addColumn(new ColumnDefinition("data_name", ColumnType.String, true).setLength(32672));
        addColumn(new ColumnDefinition("charset", ColumnType.String).setLength(32));
        addColumn(new ColumnDefinition("delimiter", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("has_header", ColumnType.Boolean));
        return this;
    }

    public static final String Create_Index_unique
            = "CREATE UNIQUE INDEX Data_Definition_unique_index on Data_Definition (data_type, data_name)";

    public static final String Query_unique
            = "SELECT * FROM Data_Definition WHERE data_type=? AND data_name=?";

    public static final String Query_Type
            = "SELECT * FROM Data_Definition WHERE data_type=?";

    public static final String DeleteID
            = "DELETE FROM Data_Definition WHERE dfid=?";

    /*
        local methods
     */
    public DataDefinition read(DataType dataType, String dataName) {
        if (dataType == null || dataName == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return read(conn, dataType, dataName);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataDefinition read(Connection conn, DataType dataType, String dataName) {
        if (conn == null || dataType == null || dataName == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Query_unique)) {
            statement.setShort(1, DataDefinition.dataType(dataType));
            statement.setString(2, DerbyBase.stringValue(dataName));
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean clear(DataType dataType, String dataName) {
        if (dataType == null || dataName == null) {
            return true;
        }
        boolean ret;
        try ( Connection conn = DerbyBase.getConnection();) {
            ret = clear(conn, dataType, dataName);
        } catch (Exception e) {
            MyBoxLog.error(e);
            ret = false;
        }
        return ret;
    }

    public boolean clear(Connection conn, DataType dataType, String dataName) {
        if (conn == null || dataType == null || dataName == null) {
            return false;
        }
        boolean ret = true;
        try {
            DataDefinition d = read(conn, dataType, dataName);
            if (d != null) {
                try ( PreparedStatement statement = conn.prepareStatement(TableDataColumn.ClearData)) {
                    statement.setLong(1, d.getDfid());
                    statement.executeUpdate();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    ret = false;
                }
                try ( PreparedStatement statement = conn.prepareStatement(TableDataDefinition.DeleteID)) {
                    statement.setLong(1, d.getDfid());
                    statement.executeUpdate();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    ret = false;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            ret = false;
        }
        return ret;
    }

    public int clearInvalid(Connection conn) {
        int count = 0;
        try {
            conn.setAutoCommit(true);
            String sql = "SELECT * FROM Data_Definition WHERE data_type="
                    + DataDefinition.dataType(DataType.DataClipboard)
                    + " OR data_type=" + DataDefinition.dataType(DataType.DataFile);
            List<DataDefinition> invalid = new ArrayList<>();
            try ( PreparedStatement statement = conn.prepareStatement(sql);
                     ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    DataDefinition data = readData(results);
                    try {
                        File file = new File(data.getDataName());
                        if (!file.exists()) {
                            invalid.add(data);
                        }
                    } catch (Exception e) {
                        invalid.add(data);
                    }
                }
            }
            count = invalid.size();
            deleteData(conn, invalid);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

}
