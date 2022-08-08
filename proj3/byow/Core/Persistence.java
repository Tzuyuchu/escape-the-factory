package byow.Core;

import java.io.*;
import java.nio.file.Files;

/** Persistence class that is able to serialize itself for game save and loading. */
public class Persistence implements Serializable {

    /** File path and name of the file containing all data of GameState. If no file exists, there
     * is no save. */
    public static final File DATA_FILE = new File("byowdata.txt");
    /** File path and name of the file containing leaderboard information. If no file exists,
     * the leaderboard is empty. */
    public static final File LEADERBOARD_FILE = new File("leaderboard.txt");

    /**
     * Attempts to write GameState to file, if one exists with a generated worldMap.
     *
     * Code credited to P. N. Hilfinger from Gitlet project.
     */
    public static void writeData(GameState data) {
        if (data == null || data.getWorldMap() == null) {
            return;
        }
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(data);
            objectStream.close();
            BufferedOutputStream str =
                    new BufferedOutputStream(Files.newOutputStream(DATA_FILE.toPath()));
            str.write(stream.toByteArray());
            str.close();
        } catch (IOException exception) {
            System.out.println("Error serializing data:\r\n" + exception.getMessage());
        }
    }

    /**
     * Attempts to write leaderboard to file.
     */
    public static void writeData(Leaderboard leaderboard) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(leaderboard);
            objectStream.close();
            BufferedOutputStream str =
                    new BufferedOutputStream(Files.newOutputStream(LEADERBOARD_FILE.toPath()));
            str.write(stream.toByteArray());
            str.close();
        } catch (IOException exception) {
            System.out.println("Error serializing leaderboard:\r\n" + exception.getMessage());
        }
    }

    /**
     * Reads the GameState from file and returns it.
     *
     * Code credited to P. N. Hilfinger from Gitlet project. */
    public static GameState readData() {
        try {
            ObjectInputStream objectStream = new ObjectInputStream(new FileInputStream(DATA_FILE));
            GameState data = (GameState) objectStream.readObject();
            objectStream.close();
            return data;
        } catch (IOException | ClassNotFoundException exception) {
            return null;
        }
    }

    /**
     * Reads the Leaderboard from file and returns it.
     */
    public static Leaderboard readLeaderboard() {
        try {
            ObjectInputStream objectStream = new ObjectInputStream(
                    new FileInputStream(LEADERBOARD_FILE));
            Leaderboard leaderboard = (Leaderboard) objectStream.readObject();
            objectStream.close();
            if (leaderboard != null) {
                return leaderboard;
            }
        } catch (IOException | ClassNotFoundException exception) {
            // Continue
        }
        return new Leaderboard();
    }

    /**
     * Erases the save file if one exists.
     */
    public static void eraseData() {
        boolean res = DATA_FILE.delete();
    }
}
