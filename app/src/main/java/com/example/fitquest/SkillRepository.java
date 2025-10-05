package com.example.fitquest;

import java.util.*;

/**
 * Central repository for all skills & passives used by the game.
 * - Contains all skill initialization from your provided tables (Tank, Rogue, Warrior).
 * - Provides lookup helpers used by AvatarModel and combat code.
 *
 * NOTE: SimpleSkill.execute(...) is a placeholder — connect to your CombatContext/Character logic.
 */
public final class SkillRepository {

    private static final Map<String, SkillModel> SKILL_MAP = new HashMap<>();
    private static final Map<String, PassiveSkill> PASSIVE_MAP = new HashMap<>();

    static {
        initPassives();
        initSkills();
    }

    private SkillRepository() {}

    // --- Public API ---
    public static SkillModel getSkillById(String id) {
        return SKILL_MAP.get(id);
    }

    public static PassiveSkill getPassiveById(String id) {
        return PASSIVE_MAP.get(id);
    }

    public static List<SkillModel> getSkillsForClass(ClassType cls) {
        List<SkillModel> out = new ArrayList<>();
        for (SkillModel s : SKILL_MAP.values()) {
            if (s.getAllowedClass() == cls) out.add(s);
        }
        return out;
    }

    public static List<PassiveSkill> getPassivesForClass(ClassType cls) {
        List<PassiveSkill> out = new ArrayList<>();
        for (PassiveSkill p : PASSIVE_MAP.values()) {
            if (p.getAllowedClass() == cls) out.add(p);
        }
        return out;
    }

    // --- Initialization helpers ---
    private static void initPassives() {
        // Tank passives
        PASSIVE_MAP.put("stoneheart",
                new SimplePassive("stoneheart",
                        "Stoneheart",
                        "DEF scaling +10% when below 50% HP",
                        1,
                        R.drawable.ic_tank_passive_1,
                        ClassType.TANK,
                        0f, // critBonus
                        0f, // strDamageBonus
                        0.10f)); // defScalingBonus

        PASSIVE_MAP.put("second_wind",
                new SimplePassive("second_wind",
                        "Second Wind",
                        "Restores 15% HP once per battle when below 20% HP",
                        1,
                        R.drawable.ic_tank_passive_2,
                        ClassType.TANK,
                        0f, 0f, 0f));

        // Rogue passives
        PASSIVE_MAP.put("shadow_instinct",
                new SimplePassive("shadow_instinct",
                        "Shadow Instinct",
                        "+10% crit baseline",
                        1,
                        R.drawable.ic_rogue_passive_1,
                        ClassType.ROGUE,
                        0.10f, 0f, 0f));

        PASSIVE_MAP.put("killers_momentum",
                new SimplePassive("killers_momentum",
                        "Killer's Momentum",
                        "Reset cooldown on kill (1x per battle)",
                        1,
                        R.drawable.ic_rogue_passive_2,
                        ClassType.ROGUE,
                        0f,0f,0f));

        // Warrior passives
        PASSIVE_MAP.put("momentum",
                new SimplePassive("momentum",
                        "Momentum",
                        "+5% damage stacking each consecutive attack",
                        1,
                        R.drawable.ic_warrior_passive_1,
                        ClassType.WARRIOR,
                        0f, 0.05f, 0f));

        PASSIVE_MAP.put("weapon_mastery",
                new SimplePassive("weapon_mastery",
                        "Weapon Mastery",
                        "+5% STR skill damage",
                        1,
                        R.drawable.ic_warrior_passive_2,
                        ClassType.WARRIOR,
                        0f, 0.05f, 0f));
    }

