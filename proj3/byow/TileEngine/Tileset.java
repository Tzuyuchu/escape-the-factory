package byow.TileEngine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */
public class Tileset {
    private static final Color floorFill = new Color(195, 195, 195);
    private static final Color floorEdge = new Color(114, 114, 114);
    private static final Color seenFloorFill = new Color(110, 110, 110);
    private static final Color seenFloorEdge = new Color(87, 87, 87);
    
    public static final TETile PLAYER_RIGHT = new TETile('@', floorEdge, floorFill,
            "You.", "player_right");
    public static final TETile PLAYER_LEFT = new TETile('@', floorEdge, floorFill,
            "You.", "player_left");

    public static final TETile ROBOT_NEUTRAL = new TETile('U', floorEdge, floorFill,
            "A scary robot!", "robot_neutral");
    public static final TETile ROBOT_ANGRY = new TETile('U', floorEdge, Color.red,
            "A scary robot!", "robot_angry");

    public static final TETile KEY = new TETile('❀', floorEdge, floorFill,
            "A key!", "key");
    public static final TETile SEEN_KEY = new TETile('❀', seenFloorEdge, seenFloorFill,
            "A key!", "seen_key");
    public static final TETile GHOST_KEY = new TETile('❀', Color.BLACK, Color.BLACK,
            "A key!", "ghost_key");

    public static final TETile WALL = new TETile('#', new Color(20, 20, 20),
            new Color(54, 54, 54), "The wall.", "wall");
    public static final TETile SEEN_WALL = new TETile('#', new Color(15, 15, 15),
            new Color(35, 35, 35), "The wall.", "seen_wall");
    public static final TETile FLOOR = new TETile('·', floorEdge, floorFill,
            "The floor.", "floor");
    public static final TETile SEEN_FLOOR = new TETile('·', seenFloorEdge, seenFloorFill,
            "The floor.", "seen_floor");
    public static final TETile GLOW_FLOOR = new TETile('·', floorEdge,
            new Color(242, 247, 157), "The floor.", "glow_floor");
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black,
            "");


    public static final TETile GRASS = new TETile('"', Color.green, Color.black,
            "grass");
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black,
            "water");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink,
            "flower");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "locked door");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "unlocked door");
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black,
            "sand");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black,
            "mountain");
    public static final TETile TREE = new TETile('♠', Color.green, Color.black,
            "tree");
}


