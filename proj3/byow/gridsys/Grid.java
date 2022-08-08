package byow.gridsys;

import byow.TileEngine.TETile;
import byow.creatures.Creature;

import java.io.Serializable;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Grid object as a more abstract way of accessing TETile[][] maps while utilizing the Point class.
 *
 * @author Nicholas Nguyen.
 */
public class Grid implements Serializable {
    /** Static fields for cardinal directions. */
    public static final int NORTH = 0;
    public static final int WEST = 1;
    public static final int SOUTH = 2;
    public static final int EAST = 3;

    /** Underlying 2-D array of the Grid. */
    private TETile[][] map;

    /** Initializer, adds map. */
    public Grid(TETile[][] m) {
        this.map = m;
    }

    /**
     * Pathfinding algorithm using A* on a grid.
     *
     * @param start Starting node to path-find from.
     * @param stop Ending node to path-find to.
     * @return A List of nodes that compose a path from start to stop, inclusive of the two nodes.
     */
    public List<TilePoint> pathfinder(TilePoint start, TilePoint stop) {
        PriorityQueue<TilePointNode> fringe = new PriorityQueue<>();
        fringe.offer(new TilePointNode(start, null, start.stepDistance(stop), 0));
        Set<TilePoint> visited = new HashSet<>();

        TilePointNode pathPtr = null;
        while (!fringe.isEmpty()) {
            TilePointNode curNode = fringe.poll();
            TilePoint curPoint = curNode.point;
            if (curPoint.equals(stop)) {
                pathPtr = curNode;
                break;
            }
            fringe.removeIf(ptNode -> ptNode.point.equals(curPoint));

            TilePoint[] nwse = {
                new TilePoint(curPoint.x, curPoint.y + 1),
                new TilePoint(curPoint.x - 1, curPoint.y),
                new TilePoint(curPoint.x, curPoint.y - 1),
                new TilePoint(curPoint.x + 1, curPoint.y),
            };
            for (TilePoint tilePoint : nwse) {
                if (getTile(tilePoint).valid() && !visited.contains(tilePoint)) {
                    int startDist = curNode.startDist + 1;
                    fringe.offer(new TilePointNode(tilePoint, curNode,
                            startDist + tilePoint.stepDistance(stop), startDist));
                }
            }
            visited.add(curPoint);
        }

        LinkedList<TilePoint> path = new LinkedList<>();
        while (pathPtr != null) {
            path.addFirst(pathPtr.point);
            pathPtr = pathPtr.parent;
        }
        return new ArrayList<>(path);
    }

    /** Helper class for pathfind(). */
    private record TilePointNode(TilePoint point, TilePointNode parent, int weight,
                                 int startDist) implements Comparable<TilePointNode> {

        @Override
        public int compareTo(TilePointNode o) {
            return weight - o.weight;
        }
    }

    /**
     * Line of sight algorithm. Determines if an unobstructed line can be drawn from an origin Tile
     * to the center of the target Tile.
     *
     * @param target The target Tile
     * @return Returns true if an unobstructed line can be drawn to the center of target, and false
     * if no such line can be made.
     */
    public boolean sight(TilePoint origin, TilePoint target) {
        if (origin.equals(target)) {
            return true;
        }
        HashSet<TilePoint> inline = new HashSet<>();
        if (origin.x == target.x) { // Vertical line case
            int dir = (target.y - origin.y) / Math.abs(target.y - origin.y);
            for (int y = origin.y + dir; y != target.y; y += dir) {
                inline.add(new TilePoint(origin.x, y));
            }
        } else {
            int dir = (target.x - origin.x) / Math.abs(target.x - origin.x);
            double slope = ((double) target.y - origin.y) / (target.x - origin.x);
            UnaryOperator<Double> lineFunc = x -> slope * (x - origin.x) + origin.y;
            double prevLow = origin.y - 0.5;
            double prevHigh = origin.y + 0.5;
            for (int x = origin.x; x != target.x; x += dir) {
                double intersection = lineFunc.apply(x + (0.5 * dir));
                double newLow = Math.round(intersection) - 0.5;
                double newHigh = roundFavorDown(intersection) + 0.5;
                List<TilePoint> betweenTiles = between(x, Math.min(prevLow, newLow),
                        Math.max(prevHigh, newHigh));
                inline.addAll(betweenTiles);
                prevLow = newLow;
                prevHigh = newHigh;
            }
            double newLow = target.y - 0.5;
            double newHigh = target.y + 0.5;
            List<TilePoint> betweenTiles = between(target.x, Math.min(prevLow, newLow),
                    Math.max(prevHigh, newHigh));
            inline.addAll(betweenTiles);
            inline.remove(origin);
            inline.remove(target);
        }

        // Iterate through all inline tiles
        for (TilePoint tile : inline) {
            if (tile.x < 0 || tile.x > map.length - 1 || tile.y < 0 || tile.y > map[0].length - 1) {
                continue;
            }
            if (!getTile(tile).valid()) {
                return false;
            }
        }
        return true;
    }

    /** Uses Math.round unless the distance is equal between the two integers, in which case
     * negative direction is favored.*/
    private double roundFavorDown(double a) {
        if (2 * a % 1 == 0) {
            return Math.floor(a);
        } else {
            return Math.round(a);
        }
    }

    /** Returns a List of TilePoints that are in between the two given doubles for y. */
    private static List<TilePoint> between(int x, double low, double high) {
        ArrayList<TilePoint> res = new ArrayList<>();
        for (double d = low + 0.5; d <= high - 0.5; d += 1) {
            res.add(new TilePoint(x, (int) d));
        }
        return res;
    }

    public TETile getTile(TilePoint p) {
        return map[p.x][p.y];
    }

    public TETile getTile(int xCoord, int yCoord) {
        return map[xCoord][yCoord];
    }

    public void setTile(TilePoint tile, TETile tileType) {
        map[tile.x][tile.y] = tileType;
    }

    public void setTile(Creature creature) {
        setTile(creature.getLoc(), creature.getAvatar());
    }

    public static TETile tileAt(TETile[][] map, TilePoint point) {
        return map[point.x][point.y];
    }

    public TETile[][] getMap() {
        return map;
    }

    public void setMap(TETile[][] newMap) {
        map = newMap;
    }
}
