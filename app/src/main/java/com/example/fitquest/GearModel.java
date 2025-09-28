package com.example.fitquest;

import java.util.List;
import java.util.Map;

public class GearModel {
    String name;
    GearType type; // Weapon, Armor, Pants, Boots, Accessory
    ClassType allowedClass;
    Map<String, Float> statBoosts; // STR, END, AGI, etc.
    List<Effect> skillAugments;     // e.g., Poison DOT +15%
}

enum GearType {
    WEAPON,
    ARMOR,
    PANTS,
    BOOTS,
    ACCESSORY
}
