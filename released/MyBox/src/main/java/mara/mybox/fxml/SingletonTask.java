package mara.mybox.fxml;

import mara.mybox.controller.BaseController;
import mara.mybox.controller.LoadingController;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class SingletonTask<Void> extends BaseTask<Void> {

    BaseController controller;
    LoadingController loading;

    public SingletonTask(BaseController controller) {
        this.controller = controller;
    }

    @Override
    protected void whenSucceeded() {
        if (controller != null) {
            controller.popSuccessful();
        }
    }

    @Override
    protected void whenFailed() {
        if (isCancelled()) {
            return;
        }
        if (controller != null) {
            if (error != null) {
                controller.popError(error);
            } else {
                controller.popFailed();
            }
        }
    }


    /*
        get/set
     */
    public BaseController getController() {
        return controller;
    }

    public void setController(BaseController controller) {
        this.controller = controller;
    }

    public LoadingController getLoading() {
        return loading;
    }

    public void setLoading(LoadingController loading) {
        this.loading = loading;
    }

}