package byow.WorldGen;

import byow.TileEngine.*;

import java.util.ArrayList;
import java.util.Random;

/** Do a bunch of stuff, call a bunch of substuff, then return a TETile[][] with a
 * generated map.
 */
public class Map {

    private final Random RANDOM;
    private final int WIDTH;
    private final int HEIGHT;
    private final double ROOM_RATIO = 0.5;
    private final int MAX_ROOM_SIZE = 10;

    private TETile[][] worldMap;
    private boolean[][] boolMap;
    private int filledCount;

    public Map(int width, int height, Random rand) {
        RANDOM = rand;
        filledCount = 0;
        WIDTH = width;
        HEIGHT = height;

        generateRooms();
        generateHallways();
    }

    private void generateRooms() {
        ArrayList<Room> roomColl = new ArrayList<>();
        while ((double) filledCount / (WIDTH * HEIGHT) < ROOM_RATIO) {
            int topLeftX = RANDOM.nextInt(WIDTH - 5);
            int topRightY = RANDOM.nextInt(HEIGHT - 5);
            int newRoomWidth = RANDOM.nextInt(5, MAX_ROOM_SIZE);
            int newRoomHeight = RANDOM.nextInt(5, MAX_ROOM_SIZE);
            Room newRoom = new Room(topLeftX, topRightY, newRoomWidth, newRoomHeighta);
            if (!newRoom.overlap(boolMap)) {
                
            }
        }
    }

    public boolean[][] getBoolMap() {
        return boolMap;
    }
}
