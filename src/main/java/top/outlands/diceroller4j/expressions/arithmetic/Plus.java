package top.outlands.diceroller4j.expressions.arithmetic;

import org.jspecify.annotations.NonNull;
import top.outlands.diceroller4j.expressions.IExpression;
import top.outlands.diceroller4j.expressions.RollResult;

import java.util.Random;

public record Plus(IExpression augend, IExpression addend) implements IExpression {
    @Override
    public int roll() {
        return augend.roll() + addend.roll();
    }
    
    @Override
    @NonNull
    public String toString() {
        return augend.toString() + " + " + addend.toString();
    }
}
