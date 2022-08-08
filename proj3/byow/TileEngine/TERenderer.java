package byow.TileEngine;

import byow.Core.GameState;
import byow.Core.Leaderboard;
import byow.Core.Persistence;
import byow.gridsys.Grid;
import byow.gridsys.TilePoint;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Driver class for rendering the canvas. Based off the TERenderer provided by CS61BL, but with
 * heavy changes extending its use from a simple Tile Rendering engine to rendering the entire
 * canvas for each interface.
 *
 * @author Nicholas Nguyen
 */
public class TERenderer {
    /** Size of an individual drawn tile in pixels. */
    public static final int TILE_SIZE = 20;
    /** Number of while loop iterations per second, for rendering. */
    public static final int FRAME_RATE = 60;


    /** Canvas width in pixels. */
    private int width;
    /** Canvas height in pixels. */
    private int height;
    /** Map offset in tiles. */
    private int xOffset;
    /** Map offset in tiles. */
    private int yOffset;

    /**
     * Same functionality as the other initialization method. The only difference is that the xOff
     * and yOff parameters will change where the renderFrame method starts drawing. For example,
     * if you select w = 60, h = 30, xOff = 3, yOff = 4 and then call renderFrame with a
     * TETile[50][25] array, the renderer will leave 3 tiles blank on the left, 7 tiles blank
     * on the right, 4 tiles blank on the bottom, and 1 tile blank on the top.
     * @param w width of the window in pixels.
     * @param h height of the window in pixels.
     */
    public void initialize(int w, int h, int xOff, int yOff) {
        this.width = w;
        this.height = h;
        this.xOffset = xOff;
        this.yOffset = yOff;
        StdDraw.setCanvasSize(width, height);
        StdDraw.setXscale(-width / 2.0, width / 2.0);
        StdDraw.setYscale(-height / 2.0, height / 2.0);
        StdDraw.enableDoubleBuffering();
    }

    /**
     * Initializes StdDraw parameters and launches the StdDraw window. w and h are the
     * width and height of the world in number of tiles. If the TETile[][] array that you
     * pass to renderFrame is smaller than this, then extra blank space will be left
     * on the right and top edges of the frame. For example, if you select w = 60 and
     * h = 30, this method will create a 60 tile wide by 30 tile tall window. If
     * you then subsequently call renderFrame with a TETile[50][25] array, it will
     * leave 10 tiles blank on the right side and 5 tiles blank on the top side. If
     * you want to leave extra space on the left or bottom instead, use the other
     * initializatiom method.
     * @param w width of the window in pixels.
     * @param h height of the window in pixels.
     */
    public void initialize(int w, int h) {
        initialize(w, h, 0, 0);
    }



    // RENDER METHODS

    /**
     * Main menu interface.
     *
     * @return A meaningful character if one is clicked by the mouse in the appropriate box.
     * Otherwise, returns the char 0.
     */
    public char renderMainMenu() {
        StdDraw.clear(Color.BLACK); // Background color
        doSetFont("Monospaced", Font.BOLD, 50, new Color(203, 215, 33));
        StdDraw.text(0, 180, "Escape the Factory!");
        StdDraw.setPenColor(new Color(36, 204, 158)); // Text highlight color
        String[] optionsEntries = {
                "New Game (N)",
                "Load Game (L)",
                "Leaderboard (B)",
//                "Character Select (WIP) (S)",
                "Change Name (C)",
                "Help and Info (H)",
                "Quit Game (Q)"
        };
        Map<String, Boolean> optionsMap = new HashMap<>();
        int leaderboardSize = Persistence.readLeaderboard().getScores().size();
        for (int i = 0; i < optionsEntries.length; i++) {
            String txt = optionsEntries[i];
            if (txt.equals("Load Game (L)") && Persistence.readData() == null
                    || txt.equals("Leaderboard (B)") && leaderboardSize == 0) {
                optionsMap.put(txt, false);
            } else {
                int yCoord = 50 - 30 * i;
                optionsMap.put(txt, highlightWhenHovered(0, yCoord, 300, 15));
            }
        }
        if (StdDraw.isMousePressed()) {
            StdDraw.pause(200);
            for (String entry : optionsEntries) {
                if (optionsMap.get(entry)) {
                    return entry.charAt(entry.length() - 2);
                }
            }
        }
        doSetFont("SansSerif", Font.PLAIN, 20, Color.WHITE);
        for (int i = 0; i < optionsEntries.length; i++) {
            String txt = optionsEntries[i];
            int yCoord = 50 - 30 * i;
            if (txt.equals("Load Game (L)") && Persistence.readData() == null
                    || txt.equals("Leaderboard (B)") && leaderboardSize == 0) {
                StdDraw.setPenColor(Color.GRAY);
                StdDraw.text(0, yCoord, txt);
                StdDraw.setPenColor(Color.WHITE);
            } else {
                StdDraw.text(0, yCoord, txt);
            }
        }

        // Version num
        doSetFont("SansSerif", Font.PLAIN, 15, Color.WHITE);
        StdDraw.textRight(width / 2.0 - 10, -height / 2.0 + 10, "Version 1.0");

        StdDraw.show();
        StdDraw.pause(1000 / FRAME_RATE);
        return 0;
    }

