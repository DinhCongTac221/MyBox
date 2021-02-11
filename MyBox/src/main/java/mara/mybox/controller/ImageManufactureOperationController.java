package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.DoublePoint;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureOperationController extends BaseImageController {

    protected ImageManufactureController imageController;
    protected ImageManufactureScopeController scopeController;
    protected ImageManufactureOperationsController operationsController;
    protected ImageView maskView;

    public ImageManufactureOperationController() {
        baseTitle = AppVariables.message("ImageManufacture");
    }

    protected void initPane() {

    }

    protected void paneExpanded() {
    }

    protected void resetOperationPane() {

    }

    public void initOperation() {
        resetOperationPane();
        imageController.resetImagePane();
    }

    @Override
    public BaseImageController refresh() {
        return null;  //Bypass since this is part of frame
    }

    /*
        events passed from image pane
     */
    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {

    }

    @FXML
    public void scopeAction() {
        imageController.controlScopePane();
    }

    public void quitPane() {
    }

}
