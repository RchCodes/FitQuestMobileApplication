package com.example.fitquest;

public class Effect {
    int duration; // in turns

    void apply(Character target, CombatContext context) {}

    void tick(Character target, CombatContext context) // per turn
    {
    }

    void remove(Character target, CombatContext context) // cleanup
    {
    }

    public char[] getName() {
        return new char[0];
    }

    public char[] getValue() {
        return new char[0];
    }
}
