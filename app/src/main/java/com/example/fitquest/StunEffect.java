package com.example.fitquest;

// Stun effect: prevents action
public class StunEffect extends StatusEffect {

    public StunEffect(String name, int duration) {
        super(name, duration, 0);
    }

    @Override
    public int modifyStat(String stat, int value) {
        return value; // stun does not modify stats; it just blocks actions
    }


    @Override
    public void onTurnStart(Character owner, CombatContext ctx) {
        super.onTurnStart(owner, ctx);
        // prevent action by setting AB to 0 or skip turn logic in tick
        owner.setActionBar(0);
    }
}