    /**
     * Seed input interface.
     *
     * @param typed The current seed typed into the prompt, updating as characters are added
     *              or removed.
     * @return A character, if a button was clicked.
     */
    public char renderSeedInput(String typed) {
        typed = typed + "_";
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(new Color(36, 204, 158));
        boolean saveSeedHovered = highlightWhenHovered(0, -30, 70, 20);
        if (saveSeedHovered && StdDraw.isMousePressed()) {
            StdDraw.pause(300);
            clearKeys();
            return 'S';
        }
        doSetFont("SansSerif", Font.ITALIC, 30, Color.WHITE);
        StdDraw.text(0, 30, typed);
        doSetFont("SansSerif", Font.BOLD, 25, Color.WHITE);
        StdDraw.text(0, 100, "Input seed below.");
        StdDraw.text(0, -30, "Save (S)");
        StdDraw.show();
        StdDraw.pause(1000 / FRAME_RATE);
        return 0;
    }

    /**
     * Leaderboard interface.
     */
    public char renderLeaderboard(Leaderboard leaderboard) {
        StdDraw.clear(Color.BLACK); // Background color
        doSetFont("SansSerif", Font.BOLD, 40, Color.WHITE);
        StdDraw.text(0, 200, "LEADERBOARD");

        List<Leaderboard.Entry> entries = leaderboard.getScores();
        doSetFont("Serif", Font.PLAIN, 20, Color.WHITE);
        StdDraw.textRight(-200, 150, "Rank");
        StdDraw.textLeft(-170, 150, "Name");
        StdDraw.textLeft(100, 150, "Keys");
        StdDraw.textLeft(200, 150, "Turns");

        for (int i = 0; i < Math.min(entries.size(), 10); i++) {
            int yCoord = 115 - 34 * i;
            StdDraw.textLeft(-170, yCoord, entries.get(i).name());
            StdDraw.textLeft(100, yCoord, Integer.toString(entries.get(i).keys()));
            StdDraw.textLeft(200, yCoord, Integer.toString(entries.get(i).turns()));
        }
        for (int i = 0; i < 10; i++) {
            int yCoord = 115 - 34 * i;
            StdDraw.line(-250, yCoord + 17, 250, yCoord + 17);
            StdDraw.textRight(-200, yCoord, (i + 1) + ".");
        }

        StdDraw.setPenColor(new Color(36, 204, 158));
        double buttonH = - height / 2.0 + 30;
        boolean returnHovered = highlightWhenHovered(0, buttonH, 120, 20);
        if (returnHovered && StdDraw.isMousePressed()) {
            StdDraw.pause(200);
            return '\n';
        }
        doSetFont("SansSerif", Font.BOLD, 25, Color.WHITE);
        StdDraw.text(0, buttonH, "Go Back (Enter)");

        StdDraw.show();
        StdDraw.pause(1000 / FRAME_RATE);
        return 0;
    }

    /**
     * Name changer interface.
     *
     * @param name The current typed input.
     * @return A character if a button was clicked.
     */
    public char renderChangeName(String name) {
        name = name + "_";
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(new Color(36, 204, 158));
        boolean saveSeedHovered = highlightWhenHovered(0, -30, 120, 20);
        if (saveSeedHovered && StdDraw.isMousePressed()) {
            StdDraw.pause(200);
            return '\n';
        }
        doSetFont("SansSerif", Font.ITALIC, 30, Color.WHITE);
        StdDraw.text(0, 30, name);
        doSetFont("SansSerif", Font.BOLD, 25, Color.WHITE);
        StdDraw.text(0, 100, "Change your character's name.");
        StdDraw.text(0, -30, "Save Name (Enter)");
        StdDraw.show();
        StdDraw.pause(1000 / FRAME_RATE);
        return 0;
    }

