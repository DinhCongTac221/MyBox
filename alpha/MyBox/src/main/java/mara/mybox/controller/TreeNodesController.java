package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Window;
import mara.mybox.db.data.TreeLeaf;
import mara.mybox.db.data.TreeLeafTag;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableTreeLeaf;
import mara.mybox.db.table.TableTreeLeafTag;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TreeNodesController extends BaseNodeSelector<TreeNode> {

    protected TreeManageController treeController;
    protected TreeNodesController caller;
    protected TableTree tableTree;
    protected TableTreeLeaf tableTreeLeaf;
    protected TableTreeLeafTag tableTreeLeafTag;
    protected String category;

    public TreeNodesController() {
    }

    public void setParameters(TreeManageController parent, boolean manageMode) {
        super.setManager(parent, manageMode);
        treeController = parent;
        tableTree = parent.tableTree;
        tableTreeLeaf = parent.tableTreeLeaf;
        tableTreeLeafTag = parent.tableTreeLeafTag;
        category = treeController.category;
        baseTitle = category;
    }

    public void setCaller(TreeNodesController caller) {
        super.setManager(null, false);
        this.caller = caller;
        tableTree = caller.tableTree;
        tableTreeLeaf = caller.tableTreeLeaf;
        tableTreeLeafTag = caller.tableTreeLeafTag;
        category = caller.category;
        cloneTree(caller.treeView, treeView, getIgnoreNode());
    }

    @Override
    public String display(TreeNode node) {
        return node.getTitle();
    }

    @Override
    public String tooltip(TreeNode node) {
        if (node.getAttribute() != null && !node.getAttribute().isBlank()) {
            return node.getTitle() + "\n" + node.getAttribute();
        } else {
            return null;
        }
    }

    public TreeNode root() {
        return tableTree.findAndCreateRoot(category);
    }

    @Override
    public TreeNode root(Connection conn) {
        return tableTree.findAndCreateRoot(conn, category);
    }

    @Override
    public int size(Connection conn, TreeNode node) {
        return TableTree.size(conn, node.getNodeid());
    }

    @Override
    public long id(TreeNode node) {
        return node.getNodeid();
    }

    @Override
    public List<TreeNode> children(Connection conn, TreeNode node) {
        return tableTree.children(conn, id(node));
    }

    @Override
    public List<TreeNode> ancestor(Connection conn, TreeNode node) {
        return tableTree.ancestor(conn, id(node));
    }

    @Override
    public TreeNode dummy() {
        return new TreeNode();
    }

    @Override
    public boolean isDummy(TreeNode node) {
        return node.getTitle() != null;
    }

    @Override
    public TreeNode createNode(TreeNode targetNode, String name) {
        if (targetNode == null) {
            return null;
        }
        TreeNode newNode = new TreeNode(targetNode.getNodeid(), name);
        newNode = tableTree.insertData(newNode);
        return newNode;
    }

    @Override
    public String name(TreeNode node) {
        return node.getTitle();
    }

    @FXML
    @Override
    protected void exportNode() {
        TreeNodeExportController exportController
                = (TreeNodeExportController) WindowTools.openStage(Fxmls.TreeNodeExportFxml);
        exportController.setController(treeController);
    }

    @Override
    protected void clearTree(Connection conn, TreeNode node) {
        TableTree.deleteChildren(conn, node.getNodeid());
    }

    @Override
    protected TreeNode rename(TreeNode node, String name) {
        node.setTitle(name);
        return tableTree.updateData(node);
    }

    @Override
    protected void delete(Connection conn, TreeNode node) {
        tableTree.deleteData(conn, node);
    }

    @Override
    protected void copyNode(Boolean onlyContents) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || isRoot(selectedItem.getValue())) {
            return;
        }
        String chainName = chainName(selectedItem);
        TreeNodeCopyController controller
                = (TreeNodeCopyController) WindowTools.openStage(Fxmls.TreeNodeCopyFxml);
        controller.tableTreeLeaf = treeController.tableTreeLeaf;
        controller.setCaller(this, selectedItem.getValue(), chainName, onlyContents);
    }

    @FXML
    @Override
    protected void moveNode() {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || isRoot(selectedItem.getValue())) {
            return;
        }
        String chainName = chainName(selectedItem);
        TreeNodeMoveController controller = (TreeNodeMoveController) WindowTools.openStage(Fxmls.TreeNodeMoveFxml);
        controller.setCaller(this, selectedItem.getValue(), chainName);
    }

    @Override
    protected void treeView(Connection conn, TreeNode node, int indent, StringBuilder s) {
        if (conn == null || node == null) {
            return;
        }
        List<TreeLeaf> leaves = tableTreeLeaf.leaves(conn, node.getNodeid());
        List<TreeNode> children = children(conn, node);
        if ((leaves == null || leaves.isEmpty()) && (children == null || children.isEmpty())) {
            s.append(" ".repeat(indent)).append("&nbsp;".repeat(indent))
                    .append(display(node)).append("<BR>\n");
        } else {
            String id = "item" + id(node);
            s.append(" ".repeat(indent)).append("&nbsp;".repeat(indent))
                    .append("<a href=\"javascript:nodeClicked('").append(id).append("')\">")
                    .append(display(node)).append("</a><BR>\n");
            s.append("<DIV id='").append(id).append("'>\n");
            if (leaves != null) {
                for (TreeLeaf leaf : leaves) {
                    s.append(" ".repeat(indent + 4)).append("&nbsp;".repeat(indent + 4))
                            .append(nameWithTags(conn, leaf)).append("<BR>\n");
                }
            }
            if (children != null) {
                for (TreeNode child : children) {
                    treeView(conn, child, indent + 4, s);
                }
            }
            s.append("</DIV>\n");
        }
    }

    public String nameWithTags(Connection conn, TreeLeaf leaf) {
        String s = leaf.getName();
        List<TreeLeafTag> tags = tableTreeLeafTag.leafTags(conn, leaf.getLeafid());
        if (tags != null && !tags.isEmpty()) {
            for (TreeLeafTag leafTag : tags) {
                s += "&nbsp;&nbsp;<SPAN style=\"border-radius:3px; "
                        + " background-color: " + FxColorTools.color2rgb(leafTag.getTag().getColor()) + ";"
                        + " color: " + FxColorTools.color2rgb(FxColorTools.invert(leafTag.getTag().getColor()))
                        + ";\">" + leafTag.getTag().getTag() + "</SPAN>";
            }
        }
        return s;
    }

    @FXML
    @Override
    protected void importExamples() {
        TreeNodeImportController controller = (TreeNodeImportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeImportFxml);
        controller.importExamples(treeController);
    }

    @FXML
    @Override
    protected void importAction() {
        TreeNodeImportController controller = (TreeNodeImportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeImportFxml);
        controller.setManage(treeController);
    }

    public TreeNodesController oneOpen() {
        TreeNodesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object.getClass().equals(myController.getClass())) {
                try {
                    controller = (TreeNodesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeNodesController) WindowTools.openStage(myFxml);
        }
        if (controller != null) {
            controller.requestMouse();
        }
        return controller;
    }

}