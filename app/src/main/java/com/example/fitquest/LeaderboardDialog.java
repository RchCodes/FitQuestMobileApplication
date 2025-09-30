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
        // recyclerView.setAdapter(adapter); // plug in your adapter here

        btnBack.setOnClickListener(v -> dismiss());

        // Firebase setup
        dbRef = FirebaseDatabase.getInstance().getReference("avatars");
        loadLeaderboard();

        return view;
    }

    private void loadLeaderboard() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                players.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    AvatarModel avatar = child.getValue(AvatarModel.class);
                    if (avatar != null) {
                        players.add(avatar);
                    }
                }
                // Example: sort by level (highest first)
                Collections.sort(players, Comparator.comparingInt(AvatarModel::getLevel).reversed());

                // if you have adapter:
                // adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log or toast error
            }
        });
    }
}
