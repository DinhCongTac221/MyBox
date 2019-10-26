package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FileInformation.FileSelectorType;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.TableFileSizeCell;
import mara.mybox.fxml.TableNumberCell;
import mara.mybox.fxml.TableTimeCell;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonImageValues;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */

/*
    T must be subClass of FileInformation
 */
public abstract class TableController<P> extends BaseController {

    protected ObservableList<P> tableData;
    protected long totalFilesNumber, totalFilesSize, fileSelectorSize, fileSelectorTime;
    protected FileSelectorType fileSelectorType;

    @FXML
    protected Button addFilesButton, insertFilesButton, addDirectoryButton, insertDirectoryButton,
            deleteFilesButton, clearFilesButton, upFilesButton, downFilesButton, viewFileButton,
            unselectAllFilesButton, selectAllFilesButton;
    @FXML
    protected TableView<P> tableView;
    @FXML
    protected TableColumn<P, String> handledColumn, fileColumn, typeColumn;
    @FXML
    protected TableColumn<P, Long> numberColumn, sizeColumn, modifyTimeColumn, createTimeColumn;
    @FXML
    protected CheckBox tableSubdirCheck, tableCreateDirCheck, tableExpandDirCheck;
    @FXML
    protected ComboBox<String> nameFiltersSelector;
    @FXML
    protected TextField tableFiltersInput;
    @FXML
    protected Label tableLabel;

