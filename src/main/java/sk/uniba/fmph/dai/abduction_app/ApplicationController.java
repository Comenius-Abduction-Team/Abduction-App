package sk.uniba.fmph.dai.abduction_app;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sk.uniba.fmph.dai.abduction_api.abducer.IAbducer;
import sk.uniba.fmph.dai.abduction_api.abducer.IExplanation;
import sk.uniba.fmph.dai.abduction_app.descriptors.ExtendedSolverDescriptor;
import sk.uniba.fmph.dai.abduction_app.descriptors.Solver;
import sk.uniba.fmph.dai.abduction_app.descriptors.SolverDescriptorMap;
import sk.uniba.fmph.dai.abduction_app.threading.ThreadManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** manages the application, both GUI and solving functionality**/
public class ApplicationController {

    ThreadManager threader;
    private final Solver DEFAULT_SOLVER = Solver.CATS;
    Solver currentSolver = DEFAULT_SOLVER;


    @FXML
    Button uploadBkN;

    @FXML
    Button uploadObservation;

    @FXML
    Button abduciblesButton;

    @FXML
    Button savelogs;

    @FXML
    Button explanations;

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
    Pane abduciblePane;
    @FXML
    Pane configuratorPane;

    @FXML
    private TitledPane settingsPane;
    @FXML
    private ScrollPane scrollPane;


// ------------------------------ HIGH-LEVEL METHODS RELATED TO RUNNING THE ABDUCTION ------------------------------
    void init(){

        Arrays.asList(Solver.values()).forEach(solver -> {
            solverChoice.getItems().add(solver.toString());
            SolverDescriptorMap.createDescriptor(solver);
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
        for (Solver solver : Solver.values()){
            if (solver.toString().equalsIgnoreCase(solverChoice.getSelectionModel().selectedItemProperty().get())) {
                currentSolver = solver;
                modifyInterfaceAccordingToCurrentSolver();
                return;
            }
        }
    }

    @FXML
    protected void uploadFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt", "*.owl"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if(selectedFile != null){
            try{
                String context = Files.readString(Path.of(selectedFile.getAbsolutePath()));
                bkText.setText(context);
            }
            catch (Exception e){
                bkText.setText("Error reading file: "+ e.getMessage());
            }
        }

    }

    @FXML
    protected void uploadObservation(){
//        System.out.println("Upload");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt", "*.owl"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if(selectedFile != null){
            try{
                String context = Files.readString(Path.of(selectedFile.getAbsolutePath()));
                observationText.setText(context);
            }
            catch (Exception e){
                observationText.setText("Error reading file: "+ e.getMessage());
            }
        }

    }

    @FXML
    protected void uploadAbducibles(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt", "*.owl"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if(selectedFile != null){
            try{
                String context = Files.readString(Path.of(selectedFile.getAbsolutePath()));
                abduciblesText.setText(context);
            }
            catch (Exception e){
                abduciblesText.setText("Error reading file: "+ e.getMessage());
            }
        }

    }



    @FXML
    protected void saveLogs(){
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Log files (*.log)", "*.log");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveTextToFile(logConsole.getText(), file);
        }
    }

    @FXML
    protected void saveExpln(){
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveTextToFile(explanationsConsole.getText(), file);
        }
    }

    private void saveTextToFile(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException e) {
            logConsole.setText(e.getMessage());
        }
    }

    @FXML
    protected void startSolving() {
        disableElement(startSolvingButton, false);
        allowElement(stopSolvingButton, false);

        clearConsole();

        threader = new ThreadManager(this);
        threader.runPrimaryThread();

        allowElement(progressIndicator, true);
    }

    @FXML
    public void stopSolving() {
        threader.stopThreads();
        disableElement(stopSolvingButton, false);
        allowElement(startSolvingButton, false);
        disableElement(progressIndicator, true);
    }

    public void processFinalExplanations(IAbducer abducer){

        addLogText(abducer.getFullLog());

        Set<IExplanation> explanations = abducer.getExplanations();
        if (explanations.isEmpty()){
            String message = abducer.getOutputMessage();
            if (message == null || message.isEmpty())
                printExplanationMessage("No explanations could be found!");
            else
                printExplanationMessage(abducer.getOutputMessage());
        }
        else {
            printExplanationMessage("FINAL EXPLANATIONS:");
            printExplanations(abducer.getExplanations());
        }

    }

