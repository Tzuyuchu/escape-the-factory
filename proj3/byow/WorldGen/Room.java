package byow.WorldGen;

/**
* This class simulates one room created within the game, containing necessary class variables
* As a convention, we refer to the dimensions of the room as including the walls, i.e.
* A room that has floor space 3x4 will be referred to as a 5x6 room, as there is an outer layer of wall
* BTW first array is the width
*/

public class Room {

    // Denoting the x and y coordinates of the top left corner of the room
    public int topLeftX;
    public int topLeftY;

    //includes walls
    public int width;
    public int length;

    // each index of the array represents a side of the room
    // 0 is top, 1 is down, 2 is left, 3 is right
    public boolean[] occupiedSides = new boolean[4];

    public Room(int topLeftX, int topLeftY, int width, int length) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.width = width;
        this.length = length;
        this.occupiedSides = occupiedSides;
    }

    public boolean overlap(boolean[][] boolMap) {
        boolean[][] boolMap = Map.getBoolMap();

    }
}
