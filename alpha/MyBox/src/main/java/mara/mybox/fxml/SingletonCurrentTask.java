package mara.mybox.fxml;

import mara.mybox.controller.BaseController;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class SingletonCurrentTask<Void> extends SingletonTask<Void> {

    public SingletonCurrentTask(BaseController controller) {
        this.controller = controller;
    }

    @Override
    protected void finalAction() {
        if (controller != null) {
            controller.setTask(null);
        }
        super.finalAction();
    }

}
