package pacman.model.entity.dynamic.ghost;

import javafx.scene.image.Image;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.physics.*;
import pacman.model.level.Level;
import pacman.model.state.FrightenedState;
import pacman.model.state.GhostState;
import pacman.model.state.NormalState;
import pacman.model.maze.Maze;
import pacman.model.strategy.GhostChaseStrategy;

import java.util.*;

/**
 * Concrete implementation of Ghost entity in Pac-Man Game
 */
public class GhostImpl implements Ghost {

    private static final int minimumDirectionCount = 8;
    private final Layer layer = Layer.FOREGROUND;
    private final Image image;


    //Set up different images for different types of ghosts
    private final Image frightenedImage = new Image("maze/ghosts/frightened.png");
    private Image currentImage;


    private final BoundingBox boundingBox;
    private final Vector2D startingPosition;
    private final Vector2D targetCorner;
    private KinematicState kinematicState;
    private GhostMode ghostMode;
    private Vector2D targetLocation;
    private Vector2D playerPosition;


    private Direction currentDirection;
    private Set<Direction> possibleDirections;
    private Map<GhostMode, Double> speeds;
    private int currentDirectionCount = 0;


    // Character representing the type of the Ghost (e.g., 'B' for Blinky.)
    private final char ghostType;

    // The strategy used by the Ghost in chase mode, determining its behavior when pursuing Pac-Man
    private GhostChaseStrategy chaseStrategy;

    // The target position for the Ghost during chase mode, calculated based on the chase strategy
    private Vector2D chaseTargetPosition;

    // Current state of the Ghost, which could vary between normal, frightened state.
    GhostState currentState;

    // The Ghost's default normal state, where it actively pursues or avoids Pac-Man depending on the mode
    GhostState normalState;

    // The frightened state of the Ghost, triggered when Pac-Man consumes a power pellet
    GhostState frightenedState;

    // Flag indicating whether the Ghost is currently paused
    private boolean isPaused;

    // Counter for tracking the duration of a pause, incremented each game tick while the Ghost is paused
    private int pauseTickCount;

    // Constant representing the duration of a pause, defining how many ticks the pause will last
    private static final int PAUSE_DURATION = 30;

    // Counter tracking the time spent in the frightened state, allowing behavior to change based on duration
    private int frightenedTickCount = 0;


    public GhostImpl(Image image, BoundingBox boundingBox, KinematicState kinematicState, GhostMode ghostMode, Vector2D targetCorner) {
        this.image = image;
        this.boundingBox = boundingBox;
        this.kinematicState = kinematicState;
        this.startingPosition = kinematicState.getPosition();
        this.ghostMode = ghostMode;
        this.possibleDirections = new HashSet<>();
        this.targetCorner = targetCorner;
        this.targetLocation = getTargetLocation();
        this.currentDirection = null;
        this.ghostType = 'g';
    }


    public GhostImpl(Image image, BoundingBox boundingBox, KinematicState kinematicState, GhostMode ghostMode, Vector2D targetCorner, char ghostType, GhostChaseStrategy chaseStrategy) {
        this.image = image;
        this.boundingBox = boundingBox;
        this.kinematicState = kinematicState;
        this.startingPosition = kinematicState.getPosition();
        this.ghostMode = ghostMode;
        this.possibleDirections = new HashSet<>();
        this.targetCorner = targetCorner;

        this.targetLocation = getTargetLocation();
        this.currentDirection = null;


        this.ghostType = ghostType;
        this.chaseStrategy = chaseStrategy;


        //initialize different state and set the current image to normal ghost image
        this.normalState = new NormalState(this);
        this.currentState = normalState;
        this.frightenedState = new FrightenedState(this);
        this.currentImage = image;


    }

    @Override
    public void setSpeeds(Map<GhostMode, Double> speeds) {
        this.speeds = speeds;
    }

    @Override
    public Image getImage() {
        return currentImage;
    }

    @Override
    public void update() {

        // Check if the Ghost is currently paused
        if (isPaused) {
            // Increment the pause tick counter to track the duration of the pause
            pauseTickCount++;

            // If the pause duration has been reached, unpause the Ghost
            if (pauseTickCount >= PAUSE_DURATION) {
                isPaused = false; // Resume the Ghost's movement
            }
            return;
        }


        currentState.updateDirection();
        this.kinematicState.update();
        this.boundingBox.setTopLeft(this.kinematicState.getPosition());
    }

    public void updateDirection() {
        // Ghosts update their target location when they reach an intersection
        if (Maze.isAtIntersection(this.possibleDirections)) {
            this.targetLocation = getTargetLocation();
        }

        Direction newDirection = selectDirection(possibleDirections);

        // Ghosts have to continue in a direction for a minimum time before changing direction
        if (this.currentDirection != newDirection) {
            this.currentDirectionCount = 0;
        }
        this.currentDirection = newDirection;

        switch (currentDirection) {
            case LEFT -> this.kinematicState.left();
            case RIGHT -> this.kinematicState.right();
            case UP -> this.kinematicState.up();
            case DOWN -> this.kinematicState.down();
        }
    }

    private Vector2D getTargetLocation() {
        return switch (this.ghostMode) {
            case CHASE -> this.chaseTargetPosition;
            case SCATTER -> this.targetCorner;
            case FRIGHTENED -> null;
        };
    }

