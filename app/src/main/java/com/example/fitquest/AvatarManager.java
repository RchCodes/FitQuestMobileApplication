package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class AvatarManager {

    private static final String TAG = "AvatarManager";
    private static final String PREFS_NAME = "FitQuestPrefs";
    private static final String AVATAR_KEY = "avatar_data";

    private static final Gson gson = new Gson();
    // --- Save offline ---
    public static void saveAvatarOffline(Context context, AvatarModel avatar) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String avatarJson = gson.toJson(avatar);
        editor.putString(AVATAR_KEY, avatarJson);
        editor.apply();
    }

    /** Load avatar from SharedPreferences; returns null if none exists */
    public static AvatarModel loadAvatarOffline(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String avatarJson = prefs.getString(AVATAR_KEY, null);
        if (avatarJson == null) {
            return null; // no saved avatar
        }
        Gson gson = new Gson();
        try {
            return gson.fromJson(avatarJson, AvatarModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // fallback if JSON corrupted
        }
    }

    // --- Save online (Firebase RTDB) ---
    public static void saveAvatarOnline(AvatarModel avatar) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return; // Ensure user is signed in

        String uid = auth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);

        // Build a map with all avatar fields + stats
        Map<String, Object> avatarMap = new HashMap<>();
        avatarMap.put("username", avatar.getUsername());
        avatarMap.put("gender", avatar.getGender());
        avatarMap.put("playerClass", avatar.getPlayerClass());

        avatarMap.put("avatar/body", avatar.getBodyStyle());
        avatarMap.put("avatar/outfit", avatar.getOutfit());
        avatarMap.put("avatar/weapon", avatar.getWeapon());

        avatarMap.put("avatar/hair/outline", avatar.getHairOutline());
        avatarMap.put("avatar/hair/fill", avatar.getHairFill());
        avatarMap.put("avatar/hair/color", avatar.getHairColor());

        avatarMap.put("avatar/eyes/outline", avatar.getEyesOutline());
        avatarMap.put("avatar/eyes/fill", avatar.getEyesFill());
        avatarMap.put("avatar/eyes/color", avatar.getEyesColor());

        avatarMap.put("avatar/nose", avatar.getNose());
        avatarMap.put("avatar/lips", avatar.getLips());

        // Stats
        avatarMap.put("coins", avatar.getCoins());
        avatarMap.put("xp", avatar.getXp());
        avatarMap.put("level", avatar.getLevel());
        avatarMap.put("rank", avatar.getRank()); // <-- add rank

        // Push all at once
        ref.updateChildren(avatarMap)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Avatar saved online successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save avatar online", e));
    }

}
