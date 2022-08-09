package byow.Core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Leaderboard implements Serializable {

    protected String savedName;
    private final List<Entry> arr;

    public Leaderboard() {
        savedName = "POMU";
        arr = new ArrayList<>();
    }

    public List<Entry> getScores() {
        return arr;
    }

    public void addScore(Entry entry) {
        arr.add(entry);
        arr.sort(null);
    }

    public record Entry(String name, int keys, int turns) implements Comparable<Entry>,
            Serializable {

        @Override
        public int compareTo(Entry o) {
            if (keys == o.keys) {
                return turns - o.turns;
            }
            return o.keys - keys;
        }
    }
}
