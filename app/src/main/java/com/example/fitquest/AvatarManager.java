package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class AvatarManager {

    private static final String PREFS_NAME = "avatar_pref";
    private static final String KEY_AVATAR = "saved_avatar";

    // Save avatar
    public static void saveAvatar(Context context, AvatarModel avatar) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(avatar);
        editor.putString(KEY_AVATAR, json);
        editor.apply();
    }

    // Load avatar
    public static AvatarModel loadAvatar(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_AVATAR, null);
        if (json == null) return null;

        Gson gson = new Gson();
        return gson.fromJson(json, AvatarModel.class);
    }

    // Reset avatar
    public static void clearAvatar(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_AVATAR).apply();
    }
}
