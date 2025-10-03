package com.example.fitquest;

// Damage over time effect
public class DOTEffect extends StatusEffect {

    private int damagePerTurn;

    public DOTEffect(String name, int duration, int damagePerTurn) {
        super(name, duration);
        this.damagePerTurn = damagePerTurn;
    }

    @Override
    public void onTurnStart(Character owner, CombatContext ctx) {
        super.onTurnStart(owner, ctx);
        boolean isExpired = false;
        if (!isExpired) {
            ctx.applyDamage(owner, owner, damagePerTurn, null); // or apply to owner? Usually DOT is on target
        }
    }
}

