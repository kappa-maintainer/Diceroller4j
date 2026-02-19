package top.outlands.diceroller4j.expressions.mechanic;

@FunctionalInterface
public interface RelationChecker {
    boolean check(int value, int threshold);
}
