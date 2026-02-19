package top.outlands.diceroller4j;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    
    private static Random random = ThreadLocalRandom.current();
    
    public static void setRandom(Random random) {
        RandomUtil.random = random;
    }
    
    public static Random getRandom() {
        return random;
    }
}
