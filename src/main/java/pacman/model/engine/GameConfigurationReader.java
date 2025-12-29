package pacman.model.engine;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pacman.ConfigurationParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class to read Game Configuration from JSONObject
 */
public class GameConfigurationReader {

    private final JSONObject gameConfig;

    public GameConfigurationReader(String configPath) {
        if (configPath == null || configPath.isBlank()) {
            throw new ConfigurationParseException("Config path is empty.");
        }

        JSONObject parsed;
        Path filePath = Paths.get(configPath);
        if (Files.exists(filePath)) {
            try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                parsed = parseConfig(reader, filePath.toString());
            } catch (IOException e) {
                throw new ConfigurationParseException("Failed to read config file: " + filePath, e);
            }
        } else {
            String resourcePath = normalizeClasspathPath(configPath);
            InputStream inputStream = GameConfigurationReader.class.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new ConfigurationParseException("Config not found on filesystem or classpath: " + configPath);
            }
            try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                parsed = parseConfig(reader, resourcePath);
            } catch (IOException e) {
                throw new ConfigurationParseException("Failed to read config resource: " + resourcePath, e);
            }
        }

        this.gameConfig = parsed;
    }

    private static JSONObject parseConfig(Reader reader, String sourceDescription) {
        JSONParser parser = new JSONParser();

        try {
            Object parsed = parser.parse(reader);
            if (!(parsed instanceof JSONObject object)) {
                throw new ConfigurationParseException("Config root must be a JSON object: " + sourceDescription);
            }
            return object;
        } catch (IOException e) {
            throw new ConfigurationParseException("Failed to read config: " + sourceDescription, e);
        } catch (ParseException e) {
            throw new ConfigurationParseException("Failed to parse config: " + sourceDescription, e);
        }
    }

    private static String normalizeClasspathPath(String path) {
        if (path.startsWith("/")) {
            return path;
        }
        return "/" + path;
    }

    /**
     * Gets the path of map file
     *
     * @return path of map file
     */
    public String getMapFile() {
        return (String) gameConfig.get("map");
    }

    /**
     * Gets the number of lives of player
     *
     * @return number of lives of player
     */
    public int getNumLives() {
        return ((Number) gameConfig.get("numLives")).intValue();
    }

    /**
     * Gets JSONArray of level configurations
     *
     * @return JSONArray of level configurations
     */
    public JSONArray getLevelConfigs() {
        return (JSONArray) gameConfig.get("levels");
    }
}
