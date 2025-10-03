package com.example.fitquest;

// Burn effect: similar to poison but could have extra hooks
public class BurnEffect extends StatusEffect {

    private int burnDamage;

    public BurnEffect(String name, int duration, int burnDamage) {
        super(name, duration);
        this.burnDamage = burnDamage;
    }

    @Override
    public void onTurnStart(Character owner, CombatContext ctx) {
        super.onTurnStart(owner, ctx);
        boolean isExpired = false;
        if (!isExpired) {
            ctx.applyDamage(null, owner, burnDamage, null);
        }
    }
}
