package top.outlands.diceroller4j;

import top.outlands.diceroller4j.expressions.Dice;
import top.outlands.diceroller4j.expressions.DiceSet;
import top.outlands.diceroller4j.expressions.DummyExpression;
import top.outlands.diceroller4j.expressions.ExpressionSet;
import top.outlands.diceroller4j.expressions.IExpression;
import top.outlands.diceroller4j.expressions.Number;
import top.outlands.diceroller4j.expressions.arithmetic.Divide;
import top.outlands.diceroller4j.expressions.arithmetic.Multiply;
import top.outlands.diceroller4j.expressions.arithmetic.Plus;
import top.outlands.diceroller4j.expressions.arithmetic.Subtract;
import top.outlands.diceroller4j.expressions.mechanic.Drop;
import top.outlands.diceroller4j.expressions.mechanic.Emphasis;
import top.outlands.diceroller4j.expressions.mechanic.Explode;
import top.outlands.diceroller4j.expressions.mechanic.Keep;
import top.outlands.diceroller4j.expressions.mechanic.Relation;
import top.outlands.diceroller4j.expressions.mechanic.Reroll;
import top.outlands.diceroller4j.expressions.statistics.Average;
import top.outlands.diceroller4j.expressions.statistics.Max;
import top.outlands.diceroller4j.expressions.statistics.Median;
import top.outlands.diceroller4j.expressions.statistics.Min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiceExpressionCompiler {
    
    private static final Pattern ARITHMETICS = Pattern.compile("([+\\-*×⋅x÷/])");

    private static final Pattern NUMBER = Pattern.compile("\\s*\\d+\\s*");

    private static final Pattern SUM = Pattern.compile("\\s*sum\\s*");
    private static final Pattern MIN = Pattern.compile("\\s*min\\s*");
    private static final Pattern MAX = Pattern.compile("\\s*max\\s*");
    private static final Pattern MEDIAN = Pattern.compile("\\s*median\\s*");
    private static final Pattern AVERAGE = Pattern.compile("\\s*average\\s*");

    private static final Pattern KEEP = Pattern.compile("\\s*(?:k(\\d+)|keep\\s*(lowest|highest)?\\s*(\\d+))\\s*");
    private static final Pattern DROP = Pattern.compile("\\s*(?:d(\\d+)|drop\\s*(lowest|highest)?\\s*(\\d+))\\s*");

    private static final Pattern EXPLODE = Pattern.compile("\\s*(?:e(\\d+)|explode\\s*(?:(always)|(\\d+)\\s*times)\\s*on\\s*(\\d+)(?:\\s*or\\s*(more|less))?)\\s*");
    private static final Pattern REROLL = Pattern.compile("\\s*(?:r(\\d+)|reroll\\s*(?:(always)|(\\d+)\\s*times)\\s*on\\s*(\\d+)(?:\\s*or\\s*(more|less))?)\\s*");
    private static final Pattern EMPHASIS = Pattern.compile("\\s*(?:(emphasis)|furthest\\s*from\\s*(\\d+))\\s*(reroll|high|low)\\s*");

    private static final Pattern DICE_SET = Pattern.compile("^\\(d(\\d+|%)(?:,d(?:(\\d+)|%))*\\)");
    private static final Pattern DICE_SET_SINGLE = Pattern.compile("(\\d*)d(\\d+|%)");
    private static final Pattern DICE = Pattern.compile("\\s*d(\\d+|%)\\s*");

    private static final IExpression dummy = new DummyExpression();

    public static IExpression compile(String expression) throws InvalidExpressionException {
        List<Token> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int level = 0;
        boolean inToken = false;
        Matcher matcher;
        for (int index = 0; index < expression.length(); index++) {
            char c = expression.charAt(index);
            if (c == ' ' && !inToken) continue;
            current.append(c);
            if (c == '(') {
                level++;
                inToken = true;
            } else if (c == ')') {
                level--;
            } else if (level == 0) {
                matcher = ARITHMETICS.matcher(String.valueOf(c));
                if (matcher.matches()) {
                    if (c == 'x' && (current.toString().endsWith("max") || current.toString().endsWith("ex"))) 
                        continue;
                    inToken = false;
                    current.deleteCharAt(current.length() - 1);
                    tokens.add(new Token(TokenType.OTHERS, parseSingleExpression(current.toString())));
                    tokens.add(new Token(parseArithmeticTokenType(matcher.group().charAt(0)), dummy));
                    current = new StringBuilder();
                }
            }
        }
        tokens.add(new Token(TokenType.OTHERS, parseSingleExpression(current.toString())));
        for (int j = 0; j < tokens.size(); j++) {
            TokenType type = tokens.get(j).tokenType;
            if (type == TokenType.MULTIPLY || type == TokenType.DIVIDE) {
                if (j > 0 && j < tokens.size() - 1) {
                    IExpression left = tokens.get(j - 1).expression;
                    IExpression right = tokens.get(j + 1).expression;
                    IExpression result;
                    if (type == TokenType.MULTIPLY) {
                        result = new Multiply(left, right);
                    } else {
                        result = new Divide(left, right);
                    }
                    tokens.remove(j + 1);
                    tokens.remove(j);
                    tokens.remove(j - 1);
                    tokens.add(j - 1, new Token(TokenType.OTHERS, result));
                    j--;
                } else {
                    throw  new InvalidExpressionException("Invalid expression: Not infix");
                }
            }
        }
        
        for (int k = 0; k < tokens.size(); k++) {
            TokenType type = tokens.get(k).tokenType;
            if (type == TokenType.PLUS || type == TokenType.SUBTRACT) {
                if (k > 0 && k < tokens.size() - 1) {
                    IExpression left = tokens.get(k - 1).expression;
                    IExpression right = tokens.get(k + 1).expression;
                    IExpression result;
                    if (type == TokenType.PLUS) {
                        result = new Plus(left, right);
                    } else {
                        result = new Subtract(left, right);
                    }
                    tokens.remove(k + 1);
                    tokens.remove(k);
                    tokens.remove(k - 1);
                    tokens.add(k - 1, new Token(TokenType.OTHERS, result));
                    k--;
                } else {
                    throw  new InvalidExpressionException("Invalid expression: Not infix");
                }
            }
        }
        
        if (tokens.size() > 1) {
            throw  new InvalidExpressionException("Invalid expression: Not infix");
        }
        
        return tokens.getFirst().expression;

    }

    private static IExpression parseSingleExpression(String expression) throws InvalidExpressionException {

        if (NUMBER.matcher(expression).matches()) {
            return parseNumber(expression);
        }
        
        String in;
        String out;
        boolean isDiceSet;
        
        if (expression.charAt(0) == '(') {
            out = expression.substring(expression.lastIndexOf(")") + 1);
            in = expression.substring(0, expression.lastIndexOf(")") + 1);
            isDiceSet = DICE_SET.matcher(expression).lookingAt();
        } else {
            Matcher singleDiceSetMatcher = DICE_SET_SINGLE.matcher(expression);
            if (singleDiceSetMatcher.lookingAt()) {
                isDiceSet = true;
                int split = singleDiceSetMatcher.end();
                out = expression.substring(split);
                in = expression.substring(0, split);
            } else {
                throw new InvalidExpressionException(expression);
            }
        }
        
        Matcher matcher;
        int count;
        int threshold;
        Relation relation;
        
        matcher = SUM.matcher(out);
        if (matcher.matches() || out.isBlank()) {
            if (isDiceSet) {
                return parseDiceSet(in);
            } else {
                return parseExpressionSet(in);
            }
        }
        
        matcher = MIN.matcher(out);
        if (matcher.matches()) {
            return new Min(isDiceSet ? diceSetToExpressionSet(parseDiceSet(in)) : parseExpressionSet(in));
        }
        
        matcher = MAX.matcher(out);
        if (matcher.matches()) {
            return new Max(isDiceSet ? diceSetToExpressionSet(parseDiceSet(in)) : parseExpressionSet(in));
        }
        
        matcher = AVERAGE.matcher(out);
        if (matcher.matches()) {
            return new Average(isDiceSet ? diceSetToExpressionSet(parseDiceSet(in)) : parseExpressionSet(in));
        }
        
        matcher = MEDIAN.matcher(out);
        if (matcher.matches()) {
            return new Median(isDiceSet ? diceSetToExpressionSet(parseDiceSet(in)) : parseExpressionSet(in));
        }
        
        matcher = KEEP.matcher(out);
        if (matcher.matches()) {
            boolean highest = true;
            if (matcher.group(1) == null) {
                highest = matcher.group(2) == null || !matcher.group(2).equals("lowest");
                count = Integer.parseInt(matcher.group(3));
            } else {
                count = Integer.parseInt(matcher.group(1));
            }
            if (count < 1) throw new InvalidExpressionException(expression);
            return new Keep(isDiceSet ? diceSetToExpressionSet(parseDiceSet(in)) : parseExpressionSet(in), count, !highest);
        }
        
        matcher = DROP.matcher(out);
        if (matcher.matches()) {
            boolean lowest = true;
            if (matcher.group(1) == null) {
                lowest = matcher.group(2) == null || !matcher.group(2).equals("highest");
                count = Integer.parseInt(matcher.group(3));
            } else {
                count = Integer.parseInt(matcher.group(1));
            }
            if (count < 1) throw new InvalidExpressionException(expression);
            return new Drop(isDiceSet ? diceSetToExpressionSet(parseDiceSet(in)) : parseExpressionSet(in), count, !lowest);
        }
        
        matcher = EXPLODE.matcher(out);
        if (matcher.matches()) {
            if (matcher.group(1) != null) {
                threshold = Integer.parseInt(matcher.group(1));
                count = 0;
                relation = Relation.GREATER_OR_EQUAL;
            } else {
                count = matcher.group(2) != null ? 0 : Integer.parseInt(matcher.group(3));
                threshold = Integer.parseInt(matcher.group(4));
                if (matcher.group(5) == null) {
                    relation = Relation.EQUAL;
                } else if (matcher.group(5).equals("more")) {
                    relation = Relation.GREATER_OR_EQUAL;
                } else {
                    relation = Relation.LESS_OR_EQUAL;
                }
            }
            return new Explode(parseDiceSet(in), count, threshold, relation);
        }
        
        matcher = REROLL.matcher(out);
        if (matcher.matches()) {
            if (matcher.group(1) != null) {
                threshold = Integer.parseInt(matcher.group(1));
                count = 0;
                relation = Relation.LESS_OR_EQUAL;
            } else {
                count = matcher.group(2) != null ? 0 : Integer.parseInt(matcher.group(3));
                threshold = Integer.parseInt(matcher.group(4));
                if (matcher.group(5) == null) {
                    relation = Relation.EQUAL;
                } else if (matcher.group(5).equals("more")) {
                    relation = Relation.GREATER_OR_EQUAL;
                } else {
                    relation = Relation.LESS_OR_EQUAL;
                }
            }
            return new Reroll(parseDiceSet(in), count, threshold, relation);
        }
        
        matcher = EMPHASIS.matcher(out);
        if (matcher.matches()) {
            Emphasis.EmphasisVariant variant = Emphasis.EmphasisVariant.valueOf(matcher.group(3).toUpperCase(Locale.ROOT));
            int average = matcher.group(1) == null ? Integer.parseInt(matcher.group(2)) : -1;
            return new Emphasis(parseDiceSet(in), variant, average);
        }
        
        throw new InvalidExpressionException(expression);
    }
    

    private static Number parseNumber(String expression) {
        return new Number(Integer.parseInt(expression.trim()));
    }

    private static DiceSet parseDiceSet(String expression) throws InvalidExpressionException {
        Matcher matcher = DICE_SET_SINGLE.matcher(expression);
        if (!matcher.matches()) {
            String[] dice_str = expression.substring(1, expression.length() - 1).split(",");
            Dice[] dice_array = new Dice[dice_str.length];
            int i = 0;
            for (String dice : dice_str) {
                matcher = DICE.matcher(dice);
                if (!matcher.matches()) {
                    throw new InvalidExpressionException("Impossible!");
                }
                int sides = matcher.group(1).equals("%") ? 1000 : Integer.parseInt(matcher.group(1));
                dice_array[i++] = new Dice(sides);
            }
            return new DiceSet(dice_array);
        } else {
            int amount = (matcher.group(1) == null || matcher.group(1).isBlank()) ? 1 : Integer.parseInt(matcher.group(1));
            int sides = Objects.equals(matcher.group(2), "%") ? 100 : Integer.parseInt(matcher.group(2));
            Dice[] diceSet = new Dice[amount];
            Arrays.fill(diceSet, new Dice(sides));
            return new DiceSet(diceSet);
        }
    }

    private static ExpressionSet parseExpressionSet(String expression) {
        List<String> subExpressions = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int level = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') {
                level++;
            } else if (c == ')') {
                level--;
            } else if (level == 1) {
                if (c == ',') {
                    subExpressions.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        subExpressions.add(current.toString());
        return new ExpressionSet(subExpressions.stream().map(s -> {
            try {
                return compile(s);
            } catch (InvalidExpressionException e) {
                return new Number(0);
            }
        }).toArray(IExpression[]::new));
    }
    
    private static ExpressionSet diceSetToExpressionSet(DiceSet diceSet) {
        return new ExpressionSet(diceSet.dices());
    }

    private static TokenType parseArithmeticTokenType(char c) throws InvalidExpressionException {
        return switch (c) {
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.SUBTRACT;
            case '*', '×', '⋅', 'x' -> TokenType.MULTIPLY;
            case '/', '÷' -> TokenType.DIVIDE;
            default -> throw new InvalidExpressionException(String.valueOf(c));
        };
    }

    private enum TokenType {
        OTHERS,

        PLUS,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
    }

    private record Token(TokenType tokenType, IExpression expression) {
    }

}