    private Direction selectDirection(Set<Direction> possibleDirections) {
        if (possibleDirections.isEmpty()) {
            return currentDirection;
        }

        // ghosts have to continue in a direction for a minimum time before changing direction
        if (currentDirection != null && currentDirectionCount < minimumDirectionCount) {
            currentDirectionCount++;
            return currentDirection;
        }

        Map<Direction, Double> distances = new HashMap<>();

        for (Direction direction : possibleDirections) {
            // ghosts never choose to reverse travel
            if (currentDirection == null || direction != currentDirection.opposite()) {
                distances.put(direction, Vector2D.calculateEuclideanDistance(this.kinematicState.getPotentialPosition(direction), this.targetLocation));
            }
        }

        // only go the opposite way if trapped
        if (distances.isEmpty()) {
            return currentDirection.opposite();
        }

        // select the direction that will reach the target location fastest
        return Collections.min(distances.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    @Override
    public void setGhostMode(GhostMode ghostMode) {
        this.ghostMode = ghostMode;
        this.kinematicState.setSpeed(speeds.get(ghostMode));
        // ensure direction is switched
        this.currentDirectionCount = minimumDirectionCount;
    }

    @Override
    public boolean collidesWith(Renderable renderable) {
        return boundingBox.collidesWith(kinematicState.getSpeed(), kinematicState.getDirection(), renderable.getBoundingBox());
    }

    @Override
    public void collideWith(Level level, Renderable renderable) {
        // Delegates the collision handling to the current state of the Ghost.
        // This allows the behavior upon collision with Pac-Man to vary based on the Ghost's state
        // (e.g., frightened, normal), promoting flexibility in handling interactions.
        currentState.onCollisionWithPacman(level, renderable);
    }


    @Override
    public void update(Vector2D playerPosition) {
        this.playerPosition = playerPosition;
    }

    @Override
    public Vector2D getPositionBeforeLastUpdate() {
        return this.kinematicState.getPreviousPosition();
    }

    @Override
    public double getHeight() {
        return this.boundingBox.getHeight();
    }

    @Override
    public double getWidth() {
        return this.boundingBox.getWidth();
    }

    @Override
    public Vector2D getPosition() {
        return this.kinematicState.getPosition();
    }

    @Override
    public void setPosition(Vector2D position) {
        this.kinematicState.setPosition(position);
    }

    @Override
    public Layer getLayer() {
        return this.layer;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public void reset() {

        // Set the current image of the Ghost to its normal appearance
        setCurrentImage(getNormalImage());

       // Reset the frightened tick counter, as the Ghost is returning to its normal state
        resetFrightenedTickCount();

       // Update the Ghost's state to its normal state, resuming standard behavior
        currentState = normalState;

      // Unpause the Ghost, allowing it to resume movement and interactions
        isPaused = false;

       // Reset the pause tick counter to ensure no residual pause duration carries over
        pauseTickCount = 0;


        // return ghost to starting position
        this.kinematicState = new KinematicStateImpl.KinematicStateBuilder()
                .setPosition(startingPosition)
                .build();
        this.boundingBox.setTopLeft(startingPosition);
        this.ghostMode = GhostMode.SCATTER;
        this.currentDirectionCount = minimumDirectionCount;

    }

    @Override
    public void setPossibleDirections(Set<Direction> possibleDirections) {
        this.possibleDirections = possibleDirections;
    }

    @Override
    public Direction getDirection() {
        return this.kinematicState.getDirection();
    }

    @Override
    public Vector2D getCenter() {
        return new Vector2D(boundingBox.getMiddleX(), boundingBox.getMiddleY());
    }


    @Override
    public char getGhostType() {
        return ghostType;
    }

    @Override
    public GhostChaseStrategy getChaseStrategy() {
        return chaseStrategy;
    }

    public void setChaseStrategy(GhostChaseStrategy chaseStrategy) {
        this.chaseStrategy = chaseStrategy;
    }

    public Vector2D getChaseTargetPosition() {
        return chaseTargetPosition;
    }

    @Override
    public void setChaseTargetPosition(Vector2D chaseTargetPosition) {
        this.chaseTargetPosition = chaseTargetPosition;
    }


    @Override
    public Vector2D chase(Vector2D pacmanPosition, Direction pacmanDirection) {
        return chaseStrategy.getTargetPosition(pacmanPosition, pacmanDirection, this.getPosition());
    }


    @Override
    public int getFrightenedTickCount() {
        return frightenedTickCount;
    }

    @Override
    public void resetFrightenedTickCount() {
        this.frightenedTickCount = 0;
    }

    @Override
    public void incrementFrightenedTickCount() {
        this.frightenedTickCount++;
    }

    @Override
    public GhostState getCurrentState() {
        return currentState;
    }


    @Override
    public void setCurrentState(GhostState currentState) {
        this.currentState = currentState;
    }

    @Override
    public Image getFrightenedImage() {
        return frightenedImage;
    }


    @Override
    public GhostState getFrightenedState() {
        return frightenedState;
    }

    @Override
    public void setCurrentImage(Image currentImage) {
        this.currentImage = currentImage;
    }


    @Override
    public Image getNormalImage() {
        return image;
    }

    @Override
    public GhostState getNormalState() {
        return normalState;
    }


    @Override
    public void startPause() {
        this.isPaused = true;
        this.pauseTickCount = 0;
    }


    @Override
    public Set<Direction> getPossibleDirections() {
        return possibleDirections;
    }


    @Override
    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }


    @Override
    public KinematicState getKinematicState() {
        return kinematicState;
    }


    @Override
    //use to render mode and speed elements in the map
    public GhostMode getGhostMode() {
        return ghostMode;
    }


    @Override
    public Map<GhostMode, Double> getSpeeds() {
        return speeds;
    }

    @Override
    public int getPoints(int consecutiveGhostsEaten) {
        return 0;
    }
}
