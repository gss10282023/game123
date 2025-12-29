package pacman.model.factories;

import javafx.scene.image.Image;
import pacman.ConfigurationParseException;
import pacman.util.ResourceUtils;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.ghost.GhostImpl;
import pacman.model.entity.dynamic.ghost.GhostMode;
import pacman.model.entity.dynamic.physics.*;
import pacman.model.strategy.*;

import java.util.Arrays;
import java.util.List;

/**
 * Concrete renderable factory for Ghost objects
 */
public class GhostFactory implements RenderableFactory {

    private static final int RIGHT_X_POSITION_OF_MAP = 448;
    private static final int TOP_Y_POSITION_OF_MAP = 16 * 3;
    private static final int BOTTOM_Y_POSITION_OF_MAP = 16 * 34;

    private static final Image BLINKY_IMAGE = new Image(ResourceUtils.toExternalForm("maze/ghosts/blinky.png"));
    private static final Image INKY_IMAGE = new Image(ResourceUtils.toExternalForm("maze/ghosts/inky.png"));
    private static final Image CLYDE_IMAGE = new Image(ResourceUtils.toExternalForm("maze/ghosts/clyde.png"));
    private static final Image PINKY_IMAGE = new Image(ResourceUtils.toExternalForm("maze/ghosts/pinky.png"));
    private static final Image GHOST_IMAGE = BLINKY_IMAGE;
    List<Vector2D> targetCorners = Arrays.asList(
            new Vector2D(0, TOP_Y_POSITION_OF_MAP),
            new Vector2D(RIGHT_X_POSITION_OF_MAP, TOP_Y_POSITION_OF_MAP),
            new Vector2D(0, BOTTOM_Y_POSITION_OF_MAP),
            new Vector2D(RIGHT_X_POSITION_OF_MAP, BOTTOM_Y_POSITION_OF_MAP)
    );

    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    @Override
    public Renderable createRenderable(
            Vector2D position
    ) {
        try {
            position = position.add(new Vector2D(4, -4));

            BoundingBox boundingBox = new BoundingBoxImpl(
                    position,
                    GHOST_IMAGE.getHeight(),
                    GHOST_IMAGE.getWidth()
            );

            KinematicState kinematicState = new KinematicStateImpl.KinematicStateBuilder()
                    .setPosition(position)
                    .build();

            return new GhostImpl(
                    GHOST_IMAGE,
                    boundingBox,
                    kinematicState,
                    GhostMode.SCATTER,
                    targetCorners.get(getRandomNumber(0, targetCorners.size() - 1)));
        } catch (Exception e) {
            throw new ConfigurationParseException(
                    String.format("Invalid ghost configuration | %s ", e));
        }
    }





    /**
     * Creates a renderable Ghost object based on the provided position and ghost type.
     * It initializes the ghost's image, chase strategy, bounding box, and kinematic state.
     *
     * @param position  The initial position of the ghost.
     * @param ghostType The type of the ghost (Blinky, Pinky, Inky, Clyde).
     * @return A Renderable instance representing the ghost.
     * @throws ConfigurationParseException If an invalid ghost configuration is encountered.
     */
    public Renderable createRenderable(Vector2D position, char ghostType) {
        try {
            Image ghostImage = getGhostImage(ghostType);  // Retrieve the image for the ghost type.
            GhostChaseStrategy chaseStrategy = getChaseStrategy(ghostType);  // Retrieve the chase strategy.

            // Adjust the initial position.
            position = position.add(new Vector2D(4, -4));

            // Create a bounding box based on the position and ghost image dimensions.
            BoundingBox boundingBox = new BoundingBoxImpl(
                    position,
                    ghostImage.getHeight(),
                    ghostImage.getWidth()
            );

            // Build the kinematic state for the ghost with its position and speed.
            KinematicState kinematicState = new KinematicStateImpl.KinematicStateBuilder()
                    .setPosition(position)
                    .setSpeed(1)
                    .build();

            // Return the fully initialized Ghost object.
            return new GhostImpl(
                    ghostImage,
                    boundingBox,
                    kinematicState,
                    GhostMode.SCATTER,  // Initialize the ghost in SCATTER mode.
                    getScatterTarget(ghostType),  // Retrieve the scatter target for the ghost type.
                    ghostType,
                    chaseStrategy
            );
        } catch (Exception e) {
            // Throw a custom exception if there is an error in the configuration.
            throw new ConfigurationParseException(
                    String.format("Invalid ghost configuration | %s", e)
            );
        }
    }

    /**
     * Retrieves the image corresponding to the specified ghost type.
     *
     * @param ghostType The type of the ghost (Blinky, Pinky, Inky, Clyde).
     * @return The image associated with the ghost type.
     * @throws IllegalArgumentException If the ghost type is invalid.
     */
    private Image getGhostImage(char ghostType) {
        switch (ghostType) {
            case RenderableType.BLINKY:
                return BLINKY_IMAGE;
            case RenderableType.PINKY:
                return PINKY_IMAGE;
            case RenderableType.INKY:
                return INKY_IMAGE;
            case RenderableType.CLYDE:
                return CLYDE_IMAGE;
            default:
                throw new IllegalArgumentException("Invalid ghost type: " + ghostType);
        }
    }

    /**
     * Retrieves the scatter target location for the specified ghost type.
     * Each ghost type has a predefined corner of the grid as its scatter target.
     *
     * @param ghostType The type of the ghost (Blinky, Pinky, Inky, Clyde).
     * @return A Vector2D representing the scatter target location.
     * @throws IllegalArgumentException If the ghost type is invalid.
     */
    private Vector2D getScatterTarget(char ghostType) {
        switch (ghostType) {
            case RenderableType.BLINKY:
                return targetCorners.get(1);  // Blinky's scatter target.
            case RenderableType.PINKY:
                return targetCorners.get(0);  // Pinky's scatter target.
            case RenderableType.INKY:
                return targetCorners.get(3);  // Inky's scatter target.
            case RenderableType.CLYDE:
                return targetCorners.get(2);  // Clyde's scatter target.
            default:
                throw new IllegalArgumentException("Invalid ghost type: " + ghostType);
        }
    }

    /**
     * Retrieves the chase strategy for the specified ghost type.
     * Each ghost type has a unique chase behavior.
     *
     * @param ghostType The type of the ghost (Blinky, Pinky, Inky, Clyde).
     * @return A GhostChaseStrategy corresponding to the ghost type.
     * @throws IllegalArgumentException If the ghost type is invalid.
     */
    private GhostChaseStrategy getChaseStrategy(char ghostType) {
        switch (ghostType) {
            case RenderableType.BLINKY:
                return new BlinkyChaseStrategy();  // Blinky's chase strategy.
            case RenderableType.PINKY:
                return new PinkyChaseStrategy();   // Pinky's chase strategy.
            case RenderableType.INKY:
                return new InkyChaseStrategy();    // Inky's chase strategy.
            case RenderableType.CLYDE:
                return new ClydeChaseStrategy();   // Clyde's chase strategy.
            default:
                throw new IllegalArgumentException("Invalid ghost type: " + ghostType);
        }
    }



}


