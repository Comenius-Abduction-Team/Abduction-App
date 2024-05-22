package sk.uniba.fmph.dai.abduction_app.threading;

import javafx.application.Platform;
import javafx.concurrent.Task;
import sk.uniba.fmph.dai.abduction_api.abducer.IAbducer;
import sk.uniba.fmph.dai.abduction_app.AbductionRunner;
import sk.uniba.fmph.dai.abduction_app.ApplicationController;

class AbductionTask extends Task<IAbducer> {

    ApplicationController controller;
    ThreadManager threader;

    AbductionTask(ThreadManager threader, ApplicationController controller){
        this.threader = threader;
        this.controller = controller;
    }

    @Override
    protected IAbducer call() {

        AbductionRunner runner = new AbductionRunner(controller, threader);
        try {
            return runner.runAbduction();
        } catch(InterruptedException ignored){
        } catch(Throwable e){
            Platform.runLater(() -> controller.showErrorMessage(e));
            controller.stopSolving();
        }

        return null;
    }

    public void updateProgressPercentage(double percentage){
        updateProgress(percentage, 100.0);
    }

    public void updateProgressMessage(String message){
        updateMessage(message);
    }

}