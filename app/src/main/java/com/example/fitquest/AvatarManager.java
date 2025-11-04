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
        rootMap.put("rankPoints", avatar.getRankPoints());
        rootMap.put("questPoints", avatar.getQuestPoints());
        rootMap.put("playerId", avatar.getPlayerId());
        
        // Stats
        rootMap.put("strength", avatar.getStrength());
        rootMap.put("endurance", avatar.getEndurance());
        rootMap.put("agility", avatar.getAgility());
        rootMap.put("flexibility", avatar.getFlexibility());
        rootMap.put("stamina", avatar.getStamina());
        
        // Physique stats
        rootMap.put("armPoints", avatar.getArmPoints());
        rootMap.put("legPoints", avatar.getLegPoints());
        rootMap.put("chestPoints", avatar.getChestPoints());
        rootMap.put("backPoints", avatar.getBackPoints());
        
        // Free points
        rootMap.put("freePhysiquePoints", avatar.getFreePhysiquePoints());
        rootMap.put("freeAttributePoints", avatar.getFreeAttributePoints());

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
        rootMap.put("equippedGear", avatar.getEquippedGear());
        // ensure ID lists are up to date
        avatar.updateSkillIds();
        rootMap.put("activeSkillIds", avatar.getActiveSkillIds() != null ? avatar.getActiveSkillIds() : new ArrayList<>());
        rootMap.put("passiveSkillIds", avatar.getPassiveSkillIds() != null ? avatar.getPassiveSkillIds() : new ArrayList<>());
        rootMap.put("completedChallenges", new ArrayList<>(avatar.getCompletedChallengeIds()));
        
        // Goals and badges
        rootMap.put("goalProgress", avatar.getGoalProgress());
        rootMap.put("avatarBadges", avatar.getAvatarBadges());


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

    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                    Long rankPoints = snapshot.child("rankPoints").getValue(Long.class);

                    avatar.setCoins(coins != null ? coins.intValue() : 0);
                    avatar.setXp(xp != null ? xp.intValue() : 0);
                    avatar.setLevel(level != null ? level.intValue() : 1);
                    avatar.setRank(rank != null ? rank.intValue() : 0);
                    avatar.setRankPoints(rankPoints != null ? rankPoints.intValue() : 0);
                    
                    // Load quest points and player ID
                    Long questPoints = snapshot.child("questPoints").getValue(Long.class);
                    String playerId = snapshot.child("playerId").getValue(String.class);
                    
                    avatar.setQuestPoints(questPoints != null ? questPoints.intValue() : 0);
                    avatar.setPlayerId(playerId);
                    
                    // Load attributes
                    Long strength = snapshot.child("strength").getValue(Long.class);
                    Long endurance = snapshot.child("endurance").getValue(Long.class);
                    Long agility = snapshot.child("agility").getValue(Long.class);
                    Long flexibility = snapshot.child("flexibility").getValue(Long.class);
                    Long stamina = snapshot.child("stamina").getValue(Long.class);
                    
                    avatar.setStrength(strength != null ? strength.intValue() : 0);
                    avatar.setEndurance(endurance != null ? endurance.intValue() : 0);
                    avatar.setAgility(agility != null ? agility.intValue() : 0);
                    avatar.setFlexibility(flexibility != null ? flexibility.intValue() : 0);
                    avatar.setStamina(stamina != null ? stamina.intValue() : 0);
                    
                    // Load physique stats
                    Long armPoints = snapshot.child("armPoints").getValue(Long.class);
                    Long legPoints = snapshot.child("legPoints").getValue(Long.class);
                    Long chestPoints = snapshot.child("chestPoints").getValue(Long.class);
                    Long backPoints = snapshot.child("backPoints").getValue(Long.class);
                    
                    avatar.setArmPoints(armPoints != null ? armPoints.intValue() : 0);
                    avatar.setLegPoints(legPoints != null ? legPoints.intValue() : 0);
                    avatar.setChestPoints(chestPoints != null ? chestPoints.intValue() : 0);
                    avatar.setBackPoints(backPoints != null ? backPoints.intValue() : 0);
                    
                    // Load free points
                    Long freePhysiquePoints = snapshot.child("freePhysiquePoints").getValue(Long.class);
                    Long freeAttributePoints = snapshot.child("freeAttributePoints").getValue(Long.class);
                    
                    avatar.setFreePhysiquePoints(freePhysiquePoints != null ? freePhysiquePoints.intValue() : 1);
                    avatar.setFreeAttributePoints(freeAttributePoints != null ? freeAttributePoints.intValue() : 1);

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

                    // --- Load owned gear ---
                    DataSnapshot ownedGearSnap = snapshot.child("ownedGear");
                    if (ownedGearSnap.exists()) {
                        for (DataSnapshot s : ownedGearSnap.getChildren()) {
                            String gearId = s.getValue(String.class);
                            if (gearId != null) {
                                avatar.addGear(gearId);
                            }
                        }
                    }
                    
                    // --- Load equipped gear ---
                    DataSnapshot equippedGearSnap = snapshot.child("equippedGear");
                    if (equippedGearSnap.exists()) {
                        for (DataSnapshot s : equippedGearSnap.getChildren()) {
                            String gearId = s.getValue(String.class);
                            if (gearId != null) {
                                try {
                                    GearType gearType = GearType.valueOf(s.getKey().toUpperCase());
                                    avatar.equipGear(gearType, gearId);
                                } catch (IllegalArgumentException e) {
                                    Log.w(TAG, "Unknown gear type: " + s.getKey());
                                }
                            }
                        }
                    }
                    
                    // --- Load goal progress ---
                    DataSnapshot goalProgressSnap = snapshot.child("goalProgress");
                    if (goalProgressSnap.exists()) {
                        for (DataSnapshot s : goalProgressSnap.getChildren()) {
                            String goalId = s.getKey();
                            String stateStr = s.getValue(String.class);
                            if (goalId != null && stateStr != null) {
                                try {
                                    GoalState state = GoalState.valueOf(stateStr);
                                    avatar.setGoalState(goalId, state);
                                } catch (IllegalArgumentException e) {
                                    Log.w(TAG, "Unknown goal state: " + stateStr);
                                }
                            }
                        }
                    }
                    
                    // --- Load avatar badges ---
                    DataSnapshot avatarBadgesSnap = snapshot.child("avatarBadges");
                    if (avatarBadgesSnap.exists()) {
                        for (DataSnapshot s : avatarBadgesSnap.getChildren()) {
                            Long badgeId = s.getValue(Long.class);
                            if (badgeId != null) {
                                avatar.addAvatarBadge(badgeId.intValue());
                            }
                        }
                    }

                    // --- Load completed challenges ---
                    DataSnapshot completedChallengesSnap = snapshot.child("completedChallenges");
                    if (completedChallengesSnap.exists()) {
                        for (DataSnapshot s : completedChallengesSnap.getChildren()) {
                            String id = s.getValue(String.class);
                            if (id != null) {
                                avatar.addCompletedChallenge(id);
                            }
                        }
                    }

                    // After setting all avatar fields, load battle history
                    avatar.loadBattleHistoryFromFirebase(() -> {
                        if (callback != null) {
                            callback.onAvatarLoaded(avatar); // optional early callback
                            callback.onLoaded(avatar);       // main callback → triggers navigation
                        }
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
