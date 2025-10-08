package com.example.fitquest;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * EnemySkillRepository
 *
 * Concrete enemy skills and passives for SLIME, VENOPODS, FLAME WOLF, and SLIME KING.
 *
 * NOTE:
 * - Skill subclasses apply damage via CombatContext.applyDamage(...)
 * - Skills include SkillEffect metadata for UI/tooltips.
 * - When you have concrete StatusEffect implementations in your codebase,
 *   extend/replace the "log-only" effect lines with context.applyStatusEffect(...).
 */
public final class EnemySkillRepository {

    /**
     * Call this to ensure all enemy skills/passives are registered (forces static block to run).
     */
    public static void init() {
        // No-op: calling this ensures static block runs
    }

    // --- Skill/Passive Maps for ID lookup ---
    private static final java.util.Map<String, SkillModel> ENEMY_SKILL_MAP = new java.util.HashMap<>();
    private static final java.util.Map<String, PassiveSkill> ENEMY_PASSIVE_MAP = new java.util.HashMap<>();

    static {
        // Register all enemy skills and passives (singletons)
        // Slime
        AcidicShot acidicShot = new AcidicShot();
        OozeSlam oozeSlam = new OozeSlam();
        ToxicBurst toxicBurst = new ToxicBurst();
        ENEMY_SKILL_MAP.put(acidicShot.getId(), acidicShot);
        ENEMY_SKILL_MAP.put(oozeSlam.getId(), oozeSlam);
        ENEMY_SKILL_MAP.put(toxicBurst.getId(), toxicBurst);
        RegenerativeOoze regenerativeOoze = new RegenerativeOoze();
        ENEMY_PASSIVE_MAP.put(regenerativeOoze.getId(), regenerativeOoze);

        // Venopods
        VenomSpikes venomSpikes = new VenomSpikes();
        TentacleCrush tentacleCrush = new TentacleCrush();
        RottingLash rottingLash = new RottingLash();
        ENEMY_SKILL_MAP.put(venomSpikes.getId(), venomSpikes);
        ENEMY_SKILL_MAP.put(tentacleCrush.getId(), tentacleCrush);
        ENEMY_SKILL_MAP.put(rottingLash.getId(), rottingLash);
        ToxicOverflow toxicOverflow = new ToxicOverflow();
        ENEMY_PASSIVE_MAP.put(toxicOverflow.getId(), toxicOverflow);

        // Flame Wolf
        ScorchingBite scorchingBite = new ScorchingBite();
        MoltenStrike moltenStrike = new MoltenStrike();
        InfernalRush infernalRush = new InfernalRush();
        ENEMY_SKILL_MAP.put(scorchingBite.getId(), scorchingBite);
        ENEMY_SKILL_MAP.put(moltenStrike.getId(), moltenStrike);
        ENEMY_SKILL_MAP.put(infernalRush.getId(), infernalRush);
        BurningFury burningFury = new BurningFury();
        ENEMY_PASSIVE_MAP.put(burningFury.getId(), burningFury);

        // Slime King
        RoyalSmash royalSmash = new RoyalSmash();
        AcidBeam acidBeam = new AcidBeam();
        SlimeShockwave slimeShockwave = new SlimeShockwave();
        ENEMY_SKILL_MAP.put(royalSmash.getId(), royalSmash);
        ENEMY_SKILL_MAP.put(acidBeam.getId(), acidBeam);
        ENEMY_SKILL_MAP.put(slimeShockwave.getId(), slimeShockwave);
        OverwhelmingPressure overwhelmingPressure = new OverwhelmingPressure();
        ENEMY_PASSIVE_MAP.put(overwhelmingPressure.getId(), overwhelmingPressure);
    }

    /** Lookup an enemy skill by ID. */
    public static SkillModel getEnemySkillById(String id) {
        return ENEMY_SKILL_MAP.get(id);
    }

    /** Lookup an enemy passive by ID. */
    public static PassiveSkill getEnemyPassiveById(String id) {
        return ENEMY_PASSIVE_MAP.get(id);
    }

    private EnemySkillRepository() {}

