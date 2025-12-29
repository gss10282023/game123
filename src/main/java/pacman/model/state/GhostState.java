package pacman.model.state;

import pacman.model.entity.Renderable;
import pacman.model.level.Level;

// Interface defining the behaviors for different states a Ghost can have in the game.
// Each GhostState implementation will represent a unique behavior, such as normal, frightened, or scatter mode.
public interface GhostState {

    /**
     * Updates the Ghost's direction based on its current state behavior.
     * For example, a frightened Ghost may move randomly, while a chasing Ghost may pursue Pac-Man.
     */
    void updateDirection();

    /**
     * Handles the behavior when the Ghost collides with Pac-Man.
     * The specific response will depend on the Ghost's current state,
     * such as being eaten in frightened mode or causing Pac-Man to lose a life in normal mode.
     *
     * @param level the current game level context
     * @param renderable the entity with which the Ghost collides, typically Pac-Man
     */
    void onCollisionWithPacman(Level level, Renderable renderable);

    /**
     * Transitions the Ghost to the next state in its state cycle.
     * This could involve switching from frightened mode back to normal or moving to scatter mode.
     */
    void switchToNextState();
}
