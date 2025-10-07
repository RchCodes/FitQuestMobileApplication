package com.example.fitquest;

// Buff effect (e.g., increase STR, AGI, END)
public class BuffEffect extends StatusEffect {

    private int strBonus, agiBonus, endBonus;

    // Constructor with stat bonuses
    public BuffEffect(String name, int duration, int iconRes, int strBonus, int agiBonus, int endBonus) {
        super(name, duration, iconRes);
        this.strBonus = strBonus;
        this.agiBonus = agiBonus;
        this.endBonus = endBonus;
    }

    public BuffEffect(String name, float magnitude, int duration, int iconRes) {
        super(name, duration, iconRes);

        // Example: convert magnitude to bonuses (rounding)
        // magnitude = 0.2f => +20% of base stat
        // You may adjust based on your design
        this.strBonus = Math.round(magnitude * 10); // arbitrary scaling
        this.agiBonus = Math.round(magnitude * 10);
        this.endBonus = Math.round(magnitude * 10);
    }

    @Override
    public int modifyStat(String stat, int value) {
        switch (stat.toLowerCase()) {
            case "str": return value + strBonus;
            case "agi": return value + agiBonus;
            case "end": return value + endBonus;
            default: return value;
        }
    }

    public int getStrBonus() { return strBonus; }
    public int getAgiBonus() { return agiBonus; }
    public int getEndBonus() { return endBonus; }

    @Override
    public void onTurnStart(Character owner, CombatContext ctx) {
        super.onTurnStart(owner, ctx);
        // Buffs are passive, the Character reads modifyStat() automatically
    }
}
