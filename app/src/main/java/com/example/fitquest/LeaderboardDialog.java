package com.example.fitquest;

import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitquest.AvatarModel;
import com.example.fitquest.R;
import com.example.fitquest.RankLeaderboardEntry;
import com.example.fitquest.LeaderboardManager;
import com.example.fitquest.LeaderboardEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardDialog extends DialogFragment {

    private RecyclerView recyclerView;
    private ImageView btnBack;
    private DatabaseReference dbRef;
    private List<AvatarModel> players = new ArrayList<>();
    private List<RankLeaderboardEntry> leaderboardEntries = new ArrayList<>();
    private RecyclerView.Adapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Make background transparent, style will handle the rest
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Force dialog to match parent AFTER it’s created
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(leaderboardEntries); // You must have a LeaderboardAdapter for RankLeaderboardEntry
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> dismiss());

        loadLeaderboard();

        return view;
    }

    private void loadLeaderboard() {
        LeaderboardManager.loadRankLeaderboard(new LeaderboardManager.LeaderboardCallback() {
            @Override
            public void onLeaderboardLoaded(List<LeaderboardEntry> entries, String type) {
                leaderboardEntries.clear();
                for (LeaderboardEntry entry : entries) {
                    if (entry instanceof RankLeaderboardEntry) {
                        leaderboardEntries.add((RankLeaderboardEntry) entry);
                    }
                }
                // Sort by rank points descending (should already be sorted)
                leaderboardEntries.sort(Comparator.comparingInt(RankLeaderboardEntry::getRankPoints).reversed());
                if (adapter != null) adapter.notifyDataSetChanged();
            }
            @Override
            public void onError(String message) {
                // Optionally show a toast or log
            }
        });
    }
}
