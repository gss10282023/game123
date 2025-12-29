package pacman.model.entity.dynamic.ghost;

import javafx.scene.image.Image;
import pacman.model.decorator.Component;
import pacman.model.entity.dynamic.DynamicEntity;
import pacman.model.entity.dynamic.physics.Direction;
import pacman.model.entity.dynamic.physics.KinematicState;
import pacman.model.entity.dynamic.physics.Vector2D;
import pacman.model.entity.dynamic.player.observer.PlayerPositionObserver;
import pacman.model.state.GhostState;
import pacman.model.strategy.GhostChaseStrategy;

import java.util.Map;
import java.util.Set;

/**
 * Represents a Ghost entity in the Pac-Man game, implementing various
 * behaviors and strategies for different game modes.
 * Ghosts are dynamic entities with kinematic properties, and they observe
 * the player's position to adjust their behavior accordingly.
 */
public interface Ghost extends DynamicEntity, PlayerPositionObserver, Component {

    /**
     * Sets the speeds of the Ghost for each game mode.
     * This allows each Ghost mode to have different speed values.
     * @param speeds a map of speeds for each GhostMode
     */
    void setSpeeds(Map<GhostMode, Double> speeds);

    /**
     * Sets the current mode of the Ghost, which is used to calculate its target position.
     * The mode affects the Ghost's behavior, such as whether it chases or flees from Pac-Man.
     * @param ghostMode the mode to set for the Ghost
     */
    void setGhostMode(GhostMode ghostMode);

    /**
     * Calculates the target position for chasing Pac-Man based on his position and direction.
     * The target position may vary depending on the chase strategy.
     * @param pacmanPosition current position of Pac-Man
     * @param pacmanDirection current direction of Pac-Man
     * @return the calculated target position as a Vector2D
     */
    Vector2D chase(Vector2D pacmanPosition, Direction pacmanDirection);

    /**
     * Retrieves the character type of the Ghost (e.g., Blinky, Pinky, etc.).
     * @return character representing the Ghost type
     */
    char getGhostType();

    /**
     * Returns the current chase strategy used by the Ghost.
     * The chase strategy determines the Ghost's behavior in chase mode.
     * @return the Ghost's chase strategy
     */
    GhostChaseStrategy getChaseStrategy();

    /**
     * Sets the chase strategy for the Ghost, allowing dynamic changes
     * in behavior during the game.
     * @param chaseStrategy the chase strategy to assign
     */
    void setChaseStrategy(GhostChaseStrategy chaseStrategy);

    /**
     * Retrieves the Ghost's target position for chase mode.
     * @return the chase target position as a Vector2D
     */
    Vector2D getChaseTargetPosition();

    /**
     * Sets the Ghost's chase target position, used for pathfinding in chase mode.
     * @param chaseTargetPosition the target position as a Vector2D
     */
    void setChaseTargetPosition(Vector2D chaseTargetPosition);

    /**
     * Updates the Ghost's direction based on its current target position and chase strategy.
     */
    void updateDirection();

    // Frightened mode tick count management

    /**
     * Gets the current tick count for the frightened state.
     * @return the frightened tick count
     */
    int getFrightenedTickCount();

    /**
     * Resets the frightened tick count, typically used when the Ghost leaves the frightened state.
     */
    void resetFrightenedTickCount();

    /**
     * Increments the frightened tick count, tracking time spent in the frightened state.
     */
    void incrementFrightenedTickCount();

    // State management

    /**
     * Gets the current state of the Ghost (e.g., normal, frightened).
     * @return the current state of the Ghost
     */
    GhostState getCurrentState();

    /**
     * Retrieves the Ghost's normal state.
     * @return the Ghost's normal state
     */
    GhostState getNormalState();

    /**
     * Sets the current state of the Ghost.
     * @param currentState the new state for the Ghost
     */
    void setCurrentState(GhostState currentState);

    /**
     * Gets the Ghost's frightened state.
     * @return the Ghost's frightened state
     */
    GhostState getFrightenedState();

    // Image management for different states

    /**
     * Retrieves the image representing the Ghost in its normal state.
     * @return the image for the normal state
     */
    Image getNormalImage();

    /**
     * Retrieves the image representing the Ghost in its frightened state.
     * @return the image for the frightened state
     */
    Image getFrightenedImage();

    /**
     * Sets the current image of the Ghost, allowing for dynamic changes based on state.
     * @param currentImage the image to set
     */
    void setCurrentImage(Image currentImage);

    // Pause and direction management

    /**
     * Initiates a pause in the Ghost's movement, typically used for game events.
     */
    void startPause();

    /**
     * Retrieves the set of possible movement directions for the Ghost.
     * @return a set of possible directions
     */
    Set<Direction> getPossibleDirections();

    /**
     * Sets the current movement direction of the Ghost.
     * @param currentDirection the direction to set
     */
    void setCurrentDirection(Direction currentDirection);

    // Kinematic state retrieval

    /**
     * Gets the kinematic state of the Ghost, including position and velocity.
     * @return the kinematic state of the Ghost
     */
    KinematicState getKinematicState();

    // Ghost mode and speed management

    /**
     * Retrieves the current mode of the Ghost.
     * @return the current Ghost mode
     */
    GhostMode getGhostMode();

    /**
     * Gets the speeds of the Ghost for each mode.
     * @return a map of speeds for each GhostMode
     */
    Map<GhostMode, Double> getSpeeds();

    /**
     * Calculates the points awarded for eating this Ghost based on the number of consecutive ghosts eaten.
     * @param consecutiveGhostsEaten the number of ghosts eaten consecutively
     * @return the points awarded
     */
    int getPoints(int consecutiveGhostsEaten);

}
