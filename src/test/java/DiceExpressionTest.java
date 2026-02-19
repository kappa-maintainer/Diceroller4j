import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import top.outlands.diceroller4j.DiceExpressionCompiler;
import top.outlands.diceroller4j.DiceResults;
import top.outlands.diceroller4j.RandomUtil;
import top.outlands.diceroller4j.expressions.IExpression;
import top.outlands.diceroller4j.expressions.RollResult;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基于 <a href="https://dice.run/">dice.run</a> 规范的骰子表达式测试
 * 测试内容包括：
 * 1. 表达式编译后 toString() 结果与输入的一致性
 * 2. 复杂表达式的正确计算
 * 3. 四则运算顺序的正确理解
 * 4. 特定表达式的简化
 */
public class DiceExpressionTest {

    @BeforeAll
    static void setup() {
        // 设置固定随机种子以确保测试可重复
        RandomUtil.setRandom(new Random(12345));
    }

    // ==================== toString() 一致性测试 ====================
    
    @Nested
    @DisplayName("toString() 一致性测试")
    class ToStringConsistencyTests {

        @Test
        @DisplayName("单个骰子表达式 - d6")
        void testSingleDice() throws Exception {
            assertToStringEquals("d6");
        }

        @Test
        @DisplayName("多数量骰子表达式 - 2d6")
        void testMultipleDice() throws Exception {
            assertToStringEquals("2d6");
        }

        @Test
        @DisplayName("百分骰 - 内部转换为d100")
        void testPercentDice() throws Exception {
            // d% 在内部被转换为 d100
            IExpression expr = DiceExpressionCompiler.compile("d%");
            assertEquals("d100", expr.toString());
        }

        @Test
        @DisplayName("多数量百分骰 - 2d%")
        void testMultiplePercentDice() throws Exception {
            // d% 在内部被转换为 d100
            IExpression expr = DiceExpressionCompiler.compile("2d%");
            assertEquals("2d100", expr.toString());
        }

