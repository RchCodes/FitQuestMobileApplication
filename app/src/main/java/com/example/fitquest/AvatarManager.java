package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Saves avatar locally (SharedPreferences) and remotely (Realtime Database under /avatars/{uid}).
 */
public class AvatarManager {
    private static final String TAG = "AvatarManager";
    private static final String PREFS_NAME = "avatar_pref";
    private static final String KEY_AVATAR = "saved_avatar";

    public interface SaveCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Save avatar locally and to RTDB (if user signed in)
    public static void saveAvatar(@NonNull Context ctx, @NonNull AvatarModel avatar, SaveCallback cb) {
        try {
            // Save locally first (fast)
            SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String json = GsonHelper.toJson(avatar);
            prefs.edit().putString(KEY_AVATAR, json).apply();

            // Save to Firebase RTDB if user logged in
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("avatars")
                        .child(uid);

                ref.setValue(avatar)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Avatar saved to RTDB for uid=" + uid);
                            if (cb != null) cb.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save avatar to RTDB", e);
                            if (cb != null) cb.onFailure(e.getMessage());
                        });
            } else {
                // not signed in — local save only
                Log.d(TAG, "No user signed in. Avatar saved locally only.");
                if (cb != null) cb.onSuccess();
            }
        } catch (Exception e) {
            Log.e(TAG, "saveAvatar exception", e);
            if (cb != null) cb.onFailure(e.getMessage());
        }
    }

    // Load avatar from local cache (SharedPreferences).
    // Returns null if no saved avatar locally.
    public static AvatarModel loadAvatarLocal(@NonNull Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_AVATAR, null);
        if (json == null) return null;
        try {
            return GsonHelper.fromJson(json, AvatarModel.class);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse local avatar json", e);
            return null;
        }
    }

    // Remove avatar locally and remotely (if signed in)
    public static void clearAvatar(@NonNull Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_AVATAR).apply();

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("avatars").child(uid).removeValue()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Avatar removed from RTDB"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove avatar from RTDB", e));
        }
    }
}
