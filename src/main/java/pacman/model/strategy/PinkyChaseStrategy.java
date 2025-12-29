package pacman.model.strategy;

import pacman.model.entity.dynamic.physics.Direction;
import pacman.model.entity.dynamic.physics.Vector2D;

public class PinkyChaseStrategy implements GhostChaseStrategy {

    // Four grid spaces ahead of Pac-Man
    private static final int OFFSET = 4;

    @Override
    public Vector2D getTargetPosition(Vector2D pacmanPosition, Direction pacmanDirection, Vector2D ghostPosition) {
        switch (pacmanDirection) {
            case UP:
                return new Vector2D(pacmanPosition.getX(), pacmanPosition.getY() - OFFSET);
            case DOWN:
                return new Vector2D(pacmanPosition.getX(), pacmanPosition.getY() + OFFSET);
            case LEFT:
                return new Vector2D(pacmanPosition.getX() - OFFSET, pacmanPosition.getY());
            case RIGHT:
                return new Vector2D(pacmanPosition.getX() + OFFSET, pacmanPosition.getY());
            default:
                return pacmanPosition;
        }
    }
}
