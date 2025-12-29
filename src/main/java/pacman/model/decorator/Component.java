package pacman.model.decorator;

public interface Component {
    // Method to calculate and return points based on the number of consecutive ghosts eaten
    int getPoints(int consecutiveGhostsEaten);
}
