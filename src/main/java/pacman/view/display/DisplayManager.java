package pacman.view.display;

import javafx.scene.Node;
import javafx.scene.text.Font;
import pacman.model.engine.observer.GameState;
import pacman.model.engine.observer.GameStateObserver;
import pacman.model.level.observer.LevelStateObserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the display nodes for Pac-Man
 */
public class DisplayManager implements LevelStateObserver, GameStateObserver {

    private static final String FONT_RESOURCE_PATH = "/maze/PressStart2P-Regular.ttf";

    private final ScoreDisplay scoreDisplay;
    private final GameStateDisplay gameStatusDisplay;
    private final NumLivesDisplay numLivesDisplay;

    public DisplayManager() {
        Font font = loadUiFontOrFallback(16);

        this.scoreDisplay = new ScoreDisplay(font);
        this.gameStatusDisplay = new GameStateDisplay(font);
        this.numLivesDisplay = new NumLivesDisplay();
    }

    private static Font loadUiFontOrFallback(double size) {
        try (InputStream inputStream = DisplayManager.class.getResourceAsStream(FONT_RESOURCE_PATH)) {
            if (inputStream != null) {
                Font loaded = Font.loadFont(inputStream, size);
                if (loaded != null) {
                    return loaded;
                }
            }
        } catch (IOException ignored) {
            // Fall back to system font.
        }

        return new Font(size);
    }

    public List<Node> getNodes() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(scoreDisplay.getNode());
        nodes.add(gameStatusDisplay.getNode());
        nodes.add(numLivesDisplay.getNode());
        return nodes;
    }

    @Override
    public void updateNumLives(int numLives) {
        numLivesDisplay.update(numLives);
    }

    @Override
    public void updateScore(int scoreChange) {
        scoreDisplay.update(scoreChange);
    }

    @Override
    public void updateGameState(GameState gameState) {
        gameStatusDisplay.update(gameState);
    }
}
