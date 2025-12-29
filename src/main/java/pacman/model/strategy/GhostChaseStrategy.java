package pacman.model.strategy;

import pacman.model.entity.dynamic.physics.Direction;
import pacman.model.entity.dynamic.physics.Vector2D;

// Interface defining the chase strategy for Ghosts in the Pac-Man game.
// Each implementation of this interface represents a unique chase behavior
// for a specific Ghost, allowing for different targeting logic.
public interface GhostChaseStrategy {

    /**
     * Determines the target position for the Ghost based on Pac-Man's position,
     * direction, and the Ghost's current position.
     *
     * @param pacmanPosition the current position of Pac-Man
     * @param pacmanDirection the current direction of Pac-Man
     * @param ghostPosition the current position of the Ghost
     * @return the calculated target position that the Ghost will attempt to reach
     */
    Vector2D getTargetPosition(Vector2D pacmanPosition, Direction pacmanDirection, Vector2D ghostPosition);
}
