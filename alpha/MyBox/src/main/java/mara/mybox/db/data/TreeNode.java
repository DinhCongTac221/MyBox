package mara.mybox.db.data;

import java.io.File;
import java.util.Date;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxFileTools.getInternalFile;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TreeNode extends BaseData {

    public static final String RootIdentify = "MyBoxTreeRoot;;;";
    public static final String NodeSeparater = " > ";
    public static final String TimePrefix = "Time:";
    public static final String TagsPrefix = "Tags:";
    public static final String TagsSeparater = ";;;";
    public static final String Root = "Root";
    public static final String WebFavorite = "WebFavorite";
    public static final String Notebook = "Notebook";
    public static final String JShellCode = "JShellCode";
    public static final String SQL = "SQL";
    public static final String JavaScript = "JavaScript";
    public static final String InformationInTree = "InformationInTree";
    public static final String JEXL = "JEXLCode";

    protected long nodeid, parentid;
    protected String category, title, value, more, serialNumber;
    protected Date updateTime;

    private void init() {
        nodeid = -1;
        parentid = -2;
        category = null;
        title = null;
        value = null;
        more = null;
        updateTime = new Date();
        serialNumber = "";
    }

    public TreeNode() {
        init();
    }

    public TreeNode(TreeNode parent, String title, String value) {
        init();
        this.parentid = parent.getNodeid();
        this.category = parent.getCategory();
        this.title = title;
        this.value = value;
    }

    public boolean isRoot() {
        return parentid == nodeid;
    }

    /*
        Static methods
     */
    public static TreeNode create() {
        return new TreeNode();
    }

    public static boolean setValue(TreeNode data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "nodeid":
                    data.setNodeid(value == null ? -1 : (long) value);
                    return true;
                case "parentid":
                    data.setParentid(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "value":
                    data.setValue(value == null ? null : (String) value);
                    return true;
                case "category":
                    data.setCategory(value == null ? null : (String) value);
                    return true;
                case "more":
                    data.setMore(value == null ? null : (String) value);
                    return true;
                case "update_time":
                    data.setUpdateTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(TreeNode data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "nodeid":
                return data.getNodeid();
            case "parentid":
                return data.getParentid();
            case "title":
                return data.getTitle();
            case "value":
                return data.getValue();
            case "category":
                return data.getCategory();
            case "more":
                return data.getMore();
            case "update_time":
                return data.getUpdateTime();
        }
        return null;
    }

    public static boolean valid(TreeNode data) {
        return data != null && data.getCategory() != null
                && data.getTitle() != null && !data.getTitle().isBlank()
                && !data.getTitle().contains(NodeSeparater);
    }

    public static File exampleFile(String category) {
        String lang = Languages.isChinese() ? "zh" : "en";
        if (WebFavorite.equals(category)) {
            return getInternalFile("/data/examples/WebFavorites_Examples_" + lang + ".txt",
                    "data", "WebFavorites_Examples_" + lang + ".txt", true);

        } else if (Notebook.equals(category)) {
            return getInternalFile("/data/examples/Notes_Examples_" + lang + ".txt",
                    "data", "Notes_Examples_" + lang + ".txt", true);

        } else if (JShellCode.equals(category)) {
            return getInternalFile("/data/examples/JShell_Examples_" + lang + ".txt",
                    "data", "JShell_Examples_" + lang + ".txt", true);

        } else if (SQL.equals(category)) {
            return getInternalFile("/data/examples/Sql_Examples_" + lang + ".txt",
                    "data", "Sql_Examples_" + lang + ".txt", true);

        } else if (JavaScript.equals(category)) {
            return getInternalFile("/data/examples/JavaScript_Examples_" + lang + ".txt",
                    "data", "JavaScript_Examples_" + lang + ".txt", true);

        } else if (InformationInTree.equals(category)) {
            return getInternalFile("/data/examples/Tree_Examples_" + lang + ".txt",
                    "data", "Tree_Examples_" + lang + ".txt", true);

        } else {
            return null;
        }
    }

    /*
        get/set
     */
    public long getNodeid() {
        return nodeid;
    }

    public TreeNode setNodeid(long nodeid) {
        this.nodeid = nodeid;
        return this;
    }

    public long getParentid() {
        return parentid;
    }

    public TreeNode setParentid(long parentid) {
        this.parentid = parentid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public TreeNode setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getValue() {
        return value;
    }

    public TreeNode setValue(String value) {
        this.value = value;
        return this;
    }

    public String getMore() {
        return more;
    }

    public TreeNode setMore(String more) {
        this.more = more;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public TreeNode setCategory(String category) {
        this.category = category;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public TreeNode setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

}
