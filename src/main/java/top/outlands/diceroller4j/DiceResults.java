package top.outlands.diceroller4j;

import top.outlands.diceroller4j.expressions.RollResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiceResults {
    private static final List<RollResult> lastBatchResults = new ArrayList<>();
    
    public static List<RollResult> getLastBatchResults() {
        return Collections.unmodifiableList(lastBatchResults);
    }
    
    public static void clear() {
        lastBatchResults.clear();
    }
    
    public static void addRollResult(int sides, int result) {
        lastBatchResults.add(new RollResult(sides, result));
    }
    
}
