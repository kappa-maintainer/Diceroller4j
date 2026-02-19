package top.outlands.diceroller4j.expressions;

import org.jspecify.annotations.NonNull;

public record RollResult(int side, int result) {
    @Override
    @NonNull
    public String toString() {
        return "d" + side + "=" + result;
    }
}
