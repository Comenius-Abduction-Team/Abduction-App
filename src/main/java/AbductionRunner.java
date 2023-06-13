import abduction_api.abducible.*;
import abduction_api.factory.AbductionFactory;
import abduction_api.manager.AbductionManager;
import abduction_api.manager.ThreadAbductionManager;
import abduction_api.monitor.AbductionMonitor;
import javafx.application.Platform;
import org.semanticweb.owlapi.model.*;

import java.util.Set;
import java.util.stream.Collectors;

public class AbductionRunner {

    public AbductionManager manager;

    ApplicationController controller;
    ThreadManager threader;

    public AbductionRunner(ApplicationController controller, ThreadManager threader){
        this.controller = controller;
        this.threader = threader;
    }

    public AbductionManager runAbduction() throws OWLOntologyCreationException, InterruptedException {

        Solver solver = controller.currentSolver;
        AbductionFactory factory = SolverDescriptor.solverFactories.get(solver);
        SolverDescriptor descriptor = controller.descriptors.get(controller.currentSolver);

        OWLOntology backgroundKnowledge = InputParser.parseOntology(controller.bkText.getText());
        if (backgroundKnowledge.getSignature().isEmpty())
            throw new RuntimeException("The background knowledge is empty!");

        OWLOntology observationOnt = InputParser.parseOntology(controller.observationText.getText());
        Set<OWLAxiom> axioms = observationOnt.getAxioms();
        axioms = axioms.stream().filter(axiom -> axiom.getAxiomType() != AxiomType.DECLARATION).collect(Collectors.toSet());
        if (axioms.isEmpty())
            throw new RuntimeException("The observation is empty!");

        if (descriptor.hasMultiObservation){
            manager = factory.getMultiObservationAbductionManager(backgroundKnowledge, axioms);
        }
        else {
            if (axioms.size() > 1)
                throw new RuntimeException("Chosen solver doesn't support multiple axioms in the observation!");
            OWLAxiom observation = axioms.stream().findFirst().get();
            manager = factory.getAbductionManager(backgroundKnowledge, observation);
        }

        AbducibleContainer container = null;
        OWLOntology abducibleOnt;

        if (descriptor.hasSymbolAbducibles && controller.symbolRadio.isSelected()){
            abducibleOnt = InputParser.parseOntology(controller.abduciblesText.getText());
            container = factory.getSymbolAbducibleContainer(abducibleOnt.getSignature());
        }
        else if (descriptor.hasAxiomAbducibles && controller.axiomRadio.isSelected()){
            abducibleOnt = InputParser.parseOntology(controller.abduciblesText.getText());
            axioms = abducibleOnt.getAxioms();
            axioms = axioms .stream()
                            .filter(axiom -> axiom.getAxiomType() != AxiomType.DECLARATION)
                            .collect(Collectors.toSet());
            container = factory.getAxiomAbducibleContainer(axioms);
        }

        if (container != null)
            manager.setAbducibleContainer(container);

        if (descriptor.hasExplanationConfiguration()){
            ExplanationConfigurator configurator = factory.getExplanationConfigurator();

            if (descriptor.hasConceptSwitch)
                ((ConceptExplanationConfigurator)configurator).allowConceptAssertions(controller.conceptCheckbox.isSelected());

            if (descriptor.hasComplexConceptSwitch)
                ((ComplexConceptExplanationConfigurator)configurator).allowComplexConcepts(controller.complexCheckbox.isSelected());
            if (descriptor.hasConceptComplementSwitch)
                ((ComplexConceptExplanationConfigurator)configurator).allowConceptComplements(controller.complementCheckbox.isSelected());

            if (descriptor.hasRoleSwitch)
                ((RoleExplanationConfigurator)configurator).allowRoleAssertions(controller.roleCheckbox.isSelected());
            if (descriptor.hasLoopSwitch)
                ((RoleExplanationConfigurator)configurator).allowLoops(controller.loopCheckbox.isSelected());

            manager.setExplanationConfigurator(configurator);
        }

        int timeout;

        try{
            timeout = controller.timeoutSetter.getValue();
        } catch(Throwable e){
            timeout = 0;
        }
        if (descriptor.hasTimeLimit) this.manager.setTimeout(timeout);

        if (descriptor.hasSpecificParameters) {
            manager.resetSolverSpecificParameters();
            String parameters = controller.parameterSetter.getText();
            if (!parameters.isEmpty()) this.manager.setSolverSpecificParameters(parameters);
        }

        if (descriptor.hasMultiThread) {

            ThreadAbductionManager threadManager = (ThreadAbductionManager) manager;
            AbductionMonitor monitor = threadManager.getAbductionMonitor();
            monitor.setWaitLimit(1000);
            Platform.runLater(() -> controller.printExplanationMessage("POSSIBLE EXPLANATIONS:"));

            threader.runSecondaryThread(threadManager);

            while (threader.isRunning()) {
                synchronized (monitor) {

                    monitor.wait();
                    if (monitor.areNewExplanationsAvailable()) {
                        Platform.runLater(() -> {
                            controller.printExplanations(monitor.getUnprocessedExplanations());
                            monitor.markExplanationsAsProcessed();
                            monitor.clearExplanations();
                        });

                    }
                    if (monitor.isNewProgressAvailable()) {
                        int progress = monitor.getProgress().getValue();
                        String message = monitor.getStatusMessage();
                        threader.updateAbductionTaskProgress(progress, message);

                        //Platform.runLater(() -> {
                              //controller.updateProgressBar(progress / 100.0);
                              //controller.changeStatusMessage(message);
                        //});
//                            Platform.runLater(() -> {
//                                controller.updateProgressBar(progress/100.0);
//                                controller.changeStatusMessage(message);
//                            });

                    }
                    if (monitor.getProgress().getValue() >= 100) {
                        //t.interrupt();
                        monitor.notify();
                        break;
                    }
                    monitor.notify();
                }

            }

            Platform.runLater(() -> controller.printExplanationMessage("\n"));

        }

        else {
            Platform.runLater(() -> {
                threader.updateAbductionTaskProgress(0, "Abduction initialized");
                //controller.updateProgressBar(0);
                //controller.changeStatusMessage("Abduction initialized");
            });

            manager.solveAbduction();

            Platform.runLater(() -> {
                threader.updateAbductionTaskProgress(100, "Abduction finished");
                //controller.updateProgressBar(100);
                //controller.changeStatusMessage("Abduction finished");
            });
        }

        return manager;

//        Platform.runLater(() -> {
//            controller.addLogText(manager.getFullLog());
//        });
//
//        Set<ExplanationWrapper> explanations = manager.getExplanations();
//        if (explanations.isEmpty()){
//            String message = manager.getOutputMessage();
//            if (message == null || message.isEmpty())
//                Platform.runLater(() -> controller.printExplanationMessage("No explanations could be found!"));
//            else
//                Platform.runLater(() -> controller.printExplanationMessage(manager.getOutputMessage()));
//        }
//        else {
//            Platform.runLater(() -> {
//                controller.printExplanationMessage("FINAL EXPLANATIONS:");
//                controller.printExplanations(manager.getExplanations());
//            });
//        }
//
//        Platform.runLater(() -> controller.stopSolving());
    }

}