    public TableController() {
        sourceExtensionFilter = CommonImageValues.AllExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    /*
        Abstract methods
     */
    protected abstract P create(File file);

    /*
        Pretected methods
     */
    protected void initTable() {
        try {
            if (tableView == null) {
                return;
            }

            tableData = FXCollections.observableArrayList();
            tableData.addListener(new ListChangeListener<P>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends P> change) {
                    if (!isSettingValues) {
                        tableChanged();
                    }
                }
            });

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (!isSettingValues) {
                        tableSelected();
                    }
                }
            });
            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        viewFileAction();
                    }
                }
            });

            initColumns();
            tableView.setItems(tableData);

            if (tableExpandDirCheck != null) {
                tableExpandDirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                        if (tableExpandDirCheck.isSelected()) {
                            expandDirectories();
                        }
                    }
                });
            }

            if (tableCreateDirCheck != null) {
                tableCreateDirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                        AppVariables.setUserConfigValue("TableCreateDirctories", newv);
                    }
                });
                tableCreateDirCheck.setSelected(AppVariables.getUserConfigBoolean("TableCreateDirctories", true));
            }

            if (nameFiltersSelector != null) {
                for (FileSelectorType type : FileSelectorType.values()) {
                    nameFiltersSelector.getItems().add(message(type.name()));
                }
                nameFiltersSelector.setVisibleRowCount(FileSelectorType.values().length);
                nameFiltersSelector.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                        String selected = nameFiltersSelector.getSelectionModel().getSelectedItem();
                        for (FileSelectorType type : FileSelectorType.values()) {
                            if (message(type.name()).equals(selected)) {
                                fileSelectorType = type;
                                break;
                            }
                        }
                        if (regexLink != null) {
                            regexLink.setVisible(fileSelectorType == FileSelectorType.NameMatchAnyRegularExpression
                                    || fileSelectorType == FileSelectorType.NameNotMatchAnyRegularExpression
                            );
                        }

                        tableFiltersInput.setText("");
                        switch (fileSelectorType) {
                            case FileSizeLargerThan:
                            case FileSizeSmallerThan:
                                tableFiltersInput.setPromptText(message("FileSizeComments"));
                                FxmlControl.setTooltip(tableFiltersInput, new Tooltip(message("FileSizeComments")));
                                break;
                            case ModifiedTimeEarlierThan:
                            case ModifiedTimeLaterThan:
                                tableFiltersInput.setText("2019-10-24 10:10:10");
                                FxmlControl.setTooltip(tableFiltersInput, new Tooltip("2019-10-24 10:10:10"));
                                break;
                            default:
                                tableFiltersInput.setPromptText(message("SeparateBySpace"));
                                FxmlControl.setTooltip(tableFiltersInput, new Tooltip(message("SeparateBySpace")));
                                break;
                        }
                    }
                });
                nameFiltersSelector.getSelectionModel().select(0);

                tableFiltersInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                        if (newv == null || newv.trim().isEmpty()) {
                            tableFiltersInput.setStyle(null);
                            fileSelectorSize = -1;
                            fileSelectorTime = -1;
                            return;
                        }
                        if (fileSelectorType == FileSelectorType.FileSizeLargerThan
                                || fileSelectorType == FileSelectorType.FileSizeSmallerThan) {
                            long v = ByteTools.checkBytesValue(newv);
                            if (v >= 0) {
                                fileSelectorSize = v;
                                tableFiltersInput.setStyle(null);
                            } else {
                                tableFiltersInput.setStyle(badStyle);
                                popError(message("FileSizeComments"));
                            }

                        } else if (fileSelectorType == FileSelectorType.ModifiedTimeEarlierThan
                                || fileSelectorType == FileSelectorType.ModifiedTimeLaterThan) {
                            Date d = DateTools.stringToDatetime(newv);
                            if (d != null) {
                                fileSelectorTime = d.getTime();
                            } else {
                                fileSelectorTime = -1;
                            }

                        }
                    }
                });
            }
            fileSelectorType = FileSelectorType.All;

            if (tableLabel != null) {
                tableLabel.setText("");
            }

            tableSelected();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initColumns() {
        try {

            if (handledColumn != null) {
                handledColumn.setCellValueFactory(new PropertyValueFactory<>("handled"));
            }
            if (fileColumn != null) {
                fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
                fileColumn.setPrefWidth(320);
            }
            if (typeColumn != null) {
                typeColumn.setCellValueFactory(new PropertyValueFactory<>("fileType"));
            }
            if (numberColumn != null) {
                numberColumn.setCellValueFactory(new PropertyValueFactory<>("filesNumber"));
                numberColumn.setCellFactory(new TableNumberCell());
            }
            if (sizeColumn != null) {
                sizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
                sizeColumn.setCellFactory(new TableFileSizeCell());
            }

            if (modifyTimeColumn != null) {
                modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
                modifyTimeColumn.setCellFactory(new TableTimeCell());
            }
            if (createTimeColumn != null) {
                createTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
                createTimeColumn.setCellFactory(new TableTimeCell());
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void tableChanged() {
        if (tableData.isEmpty()) {
            if (insertFilesButton != null) {
                insertFilesButton.setDisable(true);
            }
            if (insertDirectoryButton != null) {
                insertDirectoryButton.setDisable(true);
            }
            if (upFilesButton != null) {
                upFilesButton.setDisable(true);
            }
            if (downFilesButton != null) {
                downFilesButton.setDisable(true);
            }
            deleteFilesButton.setDisable(true);
            clearFilesButton.setDisable(true);
        } else {
            clearFilesButton.setDisable(false);
        }
        if (tableLabel != null) {
            countTotal();
            tableLabel.setText(MessageFormat.format(message("TotalFilesNumberSize"),
                    totalFilesNumber, FileTools.showFileSize(totalFilesSize)) + "    "
                    + message("DoubleClickToView"));
        }
        if (parentController != null) {
            parentController.dataChanged();
        }
    }

    protected void tableSelected() {
        P selected = tableView.getSelectionModel().getSelectedItem();
        boolean none = (selected == null);
        deleteFilesButton.setDisable(none);

        if (upFilesButton != null) {
            upFilesButton.setDisable(none);
        }
        if (downFilesButton != null) {
            downFilesButton.setDisable(none);
        }
        if (insertFilesButton != null) {
            insertFilesButton.setDisable(none);
        }
        if (insertDirectoryButton != null) {
            insertDirectoryButton.setDisable(none);
        }
        if (viewFileButton != null) {
            viewFileButton.setDisable(none);
        }
    }

    protected void expandDirectories() {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        int index = 0;
        boolean changed = false;
        isSettingValues = true;
        while (index < tableData.size()) {
            File file = file(index);
            if (file.isFile()) {
                if (isValidFile(file)) {
                    index++;
                } else {
                    tableData.remove(index);
                    changed = true;
                }
                continue;
            }
            tableData.remove(index);
            changed = true;
            List<File> files = FileTools.allFiles(file);
            if (files == null || files.isEmpty()) {
                continue;
            }
            List<File> valids = new ArrayList<>();
            for (File afile : files) {
                if (isValidFile(afile)) {
                    valids.add(afile);
                }
            }
            if (valids.isEmpty()) {
                continue;
            }
            addFiles(index, valids);
            index += valids.size();
        }
        isSettingValues = false;
        if (changed) {
            tableView.refresh();
            tableChanged();
        }
    }

    protected boolean isValidFile(File file) {
        return true;
    }

    /*
        Public methods
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            initTable();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public P data(int index) {
        try {
            return tableData.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public FileInformation fileInformation(int index) {
        try {
            return (FileInformation) tableData.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public File file(int index) {
        try {
            return fileInformation(index).getFile();
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    @Override
    public void addFilesAction() {
        addFiles(tableData.size());
    }

    public void addFiles(int index) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = AppVariables.getUserConfigPath(sourcePathKey);
            if (defaultPath.exists()) {
                fileChooser.setInitialDirectory(defaultPath);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            addFiles(index, files);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void addFile(File file) {
        addFile(tableData.size(), file);
    }

    public void addFile(int index, File file) {
        if (file == null) {
            return;
        }
        List<File> files = new ArrayList<>();
        files.add(file);
        addFiles(index, files);
    }

    public void addFiles(int index, List<File> files) {
        try {
            if (files == null || files.isEmpty()) {
                return;
            }
            recordFileAdded(files.get(0));

            List<P> infos = new ArrayList<>();
            for (File file : files) {
                P t = create(file);
                infos.add(t);
            }
            if (infos.isEmpty()) {
                return;
            }
            if (index < 0 || index >= tableData.size()) {
                tableData.addAll(infos);
            } else {
                tableData.addAll(index, infos);
            }
            tableView.refresh();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void addDirectoryAction() {
        addDirectory(tableData.size());
    }

    public void addDirectory(int index) {
        try {
            DirectoryChooser dirChooser = new DirectoryChooser();
            File defaultPath = AppVariables.getUserConfigPath(sourcePathKey);
            if (defaultPath != null) {
                dirChooser.setInitialDirectory(defaultPath);
            }
            File directory = dirChooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            addDirectory(index, directory);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void addDirectory(File directory) {
        addDirectory(tableData.size(), directory);
    }

    public void addDirectory(int index, File directory) {
        try {
            recordFileOpened(directory);

            if (tableExpandDirCheck != null && tableExpandDirCheck.isSelected()) {
                List<File> files = FileTools.allFiles(directory);
                List<File> valids = new ArrayList<>();
                for (File afile : files) {
                    if (isValidFile(afile)) {
                        valids.add(afile);
                    }
                }
                if (!valids.isEmpty()) {
                    addFiles(index, valids);
                }

            } else {
                P d = create(directory);
                if (index < 0 || index >= tableData.size()) {
                    tableData.add(d);
                } else {
                    tableData.add(index, d);
                }
                tableView.refresh();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void insertFilesAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addFiles(index);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @Override
    public void insertFile(File file) {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addFile(index, file);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @FXML
    @Override
    public void insertDirectoryAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addDirectory(index);
        } else {
            insertDirectoryButton.setDisable(true);
        }
    }

    @Override
    public void insertDirectory(File directory) {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addDirectory(index, directory);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @FXML
    public void upFilesAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        List<Integer> newselected = new ArrayList<>();
        for (Integer index : selected) {
            if (index == 0 || newselected.contains(index - 1)) {
                newselected.add(index);
                continue;
            }
            P info = tableData.get(index);
            tableData.set(index, tableView.getItems().get(index - 1));
            tableData.set(index - 1, info);
            newselected.add(index - 1);
        }
        tableView.getSelectionModel().clearSelection();
        for (Integer index : newselected) {
            tableView.getSelectionModel().select(index);
        }
        tableView.refresh();
    }

    @FXML
    public void downFilesAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        List<Integer> newselected = new ArrayList<>();
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index == tableData.size() - 1
                    || newselected.contains(index + 1)) {
                newselected.add(index);
                continue;
            }
            P info = tableData.get(index);
            tableData.set(index, tableData.get(index + 1));
            tableData.set(index + 1, info);
            newselected.add(index + 1);
        }
        tableView.getSelectionModel().clearSelection();
        for (Integer index : newselected) {
            tableView.getSelectionModel().select(index);
        }
        tableView.refresh();

    }

    public void viewFileAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index < 0 || index > tableData.size() - 1) {
            return;
        }
        FileInformation info = fileInformation(index);
        if (info.getNewName() != null && !info.getNewName().isEmpty()) {
            view(info.getNewName());
        } else {
            view(info.getFile());
        }

    }

    @FXML
    public void selectAllFilesAction() {
        isSettingValues = true;
        tableView.getSelectionModel().selectAll();
        isSettingValues = false;
        tableSelected();
    }

    @FXML
    public void unselectAllFilesAction() {
        isSettingValues = true;
        tableView.getSelectionModel().clearSelection();
        isSettingValues = false;
        tableSelected();
    }

    @FXML
    public void deleteFilesAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < 0 || index > tableData.size() - 1) {
                continue;
            }
            tableData.remove(index);
        }
        tableView.refresh();
    }

    @FXML
    public void clearFilesAction() {
        tableData.clear();
        tableView.refresh();
    }

    public void markFileHandled(int index, String message) {
        FileInformation d = fileInformation(index);
        if (d == null) {
            return;
        }
        d.setHandled(message);
        tableView.refresh();
    }

    public void markFileHandled(int index) {
        markFileHandled(index, message("Yes"));
    }

    public void countTotal() {
        totalFilesNumber = totalFilesSize = 0;
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        for (int i = 0; i < tableData.size(); i++) {
            FileInformation info = fileInformation(i);
            totalFilesNumber += info.getFilesNumber();
            totalFilesSize += info.getFileSize();
        }
    }


    /*
        get/set
     */
    public ObservableList<P> getTableData() {
        return tableData;
    }

    public void setTableData(ObservableList<P> tableData) {
        this.tableData = tableData;
    }

    public long getTotalFilesNumber() {
        return totalFilesNumber;
    }

    public void setTotalFilesNumber(long totalFilesNumber) {
        this.totalFilesNumber = totalFilesNumber;
    }

    public long getTotalFilesSize() {
        return totalFilesSize;
    }

    public void setTotalFilesSize(long totalFilesSize) {
        this.totalFilesSize = totalFilesSize;
    }

    public Button getAddFilesButton() {
        return addFilesButton;
    }

    public void setAddFilesButton(Button addFilesButton) {
        this.addFilesButton = addFilesButton;
    }

    public Button getInsertFilesButton() {
        return insertFilesButton;
    }

    public void setInsertFilesButton(Button insertFilesButton) {
        this.insertFilesButton = insertFilesButton;
    }

    public Button getAddDirectoryButton() {
        return addDirectoryButton;
    }

    public void setAddDirectoryButton(Button addDirectoryButton) {
        this.addDirectoryButton = addDirectoryButton;
    }

    public Button getInsertDirectoryButton() {
        return insertDirectoryButton;
    }

    public void setInsertDirectoryButton(Button insertDirectoryButton) {
        this.insertDirectoryButton = insertDirectoryButton;
    }

    public Button getDeleteFilesButton() {
        return deleteFilesButton;
    }

    public void setDeleteFilesButton(Button deleteFilesButton) {
        this.deleteFilesButton = deleteFilesButton;
    }

    public Button getClearFilesButton() {
        return clearFilesButton;
    }

    public void setClearFilesButton(Button clearFilesButton) {
        this.clearFilesButton = clearFilesButton;
    }

    public Button getUpFilesButton() {
        return upFilesButton;
    }

    public void setUpFilesButton(Button upFilesButton) {
        this.upFilesButton = upFilesButton;
    }

    public Button getDownFilesButton() {
        return downFilesButton;
    }

    public void setDownFilesButton(Button downFilesButton) {
        this.downFilesButton = downFilesButton;
    }

    public Button getViewFileButton() {
        return viewFileButton;
    }

    public void setViewFileButton(Button viewFileButton) {
        this.viewFileButton = viewFileButton;
    }

    public Button getUnselectAllFilesButton() {
        return unselectAllFilesButton;
    }

    public void setUnselectAllFilesButton(Button unselectAllFilesButton) {
        this.unselectAllFilesButton = unselectAllFilesButton;
    }

    public Button getSelectAllFilesButton() {
        return selectAllFilesButton;
    }

    public void setSelectAllFilesButton(Button selectAllFilesButton) {
        this.selectAllFilesButton = selectAllFilesButton;
    }

    public TableView<P> getTableView() {
        return tableView;
    }

    public void setTableView(TableView<P> tableView) {
        this.tableView = tableView;
    }

    public TableColumn<P, String> getHandledColumn() {
        return handledColumn;
    }

    public void setHandledColumn(TableColumn<P, String> handledColumn) {
        this.handledColumn = handledColumn;
    }

    public TableColumn<P, String> getFileColumn() {
        return fileColumn;
    }

    public void setFileColumn(TableColumn<P, String> fileColumn) {
        this.fileColumn = fileColumn;
    }

    public TableColumn<P, Long> getNumberColumn() {
        return numberColumn;
    }

    public void setNumberColumn(TableColumn<P, Long> numberColumn) {
        this.numberColumn = numberColumn;
    }

    public TableColumn<P, Long> getSizeColumn() {
        return sizeColumn;
    }

    public void setSizeColumn(TableColumn<P, Long> sizeColumn) {
        this.sizeColumn = sizeColumn;
    }

    public TableColumn<P, Long> getModifyTimeColumn() {
        return modifyTimeColumn;
    }

    public void setModifyTimeColumn(TableColumn<P, Long> modifyTimeColumn) {
        this.modifyTimeColumn = modifyTimeColumn;
    }

    public TableColumn<P, Long> getCreateTimeColumn() {
        return createTimeColumn;
    }

    public void setCreateTimeColumn(TableColumn<P, Long> createTimeColumn) {
        this.createTimeColumn = createTimeColumn;
    }

    public TableColumn<P, String> getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(TableColumn<P, String> typeColumn) {
        this.typeColumn = typeColumn;
    }

    public CheckBox getTableSubdirCheck() {
        return tableSubdirCheck;
    }

    public void setTableSubdirCheck(CheckBox tableSubdirCheck) {
        this.tableSubdirCheck = tableSubdirCheck;
    }

    public ComboBox<String> getNameFiltersSelector() {
        return nameFiltersSelector;
    }

    public void setNameFiltersSelector(ComboBox<String> nameFiltersSelector) {
        this.nameFiltersSelector = nameFiltersSelector;
    }

    public CheckBox getTableExpandDirCheck() {
        return tableExpandDirCheck;
    }

    public void setTableExpandDirCheck(CheckBox tableExpandDirCheck) {
        this.tableExpandDirCheck = tableExpandDirCheck;
    }

    public TextField getTableFiltersInput() {
        return tableFiltersInput;
    }

    public void setTableFiltersInput(TextField tableFiltersInput) {
        this.tableFiltersInput = tableFiltersInput;
    }

    public Label getTableLabel() {
        return tableLabel;
    }

    public void setTableLabel(Label tableLabel) {
        this.tableLabel = tableLabel;
    }

    public FileSelectorType getFileSelectorType() {
        return fileSelectorType;
    }

    public void setFileSelectorType(FileSelectorType fileSelectorType) {
        this.fileSelectorType = fileSelectorType;
    }

}