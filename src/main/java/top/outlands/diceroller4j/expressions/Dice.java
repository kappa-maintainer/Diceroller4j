package top.outlands.diceroller4j.expressions;

import org.jspecify.annotations.NonNull;
import top.outlands.diceroller4j.DiceResults;
import top.outlands.diceroller4j.RandomUtil;

public record Dice(int sides) implements IExpression {
    @Override
    public int roll() {
        int value = RandomUtil.getRandom().nextInt(sides) + 1;
        DiceResults.addRollResult(sides, value);
        return value;
    }
    
    @Override
    @NonNull
    public String toString() {
        return  "d" + sides;
    }
}
