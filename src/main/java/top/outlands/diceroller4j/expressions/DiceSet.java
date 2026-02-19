package top.outlands.diceroller4j.expressions;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;

public record DiceSet(Dice[] dices) implements IExpression {
    
    @Override
    public int roll() {
        return Arrays.stream(dices).mapToInt(Dice::roll).sum();
    }
    
    @Override
    @NonNull
    public String toString() {
        int face = dices[0].sides();
        boolean pure = isPure();
        if (pure) {
            return (dices.length == 1 ? "" : dices.length) + "d" + face;
        } else {
            StringBuilder set = new StringBuilder("(");
            for (Dice d : dices) {
                set.append("d").append(d.sides()).append(", ");
            }
            set.deleteCharAt(set.length() - 2);
            set.append(")");
            return set.toString();
        }
    }
    
    public boolean isPure() {
        boolean pure = true;
        int face = dices[0].sides();
        for (Dice d : dices) {
            if (d.sides() != face) {
                pure = false;
                break;
            }
        }
        return  pure;
    }
}
