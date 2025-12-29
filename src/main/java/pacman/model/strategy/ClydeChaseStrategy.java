package pacman.model.strategy;

import pacman.model.entity.dynamic.physics.Direction;
import pacman.model.entity.dynamic.physics.Vector2D;

public class ClydeChaseStrategy implements GhostChaseStrategy {

    // Extracted constants for clarity and maintainability
    private static final double CHASE_DISTANCE_THRESHOLD = 8.0;
    private static final Vector2D BOTTOM_LEFT_CORNER = new Vector2D(0, 544);

    @Override
    public Vector2D getTargetPosition(Vector2D pacmanPosition, Direction pacmanDirection, Vector2D ghostPosition) {
        double distance = Vector2D.calculateEuclideanDistance(pacmanPosition, ghostPosition);

        if (distance > CHASE_DISTANCE_THRESHOLD) {
            return pacmanPosition;  // Chase Pacman if distance is above the threshold
        } else {
            return BOTTOM_LEFT_CORNER;  // Scatter to the bottom-left corner if within the threshold
        }
    }
}
