package pacman.model.strategy;

import org.junit.jupiter.api.Test;
import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.physics.Direction;
import pacman.model.entity.dynamic.physics.Vector2D;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GhostChaseStrategyTest {

    @Test
    void blinkyTargetsPacmanPosition() {
        GhostChaseStrategy strategy = new BlinkyChaseStrategy();
        Vector2D target = strategy.getTargetPosition(new Vector2D(5, 6), Direction.LEFT, new Vector2D(0, 0));

        assertEquals(5, target.getX());
        assertEquals(6, target.getY());
    }

    @Test
    void pinkyTargetsFourTilesAhead() {
        GhostChaseStrategy strategy = new PinkyChaseStrategy();

        Vector2D up = strategy.getTargetPosition(new Vector2D(10, 10), Direction.UP, new Vector2D(0, 0));
        assertEquals(10, up.getX());
        assertEquals(6, up.getY());

        Vector2D right = strategy.getTargetPosition(new Vector2D(10, 10), Direction.RIGHT, new Vector2D(0, 0));
        assertEquals(14, right.getX());
        assertEquals(10, right.getY());
    }

    @Test
    void clydeScattersWhenCloseOtherwiseChases() {
        GhostChaseStrategy strategy = new ClydeChaseStrategy();

        Vector2D chase = strategy.getTargetPosition(new Vector2D(10, 10), Direction.LEFT, new Vector2D(0, 0));
        assertEquals(10, chase.getX());
        assertEquals(10, chase.getY());

        Vector2D scatter = strategy.getTargetPosition(new Vector2D(1, 1), Direction.LEFT, new Vector2D(2, 2));
        assertEquals(0, scatter.getX());
        assertEquals(544, scatter.getY());
    }

    @Test
    void inkyTargetsRelativeToBlinkyAndLookahead() {
        InkyChaseStrategy strategy = new InkyChaseStrategy();
        Ghost blinky = ghostWithPosition(new Vector2D(2, 2));
        strategy.setBlinky(blinky);

        Vector2D target = strategy.getTargetPosition(new Vector2D(10, 10), Direction.RIGHT, new Vector2D(0, 0));

        // Lookahead: (12, 10). Vector from Blinky (2, 2) is (10, 8). Scaled by 2 => (20, 16).
        // Final target: (22, 18).
        assertEquals(22, target.getX());
        assertEquals(18, target.getY());
    }

    private static Ghost ghostWithPosition(Vector2D position) {
        return (Ghost) Proxy.newProxyInstance(
                Ghost.class.getClassLoader(),
                new Class<?>[]{Ghost.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getPosition" -> {
                            return position;
                        }
                        case "toString" -> {
                            return "GhostStub(" + position + ")";
                        }
                        case "hashCode" -> {
                            return System.identityHashCode(proxy);
                        }
                        case "equals" -> {
                            return proxy == args[0];
                        }
                        default -> {
                            Class<?> returnType = method.getReturnType();
                            if (returnType.equals(boolean.class)) {
                                return false;
                            }
                            if (returnType.equals(int.class)) {
                                return 0;
                            }
                            if (returnType.equals(double.class)) {
                                return 0.0;
                            }
                            return null;
                        }
                    }
                }
        );
    }
}

