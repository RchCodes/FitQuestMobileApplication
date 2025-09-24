package com.example.fitquest;

public class AvatarModel {

    private String username;     // Player's chosen username
    private String gender;       // "male" or "female"
    private String playerClass;  // "warrior", "rogue", "tank"

    // Base body (depends on gender + class)
    private String bodyStyle;    // e.g., "body_male_warrior"

    // Hair
    private String hairOutline;  // drawable name for outline (e.g., "hair_male_1_outline")
    private String hairFill;     // drawable name for fill (e.g., "hair_male_1_fill")
    private String hairColor;    // hex color (e.g., "#FF0000")

    // Eyes
    private String eyesOutline;  // drawable name (e.g., "eyes_1_outline")
    private String eyesFill;     // drawable name (e.g., "eyes_1_fill")
    private String eyesColor;    // hex color (e.g., "#0000FF")

    // Nose & Lips (no color)
    private String nose;         // drawable name (e.g., "nose_2")
    private String lips;         // drawable name (e.g., "lips_3")

    // Required empty constructor for Firebase
    public AvatarModel() {
    }

    public AvatarModel(String username, String gender, String playerClass,
                       String bodyStyle,
                       String hairOutline, String hairFill, String hairColor,
                       String eyesOutline, String eyesFill, String eyesColor,
                       String nose, String lips) {
        this.username = username;
        this.gender = gender;
        this.playerClass = playerClass;
        this.bodyStyle = bodyStyle;
        this.hairOutline = hairOutline;
        this.hairFill = hairFill;
        this.hairColor = hairColor;
        this.eyesOutline = eyesOutline;
        this.eyesFill = eyesFill;
        this.eyesColor = eyesColor;
        this.nose = nose;
        this.lips = lips;
    }

    // Getters and Setters (needed for Firebase & Gson)

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
}
