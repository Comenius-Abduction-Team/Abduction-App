package sk.uniba.fmph.dai.abduction_app.threading;

import sk.uniba.fmph.dai.abduction_api.abducer.IThreadAbducer;
import javafx.concurrent.Task;
import sk.uniba.fmph.dai.abduction_app.ApplicationController;

/** handles the threads in thread mode abduction solving **/
public class ThreadManager {

    /** thread which processes the found explanations **/
    TaskThread<AbductionTask> primaryThread;
    /** thread in which solving itself runs **/
    TaskThread<Task<Void>> secondaryThread;
    ApplicationController controller;

    public ThreadManager(ApplicationController controller) {
        this.controller = controller;
    }

    public boolean isRunning(){
        return !(primaryThread.isInterrupted() || primaryThread.task.isCancelled());
    }

    public void runPrimaryThread(){
        primaryThread = new TaskThread<>(new AbductionTask(this, controller));
        AbductionTask task = primaryThread.task;
        setListeners(task);
        primaryThread.start();
    }

    private void setListeners(AbductionTask task){

        controller.progressBar.progressProperty().bind(task.progressProperty());

        task.messageProperty().addListener(
                (observable, oldValue, newValue) -> controller.progressMessage.setText(newValue));

        task.setOnSucceeded(state -> {
            controller.processFinalExplanations(task.getValue());
            controller.stopSolving();
        });

        task.setOnFailed(state -> {
            controller.showErrorMessage(task.getException());
            controller.stopSolving();
            controller.progressBar.progressProperty().unbind();
        });
    }

    public void stopThreads(){
        primaryThread.cancel();
        primaryThread.interrupt();
        if (secondaryThread != null) {
            secondaryThread.cancel();
            secondaryThread.interrupt();
        }
        controller.progressBar.progressProperty().unbind();
        controller.progressBar.progressProperty().setValue(0);
    }

    public void runSecondaryThread(IThreadAbducer abducer){

        secondaryThread = new TaskThread<>(new Task<>() {

            @Override
            protected Void call() {
                abducer.run();
                return null;
            }

        });

        secondaryThread.start();
    }

    public void updateAbductionTaskProgress(double percentage, String message){
        primaryThread.task.updateProgressPercentage(percentage);
        primaryThread.task.updateProgressMessage(message);
    }


}
