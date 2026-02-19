package top.outlands.diceroller4j.expressions;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Random;

public record ExpressionSet(IExpression[] expressions) implements IExpression {
    
    @Override
    public int roll() {
        return Arrays.stream(expressions).mapToInt(IExpression::roll).sum();
    }
    
    @Override
    @NonNull
    public String toString() {
        if (isDiceSet()) {
            return new DiceSet(Arrays.stream(expressions).map(e -> (Dice) e).toArray(Dice[]::new)).toString();
        }
        StringBuilder full = new StringBuilder("(");
        for (IExpression expression : expressions) {
            full.append(expression.toString());
            full.append(", ");
        }
        full.setLength(full.length() - 2);
        full.append(")");
        return full.toString();
    }
    
    public boolean isDiceSet() {
        return Arrays.stream(expressions).allMatch(e -> e instanceof Dice);
    }

}
