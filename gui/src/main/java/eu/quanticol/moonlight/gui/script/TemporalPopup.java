package eu.quanticol.moonlight.gui.script;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Class that implements the {@link MonitorPopup} interface and is responsible for the temporal monitor window
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class TemporalPopup implements MonitorPopup{

    @FXML
    AnchorPane root;
    @FXML
    Label fileCSV;
    @FXML
    MenuButton monitorMenu;

    /**
     * Opens the FileExplorer to choose a file .csv
     */
    @FXML
    private void openCSV() {
        fileCSV.setText("");
        open(root,fileCSV, "CSV Files", "*.csv");
    }

    /**
     * Sets the monitors
     *
     * @param monitors     monitors of script
     */
    public void addMonitors(String[] monitors) {
        this.setMonitors(monitors,monitorMenu);
         }

    @FXML
    public void execute(){
    }

    @FXML
    public void cancel(){
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }
}
