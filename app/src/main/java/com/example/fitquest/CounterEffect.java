package com.example.fitquest;

// Counter effect: reflects damage back to attacker
public class CounterEffect extends StatusEffect implements CombatContext.OnAttackedHook {

    private int counterDamage;

    public CounterEffect(String name, int duration, int counterDamage) {
        super(name, duration, R.drawable.ic_warrior_skill_6);
        this.counterDamage = counterDamage;
    }

    @Override
    public int modifyStat(String stat, int value) {
        return value; // counter doesn’t change stats
    }


    @Override
    public int onAttacked(Character defender, Character attacker, int damageTaken, CombatContext ctx, SkillModel skill) {
        return counterDamage;
    }
}