    private static void initSkills() {
        // ---------- TANK SKILLS ----------
        putSkill(new SimpleSkill(
                "hammer_shatter",
                "Hammer Shatter",
                "Massive hammer strike that cracks enemy defenses.",
                SkillType.DAMAGE,
                ClassType.TANK,
                R.drawable.ic_tank_skill_1,
                false,
                1,
                100, // AB cost %
                3,   // cooldown
                1.20f, // STR scaling
                0.40f, // END scaling
                0f,0f,0f,
                Arrays.asList(new SkillEffect("def_down_20", EffectType.DEBUFF, 0.20f, 2, 0))
        ));

        putSkill(new SimpleSkill(
                "iron_breaker",
                "Iron Breaker",
                "Crushing blow that ignores part of the enemy’s armor.",
                SkillType.DAMAGE,
                ClassType.TANK,
                R.drawable.ic_tank_skill_2,
                false,
                2,
                100,
                2,
                1.00f,
                0.30f,
                0f,0f,0f,
                Arrays.asList(new SkillEffect("ignore_def_25", EffectType.DEBUFF, 0.25f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "tenacity_buff",
                "Tenacity Buff",
                "Fortifies the body against damage.",
                SkillType.BUFF,
                ClassType.TANK,
                R.drawable.ic_tank_skill_3,
                false,
                3,
                50,
                3,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("buff_def_20", EffectType.BUFF, 0.20f, 2, 0))
        ));

        putSkill(new SimpleSkill(
                "shield_wall",
                "Shield Wall",
                "Forms a wall of protection.",
                SkillType.BUFF,
                ClassType.TANK,
                R.drawable.ic_tank_skill_4,
                false,
                4,
                50,
                4,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("damage_reduction_50", EffectType.SHIELD, 0.50f, 1, 0))
        ));

        putSkill(new SimpleSkill(
                "iron_will",
                "Iron Will",
                "Summons inner strength to endure.",
                SkillType.HEAL,
                ClassType.TANK,
                R.drawable.ic_tank_skill_5,
                false,
                5,
                100,
                5,
                0f,0.20f,0f,0f,0f,
                Arrays.asList(new SkillEffect("cleanse", EffectType.BUFF, 0f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "earthshatter",
                "Earthshatter",
                "Hammer smashes the ground with devastating force.",
                SkillType.DAMAGE,
                ClassType.TANK,
                R.drawable.ic_tank_skill_6,
                true,   // ultimate
                6,
                100,
                6,
                2.00f,
                0.50f,
                0f,0f,0f,
                Arrays.asList(new SkillEffect("stun_30", EffectType.DEBUFF, 0.30f, 1, 0))
        ));

        putSkill(new SimpleSkill(
                "stonewall",
                "Stonewall",
                "Braces for impact with immovable defense.",
                SkillType.BUFF,
                ClassType.TANK,
                R.drawable.ic_tank_skill_7,
                false,
                7,
                50,
                4,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("reduce_damage_30", EffectType.SHIELD, 0.30f, 2, 0))
        ));

        putSkill(new SimpleSkill(
                "fortify",
                "Fortify",
                "Restores health at the cost of offense.",
                SkillType.HEAL,
                ClassType.TANK,
                R.drawable.ic_tank_skill_8,
                false,
                8,
                100,
                4,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("heal_missing_15", EffectType.BUFF, 0.15f, 0, 0),
                        new SkillEffect("atk_down_20", EffectType.DEBUFF, 0.20f, 1, 0))
        ));

        putSkill(new SimpleSkill(
                "body_slam",
                "Body Slam",
                "Leaps body-first into the enemy.",
                SkillType.DAMAGE,
                ClassType.TANK,
                R.drawable.ic_tank_skill_9,
                false,
                9,
                100,
                3,
                1.00f,
                0.20f,
                0f,0f,0f,
                Arrays.asList(new SkillEffect("stun_25", EffectType.DEBUFF, 0.25f, 1, 0))
        ));

        putSkill(new SimpleSkill(
                "bulwark_counter",
                "Bulwark Counter",
                "Reactively counters attacks.",
                SkillType.COUNTER,
                ClassType.TANK,
                R.drawable.ic_tank_skill_10,
                false,
                10,
                0, // triggered
                4,
                0.80f,
                0f,0f,0f,0f,
                Collections.emptyList()
        ));

        putSkill(new SimpleSkill(
                "unbreakable",
                "Unbreakable",
                "Immune to negative effects for a time.",
                SkillType.BUFF,
                ClassType.TANK,
                R.drawable.ic_tank_skill_11,
                false,
                11,
                50,
                5,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("immune_debuffs", EffectType.BUFF, 0f, 2, 0))
        ));

