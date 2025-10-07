package com.example.fitquest;

public class SkillEffect {
    private String effectId;   // unique id
    private EffectType type;   // BUFF, DEBUFF, DOT, SHIELD
    private float magnitude;   // e.g. +20%, -15%
    private int duration;      // in turns
    private int iconRes;       // visual icon for the effect

    public SkillEffect(String effectId, EffectType type, float magnitude, int duration, int iconRes) {
        this.effectId = effectId;
        this.type = type;
        this.magnitude = magnitude;
        this.duration = duration;
        this.iconRes = iconRes;
    }

    public String getEffectId() { return effectId; }
    public EffectType getType() { return type; }
    public float getMagnitude() { return magnitude; }
    public int getDuration() { return duration; }
    public int getIconRes() { return iconRes; }

    @Override
    public String toString() {
        return type + " " + magnitude + "% (" + duration + " turns)";
    }

    public char[] getEffect() {
        return effectId.toCharArray();
    }

    public String getId() {
        return effectId;
    }
}
