package mara.mybox.controller;

import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.color.CIEData;
import mara.mybox.color.RGBColorSpace;
import mara.mybox.color.RGBColorSpace.ColorSpaceType;
import static mara.mybox.color.RGBColorSpace.primariesTristimulus;
import static mara.mybox.color.RGBColorSpace.whitePointMatrix;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.MatrixDoubleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-5-20
 * @Description
 * @License Apache License Version 2.0
 */
public class RGBColorSpacesController extends ChromaticityBaseController {

    protected ObservableList<RGBColorSpace> colorSpaces;

    @FXML
    public RGBColorSpaceController rgbController;
    @FXML
    public WhitePointController whiteController;
    @FXML
    protected TextArea primariesArea;
    @FXML
    protected WebView webView;
    @FXML
    protected TableView<RGBColorSpace> primariesTableView;
    @FXML
    protected TableColumn<RGBColorSpace, String> csColumn, whiteColumn, colorColumn, algorithmColumn;
    @FXML
    protected TableColumn<RGBColorSpace, Double> txcsColumn, tycsColumn, tzcsColumn, nxcsColumn, nycsColumn, nzcsColumn,
            rxcsColumn, rycsColumn, rzcsColumn;
    @FXML
    protected Button calculateButton, exportButton;

    public RGBColorSpacesController() {
        baseTitle = AppVariables.message("RGBColorSpaces");
        exportName = "RGBColorSpaces";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initData();

            initAdaptation();
            initPrimaries();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    private void initAdaptation() {

        initOptions();

        calculateButton.disableProperty().bind(Bindings.isEmpty(scaleInput.textProperty())
                .or(scaleInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.redXInput.textProperty()))
                .or(rgbController.redXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.redYInput.textProperty()))
                .or(rgbController.redYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.redZInput.textProperty()))
                .or(rgbController.redZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.greenXInput.textProperty()))
                .or(rgbController.greenXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.greenYInput.textProperty()))
                .or(rgbController.greenYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.greenZInput.textProperty()))
                .or(rgbController.greenZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.blueXInput.textProperty()))
                .or(rgbController.blueXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.blueYInput.textProperty()))
                .or(rgbController.blueYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.blueZInput.textProperty()))
                .or(rgbController.blueZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.whiteXInput.textProperty()))
                .or(rgbController.whiteXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.whiteYInput.textProperty()))
                .or(rgbController.whiteYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.whiteZInput.textProperty()))
                .or(rgbController.whiteZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(whiteController.xInput.textProperty()))
                .or(whiteController.xInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(whiteController.yInput.textProperty()))
                .or(whiteController.yInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(whiteController.zInput.textProperty()))
                .or(whiteController.zInput.styleProperty().isEqualTo(badStyle))
        );

    }

    private void initPrimaries() {
        csColumn.setCellValueFactory(new PropertyValueFactory<>("colorSpaceName"));
        whiteColumn.setCellValueFactory(new PropertyValueFactory<>("illuminantName"));
        algorithmColumn.setCellValueFactory(new PropertyValueFactory<>("adaptAlgorithm"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("colorName"));
        txcsColumn.setCellValueFactory(new PropertyValueFactory<>("X"));
        tycsColumn.setCellValueFactory(new PropertyValueFactory<>("Y"));
        tzcsColumn.setCellValueFactory(new PropertyValueFactory<>("Z"));
        nxcsColumn.setCellValueFactory(new PropertyValueFactory<>("normalizedX"));
        nycsColumn.setCellValueFactory(new PropertyValueFactory<>("normalizedY"));
        nzcsColumn.setCellValueFactory(new PropertyValueFactory<>("normalizedZ"));
        rxcsColumn.setCellValueFactory(new PropertyValueFactory<>("relativeX"));
        rycsColumn.setCellValueFactory(new PropertyValueFactory<>("relativeY"));
        rzcsColumn.setCellValueFactory(new PropertyValueFactory<>("relativeZ"));

    }

    private void initData() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String colorSpacesString;

                @Override
                protected boolean handle() {
                    colorSpaces = FXCollections.observableArrayList();
                    colorSpaces.addAll(RGBColorSpace.all(8));
                    colorSpacesString = RGBColorSpace.allTexts();
                    return colorSpacesString != null;
                }

                @Override
                protected void whenSucceeded() {
                    primariesTableView.setItems(colorSpaces);
                    primariesArea.setText(colorSpacesString);
                    primariesArea.home();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    public String exportTexts() {
        return primariesArea.getText();
    }

    @FXML
    public void calculateAction(ActionEvent event) {
        try {
            webView.getEngine().loadContent("");
            if (calculateButton.isDisabled()) {
                return;
            }
            double[][] primaries, sourceWhitePoint;
            if (rgbController.colorSpaceName != null) {
                ColorSpaceType cs = RGBColorSpace.type(rgbController.colorSpaceName);
                primaries = primariesTristimulus(cs);
                sourceWhitePoint = whitePointMatrix(cs);
            } else {
                primaries = new double[3][];
                primaries[0] = rgbController.red;
                primaries[1] = rgbController.green;
                primaries[2] = rgbController.blue;
                sourceWhitePoint = MatrixDoubleTools.columnVector(rgbController.white);
            }
            double[][] targetWhitePoint = MatrixDoubleTools.columnVector(whiteController.relative);
            if (primaries == null || sourceWhitePoint == null || targetWhitePoint == null) {
                return;
            }
            Map<String, Object> adapted = (Map<String, Object>) RGBColorSpace.primariesAdapted(primaries,
                    sourceWhitePoint, targetWhitePoint, algorithm, scale, true);
            double[][] adaptedPrimaries = (double[][]) adapted.get("adaptedPrimaries");
            double[][] normalized = MatrixDoubleTools.scale(CIEData.normalize(adaptedPrimaries), scale);
            double[][] relative = MatrixDoubleTools.scale(CIEData.relative(adaptedPrimaries), scale);
            String s = message("AdaptedPrimaries") + ": \n"
                    + message("NormalizedValuesCC") + " = \n"
                    + MatrixDoubleTools.print(normalized, 20, scale)
                    + message("RelativeValues") + " = \n"
                    + MatrixDoubleTools.print(relative, 20, scale)
                    + message("Tristimulus") + " = \n"
                    + MatrixDoubleTools.print(adaptedPrimaries, 20, scale)
                    + "\n----------------" + message("CalculationProcedure") + "----------------\n"
                    + message("ReferTo") + "： \n"
                    + "            http://brucelindbloom.com/index.html?WorkingSpaceInfo.html \n"
                    + "            http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html \n\n"
                    + (String) adapted.get("procedure");
            webView.getEngine().loadContent("<pre>" + s + "</pre>");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
