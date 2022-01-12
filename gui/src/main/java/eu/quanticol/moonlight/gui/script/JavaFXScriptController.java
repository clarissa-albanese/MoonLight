package eu.quanticol.moonlight.gui.script;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.gui.WindowController;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import eu.quanticol.moonlight.script.MoonLightParseError;
import eu.quanticol.moonlight.script.MoonLightScriptLoaderException;
import eu.quanticol.moonlight.script.ScriptLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller of JavaFX for the monitor/script.
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class JavaFXScriptController implements WindowController {

    @FXML
    TextArea console;

    @FXML
    TextArea textArea;

    @FXML
    AnchorPane root;

    @FXML
    Button runButton;

    private File scriptFile = null;
    private MoonLightScript script = null;

    /**
     * Sets title of window with the name of the current file
     *
     */
    private void setTitle(String title) {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setTitle(title);
    }

    /**
     * Checks if a script is correct or not
     */
    @FXML
    private void check() {
        try {
            script = ScriptLoader.loadFromCode(textArea.getText());
            runButton.setDisable(script == null);
            console.setText("BUILD SUCCESSFUL");
        } catch (MoonLightScriptLoaderException | IOException e) {
            ArrayList<String> errors = Arrays.stream(e.getMessage().split("\n")).collect(Collectors.toCollection(ArrayList::new));
            console.clear();
            for (String err : errors) {
                console.appendText(err + "\n");
            }
        }
    }

    /**
     * Initializes the theme of the window
     */
    @Override
    public void initializeThemes() {
        if (root.getStylesheets() != null) {
            if (!root.getStylesheets().isEmpty())
                root.getStylesheets().clear();
            root.getStylesheets().add(JsonThemeLoader.getInstance().getGeneralTheme());
        }
    }

    /**
     * Opens a window for the monitoring
     */
    @FXML
    private void run() {
        if (script.isTemporal() || !script.isSpatialTemporal())
            temporalPopup();
        else if (script.isSpatialTemporal() || !script.isTemporal())
            spatialTemporalPopup();
    }

    /**
     * Opens the window for the spatial temporal monitoring
     */
    private void spatialTemporalPopup() {
        FXMLLoader fxmlLoader = openMonitorWindow("fxml/spatioTemporalMonitorComponent.fxml");
        if(fxmlLoader != null) {
            MonitorPopup controller = fxmlLoader.getController();
            controller.addMonitors(script.getMonitors());
        }
    }

    /**
     * Opens the window for the temporal monitoring
     */
    private void temporalPopup() {
        FXMLLoader fxmlLoader = openMonitorWindow("fxml/temporalMonitorComponent.fxml");
        if(fxmlLoader != null) {
            MonitorPopup controller = fxmlLoader.getController();
            controller.addMonitors(script.getMonitors());
        }
    }

    /**
     * Saves the changes of the file or saves a new file with the written script
     */
    @FXML
    private void save() {
        DialogBuilder d = new DialogBuilder(JsonThemeLoader.getInstance().getGeneralTheme());
        try {
            if(scriptFile == null) {
                saveNewFile();
            } else saveSameFile();
           d.info("Successful saving.");
        } catch (IOException e) {
            d.error("Failed saving file.");
        }
    }

    /**
     * Saves changes of the file
     *
     */
    private void saveSameFile() throws IOException {
        Writer writer = new FileWriter(scriptFile);
        writer.write(textArea.getText());
        writer.close();
    }

    /**
     * Saves script in a new file
     *
     */
    private void saveNewFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MoonLight Script file", "*.ml");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if(file != null) {
            Writer writer = new FileWriter(file);
            writer.write(textArea.getText());
            writer.close();
            scriptFile = file;
            setTitle(scriptFile.getName());
        }
    }

    /**
     * Opens a monitoring window
     *
     */
    private FXMLLoader openMonitorWindow(String name) {
        try {
            FXMLLoader fxmlLoader;
            fxmlLoader = new FXMLLoader(ClassLoader.getSystemClassLoader().getResource(name));
            Parent newRoot = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("MoonLight");
            stage.initStyle(StageStyle.DECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(newRoot));
            Image icon = new Image(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/ML.png")).toString());
            stage.getIcons().add(icon);
            stage.show();
            return fxmlLoader;
        } catch (IOException e) {
            DialogBuilder d = new DialogBuilder(JsonThemeLoader.getInstance().getGeneralTheme());
            e.printStackTrace();
            d.warning("Failed opening monitor");
            return null;
        }
    }

    /**
     * Opens the file explorer to choose a file containing the script
     */
    @FXML
    private void open() {
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) root.getScene().getWindow();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MoonLight Script file", "*.ml");
        fileChooser.getExtensionFilters().add(extFilter);
        scriptFile = fileChooser.showOpenDialog(stage);
        if(scriptFile != null) {
            textArea.clear();
            fileToText();
            setTitle(scriptFile.getName());
            runButton.setDisable(true);
            console.setText("");
        }
    }

    /**
     * Inserts the script from the file to the text area
     */
    private void fileToText() {
        try (Scanner input = new Scanner(scriptFile)) {
            while (input.hasNextLine()) {
                textArea.appendText(input.nextLine() + "\n");
            }
        } catch (FileNotFoundException ex) {
            console.setText(ex.getMessage());
        }
    }
}
