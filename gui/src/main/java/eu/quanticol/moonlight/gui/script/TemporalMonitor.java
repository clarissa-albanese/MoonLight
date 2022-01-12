package eu.quanticol.moonlight.gui.script;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class TemporalMonitor {

    @FXML
    AnchorPane root;
    @FXML
    Label fileCSV;
    @FXML
    MenuButton monitorMenu;

    private final ArrayList<String[]> monitorsArray = new ArrayList<>();

    @FXML
    private void openCSV() {
        fileCSV.setText("");
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) root.getScene().getWindow();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV Files", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            fileCSV.setVisible(true);
            fileCSV.setText(file.getPath());
        }
    }

    public void setMonitors(String[] monitors) {
        monitorsArray.add(monitors);
        ArrayList<MenuItem> menuItems = new ArrayList<>();
        monitorsArray.forEach(a -> {
            MenuItem menuItem = new MenuItem();
            String text = Arrays.toString(a).replace("[", "").replace("]", "");
            menuItem.setText(text);
            menuItems.add(menuItem);
        });
        this.monitorMenu.getItems().addAll(menuItems);
        monitorMenu.getItems().forEach(menuItem -> menuItem.setOnAction(event -> monitorMenu.setText(menuItem.getText())));
    }

    @FXML
    private void execute(){
    }

    @FXML
    private void cancel(){
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }
}
