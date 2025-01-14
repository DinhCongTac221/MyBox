package mara.mybox.controller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2023-5-27
 * @License Apache License Version 2.0
 */
public class ControlXmlTree extends BaseTreeViewController<XmlTreeNode> {

    protected XmlEditorController xmlEditor;
    protected Document doc;
    protected Transformer transformer;

    @FXML
    protected TreeTableColumn<XmlTreeNode, String> typeColumn;
    @FXML
    protected ControlXmlNodeEdit nodeController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("typename"));

            nodeController.setParameters(this);

            clearTree();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        tree
     */
    public TreeItem<XmlTreeNode> makeTree(String xml) {
        try {
            if (xml == null) {
                clearTree();
                return null;
            }
            doc = XmlTreeNode.doc(this, xml);
            return loadTree(doc);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<XmlTreeNode> loadTree(Node doc) {
        try {
            clearTree();
            if (doc == null) {
                return null;
            }
            TreeItem<XmlTreeNode> xml = makeTreeItem(new XmlTreeNode(doc));
            treeView.setRoot(xml);
            return xml;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<XmlTreeNode> makeTreeItem(XmlTreeNode xmlTreeNode) {
        try {
            if (xmlTreeNode == null) {
                return null;
            }
            TreeItem<XmlTreeNode> item = new TreeItem(xmlTreeNode);
            item.setExpanded(true);
            Node node = xmlTreeNode.getNode();
            if (node == null) {
                return item;
            }
            NodeList children = node.getChildNodes();
            if (children != null) {
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    addTreeItem(item, -1, new XmlTreeNode(child));
                }
            }
            return item;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<XmlTreeNode> addTreeItem(TreeItem<XmlTreeNode> parent, int index, XmlTreeNode xmlTreeNode) {
        try {
            if (parent == null || xmlTreeNode == null) {
                return null;
            }
            TreeItem<XmlTreeNode> item = makeTreeItem(xmlTreeNode);
            if (item == null) {
                return null;
            }
            ObservableList<TreeItem<XmlTreeNode>> parentChildren = parent.getChildren();
            if (index >= 0 && index < parentChildren.size() - 1) {
                parentChildren.add(index, item);
            } else {
                parentChildren.add(item);
            }
            return item;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<XmlTreeNode> updateTreeItem(TreeItem<XmlTreeNode> item) {
        try {
            if (item == null) {
                return null;
            }
            TreeItem<XmlTreeNode> parentItem = item.getParent();
            if (parentItem == null) {
                return loadTree(item.getValue().getNode());
            }
            int index = parentItem.getChildren().indexOf(item);
            if (index < 0) {
                return null;
            }
            parentItem.getChildren().set(index, item);
            focusItem(item);
            return item;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void itemClicked(MouseEvent event, TreeItem<XmlTreeNode> item) {
        nodeController.editNode(item);
    }

    @FXML
    @Override
    public void clearTree() {
        super.clearTree();
        nodeController.clearNode();
    }

    /*
        values
     */
    @Override
    public boolean validNode(XmlTreeNode node) {
        return node != null && node.getNode() != null;
    }

    @Override
    public String title(XmlTreeNode node) {
        return node == null ? null : node.getTitle();
    }

    @Override
    public String value(XmlTreeNode node) {
        return node == null ? null : node.getValue();
    }

    @Override
    public String copyTitleMessage() {
        return message("CopyName");
    }

    public String xml(Node node) {
        if (node == null) {
            return null;
        }
        String encoding = node instanceof Document
                ? ((Document) node).getXmlEncoding()
                : node.getOwnerDocument().getXmlEncoding();
        if (encoding == null) {
            encoding = "utf-8";
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
            if (transformer == null) {
                transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    node instanceof Document ? "no" : "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            transformer.setOutputProperty(OutputKeys.INDENT,
                    UserConfig.getBoolean("XmlTransformerIndent", true) ? "yes" : "no");
            StreamResult streamResult = new StreamResult();
            streamResult.setOutputStream(os);
            transformer.transform(new DOMSource(node), streamResult);
            os.flush();
            os.close();
            return os.toString(encoding);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        actions
     */
    @Override
    public List<MenuItem> functionItems(TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        items.add(viewMenu);

        viewMenu.getItems().addAll(foldItems(treeItem));

        viewMenu.getItems().add(new SeparatorMenuItem());

        MenuItem menu = new MenuItem(message("NodeXML"), StyleTools.getIconImageView("iconXML.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            xml(treeItem);
        });
        viewMenu.getItems().add(menu);

        menu = new MenuItem(message("NodeTexts"), StyleTools.getIconImageView("iconTxt.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            text(treeItem);
        });
        viewMenu.getItems().add(menu);

        viewMenu.getItems().add(new SeparatorMenuItem());
        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        viewMenu.getItems().add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            XmlAddNodeController.open(this, treeItem);
        });
        menu.setDisable(treeItem.getValue() == null || !treeItem.getValue().canAddNode());
        items.add(menu);

        menu = new MenuItem(message("DeleteNode"), StyleTools.getIconImageView("iconDelete.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteNode(treeItem);
        });
        items.add(menu);

        menu = new MenuItem(message("DuplicateAfterNode"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            duplicate(treeItem, true);
        });
        menu.setDisable(treeItem.getParent() == null);
        items.add(menu);

        menu = new MenuItem(message("DuplicateToParentEnd"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            duplicate(treeItem, false);
        });
        menu.setDisable(treeItem.getParent() == null);
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(copyValueMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, value(treeItem.getValue()));
        });
        menu.setDisable(treeItem.getValue() == null);
        items.add(menu);

        menu = new MenuItem(copyTitleMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, title(treeItem.getValue()));
        });
        menu.setDisable(treeItem.getValue() == null);
        items.add(menu);

        if (xmlEditor != null && xmlEditor.sourceFile != null && xmlEditor.sourceFile.exists()) {
            items.add(new SeparatorMenuItem());
            menu = new MenuItem(message("Recover"), StyleTools.getIconImageView("iconRecover.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                recoverAction();
            });
            items.add(menu);
        }

        return items;
    }

    public void xml(TreeItem<XmlTreeNode> treeItem) {
        try {
            if (treeItem == null) {
                return;
            }
            String xml = xml(treeItem.getValue().getNode());
            if (xml == null || xml.isBlank()) {
                popInformation(message("NoData"));
            } else {
                TextPopController.loadText(this, xml);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void text(TreeItem<XmlTreeNode> treeItem) {
        try {
            if (treeItem == null) {
                return;
            }
            String texts = treeItem.getValue().getNode().getTextContent();
            if (texts == null || texts.isEmpty()) {
                popInformation(message("NoData"));
            } else {
                TextPopController.loadText(this, texts);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void deleteNode(TreeItem<XmlTreeNode> treeItem) {
        try {
            if (treeItem == null) {
                return;
            }
            TreeItem<XmlTreeNode> parentItem = treeItem.getParent();
            if (parentItem == null) {
                if (PopTools.askSure(getTitle(), message("SureClear"))) {
                    clearTree();
                }
                return;
            }
            int index = parentItem.getChildren().indexOf(treeItem);
            if (index < 0) {
                return;
            }
            Node parentNode = parentItem.getValue().getNode();
            parentNode.removeChild(treeItem.getValue().getNode());
            parentItem.getChildren().remove(index);

            xmlEditor.domChanged(true);
            xmlEditor.popInformation(message("DeletedSuccessfully"));

            nodeController.clearNode();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void duplicate(TreeItem<XmlTreeNode> treeItem, boolean afterNode) {
        try {
            if (treeItem == null) {
                return;
            }
            TreeItem<XmlTreeNode> parentItem = treeItem.getParent();
            if (parentItem == null) {
                return;
            }
            int index = parentItem.getChildren().indexOf(treeItem);
            if (index < 0) {
                return;
            }
            Node xmlNode = treeItem.getValue().getNode();
            Node newNode = xmlNode.cloneNode(true);
            Node parentNode = xmlNode.getParentNode();

            if (afterNode && index < parentItem.getChildren().size() - 1) {
                parentNode.insertBefore(newNode, xmlNode.getNextSibling());
                addTreeItem(parentItem, index + 1, new XmlTreeNode(newNode));
            } else {
                parentNode.appendChild(newNode);
                addTreeItem(parentItem, -1, new XmlTreeNode(newNode));
            }

            xmlEditor.domChanged(true);
            xmlEditor.popInformation(message("DeletedSuccessfully"));

            nodeController.clearNode();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTree(doc);
    }

    @FXML
    @Override
    public void recoverAction() {
        if (xmlEditor != null && xmlEditor.sourceFile != null && xmlEditor.sourceFile.exists()) {
            xmlEditor.fileChanged = false;
            xmlEditor.sourceFileChanged(xmlEditor.sourceFile);
        }
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("XmlHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem(message("XmlTutorial") + " - " + message("English"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.xmlEnLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("XmlTutorial") + " - " + message("Chinese"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.xmlZhLink(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            menuItem = new MenuItem(message("DomSpecification"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.domSpecification(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("XmlHelpsPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("XmlHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
