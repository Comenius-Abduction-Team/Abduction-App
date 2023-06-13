import abduction_api.manager.AbductionManager;
import abduction_api.manager.ExplanationWrapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import javafx.stage.Stage;

import java.util.*;

public class ApplicationController {

    ThreadManager threader;
    private AbductionRunner runner;
    private final Solver DEFAULT_SOLVER = Solver.MHS_MXP;
    Solver currentSolver = DEFAULT_SOLVER;

    final EnumMap<Solver, SolverDescriptor> descriptors = new EnumMap<>(Solver.class);

    @FXML
    private TabPane logPane;

    @FXML
    TextArea bkText;
    @FXML
    TextArea observationText;

    @FXML
    Spinner<Integer> timeoutSetter;
    @FXML
    Label timeoutLabel;
    @FXML
    Label timeoutSecondsLabel;

    @FXML
    private ChoiceBox<String> solverChoice;

    @FXML
    TextField parameterSetter;

    @FXML
    ToggleGroup abduciblesRadioGroup;
    @FXML
    RadioButton noAbduciblesRadio;
    @FXML
    RadioButton symbolRadio;
    @FXML
    RadioButton axiomRadio;
    @FXML
    TextArea abduciblesText;

    public Stage stage;

    @FXML
    private Button startSolvingButton;
    @FXML
    private Button stopSolvingButton;

    @FXML
    TextArea explanationsConsole;
    @FXML
    private TextArea logConsole;
    @FXML
    public TextField progressMessage;
    @FXML
    public ProgressBar progressBar;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    CheckBox conceptCheckbox;
    @FXML
    CheckBox complexCheckbox;
    @FXML
    CheckBox complementCheckbox;
    @FXML
    CheckBox roleCheckbox;
    @FXML
    CheckBox loopCheckbox;

    @FXML
    private Pane abduciblePane;
    @FXML
    private Pane configuratorPane;

    @FXML
    private TitledPane settingsPane;
    @FXML
    private ScrollPane scrollPane;

