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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        avatar.updateSkillIds();
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

        try {
            // Try to sanitize older JSON that might contain concrete skill objects
            com.google.gson.JsonElement rootElem = com.google.gson.JsonParser.parseString(avatarJson);
            if (rootElem != null && rootElem.isJsonObject()) {
                com.google.gson.JsonObject rootObj = rootElem.getAsJsonObject();

                // Helper: sanitize one field (activeSkills / passiveSkills)
                sanitizeSkillField(rootObj, "activeSkills");
                sanitizeSkillField(rootObj, "passiveSkills");

                // Also handle older naming if you saved under activeSkillIds/passiveSkillIds
                sanitizeSkillField(rootObj, "activeSkillIds");
                sanitizeSkillField(rootObj, "passiveSkillIds");

                // Convert back to string for Gson
                avatarJson = rootObj.toString();
            }

            Gson gson = new Gson();
            AvatarModel avatar = gson.fromJson(avatarJson, AvatarModel.class);

            // If the JSON contained only skill IDs, load runtime skill objects now.
            if (avatar != null) {
                avatar.loadSkillsFromIds();
            }
            return avatar;

        } catch (Exception e) {
            e.printStackTrace();
            // If parsing failed, remove corrupted data to avoid repeated crashes:
            // prefs.edit().remove(AVATAR_KEY).apply();
            return null;
        }
    }

    /** Utility used above to turn an array of skill objects into array of ids (strings) */
    private static void sanitizeSkillField(com.google.gson.JsonObject rootObj, String fieldName) {
        if (!rootObj.has(fieldName)) return;
        com.google.gson.JsonElement el = rootObj.get(fieldName);
        if (el == null || !el.isJsonArray()) return;

        com.google.gson.JsonArray arr = el.getAsJsonArray();
        boolean needsFix = false;

        // If first element is an object (likely serialized SkillModel), we need to extract ids
        for (com.google.gson.JsonElement item : arr) {
            if (item != null && item.isJsonObject()) {
                needsFix = true;
                break;
            }
        }

        if (!needsFix) return; // already an array of strings

        com.google.gson.JsonArray idArray = new com.google.gson.JsonArray();
        for (com.google.gson.JsonElement item : arr) {
            if (item != null && item.isJsonObject()) {
                com.google.gson.JsonObject obj = item.getAsJsonObject();
                // try 'id' property
                if (obj.has("id")) {
                    com.google.gson.JsonElement idEl = obj.get("id");
                    if (idEl != null && idEl.isJsonPrimitive()) {
                        idArray.add(idEl.getAsString());
                        continue;
                    }
                }else if (obj.has("skillId")) {
                    idArray.add(obj.get("skillId").getAsString());
                }
                // fallback: try 'getId' style property or 'skillId' etc. (adapt as needed)
                // If can't find an id, skip it.
            } else if (item != null && item.isJsonPrimitive()) {
                // already string -> keep
                idArray.add(item.getAsString());
            }
        }

        // Replace field with array of id strings
        rootObj.add(fieldName, idArray);
    }


    public static void saveAvatarOnline(AvatarModel avatar) {
        saveAvatarOnline(avatar, null);
    }

    // --- Save online (Firebase RTDB) ---
    public static void saveAvatarOnline(AvatarModel avatar, SaveCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            if (callback != null) callback.onFailure(new IllegalStateException("User not signed in"));
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("username", avatar.getUsername());
        rootMap.put("gender", avatar.getGender());
        rootMap.put("playerClass", avatar.getPlayerClass());
        rootMap.put("coins", avatar.getCoins());
        rootMap.put("xp", avatar.getXp());
        rootMap.put("level", avatar.getLevel());
        rootMap.put("rank", avatar.getRank());

        // Avatar appearance (nested map)
        Map<String, Object> avatarMap = new HashMap<>();
        avatarMap.put("body", avatar.getBodyStyle());
        avatarMap.put("outfit", avatar.getOutfit());
        avatarMap.put("weapon", avatar.getWeapon());

        Map<String, Object> hairMap = new HashMap<>();
        hairMap.put("outline", avatar.getHairOutline());
        hairMap.put("fill", avatar.getHairFill());
        hairMap.put("color", avatar.getHairColor());
        avatarMap.put("hair", hairMap);

        Map<String, Object> eyesMap = new HashMap<>();
        eyesMap.put("outline", avatar.getEyesOutline());
        eyesMap.put("fill", avatar.getEyesFill());
        eyesMap.put("color", avatar.getEyesColor());
        avatarMap.put("eyes", eyesMap);

        avatarMap.put("nose", avatar.getNose());
        avatarMap.put("lips", avatar.getLips());

        rootMap.put("avatar", avatarMap);

        // Collections/skills
        rootMap.put("ownedGear", new ArrayList<>(avatar.getOwnedGear()));
        // ensure ID lists are up to date
        avatar.updateSkillIds();
        rootMap.put("activeSkillIds", avatar.getActiveSkillIds() != null ? avatar.getActiveSkillIds() : new ArrayList<>());
        rootMap.put("passiveSkillIds", avatar.getPassiveSkillIds() != null ? avatar.getPassiveSkillIds() : new ArrayList<>());


        ref.updateChildren(rootMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Avatar saved online successfully");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save avatar online", e);
                    if (callback != null) callback.onFailure(e);
                });
    }


    // Callback interface for async loading
    public interface AvatarLoadCallback {
        /**
         * Called when the avatar is fully loaded (main callback)
         */
        void onLoaded(AvatarModel avatar);

        /**
         * Called if loading fails
         */
        void onError(String message);

        /**
         * Optional early callback for when avatar data is available before full processing
         * Default implementation does nothing so old code won’t break
         */
        default void onAvatarLoaded(AvatarModel avatar) { }
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
                    avatar.initializeClassData();

                    DataSnapshot avatarSnap = snapshot.child("avatar");
                    if (avatarSnap.exists()) {
                        avatar.setBodyStyle(avatarSnap.child("body").getValue(String.class));
                        avatar.setOutfit(avatarSnap.child("outfit").getValue(String.class));
                        avatar.setWeapon(avatarSnap.child("weapon").getValue(String.class));

                        DataSnapshot hairSnap = avatarSnap.child("hair");
                        if (hairSnap.exists()) {
                            avatar.setHairOutline(hairSnap.child("outline").getValue(String.class));
                            avatar.setHairFill(hairSnap.child("fill").getValue(String.class));
                            avatar.setHairColor(hairSnap.child("color").getValue(String.class));
                        }

                        DataSnapshot eyesSnap = avatarSnap.child("eyes");
                        if (eyesSnap.exists()) {
                            avatar.setEyesOutline(eyesSnap.child("outline").getValue(String.class));
                            avatar.setEyesFill(eyesSnap.child("fill").getValue(String.class));
                            avatar.setEyesColor(eyesSnap.child("color").getValue(String.class));
                        }

                        avatar.setNose(avatarSnap.child("nose").getValue(String.class));
                        avatar.setLips(avatarSnap.child("lips").getValue(String.class));
                    }

                    // Stats
                    Long coins = snapshot.child("coins").getValue(Long.class);
                    Long xp = snapshot.child("xp").getValue(Long.class);
                    Long level = snapshot.child("level").getValue(Long.class);
                    Long rank = snapshot.child("rank").getValue(Long.class);

                    avatar.setCoins(coins != null ? coins.intValue() : 0);
                    avatar.setXp(xp != null ? xp.intValue() : 0);
                    avatar.setLevel(level != null ? level.intValue() : 1);
                    avatar.setRank(rank != null ? rank.intValue() : 0);

                    // Load active skill IDs
                    // --- read active skill ids (support both "activeSkillIds" and old "activeSkills") ---
                    List<String> activeSkillIds = new ArrayList<>();
                    DataSnapshot activeIdsSnap = snapshot.child("activeSkillIds");
                    DataSnapshot activeSkillsSnap = snapshot.child("activeSkills");

                    if (activeIdsSnap.exists()) {
                        for (DataSnapshot s : activeIdsSnap.getChildren()) {
                            String id = s.getValue(String.class);
                            if (id != null) activeSkillIds.add(id);
                        }
                    } else if (activeSkillsSnap.exists()) {
                        for (DataSnapshot s : activeSkillsSnap.getChildren()) {
                            // value might be a string id, or a map/object with an "id" field
                            String id = s.getValue(String.class);
                            if (id != null) {
                                activeSkillIds.add(id);
                            } else if (s.getValue() instanceof Map) {
                                Object maybeId = s.child("id").getValue();
                                if (maybeId != null) activeSkillIds.add(String.valueOf(maybeId));
                            }
                        }
                    }
                    avatar.setActiveSkillIds(activeSkillIds);

// --- passive ---
                    List<String> passiveSkillIds = new ArrayList<>();
                    DataSnapshot passiveIdsSnap = snapshot.child("passiveSkillIds");
                    DataSnapshot passiveSkillsSnap = snapshot.child("passiveSkills");

                    if (passiveIdsSnap.exists()) {
                        for (DataSnapshot s : passiveIdsSnap.getChildren()) {
                            String id = s.getValue(String.class);
                            if (id != null) passiveSkillIds.add(id);
                        }
                    } else if (passiveSkillsSnap.exists()) {
                        for (DataSnapshot s : passiveSkillsSnap.getChildren()) {
                            String id = s.getValue(String.class);
                            if (id != null) {
                                passiveSkillIds.add(id);
                            } else if (s.getValue() instanceof Map) {
                                Object maybeId = s.child("id").getValue();
                                if (maybeId != null) passiveSkillIds.add(String.valueOf(maybeId));
                            }
                        }
                    }
                    avatar.setPassiveSkillIds(passiveSkillIds);


                    // After setting all avatar fields, load battle history
                    avatar.loadBattleHistoryFromFirebase(() -> {
                        if (callback != null) callback.onAvatarLoaded(avatar);
                    });
                    return;

                } catch (Exception e) {
                    if (callback != null) callback.onError("Failed to parse avatar: " + e.getMessage());
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load avatar from Firebase", error.toException());
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    public interface SaveCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

}
