package com.example.fitquest;

/**
 * Base abstract model for all skills in FitQuest combat.
 * Each concrete skill defines its own execution logic.
 */
public abstract class SkillModel {

    // --- Basic Info ---
    private final String id;            // unique identifier (safe for save/load/online sync)
    private final String name;
    private final String description;
    private final SkillType type;       // DAMAGE, BUFF, HEAL, etc.
    private final ClassType allowedClass; // which class can use this skill
    private final boolean isUltimate;   // true = ultimate skill
    private final int levelUnlock;      // required level to unlock

    // --- Costs & Cooldowns ---
    private final int abCost;           // Action Bar cost (e.g. 50% for buffs, 100% for attacks)
    private final int baseCooldown;     // base cooldown in turns
    private int currentCooldown;        // tracks if the skill is still cooling down

    // --- Stat Scaling (percent multipliers) ---
    private final float strScaling;
    private final float endScaling;
    private final float agiScaling;
    private final float flxScaling;
    private final float staScaling;

    // --- Constructor ---
    protected SkillModel(String id,
                         String name,
                         String description,
                         SkillType type,
                         ClassType allowedClass,
                         boolean isUltimate,
                         int levelUnlock,
                         int abCost,
                         int baseCooldown,
                         float strScaling,
                         float endScaling,
                         float agiScaling,
                         float flxScaling,
                         float staScaling) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.allowedClass = allowedClass;
        this.isUltimate = isUltimate;
        this.levelUnlock = levelUnlock;
        this.abCost = abCost;
        this.baseCooldown = baseCooldown;
        this.currentCooldown = 0;
        this.strScaling = strScaling;
        this.endScaling = endScaling;
        this.agiScaling = agiScaling;
        this.flxScaling = flxScaling;
        this.staScaling = staScaling;
    }

    // --- Execution ---
    /**
     * Executes the skill effect.
     * @param user   The character using the skill.
     * @param target The skill target (can be user if self-buff/heal).
     * @param context Provides combat state (turn order, log, buffs, etc.).
     */
    public abstract void execute(Character user, Character target, CombatContext context);

    // --- Cooldown Handling ---
    public boolean isOnCooldown() {
        return currentCooldown > 0;
    }

    public void reduceCooldown() {
        if (currentCooldown > 0) currentCooldown--;
    }

    public void resetCooldown() {
        currentCooldown = baseCooldown;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillType getType() { return type; }
    public ClassType getAllowedClass() { return allowedClass; }
    public boolean isUltimate() { return isUltimate; }
    public int getLevelUnlock() { return levelUnlock; }
    public int getAbCost() { return abCost; }
    public int getBaseCooldown() { return baseCooldown; }
    public int getCurrentCooldown() { return currentCooldown; }

    public float getStrScaling() { return strScaling; }
    public float getEndScaling() { return endScaling; }
    public float getAgiScaling() { return agiScaling; }
    public float getFlxScaling() { return flxScaling; }
    public float getStaScaling() { return staScaling; }
}

enum SkillType {
    DAMAGE,
    BUFF,
    DEBUFF,
    HEAL,
    SHIELD,
    DOT // Damage over time
}

enum ClassType {
    TANK,
    ROGUE,
    WARRIOR,
    UNIVERSAL
}