    // --- SLIME ---
    public static List<SkillModel> getSlimeSkills() {
    return List.of(
        ENEMY_SKILL_MAP.get("acidic_shot"),
        ENEMY_SKILL_MAP.get("ooze_slam"),
        ENEMY_SKILL_MAP.get("toxic_burst")
    );
    }

    public static List<PassiveSkill> getSlimePassives() {
    return List.of(
        ENEMY_PASSIVE_MAP.get("regenerative_ooze")
    );
    }

    // --- VENOPODS ---
    public static List<SkillModel> getVenopodsSkills() {
    return List.of(
        ENEMY_SKILL_MAP.get("venom_spikes"),
        ENEMY_SKILL_MAP.get("tentacle_crush"),
        ENEMY_SKILL_MAP.get("rotting_lash")
    );
    }

    public static List<PassiveSkill> getVenopodsPassives() {
    return List.of(
        ENEMY_PASSIVE_MAP.get("toxic_overflow")
    );
    }

    // --- FLAME WOLF ---
    public static List<SkillModel> getFlameWolfSkills() {
    return List.of(
        ENEMY_SKILL_MAP.get("scorching_bite"),
        ENEMY_SKILL_MAP.get("molten_strike"),
        ENEMY_SKILL_MAP.get("infernal_rush")
    );
    }

    public static List<PassiveSkill> getFlameWolfPassives() {
    return List.of(
        ENEMY_PASSIVE_MAP.get("burning_fury")
    );
    }

    // --- SLIME KING ---
    public static List<SkillModel> getSlimeKingSkills() {
    return List.of(
        ENEMY_SKILL_MAP.get("royal_smash"),
        ENEMY_SKILL_MAP.get("acid_beam"),
        ENEMY_SKILL_MAP.get("slime_shockwave")
    );
    }

    public static List<PassiveSkill> getSlimeKingPassives() {
    return List.of(
        ENEMY_PASSIVE_MAP.get("overwhelming_pressure")
    );
    }

    // -------------------------------
    // --- SLIME: Skills & Passive ---
    // -------------------------------
    private static class AcidicShot extends SimpleEnemySkill {
        AcidicShot() {
            super("acidic_shot",
                    "Acidic Shot",
                    "Fires a glob of acid at one enemy.",
                    SkillType.DAMAGE,
                    /*allowedClass=*/ null,
                    R.drawable.ic_enemy_slime_skill1,
                    false,
                    1,
                    50,
                    1,
                    1.00f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("armor_down_10", EffectType.DEBUFF, 0.10f, 2, R.drawable.ic_effect_debuff))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            context.applyDamage(user, target, damage, this);
            // apply armor reduction: if you have a StatusEffect class, convert this to applyStatusEffect
            context.pushLog(String.format(Locale.US, "%s hits %s with Acidic Shot dealing %d and reduces armor by 10%%.",
                    userName(user), userName(target), damage));
            // SkillEffect metadata already contains armor_debuff for UI
        }
    }

    private static class OozeSlam extends SimpleEnemySkill {
        OozeSlam() {
            super("ooze_slam",
                    "Ooze Slam",
                    "Slime hardens and slams the target.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_slime_passive,
                    false,
                    1,
                    60,
                    2,
                    1.00f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("slow_20", EffectType.DEBUFF, 0.20f, 1, R.drawable.ic_effect_debuff))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            context.applyDamage(user, target, damage, this);
            // 20% chance to slow: log for now
            if (Math.random() < 0.20) {
                context.pushLog(String.format(Locale.US, "%s is slowed by Ooze Slam.", userName(target)));
                // convert to an actual StatusEffect if available
            } else {
                context.pushLog(String.format(Locale.US, "%s resists the slow.", userName(target)));
            }
        }
    }

    private static class ToxicBurst extends SimpleEnemySkill {
        ToxicBurst() {
            super("toxic_burst",
                    "Toxic Burst",
                    "Slime compresses toxins then explodes.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_slime_skill3,
                    false,
                    1,
                    100,
                    3,
                    1.20f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("poison_dot", EffectType.DOT, 0.10f, 3, R.drawable.ic_effect_poison))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            context.applyDamage(user, target, damage, this);
            context.pushLog(String.format(Locale.US, "%s unleashes Toxic Burst for %d damage and applies poison.", userName(user), damage));
            // actual DOT application should use StatusEffect when available
        }
    }

