package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import mara.mybox.data.ColorData;
import mara.mybox.db.TableColorData;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-09-01
 * @License Apache License Version 2.0
 */
public class ColorImportController extends BaseController {

    public ColorImportController() {
        baseTitle = "ColorImport";
    }

    @FXML
    protected void popMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu = new MenuItem(message("InputColors"));
            menu.setOnAction((ActionEvent event) -> {
                try {
                    ColorInputController controller = (ColorInputController) openStage(CommonValues.ColorInputFxml);
                    controller.setParentController(this);
                    controller.getMyStage().toFront();
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            if (AppVariables.devMode) {
                menu = new MenuItem(message("ImportMyBoxColors"));
                menu.setOnAction((ActionEvent event) -> {
                    importColors("mybox");
                });
                popMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("ImportWebCommonColors"));
            menu.setOnAction((ActionEvent event) -> {
                importColors("web");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImportChineseTraditionalColors"));
            menu.setOnAction((ActionEvent event) -> {
                importColors("chinese");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImportJapaneseTraditionalColors"));
            menu.setOnAction((ActionEvent event) -> {
                importColors("japanese");
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void importColors(String type) {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    List<ColorData> data = ColorData.predefined(type);
                    if (data == null) {
                        return false;
                    }
                    TableColorData.writeData(data, false);
                    return writeInterface(data);
                }

                @Override
                protected void whenSucceeded() {
                }
            };
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void inputColors(TextArea valuesArea) {
        if (valuesArea == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    String[] values = valuesArea.getText().split("\n");
                    if (values == null || values.length == 0) {
                        return true;
                    }
                    List<ColorData> data = new ArrayList<>();
                    for (String value : values) {
                        value = value.trim();
                        ColorData item = new ColorData(value);
                        if (item.getColor() == null) {
                            continue;
                        }
                        item.calculate();
                        if (item.getSrgb() != null) {
                            if (!value.startsWith("#") && !value.startsWith("0x")
                                    && !value.startsWith("rgb") && !value.startsWith("hsl")) {
                                item.setColorName(value);
                            }
                            data.add(item);
                        }
                    }
                    if (data.isEmpty()) {
                        return true;
                    }
                    TableColorData.writeData(data, false);
                    return writeInterface(data);
                }

                @Override
                protected void whenSucceeded() {
                }
            };
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected boolean writeInterface(List<ColorData> data) {
        if (parentController == null) {
            return false;
        }
        String p = parentController.getClass().toString();
        if (p.contains("ColorsManageController")) {
            TableColorData.trimPalette();
            ColorsManageController pController = (ColorsManageController) parentController;
            Platform.runLater(() -> {
                pController.loadTableData();
            });
        } else if (p.contains("ColorPaletteManageController")) {
            ColorPaletteManageController pController = (ColorPaletteManageController) parentController;
            List<Color> palette = new ArrayList<>();
            for (Node node : pController.colorsPane.getChildren()) {
                Rectangle rect = (Rectangle) node;
                palette.add((Color) rect.getFill());
            }
            List<ColorData> newColors = new ArrayList<>();
            for (ColorData color : data) {
                if (!palette.contains(color.getColor())) {
                    newColors.add(color);
                }
            }
            TableColorData.addDataInPalette(newColors, true);
            Platform.runLater(() -> {
                pController.load();
            });

        } else {
            return false;
        }
        return true;
    }

}