// ------------------------------ MID-LEVEL GUI MODIFICATIONS CONTAINING SOME APPLICATION LOGIC ------------------------

    // ------------------ VISIBILITY OF ELEMENTS
    @FXML
    private void setDefaultConfiguration(){
        ExtendedSolverDescriptor descriptor = SolverDescriptorMap.getDescriptor(currentSolver);

        setCheckboxDefaultValue(descriptor.hasConceptSwitch(), descriptor.defaultConceptSwitch(), conceptCheckbox);
        setCheckboxDefaultValue(descriptor.hasComplexConceptSwitch(), descriptor.defaultComplexConceptSwitch(),
                complexCheckbox);
        setCheckboxDefaultValue(descriptor.hasComplementConceptSwitch(), descriptor.defaultComplementConceptSwitch(),
                complementCheckbox);
        setCheckboxDefaultValue(descriptor.hasRoleSwitch(), descriptor.defaultRoleSwitch(), roleCheckbox);
        setCheckboxDefaultValue(descriptor.hasLoopSwitch(), descriptor.defaultLoopSwitch(), loopCheckbox);

        disableImpossibleConceptConfiguration();
        disableImpossibleRoleConfiguration();

    }

    @FXML
    private void disableImpossibleConceptConfiguration(){
        ExtendedSolverDescriptor descriptor = SolverDescriptorMap.getDescriptor(currentSolver);
        if (! descriptor.hasConceptSwitch())
            return;
        if (conceptCheckbox.isSelected()){
            if (descriptor.hasComplexConceptSwitch())
                allowElement(complexCheckbox, false);
            if (descriptor.hasComplementConceptSwitch())
                allowElement(complementCheckbox, false);
        }
        else {
            if (descriptor.hasComplexConceptSwitch())
                disableElement(complexCheckbox, false);
            if (descriptor.hasComplementConceptSwitch())
                disableElement(complementCheckbox, false);
        }
    }

    @FXML
    private void disableImpossibleRoleConfiguration(){
        ExtendedSolverDescriptor descriptor = SolverDescriptorMap.getDescriptor(currentSolver);
        if (! descriptor.hasRoleSwitch())
            return;
        if (roleCheckbox.isSelected()){
            if (descriptor.hasLoopSwitch())
                allowElement(loopCheckbox, false);
        }
        else {
            if (descriptor.hasLoopSwitch())
                disableElement(loopCheckbox, false);
        }
    }

    void modifyInterfaceAccordingToCurrentSolver(){
        ExtendedSolverDescriptor descriptor = SolverDescriptorMap.getDescriptor(currentSolver);

        makeElementVisible(timeoutSetter, descriptor.hasTimeLimit());
        makeElementVisible(timeoutLabel, descriptor.hasTimeLimit());
        makeElementVisible(timeoutSecondsLabel, descriptor.hasTimeLimit());

        makeElementVisible(parameterSetter, descriptor.hasSpecificParameters());

        makeElementVisible(symbolRadio, descriptor.hasSymbolAbducibles());
        makeElementVisible(axiomRadio, descriptor.hasAxiomAbducibles());
        makeElementVisible(abduciblePane, descriptor.hasAbducibles());

        makeElementVisible(conceptCheckbox, descriptor.hasConceptSwitch());
        makeElementVisible(complexCheckbox, descriptor.hasComplexConceptSwitch());
        makeElementVisible(complementCheckbox, descriptor.hasComplementConceptSwitch());
        makeElementVisible(roleCheckbox, descriptor.hasRoleSwitch());
        makeElementVisible(loopCheckbox, descriptor.hasLoopSwitch());
        makeElementVisible(configuratorPane, descriptor.hasExplanationConfiguration());

        disableImpossibleConceptConfiguration();
        disableImpossibleRoleConfiguration();

    }

    @FXML
    void hideAbduciblesText(){
        disableElement(abduciblesText,true);
        disableElement(abduciblesButton, true);
    }

    @FXML
    void showAbduciblesText(){
        allowElement(abduciblesText,true);
        allowElement(abduciblesButton, true);
    }

    // ------------------ CONSOLE MODIFICATIONS

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
        logPane.getSelectionModel().select(1);
        if (exception.getMessage() != null)
            addLogText(exception.getMessage());
        else
            addLogText(exception.toString());
    }

    public void printExplanations(Set<IExplanation> explanations){
        explanations.forEach(e ->  explanationsConsole.appendText(e.toString() + "\n"));
    }

    public void printExplanationMessage(String message){
        addExplanationLogText(message + "\n");
    }

    private void addExplanationLogText(String text){
        explanationsConsole.appendText(text);
    }

    public void addLogText(String text){
        logConsole.appendText(text);
    }

    // ------------------ PROGRESS TRACKING

    public void updateProgressBar(double progress){
        if (progress == 0)
            progressBar.setProgress(0);
        else
            progressBar.setProgress(progress);
    }

    public void changeStatusMessage(String message){
        progressMessage.setText(message);
    }

// ------------------------------ LOW-LEVEL GENERIC GUI MODIFICATIONS ------------------------------

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

}