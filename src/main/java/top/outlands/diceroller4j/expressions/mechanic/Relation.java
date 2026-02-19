package top.outlands.diceroller4j.expressions.mechanic;

import java.util.function.IntFunction;

public enum Relation {
    EQUAL((value, threshold) -> value == threshold),
    LESS_OR_EQUAL((value, threshold) -> value <= threshold),
    GREATER_OR_EQUAL((value, threshold) -> value >= threshold);
    
    Relation(RelationChecker checker) {
        this.checker = checker;
    }
    
    private final RelationChecker checker;

    public RelationChecker getChecker() {
        return  checker;
    }
}
