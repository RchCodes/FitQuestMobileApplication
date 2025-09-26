package com.example.fitquest;

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

    // NEW fields
    private int coins;           // total coins
    private int xp;              // experience points
    private int level;           // player level

    private int armPoints, legPoints, chestPoints, backPoints;

    // ATTRIBUTES
    private int strength, endurance, agility, flexibility, stamina;

    // New field for rank
    private int rank; // 0=Novice, 1=Warrior, 2=Elite, 3=Hero

    private String playerId; // unique player ID

    // Required empty constructor for Firebase
    public AvatarModel() {}

    public AvatarModel(String username, String gender, String playerClass,
                       String bodyStyle, String outfit, String weapon,
                       String hairOutline, String hairFill, String hairColor,
                       String eyesOutline, String eyesFill, String eyesColor,
                       String nose, String lips) {
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

        // Initialize coins, XP, level
        this.coins = 0;
        this.xp = 0;
        this.level = 1; // start at level 1
        this.rank = 0; // default to Novice

        // Generate player ID
        this.playerId = generatePlayerId();

    }

    // --- Getters and Setters ---
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

    // --- NEW setters/getters ---
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    // Getter & Setter for rank
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    // --- Getter & Setter ---
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    // --- Utility ---
    private String generatePlayerId() {
        return String.valueOf(10000000 + (int)(Math.random() * 89999999)); // 8-digit random ID
    }

    public void addCoins(int amount) {
        this.coins += amount;
    }

    public boolean addXp(int amount) {
        this.xp += amount;
        int oldLevel = this.level;
        checkLevelUp();
        return this.level > oldLevel; // true if level increased
    }


    private void checkLevelUp() {
        int xpNeeded = this.level * 100;
        while (this.xp >= xpNeeded) {
            this.xp -= xpNeeded;
            this.level++;
            xpNeeded = this.level * 100;
        }

        // --- Rank progression ---
        if (this.level >= 20) this.rank = 3; // Hero
        else if (this.level >= 15) this.rank = 2; // Elite
        else if (this.level >= 10) this.rank = 1; // Warrior
        else this.rank = 0; // Novice
    }

    // Physique
    public void addArmPoints(int pts) { this.armPoints += pts; }
    public void addLegPoints(int pts) { this.legPoints += pts; }
    public void addChestPoints(int pts) { this.chestPoints += pts; }
    public void addBackPoints(int pts) { this.backPoints += pts; }

    // Attributes
    public void addStrength(int pts) { this.strength += pts; }
    public void addEndurance(int pts) { this.endurance += pts; }
    public void addAgility(int pts) { this.agility += pts; }
    public void addFlexibility(int pts) { this.flexibility += pts; }
    public void addStamina(int pts) { this.stamina += pts; }

    // Optional: getters for UI
    public int getArmPoints() { return armPoints; }
    public int getLegPoints() { return legPoints; }
    public int getChestPoints() { return chestPoints; }
    public int getBackPoints() { return backPoints; }
    public int getStrength() { return strength; }
    public int getEndurance() { return endurance; }
    public int getAgility() { return agility; }
    public int getFlexibility() { return flexibility; }
    public int getStamina() { return stamina; }


}
