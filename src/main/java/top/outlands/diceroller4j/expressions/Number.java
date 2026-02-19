package top.outlands.diceroller4j.expressions;

import org.jspecify.annotations.NonNull;

public record Number(int value) implements IExpression {
    @Override
    public int roll() {
        return value;
    }
    
    @Override
    @NonNull
    public String toString() {
        return Integer.toString(value);
    }
}