    private static class RegenerativeOoze extends PassiveSkill {
        RegenerativeOoze() {
            super("regenerative_ooze",
                    "Regenerative Ooze",
                    "The slime constantly reforms, healing itself slowly.",
                    1,
                    R.drawable.ic_enemy_slime_passive,
                    /*allowedClass=*/ null,
                    0f, 0f, 0f,
                    true,
                    "onTurnStart");
        }

        @Override
        public void onTurnStart(Character user, CombatContext ctx) {
            // Heal 2-5% HP
            int heal = Math.max(1, (int) (user.getMaxHp() * (2 + Math.random() * 4) / 100.0));
            user.heal(heal);
            ctx.pushLog(String.format(Locale.US, "%s regenerates %d HP (Regenerative Ooze).", userName(user), heal));
        }
    }

    // -------------------------------
    // --- VENOPODS: Skills & Passive -
    // -------------------------------
    private static class VenomSpikes extends SimpleEnemySkill {
        VenomSpikes() {
            super("venom_spikes",
                    "Venom Spikes",
                    "Launches hardened venom spikes.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_venopods_skill1,
                    false,
                    1,
                    50,
                    1,
                    1.10f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("poison_crit_10", EffectType.BUFF, 0.10f, 0, R.drawable.ic_effect_poison))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            context.applyDamage(user, target, damage, this);
            // 10% chance to apply poison crit: log for now
            if (Math.random() < 0.10) {
                context.pushLog(String.format(Locale.US, "%s's Venom Spikes inflict poison (crit poison).", userName(user)));
            }
        }
    }

    private static class TentacleCrush extends SimpleEnemySkill {
        TentacleCrush() {
            super("tentacle_crush",
                    "Tentacle Crush",
                    "Wraps and crushes the enemy over time.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_venopods_skill2,
                    false,
                    1,
                    100,
                    2,
                    1.20f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("crush_over_time", EffectType.DEBUFF, 0f, 2, R.drawable.ic_effect_debuff))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            context.applyDamage(user, target, damage, this);
            context.pushLog(String.format(Locale.US, "%s crushes %s over 2 turns.", userName(user), userName(target)));
        }
    }

    private static class RottingLash extends SimpleEnemySkill {
        RottingLash() {
            super("rotting_lash",
                    "Rotting Lash",
                    "Strikes with a decaying tentacle; physical + poison damage.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_venopods_skill3,
                    false,
                    1,
                    100,
                    3,
                    1.30f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("poison_combo", EffectType.DOT, 0.08f, 3, R.drawable.ic_effect_poison))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            context.applyDamage(user, target, damage, this);
            context.pushLog(String.format(Locale.US, "%s deals %d physical+poison with Rotting Lash.", userName(user), damage));
        }
    }

    private static class ToxicOverflow extends PassiveSkill {
        ToxicOverflow() {
            super("toxic_overflow",
                    "Toxic Overflow",
                    "Whenever hit, Venopods deals minor poison damage back.",
                    1,
                    R.drawable.ic_enemy_venopods_passive,
                    null,
                    0f, 0f, 0f,
                    true,
                    "onDamageTaken");
        }

        @Override
        public void onDamageTaken(Character user, float damage, CombatContext ctx) {
            // Deal small poison back to attacker if last attacker is available via context logs
            // We don't have reference to last attacker here; CombatContext's damage hooks would call onDamageTaken with the damage,
            // and CombatContext will usually call passive onDamageTaken with (user, damage, ctx). We will log an expected retaliation.
            // If you want to apply actual retaliation, call applyDamage in the code path where attacker is known.
            ctx.pushLog(String.format(Locale.US, "%s's Toxic Overflow triggers and will retaliate with poison.", userName(user)));
            // Implementation note: If CombatContext can pass the attacker, call attacker.applyDamage(...) here
        }
    }

