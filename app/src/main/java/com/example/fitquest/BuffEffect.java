package com.example.fitquest;

// Buff effect (e.g., increase STR, AGI, END)
public class BuffEffect extends StatusEffect {

    private int strBonus, agiBonus, endBonus;

    public BuffEffect(String name, int duration, int strBonus, int agiBonus, int endBonus) {
        super(name, duration);
        this.strBonus = strBonus;
        this.agiBonus = agiBonus;
        this.endBonus = endBonus;
    }

    public int getStrBonus() { return strBonus; }
    public int getAgiBonus() { return agiBonus; }
    public int getEndBonus() { return endBonus; }

    @Override
    public void onTurnStart(Character owner, CombatContext ctx) {
        super.onTurnStart(owner, ctx);
        // Buffs usually apply stat bonuses passively; owner.getEffectiveStrength() should read active buffs
    }
}
