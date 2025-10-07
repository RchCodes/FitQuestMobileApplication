package com.example.fitquest;

// Damage over time effect
public class DOTEffect extends StatusEffect {

    private int damagePerTurn;
    private float magnitudeFraction = 0f; // if >0 use fractional magnitude of owner's max HP

    public DOTEffect(String name, int duration, int damagePerTurn, int iconRes) {
        super(name, duration, iconRes); // ✅ Pass all required params
        this.damagePerTurn = damagePerTurn;
    }

    // New constructor: use fractional magnitude (e.g. 0.08f means 8% max HP per tick)
    public DOTEffect(String name, float magnitudeFraction, int duration, int iconRes) {
        super(name, duration, iconRes);
        this.magnitudeFraction = magnitudeFraction;
        this.damagePerTurn = 0;
    }

    @Override
    public void onTurnStart(Character owner, CombatContext ctx) {
        super.onTurnStart(owner, ctx);

        if (!isExpired()) {
            int dmg = damagePerTurn;
            if (magnitudeFraction > 0f) {
                dmg = Math.max(1, Math.round(owner.getMaxHp() * magnitudeFraction));
            }
            // Apply DOT damage to the character who has this effect
            ctx.applyDamage(null, owner, dmg, null);
            ctx.pushLog(owner.getName() + " takes " + dmg + " DOT damage from " + getName());
        }
    }

    @Override
    public int modifyStat(String stat, int value) {
        // DOT doesn’t modify stats
        return value;
    }
}
