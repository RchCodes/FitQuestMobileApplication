package com.example.fitquest;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.PopupWindow;

public class Challenge {

    private final Activity activity;
    private static final int TOTAL_LEVELS = 25;
    private static final int CURRENT_LEVEL = 3; // Change this to reflect player's current progress

    public Challenge(Activity activity) {
        this.activity = activity;
    }

    public void show() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View popupView = inflater.inflate(R.layout.challenge, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                true
        );

        View rootView = activity.findViewById(android.R.id.content);
        popupWindow.showAtLocation(rootView, android.view.Gravity.CENTER, 0, 0);

        GridLayout gridLayout = popupView.findViewById(R.id.challenge_grid);

        for (int i = 1; i <= TOTAL_LEVELS; i++) {
            Button button = new Button(activity);
            button.setText(String.valueOf(i));
            button.setTextColor(activity.getResources().getColor(android.R.color.white));
            button.setPadding(8, 8, 8, 8);

            // Assign background based on status
            if (i < CURRENT_LEVEL) {
                button.setBackgroundResource(R.drawable.bg_challenge_unlocked);
                button.setEnabled(true);
            } else if (i == CURRENT_LEVEL) {
                button.setBackgroundResource(R.drawable.bg_challenge_current);
                button.setEnabled(true);
            } else {
                button.setBackgroundResource(R.drawable.bg_challenge_locked);
                button.setEnabled(false); // Prevent clicking locked levels
            }

            // Set layout size
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 120;
            params.height = 120;
            button.setLayoutParams(params);

            int finalI = i;
            button.setOnClickListener(v -> {
                if (finalI <= CURRENT_LEVEL) {
                    // You can replace this with actual navigation logic
                    // Toast.makeText(activity, "Starting Level " + finalI, Toast.LENGTH_SHORT).show();
                }
            });

            gridLayout.addView(button);
        }
    }
}
