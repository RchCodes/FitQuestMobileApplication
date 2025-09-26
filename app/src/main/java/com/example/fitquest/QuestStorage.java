package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestStorage {
    private static final String TAG = "QuestStorage";
    private static final String PREFS = "FitQuestPrefs";
    private static final String QUESTS_KEY = "quests_data_v1";
    private static final Gson gson = new Gson();

    // Save offline
    public static void saveQuestsOffline(Context ctx, List<QuestModel> quests) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = gson.toJson(quests);
        prefs.edit().putString(QUESTS_KEY, json).apply();
    }

    // Load offline (fallback to default empty list)
    public static List<QuestModel> loadQuestsOffline(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = prefs.getString(QUESTS_KEY, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<QuestModel>>(){}.getType();
        try {
            return gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, "Failed parse quests json", e);
            return new ArrayList<>();
        }
    }

    // Save online (per-user)
    public static void saveQuestsOnline(List<QuestModel> quests) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("quests");
        ref.setValue(quests)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Quests saved online"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed saving quests online", e));
    }

    // Optionally implement loadQuestsOnline(...) if you want remote-first behavior
}