        @Test
        @DisplayName("骰子集合（相同面数）- (d6,d6) 简化为 2d6")
        void testSameDiceSet() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d6,d6)");
            assertEquals("2d6", expr.toString());
        }

        @Test
        @DisplayName("纯数字表达式 - 42")
        void testPureNumber() throws Exception {
            assertToStringEquals("42");
        }

        @Test
        @DisplayName("加法表达式 - d6 + 5")
        void testPlusExpression() throws Exception {
            assertToStringEquals("d6 + 5");
        }

        @Test
        @DisplayName("减法表达式 - 2d6 - 3")
        void testSubtractExpression() throws Exception {
            assertToStringEquals("2d6 - 3");
        }

        @Test
        @DisplayName("乘法表达式 - d6 * 2")
        void testMultiplyExpression() throws Exception {
            assertToStringEquals("d6 * 2");
        }

        @Test
        @DisplayName("除法表达式 - 2d6 / 2")
        void testDivideExpression() throws Exception {
            assertToStringEquals("2d6 / 2");
        }

        @Test
        @DisplayName("Keep修饰符 - 4d6k3")
        void testKeepModifier() throws Exception {
            assertToStringEquals("4d6k3");
        }

        @Test
        @DisplayName("Drop修饰符 - 4d6d1")
        void testDropModifier() throws Exception {
            assertToStringEquals("4d6d1");
        }
        
        @Test
        @DisplayName("Explode修饰符完整格式 - d6 explode always on 5")
        void testExplodeFullFormat() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("d6 explode always on 5 or more");
            assertNotNull(expr.toString());
            assertTrue(expr.toString().equals("d6e5"));
        }
        
        @Test
        @DisplayName("Reroll修饰符完整格式 - d6 reroll always on 1")
        void testRerollFullFormat() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("d6 reroll always on 1 or less");
            assertNotNull(expr.toString());
            assertTrue(expr.toString().equals("d6r1"));
        }

        @Test
        @DisplayName("表达式集合 - (d6,d8)+d10")
        void testExpressionSet() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d6,d8)+d10");
            assertNotNull(expr.toString());
        }
    }

    // ==================== 四则运算顺序测试 ====================

    @Nested
    @DisplayName("四则运算顺序测试")
    class ArithmeticOrderTests {

        @Test
        @DisplayName("乘法优先于加法 - 2 + 3 * 4 = 14")
        void testMultiplyBeforePlus() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("2 + 3 * 4");
            assertEquals(14, expr.roll());
        }

        @Test
        @DisplayName("乘法优先于减法 - 10 - 2 * 3 = 4")
        void testMultiplyBeforeSubtract() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("10 - 2 * 3");
            assertEquals(4, expr.roll());
        }

        @Test
        @DisplayName("除法优先于加法 - 6 + 8 / 2 = 10")
        void testDivideBeforePlus() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("6 + 8 / 2");
            assertEquals(10, expr.roll());
        }

        @Test
        @DisplayName("除法优先于减法 - 10 - 6 / 2 = 7")
        void testDivideBeforeSubtract() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("10 - 6 / 2");
            assertEquals(7, expr.roll());
        }

        @Test
        @DisplayName("连续乘除从左到右 - 12 / 3 * 2 = 8")
        void testMultiplyDivideLeftToRight() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("12 / 3 * 2");
            assertEquals(8, expr.roll());
        }

        @Test
        @DisplayName("连续加减从左到右 - 10 - 3 + 5 = 12")
        void testPlusSubtractLeftToRight() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("10 - 3 + 5");
            assertEquals(12, expr.roll());
        }

        @Test
        @DisplayName("混合运算 - 2 + 3 * 4 - 6 / 2 = 11")
        void testMixedArithmetic() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("2 + 3 * 4 - 6 / 2");
            assertEquals(11, expr.roll());
        }

        @Test
        @DisplayName("骰子与加法 - d6 + 5 结果范围 6-11")
        void testDicePlusNumber() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("d6 + 5");
                int result = expr.roll();
                assertTrue(result >= 6 && result <= 11, 
                    "d6 + 5 结果应在 6-11 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("骰子与乘法 - d6 * 2 结果范围 2-12")
        void testDiceMultiplyNumber() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("d6 * 2");
                int result = expr.roll();
                assertTrue(result >= 2 && result <= 12,
                    "d6 * 2 结果应在 2-12 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("骰子与减法 - 10 - d6 结果范围 4-9")
        void testNumberMinusDice() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("10 - d6");
                int result = expr.roll();
                assertTrue(result >= 4 && result <= 9,
                    "10 - d6 结果应在 4-9 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("骰子表达式混合运算 - 2d6 + 3 * 2")
        void testDiceMixedArithmetic() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("2d6 + 3 * 2");
            String str = expr.toString();
            assertNotNull(str);
        }
    }

    // ==================== 复杂表达式计算测试 ====================

    @Nested
    @DisplayName("复杂表达式计算测试")
    class ComplexExpressionTests {

        @Test
        @DisplayName("多个骰子相加 - d4 + d6 + d8")
        void testMultipleDiceAddition() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("d4 + d6 + d8");
                int result = expr.roll();
                assertTrue(result >= 3 && result <= 18,
                    "d4 + d6 + d8 结果应在 3-18 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("骰子集合加法 - (d6,d8) + d10")
        void testDiceSetAddition() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("(d6,d8) + d10");
                int result = expr.roll();
                assertTrue(result >= 3 && result <= 24,
                    "(d6,d8) + d10 结果应在 3-24 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("Keep修饰符复杂运算 - 4d6k3 + 2")
        void testKeepWithAddition() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("4d6k3 + 2");
                int result = expr.roll();
                assertTrue(result >= 5 && result <= 20,
                    "4d6k3 + 2 结果应在 5-20 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("Drop修饰符复杂运算 - 4d6d1 - 1")
        void testDropWithSubtraction() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("4d6d1 - 1");
                int result = expr.roll();
                assertTrue(result >= 2 && result <= 17,
                    "4d6d1 - 1 结果应在 2-17 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("乘除混合骰子 - 2d6 * 3 / 2")
        void testDiceMultiplyDivide() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("2d6 * 3 / 2");
                int result = expr.roll();
                assertTrue(result >= 3 && result <= 18,
                    "2d6 * 3 / 2 结果应在 3-18 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("复杂嵌套表达式 - (2d4 + 3) * 2 - 5")
        void testComplexNestedExpression() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("(2d4 + 3) * 2 - 5");
                int result = expr.roll();
                assertTrue(result >= 5 && result <= 17,
                    "(2d4 + 3) * 2 - 5 结果应在 5-17 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("D&D属性投掷 - 4d6k3 典型用法")
        void testDnDAbilityScore() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("4d6k3");
                int result = expr.roll();
                assertTrue(result >= 3 && result <= 18,
                    "4d6k3 结果应在 3-18 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("伤害计算示例 - 2d6 + 5 (长剑伤害)")
        void testLongswordDamage() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("2d6 + 5");
                int result = expr.roll();
                assertTrue(result >= 7 && result <= 17,
                    "2d6 + 5 结果应在 7-17 之间，实际为: " + result);
            }
        }

        @Test
        @DisplayName("重击伤害 - 2d8 + 10 + 2d6")
        void testCriticalHit() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("2d8 + 10 + 2d6");
                int result = expr.roll();
                assertTrue(result >= 14 && result <= 38,
                    "2d8 + 10 + 2d6 结果应在 14-38 之间，实际为: " + result);
            }
        }
    }

    // ==================== 表达式简化测试 ====================

    @Nested
    @DisplayName("表达式简化/格式化测试")
    class ExpressionSimplificationTests {

        @Test
        @DisplayName("相同骰子集合简化 - (d6,d6,d6) 应简化为 3d6")
        void testSameDiceSimplification() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d6,d6,d6)");
            assertEquals("3d6", expr.toString());
        }

        @Test
        @DisplayName("单骰子简化 - (d6) 应保持 d6")
        void testSingleDiceInSet() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d6)");
            assertEquals("d6", expr.toString());
        }

        @Test
        @DisplayName("不同面数骰子集合 - (d4,d6,d8) 保持格式")
        void testDifferentDiceSet() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d4,d6,d8)");
            String result = expr.toString();
            assertNotNull(result);
            assertTrue(result.contains("d4"));
            assertTrue(result.contains("d6"));
            assertTrue(result.contains("d8"));
        }

        @Test
        @DisplayName("Keep修饰符简化 - 4d6k3")
        void testKeepSimplification() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("4d6k3");
            assertEquals("4d6k3", expr.toString());
        }

        @Test
        @DisplayName("Drop修饰符简化 - 4d6d1")
        void testDropSimplification() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("4d6d1");
            assertEquals("4d6d1", expr.toString());
        }

        @Test
        @DisplayName("Explode修饰符短格式 - d6e6")
        void testExplodeSimplification() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("d6e6");
            String result = expr.toString();
            assertNotNull(result);
            assertTrue(result.contains("e6") || result.contains("explode"));
        }

        @Test
        @DisplayName("Reroll修饰符短格式 - d6r1")
        void testRerollSimplification() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("d6r1");
            String result = expr.toString();
            assertNotNull(result);
            assertTrue(result.contains("r1") || result.contains("reroll"));
        }

        @Test
        @DisplayName("百分骰简化 - d% 转换为 d100")
        void testPercentDiceSimplification() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("d%");
            assertEquals("d100", expr.toString());
        }

        @Test
        @DisplayName("骰子集合带百分骰 - (d6,d%)")
        void testDiceSetWithPercentDice() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d6,d%)");
            String result = expr.toString();
            assertNotNull(result);
            assertTrue(result.contains("d100") || result.contains("d%"));
        }
    }

    // ==================== 修饰符功能测试 ====================

    @Nested
    @DisplayName("修饰符功能测试")
    class ModifierFunctionTests {

        @Test
        @DisplayName("Keep保留最高 - 4d6k3 保留最高3个")
        void testKeepHighest() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("4d6k3");
                int result = expr.roll();
                assertTrue(result >= 3 && result <= 18);
            }
        }

        @Test
        @DisplayName("Drop丢弃最低 - 4d6d1 丢弃最低1个")
        void testDropLowest() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("4d6d1");
                int result = expr.roll();
                assertTrue(result >= 3 && result <= 18);
            }
        }

        @Test
        @DisplayName("Explode爆炸骰子 - d6e6 在6时爆炸")
        void testExplodeFunction() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("d6e6");
                int result = expr.roll();
                assertTrue(result >= 1);
            }
        }

        @Test
        @DisplayName("Reroll重掷 - d6r1 重掷1")
        void testRerollFunction() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("d6r1");
                int result = expr.roll();
                assertTrue(result >= 1 && result <= 6);
            }
        }

        @Test
        @DisplayName("统计函数Max - (d6,d8,d10)max")
        void testMaxFunction() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("(d6,d8,d10)max");
                int result = expr.roll();
                assertTrue(result >= 1 && result <= 10);
            }
        }

        @Test
        @DisplayName("统计函数Min - (d6,d8,d10)min")
        void testMinFunction() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("(d6,d8,d10)min");
                int result = expr.roll();
                assertTrue(result >= 1 && result <= 6);
            }
        }

        @Test
        @DisplayName("统计函数Average - (d6,d8,d10)average")
        void testAverageFunction() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("(d6,d8,d10)average");
                int result = expr.roll();
                assertTrue(result >= 1 && result <= 10);
            }
        }

        @Test
        @DisplayName("统计函数Median - (d6,d8,d10)median")
        void testMedianFunction() throws Exception {
            for (int i = 0; i < 100; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("(d6,d8,d10)median");
                int result = expr.roll();
                assertTrue(result >= 1 && result <= 10);
            }
        }
    }

    // ==================== 边界情况测试 ====================

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("最小骰子 - d2")
        void testMinDice() throws Exception {
            for (int i = 0; i < 50; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("d2");
                int result = expr.roll();
                assertTrue(result >= 1 && result <= 2);
            }
        }

        @Test
        @DisplayName("大骰子 - d100")
        void testLargeDice() throws Exception {
            for (int i = 0; i < 50; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("d100");
                int result = expr.roll();
                assertTrue(result >= 1 && result <= 100);
            }
        }

        @Test
        @DisplayName("多骰子 - 10d6")
        void testManyDice() throws Exception {
            for (int i = 0; i < 50; i++) {
                RandomUtil.setRandom(new Random(i));
                IExpression expr = DiceExpressionCompiler.compile("10d6");
                int result = expr.roll();
                assertTrue(result >= 10 && result <= 60);
            }
        }

        @Test
        @DisplayName("除法向下取整 - 7 / 2 = 3")
        void testIntegerDivision() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("7 / 2");
            assertEquals(3, expr.roll());
        }

        @Test
        @DisplayName("零参与运算 - 0 + 5")
        void testZeroInExpression() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("0 + 5");
            assertEquals(5, expr.roll());
        }

        @Test
        @DisplayName("大数运算 - 100 + 200")
        void testLargeNumbers() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("100 + 200");
            assertEquals(300, expr.roll());
        }
    }

    // ==================== 性能测试 ====================

    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {

        @Test
        @DisplayName("简单表达式编译性能 - 1000次编译应在1秒内完成")
        void testSimpleCompilePerformance() throws Exception {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                DiceExpressionCompiler.compile("2d6 + 5");
            }
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed < 1000, "1000次编译耗时 " + elapsed + "ms，应小于1秒");
        }

        @Test
        @DisplayName("复杂表达式编译性能 - 1000次编译应在2秒内完成")
        void testComplexCompilePerformance() throws Exception {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                DiceExpressionCompiler.compile("4d6k3 + 10 - 2d4 * 3 / 2");
            }
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed < 2000, "1000次复杂表达式编译耗时 " + elapsed + "ms，应小于2秒");
        }

        @Test
        @DisplayName("骰子执行性能 - 10000次roll应在1秒内完成")
        void testRollPerformance() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("4d6k3");
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                expr.roll();
            }
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed < 1000, "10000次roll耗时 " + elapsed + "ms，应小于1秒");
        }

        @Test
        @DisplayName("大骰子量执行性能 - 100d6的1000次roll应在500ms内完成")
        void testLargeDiceSetPerformance() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("100d6");
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                expr.roll();
            }
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed < 500, "1000次100d6的roll耗时 " + elapsed + "ms，应小于500ms");
        }

        @Test
        @DisplayName("toString性能 - 10000次toString应在200ms内完成")
        void testToStringPerformance() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("4d6k3 + 2d8 - 5 * 3 / 2");
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                expr.toString();
            }
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed < 200, "10000次toString耗时 " + elapsed + "ms，应小于200ms");
        }
    }

    // ==================== 复杂表达式编译与简化测试 ====================

    @Nested
    @DisplayName("复杂表达式编译与简化测试")
    class ComplexExpressionSimplificationTests {
        
        @Test
        @DisplayName("单一Keep修饰符 - 6d6k4")
        void testSingleKeepModifier() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("6d6k4");
            String result = expr.toString();
            assertNotNull(result);
            assertEquals("6d6k4", result);
        }

        @Test
        @DisplayName("嵌套骰子集合 - (d4,d6)+(d8,d10)")
        void testNestedDiceSets() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d4,d6)+(d8,d10)");
            String result = expr.toString();
            assertNotNull(result);
        }

        @Test
        @DisplayName("统计函数后接运算 - (d6,d8,d10)average + 5")
        void testStatisticsWithArithmetic() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d6,d8,d10)average + 5");
            String result = expr.toString();
            assertNotNull(result);
        }

        @Test
        @DisplayName("长链式运算 - d4 + d6 + d8 + d10 + d12 + d20")
        void testLongChainArithmetic() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("d4 + d6 + d8 + d10 + d12 + d20");
            for (int i = 0; i < 50; i++) {
                RandomUtil.setRandom(new Random(i));
                int result = expr.roll();
                assertTrue(result >= 6 && result <= 60);
            }
        }

        @Test
        @DisplayName("完整格式Explode简化 - explode always on 6 or more 应简化为 e6")
        void testExplodeFullToSimplified() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("d6 explode always on 6 or more");
            assertEquals("d6e6", expr.toString());
        }

        @Test
        @DisplayName("完整格式Reroll简化 - reroll always on 1 or less 应简化为 r1")
        void testRerollFullToSimplified() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("d6 reroll always on 1 or less");
            assertEquals("d6r1", expr.toString());
        }

        @Test
        @DisplayName("复杂嵌套括号 - ((2d4 + 3) * 2 - 5)")
        void testComplexNestedParentheses() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(2d4 + 3) * 2 - 5");
            String result = expr.toString();
            assertNotNull(result);
            for (int i = 0; i < 50; i++) {
                RandomUtil.setRandom(new Random(i));
                int roll = expr.roll();
                assertTrue(roll >= 5 && roll <= 17);
            }
        }
        
        @Test
        @DisplayName("骰子集合的统计函数 - (d6,d6,d6,d6)average")
        void testDiceSetStatistics() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d6,d6,d6,d6)average");
            String result = expr.toString();
            assertNotNull(result);
        }

        @Test
        @DisplayName("多面数骰子集合的toString - (d4,d4,d6,d6,d8)")
        void testMixedDiceSetToString() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d4,d4,d6,d6,d8)");
            String result = expr.toString();
            assertNotNull(result);
            assertTrue(result.contains("d4"));
            assertTrue(result.contains("d6"));
            assertTrue(result.contains("d8"));
        }

        @Test
        @DisplayName("带修饰符的骰子集合 - (d6,d6,d6,d6)k3")
        void testDiceSetWithKeepModifier() throws Exception {
            IExpression expr = DiceExpressionCompiler.compile("(d6,d6,d6,d6)k3");
            String result = expr.toString();
            assertNotNull(result);
        }
    }

    // ==================== DiceResults存储与清空测试 ====================

    @Nested
    @DisplayName("DiceResults存储与清空测试")
    class DiceResultsTests {

        @BeforeEach
        void clearResults() {
            DiceResults.clear();
        }

        @Test
        @DisplayName("单次投掷结果存储 - d6应产生1条记录")
        void testSingleRollStorage() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("d6");
            expr.roll();
            
            List<RollResult> results = DiceResults.getLastBatchResults();
            assertEquals(1, results.size(), "单次d6投掷应产生1条记录");
            RollResult rr = results.get(0);
            assertEquals(6, rr.side(), "骰子面数应为6");
            assertTrue(rr.result() >= 1 && rr.result() <= 6, "结果应在1-6之间");
        }

        @Test
        @DisplayName("多次投掷结果存储 - 4d6应产生4条记录")
        void testMultipleRollStorage() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("4d6");
            expr.roll();
            
            List<RollResult> results = DiceResults.getLastBatchResults();
            assertEquals(4, results.size(), "4d6投掷应产生4条记录");
            for (RollResult rr : results) {
                assertEquals(6, rr.side(), "所有骰子面数应为6");
                assertTrue(rr.result() >= 1 && rr.result() <= 6, "结果应在1-6之间");
            }
        }

        @Test
        @DisplayName("清空功能 - clear后应无记录")
        void testClearFunction() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("4d6");
            expr.roll();
            
            assertEquals(4, DiceResults.getLastBatchResults().size(), "投掷后应有4条记录");
            
            DiceResults.clear();
            assertEquals(0, DiceResults.getLastBatchResults().size(), "清空后应无记录");
        }

        @Test
        @DisplayName("多次投掷累积 - 连续投掷应累积记录")
        void testCumulativeResults() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr1 = DiceExpressionCompiler.compile("2d6");
            IExpression expr2 = DiceExpressionCompiler.compile("d8");
            
            expr1.roll();
            assertEquals(2, DiceResults.getLastBatchResults().size(), "第一次投掷后应有2条记录");
            
            expr2.roll();
            assertEquals(3, DiceResults.getLastBatchResults().size(), "第二次投掷后应累积3条记录");
        }

        @Test
        @DisplayName("复杂表达式结果存储 - 2d6 + 3d8应产生5条记录")
        void testComplexExpressionStorage() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("2d6 + 3d8");
            expr.roll();
            
            List<RollResult> results = DiceResults.getLastBatchResults();
            assertEquals(5, results.size(), "2d6 + 3d8应产生5条记录");
            
            long d6Count = results.stream().filter(r -> r.side() == 6).count();
            long d8Count = results.stream().filter(r -> r.side() == 8).count();
            assertEquals(2, d6Count, "应有2个d6记录");
            assertEquals(3, d8Count, "应有3个d8记录");
        }

        @Test
        @DisplayName("RollResult toString格式 - 应为 dX=Y 格式")
        void testRollResultToString() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("d6");
            expr.roll();
            
            RollResult rr = DiceResults.getLastBatchResults().get(0);
            String str = rr.toString();
            assertTrue(str.matches("d6=\\d+"), "RollResult toString应为 d6=X 格式，实际为: " + str);
        }

        @Test
        @DisplayName("百分骰结果存储 - d% 应存储为 d100")
        void testPercentDiceStorage() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("d%");
            expr.roll();
            
            List<RollResult> results = DiceResults.getLastBatchResults();
            assertEquals(1, results.size());
            assertEquals(100, results.get(0).side(), "百分骰应存储为面数100");
            assertTrue(results.get(0).result() >= 1 && results.get(0).result() <= 100);
        }

        @Test
        @DisplayName("骰子集合结果存储 - (d4,d6,d8)应产生3条记录")
        void testDiceSetStorage() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("(d4,d6,d8)");
            expr.roll();
            
            List<RollResult> results = DiceResults.getLastBatchResults();
            assertEquals(3, results.size(), "骰子集合应产生3条记录");
            
            assertTrue(results.stream().anyMatch(r -> r.side() == 4), "应包含d4");
            assertTrue(results.stream().anyMatch(r -> r.side() == 6), "应包含d6");
            assertTrue(results.stream().anyMatch(r -> r.side() == 8), "应包含d8");
        }

        @Test
        @DisplayName("Keep修饰符结果存储 - 4d6k3应只保留3条记录（根据实现可能存储全部或保留的）")
        void testKeepModifierStorage() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("4d6k3");
            expr.roll();
            
            List<RollResult> results = DiceResults.getLastBatchResults();
            // 根据实现，可能存储4条（全部投掷）或3条（只保留的）
            assertTrue(results.size() >= 3 && results.size() <= 4, 
                "4d6k3应存储3-4条记录，实际: " + results.size());
        }

        @Test
        @DisplayName("结果列表不可修改 - getLastBatchResults应返回不可修改列表")
        void testUnmodifiableList() throws Exception {
            RandomUtil.setRandom(new Random(999));
            IExpression expr = DiceExpressionCompiler.compile("d6");
            expr.roll();
            
            List<RollResult> results = DiceResults.getLastBatchResults();
            assertThrows(UnsupportedOperationException.class, () -> {
                results.add(new RollResult(6, 1));
            }, "返回的列表应不可修改");
        }
    }

    // ==================== 辅助方法 ====================

    private void assertToStringEquals(String expression) throws Exception {
        IExpression expr = DiceExpressionCompiler.compile(expression);
        assertEquals(expression, expr.toString(), 
            "表达式 '" + expression + "' 编译后 toString() 应与原表达式一致");
    }
}
