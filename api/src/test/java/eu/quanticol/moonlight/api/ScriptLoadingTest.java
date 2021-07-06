package eu.quanticol.moonlight.api;

import eu.quanticol.moonlight.MoonLightScript;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScriptLoadingTest {

    @Test
    public void testLoadScript() throws IOException {
        URL url = ScriptLoadingTest.class.getClassLoader().getResource("multipleMonitors.mls");
        MoonLightScript script = Matlab.loadFromFile(url.getFile());
        assertTrue(script.isTemporal());
    }

}


