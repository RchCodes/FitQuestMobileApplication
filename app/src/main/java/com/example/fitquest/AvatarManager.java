package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    // Callback interface for async loading
    public interface AvatarLoadCallback {
        void onLoaded(AvatarModel avatar);
        void onError(String message);
    }

    /** Load avatar from Firebase Realtime Database */
    public static void loadAvatarOnline(AvatarLoadCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            if (callback != null) callback.onError("User not signed in");
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (callback != null) callback.onError("No avatar data found");
                    return;
                }

                try {
                    AvatarModel avatar = new AvatarModel();

                    avatar.setUsername(snapshot.child("username").getValue(String.class));
                    avatar.setGender(snapshot.child("gender").getValue(String.class));
                    avatar.setPlayerClass(snapshot.child("playerClass").getValue(String.class));

                    avatar.setBodyStyle(snapshot.child("avatar/body").getValue(String.class));
                    avatar.setOutfit(snapshot.child("avatar/outfit").getValue(String.class));
                    avatar.setWeapon(snapshot.child("avatar/weapon").getValue(String.class));

                    avatar.setHairOutline(snapshot.child("avatar/hair/outline").getValue(String.class));
                    avatar.setHairFill(snapshot.child("avatar/hair/fill").getValue(String.class));
                    avatar.setHairColor(snapshot.child("avatar/hair/color").getValue(String.class));

                    avatar.setEyesOutline(snapshot.child("avatar/eyes/outline").getValue(String.class));
                    avatar.setEyesFill(snapshot.child("avatar/eyes/fill").getValue(String.class));
                    avatar.setEyesColor(snapshot.child("avatar/eyes/color").getValue(String.class));

                    avatar.setNose(snapshot.child("avatar/nose").getValue(String.class));
                    avatar.setLips(snapshot.child("avatar/lips").getValue(String.class));

                    // Stats
                    Long coins = snapshot.child("coins").getValue(Long.class);
                    Long xp = snapshot.child("xp").getValue(Long.class);
                    Long level = snapshot.child("level").getValue(Long.class);
                    Long rank = snapshot.child("rank").getValue(Long.class);

                    avatar.setCoins(coins != null ? coins.intValue() : 0);
                    avatar.setXp(xp != null ? xp.intValue() : 0);
                    avatar.setLevel(level != null ? level.intValue() : 1);
                    avatar.setRank(rank != null ? rank.intValue() : 0);

                    if (callback != null) callback.onLoaded(avatar);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse avatar from Firebase", e);
                    if (callback != null) callback.onError("Failed to parse avatar");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load avatar from Firebase", error.toException());
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    public static void saveAvatar(Context context, AvatarModel avatar) {
    }
}
