package top.outlands.diceroller4j.expressions.arithmetic;

import org.jspecify.annotations.NonNull;
import top.outlands.diceroller4j.expressions.IExpression;
import top.outlands.diceroller4j.expressions.RollResult;

import java.util.Random;

public record Divide(IExpression dividend, IExpression divisor) implements IExpression {

    @Override
    public int roll() {
        return dividend.roll() / divisor.roll();
    }
    
    @Override
    @NonNull
    public String toString() {
        return dividend.toString() + " / " + divisor.toString();
    }
}
