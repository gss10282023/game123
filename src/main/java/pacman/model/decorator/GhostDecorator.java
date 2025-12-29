package pacman.model.decorator;

public abstract class GhostDecorator implements Component {
    // The decorated Component object, representing a specific Ghost instance
    Component component;


    // the points earned by eating multiple ghosts consecutively
    public abstract int getPoints(int consecutiveGhostsEaten);
}
