package com.example.fitquest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Character {

    private final AvatarModel avatar;

    // Combat stats
    private int maxHp;
    private int currentHp;
    private int actionBar; // 0-100
    private boolean isAlive;

    // Buffs & debuffs
    private final List<StatusEffect> statusEffects = new ArrayList<>();

    private final List<PassiveSkill> passiveSkills;

    // Skill cooldown tracking
    private final List<SkillModel> activeSkills;

    // --- Constructor ---
    public Character(AvatarModel avatar) {
        this.avatar = avatar;

        // --- Initialize Active Skills ---
        List<SkillModel> avatarSkills = avatar.getActiveSkills();
        if (avatarSkills == null || avatarSkills.isEmpty()) {
            ClassType type = avatar.getClassType();
            if (type != null) {
                this.activeSkills = new ArrayList<>(SkillRepository.getSkillsForClass(type));
            } else {
                this.activeSkills = new ArrayList<>();
            }
        } else {
            this.activeSkills = new ArrayList<>(avatarSkills);
        }

        // --- Initialize Passive Skills ---
        List<PassiveSkill> avatarPassives = avatar.getPassiveSkills();
        if (avatarPassives == null || avatarPassives.isEmpty()) {
            ClassType type = avatar.getClassType();
            if (type != null) {
                this.passiveSkills = new ArrayList<>(SkillRepository.getPassivesForClass(type));
            } else {
                this.passiveSkills = new ArrayList<>();
            }
            avatar.setPassiveSkills(this.passiveSkills); // Sync back to avatar
        } else {
            this.passiveSkills = new ArrayList<>(avatarPassives);
        }

        // --- Initialize stats ---
        recalcMaxHp();
        this.currentHp = this.maxHp;
        this.actionBar = 0;
        this.isAlive = true;
    }


    public Character(String name, int baseHP, int baseAttack, int baseDefense, int baseSpeed, AvatarModel avatar, List<PassiveSkill> passiveSkills, List<SkillModel> activeSkills) {

        this.avatar = avatar;
        this.passiveSkills = passiveSkills;
        this.activeSkills = activeSkills;
    }



    // --- Core Calculations ---
    private void recalcMaxHp() {
        // Base endurance *10 + chest physique bonus
        int baseHp = avatar.getEndurance() * 10;
        baseHp += avatar.getChestPoints() * 10;
        // Include gear boosts
        for (Map.Entry<String, String> entry : avatar.getEquippedGear().entrySet()) {
            GearModel gear = GearRepository.getGearById(entry.getValue());
            if (gear != null && gear.getStatBoosts() != null) {
                Float hpBoost = gear.getStatBoosts().get("HP");
                if (hpBoost != null) baseHp += hpBoost;
            }
        }
        this.maxHp = Math.max(50, baseHp);
    }

    public int calculateDamageOutput() {
        int str = getEffectiveStrength();
        return str * 2; // simple base formula
    }

    // --- Effective Stats (apply physique, gear, buffs) ---
    public int getEffectiveStrength() {
        int str = avatar.getStrength();
        str += (int) (str * (avatar.getArmPoints() * 0.05f)); // 5% per arm point
        str = applyGearStatBoosts("STR", str);
        return applyBuffsDebuffs("STR", str);
    }

    public int getEffectiveAgility() {
        int agi = avatar.getAgility();
        agi += (int) (agi * (avatar.getLegPoints() * 0.05f));
        agi = applyGearStatBoosts("AGI", agi);
        return applyBuffsDebuffs("AGI", agi);
    }

    public int getEffectiveEndurance() {
        int end = avatar.getEndurance();
        end += (int) (end * (avatar.getChestPoints() * 0.05f));
        end = applyGearStatBoosts("END", end);
        return applyBuffsDebuffs("END", end);
    }

    public int getEffectiveFlexibility() {
        int flx = avatar.getFlexibility();
        flx += (int) (flx * (avatar.getBackPoints() * 0.05f));
        flx = applyGearStatBoosts("FLX", flx);
        return applyBuffsDebuffs("FLX", flx);
    }

    public int getEffectiveStamina() {
        int sta = avatar.getStamina();
        sta = applyGearStatBoosts("STA", sta);
        return applyBuffsDebuffs("STA", sta);
    }

    private int applyGearStatBoosts(String stat, int baseValue) {
        int modified = baseValue;
        for (Map.Entry<String, String> entry : avatar.getEquippedGear().entrySet()) {
            GearModel gear = GearRepository.getGearById(entry.getValue());
            if (gear != null && gear.getStatBoosts() != null) {
                Float boost = gear.getStatBoosts().get(stat);
                if (boost != null) modified += boost;
            }
        }
        return modified;
    }

    private int applyBuffsDebuffs(String stat, int baseValue) {
        int modified = baseValue;
        for (StatusEffect effect : statusEffects) {
            modified = effect.modifyStat(stat, modified);
        }
        return modified;
    }

    // --- Buff/Debuff Handling ---
    public void addStatusEffect(StatusEffect effect) {
        statusEffects.add(effect);
    }

    public void processStatusEffects(CombatContext ctx) {
        Iterator<StatusEffect> iter = statusEffects.iterator();
        while (iter.hasNext()) {
            StatusEffect effect = iter.next();
            effect.onTurnStart(this, ctx); // ctx is now passed in
            if (effect.isExpired()) {
                iter.remove();
            }
        }
    }


    // --- Combat API ---
    public void takeDamage(int amount) {
        int reduced = Math.max(0, amount);
        currentHp -= reduced;
        if (currentHp <= 0) {
            currentHp = 0;
            isAlive = false;
        }
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public void increaseActionBar(int amount) {
        actionBar = Math.max(0, Math.min(100, actionBar + amount));
    }

    public boolean isActionBarFull() {
        return actionBar >= 100;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getActionBar() { return actionBar; }

    public AvatarModel getAvatar() { return avatar; }
    public List<StatusEffect> getStatusEffects() { return statusEffects; }
    public List<SkillModel> getActiveSkills() { return activeSkills; }

    // --- Skill Execution ---
    public boolean canUseSkill(SkillModel skill) {
        return !skill.isOnCooldown() && skill.getAbCost() <= actionBar;
    }

    public void useSkill(SkillModel skill, Character target, CombatContext context) {
        if (!canUseSkill(skill)) return;
        skill.execute(this, target, context);
        skill.resetCooldown();
        actionBar -= skill.getAbCost();
        if (actionBar < 0) actionBar = 0;
    }

    public void reduceAllCooldowns() {
        for (SkillModel skill : activeSkills) {
            skill.reduceCooldown();
        }
    }

    // --- Passive Skills Hooks ---
    public void onKill(Character target, CombatContext ctx) {
        for (PassiveSkill ps : avatar.getPassiveSkills()) {
            ps.onKill(this, target, ctx);
        }
    }

    public void onDamageTaken(float damage, CombatContext ctx) {
        for (PassiveSkill ps : avatar.getPassiveSkills()) {
            ps.onDamageTaken(this, damage, ctx);
        }
    }

    public void onTurnStart(CombatContext ctx) {
        for (PassiveSkill ps : avatar.getPassiveSkills()) {
            ps.onTurnStart(this, ctx);
        }
        processStatusEffects(ctx);
        reduceAllCooldowns();
    }

    // --- Utility ---
    public void recalcStatsFromAvatar() {
        recalcMaxHp();
        if (currentHp > maxHp) currentHp = maxHp;
    }

    public void setActionBar(int i) {
        actionBar = i;
    }

    public String getName() {
        return avatar.getUsername();
    }

    public int getLevel() {
        return avatar.getLevel();
    }

    public float getAttack() {
        return getEffectiveStrength();
    }

    public void applyStatusEffect(StatusEffect statusEffect) {
        statusEffects.add(statusEffect);
    }

    public void modifyTempStrength(int strBonus) {
        // Apply stat bonuses if needed
        getEffectiveStrength();
    }

    public void modifyTempAgility(int agiBonus) {
        // Apply stat bonuses if needed
        getEffectiveAgility();
    }

    public void modifyTempEndurance(int endBonus) {
        // Apply stat bonuses if needed
        getEffectiveEndurance();
    }
}
