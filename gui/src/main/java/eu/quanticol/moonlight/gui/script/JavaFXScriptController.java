package eu.quanticol.moonlight.gui.script;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.gui.WindowController;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import eu.quanticol.moonlight.script.MoonLightScriptLoaderException;
import eu.quanticol.moonlight.script.ScriptLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class JavaFXScriptController implements WindowController {

    @FXML
    Label console;

    @FXML
    TextArea textArea;

    @FXML
    AnchorPane root;

    @FXML
    Button runButton;

    private File scriptFile = null;
    private MoonLightScript script = null;


    @FXML
    private void check() {
        try {
            script = ScriptLoader.loadFromCode(textArea.getText());
            runButton.setDisable(script == null);
            console.setText("BUILD SUCCESSFUL");
        } catch (MoonLightScriptLoaderException | IOException e) {
            console.setText(e.getMessage());
        }
    }

    @Override
    public void initializeThemes() {
        if (root.getStylesheets() != null) {
            if (!root.getStylesheets().isEmpty())
                root.getStylesheets().clear();
            root.getStylesheets().add(JsonThemeLoader.getInstance().getGeneralTheme());
        }
    }

    @FXML
    private void run() {
        if (script.isTemporal() || !script.isSpatialTemporal())
            temporalPopup();
        else if (script.isSpatialTemporal() || !script.isTemporal())
            spatialTemporalPopup();
    }

    private void spatialTemporalPopup() {
    }

    private void temporalPopup() {
    }

    @FXML
    private void save() {
    }

    @FXML
    private void open() {
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) root.getScene().getWindow();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Script file", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        scriptFile = fileChooser.showOpenDialog(stage);
        fileToText();
    }

    private void fileToText() {
        try {
            Scanner s = new Scanner(scriptFile).useDelimiter("\\s+");
            while (s.hasNext()) {
                if (s.hasNextInt()) { // check if next token is an int
                    textArea.appendText(s.nextInt() + " "); // display the found integer
                } else {
                    textArea.appendText(s.next() + " "); // else read the next token
                }
            }
        } catch (FileNotFoundException ex) {
            console.setText(ex.getMessage());
        }
    }
}
