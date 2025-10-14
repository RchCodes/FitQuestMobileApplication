package com.example.fitquest;

import java.util.HashMap;
import java.util.Map;

public class LevelProgression {

    private static final int MAX_LEVEL = 50;

    // Map of level -> max XP required for that level
    private static final Map<Integer, Integer> levelMaxXpMap = new HashMap<>();

    static {
        // Populate XP requirements with a progressive curve
        // Formula: baseXP + growth per level
        int baseXP = 100; // Level 1 XP
        int growth = 50;  // additional XP per level increment
        for (int level = 1; level <= MAX_LEVEL; level++) {
            int xpNeeded = baseXP + (level - 1) * growth;
            levelMaxXpMap.put(level, xpNeeded);
        }
    }

    /** Returns the max XP needed for the given level. If level exceeds max, returns 0. */
    public static int getMaxXpForLevel(int level) {
        if (level > MAX_LEVEL) return 0;
        return levelMaxXpMap.getOrDefault(level, 0);
    }

    /** Returns the max allowed level */
    public static int getMaxLevel() {
        return MAX_LEVEL;
    }
}
