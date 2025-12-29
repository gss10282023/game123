package pacman.model.factories;

import javafx.scene.image.Image;
import pacman.ConfigurationParseException;
import pacman.util.ResourceUtils;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.physics.*;
import pacman.model.entity.dynamic.player.Pacman;
import pacman.model.entity.dynamic.player.PacmanVisual;

import java.util.HashMap;
import java.util.Map;

/**
 * Concrete renderable factory for Pac-Man objects
 */
public class PacmanFactory implements RenderableFactory {
    private static final Image playerLeftImage = new Image(ResourceUtils.toExternalForm("maze/pacman/playerLeft.png"));
    private static final Image playerRightImage = new Image(ResourceUtils.toExternalForm("maze/pacman/playerRight.png"));
    private static final Image playerUpImage = new Image(ResourceUtils.toExternalForm("maze/pacman/playerUp.png"));
    private static final Image playerDownImage = new Image(ResourceUtils.toExternalForm("maze/pacman/playerDown.png"));
    private static final Image playerClosedImage = new Image(ResourceUtils.toExternalForm("maze/pacman/playerClosed.png"));

    @Override
    public Renderable createRenderable(
            Vector2D position
    ) {
        try {
            Map<PacmanVisual, Image> images = new HashMap<>();
            images.put(PacmanVisual.UP, playerUpImage);
            images.put(PacmanVisual.DOWN, playerDownImage);
            images.put(PacmanVisual.LEFT, playerLeftImage);
            images.put(PacmanVisual.RIGHT, playerRightImage);
            images.put(PacmanVisual.CLOSED, playerClosedImage);

            Image currentImage = playerLeftImage;
            position = position.add(new Vector2D(4, -4));

            BoundingBox boundingBox = new BoundingBoxImpl(
                    position,
                    currentImage.getHeight(),
                    currentImage.getWidth()
            );

            KinematicState kinematicState = new KinematicStateImpl.KinematicStateBuilder()
                    .setPosition(position)
                    .setDirection(Direction.LEFT)
                    .build();

            return new Pacman(
                    currentImage,
                    images,
                    boundingBox,
                    kinematicState
            );

        } catch (Exception e) {
            throw new ConfigurationParseException(
                    String.format("Invalid dynamic entity configuration | %s", e));
        }
    }
}
