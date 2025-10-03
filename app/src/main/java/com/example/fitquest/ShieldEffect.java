package com.example.fitquest;

// Shield effect: absorbs incoming damage
public class ShieldEffect extends StatusEffect implements CombatContext.OnIncomingDamageHook {

    private int remainingShield;

    public ShieldEffect(String name, int duration, int shieldAmount) {
        super(name, duration);
        this.remainingShield = shieldAmount;
    }

    public int absorbDamage(int incoming) {
        int absorbed = Math.min(remainingShield, incoming);
        remainingShield -= absorbed;
        boolean isExpired;
        if (remainingShield <= 0) isExpired = true;
        return incoming - absorbed;
    }

    @Override
    public int onIncomingDamage(Character defender, Character attacker, int damage, CombatContext ctx, SkillModel skill) {
        return absorbDamage(damage);
    }
}
