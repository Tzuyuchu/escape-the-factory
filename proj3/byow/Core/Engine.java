package byow.Core;

import byow.creatures.Player;
import byow.input.InputSource;
import byow.input.KeyboardInputSource;
import byow.input.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.WorldGen.MSTMapGenerator;
import byow.WorldGen.MapGenerator;

import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * Main menu class of BYOW. Finds a way to retrieve a GameState object, either by loading or
 * generating a new world, then runs this GameState, passing in relevant input such as the method of
 * input and whether the program should render via StdDraw. Does not actually do any drawing itself,
 * as these rendering functions are passed to TERenderer.
 *
 * @author Nicholas Nguyen
 */
public class Engine {

    /**
     * Map generation algorithm to be used for generating a new map via the MapGenerator interface.
     * Use with MSTMapGenerator() or RFMapGenerator().
     */
    public static final MapGenerator MAP_ALGORITHM = new MSTMapGenerator();

    /** Width of the canvas in pixels. */
    public static final int CANVAS_WIDTH = 1050;
    /** Height of the canvas in pixels. */
    public static final int CANVAS_HEIGHT = 550;
    /** Width of the map in tiles. */
    public static final int MAP_WIDTH = 60;
    /** Height of the map in tiles. */
    public static final int MAP_HEIGHT = 60;

    /** Renderer used for the current session's canvas. Regenerated every time a program is run,
     *  and uses local fields from the Engine class to set up the canvas via ter.initialize(). */
    TERenderer ter = new TERenderer();
    /** The type of character input being given to the engine. */
    private InputSource input;
    /** Determines whether the engine should render the canvas. Set to false for autograder. */
    private boolean render;

    /** Leaderboard object. It is null by default, until it is read or created. */
    private Leaderboard leaderboard;

    /**
     * Method used for exploring a fresh world. This method initializes the renderer, then calls
     * runMainMenu() which starts the game program. After a quit happens, the program attempts to
     * write necessary data to file, then exits with exit code 0.
     */
    public void interactWithKeyboard() {
        if (!ter.isInitialized()) {
            ter.initialize(CANVAS_WIDTH, CANVAS_HEIGHT);
            input = new KeyboardInputSource();
            render = true;
        }
        GameState game = doMainMenu();
        if (game != null) {
            game.runGame();
            if (game.isGameFinished()) {
                Persistence.eraseData();
                updateLeaderboard(game);
            } else {
                Persistence.writeData(game);
            }
            if (game.isStartNewGame()) {
                interactWithKeyboard();
            }
        }
        System.exit(0);
    }

    /**
     * Method used for autograding and testing code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Unlike interactWithKeyboard(), this method does not use the renderer, so it passes false
     * for the render argument into runMainMenu().
     *
     * @param inputString the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String inputString) {
        input = new StringInputDevice(inputString);
        render = false;
        GameState game = doMainMenu();
        if (game != null) {
            game.runGame();
            if (game.isGameFinished()) {
                Persistence.eraseData();
                updateLeaderboard(game);
            } else {
                Persistence.writeData(game);
            }
            return game.getWorldMap();
        }
        return null;
    }



    /**
     * Main menu method. Runs a while loop until no more input is possible or a valid input is
     * entered. Each iteration, renders the main menu interface if render is true, then checks
     * if a key has been inputted. Ultimately creates the GameState object and returns it.
     *
     * @return GameState object, which either was created or loaded.
     */
    public GameState doMainMenu() {
        int character = Player.AVATAR1;
        leaderboard = Persistence.readLeaderboard();
        String name = leaderboard.savedName;
        while (input.possibleNextInput()) {
            char nextKey = 0;
            if (render) {
                nextKey = ter.renderMainMenu();
            }
            if (input.hasNextKey() && nextKey == 0) {
                nextKey = input.getNextKey();
            }
            switch (nextKey) {
                case 'N' -> {
                    return generateWorld(character, name);
                }
                case 'L' -> {
                    return loadFromFile();
                }
                case 'B' -> showLeaderboard();
                case 'S' -> { /* Char select*/ }
                case 'C' -> name = changeName(name);
                case 'H' -> help();
                case 'Q' -> {
                    return null;
                }
                default -> { }
            }
        }
        return null;
    }

