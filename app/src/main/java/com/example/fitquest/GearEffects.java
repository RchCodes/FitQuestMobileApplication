package com.example.fitquest;

public class GearEffects {
    private String name;        // e.g., "Poison DOT"
    private String modifier;    // e.g., "+15%"
    private String description; // Optional detailed explanation

    public GearEffects(String name, String modifier) {
        this(name, modifier, "");
    }

    public GearEffects(String name, String modifier, String description) {
        this.name = name;
        this.modifier = modifier;
        this.description = description;
    }

    // Getters / Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getModifier() { return modifier; }
    public void setModifier(String modifier) { this.modifier = modifier; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        if (description == null || description.isEmpty()) {
            return name + " " + modifier;
        }
        return name + " " + modifier + " - " + description;
    }
}
