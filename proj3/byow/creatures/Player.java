package byow.creatures;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.gridsys.TilePoint;

/**
 * Player object for BYOW. Supports looking left and right, which changes the object's active avatar
 * based on most recent movement.
 *
 * @author Nicholas Nguyen
 */
public class Player extends Creature {
    public static final int AVATAR1 = 0;

    private final TETile leftAvatar = Tileset.PLAYER_LEFT;
    private final TETile rightAvatar = Tileset.PLAYER_RIGHT;

    public Player(int initialX, int initialY, int character) {
        super(initialX, initialY, null);
        setAvatar(rightAvatar);
    }

    public Player(TilePoint tile, int character) {
        super(tile, null);
        setAvatar(rightAvatar);
    }

    public void lookLeft() {
        setAvatar(leftAvatar);
    }

    public void lookRight() {
        setAvatar(rightAvatar);
    }
}
