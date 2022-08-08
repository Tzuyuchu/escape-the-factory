package byow.gridsys;

import java.awt.*;

/**
 * TilePoint object for the grid-tracking system. Extends the Java Point class, and adds specific
 * methods that are useful for the program.
 *
 * @author Nicholas Nguyen
 */
public class TilePoint extends Point {
    // Inherited instance fields (int x, int y) from Point

    public TilePoint(int xCoord, int yCoord) {
        super(xCoord, yCoord);
    }

    public TilePoint(TilePoint p) {
        super(p.x, p.y);
    }

    /** Returns direction to adjacent tile. */
    public int getDirTo(TilePoint o) {
        if (x - o.x == 1 && y - o.y == 0) {
            return Grid.WEST;
        } else if (x - o.x == -1 && y - o.y == 0) {
            return Grid.EAST;
        } else if (y - o.y == 1 && x - o.x == 0) {
            return Grid.SOUTH;
        } else if (y - o.y == -1 && x - o.x == 0) {
            return Grid.NORTH;
        } else {
            return -1;
        }
    }

    /** Translates tile one step in the given direction. */
    public void moveInDir(int dir, int distance) {
        int[] dirX = {0, -1, 0, 1};
        int[] dirY = {1, 0, -1, 0};

        x += dirX[dir] * distance;
        y += dirY[dir] * distance;
    }

    public void changePos(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public int stepDistance(TilePoint o) {
        return Math.abs(x - o.x) + Math.abs(y - o.y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
