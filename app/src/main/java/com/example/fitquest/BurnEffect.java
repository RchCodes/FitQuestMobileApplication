package com.example.fitquest;

// Burn effect: similar to poison but could have extra hooks
public class BurnEffect extends StatusEffect {

    private int burnDamage;

    public BurnEffect(String name, int duration, int burnDamage) {
        super(name, duration, R.drawable.ic_enemy_flamewolf_passive);
        this.burnDamage = burnDamage;
    }

    @Override
    public int modifyStat(String stat, int value) {
        return value; // burn does not alter stats directly
    }


    @Override
    public void onTurnStart(Character owner, CombatContext ctx) {
        super.onTurnStart(owner, ctx);
        if (!isExpired()) {
            ctx.applyDamage(null, owner, burnDamage, null);
        }
    }
}
