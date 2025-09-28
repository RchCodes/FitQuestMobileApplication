package com.example.fitquest;

public class Character {
    private AvatarModel avatar;
    private CombatContext combatStats;

    public Character(AvatarModel avatar) {
        this.avatar = avatar;
        this.combatStats = new CombatContext(avatar); // generate battle stats
    }

    public AvatarModel getAvatar() { return avatar; }
    public CombatContext getCombatStats() { return combatStats; }

    // Utility
    public boolean isAlive() {
        return combatStats.getCurrentHp() > 0;
    }

    public void takeDamage(int damage) {
        combatStats.setCurrentHp(combatStats.getCurrentHp() - damage);
    }
}

