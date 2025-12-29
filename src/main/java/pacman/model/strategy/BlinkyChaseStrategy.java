package pacman.model.strategy;

import pacman.model.entity.dynamic.physics.Direction;
import pacman.model.entity.dynamic.physics.Vector2D;

// Strategy for Blinky's chase behavior, where Blinky targets Pac-Man's exact position.
// This is part of the strategy pattern, allowing each ghost to have a unique chasing style.
public class BlinkyChaseStrategy implements GhostChaseStrategy {

    /**
     * Returns the target position for Blinky during chase mode.
     * Blinky directly targets Pac-Man's current position, aiming to catch up as quickly as possible.
     *
     * @param pacmanPosition the current position of Pac-Man
     * @param pacmanDirection the current direction of Pac-Man (not used in Blinky's strategy)
     * @param ghostPosition the current position of Blinky
     * @return Pac-Man's current position, which Blinky will use as the target
     */
    @Override
    public Vector2D getTargetPosition(Vector2D pacmanPosition, Direction pacmanDirection, Vector2D ghostPosition) {
        return pacmanPosition;
    }
}