    /**
     * Help interface.
     */
    public char renderHelp() {
        StdDraw.clear(Color.BLACK); // Background color
        String[] lines = {
                "Welcome to Escape the Dungeon!",
                "",
                "In this post-apocalyptic world, sentient Turing-complete robots have taken over",
                "the world, leaving you to be the last person on Earth. You've managed to survive",
                "the apocalypse due to being (luckily) cryofrozen for the past century. Somehow",
                "you've awoken from your long slumber, to find yourself in a dangerous factory",
                "guarded by deadly robots. To escape, you must find the five keys inside the",
                "factory, which combine to form a teleporter for you to time travel back to the",
                "past and rid these robots before they get the chance to destroy humanity.",
                "",
                "WASD to move, or click on a floor tile to move there. SPACE to wait. The robots act",
                "every time you move one square, so be careful! They will chase you down if they see",
                "you. Fortunately, robots do not see well as well in the dark as you. When a robot",
                "collides with you, it will explode, injuring you in the process. Good luck on",
                "your quest to save humanity.",
                "",
                "0 to toggle fog-of-war. 9 to toggle perspective. ESC or : to open pause menu."
        };
        doSetFont("Serif", Font.PLAIN, 20, Color.WHITE);
        for (int i = 0; i < lines.length; i++) {
            int yCoord = 220 - 25 * i;
            StdDraw.text(0, yCoord, lines[i]);
        }


        StdDraw.setPenColor(new Color(36, 204, 158));
        double buttonH = - height / 2.0 + 30;
        boolean returnHovered = highlightWhenHovered(0, buttonH, 120, 20);
        if (returnHovered && StdDraw.isMousePressed()) {
            StdDraw.pause(200);
            return '\n';
        }
        doSetFont("SansSerif", Font.BOLD, 25, Color.WHITE);
        StdDraw.text(0, buttonH, "Go Back (Enter)");

        StdDraw.show();
        StdDraw.pause(1000 / FRAME_RATE);
        return 0;
    }

    /**
     * Takes in a 2d array of TETile objects and renders the 2d array to the screen, starting from
     * xOffset and yOffset.
     *
     * If the array is an NxM array, then the element displayed at positions would be as follows,
     * given in units of tiles.
     *
     *              positions   xOffset |xOffset+1|xOffset+2| .... |xOffset+world.length
     *
     * startY+world[0].length   [0][M-1] | [1][M-1] | [2][M-1] | .... | [N-1][M-1]
     *                    ...    ......  |  ......  |  ......  | .... | ......
     *               startY+2    [0][2]  |  [1][2]  |  [2][2]  | .... | [N-1][2]
     *               startY+1    [0][1]  |  [1][1]  |  [2][1]  | .... | [N-1][1]
     *                 startY    [0][0]  |  [1][0]  |  [2][0]  | .... | [N-1][0]
     *
     * By varying xOffset, yOffset, and the size of the screen when initialized, you can leave
     * empty space in different places to leave room for other information, such as a GUI.
     * This method assumes that the xScale and yScale have been set such that the max x
     * value is the width of the screen in tiles, and the max y value is the height of
     * the screen in tiles.
     * @param gameState The current game being played.
     */
    public TilePoint renderGame(GameState gameState) {
        TilePoint mouseHover = null;
        TETile[][] world = gameState.getCurrMap();
        int numXTiles = world.length;
        int numYTiles = world[0].length;
        StdDraw.clear(new Color(0, 0, 0));
        Font font = new Font("Monaco", Font.BOLD, 14);
        StdDraw.setFont(font);
        for (int x = 0; x < numXTiles; x++) {
            for (int y = numYTiles - 1; y >= 0; y--) {
                if (world[x][y] == null) {
                    throw new IllegalArgumentException("Tile at position x=" + x + ", y=" + y
                            + " is null.");
                }
                if (gameState.doPerspectiveShift()) {
                    double dx = (x - numXTiles / 2.0 + gameState.getxOffsetShift());
                    double dy = (y - numYTiles / 2.0 + gameState.getyOffsetShift());
                    double xCoord = 0.75 * (dx + dy) * TILE_SIZE;
                    double yCoord = 0.5 * (dy - dx) * TILE_SIZE;
                    if (mouseIn(-width / 2.0, width / 2.0, -height / 2.0,
                            height / 2.0) && mouseInDiamond(xCoord, yCoord)) {
                        mouseHover = new TilePoint(x, y);
                    }
                    world[x][y].drawPerspective(xCoord, yCoord);
                } else {
                    int xCoord = (x - numXTiles / 2 + xOffset) * TILE_SIZE;
                    int yCoord = (y - numYTiles / 2 + yOffset) * TILE_SIZE;
                    if (mouseIn(-width / 2.0, width / 2.0, -height / 2.0,
                            height / 2.0) && mouseInSquare(xCoord, yCoord)) {
                        mouseHover = new TilePoint(x, y);
                    }
                    world[x][y].draw(xCoord, yCoord);
                }
            }
        }

        // Sidebar
        StdDraw.setPenColor(new Color(30, 30, 30));
        StdDraw.filledRectangle(width / 2.0 - 50, 0, 50, height / 2.0);
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(width / 2.0 - 100, 0, 2, height / 2.0);
        StdDraw.setPenColor(Color.LIGHT_GRAY);
        StdDraw.filledRectangle(width / 2.0 - 103, 0, 1, height / 2.0);
        doSetFont("Monaco", Font.PLAIN, 14, Color.WHITE);

        // Sidebar info
        double x = width / 2.0 - 50;
        double y = height / 2.0;
        StdDraw.text(x, y - 30, gameState.getName());
        StdDraw.text(x, y - 50, "HP: " + gameState.getHealth() + " / 3");
        StdDraw.text(x, y - 130, "Turn " + gameState.getTurn());
        if (mouseHover != null) {
            String description = Grid.tileAt(world, mouseHover).description();
            StdDraw.text(x, y - 200, description);
        }
        StdDraw.picture(x, y - 260, "byow/textures/key_clear.png");
        StdDraw.text(x, y - 280, "Keys Found:");
        StdDraw.text(x, y - 297, gameState.getKeysRetrieved() + " / 5");
        Date date = new Date();
        SimpleDateFormat topPatt = new SimpleDateFormat("h:m a");
        SimpleDateFormat botPatt = new SimpleDateFormat("M/d/y");
        StdDraw.text(x, -height / 2.0 + 37, topPatt.format(date));
        StdDraw.text(x, -height / 2.0 + 20, botPatt.format(date));

        StdDraw.show();
        StdDraw.pause(1000 / FRAME_RATE);
        if (StdDraw.isMousePressed() && mouseHover != null
                && Grid.tileAt(world, mouseHover).valid()) {
            return mouseHover;
        }
        return null;
    }

