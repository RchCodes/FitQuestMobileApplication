package com.example.fitquest;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

    private int questPoints;
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
    private transient ProfileChangeListener listener;

    private transient boolean checkingLevelUp = false;

    // --- Inventory ---
    private Set<String> ownedGearIds = new HashSet<>(); // all owned items (by ID)

    // --- Restricted Gear Slots ---
    private final Map<GearType, String> equippedGear = new EnumMap<>(GearType.class);

    private Map<String, GoalState> goalProgress = new HashMap<>();

    // --- NEW: Skill System ---
    private transient List<PassiveSkill> passiveSkills = new ArrayList<>(2); // fixed 2
    private transient List<SkillModel> activeSkills = new ArrayList<>(5);   // up to 5

    private List<BattleHistoryModel> battleHistory = new ArrayList<>();

    // --- Fitness Challenges ---
    private List<ChallengeModel> fitnessChallenges = new ArrayList<>();
    private Set<String> completedChallengeIds = new HashSet<>();

    private List<String> activeSkillIds;
    private List<String> passiveSkillIds;

    List<Integer> avatarBadges = new ArrayList<>();

    // Constructors
    public AvatarModel() {
        initBase();
    }

    public AvatarModel(String username, String gender, String playerClass,
                       String bodyStyle, String outfit, String weapon,
                       String hairOutline, String hairFill, String hairColor,
                       String eyesOutline, String eyesFill, String eyesColor,
                       String nose, String lips) {

        initBase();

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
        assignClassPassiveSkills(); // auto-assign passives at level 1
    }

    // --- Gear Slot Setup ---
    private void initGearSlots() {
        for (GearType type : Arrays.asList(GearType.WEAPON, GearType.ARMOR, GearType.PANTS, GearType.BOOTS, GearType.ACCESSORY)) {
            equippedGear.put(type, null); // start empty
        }
    }

    /**
     * Copy constructor: create a temporary/working copy of the avatar.
     * This intentionally copies appearance, class, stats and IDs so the temp object
     * can safely be used by UI code without accidentally losing class metadata.
     */
    public AvatarModel(AvatarModel other) {
        if (other == null) {
            initBase();
            return;
        }

        // Basic identity & appearance
        this.username = other.username;
        this.gender = other.gender;
        this.playerClass = other.playerClass;
        this.bodyStyle = other.bodyStyle;
        this.outfit = other.outfit;
        this.weapon = other.weapon;
        this.hairOutline = other.hairOutline;
        this.hairFill = other.hairFill;
        this.hairColor = other.hairColor;
        this.eyesOutline = other.eyesOutline;
        this.eyesFill = other.eyesFill;
        this.eyesColor = other.eyesColor;
        this.nose = other.nose;
        this.lips = other.lips;

        // Core progression
        this.coins = other.coins;
        this.xp = other.xp;
        this.level = other.level;
        this.rank = other.rank;
        this.rankPoints = other.rankPoints;
        this.playerId = other.playerId;

        // Free points & stats
        this.freePhysiquePoints = other.freePhysiquePoints;
        this.freeAttributePoints = other.freeAttributePoints;

        this.armPoints = other.armPoints;
        this.legPoints = other.legPoints;
        this.chestPoints = other.chestPoints;
        this.backPoints = other.backPoints;

        this.strength = other.strength;
        this.endurance = other.endurance;
        this.agility = other.agility;
        this.flexibility = other.flexibility;
        this.stamina = other.stamina;

        // Inventory & gear (shallow copy safe for temp usage)
        this.ownedGearIds = other.ownedGearIds != null ? new HashSet<>(other.ownedGearIds) : new HashSet<>();
        this.equippedGear.putAll(other.equippedGear);

        // Goals
        this.goalProgress = other.goalProgress != null ? new HashMap<>(other.goalProgress) : new HashMap<>();

        // Battle history (keep history)
        this.battleHistory = other.battleHistory != null ? new ArrayList<>(other.battleHistory) : new ArrayList<>();

        // Skill IDs and runtime lists
        this.activeSkillIds = other.activeSkillIds != null ? new ArrayList<>(other.activeSkillIds) : new ArrayList<>();
        this.passiveSkillIds = other.passiveSkillIds != null ? new ArrayList<>(other.passiveSkillIds) : new ArrayList<>();
        loadSkillsFromIds();
    }

    public void initializeClassData() {
        initializeClassStats();
        initializeClassSkills();
        assignClassPassiveSkills();
    }

    // Base initialization (no class-specific skills yet)
    private void initBase() {
        this.level = 1;
        this.xp = 0;
        this.coins = 0;
        this.rank = 0;
        this.rankPoints = 0;
        this.playerId = generatePlayerId();
        this.freePhysiquePoints = 1;
        this.freeAttributePoints = 1;

        this.armPoints = 0;
        this.legPoints = 0;
        this.chestPoints = 0;
        this.backPoints = 0;

        this.strength = 0;
        this.endurance = 0;
        this.agility = 0;
        this.flexibility = 0;
        this.stamina = 0;

        initGearSlots();
        initFitnessChallenges();
    }

    private void assignClassPassiveSkills() {
        if (passiveSkills == null) passiveSkills = new ArrayList<>(2);
        passiveSkills.clear();

        ClassType ct = getClassType();
        if (ct == null) return;

        // Example: replace with your real mapping
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

        if (activeSkills == null) activeSkills = new ArrayList<>(5);
        activeSkills.clear();

        switch (ct) {
            case WARRIOR:
                SkillModel sword_slash = SkillRepository.getSkillById("sword_slash");
                if (sword_slash != null) activeSkills.add(sword_slash);
                break;
            case ROGUE:
                SkillModel backstab = SkillRepository.getSkillById("backstab");
                if (backstab != null) activeSkills.add(backstab);
                break;
            case TANK:
                SkillModel hammerShatter = SkillRepository.getSkillById("hammer_shatter");
                if (hammerShatter != null) activeSkills.add(hammerShatter);
                break;
            default:
                SkillModel defaultAttack = SkillRepository.getSkillById("basic_attack");
                if (defaultAttack != null) activeSkills.add(defaultAttack);
                break;
        }

        updateSkillIds();
    }

    // --- Skill Management Methods ---
    public List<PassiveSkill> getPassiveSkills() {
        if (passiveSkills == null) passiveSkills = new ArrayList<>(2);
        return Collections.unmodifiableList(passiveSkills);
    }


    public List<SkillModel> getActiveSkills() {
        if (activeSkills == null) activeSkills = new ArrayList<>(5);
        return Collections.unmodifiableList(activeSkills);
    }

    /**
     * Adds a new active skill (non-ultimate).
     */
    public boolean addActiveSkill(SkillModel skill) {
        if (skill == null) return false;
        if (activeSkills == null) activeSkills = new ArrayList<>(5);
        if (activeSkills.stream().anyMatch(s -> s.getId().equals(skill.getId()))) return false;
        if (activeSkills.size() >= 5) return false; // full
        if (skill.isUltimate() && hasUltimateSkill()) return false; // only 1 ultimate
        activeSkills.add(skill);
        updateSkillIds();
        notifyChange();
        return true;
    }

    /**
     * Replace an existing active skill at slot.
     */
    public boolean replaceActiveSkill(int slot, SkillModel skill) {
        if (skill == null) return false;
        if (activeSkills == null) activeSkills = new ArrayList<>(5);
        if (slot < 0 || slot >= activeSkills.size()) return false;
        if (skill.isUltimate() && hasUltimateSkill() && !activeSkills.get(slot).isUltimate()) {
            return false; // avoid duplicate ultimate
        }
        activeSkills.set(slot, skill);
        updateSkillIds();
        notifyChange();
        return true;
    }

    /**
     * Checks if ultimate already exists.
     */
    public boolean hasUltimateSkill() {
        if (activeSkills == null) activeSkills = new ArrayList<>(5);
        return activeSkills.stream().anyMatch(SkillModel::isUltimate);
    }

    public void removeActiveSkill(int slot) {
        if (activeSkills == null) activeSkills = new ArrayList<>(5);
        if (slot >= 0 && slot < activeSkills.size()) {
            activeSkills.remove(slot);
            updateSkillIds();
            notifyChange();
        }
    }

    public ClassType getClassType() {
        if (playerClass == null) return null;
        try {
            // accept both "WARRIOR" and "warrior"
            return ClassType.valueOf(playerClass.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // fallback mapping in case playerClass is something custom
            switch (playerClass.toLowerCase(Locale.ROOT)) {
                case "warrior":
                    return ClassType.WARRIOR;
                case "rogue":
                    return ClassType.ROGUE;
                case "tank":
                    return ClassType.TANK;
                default:
                    return null;
            }
        }
    }

    // --- Getters & Setters ---
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(String playerClass) {
        this.playerClass = playerClass;
    }

    public String getBodyStyle() {
        return bodyStyle;
    }

    public void setBodyStyle(String bodyStyle) {
        this.bodyStyle = bodyStyle;
    }

    public String getOutfit() {
        return outfit;
    }

    public void setOutfit(String outfit) {
        this.outfit = outfit;
    }

    public String getWeapon() {
        return weapon;
    }

    public void setWeapon(String weapon) {
        this.weapon = weapon;
    }

    public String getHairOutline() {
        return hairOutline;
    }

    public void setHairOutline(String hairOutline) {
        this.hairOutline = hairOutline;
    }

    public String getHairFill() {
        return hairFill;
    }

    public void setHairFill(String hairFill) {
        this.hairFill = hairFill;
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    public String getEyesOutline() {
        return eyesOutline;
    }

    public void setEyesOutline(String eyesOutline) {
        this.eyesOutline = eyesOutline;
    }

    public String getEyesFill() {
        return eyesFill;
    }

    public void setEyesFill(String eyesFill) {
        this.eyesFill = eyesFill;
    }

    public String getEyesColor() {
        return eyesColor;
    }

    public void setEyesColor(String eyesColor) {
        this.eyesColor = eyesColor;
    }

    public String getNose() {
        return nose;
    }

    public void setNose(String nose) {
        this.nose = nose;
    }

    public String getLips() {
        return lips;
    }

    public void setLips(String lips) {
        this.lips = lips;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
        notifyChange();
    }

    public void addCoins(int amount) {
        this.coins += amount;
        notifyChange();
    }

    public int getXp() {
        return xp;
    }

    /**
     * Adds XP and returns true if level increased
     */
    // Cleaned up addXp()
    public boolean addXp(int amount) {
        if (amount <= 0) return false;
        xp += amount;
        return checkLevelUp();
    }


    public int getLevel() {
        return level;
    }

    public int getRank() {
        return rank;
    }

    public int getRankPoints() {
        return rankPoints;
    }

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
            case 0:
                return "Novice";
            case 1:
                return "Veteran";
            case 2:
                return "Elite";
            case 3:
                return "Hero";
            case 4:
                return "Legendary";
            default:
                return "Unknown";
        }
    }

    public int getRankDrawableRes() {
        switch (rank) {
            case 0:
                return R.drawable.rank_novice;
            case 1:
                return R.drawable.rank_veteran;
            case 2:
                return R.drawable.rank_elite;
            case 3:
                return R.drawable.rank_hero;
            case 4:
                return R.drawable.rank_legendary;
            default:
                return R.drawable.rank_novice;
        }
    }

    public String getPlayerId() {
        return playerId;
    }

    // --- Free Points ---
    public int getFreePhysiquePoints() {
        return Math.max(0, freePhysiquePoints);
    }

    public int getFreeAttributePoints() {
        return Math.max(0, freeAttributePoints);
    }

    public void addFreePhysiquePoints(int pts) {
        freePhysiquePoints = Math.max(0, freePhysiquePoints + pts);
    }

    public void addFreeAttributePoints(int pts) {
        freeAttributePoints = Math.max(0, freeAttributePoints + pts);
    }

    // --- Physique ---
    public int getArmPoints() {
        return armPoints;
    }

    public int getLegPoints() {
        return legPoints;
    }

    public int getChestPoints() {
        return chestPoints;
    }

    public int getBackPoints() {
        return backPoints;
    }

    public void addArmPoints(int pts) {
        armPoints = Math.max(0, armPoints + pts);
    }

    public void addLegPoints(int pts) {
        legPoints = Math.max(0, legPoints + pts);
    }

    public void addChestPoints(int pts) {
        chestPoints = Math.max(0, chestPoints + pts);
    }

    public void addBackPoints(int pts) {
        backPoints = Math.max(0, backPoints + pts);
    }

    // --- Attributes ---
    public int getStrength() {
        return strength;
    }

    public int getEndurance() {
        return endurance;
    }

    public int getAgility() {
        return agility;
    }

    public int getFlexibility() {
        return flexibility;
    }

    public int getStamina() {
        return stamina;
    }

    public void addStrength(int pts) {
        strength = Math.max(0, strength + pts);
    }

    public void addEndurance(int pts) {
        endurance = Math.max(0, endurance + pts);
    }

    public void addAgility(int pts) {
        agility = Math.max(0, agility + pts);
    }

    public void addFlexibility(int pts) {
        flexibility = Math.max(0, flexibility + pts);
    }

    public void addStamina(int pts) {
        stamina = Math.max(0, stamina + pts);
    }

    // --- Level & Rank logic ---
    private boolean checkLevelUp() {
        if (checkingLevelUp) return false;
        checkingLevelUp = true;

        boolean leveledUp = false;

        try {
            int xpRequired;
            while (level < LevelProgression.getMaxLevel()) {
                xpRequired = LevelProgression.getMaxXpForLevel(level);
                if (xp < xpRequired) break;

                xp -= xpRequired;
                level++;
                leveledUp = true;
                onLevelUp();
            }

            // Clamp XP to valid range
            int xpCap = LevelProgression.getMaxXpForLevel(level);
            if (xp > xpCap) xp = xpCap - 1;

        } finally {
            checkingLevelUp = false;
        }

        if (leveledUp) notifyChange();
        return leveledUp;
    }

    /**
     * Acquire new skills based on the avatar's level and class
     */
    private void acquireSkillsForLevel(int newLevel) {
        ClassType ct = getClassType();
        if (ct == null) return;

        // Get all available skills for this class
        List<SkillModel> availableSkills = SkillRepository.getSkillsForClass(ct);
        if (availableSkills == null) return;

        for (SkillModel skill : availableSkills) {
            // Check if this skill should be unlocked at this level
            if (skill.getLevelUnlock() == newLevel) {
                boolean alreadyHasSkill = false;
                if (activeSkills != null) {
                    for (SkillModel existingSkill : activeSkills) {
                        if (existingSkill.getId().equals(skill.getId())) {
                            alreadyHasSkill = true;
                            break;
                        }
                    }
                }

                if (!alreadyHasSkill) {
                    if (activeSkills == null) activeSkills = new ArrayList<>(5);
                    // Try to auto-equip if there's space
                    if (activeSkills.size() < 5) {
                        activeSkills.add(skill);
                    } else {
                        // If full, still add to list for now (preserve behavior)
                        activeSkills.add(skill);
                    }
                }
            }
        }

        updateSkillIds();
    }

    /**
     * Get newly acquired skills for a specific level (for display purposes)
     */
    public List<SkillModel> getNewlyAcquiredSkills(int targetLevel) {
        List<SkillModel> newSkills = new ArrayList<>();
        ClassType ct = getClassType();
        if (ct == null) return newSkills;

        List<SkillModel> availableSkills = SkillRepository.getSkillsForClass(ct);
        if (availableSkills == null) return newSkills;

        for (SkillModel skill : availableSkills) {
            if (skill.getLevelUnlock() == targetLevel) {
                boolean alreadyHasSkill = false;
                if (activeSkills != null) {
                    for (SkillModel existingSkill : activeSkills) {
                        if (existingSkill.getId().equals(skill.getId())) {
                            alreadyHasSkill = true;
                            break;
                        }
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
        // NOTE: do not call notifyChange() here to avoid duplicated notifications
    }

    public int getXpNeeded() {
        return Math.max(0, LevelProgression.getMaxXpForLevel(level) - xp);
    }

    // --- Utility ---
    private String generatePlayerId() {
        return String.valueOf(10000000 + (int) (Math.random() * 89999999));
    }

    // --- Setters for XP, Level, Rank ---
    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
        checkLevelUp();
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(level, LevelProgression.getMaxLevel()));
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
        if (ownedGearIds == null) ownedGearIds = new HashSet<>();
        ownedGearIds.add(gearId);
        notifyChange();
    }

    public boolean ownsGear(String gearId) {
        if (ownedGearIds == null) ownedGearIds = new HashSet<>();
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
        notifyChange();
    }

    public Map<GearType, String> getEquippedGear() {
        return equippedGear;
    }

    public Set<String> getOwnedGear() {
        if (ownedGearIds == null) ownedGearIds = new HashSet<>();
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
                    // Change avatar outfit sprite (keep the same format you use elsewhere)
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
        if (goalProgress == null) goalProgress = new HashMap<>();
        return goalProgress;
    }

    public GoalState getGoalState(String goalId) {
        if (goalProgress == null) goalProgress = new HashMap<>();
        return goalProgress.getOrDefault(goalId, GoalState.PENDING);
    }

    public void setGoalState(String goalId, GoalState state) {
        if (goalProgress == null) goalProgress = new HashMap<>();
        goalProgress.put(goalId, state);
        notifyChange();
    }

    public boolean isGoalCompleted(String goalId) {
        if (goalProgress == null) goalProgress = new HashMap<>();
        return goalProgress.getOrDefault(goalId, GoalState.PENDING) == GoalState.COMPLETED;
    }

    public boolean isGoalClaimed(String goalId) {
        if (goalProgress == null) goalProgress = new HashMap<>();
        return goalProgress.getOrDefault(goalId, GoalState.PENDING) == GoalState.CLAIMED;
    }

    public void resetGoals() {
        if (goalProgress == null) goalProgress = new HashMap<>();
        goalProgress.clear();
        notifyChange();
    }


    public void copyFrom(AvatarModel other) {
        // Intentionally only copy stats & free points (used by CharacterStats.apply)
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

        notifyChange();
    }

    public List<BattleHistoryModel> getBattleHistory() {
        if (battleHistory == null) battleHistory = new ArrayList<>();
        return battleHistory;
    }

    public void addBattleHistory(BattleHistoryModel battleEntry) {
        if (battleHistory == null) battleHistory = new ArrayList<>();
        battleHistory.add(0, battleEntry); // Add to beginning of list
        // Keep only the last 50 battle entries
        if (battleHistory.size() > 50) {
            battleHistory = new ArrayList<>(battleHistory.subList(0, 50));
        }
        notifyChange();
        saveBattleHistoryToFirebase();
    }

    private void saveBattleHistoryToFirebase() {
        if (playerId == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(playerId).child("battleHistory");
        ref.setValue(battleHistory);
    }

    public void loadBattleHistoryFromFirebase(final Runnable onLoaded) {
        if (playerId == null) {
            if (onLoaded != null) onLoaded.run();
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(playerId).child("battleHistory");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<BattleHistoryModel> loaded = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    BattleHistoryModel entry = child.getValue(BattleHistoryModel.class);
                    if (entry != null) loaded.add(entry);
                }
                battleHistory = loaded;
                if (onLoaded != null) onLoaded.run();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (onLoaded != null) onLoaded.run();
            }
        });
    }

    private void onLevelUp() {
        addFreeAttributePoints(5);
        addFreePhysiquePoints(2);
        acquireSkillsForLevel(level);

        Log.i("AvatarModel", "Level Up! New level: " + level);
        notifyChange();
    }

    public void setPassiveSkills(List<PassiveSkill> passiveSkills) {
        this.passiveSkills = passiveSkills != null ? new ArrayList<>(passiveSkills) : new ArrayList<>();
        updateSkillIds();
        notifyChange();
    }


    public void loadSkillsFromIds() {
        activeSkills = new ArrayList<>();
        if (activeSkillIds != null) {
            for (String id : activeSkillIds) {
                SkillModel skill = SkillRepository.getSkillById(id);
                if (skill != null) activeSkills.add(skill);
            }
        }

        passiveSkills = new ArrayList<>();
        if (passiveSkillIds != null) {
            for (String id : passiveSkillIds) {
                PassiveSkill skill = SkillRepository.getPassiveById(id);
                if (skill != null) passiveSkills.add(skill);
            }
        }
    }

    public void updateSkillIds() {
        if (activeSkills != null) {
            activeSkillIds = new ArrayList<>();
            for (SkillModel s : activeSkills) {
                if (s != null && s.getId() != null) activeSkillIds.add(s.getId());
            }
        }

        if (passiveSkills != null) {
            passiveSkillIds = new ArrayList<>();
            for (PassiveSkill p : passiveSkills) {
                if (p != null && p.getId() != null) passiveSkillIds.add(p.getId());
            }
        }
    }

    // --- Skill ID getters (used for saving) ---
    public List<String> getActiveSkillIds() {
        return activeSkillIds;
    }

    public List<String> getPassiveSkillIds() {
        return passiveSkillIds;
    }

    // --- Skill ID setters (used when loading from Firebase) ---
    public void setActiveSkillIds(List<String> ids) {
        this.activeSkillIds = ids != null ? new ArrayList<>(ids) : new ArrayList<>();
        // Rebuild runtime skill objects
        loadSkillsFromIds();
        notifyChange();
    }

    public void setPassiveSkillIds(List<String> ids) {
        this.passiveSkillIds = ids != null ? new ArrayList<>(ids) : new ArrayList<>();
        // Rebuild runtime passive objects
        loadSkillsFromIds();
        notifyChange();
    }

    public List<SkillModel> getAvailableSkills() {
        ClassType ct = getClassType();
        if (ct == null) return new ArrayList<>();
        return SkillRepository.getSkillsForClass(ct);
    }

    public void setActiveSkills(List<SkillModel> activeSlots) {
        if (activeSlots == null) {
            this.activeSkills = new ArrayList<>(5);
        } else {
            this.activeSkills = new ArrayList<>(activeSlots);
        }
        updateSkillIds();
        notifyChange();
    }

    public List<SkillModel> getUnlockedSkills() {
        List<SkillModel> unlocked = new ArrayList<>();
        ClassType ct = getClassType();
        if (ct == null) return unlocked;

        List<SkillModel> allClassSkills = SkillRepository.getSkillsForClass(ct);
        if (allClassSkills == null) return unlocked;

        int lvl = this.getLevel();
        for (SkillModel skill : allClassSkills) {
            if (skill.getLevelUnlock() <= lvl) {
                unlocked.add(skill);
            }
        }

        return unlocked;
    }

    public List<ChallengeModel> getFitnessChallenges() {
        if (fitnessChallenges == null) fitnessChallenges = new ArrayList<>();
        return fitnessChallenges;
    }

    public void setFitnessChallenges(List<ChallengeModel> challenges) {
        this.fitnessChallenges = challenges != null ? new ArrayList<>(challenges) : new ArrayList<>();
    }

    public Set<String> getCompletedChallengeIds() {
        if (completedChallengeIds == null) completedChallengeIds = new HashSet<>();
        return completedChallengeIds;
    }

    public void markChallengeCompleted(String challengeId) {
        if (completedChallengeIds == null) completedChallengeIds = new HashSet<>();
        if (!completedChallengeIds.contains(challengeId)) {
            completedChallengeIds.add(challengeId);
            // optional: also reward XP/coins here if you want instant rewards
        }
    }
    public boolean isChallengeCompleted(String challengeId) {
        return completedChallengeIds != null && completedChallengeIds.contains(challengeId);
    }

    public void addChallenge(ChallengeModel challenge) {
        if (challenge == null) return;
        fitnessChallenges.add(challenge);
        notifyChange();
    }

    public void completeChallenge(String challengeId) {
        completedChallengeIds.add(challengeId);
        notifyChange();
    }

    private void initFitnessChallenges() {
        if (fitnessChallenges == null || fitnessChallenges.isEmpty()) {
            this.fitnessChallenges = ChallengeManager.getDefaultChallenges(); // static source
        }
        if (completedChallengeIds == null) {
            this.completedChallengeIds = new HashSet<>();
        }
    }

    public boolean hasSprite() {
        return bodyStyle != null && !bodyStyle.isEmpty()
                && outfit != null && !outfit.isEmpty()
                && weapon != null && !weapon.isEmpty()
                && hairOutline != null && !hairOutline.isEmpty()
                && hairFill != null && !hairFill.isEmpty()
                && hairColor != null && !hairColor.isEmpty()
                && eyesOutline != null && !eyesOutline.isEmpty()
                && eyesFill != null && !eyesFill.isEmpty()
                && eyesColor != null && !eyesColor.isEmpty()
                && nose != null && !nose.isEmpty()
                && lips != null && !lips.isEmpty();

    }

    public void addAvatarBadge(int rewardBadge) {
        if (avatarBadges == null) avatarBadges = new ArrayList<>();
        if (!avatarBadges.contains(rewardBadge)) {
            {
                avatarBadges.add(rewardBadge);
            }
        }
    }

    public void addCompletedChallenge(String challengeId) {
        if (challengeId != null) completedChallengeIds.add(challengeId);
    }

    public void setCompletedChallengeIds(Set<String> ids) {
        completedChallengeIds.clear();
        if (ids != null) completedChallengeIds.addAll(ids);
    }

}


