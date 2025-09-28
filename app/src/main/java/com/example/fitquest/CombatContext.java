package com.example.fitquest;

import com.example.fitquest.AvatarModel;

public class CombatContext {
    private int maxHp;
    private int currentHp;
    private int attack;
    private int defense;
    private int speed;
    private double dodgeChance;

    public CombatContext(AvatarModel avatar) {
        this.maxHp = 50 + (avatar.getEndurance() * 10) + (avatar.getStamina() * 5);
        this.currentHp = this.maxHp;

        this.attack = 5 + (avatar.getStrength() * 2) + avatar.getArmPoints();
        this.defense = 5 + (avatar.getChestPoints() * 2) + avatar.getBackPoints();
        this.speed = 5 + avatar.getAgility() * 2 + avatar.getLegPoints();
        this.dodgeChance = Math.min(0.3, 0.05 + avatar.getFlexibility() * 0.01);
    }

    // --- Getters ---
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int hp) { this.currentHp = Math.max(0, Math.min(maxHp, hp)); }

    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public double getDodgeChance() { return dodgeChance; }
}
