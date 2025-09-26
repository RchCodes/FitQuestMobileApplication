package com.example.fitquest;

import java.io.Serializable;

/**
 * Represents the rewards for completing a quest.
 * Includes coins, XP, body-part-specific physique points, general physique points, and attribute points.
 */
public class QuestReward implements Serializable {

    private int coins;
    private int xp;

    // Body-part-specific physique points
    private int armPoints;
    private int legPoints;
    private int chestPoints;
    private int backPoints;

    // Generic points
    private int physiquePoints;
    private int attributePoints;

    public QuestReward(int coins, int xp, int armPoints, int legPoints, int chestPoints, int backPoints,
                       int physiquePoints, int attributePoints) {
        this.coins = coins;
        this.xp = xp;
        this.armPoints = armPoints;
        this.legPoints = legPoints;
        this.chestPoints = chestPoints;
        this.backPoints = backPoints;
        this.physiquePoints = physiquePoints;
        this.attributePoints = attributePoints;
    }

    // --- Getters ---
    public int getCoins() { return coins; }
    public int getXp() { return xp; }
    public int getArmPoints() { return armPoints; }
    public int getLegPoints() { return legPoints; }
    public int getChestPoints() { return chestPoints; }
    public int getBackPoints() { return backPoints; }
    public int getPhysiquePoints() { return physiquePoints; }
    public int getAttributePoints() { return attributePoints; }
}
