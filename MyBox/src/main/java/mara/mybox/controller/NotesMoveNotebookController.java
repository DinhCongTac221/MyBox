package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import mara.mybox.db.data.Notebook;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-9
 * @License Apache License Version 2.0
 */
public class NotesMoveNotebookController extends ControlNotebookSelector {

    protected Notebook sourceBook;

    @FXML
    protected Label sourceLabel;

    public NotesMoveNotebookController() {
        baseTitle = message("Notebook");
    }

    public void setValues(NotesController notesController, Notebook sourceBook, String name) {
        this.sourceBook = sourceBook;
        sourceLabel.setText(message("NotebookMoved") + ":\n" + name);
        setValues(notesController);
    }

    @Override
    public Notebook getIgnoreBook() {
        return sourceBook;
    }

    @FXML
    @Override
    public void okAction() {
        if (notesController == null || sourceBook == null || sourceBook.isRoot()) {
            return;
        }
        TreeItem<Notebook> selectedNode = treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            return;
        }
        Notebook targetBook = selectedNode.getValue();
        if (targetBook == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }

            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    sourceBook.setOwner(targetBook.getNbid());
                    tableNotebook.updateData(sourceBook);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (notesController == null || !notesController.getMyStage().isShowing()) {
                        notesController = NotesController.oneOpen();
                    } else {
                        notesController.refreshBooks();
                        notesController.bookChanged(sourceBook);
                        notesController.bookChanged(targetBook);
                    }
                    notesController.popSuccessful();
                    closeStage();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
