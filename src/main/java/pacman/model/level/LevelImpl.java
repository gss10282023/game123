package pacman.model.level;

import org.json.simple.JSONObject;
import pacman.ConfigurationParseException;
import pacman.model.engine.observer.GameState;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.DynamicEntity;
import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.ghost.GhostMode;
import pacman.model.entity.dynamic.physics.PhysicsEngine;
import pacman.model.entity.dynamic.physics.Vector2D;
import pacman.model.entity.dynamic.player.Controllable;
import pacman.model.entity.dynamic.player.Pacman;
import pacman.model.entity.staticentity.StaticEntity;
import pacman.model.entity.staticentity.collectable.Collectable;
import pacman.model.level.observer.LevelStateObserver;
import pacman.model.state.FrightenedState;
import pacman.model.state.NormalState;
import pacman.model.maze.Maze;
import pacman.model.strategy.InkyChaseStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Concrete implement of Pac-Man level
 */
public class LevelImpl implements Level {

    private static final int START_LEVEL_TIME = 100;
    private final Maze maze;
    private final List<LevelStateObserver> observers;
    private List<Renderable> renderables;
    private Pacman player;
    private List<Ghost> ghosts;
    private int tickCount;
    private Map<GhostMode, Integer> modeLengths;
    private int numLives;
    private int points;
    private GameState gameState;
    private List<Renderable> collectables;
    private GhostMode currentGhostMode;




    private List<Renderable> powerPellets;

    private static final int SECONDS_TRANSFER = 30;


    private int consecutiveGhostsEaten = 0;




    public LevelImpl(JSONObject levelConfiguration,
                     Maze maze) {
        this.renderables = new ArrayList<>();
        this.maze = maze;
        this.tickCount = 0;
        this.observers = new ArrayList<>();
        this.modeLengths = new HashMap<>();
        this.gameState = GameState.READY;
        this.currentGhostMode = GhostMode.SCATTER;
        this.points = 0;

        initLevel(new LevelConfigurationReader(levelConfiguration));
    }

    private void initLevel(LevelConfigurationReader levelConfigurationReader) {
        // Fetch all renderables for the level
        this.renderables = maze.getRenderables();

        // Set up player
        if (!(maze.getControllable() instanceof Controllable)) {
            throw new ConfigurationParseException("Player entity is not controllable");
        }
        this.player = (Pacman) maze.getControllable();
        this.player.setSpeed(levelConfigurationReader.getPlayerSpeed());
        setNumLives(maze.getNumLives());

        // Set up ghosts
        this.ghosts = maze.getGhosts().stream()
                .map(element -> (Ghost) element)
                .collect(Collectors.toList());
        Map<GhostMode, Double> ghostSpeeds = levelConfigurationReader.getGhostSpeeds();






        // Initialize a reference for Blinky, setting it to null initially
        Ghost blinky = null;

// Loop through each Ghost in the list of Ghosts
        for (Ghost ghost : this.ghosts) {
            // Register each Ghost as an observer of the player, enabling updates based on player position
            player.registerObserver(ghost);

            // Set the speeds for each Ghost based on the predefined ghostSpeeds map
            ghost.setSpeeds(ghostSpeeds);

            // Set the initial mode for each Ghost (e.g., chase or scatter mode)
            ghost.setGhostMode(this.currentGhostMode);

            // Check if the current Ghost is Blinky (identified by 'b' type),
            // and if so, store a reference to it in the `blinky` variable
            if (ghost.getGhostType() == 'b') {
                blinky = ghost;
            }
        }

// Second loop to configure Inky's chase strategy, if Blinky has been found
        for (Ghost ghost : this.ghosts) {
            // Check if the current Ghost is Inky (identified by 'i' type) and Blinky exists
            if (ghost.getGhostType() == 'i' && blinky != null) {
                // Retrieve Inky's chase strategy and cast it to InkyChaseStrategy
                InkyChaseStrategy inkyChaseStrategy = (InkyChaseStrategy)ghost.getChaseStrategy();

                // Set Blinky as a reference for Inky's chase strategy, as Inky's movement is influenced by Blinky's position
                inkyChaseStrategy.setBlinky(blinky);
            }
        }



        this.modeLengths = levelConfigurationReader.getGhostModeLengths();
        // Set up collectables
        this.collectables = new ArrayList<>(maze.getPellets());


        //Set up power pellets
        this.powerPellets = new ArrayList<>(maze.getPowerPellets());

    }

