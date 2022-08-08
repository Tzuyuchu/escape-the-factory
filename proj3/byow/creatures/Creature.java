package byow.creatures;

import byow.gridsys.TilePoint;
import byow.TileEngine.TETile;

import java.io.Serializable;

/**
 * Creature object for BYOW. Represents a dynamic element of the map, that can either change or
 * move over the course of the game.
 *
 * @author Nicholas Nguyen
 */
public class Creature implements Serializable {
    private TilePoint location;
    private TETile avatar;

    public Creature(int initialX, int initialY, TETile tile) {
        location = new TilePoint(initialX, initialY);
        this.avatar = tile;
    }

    public Creature(TilePoint initialPoint, TETile tile) {
        location = initialPoint;
        this.avatar = tile;
    }

    public TilePoint getLoc() {
        return location;
    }

    public int getX() {
        return location.x;
    }

    public int getY() {
        return location.y;
    }

    public void setLoc(int x, int y) {
        location = new TilePoint(x, y);
    }

    public void setLoc(TilePoint newPoint) {
        location = newPoint;
    }

    public void move(int dx, int dy) {
        location.changePos(dx, dy);
    }

    public TETile getAvatar() {
        return avatar;
    }

    public void setAvatar(TETile newAvatar) {
        avatar = newAvatar;
    }
}
