package com.example.fitquest;

public abstract class PassiveSkill {
    private final String id;
    private final String name;
    private final String description;
    private final int levelUnlock;
    private final int iconResId;
    private final ClassType allowedClass;

    private final float critBonus;
    private final float strDamageBonus;
    private final float defScalingBonus;

    // NEW FIELDS (optional for enemies)
    private final boolean isEnemyOnly;
    private final String triggerCondition; // e.g. "onKill", "onDeath", "onTurnStart"

    public PassiveSkill(String id,
                        String name,
                        String description,
                        int levelUnlock,
                        int iconResId,
                        ClassType allowedClass,
                        float critBonus,
                        float strDamageBonus,
                        float defScalingBonus) {
        this(id, name, description, levelUnlock, iconResId, allowedClass, critBonus, strDamageBonus, defScalingBonus, false, null);
    }

    public PassiveSkill(String id,
                        String name,
                        String description,
                        int levelUnlock,
                        int iconResId,
                        ClassType allowedClass,
                        float critBonus,
                        float strDamageBonus,
                        float defScalingBonus,
                        boolean isEnemyOnly,
                        String triggerCondition) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.levelUnlock = levelUnlock;
        this.iconResId = iconResId;
        this.allowedClass = allowedClass;
        this.critBonus = critBonus;
        this.strDamageBonus = strDamageBonus;
        this.defScalingBonus = defScalingBonus;
        this.isEnemyOnly = isEnemyOnly;
        this.triggerCondition = triggerCondition;
    }

    public ClassType getAllowedClass() { return allowedClass; }
    public boolean isEnemyOnly() { return isEnemyOnly; }
    public String getTriggerCondition() { return triggerCondition; }

    // Event hooks (subclasses override)
    public void onKill(Character user, Character target, CombatContext ctx) {}
    public void onDamageTaken(Character user, float damage, CombatContext ctx) {}
    public void onTurnStart(Character user, CombatContext ctx) {}
    public void onDeath(Character user, CombatContext ctx) {} // new trigger

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public int getIconResId() { return iconResId; }
    public String getDescription() { return description; }

    public int getCritBonus() {
        return (int) (critBonus * 100);
    }

    public int getStrDamageBonus() {
        return (int) (strDamageBonus * 100);

    }

    public int getDefScalingBonus() {
        return (int) (defScalingBonus * 100);
    }
}
