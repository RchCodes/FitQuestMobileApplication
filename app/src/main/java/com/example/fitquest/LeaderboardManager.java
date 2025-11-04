package com.example.fitquest;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardManager {
    
    private static final String TAG = "LeaderboardManager";
    private static final String RANK_LEADERBOARD_PATH = "rank_leaderboard";
    private static final String QUEST_LEADERBOARD_PATH = "quest_leaderboard";
    
    public interface LeaderboardCallback {
        void onLeaderboardLoaded(List<LeaderboardEntry> entries, String type);
        void onError(String message);
    }
    
    /**
     * Update rank leaderboard with arena battle results
     */
    public static void updateRankLeaderboard(Context context, AvatarModel avatar) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rankRef = database.getReference(RANK_LEADERBOARD_PATH);
        
        String userId = avatar.getPlayerId();
        RankLeaderboardEntry entry = new RankLeaderboardEntry(
            avatar.getUsername(),
            avatar.getPlayerId(),
            avatar.getRankPoints(),
            avatar.getLevel(),
            avatar.getRank(),
            System.currentTimeMillis()
        );
        
        rankRef.child(userId).setValue(entry)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Rank leaderboard updated for user: " + avatar.getUsername());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update rank leaderboard: " + e.getMessage());
            });
    }
    
    /**
     * Update quest leaderboard when quest is completed
     */
    public static void updateQuestLeaderboard(Context context, String questId, QuestModel quest) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference questRef = database.getReference(QUEST_LEADERBOARD_PATH);
        
        // Get current avatar to get user info
        AvatarModel avatar = AvatarManager.loadAvatarOffline(context);
        if (avatar == null) return;
        
        String userId = avatar.getPlayerId();
        
        // Load existing entry or create new one
        questRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                QuestLeaderboardEntry entry;
                if (dataSnapshot.exists()) {
                    entry = dataSnapshot.getValue(QuestLeaderboardEntry.class);
                    if (entry != null) {
                        entry.incrementQuestsCompleted();
                        entry.setLastQuestTime(System.currentTimeMillis());
                    } else {
                        entry = new QuestLeaderboardEntry(
                            avatar.getUsername(),
                            avatar.getPlayerId(),
                            1,
                            System.currentTimeMillis()
                        );
                    }
                } else {
                    entry = new QuestLeaderboardEntry(
                        avatar.getUsername(),
                        avatar.getPlayerId(),
                        1,
                        System.currentTimeMillis()
                    );
                }
                
                questRef.child(userId).setValue(entry)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Quest leaderboard updated for user: " + avatar.getUsername());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update quest leaderboard: " + e.getMessage());
                    });
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to load quest leaderboard entry: " + databaseError.getMessage());
            }
        });
    }
    
    /**
     * Load rank leaderboard (top players by rank points)
     */
    public static void loadRankLeaderboard(LeaderboardCallback callback) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rankRef = database.getReference(RANK_LEADERBOARD_PATH);

        rankRef.orderByChild("rankPoints").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<LeaderboardEntry> entries = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RankLeaderboardEntry entry = snapshot.getValue(RankLeaderboardEntry.class);

                    if (entry != null) {
                        boolean needsUpdate = false;

                        // Fill missing rankPoints
                        if (entry.getRankPoints() < 0) {
                            entry.setRankPoints(0);
                            needsUpdate = true;
                        }

                        // Fill missing level
                        if (entry.getLevel() <= 0) {
                            entry.setLevel(1);
                            needsUpdate = true;
                        }

                        // Fill missing rank
                        if (entry.getRank() < 0) {
                            entry.setRank(0);
                            needsUpdate = true;
                        }

                        // Push fixed entry back to Firebase if any field was missing
                        if (needsUpdate) {
                            rankRef.child(entry.getUserId()).setValue(entry)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated missing fields for: " + entry.getUsername()))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update missing fields: " + e.getMessage()));
                        }

                        entries.add(entry);
                    }
                }

                // Sort by rank points descending
                entries.sort((a, b) -> Integer.compare(((RankLeaderboardEntry)b).getRankPoints(),
                        ((RankLeaderboardEntry)a).getRankPoints()));

                Log.d(TAG, "Rank leaderboard loaded: " + entries.size() + " entries");
                callback.onLeaderboardLoaded(entries, "rank");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Failed to load rank leaderboard: " + databaseError.getMessage());
            }
        });
    }


    /**
     * Load quest leaderboard (top players by quests completed)
     */
    public static void loadQuestLeaderboard(LeaderboardCallback callback) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference questRef = database.getReference(QUEST_LEADERBOARD_PATH);
        
        questRef.orderByChild("questsCompleted").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<LeaderboardEntry> entries = new ArrayList<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    QuestLeaderboardEntry entry = snapshot.getValue(QuestLeaderboardEntry.class);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
                
                // Sort by quests completed (descending)
                Collections.sort(entries, (a, b) -> Integer.compare(b.getQuestsCompleted(), a.getQuestsCompleted()));
                
                Log.d(TAG, "Quest leaderboard loaded: " + entries.size() + " entries");
                callback.onLeaderboardLoaded(entries, "quest");
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Failed to load quest leaderboard: " + databaseError.getMessage());
            }
        });
    }
}
