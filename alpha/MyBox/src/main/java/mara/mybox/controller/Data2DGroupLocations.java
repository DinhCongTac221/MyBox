package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-14
 * @License Apache License Version 2.0
 */
public class Data2DGroupLocations extends Data2DLocationDistributionController {

    public Data2DGroupLocations() {
        baseTitle = message("GroupData") + " - " + message("XYChart");
    }

    /*
        static
     */
    public static Data2DGroupLocations open(ControlData2DLoad tableController) {
        try {
            Data2DGroupLocations controller = (Data2DGroupLocations) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartGroupXYFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
