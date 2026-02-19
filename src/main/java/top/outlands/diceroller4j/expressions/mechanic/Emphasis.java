package top.outlands.diceroller4j.expressions.mechanic;

import org.jspecify.annotations.NonNull;
import top.outlands.diceroller4j.expressions.Dice;
import top.outlands.diceroller4j.expressions.DiceSet;
import top.outlands.diceroller4j.expressions.IExpression;

import java.util.Random;

public record Emphasis(DiceSet diceSet, EmphasisVariant variant, int from) implements IExpression {
    @Override
    public int roll() {
        int sum = 0;
        for (int i = 0 ; i < diceSet.dices().length ; i++) {
            Dice dice = diceSet.dices()[i];
            int a = dice.roll();
            int b = dice.roll();
            int average = from < 0 ? dice.sides() / 2 : from;
            switch (Integer.compare(Math.abs(a - average),  Math.abs(b - average))){
                case 0 -> {
                    switch (variant) {
                        case REROLL -> i--;
                        case HIGH -> sum += Math.max(a, b);
                        case LOW -> sum += Math.min(a, b);
                    }
                }
                case -1 -> sum += b;
                case 1 -> sum += a;
            }
        }
        return sum;
    }
    
    @Override
    @NonNull
    public String toString() {
        String expr = diceSet.toString();
        if (from < 0) {
            expr += " emphasis";
        } else {
            expr += " furthest from " + from;
        }
        expr += switch (variant) {
            case REROLL -> "";
            case LOW ->  " low";
            case HIGH -> " high";
        };
        return expr;
    }
    
    public enum EmphasisVariant {
        REROLL,
        HIGH,
        LOW,
    }
}
