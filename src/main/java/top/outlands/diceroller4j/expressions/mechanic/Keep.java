package top.outlands.diceroller4j.expressions.mechanic;

import org.jspecify.annotations.NonNull;
import top.outlands.diceroller4j.expressions.ExpressionSet;
import top.outlands.diceroller4j.expressions.IExpression;

import java.util.Arrays;
import java.util.Random;

/**
 * Keep higher results by default.
 * @param count the amount to keep
 * @param inverted keep lower results instead
 * @param expressions result expressions
 */
public record Keep(ExpressionSet expressions, int count, boolean inverted) implements IExpression {
    @Override
    public int roll() {
        int[] values = Arrays.stream(expressions.expressions()).mapToInt(IExpression::roll).toArray();
        Arrays.sort(values);
        int sum = 0;
        if (inverted) {
            for (int i = 0; i < count; i++) {
                sum += values[i];
            }
        } else {
            for (int i = 0; i < count; i++) {
                sum += values[values.length - 1 - i];
            }
        }
        return sum;
    }
    
    @Override
    @NonNull
    public String toString() {
        String expr = expressions().toString();
        if (expr.charAt(0) != '(') {
            if (!inverted) {
                return expr + "k" + count;
            }  else {
                return expr + " keep lowest " + count;
            }
        }  else {
            if (!inverted) {
                return expr + " keep " + count;
            } else  {
                return expr + " keep lowest " + count;
            }
        }
    }
}
