package com.example.fitquest;

// Poison effect: applies damage over time each turn
public class PoisonEffect extends StatusEffect {

    private int poisonDamage;

    public PoisonEffect(String name, int duration, int poisonDamage) {
        super(name, duration, R.drawable.ic_effect_poison);
        this.poisonDamage = poisonDamage;
    }

    @Override
    public void onTurnStart(Character owner, CombatContext ctx) {
        super.onTurnStart(owner, ctx);
        if (!isExpired()) {
            // Apply poison damage (environmental)
            ctx.applyDamage(null, owner, poisonDamage, null);
        }
    }

    @Override
    public int modifyStat(String stat, int value) {
        // Poison does not alter base stats; it only deals damage
        return value;
    }
}
