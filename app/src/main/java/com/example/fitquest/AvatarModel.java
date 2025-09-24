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
}
