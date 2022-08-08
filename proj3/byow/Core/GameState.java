package byow.Core;

import byow.creatures.Creature;
import byow.creatures.Key;
import byow.creatures.Player;
import byow.creatures.Robot;
import byow.gridsys.Grid;
import byow.gridsys.TilePoint;
import byow.input.InputSource;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.*;

/**
 * GameState class of BYOW. Stores all data of a save, and directs the program to run through
 * various while loop methods depending on the current state of the game. Every field except for
 * ter, inputSource, and doRender are saved when a game is quit for whatever reason, while these
 * three fields are transient because they may change with next program run.
 *
 * @author Nicholas Nguyen
 */
public class GameState implements Serializable {

    /** Number of enemies in the map. */
    public static final int ENEMIES_COUNT = 5;

    /** Renderer used for the current session's canvas. Regenerated every time a program is run,
     *  and uses local fields from the Engine class to set up the canvas via ter.initialize(). */
    private transient TERenderer ter;
    /** The type of character input being given to the engine. */
    private transient InputSource inputSource;
    /** Determines whether the engine should render the canvas. Set to false for autograder. */
    private transient boolean doRender;
    /** Indicates program behavior after the game exits and saves. */
    private transient boolean startNewGame = false;

    /** Width of the map's grid; in other worlds, the number of tiles in a row. */
    private final int width;
    /** Height of the map's grid; in other worlds, ther number of tiles in a column. */
    private final int height;
    /** Underlying information about the generated world. */
    private final Grid worldGrid;
    /** The current actual map being displayed. Differs from data.worldMap, which is a reference
     *  map that the code uses to determine how currMap looks or changes. */
    private final Grid currGrid;

    /** x offset of the map from the middle. */
    private int xOffset;
    /** y offset of the map from the middle. */
    private int yOffset;
    /** x offset of the map from the middle, in perspective shift. */
    private int xOffsetShift;
    /** y offset of the map from the middle, in perspective shift. */
    private int yOffsetShift;

    /** Player name. */
    private final String name;
    /** Character type, chosen at character select but is 0 by default. */
    private final int character;
    /** Pseudo-random object for use in determining random events in a reproducable manner. */
    private final Random random;
    /** Seed of pseudo-random object for player if they want it. */
    private final long seed;

    /** Turn counter. Increases whenever the player performs an action. */
    private int turn;
    /** Player health. */
    private int health;
    /** Keys retrieved. */
    private int keysRetrieved;

    /** Player character object. */
    private Player player;
    /** List of robots. */
    private final List<Robot> robots;
    /** List of all Key objects. */
    private final List<Key> keys;

    /** Messages to be displayed when hurt. */
    private final List<String> harmMessages = new ArrayList<>(List.of(
            "The robot hurt you.",
            "Ouch! Be careful!",
            "You've been brutalized..."
    ));
    /** Messages to be displayed when finding a key. */
    private final Queue<String> keyMessages = new LinkedList<>(List.of(
            "You found a key!",
            "You found a key!",
            "More than halfway!",
            "One more key needed!",
            "The keys open a portal. You win!"
    ));

    /** Toggle-able setting that determines whether fog-of-war is on or off in rendering. */
    private boolean doFogOfWar = true;
    /** Tiles that have been seen by the player. */
    private final HashSet<TilePoint> visited;
    /** Toggle-able setting that determines the perspective (2D for false and 2.5D for true). */
    private boolean doPerspectiveShift = false;

    public GameState(TERenderer renderer, InputSource input, boolean render, TETile[][] map,
                     Random rand, long s, int c, String n) {
        ter = renderer;
        inputSource = input;
        doRender = render;
        worldGrid = new Grid(map);
        currGrid = new Grid(new TETile[map.length][]);
        width = map.length;
        height = width > 0 ? map[0].length : 0;
        copyDataMap();
        random = rand;
        seed = s;
        robots = new ArrayList<>();
        keys = new ArrayList<>();
        character = c;
        name = n;
        turn = 0;
        health = 3;
        keysRetrieved = 0;
        visited = new HashSet<>();
    }

    public void updateOldGameState(TERenderer renderer, InputSource input, boolean render) {
        ter = renderer;
        ter.changeXOffset(xOffset);
        ter.changeYOffset(yOffset);
        inputSource = input;
        doRender = render;
    }

    public void setUpWorld() {
        TilePoint playerCoord = findEmptySpot();
        player = new Player(playerCoord, character);

        for (int i = 0; i < ENEMIES_COUNT; i++) {
            robots.add(new Robot(findEmptySpotForNPC()));
        }

        for (int i = 0; i < 5; i++) {
            keys.add(new Key(findEmptySpotForNPC()));
        }

        changeXOffset(width / 2 - playerCoord.x);
        changeYOffset(height / 2 - playerCoord.y);
        xOffsetShift = xOffset;
        yOffsetShift = yOffset;
    }