    @Override
    public List<Renderable> getRenderables() {
        return this.renderables;
    }

    private List<DynamicEntity> getDynamicEntities() {
        return renderables.stream().filter(e -> e instanceof DynamicEntity).map(e -> (DynamicEntity) e).collect(
                Collectors.toList());
    }

    private List<StaticEntity> getStaticEntities() {
        return renderables.stream().filter(e -> e instanceof StaticEntity).map(e -> (StaticEntity) e).collect(
                Collectors.toList());
    }

    @Override
    public void tick() {
        if (this.gameState != GameState.IN_PROGRESS) {

            if (tickCount >= START_LEVEL_TIME) {
                setGameState(GameState.IN_PROGRESS);
                tickCount = 0;
            }

        } else {


            // Loop through each Ghost in the list of Ghosts
            for (Ghost ghost : ghosts) {
                // Check if the current state of the Ghost is an instance of FrightenedState
                if (ghost.getCurrentState() instanceof FrightenedState) {
                    // Increment the frightened tick count to track how long the Ghost has been in the frightened state
                    ghost.incrementFrightenedTickCount();

                    // Check if the frightened tick count has reached or exceeded the maximum duration for frightened mode
                    // The duration is determined by modeLengths for FRIGHTENED mode and scaled by SECONDS_TRANSFER
                    if (ghost.getFrightenedTickCount() >= (modeLengths.get(GhostMode.FRIGHTENED)) * SECONDS_TRANSFER) {
                        // Reset the count of consecutive ghosts eaten to 0, as frightened mode ends
                        this.consecutiveGhostsEaten = 0;

                        // Transition the Ghost to its next state as frightened mode duration has ended
                        ghost.getCurrentState().switchToNextState();
                    }
                }
            }





            if (tickCount == modeLengths.get(currentGhostMode)) {

                // update ghost mode
                this.currentGhostMode = GhostMode.getNextGhostMode(currentGhostMode);
                for (Ghost ghost : this.ghosts) {

                    //Only change mode not in Frightened mode
                    if (!(ghost.getCurrentState() instanceof FrightenedState)){
                        ghost.setGhostMode(this.currentGhostMode);
                    }

                }

                tickCount = 0;
            }

            if (tickCount % Pacman.PACMAN_IMAGE_SWAP_TICK_COUNT == 0) {
                this.player.switchImage();
            }

            // Update the dynamic entities
            List<DynamicEntity> dynamicEntities = getDynamicEntities();

            for (DynamicEntity dynamicEntity : dynamicEntities) {
                maze.updatePossibleDirections(dynamicEntity);
                dynamicEntity.update();
            }

            for (int i = 0; i < dynamicEntities.size(); ++i) {
                DynamicEntity dynamicEntityA = dynamicEntities.get(i);

                // handle collisions between dynamic entities
                for (int j = i + 1; j < dynamicEntities.size(); ++j) {
                    DynamicEntity dynamicEntityB = dynamicEntities.get(j);

                    if (dynamicEntityA.collidesWith(dynamicEntityB) ||
                            dynamicEntityB.collidesWith(dynamicEntityA)) {
                        dynamicEntityA.collideWith(this, dynamicEntityB);
                        dynamicEntityB.collideWith(this, dynamicEntityA);
                    }
                }

                // handle collisions between dynamic entities and static entities
                for (StaticEntity staticEntity : getStaticEntities()) {
                    if (dynamicEntityA.collidesWith(staticEntity)) {
                        dynamicEntityA.collideWith(this, staticEntity);
                        PhysicsEngine.resolveCollision(dynamicEntityA, staticEntity);
                    }
                }
            }
        }


        for (Ghost ghost : ghosts) {
            // Get the target position based on the player's position and direction
            Vector2D position = ghost.chase(player.getPosition(), player.getDirection());

            Vector2D closestPosition;  // Stores the closest valid position

            // Check if the position is out of grid bounds and adjust accordingly
            if (position.getX() < 0 && position.getY() < 0) {
                // If the position is in the top-left negative area, adjust to (0, 0)
                closestPosition = new Vector2D(0, 0);
            } else if (position.getX() < 0) {
                // If the X-coordinate is out of bounds, set X to 0 and keep Y unchanged
                closestPosition = new Vector2D(0, position.getY());
            } else if (position.getY() < 0) {
                // If the Y-coordinate is out of bounds, set Y to 0 and keep X unchanged
                closestPosition = new Vector2D(position.getX(), 0);
            } else {
                // If both coordinates are valid, use the original position
                closestPosition = new Vector2D(position.getX(), position.getY());
            }

            // Set the ghost's chase target to the closest valid position
            ghost.setChaseTargetPosition(closestPosition);
        }

        tickCount++;
    }

