package top.outlands.diceroller4j.expressions.arithmetic;

import org.jspecify.annotations.NonNull;
import top.outlands.diceroller4j.expressions.IExpression;
import top.outlands.diceroller4j.expressions.RollResult;

import java.util.Random;

public record Subtract(IExpression minuend, IExpression subtrahend) implements IExpression {
    @Override
    public int roll() {
        return minuend.roll() - subtrahend.roll();
    }

    @Override
    @NonNull
    public String toString() {
        return minuend.toString() + " - " + subtrahend.toString();
    }
}