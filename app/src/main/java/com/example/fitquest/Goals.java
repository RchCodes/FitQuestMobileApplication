package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Goals {

    private final Dialog dialog;
    private final LinearLayout goalsList;

    // Sample static goals
    private static final String[] goalDescriptions = {
            "Reach Level 10",
            "Reach Level 30",
            "Reach Level 50",
            "Reach Level 100",
            "Complete 100 Push-ups",
            "Complete 100 Squats",
            "Complete 100 Tricep Dips",
            "Complete 50,000 Steps",
            "Complete 100,000 Steps"
    };

    public Goals(Context context) {
        // Inflate layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.goals, null);

        // Dialog setup
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        goalsList = view.findViewById(R.id.goals_list);

        populateGoals(context);
    }

    private void populateGoals(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);

        for (String goal : goalDescriptions) {
            View goalItem = inflater.inflate(R.layout.goal_items, goalsList, false);

            TextView goalDescription = goalItem.findViewById(R.id.goal_description);
            Button claimButton = goalItem.findViewById(R.id.claim_button);

            goalDescription.setText(goal);

            claimButton.setOnClickListener(v ->
                    Toast.makeText(context, "Claimed: " + goal, Toast.LENGTH_SHORT).show()
            );

            goalsList.addView(goalItem);
        }
    }

    public void show() {
        dialog.show();
    }
}