    /**
     * Renders a pop-up to be shown mid-game. Does not wipe the animation board, and pauses at
     * this screen for 1 second.
     *
     * @param message The message to be displayed on the screen.
     */
    public void renderPopUp(String message) {
        StdDraw.setPenColor(new Color(36, 204, 158));
        StdDraw.filledRectangle(0, 200, 300, 20);
        doSetFont("Monospaced", Font.BOLD, 30, Color.WHITE);
        StdDraw.text(0, 200, message);
        StdDraw.show();
        StdDraw.pause(1500);
        clearKeys();
    }

    /**
     * In-game interface that confirms if the user wants to quit.
     */
    public char renderPauseMenu(long seed) {
        StdDraw.clear(Color.BLACK); // Background color
        doSetFont("SansSerif", Font.BOLD, 40, Color.WHITE);
        StdDraw.text(0, 190, "GAME PAUSED");
        StdDraw.setPenColor(new Color(36, 204, 158)); // Text highlight color
        String[] optionsEntries = {
                "Return to game (R)",
                "Quit to main menu (M)",
                "Quit to desktop (Q)",
                "",
                "Toggle Perspective Shift (9)",
                "Toggle Fog-of-War (0)",
        };
        Map<String, Boolean> optionsMap = new HashMap<>();
        for (int i = 0; i < optionsEntries.length; i++) {
            if (optionsEntries[i].equals("")) {
                optionsMap.put(optionsEntries[i], false);
            }
            int yCoord = 50 - 30 * i;
            optionsMap.put(optionsEntries[i],
                    highlightWhenHovered(0, yCoord, 200, 15));
        }
        if (StdDraw.isMousePressed()) {
            StdDraw.pause(200);
            for (String entry : optionsEntries) {
                if (optionsMap.get(entry)) {
                    return entry.charAt(entry.length() - 2);
                }
            }
        }
        doSetFont("SansSerif", Font.PLAIN, 20, Color.WHITE);
        for (int i = 0; i < optionsEntries.length; i++) {
            int yCoord = 50 - 30 * i;
            StdDraw.text(0, yCoord, optionsEntries[i]);
        }

        doSetFont("SansSerif", Font.PLAIN, 15, Color.WHITE);
        StdDraw.textRight(width / 2.0 - 10, -height / 2.0 + 10, "Seed: " + seed);

        StdDraw.show();
        StdDraw.pause(1000 / FRAME_RATE);
        return 0;
    }

