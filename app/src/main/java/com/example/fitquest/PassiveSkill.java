package com.example.fitquest;

import com.example.fitquest.ClassType;

public abstract class PassiveSkill {
    private final String id;
    private final String name;
    private final String description;
    private final int levelUnlock;
    private final int iconResId;
    private final ClassType allowedClass; // <--- NEW

    // Stat bonuses
    private final float critBonus;
    private final float strDamageBonus;
    private final float defScalingBonus;

    public PassiveSkill(String id,
                        String name,
                        String description,
                        int levelUnlock,
                        int iconResId,
                        ClassType allowedClass,
                        float critBonus,
                        float strDamageBonus,
                        float defScalingBonus) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.levelUnlock = levelUnlock;
        this.iconResId = iconResId;
        this.allowedClass = allowedClass;
        this.critBonus = critBonus;
        this.strDamageBonus = strDamageBonus;
        this.defScalingBonus = defScalingBonus;
    }

    public ClassType getAllowedClass() { return allowedClass; }

    // Event hooks (subclasses override if needed)
    public void onKill(Character user, Character target, CombatContext ctx) {}
    public void onDamageTaken(Character user, float damage, CombatContext ctx) {}
    public void onTurnStart(Character user, CombatContext ctx) {}

    public int getIconResId() {
        return iconResId;
    }
}
