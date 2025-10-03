package com.example.fitquest;
/**
 * Base class for buffs and debuffs
 */
public abstract class StatusEffect {
    protected final String name;
    protected int duration; // in turns

    public StatusEffect(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() { return name; }
    public boolean isExpired() { return duration <= 0; }
    public void reduceDuration() { duration--; }

    /**
     * Apply effect at the start of the turn (e.g., DOT, heal)
     */
    public void onTurnStart(Character character, CombatContext ctx) {}

    /**
     * Modify stat dynamically (STR, END, AGI, FLX, STA)
     */
    public int modifyStat(String stat, int value) { return value; }
}

