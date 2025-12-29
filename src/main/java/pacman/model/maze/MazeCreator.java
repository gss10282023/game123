package pacman.model.maze;

import pacman.ConfigurationParseException;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.physics.Vector2D;
import pacman.model.factories.RenderableFactoryRegistry;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Responsible for creating renderables and storing it in the Maze
 */
public class MazeCreator {

    public static final int RESIZING_FACTOR = 16;
    private final String fileName;
    private final RenderableFactoryRegistry renderableFactoryRegistry;

    public MazeCreator(String fileName,
                       RenderableFactoryRegistry renderableFactoryRegistry) {
        this.fileName = fileName;
        this.renderableFactoryRegistry = renderableFactoryRegistry;
    }

    public Maze createMaze() {
        Maze maze = new Maze();

        try (InputStream inputStream = openMazeStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int y = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                char[] row = line.toCharArray();

                for (int x = 0; x < row.length; x++) {
                    Vector2D position = new Vector2D(x * RESIZING_FACTOR, y * RESIZING_FACTOR);

                    char renderableType = row[x];
                    Renderable renderable = renderableFactoryRegistry.createRenderable(
                            renderableType, position
                    );

                    maze.addRenderable(renderable, renderableType, x, y);
                }

                y += 1;
            }
        } catch (IOException e) {
            throw new ConfigurationParseException("Failed to read maze map: " + fileName, e);
        }

        return maze;
    }

    private InputStream openMazeStream() throws IOException {
        Path filePath = Paths.get(fileName);
        if (Files.exists(filePath)) {
            return Files.newInputStream(filePath);
        }

        String resourcePath = normalizeClasspathPath(fileName);
        InputStream inputStream = MazeCreator.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new ConfigurationParseException("Maze map not found on filesystem or classpath: " + fileName);
        }

        return inputStream;
    }

    private static String normalizeClasspathPath(String path) {
        if (path.startsWith("/")) {
            return path;
        }
        return "/" + path;
    }
}
