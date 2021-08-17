package mara.mybox.controller;

import java.util.List;
import javafx.scene.control.CheckBoxTreeItem;
import mara.mybox.db.table.TableDataset;
import mara.mybox.fxml.ConditionNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import static mara.mybox.value.Languages.tableMessage;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-07-18
 * @License Apache License Version 2.0
 */
public class DatasetSourceController extends ControlConditionTree {

    protected List<String> categories;

    public DatasetSourceController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            loadTree();
            select(Languages.message("Categories"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void loadTree() {
        clearTree();
        categories = TableDataset.dataCategories();
        if (categories == null || categories.isEmpty()) {
            return;
        }
        try {
            CheckBoxTreeItem<ConditionNode> allItem = new CheckBoxTreeItem(
                    ConditionNode.create(Languages.message("Categories"))
                            .setTitle(Languages.message("Categories"))
                            .setCondition("")
            );
            allItem.setExpanded(true);
            treeView.setRoot(allItem);

            for (String category : categories) {
                String name = Languages.tableMessage(category);
                CheckBoxTreeItem<ConditionNode> item = new CheckBoxTreeItem(
                        ConditionNode.create(name)
                                .setTitle(name)
                                .setCondition(" data_category='" + category.replaceAll("'", "''") + "' ")
                );
                item.setExpanded(true);
                allItem.getChildren().add(item);
            }
            treeView.setSelection();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
