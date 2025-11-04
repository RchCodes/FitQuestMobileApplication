package com.example.fitquest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class BattleHistoryDialog extends DialogFragment {

    private AvatarModel avatar;

    public BattleHistoryDialog(AvatarModel avatar) {
        this.avatar = avatar;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.battle_history_dialog, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerBattleHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load battle history asynchronously from Firebase
        if (avatar != null) {
            // Show loading state initially
            BattleHistoryAdapter loadingAdapter = new BattleHistoryAdapter(new ArrayList<>());
            recyclerView.setAdapter(loadingAdapter);
            
            avatar.loadBattleHistoryFromFirebase(() -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        List<BattleHistoryModel> historyList = avatar.getBattleHistory();
                        BattleHistoryAdapter adapter = new BattleHistoryAdapter(historyList);
                        recyclerView.setAdapter(adapter);
                        
                        // Animate RecyclerView items
                        recyclerView.setAlpha(0f);
                        recyclerView.animate().alpha(1f).setDuration(400).start();
                    });
                }
            });
        } else {
            BattleHistoryAdapter adapter = new BattleHistoryAdapter(null);
            recyclerView.setAdapter(adapter);
        }

        // Swipe-to-dismiss
        GestureDetectorCompat gestureDetector = new GestureDetectorCompat(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        if (Math.abs(e2.getY() - e1.getY()) > 200 && Math.abs(velocityY) > 1000) {
                            dismiss();
                            return true;
                        }
                        return false;
                    }
                });

        view.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
