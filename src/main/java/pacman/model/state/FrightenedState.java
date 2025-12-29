package pacman.model.state;

import pacman.model.decorator.Component;
import pacman.model.decorator.FrightenedGhostDecorator;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.ghost.GhostMode;
import pacman.model.entity.dynamic.physics.Direction;
import pacman.model.level.Level;
import pacman.model.maze.Maze;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Represents the frightened state of a Ghost, where it flees from Pac-Man and moves randomly
public class FrightenedState implements GhostState {

    // The Ghost that is currently in the frightened state
    Ghost ghost;

    // Constructor to initialize the Ghost in its frightened state
    public FrightenedState(Ghost ghost) {
        this.ghost = ghost;
    }

    @Override
    public void updateDirection() {
        // Retrieve possible directions the Ghost can move in from its current position
        Set<Direction> possibleDirections = ghost.getPossibleDirections();

        // If the Ghost is at an intersection, it chooses a random direction to flee
        if (Maze.isAtIntersection(possibleDirections)) {
            // Select a random direction from the available directions
            Direction newDirection = selectRandomDirection(ghost, possibleDirections);

            // Update the Ghost's current direction to the new randomly selected direction
            ghost.setCurrentDirection(newDirection);

            // Update the Ghost's kinematic state based on the new direction to move it accordingly
            switch (newDirection) {
                case LEFT -> ghost.getKinematicState().left();
                case RIGHT -> ghost.getKinematicState().right();
                case UP -> ghost.getKinematicState().up();
                case DOWN -> ghost.getKinematicState().down();
            }
        }
    }

    // Helper method to select a random direction, avoiding the opposite of the current direction
    private Direction selectRandomDirection(Ghost ghost, Set<Direction> possibleDirections) {
        List<Direction> validDirections = new ArrayList<>();
        Random random = new Random();

        // Only add directions that are not the opposite of the current direction, if it exists
        for (Direction direction : possibleDirections) {
            if (ghost.getDirection() == null || direction != ghost.getDirection().opposite()) {
                validDirections.add(direction);
            }
        }

        // If no valid directions are found, choose the opposite direction as a fallback
        if (validDirections.isEmpty()) {
            return ghost.getDirection().opposite();
        }

        // Randomly select a direction from the list of valid directions
        return validDirections.get(random.nextInt(validDirections.size()));
    }

    @Override
    public void onCollisionWithPacman(Level level, Renderable renderable) {
        // Check if the collision is with the player (Pac-Man)
        if (level.isPlayer(renderable)) {
            // Transition the Ghost to its next state upon collision with Pac-Man
            switchToNextState();

            // Reset the Ghost's frightened state properties
            ghost.reset();

            // Increment the consecutive ghost eat count for scoring
            level.setConsecutiveGhostsEaten(level.getConsecutiveGhostsEaten() + 1);

            // Wrap the Ghost in a FrightenedGhostDecorator to calculate score for eating it
            Component component = ghost;
            component = new FrightenedGhostDecorator(ghost);
            int score = component.getPoints(level.getConsecutiveGhostsEaten());

            // Notify observers of the score change resulting from eating the Ghost
            level.notifyObserversWithScoreChange(score);

            // Start a pause for the Ghost after it is eaten, providing a temporary delay
            ghost.startPause();
        }
    }

    @Override
    public void switchToNextState() {
        // Change the Ghost's mode to SCATTER, ending the frightened state
        ghost.setGhostMode(GhostMode.SCATTER);

        // Reset the Ghost's image to its normal appearance
        ghost.setCurrentImage(ghost.getNormalImage());

        // Reset the frightened tick count as the frightened state ends
        ghost.resetFrightenedTickCount();

        // Set the Ghost's state back to its normal state
        ghost.setCurrentState(ghost.getNormalState());
    }
}
