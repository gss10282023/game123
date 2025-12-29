package pacman.model.decorator;

public class FrightenedGhostDecorator extends GhostDecorator {

    // Initial base points for eating a frightened ghost
    private static final int INITIAL_POINTS = 200;

    // Constructor that accepts a Component (e.g., a ghost) to decorate
    public FrightenedGhostDecorator(Component component) {
        this.component = component;
    }

    // Overrides the getPoints method to calculate the points based on
    // the number of consecutive ghosts eaten, with points doubling for each
    // additional frightened ghost eaten in sequence.
    @Override
    public int getPoints(int consecutiveGhostsEaten) {
        int pointsAwarded = INITIAL_POINTS * (int) Math.pow(2, consecutiveGhostsEaten - 1);
        return pointsAwarded;
    }

}
