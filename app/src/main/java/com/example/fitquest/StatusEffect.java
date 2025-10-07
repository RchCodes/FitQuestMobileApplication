package com.example.fitquest;

/**
 * Minimal StatusEffect base used by Combat pipeline.
 * Extend or instantiate concrete subclasses as needed.
 */
public abstract class StatusEffect {
    protected String name;
    protected int remainingTurns;
    protected final int iconRes;

    public StatusEffect(String name, int duration, int iconRes) {
        this.name = name;
        this.remainingTurns = duration;
        this.iconRes = iconRes;
    }

    /** Called at start of owner's turn (Character.processStatusEffects calls this). */
    public void onTurnStart(Character owner, CombatContext ctx) {
        // Default: decrement duration
        if (remainingTurns > 0) remainingTurns--;
    }

    /** Whether this effect is expired and should be removed. */
    public boolean isExpired() {
        return remainingTurns == 0;
    }

    public String getName() { return name; }
    public int getRemainingTurns() { return remainingTurns; }
    public int getIconRes() { return iconRes; }

    /** Some effects may absorb damage; override if needed. Returns remaining damage after absorb. */
    public int absorbDamage(int incoming) { return incoming; }

    public abstract int modifyStat(String stat, int value);
}
