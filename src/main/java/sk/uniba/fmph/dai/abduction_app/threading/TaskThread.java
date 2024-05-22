package sk.uniba.fmph.dai.abduction_app.threading;

import javafx.concurrent.Task;

class TaskThread<TASK extends Task<?>> extends Thread {

    TASK task;

    TaskThread(TASK task){
        super(task);
        this.task = task;
        this.setDaemon(true);
    }

    void cancel(){
        task.cancel();
    }

}
