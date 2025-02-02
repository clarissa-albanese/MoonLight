package eu.quanticol.moonlight.gui.io;

import java.io.IOException;

/**
 * Interface that defines how to generate themes for the application and load them to/from a .json file
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public interface ThemeLoader {

    String getGeneralTheme();

    void setGeneralTheme(String generalTheme);

    String getGraphTheme();

    void setGraphTheme(String graphTheme);

    void saveToJson() throws IOException;
}

