package byow.WorldGen;

import byow.TileEngine.TETile;

import java.util.Random;

/**
 * World generation interface.
 *
 * @author Nicholas Nguyen
 */
public interface MapGenerator {

    /** Generates a functional map using a provided width, height, and pseudo-random object,
     *  and sets it as its worldMap. */
    TETile[][] generate(int width, int height, Random rand);

    /** Retrieves the intMap */
    int[][] getIntMap();

    /** Returns a TETile[][] map using a intMap associated with the object's worldMap. Used
     *  for testing purposes. */
    TETile[][] getNumMap();

}
