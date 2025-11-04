package com.example.fitquest;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardDialog extends DialogFragment {

    private RecyclerView recyclerView;
    private ImageView btnBack, btnArena, btnQuest;
    private List<RankLeaderboardEntry> leaderboardEntries = new ArrayList<>();
    private LeaderboardAdapter adapter;

    private static final String TAG = "LeaderboardDialog";

    private enum LeaderboardType { RANK, QUEST }
    private LeaderboardType currentType = LeaderboardType.RANK;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.leaderboard_dialog, container, false);

        recyclerView = view.findViewById(R.id.recycler_leaderboard);
        btnBack = view.findViewById(R.id.btn_back);
        btnArena = view.findViewById(R.id.button_lb_arena);
        btnQuest = view.findViewById(R.id.button_lb_quest2);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(leaderboardEntries, getCurrentUserId());
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> dismiss());

        // Tab switching
        btnArena.setOnClickListener(v -> switchLeaderboard(LeaderboardType.RANK));
        btnQuest.setOnClickListener(v -> switchLeaderboard(LeaderboardType.QUEST));

        // Load default leaderboard (Rank)
        switchLeaderboard(currentType);

        return view;
    }

    /** Switch leaderboard type and reload data */
    private void switchLeaderboard(LeaderboardType type) {
        currentType = type;
        if (type == LeaderboardType.RANK) {
            loadRankLeaderboard();
            btnArena.setImageResource(R.drawable.button_lb_arena);
            btnArena.setAlpha(1f);
            btnQuest.setImageResource(R.drawable.button_lb_quest);
            btnQuest.setAlpha(0.5f);
        } else {
            loadQuestLeaderboard();
            btnArena.setAlpha(0.5f);
            btnArena.setImageResource(R.drawable.button_lb_arena2);
            btnQuest.setAlpha(1f);
            btnQuest.setImageResource(R.drawable.button_lb_quest2);
        }
    }

    /** Load rank leaderboard from Firebase */
    private void loadRankLeaderboard() {
        // Show loading state
        leaderboardEntries.clear();
        adapter.notifyDataSetChanged();
        
        LeaderboardManager.loadRankLeaderboard(new LeaderboardManager.LeaderboardCallback() {
            @Override
            public void onLeaderboardLoaded(List<LeaderboardEntry> entries, String type) {
                if (getActivity() == null) return; // Activity destroyed
                
                leaderboardEntries.clear();
                for (LeaderboardEntry entry : entries) {
                    if (entry instanceof RankLeaderboardEntry) {
                        leaderboardEntries.add((RankLeaderboardEntry) entry);
                    }
                }
                // Sort descending
                leaderboardEntries.sort((a, b) -> Integer.compare(b.getRankPoints(), a.getRankPoints()));
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Rank leaderboard loaded: " + leaderboardEntries.size() + " players");
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Rank leaderboard load error: " + message);
                // Could show error message to user here
            }
        });
    }

    /** Load quest leaderboard from Firebase */
    private void loadQuestLeaderboard() {
        // Show loading state
        leaderboardEntries.clear();
        adapter.notifyDataSetChanged();
        
        LeaderboardManager.loadQuestLeaderboard(new LeaderboardManager.LeaderboardCallback() {
            @Override
            public void onLeaderboardLoaded(List<LeaderboardEntry> entries, String type) {
                if (getActivity() == null) return; // Activity destroyed
                
                leaderboardEntries.clear();
                for (LeaderboardEntry entry : entries) {
                    // Map QuestLeaderboardEntry → RankLeaderboardEntry for adapter compatibility
                    if (entry instanceof QuestLeaderboardEntry) {
                        QuestLeaderboardEntry q = (QuestLeaderboardEntry) entry;
                        RankLeaderboardEntry rankEntry = new RankLeaderboardEntry(
                                q.getUsername(),
                                q.getUserId(),
                                q.getQuestsCompleted(), // treat as "score"
                                1, // default level
                                0, // default rank
                                q.getLastQuestTime()
                        );
                        leaderboardEntries.add(rankEntry);
                    }
                }
                // Sort descending by quests completed
                leaderboardEntries.sort((a, b) -> Integer.compare(b.getRankPoints(), a.getRankPoints()));
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Quest leaderboard loaded: " + leaderboardEntries.size() + " players");
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Quest leaderboard load error: " + message);
                // Could show error message to user here
            }
        });
    }

    /** Get current user ID from AvatarManager */
    private String getCurrentUserId() {
        AvatarModel avatar = AvatarManager.loadAvatarOffline(getContext());
        return avatar != null ? avatar.getPlayerId() : "";
    }
}
