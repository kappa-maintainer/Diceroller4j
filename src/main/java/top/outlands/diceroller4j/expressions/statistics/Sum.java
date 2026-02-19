package top.outlands.diceroller4j.expressions.statistics;

import org.jspecify.annotations.NonNull;
import top.outlands.diceroller4j.expressions.ExpressionSet;
import top.outlands.diceroller4j.expressions.IExpression;

import java.util.Random;

public record Sum(ExpressionSet dices) implements IExpression {

    @Override
    public int roll() {
        return dices.roll();
    }

    @Override
    @NonNull
    public String toString() {
        String expr = dices.toString();
        if (expr.charAt(0) != '(') {
            expr += " ";
        }
        return expr + "sum";
    }
}