    /**
     * Repeatedly generates a random x and y coordinate until one matches a floor tile that is
     * not occupied by another creature.
     *
     * @return The TilePoint coordinates of the empty spot.
     */
    public TilePoint findEmptySpot() {
        int testX;
        int testY;
        boolean validate;
        do {
            testX = RandomUtils.uniform(random, width - 1);
            testY = RandomUtils.uniform(random, height - 1);
            if (worldGrid.getMap()[testX][testY].character() != '·') {
                validate = false;
                continue;
            }
            validate = true;
            for (Creature creature : robots) {
                if (creature.getX() == testX && creature.getY() == testY) {
                    validate = false;
                    break;
                }
            }
        } while (!validate);
        return new TilePoint(testX, testY);
    }

    /**
     * Extended routine for findEmptySpot that also ensures the coordinate is not within a certain
     * distance of the player.
     */
    public TilePoint findEmptySpotForNPC() {
        TETile[][] worldMap = worldGrid.getMap();
        int spawnDist = Math.min(worldMap.length, worldMap[0].length) / 4;
        TilePoint cur;
        do {
            cur = findEmptySpot();
        } while (cur.distance(player.getLoc()) < spawnDist);
        return cur;
    }

    /**
     * Driver method of the bulk of the game. Initializes the map, then runs through a while loop
     * checking for user input and rendering the canvas. Player movements advance the turn counter
     * by 1, allowing monsters to move one step after the player moves.
     */
    public void runGame() {
        updateMap();
        boolean doQuit = false;

        while (inputSource.possibleNextInput() && !doQuit) {
            if (doRender) {
                TilePoint clickedDest = ter.renderGame(this);
                if (clickedDest != null) {
                    moveAvatar(clickedDest);
                    ter.clearKeys();
                }
            }
            if (inputSource.hasNextKey()) {
                char nextInputKey = inputSource.getNextKey();
                switch (nextInputKey) {
                    case 'W' -> moveAvatar(Grid.NORTH);
                    case 'A' -> moveAvatar(Grid.WEST);
                    case 'S' -> moveAvatar(Grid.SOUTH);
                    case 'D' -> moveAvatar(Grid.EAST);
                    case ' ' -> {
                        robotsMove();
                        updateMap();
                        turn++;
                    }

                    case '9' -> togglePerspective();
                    case '0' -> toggleFogOfWar();

                    case ':', KeyEvent.VK_ESCAPE -> {
                        int result = pauseMenu();
                        switch (result) {
                            case 'Q' -> doQuit = true;
                            case 'M' -> {
                                doQuit = true;
                                startNewGame = true;
                            }
                            case '9' -> togglePerspective();
                            case '0' -> toggleFogOfWar();
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            if (health <= 0 || keysRetrieved == 5) {
                startNewGame = gameOver();
                doQuit = true;
            }
        }
    }

    private void copyDataMap() {
        TETile[][] newMap = new TETile[width][height];
        for (int i = 0; i < width; i++) {
            newMap[i] = Arrays.copyOf(worldGrid.getMap()[i], height);
        }
        currGrid.setMap(newMap);
    }

    /**
     * Moves the avatar one tile in the given direction, and updates data.charX and data.charY
     *
     * @param dir The direction the avatar moves in. 0 = north, 1 = west, 2 = south, 3 = east
     */
    private void moveAvatar(int dir) {

        TilePoint oldLoc = player.getLoc();
        TilePoint newLoc = new TilePoint(oldLoc);
        newLoc.moveInDir(dir, 1);

        if (worldGrid.getTile(newLoc).valid()) {
            // Adjust canvas offset if the avatar is near the edge of the canvas
            if (doPerspectiveShift) {
                fixOffsetPerspective(dir, newLoc);
            } else {
                fixOffset(dir, newLoc);
            }

            // Replaces old tile.
            player.setLoc(new TilePoint(newLoc));
            currGrid.setTile(oldLoc, Tileset.FLOOR);

            // Collision checking
            switch (currGrid.getTile(newLoc).character()) {
                case 'U' -> robotCollision(newLoc);
                case '❀' -> keyCollision(newLoc);
                default -> { }
            }

            robotsMove();
            updateMap();
            turn++;
        }

    }

    /**
     * Moves avatar to the listed point and adjusts for offset.
     *
     * @param destination Point to move the avatar to.
     */
    private void moveAvatar(TilePoint destination) {
        List<TilePoint> path = worldGrid.pathfinder(player.getLoc(), destination);
        path.remove(0);
        for (TilePoint tile : path) {
            currGrid.setTile(tile, Tileset.GLOW_FLOOR);
        }
        for (TilePoint tile : path) {
            int dir = player.getLoc().getDirTo(tile);
            moveAvatar(dir);
            if (doRender) {
                ter.renderGame(this);
                StdDraw.pause(50);
            }
        }
    }

    /** Offset-checking for a 2D map. */
    private void fixOffset(int dir, TilePoint newLoc) {
        int tileSize = TERenderer.TILE_SIZE;
        int canvasWidth = Engine.CANVAS_WIDTH;
        int canvasHeight = Engine.CANVAS_HEIGHT;

        // Change avatar direction if avatar moves left/right
        switch (dir) {
            case Grid.EAST -> player.lookRight();
            case Grid.WEST -> player.lookLeft();
            default -> { }
        }

        // Adjust for offset
        if (newLoc.x < width / 2 - ter.getXOffset() - (canvasWidth / tileSize) / 6) {
            changeXOffset(1);
        } else if (newLoc.x > width / 2 - ter.getXOffset()
                + ((canvasWidth - 100) / tileSize) / 6) {
            changeXOffset(-1);
        } else if (newLoc.y < height / 2 - ter.getYOffset() - (canvasHeight / tileSize) / 6) {
            changeYOffset(1);
        } else if (newLoc.y > height / 2 - ter.getYOffset() + (canvasHeight / tileSize) / 6) {
            changeYOffset(-1);
        }
    }

    private void fixOffsetPerspective(int dir, TilePoint newLoc) {
        int tileSize = TERenderer.TILE_SIZE;
        int canvasWidth = Engine.CANVAS_WIDTH;
        int canvasHeight = Engine.CANVAS_HEIGHT;

        // Change avatar direction if avatar moves left/right
        switch (dir) {
            case Grid.NORTH, Grid.EAST -> player.lookRight();
            case Grid.WEST, Grid.SOUTH -> player.lookLeft();
            default -> { }
        }

        // Adjust offset
        double dx = (newLoc.x - width / 2.0 + xOffsetShift);
        double dy = (newLoc.y - height / 2.0 + yOffsetShift);
        double xCoord = 0.75 * (dx + dy) * tileSize;
        double yCoord = 0.5 * (dy - dx) * tileSize;
        if (xCoord > (canvasWidth - 100) / 6.0 || xCoord < -canvasWidth / 6.0
            || yCoord > canvasHeight / 6.0 || yCoord < -canvasHeight / 6.0) {
            switch (dir) {
                case Grid.NORTH -> yOffsetShift--;
                case Grid.WEST -> xOffsetShift++;
                case Grid.SOUTH -> yOffsetShift++;
                case Grid.EAST -> xOffsetShift--;
                default -> { }
            }
        }
    }

    /** Resolves a robot-player collision, respawning the robot after health depletes. */
    private void robotCollision(TilePoint tile) {
        Robot explodingBot = null;
        for (Robot robot : robots) {
            if (robot.getLoc().equals(tile)) {
                explodingBot = robot;
            }
        }
        assert explodingBot != null;
        if (doRender) {
            ter.renderPopUp(harmMessages.get(Math.min(3 - health, 2)));
        }
        health--;
        robots.remove(explodingBot);
        robots.add(new Robot(findEmptySpotForNPC()));
    }

    /** Resolves key-player collision. */
    private void keyCollision(TilePoint tile) {
        Key foundKey = null;
        for (Key key : keys) {
            if (key.getLoc().equals(tile)) {
                foundKey = key;
            }
        }
        assert foundKey != null;
        if (doRender) {
            ter.renderPopUp(keyMessages.poll());
        }
        if (health < 3) {
            health++;
        }
        keysRetrieved++;
        keys.remove(foundKey);
    }

    /** Iterates through every robot's actions, then checks for player collision. */
    private void robotsMove() {
        Robot toReplace = null;
        for (Robot robot : robots) {
            if (RandomUtils.uniform(random, 4) == 0) {
                continue;
            }
            TilePoint oldTile = new TilePoint(robot.getLoc());
            boolean res = robot.action(this);
            currGrid.setTile(oldTile, Tileset.FLOOR);
            if (res) {
                toReplace = robot;
            }
        }
        if (toReplace != null) {
            robotCollision(toReplace.getLoc());
        }

        // Shuffle robot positions
        RandomUtils.shuffle(random, robots);
    }

    /** Updates the map, adjusting for fog-of-way if enabled. */
    public void updateMap() {
        if (doFogOfWar) {
            copyDataMap();
            updateCreatures();
            updateFogOfWar();
        } else {
            updateCreatures();
        }
    }

    /** Sets every creature to its updated location on the map. */
    public void updateCreatures() {
        for (Robot robot : robots) {
            currGrid.setTile(robot);
        }
        for (Key key : keys) {
            currGrid.setTile(key);
        }
        currGrid.setTile(player);
    }

    /**
     * Turns currMap into a grid of Tileset.NOTHING except for select tiles that are a RANGE
     * distance away from the avatar tile.
     *
     * For now, has no pathfinding functionalities, meaning the avatar can see through walls.
     */
    private void updateFogOfWar() {
        TilePoint playerLoc = player.getLoc();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                char tileChar = currGrid.getTile(x, y).character();
                if (tileChar == ' ') {
                    continue;
                }
                TilePoint tile = new TilePoint(x, y);
                if (playerLoc.distance(tile) > 15 || !worldGrid.sight(playerLoc, tile)) {
                    if (visited.contains(tile)) {
                        switch (tileChar) {
                            case '#' -> currGrid.setTile(tile, Tileset.SEEN_WALL);
                            case '❀' -> currGrid.setTile(tile, Tileset.SEEN_KEY);
                            default -> currGrid.setTile(tile, Tileset.SEEN_FLOOR);
                        }
                    } else if (tileChar == '❀') {
                        currGrid.setTile(tile, Tileset.GHOST_KEY);
                    } else {
                        currGrid.setTile(tile, Tileset.NOTHING);
                    }
                } else {
                    visited.add(tile);
                }
            }
        }
    }

    /** Menu page activated when ':' is pressed. Lets the user confirm a session quit by pressing
     *  'W'. */
    private char pauseMenu() {
        while (inputSource.possibleNextInput()) {
            char nextKey = 0;
            if (doRender) {
                nextKey = ter.renderPauseMenu(seed);
            }
            if (inputSource.hasNextKey()) {
                nextKey = inputSource.getNextKey();
            }
            switch (nextKey) {
                case 'Q', 'M', ':', 'R', KeyEvent.VK_ESCAPE, '9', '0' -> {
                    return nextKey;
                }
                default -> { }
            }
        }
        return 0;
    }

    private boolean gameOver() {
        while (inputSource.possibleNextInput()) {
            char nextKey = 0;
            if (doRender) {
                nextKey = ter.renderGameOver();
            }
            if (inputSource.hasNextKey()) {
                nextKey = inputSource.getNextKey();
            }
            switch (nextKey) {
                case 'M': return true;
                case 'Q': return false;
                default: break;
            }
        }
        return false;
    }

    // SETTERS AND GETTERS

    /** Changes the xOffset field for both the GameState and TERenderer objects. */
    public void changeXOffset(int xOff) {
        this.xOffset += xOff;
        ter.changeXOffset(xOff);
    }

    /** Changes the yOffset field for both the GameState and TERenderer objects. */
    public void changeYOffset(int yOff) {
        this.yOffset += yOff;
        ter.changeYOffset(yOff);
    }

    /** Toggles fog-of-war. */
    public void toggleFogOfWar() {
        doFogOfWar = !doFogOfWar;
        if (doFogOfWar) {
            updateFogOfWar();
        } else {
            copyDataMap();
            updateCreatures();
        }
    }

    /** Toggles perspective shift. */
    public void togglePerspective() {
        int newOffsetX = width / 2 - player.getX();
        int newOffsetY = height / 2 - player.getY();
        doPerspectiveShift = !doPerspectiveShift;
        if (doPerspectiveShift) {
            xOffsetShift = newOffsetX;
            yOffsetShift = newOffsetY;
        } else {
            xOffset = newOffsetX;
            yOffset = newOffsetY;
            ter.setXOffset(newOffsetX);
            ter.setYOffset(newOffsetY);
        }
        if (doFogOfWar) {
            updateFogOfWar();
        } else {
            copyDataMap();
            updateCreatures();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public int getTurn() {
        return turn;
    }

    public int getHealth() {
        return health;
    }

    public Grid getWorldGrid() {
        return worldGrid;
    }

    public Grid getCurrGrid() {
        return currGrid;
    }

    public TETile[][] getCurrMap() {
        return currGrid.getMap();
    }

    public TETile[][] getWorldMap() {
        return worldGrid.getMap();
    }

    public List<Robot> getRobots() {
        return robots;
    }

    public int getKeysRetrieved() {
        return keysRetrieved;
    }

    public Random getRandom() {
        return random;
    }

    public int getxOffsetShift() {
        return xOffsetShift;
    }

    public int getyOffsetShift() {
        return yOffsetShift;
    }

    public boolean doPerspectiveShift() {
        return doPerspectiveShift;
    }

    public boolean isStartNewGame() {
        return startNewGame;
    }

    public boolean isGameFinished() {
        return health <= 0 || keysRetrieved == 5;
    }

    public Leaderboard.Entry getGameScore() {
        return new Leaderboard.Entry(name, keysRetrieved, turn);
    }
}
