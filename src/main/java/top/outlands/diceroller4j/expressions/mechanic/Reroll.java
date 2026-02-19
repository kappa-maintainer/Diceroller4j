package top.outlands.diceroller4j.expressions.mechanic;

import org.jspecify.annotations.NonNull;
import top.outlands.diceroller4j.expressions.Dice;
import top.outlands.diceroller4j.expressions.DiceSet;
import top.outlands.diceroller4j.expressions.IExpression;

public record Reroll(DiceSet diceSet, int times, int threshold, Relation relation) implements IExpression {
    
    @Override
    public int roll() {
        RelationChecker checker = relation.getChecker();
        int sum = 0;
        for (int i = 0 ; i < diceSet.dices().length ; i++) {
            Dice dice = diceSet.dices()[i];
            int result = dice.roll();
            int counter = 0;
            while (checker.check(result, threshold) && (counter <= times || times == 0)) {
                counter++;
                result = dice.roll();
            }
            sum += result;
        }
        return sum;
    }
    
    @Override
    @NonNull
    public String toString() {
        String expr = diceSet.toString();
        if (expr.charAt(0) != '(') {
            if (times == 0 && relation == Relation.LESS_OR_EQUAL) {
                return expr + "r" + threshold;
            }
        }
        expr += " reroll";
        expr += times == 0 ? " always" : times + " times";
        expr += " on " + threshold;
        expr += switch (relation) {
            case GREATER_OR_EQUAL -> " or more";
            case LESS_OR_EQUAL -> "  or less";
            case EQUAL -> "";
        };
        return expr;
    }
}