    // --------------------------------
    // --- FLAME WOLF: Skills/Passive
    // --------------------------------
    private static class ScorchingBite extends SimpleEnemySkill {
        ScorchingBite() {
            super("scorching_bite",
                    "Scorching Bite",
                    "Bites with flaming jaws; applies burn.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_flamewolf_skill1,
                    false,
                    1,
                    50,
                    1,
                    1.20f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("burn_2", EffectType.DOT, 0.08f, 2, R.drawable.ic_effect_poison))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            context.applyDamage(user, target, damage, this);
            context.pushLog(String.format(Locale.US, "%s bites with flames for %d damage and applies burn.", userName(user), damage));
        }
    }

    private static class MoltenStrike extends SimpleEnemySkill {
        MoltenStrike() {
            super("molten_strike",
                    "Molten Strike",
                    "Claws ignite with molten heat.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_flamewolf_skill2,
                    false,
                    1,
                    60,
                    2,
                    1.30f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("heavy_fire", EffectType.DEBUFF, 0f, 0, R.drawable.ic_effect_debuff))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            context.applyDamage(user, target, damage, this);
            context.pushLog(String.format(Locale.US, "%s hits %s with Molten Strike for %d fire damage.", userName(user), userName(target), damage));
        }
    }

    private static class InfernalRush extends SimpleEnemySkill {
        InfernalRush() {
            super("infernal_rush",
                    "Infernal Rush",
                    "Charges engulfed in flames; chance to knock back.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_flamewolf_skill3,
                    false,
                    1,
                    70,
                    3,
                    1.00f, 0f, 0.30f, 0f, 0f,
                    Arrays.asList(new SkillEffect("knockback_chance", EffectType.BUFF, 0.20f, 0, R.drawable.ic_effect_debuff))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            context.applyDamage(user, target, damage, this);
            if (Math.random() < 0.15) {
                context.pushLog(String.format(Locale.US, "%s knocks back %s with Infernal Rush.", userName(user), userName(target)));
            }
        }
    }

    private static class BurningFury extends PassiveSkill {
        BurningFury() {
            super("burning_fury",
                    "Burning Fury",
                    "Each time HP drops by 25%, the next attack deals +20% fire damage.",
                    1,
                    R.drawable.ic_enemy_flamewolf_passive,
                    null,
                    0f, 0f, 0f,
                    true,
                    "onDamageTaken");
        }

        @Override
        public void onDamageTaken(Character user, float damage, CombatContext ctx) {
            // Check threshold crossings
            int hp = user.getCurrentHp();
            int max = user.getMaxHp();
            // We'll set a simple "flag" status on the avatar via a SkillEffect in the passive description (UI), but for runtime,
            // we log and you may implement a StatusEffect to store this "next strike buff".
            if (hp > 0 && (hp * 4 <= max * 3)) { // if dropped by 25% or more relative? approximate
                ctx.pushLog(String.format(Locale.US, "%s triggers Burning Fury; next attack will deal +20%% fire damage.", userName(user)));
                // Implementers: apply a temporary status or set a flag on Character for next attack
            }
        }
    }

    // --------------------------------
    // --- SLIME KING: Skills/Passive
    // --------------------------------
    private static class RoyalSmash extends SimpleEnemySkill {
        RoyalSmash() {
            super("royal_smash",
                    "Royal Smash",
                    "Slams enemy with giant slime fists.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_slimeking_skill3,
                    false,
                    1,
                    80,
                    2,
                    1.50f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("crit_chance_20", EffectType.BUFF, 0.20f, 0, R.drawable.ic_effect_buff))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            // 20% chance to critical
            if (Math.random() < 0.20) {
                damage = (int) (damage * 1.5);
                context.pushLog(String.format(Locale.US, "%s lands a critical Royal Smash!", userName(user)));
            }
            context.applyDamage(user, target, damage, this);
        }
    }

    private static class AcidBeam extends SimpleEnemySkill {
        AcidBeam() {
            super("acid_beam",
                    "Acid Beam",
                    "Fires concentrated acid ray.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_slimeking_skill2,
                    false,
                    1,
                    70,
                    3,
                    1.30f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("def_down_15", EffectType.DEBUFF, 0.15f, 2, R.drawable.ic_effect_debuff))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            context.applyDamage(user, target, damage, this);
            context.pushLog(String.format(Locale.US, "%s fires Acid Beam dealing %d and reduces defense by 15%%.", userName(user), damage));
        }
    }

