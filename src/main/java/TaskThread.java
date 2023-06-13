import javafx.concurrent.Task;

public class TaskThread<TASK extends Task<?>> extends Thread {

    public TASK task;

    public TaskThread(TASK task){
        super(task);
        this.task = task;
        this.setDaemon(true);
    }

    public void cancel(){
        task.cancel();
    }

}