    void init(){
        Arrays.asList(Solver.values()).forEach(solver -> {
            descriptors.put(solver, new SolverDescriptor(solver));
            solverChoice.getItems().add(solver.toString());
        });
        solverChoice.setValue(DEFAULT_SOLVER.toString());

        timeoutSetter.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0)
        );

        modifyInterfaceAccordingToCurrentSolver();
        setDefaultConfiguration();

        solverChoice.getSelectionModel().selectedItemProperty().addListener(event -> changeSolver());

        explanationsConsole.setText("");
        logConsole.setText("");

    }

    void changeSolver(){
        Arrays.asList(Solver.values()).forEach(solver -> {
            if (solver.toString().equalsIgnoreCase(solverChoice.getSelectionModel().selectedItemProperty().get())) {
                currentSolver = solver;
                modifyInterfaceAccordingToCurrentSolver();
            }
        });
    }

    void modifyInterfaceAccordingToCurrentSolver(){
        SolverDescriptor descriptor = descriptors.get(currentSolver);

        makeElementVisible(timeoutSetter, descriptor.hasTimeLimit);
        makeElementVisible(timeoutLabel, descriptor.hasTimeLimit);
        makeElementVisible(timeoutSecondsLabel, descriptor.hasTimeLimit);

        makeElementVisible(parameterSetter, descriptor.hasSpecificParameters);

        makeElementVisible(symbolRadio, descriptor.hasSymbolAbducibles);
        makeElementVisible(axiomRadio, descriptor.hasAxiomAbducibles);
        makeElementVisible(abduciblePane, descriptor.hasAbducibles());

        makeElementVisible(conceptCheckbox, descriptor.hasConceptSwitch);
        makeElementVisible(complexCheckbox, descriptor.hasComplexConceptSwitch);
        makeElementVisible(complementCheckbox, descriptor.hasConceptComplementSwitch);
        makeElementVisible(roleCheckbox, descriptor.hasRoleSwitch);
        makeElementVisible(loopCheckbox, descriptor.hasLoopSwitch);
        makeElementVisible(configuratorPane, descriptor.hasExplanationConfiguration());

        disableImpossibleConceptConfiguration();
        disableImpossibleRoleConfiguration();

    }

    @FXML
    private void setDefaultConfiguration(){
        SolverDescriptor descriptor = descriptors.get(currentSolver);

        setCheckboxDefaultValue(descriptor.hasConceptSwitch, descriptor.defaultConceptSwitch, conceptCheckbox);
        setCheckboxDefaultValue(descriptor.hasComplexConceptSwitch, descriptor.defaultComplexConceptSwitch, complexCheckbox);
        setCheckboxDefaultValue(descriptor.hasConceptComplementSwitch, descriptor.defaultConceptComplementSwitch, complementCheckbox);
        setCheckboxDefaultValue(descriptor.hasRoleSwitch, descriptor.defaultRoleSwitch, roleCheckbox);
        setCheckboxDefaultValue(descriptor.hasLoopSwitch, descriptor.defaultLoopSwitch, loopCheckbox);

        disableImpossibleConceptConfiguration();
        disableImpossibleRoleConfiguration();

    }

    @FXML
    private void disableImpossibleConceptConfiguration(){
        SolverDescriptor descriptor = descriptors.get(currentSolver);
        if (! descriptor.hasConceptSwitch)
            return;
        if (conceptCheckbox.isSelected()){
            if (descriptor.hasComplexConceptSwitch)
                allowElement(complexCheckbox, false);
            if (descriptor.hasConceptComplementSwitch)
                allowElement(complementCheckbox, false);
        }
        else {
            if (descriptor.hasComplexConceptSwitch)
                disableElement(complexCheckbox, false);
            if (descriptor.hasConceptComplementSwitch)
                disableElement(complementCheckbox, false);
        }
    }

    @FXML
    private void disableImpossibleRoleConfiguration(){
        SolverDescriptor descriptor = descriptors.get(currentSolver);
        if (! descriptor.hasRoleSwitch)
            return;
        if (roleCheckbox.isSelected()){
            if (descriptor.hasLoopSwitch)
                allowElement(loopCheckbox, false);
        }
        else {
            if (descriptor.hasLoopSwitch)
                disableElement(loopCheckbox, false);
        }
    }

    private void setCheckboxDefaultValue(boolean hasSwitch, boolean defaultValue, CheckBox checkbox){
        if (hasSwitch)
            checkbox.setSelected(defaultValue);
    }

    private void disableElement(Node element, boolean affectVisibility){
        element.setDisable(true);
        element.setFocusTraversable(false);
        if (affectVisibility)
            element.setVisible(false);
    }

    private void allowElement(Node element, boolean affectVisibility){
        element.setDisable(false);
        element.setFocusTraversable(true);
        if (affectVisibility)
            element.setVisible(true);
    }

    private void makeElementVisible(Node element, boolean shouldBeVisible){
        if (shouldBeVisible)
            allowElement(element, true);
        else
            disableElement(element, true);
    }

    @FXML
    protected void startSolving() {
        disableElement(startSolvingButton, false);
        allowElement(stopSolvingButton, false);

        clearConsole();

        threader = new ThreadManager(this);
        threader.runAbductionThread();

        allowElement(progressIndicator, true);
    }

    public void clearConsole(){
        logConsole.setStyle("-fx-text-fill: black");
        logConsole.clear();
        explanationsConsole.clear();
        progressMessage.setText("");
        updateProgressBar(0);
    }

    public void showErrorMessage(Throwable exception){
        logConsole.setStyle("-fx-text-fill: #600000");
        addLogText("AN ERROR OCURRED!\n");
        //OutputStream out = new ByteArrayOutputStream();
        //PrintStream stream = new PrintStream(out);
        //exception.printStackTrace(stream);
        //exception.printStackTrace();
        logPane.getSelectionModel().select(1);
        if (exception.getMessage() != null)
            addLogText(exception.getMessage());
        else
            addLogText(exception.toString());
    }

    public void updateProgressBar(double progress){
        if (progress == 0)
            progressBar.setProgress(0);
        else
            progressBar.setProgress(progress);
    }

    public void changeStatusMessage(String message){
        progressMessage.setText(message);
    }

    public void printExplanations(Set<ExplanationWrapper> explanations){
        explanations.forEach(e ->  explanationsConsole.appendText(e.toString() + "\n"));
    }

    public void printExplanationMessage(String message){
        addExplanationLogText(message + "\n");
    }

    private void addExplanationLogText(String text){
        explanationsConsole.appendText(text);
    }

    @FXML
    protected void stopSolving() {
        threader.stopThreads();
        disableElement(stopSolvingButton, false);
        allowElement(startSolvingButton, false);
        disableElement(progressIndicator, true);
    }

    public void addLogText(String text){
        logConsole.appendText(text);
    }

    @FXML
    void hideAbduciblesText(){
        disableElement(abduciblesText,true);
    }

    @FXML
    void showAbduciblesText(){
        allowElement(abduciblesText,true);
    }

    void processFinalExplanations(AbductionManager manager){

        addLogText(manager.getFullLog());

        Set<ExplanationWrapper> explanations = manager.getExplanations();
        if (explanations.isEmpty()){
            String message = manager.getOutputMessage();
            if (message == null || message.isEmpty())
                printExplanationMessage("No explanations could be found!");
            else
                printExplanationMessage(manager.getOutputMessage());
        }
        else {
                printExplanationMessage("FINAL EXPLANATIONS:");
                printExplanations(manager.getExplanations());
        }

    }

}