    @Override
    public boolean isPlayer(Renderable renderable) {
        return renderable == this.player;
    }

    @Override
    public boolean isCollectable(Renderable renderable) {
        return maze.getPellets().contains(renderable) && ((Collectable) renderable).isCollectable();
    }

    @Override
    public void collect(Collectable collectable) {
        this.points += collectable.getPoints();
        notifyObserversWithScoreChange(collectable.getPoints());

        if(powerPellets.contains(collectable)){

            consecutiveGhostsEaten = 0;

            for(Ghost ghost: this.ghosts){

                ghost.resetFrightenedTickCount();
                if(ghost.getCurrentState() instanceof NormalState) {
                    ghost.getCurrentState().switchToNextState();
                }
            }
        }



        this.collectables.remove(collectable);
    }

    @Override
    public void handleLoseLife() {
        if (gameState == GameState.IN_PROGRESS) {
            for (DynamicEntity dynamicEntity : getDynamicEntities()) {
                dynamicEntity.reset();
            }
            setNumLives(numLives - 1);
            consecutiveGhostsEaten = 0;
            setGameState(GameState.READY);
            tickCount = 0;
        }
    }

    @Override
    public void moveLeft() {
        player.left();
    }

    @Override
    public void moveRight() {
        player.right();
    }

    @Override
    public void moveUp() {
        player.up();
    }

    @Override
    public void moveDown() {
        player.down();
    }

    @Override
    public boolean isLevelFinished() {
        return collectables.isEmpty();
    }

    @Override
    public void registerObserver(LevelStateObserver observer) {
        this.observers.add(observer);
        observer.updateNumLives(this.numLives);
        observer.updateGameState(this.gameState);
    }

    @Override
    public void removeObserver(LevelStateObserver observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObserversWithNumLives() {
        for (LevelStateObserver observer : observers) {
            observer.updateNumLives(this.numLives);
        }
    }

    private void setGameState(GameState gameState) {
        this.gameState = gameState;
        notifyObserversWithGameState();
    }

    @Override
    public void notifyObserversWithGameState() {
        for (LevelStateObserver observer : observers) {
            observer.updateGameState(gameState);
        }
    }

    /**
     * Notifies observer of change in player's score
     */
    public void notifyObserversWithScoreChange(int scoreChange) {
        for (LevelStateObserver observer : observers) {
            observer.updateScore(scoreChange);
        }
    }

    @Override
    public int getPoints() {
        return this.points;
    }

    @Override
    public int getNumLives() {
        return this.numLives;
    }

    private void setNumLives(int numLives) {
        this.numLives = numLives;
        notifyObserversWithNumLives();
    }

    @Override
    public void handleGameEnd() {
        this.renderables.removeAll(getDynamicEntities());
    }


    @Override
    public int getConsecutiveGhostsEaten() {
        return consecutiveGhostsEaten;
    }

    @Override
    public void setConsecutiveGhostsEaten(int consecutiveGhostsEaten) {
        this.consecutiveGhostsEaten = consecutiveGhostsEaten;
    }
}