/*
🛡 Tank Skills
Skill Name	Type	Description	Scaling	AB / Turn Impact	Cooldown	Effect	Level Unlock	Equipable
Hammer Shatter	Attack	Massive hammer strike that cracks enemy defenses.	120% STR + 40% END	100%	3 turns	20% chance to reduce enemy DEF by 20% for 2 turns	1	✅
Iron Breaker	Attack	Crushing blow that ignores part of the enemy’s armor.	100% STR + 30% END	100%	2 turns	Ignores 25% of enemy DEF	2	✅
Tenacity Buff	Buff	Fortifies the body against damage.	Buff only	50%	3 turns	+20% DEF for 2 turns	3	✅
Shield Wall	Buff	Forms a wall of protection.	None	50%	4 turns	Reduces all incoming damage by 50% for 1 turn	4	✅
Iron Will	Heal	Summons inner strength to endure.	10% Max HP + 20% END	100%	5 turns	Removes all debuffs	5	✅
Earthshatter (Ultimate)	Attack	Hammer smashes the ground with devastating force.	200% STR + 50% END	100%	6 turns	30% chance to stun enemy 1 turn	6	✅
Stonewall	Buff	Braces for impact with immovable defense.	None	50%	4 turns	Reduces damage by 30% for 2 turns	7	✅
Fortify	Heal	Restores health at the cost of offense.	Heal = 15% missing HP	100%	4 turns	-20% ATK for 1 turn	8	✅
Body Slam	Attack	Leaps body-first into the enemy.	100% STR + 20% END	100%	3 turns	25% chance to stun	9	✅
Bulwark Counter	Counter	Reactively counters attacks.	80% STR	Triggered	4 turns	Deals counter damage if hit	10	✅
Unbreakable	Buff	Immune to negative effects for a time.	None	50%	5 turns	Immune to debuffs 2 turns	11	✅
Titan’s Wrath (Ultimate)	Attack	Crushing strike that risks leaving you open.	250% STR	100%	6 turns	Cannot defend next turn	12	✅
Ground Slam	Attack	Slams the ground to knock back enemy.	90% STR + 20% END	100%	3 turns	Reduces enemy AB by 20%	13	✅
Passives (always active, not equipable)
Passive Name	Effect	Level Unlock
Stoneheart	DEF scaling +10% when below 50% HP	1
Second Wind	Restores 15% HP once per battle below 20% HP	1
________________________________________
🗡 Rogue Skills
Skill Name	Type	Description	Scaling	AB / Turn Impact	Cooldown	Effect	Level Unlock	Equipable
Backstab	Attack	Deadly strike from shadows.	150% STR + 50% AGI	100%	3	+30% crit if target debuffed	1	✅
Dagger Throw	Attack	Reliable ranged strike.	100% STR + 30% AGI	100%	2	Always hits	2	✅
Shadow Slash	Attack	Fast attack with chance to double hit.	90% STR + 40% AGI	100%	2	25% chance double strike	3	✅
Smoke Veil	Buff	Vanishes into smoke to avoid danger.	None	50%	4	Dodge next attack; next strike guaranteed crit	4	✅
Venom Blades	DOT	Infuses weapons with poison.	80% STR + DOT	100%	4	Applies poison 3 turns	5	✅
Death Mark (Ultimate)	Attack	Marks target for execution.	120% STR + 50% AGI	100%	6	Enemy takes +30% damage 3 turns	6	✅
Ambush	Attack	Strikes first from shadows.	130% STR + 40% AGI	100%	3	Guaranteed first strike if AB full	7	✅
Bleeding Cut	DOT	Inflicts a bleeding wound.	100% STR + DOT	100%	3	Enemy suffers bleed 2 turns	8	✅
Shadowstep	Buff	Steps into shadows to evade.	None	50%	4	Dodges next attack; restores 10% STA	9	✅
Cripple	Debuff	Weakens enemy legs.	100% STR + 20% FLX	100%	3	Reduces enemy STR 20% 2 turns	10	✅
Rapid Strikes	Attack	Flurry of light blows.	3 × (60% STR + 20% AGI)	100%	4	Higher crit chance	11	✅
Shadow Clone	Buff	Summons illusion to absorb damage.	None	50%	5	Clone absorbs next attack	12	✅
Assassin’s Finale (Ultimate)	Attack	Executes weakened foes.	200% STR + 60% AGI	100%	6	Instantly kills target <20% HP	13	✅
Passives
Passive Name	Effect	Level Unlock
Shadow Instinct	+10% crit baseline	1
Killer’s Momentum	Reset cooldown on kill (1x per battle)	1
________________________________________
⚔ Warrior Skills
Skill Name	Type	Description	Scaling	AB / Turn Impact	Cooldown	Effect	Level Unlock	Equipable
Sword Slash	Attack	Standard sword strike.	110% STR	100%	1	None	1	✅
Charge Strike	Attack	Rushing attack.	130% STR + 30% AGI	100%	2	+10% chance to act first if AB full	2	✅
Sword Aura Strike	Attack	Attack empowered by sword aura.	120% STR + 20% AGI	100%	3	+25% damage if enemy buffed	3	✅
Battle Cry	Buff	Roar boosts strength.	Buff only	50%	3	+20% STR 2 turns	4	✅
Executioner’s Slash	Attack	Finishing blow.	160% STR	100%	4	Double damage if enemy HP <30%	5	✅
Blazing Tempest (Ultimate)	Attack	Whirlwind of strikes.	3–5 × 80% STR	100%	6	Extra action if target killed	6	✅
Cleave	Attack	Sweeping sword attack.	120% STR	100%	3	+30% damage if enemy HP >70%	7	✅
Parry	Counter	Defensive stance.	60% STR	Triggered	3	Blocks + counter	8	✅
Adrenaline Rush	Buff	Push beyond limits.	None	50%	4	Gain 1 extra action; lose 5% HP	9	✅
Hero’s Guard	Buff	Balanced defense.	None	50%	4	+20% END 2 turns; heal 10% HP	10	✅
Overpower	Attack	Shatters enemy defenses.	140% STR	100%	3	Ignores enemy buffs	11	✅
Rallying Roar	Buff	Inspires inner strength.	Buff only	50%	4	+20% ATK & STA regen 2 turns	12	✅
Colossus Strike (Ultimate)	Attack	Devastating single hit.	250% STR	100%	6	20% chance to bypass defenses	13	✅
Passives
Passive Name	Effect	Level Unlock
Momentum	+5% damage stacking each consecutive attack	1
Weapon Mastery	+5% STR skill damage	1
 */