    /**
     * Game over interface.
     *
     * @return A character if a button was clicked.
     */
    public char renderGameOver() {
        StdDraw.clear(Color.BLACK); // Background color
        doSetFont("SansSerif", Font.BOLD, 40, Color.WHITE);
        StdDraw.text(0, 190, "GAME OVER");
        StdDraw.setPenColor(new Color(36, 204, 158)); // Text highlight color
        String[] optionsEntries = {
                "Quit to main menu (M)",
                "Quit to desktop (Q)"
        };
        Map<String, Boolean> optionsMap = new HashMap<>();
        for (int i = 0; i < optionsEntries.length; i++) {
            int yCoord = 50 - 30 * i;
            optionsMap.put(optionsEntries[i],
                    highlightWhenHovered(0, yCoord, 200, 15));
        }
        if (StdDraw.isMousePressed()) {
            StdDraw.pause(200);
            for (String entry : optionsEntries) {
                if (optionsMap.get(entry)) {
                    return entry.charAt(entry.length() - 2);
                }
            }
        }
        doSetFont("SansSerif", Font.PLAIN, 20, Color.WHITE);
        for (int i = 0; i < optionsEntries.length; i++) {
            int yCoord = 50 - 30 * i;
            StdDraw.text(0, yCoord, optionsEntries[i]);
        }

        StdDraw.show();
        StdDraw.pause(1000 / FRAME_RATE);
        return 0;
    }



    // HELPER METHODS

    /**
     * Useful helper method that returns whether the mouse is currently inside a rectangle bounded
     * by the inputs.
     *
     * @param lowerX lower bound of x coordinate, in pixels
     * @param upperX upper bound of x coordinate, in pixels
     * @param lowerY lower bound of y coordinate, in pixels
     * @param upperY upper bound of y coordinate, in pixels
     * @return whether the mouse is currently inside the given bounds.
     */
    private boolean mouseIn(double lowerX, double upperX, double lowerY, double upperY) {
        double curX = StdDraw.mouseX();
        double curY = StdDraw.mouseY();
        return curX >= lowerX && curX <= upperX && curY >= lowerY && curY <= upperY;
    }

    /** Specific usage of mouseIn for squares. */
    private boolean mouseInSquare(double x, double y) {
        return mouseIn(x - TILE_SIZE / 2.0, x + TILE_SIZE / 2.0,
                y - TILE_SIZE / 2.0, y + TILE_SIZE / 2.0);
    }

    /** Different implementation of mouseIn for diamonds. */
    private boolean mouseInDiamond(double xCoord, double yCoord) {
        double mX = StdDraw.mouseX();
        double mY = StdDraw.mouseY();
        double leftX = xCoord - TILE_SIZE / 2.0;
        double rightX = xCoord + TILE_SIZE / 2.0;
        double slope = 2.0 / 3.0;
        UnaryOperator<Double> tr = x -> - slope * (x - rightX) + yCoord;
        UnaryOperator<Double> br = x -> slope * (x - rightX) + yCoord;
        UnaryOperator<Double> tl = x -> slope * (x - leftX) + yCoord;
        UnaryOperator<Double> bl = x -> - slope * (x - leftX) + yCoord;
        return mY <= Math.min(tr.apply(mX), tl.apply(mX))
                && mY >= Math.max(br.apply(mX), br.apply(mX));
    }

    /**
     * Helper method that highlights a rectangular box only if the mouse is currently hovered over
     * where it would be.
     *
     * @param x x-coordinate of center of highlight box
     * @param y y-coordinate of center of highlight box
     * @param halfWidth distance from center to side of box
     * @param halfHeight distance from center to top of box
     * @return Whether the mouse is currently inside the highlight box.
     */
    private boolean highlightWhenHovered(double x, double y,
                                         double halfWidth, double halfHeight) {
        if (mouseIn(x - halfWidth, x + halfWidth,
                y - halfHeight, y + halfHeight)) {
            StdDraw.filledRectangle(x, y, halfWidth, halfHeight);
            return true;
        }
        return false;
    }

    /** Helper function for font setting in concise manner. */
    private void doSetFont(String fontName, int fontStyle, int size, Color color) {
        Font font = new Font(fontName, fontStyle, size);
        StdDraw.setFont(font);
        StdDraw.setPenColor(color);
    }

    /** Helper method to clear the keyTyped queue. */
    public void clearKeys() {
        while (StdDraw.hasNextKeyTyped()) {
            StdDraw.nextKeyTyped();
        }
    }

    public void setXOffset(int xOff) {
        xOffset = xOff;
    }

    public void setYOffset(int yOff) {
        yOffset = yOff;
    }

    public void changeXOffset(int change) {
        xOffset += change;
    }

    public void changeYOffset(int change) {
        yOffset += change;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }
}
