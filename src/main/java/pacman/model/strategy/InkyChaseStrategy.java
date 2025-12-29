package pacman.model.strategy;

import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.physics.Direction;
import pacman.model.entity.dynamic.physics.Vector2D;

public class InkyChaseStrategy implements GhostChaseStrategy {

    // Constants for improved readability and maintainability
    private static final int LOOKAHEAD_SPACES = 2;
    private static final int SCALING_FACTOR = 2;

    private Ghost blinky;

    @Override
    public Vector2D getTargetPosition(Vector2D pacmanPosition, Direction pacmanDirection, Vector2D ghostPosition) {
        // Calculate the position two spaces ahead based on Pacman's direction
        Vector2D twoSpacesAhead;
        switch (pacmanDirection) {
            case UP:
                twoSpacesAhead = new Vector2D(pacmanPosition.getX(), pacmanPosition.getY() - LOOKAHEAD_SPACES);
                break;
            case DOWN:
                twoSpacesAhead = new Vector2D(pacmanPosition.getX(), pacmanPosition.getY() + LOOKAHEAD_SPACES);
                break;
            case LEFT:
                twoSpacesAhead = new Vector2D(pacmanPosition.getX() - LOOKAHEAD_SPACES, pacmanPosition.getY());
                break;
            case RIGHT:
                twoSpacesAhead = new Vector2D(pacmanPosition.getX() + LOOKAHEAD_SPACES, pacmanPosition.getY());
                break;
            default:
                twoSpacesAhead = pacmanPosition;
        }

        Vector2D blinkyPosition = blinky.getPosition();

        // Calculate the vector difference between the lookahead position and Blinky's position
        double xDifference = twoSpacesAhead.getX() - blinkyPosition.getX();
        double yDifference = twoSpacesAhead.getY() - blinkyPosition.getY();
        Vector2D vector = new Vector2D(xDifference, yDifference);

        // Scale the vector by the scaling factor
        Vector2D scaledVector = new Vector2D(vector.getX() * SCALING_FACTOR, vector.getY() * SCALING_FACTOR);

        // Return the final target position
        return blinkyPosition.add(scaledVector);
    }

    public void setBlinky(Ghost ghost) {
        this.blinky = ghost;
    }


}