    private static class SlimeShockwave extends SimpleEnemySkill {
        SlimeShockwave() {
            super("slime_shockwave",
                    "Slime Shockwave",
                    "Compresses body, releasing slime energy that partially ignores defense.",
                    SkillType.DAMAGE,
                    null,
                    R.drawable.ic_enemy_slimeking_skill1,
                    false,
                    1,
                    90,
                    4,
                    1.40f, 0f, 0f, 0f, 0f,
                    Arrays.asList(new SkillEffect("ignore_def_part", EffectType.DEBUFF, 0.25f, 0, R.drawable.ic_effect_debuff))
            );
        }

        @Override
        public void execute(Character user, Character target, CombatContext context) {
            int damage = estimateDamage(user);
            // "Ignores part of defense" is logged; you can implement actual ignore in OnIncomingDamageHook
            context.applyDamage(user, target, damage, this);
            context.pushLog(String.format(Locale.US, "%s crushes with Slime Shockwave for %d (ignores part of defense).", userName(user), damage));
        }
    }

    private static class OverwhelmingPressure extends PassiveSkill {
        OverwhelmingPressure() {
            super("overwhelming_pressure",
                    "Overwhelming Pressure",
                    "At the start of its turn, deals minor unavoidable magic damage to the enemy.",
                    1,
                    R.drawable.ic_enemy_slimeking_passive,
                    null,
                    0f, 0f, 0f,
                    true,
                    "onTurnStart");
        }

        @Override
        public void onTurnStart(Character user, CombatContext ctx) {
            // Deal small unavoidable magic damage to opponent
            Character opponent = (ctx != null && user != null) ? (ctx.getResult() == CombatContext.Result.ONGOING ? null : null) : null;
            // We don't have direct opponent reference here; CombatContext usually will call passives with the user parameter only.
            // Instead, just log an expected behavior; implementers can tie this in the main CombatContext turn loop:
            ctx.pushLog(String.format(Locale.US, "%s emanates Overwhelming Pressure dealing minor unavoidable magic damage.", userName(user)));
            // Implementation note: to actually apply damage, CombatContext should call this passive and pass the opponent or provide helper to resolve.
        }
    }

    // ---------------------------
    // --- Helper base classes ---
    // ---------------------------
    private abstract static class SimpleEnemySkill extends SkillModel {
        protected SimpleEnemySkill(String id, String name, String description, SkillType type,
                                   ClassType allowedClass, int iconRes, boolean isUltimate, int levelUnlock,
                                   int abCost, int baseCooldown,
                                   float strScaling, float endScaling, float agiScaling, float flxScaling, float staScaling,
                                   List<SkillEffect> effects) {
            super(id, name, description, type, allowedClass, iconRes, isUltimate, levelUnlock,
                    abCost, baseCooldown, strScaling, endScaling, agiScaling, flxScaling, staScaling, effects);
        }

        /**
         * Estimates damage using the skill's scaling and user's effective stats.
         * Subclasses call CombatContext.applyDamage(...) with this value.
         */
        protected int estimateDamage(Character user) {
            double dmg = 0.0;
            if (getStrScaling() > 0) dmg += getStrScaling() * user.getEffectiveStrength();
            if (getEndScaling() > 0) dmg += getEndScaling() * user.getEffectiveEndurance();
            if (getAgiScaling() > 0) dmg += getAgiScaling() * user.getEffectiveAgility();
            if (getFlxScaling() > 0) dmg += getFlxScaling() * user.getEffectiveFlexibility();
            if (getStaScaling() > 0) dmg += getStaScaling() * user.getEffectiveStamina();
            return Math.max(1, (int) Math.round(dmg));
        }
    }

    // Small helpers to avoid repeating calls
    private static String userName(Character c) {
        if (c == null || c.getAvatar() == null) return "Unknown";
        String n = c.getAvatar().getUsername();
        return (n == null || n.isEmpty()) ? "Enemy" : n;
    }

}
