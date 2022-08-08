package byow.input;

/**
 * Created by hug.
 */
public interface InputSource {
    boolean hasNextKey();
    char getNextKey();
    boolean possibleNextInput();
}
