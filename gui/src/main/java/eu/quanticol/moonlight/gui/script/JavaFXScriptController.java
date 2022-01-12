package eu.quanticol.moonlight.gui.script;

import eu.quanticol.moonlight.MoonLightScript;
import eu.quanticol.moonlight.gui.WindowController;
import eu.quanticol.moonlight.gui.io.JsonThemeLoader;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import eu.quanticol.moonlight.script.MoonLightScriptLoaderException;
import eu.quanticol.moonlight.script.ScriptLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
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


    private void setTitle(String title) {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setTitle(title);
    }

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
        FXMLLoader fxmlLoader = openMonitorWindow("fxml/spatioTemporalMonitorComponent.fxml");
        assert fxmlLoader != null;
        SpatioTemporalMonitor controller = fxmlLoader.getController();
        controller.setMonitors(script.getMonitors());
    }

    private void temporalPopup() {
        FXMLLoader fxmlLoader = openMonitorWindow("fxml/temporalMonitorComponent.fxml");
        assert fxmlLoader != null;
        TemporalMonitor controller = fxmlLoader.getController();
        controller.setMonitors(script.getMonitors());
    }

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

    private void saveSameFile() throws IOException {
        Writer writer = new FileWriter(scriptFile);
        writer.write(textArea.getText());
        writer.close();
    }

    private void saveNewFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Script file", "*.txt");
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

    private FXMLLoader openMonitorWindow(String name) {
        try {
            FXMLLoader fxmlLoader;
            fxmlLoader = new FXMLLoader(ClassLoader.getSystemClassLoader().getResource(name));
            Parent newRoot = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("MoonLight");
            stage.initStyle(StageStyle.DECORATED);
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

    @FXML
    private void open() {
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) root.getScene().getWindow();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Script file", "*.txt");
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
