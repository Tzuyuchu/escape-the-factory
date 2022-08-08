package byow.worldgen;

import byow.Core.RandomUtils;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Minimum Spanning Tree Map Generator. This algorithm builds rooms in random locations within
 * the provided grid, until a certain ratio is reached. Then, it constructs a minimum spanning tree
 * between the rooms as node, with the weight being distance between centers of each room. For each
 * leaf room with only one connection, the algorithm randomly chooses a number to connect to a
 * second room, if the second room is closer to the leaf room than it is to the first room. This
 * adds a number of cycles in the final map, resulting in less predictability in hallway generation.
 *
 * The program attempts to generate hallways between connected rooms in the following order:
 *      - 2-wide straight hallways
 *      - 2-wide turning hallways
 *      - 1-wide straight hallways
 *      - 1-wide turning hallways
 *
 * If no turn is made in the final draft, or some room remains unconnected due to collision, the
 * world is re-generated.
 *
 * @author Nicholas Nguyen
 */
public class MSTMapGenerator implements MapGenerator {

    /** The ratio of tiles occupied by Rooms with total number of tiles. When the ratio is
     * exceeded, the program stops generating random Rooms. */
    private static final double ROOM_RATIO = 0.5;
    /** The maximum possible size of a room, inclusive of the walls. */
    private static final int[] MAX_ROOM_SIZES = {0, 0, 0, 0, 0, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1};
    /** Chance that a Room is connected to its closest neighbor (if that was not already
     *  included in the MST). */
    private static final double EXTRA_ROOM_CHANCE = 0.7;

    private List<MSTRoom> roomColl;
    /** The Random object associated with the given seed, used for pseudo-random generation. */
    private Random rand;
    /** The width of the map. */
    private int width;
    /** The height of the map. */
    private int height;
    /** 2-dimensional array of TETiles, representing the world map. */
    private TETile[][] worldMap;
    /** Determines whether a turning hallway has been made in this map. */
    private boolean madeTurn;

    /** 2-dimensional array that indicates whether a tile is already filled by something
     * that was randomly generated. Is useful for collision detection. Each space contains a int.
     * 0 indicates an empty space, -1 indicates a hallway. Rooms are indicated by their room
     * number. */
    private int[][] intMap;

