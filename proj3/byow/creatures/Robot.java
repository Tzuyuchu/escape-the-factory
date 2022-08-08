package byow.creatures;

import byow.Core.GameState;
import byow.Core.RandomUtils;
import byow.TileEngine.Tileset;
import byow.gridsys.Grid;
import byow.gridsys.TilePoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Enemy class for BYOW. The Robot uses both fog-of-war and pathfinding to wander the map or track
 * down the player.
 *
 * @author Nicholas Nguyen
 */
public class Robot extends Creature {

    /** Current queue of robot's actions. */
    private final Queue<TilePoint> moveQueue;

    public Robot(int initialX, int initialY) {
        super(initialX, initialY, Tileset.ROBOT_NEUTRAL);
        moveQueue = new LinkedList<>();
    }

    public Robot(TilePoint initialPoint) {
        super(initialPoint, Tileset.ROBOT_NEUTRAL);
        moveQueue = new LinkedList<>();
    }

    /**
     * Called once every time the player moves, and controls the robot's movements. The robot first
     * checks to see if the player is in sight. If it sees the player, it then sets its Queue to a
     * new pathfind towards the player. The robot then checks if it has any movement in its queue,
     * and if so, it follows the next command.
     *
     * If the queue is empty, the robot takes a random valid move, or stays in place.
     *
     * @param game Current game and its involved data.
     */
    public boolean action(GameState game) {
        Grid gameGrid = game.getWorldGrid();
        TilePoint playerLoc = game.getPlayer().getLoc();

        // Sets up queue if the player is in sight, otherwise, turn neutral.
        if (getLoc().distance(playerLoc) <= 10 && gameGrid.sight(getLoc(), playerLoc)) {
            setAvatar(Tileset.ROBOT_ANGRY);
            List<TilePoint> path = gameGrid.pathfinder(getLoc(), playerLoc);
            if (path.size() > 0) {
                path.remove(0);
            }
            moveQueue.clear();
            for (TilePoint tile : path) {
                moveQueue.offer(tile);
            }
        } else {
            setAvatar(Tileset.ROBOT_NEUTRAL);
        }

        // Makes movement
        if (moveQueue.isEmpty()) {
            ArrayList<Integer> possibleMoves = new ArrayList<>();
            possibleMoves.add(-1);

            // Checking each cardinal direction 0 -> 3
            for (int i = 0; i <= 3; i++) {
                TilePoint tile = new TilePoint(getLoc());
                tile.moveInDir(i, 1);
                if (gameGrid.getTile(tile).valid()) {
                    possibleMoves.add(i);
                }
            }
            int chosenMove = possibleMoves.get(RandomUtils.uniform(
                    game.getRandom(), possibleMoves.size()));
            if (chosenMove != -1) {
                getLoc().moveInDir(chosenMove, 1);
            }
        } else {
            TilePoint nextMove = moveQueue.poll();
            for (Robot robot : game.getRobots()) {
                if (robot.getLoc().equals(nextMove)) {
                    return false;
                }
            }
            setLoc(nextMove);
        }

        // Checks if the robot is on the player.
        return getLoc().equals(playerLoc);
    }
}
