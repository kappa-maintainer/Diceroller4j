package top.outlands.diceroller4j.expressions.arithmetic;

import org.jspecify.annotations.NonNull;
import top.outlands.diceroller4j.expressions.IExpression;
import top.outlands.diceroller4j.expressions.RollResult;

import java.util.Random;

public record Multiply(IExpression multiplier, IExpression multiplicator) implements IExpression {
    @Override
    public int roll() {
        return multiplier.roll() * multiplicator.roll();
    }
    
    @Override
    @NonNull
    public String toString() {
        return multiplier.toString() + " * " + multiplicator.toString(); 
    }
}
