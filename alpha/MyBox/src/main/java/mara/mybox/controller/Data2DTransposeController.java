package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-9
 * @License Apache License Version 2.0
 */
public class Data2DTransposeController extends Data2DHandleController {

    public Data2DTransposeController() {
        includeTable = true;
    }

    @Override
    public void handleFileTask() {
        popError(message("NotSupport"));
    }

    @Override
    public boolean handleRows() {
        try {
            List<List<String>> selectedData = tableController.selectedData(all(),
                    rowNumberCheck.isSelected(), colNameCheck.isSelected());
            if (selectedData == null) {
                return false;
            }
            int rowsNumber = selectedData.size(), columnsNumber = selectedData.get(0).size();
            handledData = new ArrayList<>();
            for (int r = 0; r < columnsNumber; ++r) {
                List<String> row = new ArrayList<>();
                for (int c = 0; c < rowsNumber; ++c) {
                    row.add(selectedData.get(c).get(r));
                }
                handledData.add(row);
            }
            handledNames = null;
            handledColumns = null;
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    /*
        static
     */
    public static Data2DTransposeController open(ControlData2DEditTable tableController) {
        try {
            Data2DTransposeController controller = (Data2DTransposeController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DTransposeFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
