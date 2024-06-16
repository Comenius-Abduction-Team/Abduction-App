package sk.uniba.fmph.dai.abduction_app;

import sk.uniba.fmph.dai.abduction_api.abducible.*;
import sk.uniba.fmph.dai.abduction_api.factory.IAbductionFactory;
import sk.uniba.fmph.dai.abduction_api.abducer.IAbducer;
import sk.uniba.fmph.dai.abduction_api.abducer.IThreadAbducer;
import sk.uniba.fmph.dai.abduction_api.monitor.AbductionMonitor;
import javafx.application.Platform;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.abduction_app.descriptors.ExtendedSolverDescriptor;
import sk.uniba.fmph.dai.abduction_app.descriptors.SolverDescriptorMap;
import sk.uniba.fmph.dai.abduction_app.io.InputParser;
import sk.uniba.fmph.dai.abduction_app.threading.ThreadManager;

import java.util.Set;
import java.util.stream.Collectors;

/** solves abduction directly using the API objects **/
public class AbductionRunner {

    public IAbducer abducer;

    ApplicationController controller;
    ThreadManager threader;

    public AbductionRunner(ApplicationController controller, ThreadManager threader){
        this.controller = controller;
        this.threader = threader;
    }

    public IAbducer runAbduction() throws OWLOntologyCreationException, InterruptedException {

        ExtendedSolverDescriptor descriptor = SolverDescriptorMap.getDescriptor(controller.currentSolver);
        IAbductionFactory factory = descriptor.getFactory();

        OWLOntology backgroundKnowledge = InputParser.parseOntology(controller.bkText.getText());
        if (backgroundKnowledge.getSignature().isEmpty())
            throw new RuntimeException("The background knowledge is empty!");

        OWLOntology observationOnt = InputParser.parseOntology(controller.observationText.getText());
        Set<OWLAxiom> axioms = observationOnt.getAxioms();
        axioms = axioms.stream().filter(axiom -> axiom.getAxiomType() != AxiomType.DECLARATION).collect(Collectors.toSet());
        if (axioms.isEmpty())
            throw new RuntimeException("The observation is empty!");

        abducer = factory.getAbducer(backgroundKnowledge, axioms);

        IAbducibles abducibles = setupAbducibles(descriptor, factory);

        if (abducibles != null)
            abducer.setAbducibles(abducibles);

        IExplanationConfigurator configurator = setupConfigurator(descriptor, factory);

        if (configurator != null)
            abducer.setExplanationConfigurator(configurator);

        setupTimeout(descriptor);

        setupSpecificParameters(descriptor);

        if (descriptor.hasThreadMode()) {
            runThreadMode();
        }
        else {
            runStandardMode();
        }

        return abducer;
    }

    private IAbducibles setupAbducibles(ExtendedSolverDescriptor descriptor, IAbductionFactory factory)
            throws OWLOntologyCreationException {

        IAbducibles abducibles = null;

        if (descriptor.hasSymbolAbducibles() && controller.symbolRadio.isSelected()){
            OWLOntology abducibleOnt = InputParser.parseOntology(controller.abduciblesText.getText());
            abducibles = factory.getSymbolAbducibles(abducibleOnt.getSignature());
        }
        else if (descriptor.hasAxiomAbducibles() && controller.axiomRadio.isSelected()){
            OWLOntology abducibleOnt = InputParser.parseOntology(controller.abduciblesText.getText());
            Set<OWLAxiom> axioms = abducibleOnt.getAxioms();
            axioms = axioms .stream()
                    .filter(axiom -> axiom.getAxiomType() != AxiomType.DECLARATION)
                    .collect(Collectors.toSet());
            abducibles = factory.getAxiomAbducibles(axioms);
        }
        return abducibles;
    }

    private IExplanationConfigurator setupConfigurator(ExtendedSolverDescriptor descriptor, IAbductionFactory factory) {

        IExplanationConfigurator configurator = factory.getExplanationConfigurator();

        if (configurator == null)
            return null;

        if (descriptor.hasExplanationConfiguration()){

            if (descriptor.hasConceptSwitch())
                ((IConceptConfigurator) configurator).allowConceptAssertions(controller.conceptCheckbox.isSelected());

            if (descriptor.hasComplexConceptSwitch())
                ((IComplexConceptConfigurator) configurator).allowComplexConcepts(controller.complexCheckbox.isSelected());
            if (descriptor.hasComplementConceptSwitch())
                ((IComplexConceptConfigurator) configurator).allowComplementConcepts(controller.complementCheckbox.isSelected());

            if (descriptor.hasRoleSwitch())
                ((IRoleConfigurator) configurator).allowRoleAssertions(controller.roleCheckbox.isSelected());
            if (descriptor.hasLoopSwitch())
                ((IRoleConfigurator) configurator).allowLoops(controller.loopCheckbox.isSelected());
        }

        return configurator;

    }

    private void setupTimeout(ExtendedSolverDescriptor descriptor) {
        int timeout;

        try{
            timeout = controller.timeoutSetter.getValue();
        } catch(Throwable e){
            timeout = 0;
        }
        if (descriptor.hasTimeLimit()) abducer.setTimeout(timeout);
    }

    private void setupSpecificParameters(ExtendedSolverDescriptor descriptor) {
        if (descriptor.hasSpecificParameters()) {
            abducer.resetSolverSpecificParameters();
            String parameters = controller.parameterSetter.getText();
            if(controller.strictRelevant.isSelected()){
                parameters += "-sR false";
                parameters += " ";
            }
            if(controller.depthSetter.getValue() != 0){
                parameters += "-d " + controller.depthSetter.getValue();
                parameters +=" ";
            }
//            System.out.println(controller.algoChoise.getValue());
            parameters += "-alg " + controller.algoChoise.getValue();
//            parameters += "-alg mhs-mxp";

            if (!parameters.isEmpty()) abducer.setSolverSpecificParameters(parameters);
        }
    }

    private void runStandardMode() {
        Platform.runLater(() -> {
            threader.updateAbductionTaskProgress(0, "Abduction initialized");
        });

        abducer.solveAbduction();

        Platform.runLater(() -> {
            threader.updateAbductionTaskProgress(100, "Abduction finished");
        });
    }

    private void runThreadMode() throws InterruptedException {

        IThreadAbducer threadManager = (IThreadAbducer) abducer;
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
                    int progress = monitor.getProgressPercentage().getValue();
                    String message = monitor.getStatusMessage();
                    threader.updateAbductionTaskProgress(progress, message);

                }
                if (monitor.getProgressPercentage().getValue() >= 100) {
                    //t.interrupt();
                    monitor.notify();
                    break;
                }
                monitor.notify();
            }

        }

        Platform.runLater(() -> controller.printExplanationMessage("\n"));
    }

}
