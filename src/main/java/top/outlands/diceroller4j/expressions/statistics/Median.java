package top.outlands.diceroller4j.expressions.statistics;

import org.jspecify.annotations.NonNull;
import top.outlands.diceroller4j.expressions.ExpressionSet;
import top.outlands.diceroller4j.expressions.IExpression;

import java.util.Arrays;
import java.util.Random;

public record Median(ExpressionSet dices) implements IExpression {

    @Override
    public int roll() {
        int[] values = Arrays.stream(dices.expressions()).mapToInt(d -> d.roll()).toArray();
        Arrays.sort(values);
        return values[values.length / 2];
    }

    @Override
    @NonNull
    public String toString() {
        String expr = dices.toString();
        if (expr.charAt(0) != '(') {
            expr += " ";
        }
        return expr + "median";
    }
}