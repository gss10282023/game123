package pacman.model.maze;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pacman.ConfigurationParseException;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.physics.Vector2D;
import pacman.model.factories.RenderableFactory;
import pacman.model.factories.RenderableFactoryRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MazeCreatorTest {

    private static final class RecordingRegistry implements RenderableFactoryRegistry {
        private final List<Call> calls = new ArrayList<>();

        @Override
        public Renderable createRenderable(char renderableType, Vector2D position) {
            calls.add(new Call(renderableType, position));
            return null;
        }

        @Override
        public void registerFactory(char renderableType, RenderableFactory renderableFactory) {
            // Not needed for tests.
        }

        private List<Call> getCalls() {
            return calls;
        }

        private record Call(char type, Vector2D position) {
        }
    }

    @Test
    void parsesMapAndCallsRegistryWithTilePositions(@TempDir Path tempDir) throws IOException {
        Path mapFile = tempDir.resolve("map.txt");
        Files.writeString(mapFile, "1p\n7z\n", StandardCharsets.UTF_8);

        RecordingRegistry registry = new RecordingRegistry();
        MazeCreator creator = new MazeCreator(mapFile.toString(), registry);

        creator.createMaze();

        List<RecordingRegistry.Call> calls = registry.getCalls();
        assertEquals(4, calls.size());

        assertEquals('1', calls.get(0).type());
        assertEquals(0, calls.get(0).position().getX());
        assertEquals(0, calls.get(0).position().getY());

        assertEquals('p', calls.get(1).type());
        assertEquals(16, calls.get(1).position().getX());
        assertEquals(0, calls.get(1).position().getY());

        assertEquals('7', calls.get(2).type());
        assertEquals(0, calls.get(2).position().getX());
        assertEquals(16, calls.get(2).position().getY());

        assertEquals('z', calls.get(3).type());
        assertEquals(16, calls.get(3).position().getX());
        assertEquals(16, calls.get(3).position().getY());
    }

    @Test
    void throwsWhenMapMissing() {
        String missing = "__missing_map__" + java.util.UUID.randomUUID() + ".txt";

        RecordingRegistry registry = new RecordingRegistry();
        MazeCreator creator = new MazeCreator(missing, registry);

        ConfigurationParseException exception = assertThrows(ConfigurationParseException.class, creator::createMaze);
        assertTrue(exception.getMessage().contains("Maze map not found"));
    }
}

