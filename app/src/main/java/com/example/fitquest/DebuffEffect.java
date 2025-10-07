package com.example.fitquest;

// Debuff effect (e.g., decrease STR, AGI, END)
public class DebuffEffect extends StatusEffect {

    private int strPenalty, agiPenalty, endPenalty;

    public DebuffEffect(String name, int duration, int strPenalty, int agiPenalty, int endPenalty) {
        super(name, duration, 0);
        this.strPenalty = strPenalty;
        this.agiPenalty = agiPenalty;
        this.endPenalty = endPenalty;
    }

    @Override
    public int modifyStat(String stat, int value) {
        switch (stat) {
            case "STR": return value - strPenalty;
            case "AGI": return value - agiPenalty;
            case "END": return value - endPenalty;
            default: return value;
        }
    }



    public int getStrPenalty() { return strPenalty; }
    public int getAgiPenalty() { return agiPenalty; }
    public int getEndPenalty() { return endPenalty; }

    @Override
    public void onTurnStart(Character owner, CombatContext ctx) {
        super.onTurnStart(owner, ctx);
        // Debuffs usually apply stat penalties passively; owner.getEffective* reads these
    }
}
