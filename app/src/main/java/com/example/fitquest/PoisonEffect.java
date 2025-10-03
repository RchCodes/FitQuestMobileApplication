package com.example.fitquest;

// Poison effect: applies DOT each turn
public class PoisonEffect extends StatusEffect {

    private int poisonDamage;

    public PoisonEffect(String name, int duration, int poisonDamage) {
        super(name, duration);
        this.poisonDamage = poisonDamage;
    }

    @Override
    public void onTurnStart(Character owner, CombatContext ctx) {
        super.onTurnStart(owner, ctx);
        boolean isExpired = false;
        if (!isExpired) {
            ctx.applyDamage(null, owner, poisonDamage, null); // null attacker for environmental damage
        }
    }
}