    /**
     * New World method which is run when the New World (N) option is selected. Allows the user to
     * input a seed, or, if left empty, generates a random long to see the Random object. Sets up
     * the world, then returns the GameState.
     *
     * @return Created GameState object with a generated world.
     */
    public GameState generateWorld(int character, String name) {
        StringBuilder seedBuilder = new StringBuilder();
        while (input.possibleNextInput()) {
            char nextInputKey = 0;
            if (render) {
                nextInputKey = ter.renderSeedInput(seedBuilder.toString());
            }
            if (input.hasNextKey()) {
                nextInputKey = input.getNextKey();
            }
            if (nextInputKey == 'S') {
                break;
            } else if (Character.isDigit(nextInputKey)) {
                seedBuilder.append(nextInputKey);
            } else if (nextInputKey == '\b' && seedBuilder.length() > 0) {
                seedBuilder.deleteCharAt(seedBuilder.length() - 1);
            }
        }
        long seed = seedBuilder.length() == 0 ? Math.abs((new Random()).nextLong())
                : Long.parseLong(seedBuilder.toString());
        Random random = new Random(seed);
        TETile[][] worldMap = MAP_ALGORITHM.generate(MAP_WIDTH, MAP_HEIGHT, random);
        GameState game = new GameState(ter, input, render, worldMap, random, seed, character, name);
        game.setUpWorld();
        return game;
    }

    /**
     * Attempts to read the data from file using the Persistence class, then updates it with the
     * current session's input, render, and TERenderer.
     *
     * @return GameState object, which can be null if no file exists.
     */
    public GameState loadFromFile() {
        GameState oldGameState = Persistence.readData();
        if (oldGameState != null) {
            oldGameState.updateOldGameState(ter, input, render);
            return oldGameState;
        }
        return null;
    }

    public void updateLeaderboard(GameState game) {
        leaderboard.addScore(game.getGameScore());
        leaderboard.savedName = game.getName();
        Persistence.writeData(leaderboard);
    }

    public void showLeaderboard() {
        if (leaderboard.getScores().size() == 0) {
            return;
        }
        while (input.possibleNextInput()) {
            char nextKey = 0;
            if (render) {
                nextKey = ter.renderLeaderboard(leaderboard);
            }
            if (input.hasNextKey()) {
                nextKey = input.getNextKey();
            }
            switch (nextKey) {
                case '\n', KeyEvent.VK_ESCAPE: return;
                default: break;
            }
        }
    }

    /**
     * Name changer interface.
     *
     * @param name The most currently saved name on file.
     * @return The new name.
     */
    public String changeName(String name) {
        StringBuilder newName = new StringBuilder(name);
        while (input.possibleNextInput()) {
            char nextKey = 0;
            if (render) {
                nextKey = ter.renderChangeName(newName.toString());
            }
            if (input.hasNextKey()) {
                nextKey = input.getNextKey();
            }
            if (nextKey == '\n') {
                return newName.toString();
            } else if (Character.isLetterOrDigit(nextKey) || Character.isWhitespace(nextKey)) {
                newName.append(nextKey);
            } else if (nextKey == '\b' && newName.length() > 0) {
                newName.deleteCharAt(newName.length() - 1);
            }
        }
        if (newName.length() == 0) {
            return name;
        }
        leaderboard.savedName = newName.toString();
        return newName.toString();
    }

    /**
     * Help and Info interface.
     */
    public void help() {
        while (input.possibleNextInput()) {
            char nextKey = 0;
            if (render) {
                nextKey = ter.renderHelp();
            }
            if (input.hasNextKey()) {
                nextKey = input.getNextKey();
            }
            switch (nextKey) {
                case '\n', KeyEvent.VK_ESCAPE: return;
                default: break;
            }
        }
    }
}
