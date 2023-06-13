import abduction_api.manager.AbductionManager;
import abduction_api.manager.ThreadAbductionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class ThreadManager {

    TaskThread<AbductionTask> primaryThread;
    TaskThread<Task<Void>> secondaryThread;
    ThreadManager threader = this;
    ApplicationController controller;
    AbductionRunner runner;

    public ThreadManager(ApplicationController controller) {
        this.controller = controller;
    }

    public boolean isRunning(){
        return !(primaryThread.isInterrupted() || primaryThread.task.isCancelled());
    }

    public void runAbductionThread(){
        primaryThread = new TaskThread<AbductionTask>(new AbductionTask());
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

    public void runSecondaryThread(ThreadAbductionManager abductionManager){

        secondaryThread = new TaskThread<Task<Void>>(new Task<Void>() {

            @Override
            protected Void call(){
                abductionManager.run();
                return null;
            }

        });

        secondaryThread.start();
    }

    private class AbductionTask extends Task<AbductionManager>{

        @Override
        protected AbductionManager call() throws Exception {

            runner = new AbductionRunner(controller, threader);
            try {
                return runner.runAbduction();
            } catch(InterruptedException ignored){
            } catch(Throwable e){
                Platform.runLater(() -> controller.showErrorMessage(e));
                controller.stopSolving();
            }

            return null;
        }

        public void updateProgress(double percentage){
            updateProgress(percentage, 100.0);
        }

        public void updateProgressMessage(String message){
            updateMessage(message);
        }

    }

    public void updateAbductionTaskProgress(double percentage, String message){
        primaryThread.task.updateProgress(percentage);
        primaryThread.task.updateProgressMessage(message);
    }


}
