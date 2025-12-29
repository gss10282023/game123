package pacman.model.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pacman.ConfigurationParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GameConfigurationReaderTest {

    @Test
    void readsConfigFromClasspath() {
        GameConfigurationReader reader = new GameConfigurationReader("/config.json");

        assertEquals("new-map.txt", reader.getMapFile());
        assertEquals(3, reader.getNumLives());
        assertNotNull(reader.getLevelConfigs());
        assertFalse(reader.getLevelConfigs().isEmpty());
    }

    @Test
    void readsConfigFromFileSystem(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("config.json");
        Files.writeString(
                configFile,
                """
                        {
                          "map": "map.txt",
                          "numLives": 1,
                          "levels": []
                        }
                        """,
                StandardCharsets.UTF_8
        );

        GameConfigurationReader reader = new GameConfigurationReader(configFile.toString());

        assertEquals("map.txt", reader.getMapFile());
        assertEquals(1, reader.getNumLives());
        assertNotNull(reader.getLevelConfigs());
        assertEquals(0, reader.getLevelConfigs().size());
    }

    @Test
    void throwsWhenConfigMissing() {
        String missing = "__missing_config__" + java.util.UUID.randomUUID() + ".json";

        ConfigurationParseException exception = assertThrows(
                ConfigurationParseException.class,
                () -> new GameConfigurationReader(missing)
        );

        assertTrue(exception.getMessage().contains("Config not found"));
    }
}

