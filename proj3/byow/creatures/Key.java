package byow.creatures;

import byow.TileEngine.Tileset;
import byow.gridsys.TilePoint;

/**
 * Key object for BYOW. Is mostly non-functional, and is listed as a Creature simply to hold its
 * coordinates.
 */
public class Key extends Creature {
    public Key(int initialX, int initialY) {
        super(initialX, initialY, Tileset.KEY);
    }

    public Key(TilePoint initialPoint) {
        super(initialPoint, Tileset.KEY);
    }
}