        putSkill(new SimpleSkill(
                "titans_wrath",
                "Titan's Wrath",
                "Crushing strike that risks leaving you open.",
                SkillType.DAMAGE,
                ClassType.TANK,
                R.drawable.ic_tank_skill_12,
                true,
                12,
                100,
                6,
                2.50f,
                0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("self_open", EffectType.DEBUFF, 0f, 1, 0))
        ));

        putSkill(new SimpleSkill(
                "ground_slam",
                "Ground Slam",
                "Slams the ground to knock back enemy.",
                SkillType.DAMAGE,
                ClassType.TANK,
                R.drawable.ic_tank_skill_13,
                false,
                13,
                100,
                3,
                0.90f,
                0.20f,
                0f,0f,0f,
                Arrays.asList(new SkillEffect("reduce_enemy_ab_20", EffectType.DEBUFF, 0.20f, 2, 0))
        ));


        // ---------- ROGUE SKILLS ----------
        putSkill(new SimpleSkill(
                "backstab",
                "Backstab",
                "Deadly strike from shadows.",
                SkillType.DAMAGE,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_1,
                false,
                1,
                100,
                3,
                1.50f,
                0f,
                0.50f, // AGI scaling included in third param, but SkillModel constructor has agiScaling param; we put 0.5f in agi slot
                0f,0f,
                Arrays.asList(new SkillEffect("crit_if_debuffed", EffectType.BUFF, 0.30f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "dagger_throw",
                "Dagger Throw",
                "Reliable ranged strike.",
                SkillType.DAMAGE,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_2,
                false,
                2,
                100,
                2,
                1.00f,
                0f,
                0.30f,
                0f,0f,
                Arrays.asList(new SkillEffect("true_hit", EffectType.BUFF, 0f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "shadow_slash",
                "Shadow Slash",
                "Fast attack with chance to double hit.",
                SkillType.DAMAGE,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_3,
                false,
                3,
                100,
                2,
                0.90f,
                0f,
                0.40f,
                0f,0f,
                Arrays.asList(new SkillEffect("double_hit_25", EffectType.BUFF, 0.25f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "smoke_veil",
                "Smoke Veil",
                "Vanishes into smoke to avoid danger.",
                SkillType.BUFF,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_4,
                false,
                4,
                50,
                4,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("dodge_next", EffectType.BUFF, 0f, 1, 0),
                        new SkillEffect("next_guaranteed_crit", EffectType.BUFF, 1f, 1, 0))
        ));

        putSkill(new SimpleSkill(
                "venom_blades",
                "Venom Blades",
                "Infuses weapons with poison.",
                SkillType.DOT,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_5,
                false,
                5,
                100,
                4,
                0.80f,
                0f,
                0.30f,
                0f,0f,
                Arrays.asList(new SkillEffect("poison_dot", EffectType.DOT, 0.08f, 3, 0))
        ));

        putSkill(new SimpleSkill(
                "death_mark",
                "Death Mark",
                "Marks target for execution.",
                SkillType.DAMAGE,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_6,
                true,
                6,
                100,
                6,
                1.20f,
                0f,
                0.50f,
                0f,0f,
                Arrays.asList(new SkillEffect("take_30pct_more", EffectType.DEBUFF, 0.30f, 3, 0))
        ));

        putSkill(new SimpleSkill(
                "ambush",
                "Ambush",
                "Strikes first from shadows.",
                SkillType.DAMAGE,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_7,
                false,
                7,
                100,
                3,
                1.30f,
                0f,
                0.40f,
                0f,0f,
                Arrays.asList(new SkillEffect("first_strike_if_ab_full", EffectType.BUFF, 1f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "bleeding_cut",
                "Bleeding Cut",
                "Inflicts a bleeding wound.",
                SkillType.DOT,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_8,
                false,
                8,
                100,
                3,
                1.00f,
                0f,
                0.30f,
                0f,0f,
                Arrays.asList(new SkillEffect("bleed", EffectType.DOT, 0.10f, 2, 0))
        ));

        putSkill(new SimpleSkill(
                "shadowstep",
                "Shadowstep",
                "Steps into shadows to evade.",
                SkillType.BUFF,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_9,
                false,
                9,
                50,
                4,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("dodge_and_restore_sta", EffectType.BUFF, 0.10f, 1, 0))
        ));

        putSkill(new SimpleSkill(
                "cripple",
                "Cripple",
                "Weakens enemy legs.",
                SkillType.DEBUFF,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_10,
                false,
                10,
                100,
                3,
                1.00f,
                0f,
                0f,
                0.20f,0f,
                Arrays.asList(new SkillEffect("reduce_enemy_str_20", EffectType.DEBUFF, 0.20f, 2, 0))
        ));

        putSkill(new SimpleSkill(
                "rapid_strikes",
                "Rapid Strikes",
                "Flurry of light blows.",
                SkillType.DAMAGE,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_11,
                false,
                11,
                100,
                4,
                0.60f * 3, // simplified as total multiplier
                0f,
                0.20f,
                0f,0f,
                Arrays.asList(new SkillEffect("higher_crit", EffectType.BUFF, 0.15f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "shadow_clone",
                "Shadow Clone",
                "Summons illusion to absorb damage.",
                SkillType.BUFF,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_12,
                false,
                12,
                50,
                5,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("clone_absorb_next", EffectType.SHIELD, 0f, 1, 0))
        ));

        putSkill(new SimpleSkill(
                "assassins_finale",
                "Assassin's Finale",
                "Executes weakened foes.",
                SkillType.DAMAGE,
                ClassType.ROGUE,
                R.drawable.ic_rogue_skill_13,
                true,
                13,
                100,
                6,
                2.00f,
                0f,
                0.60f,
                0f,0f,
                Arrays.asList(new SkillEffect("execute_under_20", EffectType.DEBUFF, 0f, 0, 0))
        ));


        // ---------- WARRIOR SKILLS ----------
        putSkill(new SimpleSkill(
                "sword_slash",
                "Sword Slash",
                "Standard sword strike.",
                SkillType.DAMAGE,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_1,
                false,
                1,
                100,
                1,
                1.10f,
                0f,0f,0f,0f,
                Collections.emptyList()
        ));

        putSkill(new SimpleSkill(
                "charge_strike",
                "Charge Strike",
                "Rushing attack.",
                SkillType.DAMAGE,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_2,
                false,
                2,
                100,
                2,
                1.30f,
                0f,
                0.30f,
                0f,0f,
                Arrays.asList(new SkillEffect("act_first_bonus", EffectType.BUFF, 0.10f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "sword_aura_strike",
                "Sword Aura Strike",
                "Attack empowered by sword aura.",
                SkillType.DAMAGE,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_3,
                false,
                3,
                100,
                3,
                1.20f,
                0f,
                0.20f,
                0f,0f,
                Arrays.asList(new SkillEffect("extra_damage_if_enemy_buffed", EffectType.BUFF, 0.25f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "battle_cry",
                "Battle Cry",
                "Roar boosts strength.",
                SkillType.BUFF,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_4,
                false,
                4,
                50,
                3,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("buff_str_20", EffectType.BUFF, 0.20f, 2, 0))
        ));

        putSkill(new SimpleSkill(
                "executioners_slash",
                "Executioner's Slash",
                "Finishing blow.",
                SkillType.DAMAGE,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_5,
                false,
                5,
                100,
                4,
                1.60f,
                0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("double_damage_under_30pct", EffectType.BUFF, 2.0f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "blazing_tempest",
                "Blazing Tempest",
                "Whirlwind of strikes.",
                SkillType.DAMAGE,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_6,
                true,
                6,
                100,
                6,
                0.80f * 4, // representing 3-5 × 80% as a simplified multiplier
                0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("extra_action_if_kill", EffectType.BUFF, 1f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "cleave",
                "Cleave",
                "Sweeping sword attack.",
                SkillType.DAMAGE,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_7,
                false,
                7,
                100,
                3,
                1.20f,
                0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("bonus_vs_high_hp", EffectType.BUFF, 0.30f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "parry",
                "Parry",
                "Defensive stance.",
                SkillType.COUNTER,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_8,
                false,
                8,
                0,
                3,
                0.60f,
                0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("block_and_counter", EffectType.BUFF, 0f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "adrenaline_rush",
                "Adrenaline Rush",
                "Push beyond limits.",
                SkillType.BUFF,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_9,
                false,
                9,
                50,
                4,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("extra_action", EffectType.BUFF, 1f, 0, 0),
                        new SkillEffect("hp_cost_5percent", EffectType.DEBUFF, 0.05f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "heros_guard",
                "Hero's Guard",
                "Balanced defense.",
                SkillType.BUFF,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_10,
                false,
                10,
                50,
                4,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("buff_end_20", EffectType.BUFF, 0.20f, 2, 0),
                        new SkillEffect("heal_10pct", EffectType.BUFF, 0.10f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "overpower",
                "Overpower",
                "Shatters enemy defenses.",
                SkillType.DAMAGE,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_11,
                false,
                11,
                100,
                3,
                1.40f,
                0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("ignore_enemy_buffs", EffectType.DEBUFF, 0f, 0, 0))
        ));

        putSkill(new SimpleSkill(
                "rallying_roar",
                "Rallying Roar",
                "Inspires inner strength.",
                SkillType.BUFF,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_12,
                false,
                12,
                50,
                4,
                0f,0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("buff_atk_20_sta_regen", EffectType.BUFF, 0.20f, 2, 0))
        ));

        putSkill(new SimpleSkill(
                "colossus_strike",
                "Colossus Strike",
                "Devastating single hit.",
                SkillType.DAMAGE,
                ClassType.WARRIOR,
                R.drawable.ic_warrior_skill_13,
                true,
                13,
                100,
                6,
                2.50f,
                0f,0f,0f,0f,
                Arrays.asList(new SkillEffect("bypass_def_20chance", EffectType.DEBUFF, 0.20f, 0, 0))
        ));
    }

    // Helper to put skill to map (ensures non-null id)
    private static void putSkill(SkillModel s) {
        if (s != null && s.getId() != null) SKILL_MAP.put(s.getId(), s);
    }

    // --- Simple concrete implementations to avoid adding extra files ---
    private static class SimpleSkill extends SkillModel {

        protected SimpleSkill(String id, String name, String description,
                              SkillType type, ClassType allowedClass,
                              int iconRes, boolean isUltimate, int levelUnlock,
                              int abCost, int baseCooldown,
                              float strScaling, float endScaling, float agiScaling,
                              float flxScaling, float staScaling,
                              List<SkillEffect> effects) {
            super(id, name, description, type, allowedClass, iconRes, isUltimate,
                    levelUnlock, abCost, baseCooldown,
                    strScaling, endScaling, agiScaling, flxScaling, staScaling, effects);
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            // Placeholder execution behavior:
            // - integrate with your CombatContext to apply damage/effects, logging, AB usage, cooldown management.
            // For now, keep minimal to allow compilation.
            System.out.println("Executing skill: " + getName() + " by " + (user != null ? user.getName() : "unknown"));
            // TODO: implement real combat logic
        }
    }

    private static class SimplePassive extends PassiveSkill {
        protected SimplePassive(String id, String name, String description, int levelUnlock, int iconResId,
                                ClassType allowedClass, float critBonus, float strDamageBonus, float defScalingBonus) {
            super(id, name, description, levelUnlock, iconResId, allowedClass, critBonus, strDamageBonus, defScalingBonus);
        }

        // Override hooks optionally in future.
    }
}
