package com.example.fitquest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AvatarModel {

    private String username;
    private String gender;
    private String playerClass;

    private String bodyStyle;
    private String outfit;
    private String weapon;

    private String hairOutline;
    private String hairFill;
    private String hairColor;

    private String eyesOutline;
    private String eyesFill;
    private String eyesColor;

    private String nose;
    private String lips;

    // Core progression
    private int coins;
    private int xp;
    private int level;
    private int rank;
    private int rankPoints;
    private String playerId;

    // Free points
    private int freePhysiquePoints = 1;
    private int freeAttributePoints = 1;

    // Physique stats
    private int armPoints = 0;
    private int legPoints = 0;
    private int chestPoints = 0;
    private int backPoints = 0;

    // Attributes
    private int strength = 0;
    private int endurance = 0;
    private int agility = 0;
    private int flexibility = 0;
    private int stamina = 0;
    private ProfileChangeListener listener;

    // --- Inventory ---
    private Set<String> ownedGearIds = new HashSet<>(); // all owned items (by ID)

    // --- Restricted Gear Slots ---
    private final Map<GearType, String> equippedGear = new EnumMap<>(GearType.class);

    private Map<String, GoalState> goalProgress = new HashMap<>();

    // --- NEW: Skill System ---
    private List<PassiveSkill> passiveSkills = new ArrayList<>(2); // fixed 2
    private List<SkillModel> activeSkills = new ArrayList<>(5);   // up to 5

    private List<BattleHistoryModel> battleHistory = new ArrayList<>();


    // Constructors
    public AvatarModel() {
        this.level = 1;
        this.xp = 0;
        this.coins = 0;
        this.rank = 0;
        this.rankPoints = 0;
        this.playerId = generatePlayerId();

        // Initialize stats to 0 first, will be set by class initialization
        this.strength = 0;
        this.endurance = 0;
        this.agility = 0;
        this.flexibility = 0;

        assignClassPassiveSkills(); // auto-assign passives at level 1
        initGearSlots();
    }

    public AvatarModel(String username, String gender, String playerClass,
                       String bodyStyle, String outfit, String weapon,
                       String hairOutline, String hairFill, String hairColor,
                       String eyesOutline, String eyesFill, String eyesColor,
                       String nose, String lips) {
        this();
        this.username = username;
        this.gender = gender;
        this.playerClass = playerClass;
        this.bodyStyle = bodyStyle;
        this.outfit = outfit;
        this.weapon = weapon;
        this.hairOutline = hairOutline;
        this.hairFill = hairFill;
        this.hairColor = hairColor;
        this.eyesOutline = eyesOutline;
        this.eyesFill = eyesFill;
        this.eyesColor = eyesColor;
        this.nose = nose;
        this.lips = lips;
        
        // Initialize class-based stats and skills
        initializeClassStats();
        initializeClassSkills();
    }

    // --- Gear Slot Setup ---
    private void initGearSlots() {
        for (GearType type : Arrays.asList(GearType.WEAPON, GearType.ARMOR, GearType.PANTS, GearType.BOOTS, GearType.ACCESSORY)) {
            equippedGear.put(type, null); // start empty
        }
    }

    public AvatarModel(AvatarModel other) {
        this.armPoints = other.armPoints;
        this.legPoints = other.legPoints;
        this.chestPoints = other.chestPoints;
        this.backPoints = other.backPoints;
        this.strength = other.strength;
        this.endurance = other.endurance;
        this.agility = other.agility;
        this.flexibility = other.flexibility;
        this.stamina = other.stamina;
        this.freePhysiquePoints = other.freePhysiquePoints;
        this.freeAttributePoints = other.freeAttributePoints;
    }

    private void assignClassPassiveSkills() {
        passiveSkills.clear();

        ClassType ct = getClassType();
        if (ct == null) return;

        // Example: you should map real skills per class
        switch (ct) {
            case WARRIOR:
                passiveSkills.add(SkillRepository.getPassiveById("momentum"));
                passiveSkills.add(SkillRepository.getPassiveById("weapon_mastery"));
                break;
            case TANK:
                passiveSkills.add(SkillRepository.getPassiveById("stoneheart"));
                passiveSkills.add(SkillRepository.getPassiveById("second_wind"));
                break;
            case ROGUE:
                passiveSkills.add(SkillRepository.getPassiveById("shadow_instinct"));
                passiveSkills.add(SkillRepository.getPassiveById("killers_momentum"));
                break;
            default:
                break;
        }
    }
    
    /**
     * Initialize base stats based on class
     */
    private void initializeClassStats() {
        ClassType ct = getClassType();
        if (ct == null) return;
        
        // Reset stats to 0 first
        this.strength = 0;
        this.endurance = 0;
        this.agility = 0;
        this.flexibility = 0;
        
        switch (ct) {
            case WARRIOR:
                // Warriors are balanced with slight strength focus
                this.strength = 12;
                this.endurance = 10;
                this.agility = 8;
                this.flexibility = 6;
                break;
            case ROGUE:
                // Rogues are agile and flexible
                this.strength = 8;
                this.endurance = 8;
                this.agility = 12;
                this.flexibility = 10;
                break;
            case TANK:
                // Tanks are endurance focused
                this.strength = 10;
                this.endurance = 14;
                this.agility = 6;
                this.flexibility = 8;
                break;
            default:
                // Default balanced stats
                this.strength = 10;
                this.endurance = 10;
                this.agility = 10;
                this.flexibility = 10;
                break;
        }
    }
    
    /**
     * Initialize starting active skills based on class
     */
    private void initializeClassSkills() {
        ClassType ct = getClassType();
        if (ct == null) return;
        
        // Clear any existing active skills
        activeSkills.clear();
        
        switch (ct) {
            case WARRIOR:
                // Warriors start with basic attack and defensive skills
                SkillModel sword_slash = SkillRepository.getSkillById("sword_slash");
                if (sword_slash != null) activeSkills.add(sword_slash);
                break;
            case ROGUE:
                // Rogues start with quick attack and dodge skills
                SkillModel backstab = SkillRepository.getSkillById("backstab");
                if (backstab != null) activeSkills.add(backstab);
                break;
            case TANK:
                // Tanks start with shield skills and taunt
                SkillModel hammerShatter = SkillRepository.getSkillById("hammer_shatter");
                if (hammerShatter != null) activeSkills.add(hammerShatter);
                break;
            default:
                // Default basic attack for any class
                SkillModel defaultAttack = SkillRepository.getSkillById("basic_attack");
                if (defaultAttack != null) activeSkills.add(defaultAttack);
                break;
        }
    }

    // --- Skill Management Methods ---
    public List<PassiveSkill> getPassiveSkills() {
        return Collections.unmodifiableList(passiveSkills);
    }

    public List<SkillModel> getActiveSkills() {
        return Collections.unmodifiableList(activeSkills);
    }

    /** Adds a new active skill (non-ultimate). */
    public boolean addActiveSkill(SkillModel skill) {
        if (activeSkills.stream().anyMatch(s -> s.getId().equals(skill.getId()))) return false;
        if (activeSkills.size() >= 5) return false; // full
        if (skill.isUltimate() && hasUltimateSkill()) return false; // only 1 ultimate
        activeSkills.add(skill);
        return true;
    }

    /** Replace an existing active skill at slot. */
    public boolean replaceActiveSkill(int slot, SkillModel skill) {
        if (slot < 0 || slot >= activeSkills.size()) return false;
        if (skill.isUltimate() && hasUltimateSkill() && !activeSkills.get(slot).isUltimate()) {
            return false; // avoid duplicate ultimate
        }
        activeSkills.set(slot, skill);
        return true;
    }

    /** Checks if ultimate already exists. */
    public boolean hasUltimateSkill() {
        return activeSkills.stream().anyMatch(SkillModel::isUltimate);
    }

    public void removeActiveSkill(int slot) {
        if (slot >= 0 && slot < activeSkills.size()) {
            activeSkills.remove(slot);
        }
    }

    public ClassType getClassType() {
        try {
            return ClassType.valueOf(playerClass);
        } catch (Exception e) {
            return null;
        }
    }

    // --- Getters & Setters ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPlayerClass() { return playerClass; }
    public void setPlayerClass(String playerClass) { this.playerClass = playerClass; }

    public String getBodyStyle() { return bodyStyle; }
    public void setBodyStyle(String bodyStyle) { this.bodyStyle = bodyStyle; }

    public String getOutfit() { return outfit; }
    public void setOutfit(String outfit) { this.outfit = outfit; }

    public String getWeapon() { return weapon; }
    public void setWeapon(String weapon) { this.weapon = weapon; }

    public String getHairOutline() { return hairOutline; }
    public void setHairOutline(String hairOutline) { this.hairOutline = hairOutline; }

    public String getHairFill() { return hairFill; }
    public void setHairFill(String hairFill) { this.hairFill = hairFill; }

    public String getHairColor() { return hairColor; }
    public void setHairColor(String hairColor) { this.hairColor = hairColor; }

    public String getEyesOutline() { return eyesOutline; }
    public void setEyesOutline(String eyesOutline) { this.eyesOutline = eyesOutline; }

    public String getEyesFill() { return eyesFill; }
    public void setEyesFill(String eyesFill) { this.eyesFill = eyesFill; }

    public String getEyesColor() { return eyesColor; }
    public void setEyesColor(String eyesColor) { this.eyesColor = eyesColor; }

    public String getNose() { return nose; }
    public void setNose(String nose) { this.nose = nose; }

    public String getLips() { return lips; }
    public void setLips(String lips) { this.lips = lips; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; notifyChange(); }
    public void addCoins(int amount) { this.coins += amount; notifyChange(); }

    public int getXp() { return xp; }
    /** Adds XP and returns true if level increased */
    public boolean addXp(int amount) {
        if (level >= LevelProgression.getMaxLevel()) {
            xp = LevelProgression.getMaxXpForLevel(level);
            return false; // cannot level up further
        }

        xp += amount;
        int oldLevel = level;
        checkLevelUp();
        notifyChange();
        return level > oldLevel;
    }


    public int getLevel() { return level; }
    public int getRank() { return rank; }
    public int getRankPoints() { return rankPoints; }
    
    public void addRankPoints(int points) {
        this.rankPoints += points;
        updateRank();
        notifyChange();
    }
    
    public void setRankPoints(int points) {
        this.rankPoints = Math.max(0, points);
        updateRank();
        notifyChange();
    }
    
    public String getRankName() {
        switch (rank) {
            case 0: return "Novice";
            case 1: return "Veteran";
            case 2: return "Elite";
            case 3: return "Hero";
            case 4: return "Legendary";
            default: return "Unknown";
        }
    }
    
    public int getRankDrawableRes() {
        switch (rank) {
            case 0: return R.drawable.rank_novice;
            case 1: return R.drawable.rank_veteran;
            case 2: return R.drawable.rank_elite;
            case 3: return R.drawable.rank_hero;
            case 4: return R.drawable.rank_legendary;
            default: return R.drawable.rank_novice;
        }
    }

    public String getPlayerId() { return playerId; }

    // --- Free Points ---
    public int getFreePhysiquePoints() { return freePhysiquePoints; }
    public int getFreeAttributePoints() { return freeAttributePoints; }
    public void addFreePhysiquePoints(int pts) { freePhysiquePoints += pts; }
    public void addFreeAttributePoints(int pts) { freeAttributePoints += pts; }

    // --- Physique ---
    public int getArmPoints() { return armPoints; }
    public int getLegPoints() { return legPoints; }
    public int getChestPoints() { return chestPoints; }
    public int getBackPoints() { return backPoints; }

    public void addArmPoints(int pts) { armPoints += pts; }
    public void addLegPoints(int pts) { legPoints += pts; }
    public void addChestPoints(int pts) { chestPoints += pts; }
    public void addBackPoints(int pts) { backPoints += pts; }

    // --- Attributes ---
    public int getStrength() { return strength; }
    public int getEndurance() { return endurance; }
    public int getAgility() { return agility; }
    public int getFlexibility() { return flexibility; }
    public int getStamina() { return stamina; }

    public void addStrength(int pts) { strength += pts; }
    public void addEndurance(int pts) { endurance += pts; }
    public void addAgility(int pts) { agility += pts; }
    public void addFlexibility(int pts) { flexibility += pts; }
    public void addStamina(int pts) { stamina += pts; }

    // --- Level & Rank logic ---
    private void checkLevelUp() {
        int oldLevel = level;
        while (level < LevelProgression.getMaxLevel() && xp >= LevelProgression.getMaxXpForLevel(level)) {
            xp -= LevelProgression.getMaxXpForLevel(level);
            level++;
            
            // Handle skill acquisition for each level gained
            acquireSkillsForLevel(level);
        }
        if (level >= LevelProgression.getMaxLevel()) {
            level = LevelProgression.getMaxLevel();
            xp = LevelProgression.getMaxXpForLevel(level);
        }
        onLevelUp();
        updateRank();
    }
    
    /**
     * Acquire new skills based on the avatar's level and class
     */
    private void acquireSkillsForLevel(int newLevel) {
        ClassType ct = getClassType();
        if (ct == null) return;
        
        // Get all available skills for this class
        List<SkillModel> availableSkills = SkillRepository.getSkillsForClass(ct);
        
        for (SkillModel skill : availableSkills) {
            // Check if this skill should be unlocked at this level
            if (skill.getLevelUnlock() == newLevel) {
                // Check if we already have this skill
                boolean alreadyHasSkill = false;
                for (SkillModel existingSkill : activeSkills) {
                    if (existingSkill.getId().equals(skill.getId())) {
                        alreadyHasSkill = true;
                        break;
                    }
                }
                
                // If we don't have this skill yet, add it
                if (!alreadyHasSkill) {
                    // Try to auto-equip if there's space
                    if (activeSkills.size() < 5) {
                        activeSkills.add(skill);
                    } else {
                        // Add to available skills (could be stored separately for future use)
                        // For now, we'll just add it to active skills anyway
                        activeSkills.add(skill);
                    }
                }
            }
        }
    }
    
    /**
     * Get newly acquired skills for a specific level (for display purposes)
     */
    public List<SkillModel> getNewlyAcquiredSkills(int targetLevel) {
        List<SkillModel> newSkills = new ArrayList<>();
        ClassType ct = getClassType();
        if (ct == null) return newSkills;
        
        List<SkillModel> availableSkills = SkillRepository.getSkillsForClass(ct);
        
        for (SkillModel skill : availableSkills) {
            if (skill.getLevelUnlock() == targetLevel) {
                // Check if we already have this skill
                boolean alreadyHasSkill = false;
                for (SkillModel existingSkill : activeSkills) {
                    if (existingSkill.getId().equals(skill.getId())) {
                        alreadyHasSkill = true;
                        break;
                    }
                }
                
                if (!alreadyHasSkill) {
                    newSkills.add(skill);
                }
            }
        }
        
        return newSkills;
    }

    private void updateRank() {
        // Rank system based on rank points earned from arena battles
        if (rankPoints >= 500) rank = 4; // Legendary
        else if (rankPoints >= 351) rank = 3; // Hero
        else if (rankPoints >= 201) rank = 2; // Elite
        else if (rankPoints >= 101) rank = 1; // Veteran
        else rank = 0; // Novice
        notifyChange();
    }

    public int getXpNeeded() {
        if (level >= LevelProgression.getMaxLevel()) return 0;
        return LevelProgression.getMaxXpForLevel(level) - xp;
    }

    // --- Utility ---
    private String generatePlayerId() {
        return String.valueOf(10000000 + (int)(Math.random() * 89999999));
    }

    // --- Setters for XP, Level, Rank ---
    public void setXp(int xp) {
        if (xp < 0) xp = 0;
        this.xp = xp;
        checkLevelUp(); // Ensure level and rank stay consistent
        notifyChange();
    }

    public void setLevel(int level) {
        this.level = Math.min(level, LevelProgression.getMaxLevel());
        checkLevelUp(); // Adjust XP and rank if needed
        notifyChange();
    }

    public void setRank(int rank) {
        this.rank = Math.max(0, Math.min(rank, 4)); // 0=Novice, 4=Legendary
        notifyChange();
    }

    // Listener setter
    public void setProfileChangeListener(ProfileChangeListener listener) {
        this.listener = listener;
    }

    private void notifyChange() {
        if (listener != null) {
            listener.onProfileChanged(this);
        }
    }

    // --- Methods ---
    public void addGear(String gearId) {
        ownedGearIds.add(gearId);
    }

    public boolean ownsGear(String gearId) {
        return ownedGearIds.contains(gearId);
    }

    public void equipGear(GearType slot, String gearId) {
        if (gearId != null && ownsGear(gearId)) {
            equippedGear.put(slot, gearId);
            notifyChange();
        }
    }

    public void unequipGear(GearType slot) {
        equippedGear.remove(slot);
    }

    public Map<GearType, String> getEquippedGear() {
        return equippedGear;
    }

    public Set<String> getOwnedGear() {
        return ownedGearIds;
    }


    public String getFormattedCoins() {
        if (coins >= 1_000_000_000) {
            return (coins / 1_000_000_000) + "B";
        } else if (coins >= 1_000_000) {
            return (coins / 1_000_000) + "M";
        } else if (coins >= 1_000) {
            return (coins / 1_000) + "K";
        } else {
            return String.valueOf(coins);
        }
    }

    public void checkOutfitCompletion() {
        // Get equipped gear by type
        String armorId = equippedGear.get(GearType.ARMOR);
        String pantsId = equippedGear.get(GearType.PANTS);
        String bootsId = equippedGear.get(GearType.BOOTS);

        if (armorId == null || pantsId == null || bootsId == null) return;

        GearModel armor = GearRepository.getGearById(armorId);
        GearModel pants = GearRepository.getGearById(pantsId);
        GearModel boots = GearRepository.getGearById(bootsId);

        // All 3 must exist and share the same setId
        if (armor != null && pants != null && boots != null) {
            if (armor.getSetId() != null
                    && armor.getSetId().equals(pants.getSetId())
                    && armor.getSetId().equals(boots.getSetId())) {

                // If the set defines an outfit
                if (armor.isCompletesOutfit() && pants.isCompletesOutfit() && boots.isCompletesOutfit()) {
                    // Change avatar outfit sprite
                    this.outfit = "res://" + armor.getOutfitSpriteRes();
                    notifyChange();
                }
            }
        }

    }

    public boolean isEquipped(GearModel gear) {
        if (gear == null || equippedGear == null) return false;
        // get the currently equipped gear ID for this type
        String equippedId = equippedGear.get(gear.getType());
        return equippedId != null && equippedId.equals(gear.getId());
    }

    public Map<String, GoalState> getGoalProgress() {
        return goalProgress;
    }

    public GoalState getGoalState(String goalId) {
        return goalProgress.getOrDefault(goalId, GoalState.PENDING);
    }

    public void setGoalState(String goalId, GoalState state) {
        goalProgress.put(goalId, state);
    }

    public boolean isGoalCompleted(String goalId) {
        return goalProgress.getOrDefault(goalId, GoalState.PENDING) == GoalState.COMPLETED;
    }

    public boolean isGoalClaimed(String goalId) {
        return goalProgress.getOrDefault(goalId, GoalState.PENDING) == GoalState.CLAIMED;
    }

    public void resetGoals() {
        goalProgress.clear();
    }


    public void copyFrom(AvatarModel other) {
        this.armPoints = other.armPoints;
        this.legPoints = other.legPoints;
        this.chestPoints = other.chestPoints;
        this.backPoints = other.backPoints;
        this.strength = other.strength;
        this.endurance = other.endurance;
        this.agility = other.agility;
        this.flexibility = other.flexibility;
        this.stamina = other.stamina;
        this.freePhysiquePoints = other.freePhysiquePoints;
        this.freeAttributePoints = other.freeAttributePoints;
    }

    public List<BattleHistoryModel> getBattleHistory() {
        return battleHistory;
    }
    
    public void addBattleHistory(BattleHistoryModel battleEntry) {
        battleHistory.add(0, battleEntry); // Add to beginning of list
        
        // Keep only the last 50 battle entries
        if (battleHistory.size() > 50) {
            battleHistory = new ArrayList<>(battleHistory.subList(0, 50));
        }
        
        notifyChange();
    }

    private void onLevelUp() {
        addFreeAttributePoints(5);
        acquireSkillsForLevel(level);
        notifyChange();
    }
}

