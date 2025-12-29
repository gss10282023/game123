package pacman.model.state;

import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.ghost.GhostMode;
import pacman.model.level.Level;

// Represents the normal state of a Ghost, where it actively chases or interacts with Pac-Man in the usual mode.
public class NormalState implements GhostState {

    // The Ghost that is currently in the normal state
    Ghost ghost;

    // Constructor that initializes the Ghost in its normal state
    public NormalState(Ghost ghost) {
        this.ghost = ghost;
    }

    @Override
    public void updateDirection() {
        // In normal state, the Ghost updates its direction based on its current chase or scatter mode
        ghost.updateDirection();
    }

    @Override
    public void onCollisionWithPacman(Level level, Renderable renderable) {
        // Check if the Ghost collides with the player (Pac-Man)
        if (level.isPlayer(renderable)) {
            // Trigger the action to handle Pac-Man losing a life
            level.handleLoseLife();
        }
    }

    @Override
    public void switchToNextState() {
        // Change the Ghost's image to its frightened appearance
        ghost.setCurrentImage(ghost.getFrightenedImage());

        // Set the Ghost's mode to FRIGHTENED, indicating that it should flee from Pac-Man
        ghost.setGhostMode(GhostMode.FRIGHTENED);

        // Transition the Ghost's state to its frightened state
        ghost.setCurrentState(ghost.getFrightenedState());
    }
}
