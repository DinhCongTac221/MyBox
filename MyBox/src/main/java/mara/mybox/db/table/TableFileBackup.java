package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.data.FileBackup;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2021-2-26
 * @License Apache License Version 2.0
 */
public class TableFileBackup extends BaseTable<FileBackup> {

    public static final int Default_Max_Backups = 10;

    public TableFileBackup() {
        tableName = "File_Backup";
        defineColumns();
    }

    public TableFileBackup(boolean defineColumns) {
        tableName = "File_Backup";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableFileBackup defineColumns() {
        addColumn(new ColumnDefinition("fbid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("file", ColumnType.String, true).setLength(4096));
        addColumn(new ColumnDefinition("backup", ColumnType.String, true).setLength(4096));
        addColumn(new ColumnDefinition("record_time", ColumnType.Datetime, true));
        return this;
    }

    public static final String Create_Index
            = "CREATE INDEX File_Backup_index on File_Backup (  file, backup, record_time )";

    public static final String FileQuery
            = "SELECT * FROM File_Backup  WHERE file=? ORDER BY record_time DESC";

    public static final String DeleteFile
            = "DELETE FROM File_Backup  WHERE file=?";

    public static final String DeleteBackup
            = "DELETE FROM File_Backup  WHERE file=? AND backup=?";

    public List<FileBackup> read(String file) {
        List<FileBackup> dataList = new ArrayList<>();
        if (file == null || file.trim().isBlank()) {
            return dataList;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return read(conn, file);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return dataList;
    }

    public List<FileBackup> read(Connection conn, String filename) {
        List<FileBackup> records = new ArrayList<>();
        if (filename == null || filename.isBlank()) {
            return records;
        }
        int max = AppVariables.getUserConfigInt("MaxFileBackups", Default_Max_Backups);
        if (max <= 0) {
            max = Default_Max_Backups;
            AppVariables.setUserConfigInt("MaxFileBackups", Default_Max_Backups);
        }
        try ( PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            statement.setString(1, filename);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    FileBackup data = (FileBackup) readData(results);
                    records.add(data);
                }
            }
            List<FileBackup> valid = new ArrayList<>();
            for (int i = 0; i < records.size(); ++i) {
                FileBackup record = records.get(i);
                File file = record.getFile();
                if (!file.exists()) {
                    clearBackups(conn, filename);
                    continue;
                }
                File backup = record.getBackup();
                if (!backup.exists()) {
                    deleteBackup(conn, record);
                    continue;
                }
                valid.add(records.get(i));
            }
            if (valid.size() > max) {
                for (int i = max; i < valid.size(); ++i) {
                    deleteBackup(conn, valid.get(i));
                }
                records = valid.subList(0, max);
            } else {
                records = valid;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public void clearBackups(String filename) {
        if (filename == null) {
            return;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            clearBackups(conn, filename);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearBackups(Connection conn, String filename) {
        if (conn == null || filename == null) {
            return;
        }
        try ( PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            statement.setString(1, filename);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    FileBackup data = (FileBackup) readData(results);
                    FileTools.delete(data.getBackup());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        try ( PreparedStatement statement = conn.prepareStatement(DeleteFile)) {
            statement.setString(1, filename);
            statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void deleteBackup(FileBackup record) {
        if (record == null) {
            return;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            deleteBackup(conn, record);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void deleteBackup(Connection conn, FileBackup record) {
        if (conn == null || record == null || record.getFile() == null || record.getBackup() == null) {
            return;
        }
        deleteBackup(conn, record.getFile().getAbsolutePath(), record.getBackup().getAbsolutePath());
    }

    public static void deleteBackup(Connection conn, String filename, String backup) {
        if (conn == null || filename == null) {
            return;
        }
        try ( PreparedStatement statement = conn.prepareStatement(DeleteBackup)) {
            statement.setString(1, filename);
            statement.setString(2, backup);
            statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        FileTools.delete(new File(backup));
    }

}
