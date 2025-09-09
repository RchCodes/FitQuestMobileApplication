package com.example.fitquest;

import java.io.Serializable;

public class AvatarModel implements Serializable {
    public boolean isMale;
    public String chosenClass;

    public int hairOutlineRes;
    public int hairFillRes;
    public int eyesOutlineRes;
    public int eyesFillRes;
    public int noseRes;
    public int lipsRes;

    public int hairColor;
    public int eyesColor;
    public int lipsColor;

    // ✅ No-arg constructor (for Gson and SharedPreferences)
    public AvatarModel() { }

    // ✅ Param constructor (optional)
    public AvatarModel(boolean isMale, String chosenClass) {
        this.isMale = isMale;
        this.chosenClass = chosenClass;
    }
}
