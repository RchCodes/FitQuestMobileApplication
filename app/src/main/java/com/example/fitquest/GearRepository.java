package com.example.fitquest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory repository for all gear. Use getAllGear() and getGearById().
 */
public class GearRepository {

    private static final List<GearModel> allGear = new ArrayList<>();

    static {

        // Hammer of Ruin (Tank)
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STR", 10f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Hammer Shatter",
                    "+1 extra turn",
                    "Hammer Shatter reduces enemy defense for +1 extra turn."
            ));

            GearModel g = new GearModel(
                    "hammer_of_ruin",
                    "Hammer of Ruin",
                    2000,
                    GearType.WEAPON,
                    R.drawable.weapon_hammer_of_ruin_icon, // iconRes
                    "TANK",
                    stats,
                    effects,
                    "A massive warhammer forged to crush armor.",
                    null, // setId
                    false,
                    0,
                    R.drawable.weapon_hammer_of_ruin_male, // male sprite
                    R.drawable.weapon_hammer_of_ruin_female  // female sprite
            );
            allGear.add(g);
        }

        // Venomfang Daggers (Rogue)
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("AGI", 5f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Venom Blades",
                    "+15%",
                    "Venom Blades poison damage +15%."
            ));

            GearModel g = new GearModel(
                    "venomfang_daggers",
                    "Venomfang Daggers",
                    1800,
                    GearType.WEAPON,
                    R.drawable.weapon_venomfang_daggers_icon,
                    "ROGUE",
                    stats,
                    effects,
                    "Daggers dipped in lethal venom.",
                    null,
                    false,
                    0,
                    R.drawable.weapon_venomfang_daggers_male,
                    R.drawable.weapon_venomfang_daggers_female
            );
            allGear.add(g);
        }

        // Bloodfang Blade (Warrior)
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STR", 12f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Executioner's Slash",
                    "+20% <30% HP",
                    "Executioner’s Slash deals +20% damage to enemies with <30% HP."
            ));

            GearModel g = new GearModel(
                    "bloodfang_blade",
                    "Bloodfang Blade",
                    2200,
                    GearType.WEAPON,
                    R.drawable.weapon_bloodfang_blade_icon,
                    "WARRIOR",
                    stats,
                    effects,
                    "A crimson greatsword thirsting for victory.",
                    null,
                    false,
                    0,
                    R.drawable.weapon_bloodfang_blade_male,
                    R.drawable.weapon_bloodfang_blade_female
            );
            allGear.add(g);
        }

        // Stormbreaker Greatsword (Warrior)
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STR", 8f);
            stats.put("AGI", 5f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Blazing Tempest",
                    "+10% crit",
                    "Blazing Tempest gains +10% critical chance."
            ));

            GearModel g = new GearModel(
                    "stormbreaker_greatsword",
                    "Stormbreaker Greatsword",
                    2500,
                    GearType.WEAPON,
                    R.drawable.weapon_stormbreaker_greatsword_icon,
                    "WARRIOR",
                    stats,
                    effects,
                    "Crackling with lightning energy.",
                    null,
                    false,
                    0,
                    R.drawable.weapon_stormbreaker_greatsword_male,
                    R.drawable.weapon_stormbreaker_greatsword_female
            );
            allGear.add(g);
        }

        // Silent Fang (Rogue)
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("AGI", 7f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Backstab",
                    "Ignore 20% DEF",
                    "Backstab ignores 20% of the target's defense."
            ));

            GearModel g = new GearModel(
                    "silent_fang",
                    "Silent Fang",
                    1900,
                    GearType.WEAPON,
                    R.drawable.weapon_silent_fang_icon,
                    "ROGUE",
                    stats,
                    effects,
                    "A dagger that strikes from shadows without warning.",
                    null,
                    false,
                    0,
                    R.drawable.weapon_silent_fang_male,
                    R.drawable.weapon_silent_fang_female
            );
            allGear.add(g);
        }

        // -------------------------
        // Armor
        // -------------------------
        // Titan Plate
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("END", 15f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Tenacity Buff",
                    "+5% HP regen per turn",
                    "Tenacity Buff also grants 5% HP regen per turn."
            ));

            GearModel g = new GearModel(
                    "titan_plate",
                    "Titan Plate",
                    1500,
                    GearType.ARMOR,
                    R.drawable.ic_armor,
                    "TANK",
                    stats,
                    effects,
                    "Unyielding armor of stone and steel.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Lionheart Armor
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("END", 10f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Battle Cry",
                    "+1 turn duration",
                    "Battle Cry lasts +1 turn."
            ));

            GearModel g = new GearModel(
                    "lionheart_armor",
                    "Lionheart Armor",
                    1400,
                    GearType.ARMOR,
                    R.drawable.ic_armor,
                    "WARRIOR",
                    stats,
                    effects,
                    "Armor that roars with bravery.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Nightveil Cloak
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("AGI", 12f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Smoke Veil",
                    "Cooldown -1 turn",
                    "Smoke Veil cooldown reduced by 1 turn."
            ));

            GearModel g = new GearModel(
                    "nightveil_cloak",
                    "Nightveil Cloak",
                    1300,
                    GearType.ARMOR,
                    R.drawable.ic_armor,
                    "ROGUE",
                    stats,
                    effects,
                    "Shadows weave into this assassin’s garb.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Guardian Aegis
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("END", 8f);
            stats.put("STR", 8f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Divine Protection",
                    "5% permanent damage reduction",
                    "5% permanent damage reduction."
            ));

            GearModel g = new GearModel(
                    "guardian_aegis",
                    "Guardian Aegis",
                    1600,
                    GearType.ARMOR,
                    R.drawable.ic_armor,
                    "UNIVERSAL",
                    stats,
                    effects,
                    "Sacred armor blessed with divine protection.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Dragonhide Armor
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STA", 10f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Dragon Scales",
                    "-25% damage from DoTs",
                    "Reduces damage from DoTs by 25%."
            ));

            GearModel g = new GearModel(
                    "dragonhide_armor",
                    "Dragonhide Armor",
                    1700,
                    GearType.ARMOR,
                    R.drawable.ic_armor,
                    "UNIVERSAL",
                    stats,
                    effects,
                    "Forged from mythical dragon scales.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // -------------------------
        // Pants
        // -------------------------
        // Stonebreaker Greaves
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STR", 10f);
            stats.put("END", 5f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Earthshatter",
                    "+10% stun chance",
                    "Earthshatter stun chance +10%."
            ));

            GearModel g = new GearModel(
                    "stonebreaker_greaves",
                    "Stonebreaker Greaves",
                    900,
                    GearType.PANTS,
                    R.drawable.ic_pants,
                    "TANK",
                    stats,
                    effects,
                    "Heavy greaves that shake the ground.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Phantom Leggings
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("FLEX", 15f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Evasion",
                    "+10% dodge chance",
                    "+10% dodge chance."
            ));

            GearModel g = new GearModel(
                    "phantom_leggings",
                    "Phantom Leggings",
                    850,
                    GearType.PANTS,
                    R.drawable.ic_pants,
                    "ROGUE",
                    stats,
                    effects,
                    "Light as mist, perfect for evasion.",
                    null,
                    false,
                    0,
                   0,
                    0
            );
            allGear.add(g);
        }

        // Warlord’s Legguards
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STR", 12f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Charge Strike",
                    "+10% damage, +5% stun chance",
                    "Charge Strike +10% damage, +5% stun chance."
            ));

            GearModel g = new GearModel(
                    "warlords_legguards",
                    "Warlord’s Legguards",
                    950,
                    GearType.PANTS,
                    R.drawable.ic_pants,
                    "WARRIOR",
                    stats,
                    effects,
                    "Built for champions of the arena.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Endurance Wraps
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STA", 10f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Fatigue Reduction",
                    "-5% skill fatigue cost",
                    "-5% skill fatigue cost."
            ));

            GearModel g = new GearModel(
                    "endurance_wraps",
                    "Endurance Wraps",
                    800,
                    GearType.PANTS,
                    R.drawable.ic_pants,
                    "UNIVERSAL",
                    stats,
                    effects,
                    "Sturdy bindings for long battles.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Balanced Greaves
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("AGI", 5f);
            stats.put("END", 5f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Initiative",
                    "+5% initiative in turn order",
                    "+5% initiative in turn order."
            ));

            GearModel g = new GearModel(
                    "balanced_greaves",
                    "Balanced Greaves",
                    820,
                    GearType.PANTS,
                    R.drawable.ic_pants,
                    "UNIVERSAL",
                    stats,
                    effects,
                    "Carefully crafted for balance.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // -------------------------
        // Boots
        // -------------------------
        // Phantom Boots
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("FLEX", 10f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Shadowstep",
                    "+10% bonus damage",
                    "Shadowstep +10% bonus damage."
            ));

            GearModel g = new GearModel(
                    "phantom_boots",
                    "Phantom Boots",
                    700,
                    GearType.BOOTS,
                    R.drawable.ic_boots,
                    "ROGUE",
                    stats,
                    effects,
                    "Step between shadows with ease.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Colossus Treads
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("END", 12f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Stability",
                    "-20% knockback/stun duration",
                    "-20% knockback/stun duration."
            ));

            GearModel g = new GearModel(
                    "colossus_treads",
                    "Colossus Treads",
                    750,
                    GearType.BOOTS,
                    R.drawable.ic_boots,
                    "TANK",
                    stats,
                    effects,
                    "Heavy boots that leave cracks on the ground.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Berserker Boots
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STR", 8f);
            stats.put("STA", 8f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Rage Attack Speed",
                    "+5% per turn (max 3 stacks)",
                    "Gain +5% Attack Speed per turn (max 3 stacks)."
            ));

            GearModel g = new GearModel(
                    "berserker_boots",
                    "Berserker Boots",
                    720,
                    GearType.BOOTS,
                    R.drawable.ic_boots,
                    "WARRIOR",
                    stats,
                    effects,
                    "Fueled by rage, lighter the longer you fight.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Swiftstep Boots
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("AGI", 12f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Critical Evasion",
                    "+8% crit evasion chance",
                    "+8% crit evasion chance."
            ));

            GearModel g = new GearModel(
                    "swiftstep_boots",
                    "Swiftstep Boots",
                    710,
                    GearType.BOOTS,
                    R.drawable.ic_boots,
                    "UNIVERSAL",
                    stats,
                    effects,
                    "Speed is survival.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Ironmarch Boots
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STA", 10f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Damage Reduction",
                    "+5% under 50% HP",
                    "+5% damage reduction under 50% HP."
            ));

            GearModel g = new GearModel(
                    "ironmarch_boots",
                    "Ironmarch Boots",
                    730,
                    GearType.BOOTS,
                    R.drawable.ic_boots,
                    "UNIVERSAL",
                    stats,
                    effects,
                    "March through pain, march through fear.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // -------------------------
        // Accessories
        // -------------------------
        // Amulet of Vigor
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STA", 10f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Fatigue Reduction",
                    "-10% skill fatigue cost",
                    "-10% skill fatigue cost."
            ));

            GearModel g = new GearModel(
                    "amulet_of_vigor",
                    "Amulet of Vigor",
                    500,
                    GearType.ACCESSORY,
                    R.drawable.ic_accessory,
                    "UNIVERSAL",
                    stats,
                    effects,
                    "Brimming with life energy.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Ring of Shadows
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("AGI", 8f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Crit Boost",
                    "+5% crit chance",
                    "+5% Crit Chance."
            ));

            GearModel g = new GearModel(
                    "ring_of_shadows",
                    "Ring of Shadows",
                    480,
                    GearType.ACCESSORY,
                    R.drawable.ic_accessory,
                    "ROGUE",
                    stats,
                    effects,
                    "A band that hides the wearer’s movements.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Stoneheart Pendant
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("END", 10f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "HP Shield",
                    "5% per turn",
                    "Grants 5% HP shield each turn."
            ));

            GearModel g = new GearModel(
                    "stoneheart_pendant",
                    "Stoneheart Pendant",
                    500,
                    GearType.ACCESSORY,
                    R.drawable.ic_accessory,
                    "TANK",
                    stats,
                    effects,
                    "A gem carved from the earth’s core.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Warrior’s Crest
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("STR", 10f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Rallying Roar",
                    "+5% stamina restore",
                    "Rallying Roar restores +5% stamina."
            ));

            GearModel g = new GearModel(
                    "warriors_crest",
                    "Warrior’s Crest",
                    500,
                    GearType.ACCESSORY,
                    R.drawable.ic_accessory,
                    "WARRIOR",
                    stats,
                    effects,
                    "Symbol of countless victories.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }

        // Charm of Resilience
        {
            Map<String, Float> stats = new HashMap<>();
            stats.put("END", 5f);
            stats.put("STA", 5f);

            List<GearEffects> effects = new ArrayList<>();
            effects.add(new GearEffects(
                    "Debuff Reduction",
                    "-25% debuff duration",
                    "-25% debuff duration."
            ));

            GearModel g = new GearModel(
                    "charm_of_resilience",
                    "Charm of Resilience",
                    500,
                    GearType.ACCESSORY,
                    R.drawable.ic_accessory,
                    "UNIVERSAL",
                    stats,
                    effects,
                    "Wards off harmful effects.",
                    null,
                    false,
                    0,
                    0,
                    0
            );
            allGear.add(g);
        }
    }

    public static List<GearModel> getAllGear() {
        return new ArrayList<>(allGear);
    }

    public static GearModel getGearById(String id) {
        if (id == null) return null;
        for (GearModel g : allGear) {
            if (id.equals(g.getId())) return g;
        }
        return null;
    }
}
