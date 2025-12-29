package pacman.model.factories;

import javafx.scene.image.Image;
import pacman.ConfigurationParseException;
import pacman.util.ResourceUtils;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.physics.BoundingBox;
import pacman.model.entity.dynamic.physics.BoundingBoxImpl;
import pacman.model.entity.dynamic.physics.Vector2D;
import pacman.model.entity.staticentity.collectable.Pellet;

/**
 * Concrete renderable factory for Pellet objects
 */
public class PelletFactory implements RenderableFactory {
    private static final Image PELLET_IMAGE = new Image(ResourceUtils.toExternalForm("maze/pellet.png"));
    private static final int NUM_POINTS = 100;
    private final Renderable.Layer layer = Renderable.Layer.BACKGROUND;


    // Points awarded for consuming a normal pellet
    private static final int PELLET_POINTS = 10;
    // Points awarded for consuming a power pellet
    private static final int POWER_PELLET_POINTS = 50;

    // Scale factor for power pellets (twice the size of normal pellets)
    private static final double POWER_PELLET_SCALE = 2.0;

    // Offset to center the power pellet in its grid space
    private static final double OFFSET = -8.0;


    @Override
    public Renderable createRenderable(
            Vector2D position
    ) {
        try {

            BoundingBox boundingBox = new BoundingBoxImpl(
                    position,
                    PELLET_IMAGE.getHeight(),
                    PELLET_IMAGE.getWidth()
            );

            return new Pellet(
                    boundingBox,
                    layer,
                    PELLET_IMAGE,
                    NUM_POINTS
            );

        } catch (Exception e) {
            throw new ConfigurationParseException(
                    String.format("Invalid pellet configuration | %s", e));
        }
    }

    /**
     * Creates a pellet based on the given type and position.
     *
     * @param position   The position where the pellet will be created.
     * @param pelletType The type of the pellet ('7' for normal, 'z' for power).
     * @return A Renderable object representing the specified pellet type.
     */
    public Renderable createRenderable(Vector2D position, char pelletType) {
        try {
            // Get the width and height based on the pellet type
            double[] size = getSizeByType(pelletType);
            double width = size[0];
            double height = size[1];

            // Determine the points and position offset based on the pellet type
            int points = (pelletType == RenderableType.POWER_PELLET) ? POWER_PELLET_POINTS : PELLET_POINTS;
            Vector2D offsetPosition = (pelletType == RenderableType.POWER_PELLET)
                    ? applyOffset(position, OFFSET)
                    : position;

            // Create a bounding box for the pellet
            BoundingBox boundingBox = new BoundingBoxImpl(
                    offsetPosition,
                    height,
                    width
            );

            // Return the pellet as a renderable object
            return new Pellet(boundingBox, layer, PELLET_IMAGE, points);

        } catch (Exception e) {
            throw new ConfigurationParseException(
                    String.format("Invalid pellet configuration | %s", e));
        }
    }

    /**
     * Calculates the width and height of the pellet based on its type.
     *
     * If the pellet is a power pellet, its size will be scaled by the POWER_PELLET_SCALE factor.
     *
     * @param pelletType The type of the pellet ('7' for normal, 'z' for power).
     * @return An array containing the width and height of the pellet.
     */
    private double[] getSizeByType(char pelletType) {
        double width = PELLET_IMAGE.getWidth();
        double height = PELLET_IMAGE.getHeight();

        // If the pellet is a power pellet, scale its size by the defined factor
        if (pelletType == RenderableType.POWER_PELLET) {
            width *= POWER_PELLET_SCALE;
            height *= POWER_PELLET_SCALE;
        }

        // Return the calculated width and height as an array
        return new double[]{width, height};
    }

    /**
     * Applies a fixed offset to the given position.
     *
     * This is used to center the power pellet in its grid space.
     *
     * @param position The original position of the pellet.
     * @param offset   The offset to be applied to both X and Y coordinates.
     * @return A new Vector2D object representing the offset position.
     */
    private Vector2D applyOffset(Vector2D position, double offset) {
        return new Vector2D(position.getX() + offset, position.getY() + offset);
    }
}
