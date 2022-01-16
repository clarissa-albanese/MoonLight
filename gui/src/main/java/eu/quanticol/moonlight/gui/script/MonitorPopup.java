package eu.quanticol.moonlight.gui.script;

import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

/**
 * Interface that defines a monitor popups
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public interface MonitorPopup {

    /**
     * Opens a fileChooser
     *
     * @param root           anchorPane of window
     * @param label          label of filePath
     * @param description    description of files
     * @param extensions     extensions of files
     */
    default void open(AnchorPane root, Label label, String description, String extensions){
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) root.getScene().getWindow();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(description, extensions);
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            label.setVisible(true);
            label.setText(file.getPath());
        }
    }

    void execute();

    void cancel();

    void addMonitors(String[] monitors);

    /**
     * Sets the monitors
     *
     * @param monitors         monitors to add
     * @param monitorMenu      menuButton of monitors
     */
    default void setMonitors(String[] monitors, MenuButton monitorMenu){
        ArrayList<MenuItem> menuItems = new ArrayList<>();
        for (String monitor : monitors) {
            MenuItem menuItem = new MenuItem();
            menuItem.setText(monitor);
            menuItems.add(menuItem);
        }
        monitorMenu.getItems().addAll(menuItems);
        monitorMenu.getItems().forEach(menuItem -> menuItem.setOnAction(event -> monitorMenu.setText(menuItem.getText())));
    }
}