    /**
     * Generates a functional map using a provided width, height, and pseudo-random object,
     * and sets it as its worldMap.
     */
    @Override
    public TETile[][] generate(int w, int h, Random r) {
        width = w;
        height = h;
        rand = r;
        worldMap = new TETile[w][h];
        intMap = new int[w][h];
        madeTurn = false;

        // Sets the backdrop of the worldMap as NOTHING tiles.
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                worldMap[x][y] = Tileset.NOTHING;
            }
        }

        // Generate and fill rooms.
        generateRooms();
        for (MSTRoom room : roomColl) {
            for (int x = room.west; x <= room.east; x++) {
                for (int y = room.south; y <= room.north; y++) {
                    if (x == room.west || x == room.east || y == room.north || y == room.south) {
                        worldMap[x][y] = Tileset.WALL;
                    } else {
                        worldMap[x][y] = Tileset.FLOOR;
                    }
                }
            }
        }

        // Generate and fill hallways.
        connectRooms();
        generateHallways(4);
        generateHallways(3);

        // Checks that every necessary hallway is created. If not, restart the world generation.
        if (!madeTurn) {
            return generate(w, h, r);
        }
        for (MSTRoom room : roomColl) {
            if (!room.connections.isEmpty()) {
                return generate(w, h, r);
            }
        }

        return worldMap;
    }



    /** Generates a list of MSTRoom objects that fit within the Map. This is accomplished by
     *  repeatedly generating random MSTRoom objects, adding them to the List if they are valid.
     *  This is repeated until the amount of tiles occupied by Rooms exceeds {@link #ROOM_RATIO}.
     */
    private void generateRooms() {
        roomColl = new ArrayList<>();
        int roomNum = 1;

        double filledCount = 0;
        while (filledCount / (width * height) < ROOM_RATIO) {
            int west = RandomUtils.uniform(rand, width - 5);
            int south = RandomUtils.uniform(rand, height - 5);
            int newRoomWidth = RandomUtils.discrete(rand, MAX_ROOM_SIZES);
            int newRoomHeight = RandomUtils.discrete(rand, MAX_ROOM_SIZES);
            MSTRoom newRoom = new MSTRoom(west, south, newRoomWidth, newRoomHeight, roomNum);
            if (newRoom.noOverlap(intMap)) {
                roomColl.add(newRoom);
                filledCount += newRoomHeight * newRoomWidth;
                updateIntMap(newRoom);
                roomNum += 1;
            }
        }
    }

    /** Naive attempt at creating a Minimum Spanning Tree of all the Rooms using distance.*/
    private void connectRooms() {
        List<MSTRoom> unconnected = new ArrayList<>(List.copyOf(roomColl));
        List<MSTRoom> connected = new ArrayList<>();
        connected.add(unconnected.remove(0));
        while (unconnected.size() != 0) {
            MSTRoom clstCon = connected.get(0);
            MSTRoom clstUncon = unconnected.get(0);
            double minDist = Integer.MAX_VALUE;
            for (MSTRoom unconRoom : unconnected) {
                for (MSTRoom conRoom : connected) {
                    double dist = unconRoom.distance(conRoom);
                    if (dist < minDist) {
                        clstCon = conRoom;
                        clstUncon = unconRoom;
                        minDist = dist;
                    }
                }
            }
            clstCon.connections.add(clstUncon);
            clstUncon.connections.add(clstCon);
            unconnected.remove(clstUncon);
            connected.add(clstUncon);
        }
        // Extra rooms
        for (MSTRoom room : connected) {
            if (room.connections.size() > 1) {
                continue;
            }
            double extraConChance = RandomUtils.uniform(rand);
            if (extraConChance < EXTRA_ROOM_CHANCE) {
                MSTRoom clstCon = null;
                double minDist = Integer.MAX_VALUE;
                for (MSTRoom otherRoom : connected) {
                    if (otherRoom == room || room.connections.contains(otherRoom)) {
                        continue;
                    }
                    double dist = room.distance(otherRoom);
                    if (dist < minDist) {
                        minDist = dist;
                        clstCon = otherRoom;
                    }
                }
                MSTRoom roomNeighbor = room.connections.get(0);
                if (clstCon != null && minDist < roomNeighbor.distance(clstCon)) {
                    room.connections.add(clstCon);
                    clstCon.connections.add(room);
                    room.roomHappy = true;
                }
            }
        }
    }

    /** Driver method for creating hallways of a given size. Iterates through each room, creating
     *  a hallway with its connection then removing the connection if a hallway is successfully
     *  constructed. */
    private void generateHallways(int size) {
        ArrayList<MSTRoom> removed = new ArrayList<>();
        for (MSTRoom room : roomColl) {
            removed.clear();
            for (MSTRoom conRoom : room.connections) {
                boolean res = generateDirectHallway(room, conRoom, size);
                if (res) {
                    madeTurn = true;
                    conRoom.connections.remove(room);
                    removed.add(conRoom);
                }
            }
            room.connections.removeAll(removed);
        }
        for (MSTRoom room : roomColl) {
            removed.clear();
            for (MSTRoom conRoom : room.connections) {
                boolean res = generateTurningHallway(room, conRoom, size);
                if (res) {
                    madeTurn = true;
                    conRoom.connections.remove(room);
                    removed.add(conRoom);
                }
            }
            room.connections.removeAll(removed);
        }
    }

    /** Attempts to build a direct hallway of a given size with no turns between two rooms.
     *  Returns true of a room is successfully built, false if the space between the rooms are
     *  insufficient. */
    private boolean generateDirectHallway(MSTRoom room1, MSTRoom room2, int size) {
        int indSize = size - 1;
        int[] room1Lower = {room1.west, room1.south};
        int[] room1Higher = {room1.east, room1.north};
        int[] room2Lower = {room2.west, room2.south};
        int[] room2Higher = {room2.east, room2.north};
        int wallDir = (room1.east < room2.west || room2.east < room1.west) ? 0 : 1;
        int boundDir = (wallDir + 1) % 2;

        int upperBound = Math.min(room1Higher[boundDir], room2Higher[boundDir]);
        int lowerBound = Math.max(room1Lower[boundDir], room2Lower[boundDir]);
        if (upperBound - lowerBound >= indSize) {
            int smallWall = (lowerBound + indSize == upperBound)
                    ? lowerBound : rand.nextInt(lowerBound, upperBound - indSize);
            int largeWall = smallWall + indSize;
            int start = Math.min(room1Higher[wallDir], room2Higher[wallDir]);
            int stop = Math.max(room1Lower[wallDir], room2Lower[wallDir]);
            buildHallway(3 * boundDir, smallWall, largeWall, start, stop);
            return true;
        }
        return false;
    }

    /** Helper method of building a hallway between two points, given a direction and coordinates
     *  for sizes. */
    private void buildHallway(int dir, int smallWall, int largeWall, int start, int stop) {
        // if dir is 0, start -> stop (y coords inc), left -> right (x coords inc) - 1 / 0
        // if dir is 1, start -> stop (x coords dec), left -> right (y coords inc) - 0 / -1
        // if dir is 2, start -> stop (y coords dec), right -> left (x coords inc) - -1 / 0
        // if dir is 3, start -> stop (x coords inc), right -> left (y coords inc) - 0 / 1
        int[] dirX = {0, -1, 0, 1};
        int[] dirY = {1, 0, -1, 0};
        int i = start;
        do {
            for (int j = smallWall; j <= largeWall; j += 1) {
                int x = (dir % 2 == 0) ? j : i;
                int y = (dir % 2 == 0) ? i : j;
                intMap[x][y] = -1;
                worldMap[x][y] = (j == smallWall || j == largeWall)
                        ? Tileset.WALL : Tileset.FLOOR;
            }
            if (i == stop) {
                break;
            }
            i += dirX[dir] + dirY[dir];
        } while (true);
    }

    /**
     * Generates a hallway that has a turn between two rooms, of a given size. If generation is
     * successful, returns true, otherwise returns false. This is split into four cases.
     *
     * Case 1. Most-west is most-south.
     *      - Case 1.1. Test northwest
     *      - Case 1.2. Test southeast
     * Case 2. Most-west is not most-south.
     *      - Case 2.1. Test southwest
     *      - Case 2.2. Test northeast
     */
    private boolean generateTurningHallway(MSTRoom room1, MSTRoom room2, int size) {
        MSTRoom westRoom = room1.west < room2.west ? room1 : room2;
        MSTRoom southRoom = room1.south < room2.south ? room1 : room2;
        if (westRoom == southRoom) {
            MSTRoom eastRoom = room1.west < room2.west ? room2 : room1;
            return generateTurningHallwayCase1(westRoom, eastRoom, size);
        }
        return generateTurningHallwayCase2(westRoom, southRoom, size);
    }

    /** Sub-method routine of generateTurningHallway, in the case of the south room also being
     *  the west room. */
    private boolean generateTurningHallwayCase1(MSTRoom westRoom, MSTRoom eastRoom, int size) {
        // Test northwest
        if (eastRoom.north - westRoom.north >= size - 1
                && eastRoom.west - westRoom.west >= size - 1) {
            int southNodeWall = Math.max(westRoom.north, eastRoom.south);
            int eastNodeWall = Math.min(westRoom.east, eastRoom.west);
            boolean valid = true;
            for (int x = eastNodeWall - 1; x > eastNodeWall - size; x--) {
                for (int y = southNodeWall + 1; y < southNodeWall + size; y++) {
                    if (isOutOfRange(x, width) || isOutOfRange(y, height)
                            || intMap[x][y] != 0) {
                        valid = false;
                        break;
                    }
                }
            }
            if (valid) { // There exists a valid hallway path
                //Build Hallway going south
                buildHallway(2, eastNodeWall - size + 1, eastNodeWall,
                        southNodeWall, westRoom.north);
                // Build hallway going east
                buildHallway(3, southNodeWall, southNodeWall + size - 1,
                        eastNodeWall, eastRoom.west);
                // Build corner
                for (int x = eastNodeWall - 1; x > eastNodeWall - size; x--) {
                    for (int y = southNodeWall + 1; y < southNodeWall + size; y++) {
                        intMap[x][y] = -1;
                        worldMap[x][y] = (x == eastNodeWall - size + 1
                                || y == southNodeWall + size - 1) ? Tileset.WALL : Tileset.FLOOR;
                    }
                }
                return true;
            }
        }
        // Test southeast
        if (eastRoom.south - westRoom.south >= size - 1
                && eastRoom.east - westRoom.east >= size - 1) {
            int northNodeWall = Math.min(westRoom.north, eastRoom.south);
            int westNodeWall = Math.max(westRoom.east, eastRoom.west);
            boolean valid = true;
            for (int x = westNodeWall + 1; x < westNodeWall + size; x++) {
                for (int y = northNodeWall - 1; y > northNodeWall - size; y--) {
                    if (isOutOfRange(x, width) || isOutOfRange(y, height)
                            || intMap[x][y] != 0) {
                        valid = false;
                        break;
                    }
                }
            }
            if (valid) { // There exists a valid hallway path
                //Build Hallway going north
                buildHallway(0, westNodeWall, westNodeWall + size - 1,
                        northNodeWall, eastRoom.south);
                // Build hallway going west
                buildHallway(1,  northNodeWall - size + 1,  northNodeWall,
                        westNodeWall, westRoom.east);
                // Build corner
                for (int x = westNodeWall + 1; x < westNodeWall + size; x++) {
                    for (int y = northNodeWall - 1; y > northNodeWall - size; y--) {
                        intMap[x][y] = -1;
                        worldMap[x][y] = (x == westNodeWall + size - 1
                                || y == northNodeWall - size + 1)
                                ? Tileset.WALL : Tileset.FLOOR;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /** Sub-method routine of generateTurningHallway, in the case of the south room not being
     *  the west room. */
    private boolean generateTurningHallwayCase2(MSTRoom westRoom, MSTRoom southRoom, int size) {
        // Test northeast
        if (westRoom.north - southRoom.north >= size - 1
                && southRoom.east - westRoom.east >= size - 1) {
            int westNodeWall = Math.max(westRoom.east, southRoom.west);
            int southNodeWall = Math.max(westRoom.south, southRoom.north);
            boolean valid = true;
            // Check for overlaps
            for (int x = westNodeWall + 1; x < westNodeWall + size; x++) {
                for (int y = southNodeWall + 1; y < southNodeWall + size; y++) {
                    if (isOutOfRange(x, width) || isOutOfRange(y, height)
                            || intMap[x][y] != 0) {
                        valid = false;
                        break;
                    }
                }
            }
            if (valid) { // There exists a valid hallway path
                //Build Hallway going west
                buildHallway(1, southNodeWall, southNodeWall + size - 1,
                        westNodeWall, westRoom.east);
                // Build hallway going south
                buildHallway(2, westNodeWall, westNodeWall + size - 1,
                        southNodeWall, southRoom.north);
                // Build corner
                for (int x = westNodeWall + 1; x < westNodeWall + size; x++) {
                    for (int y = southNodeWall + 1; y < southNodeWall + size; y++) {
                        intMap[x][y] = -1;
                        worldMap[x][y] = (x == westNodeWall + size - 1
                                || y == southNodeWall + size - 1)
                                ? Tileset.WALL : Tileset.FLOOR;
                    }
                }
                return true;
            }
        }
        // Test southwest
        if (westRoom.south - southRoom.south >= size - 1
                && southRoom.west - westRoom.west >= size - 1) {
            int eastNodeWall = Math.min(westRoom.east, southRoom.west);
            int southNodeWall = Math.min(westRoom.south, southRoom.north);
            boolean valid = true;
            // Check for overlaps
            for (int x = eastNodeWall - 1; x > eastNodeWall - size; x--) {
                for (int y = southNodeWall - 1; y > southNodeWall - size; y--) {
                    if (isOutOfRange(x, width) || isOutOfRange(y, height)
                            || intMap[x][y] != 0) {
                        valid = false;
                        break;
                    }
                }
            }
            if (valid) { // There exists a valid hallway path
                //Build Hallway going east
                buildHallway(3, southNodeWall - size + 1, southNodeWall,
                        eastNodeWall, southRoom.west);
                // Build hallway going north
                buildHallway(0, eastNodeWall - size + 1, eastNodeWall,
                        southNodeWall, westRoom.south);
                // Build corner
                for (int x = eastNodeWall - 1; x > eastNodeWall - size; x--) {
                    for (int y = southNodeWall - 1; y > southNodeWall - size; y--) {
                        intMap[x][y] = -1;
                        worldMap[x][y] = (x == eastNodeWall - size + 1
                                || y == southNodeWall - size + 1)
                                ? Tileset.WALL : Tileset.FLOOR;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /** Helper method that checks if a value is within a min and max. Used to check for whether
     *  an index is on a possible tile. */
    public static boolean isOutOfRange(int value, int max) {
        return value < 0 || value > max;
    }

    /** Updates {@link #intMap} to reflect the existence of the given MSTRoom in the Map.
     *
     *  @param room Newly created MSTRoom object.
     */
    private void updateIntMap(MSTRoom room) {
        for (int x = room.west; x <= room.east; x++) {
            for (int y = room.south; y <= room.north; y++) {
                intMap[x][y] = room.roomNum;
            }
        }
    }

    /**
     * Retrieves the intMap
     */
    @Override
    public int[][] getIntMap() {
        return intMap;
    }

    /**
     * Returns a TETile[][] map using a intMap associated with the object's worldMap. Used
     * for testing purposes.
     */
    @Override
    public TETile[][] getNumMap() {
        TETile[][] resMap = new TETile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                char thisChar = charRepr(intMap[x][y]);
                Color bgCol = thisChar == '`' ? new Color(0, 0, 0)
                        : new Color(59, 123, 134);
                resMap[x][y] = new TETile(thisChar, new Color(255, 255, 255),
                        bgCol, "debug");
            }
        }
        return resMap;
    }

    private char charRepr(int value) {
        return (char) ('a' + value - 1);
    }


    private static class MSTRoom {

        // Denotes the edges of the room.
        protected int north;
        protected int west;
        protected int south;
        protected int east;

        //includes walls
        protected int width;
        protected int height;

        protected int roomNum;
        protected List<MSTRoom> connections;

        protected boolean roomHappy = false;

        MSTRoom(int west, int south, int width, int height, int roomNum) {
            this.north = south + height - 1;
            this.west = west;
            this.south = south;
            this.east = west + width - 1;

            this.width = width;
            this.height = height;

            this.roomNum = roomNum;
            this.connections = new ArrayList<>();
        }

        public boolean noOverlap(int[][] intMap) {
            //this function checks to see if the boolMap currently holds any rooms that will
            //overlap with the current room. It returns false if there is an overlap

            for (int i = west; i <= east; i++) {
                for (int j = south; j <= north; j++) {
                    if (i >= intMap.length || j >= intMap[i].length || intMap[i][j] != 0) {
                        return false;
                    }
                }
            }
            return true;
        }

        public double distance(MSTRoom otherRoom) {
            int avgX1 = west + width / 2;
            int avgX2 = otherRoom.west + otherRoom.width / 2;
            int avgY1 = south + height / 2;
            int avgY2 = otherRoom.south + otherRoom.height / 2;
            double xDiff = Math.abs(avgX1 - avgX2);
            double yDiff = Math.abs(avgY1 - avgY2);
            return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        }
    }
}
