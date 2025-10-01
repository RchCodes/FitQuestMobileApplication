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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Goals {

    private final Dialog dialog;
    private final LinearLayout goalsList;
    private final AvatarModel avatar;

    // Define goal IDs mapping to descriptions
    private static final String[][] GOALS = {
            {"LEVEL_10", "Reach Level 10"},
            {"LEVEL_30", "Reach Level 20"},
            {"LEVEL_50", "Reach Level 30"},
            {"LEVEL_100", "Reach Level 100"},
            {"PUSHUP_100", "Complete 100 Push-ups"},
            {"SQUAT_100", "Complete 100 Squats"},
            {"DIP_100", "Complete 100 Tricep Dips"},
            {"STEPS_50000", "Complete 50,000 Steps"},
            {"STEPS_100000", "Complete 100,000 Steps"}
    };

    public Goals(Context context, AvatarModel avatar) {
        this.avatar = avatar;

        // Inflate layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.goals, null);

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        goalsList = view.findViewById(R.id.goals_list);

        initDefaultGoals();
        populateGoals(context);
    }

    private void initDefaultGoals() {
        for (String[] goal : GOALS) {
            String id = goal[0];
            if (!avatar.getGoalProgress().containsKey(id)) {
                avatar.setGoalState(id, GoalState.PENDING);
            }
        }
    }

    private void populateGoals(Context context) {
        goalsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Sort goals: PENDING → COMPLETED → CLAIMED
        List<Map.Entry<String, GoalState>> sortedGoals = new ArrayList<>(avatar.getGoalProgress().entrySet());
        Collections.sort(sortedGoals, Comparator.comparing(e -> e.getValue().ordinal()));

        for (Map.Entry<String, GoalState> entry : sortedGoals) {
            String goalId = entry.getKey();
            GoalState state = entry.getValue();
            String description = getGoalDescription(goalId);

            View goalItem = inflater.inflate(R.layout.goal_items, goalsList, false);
            TextView goalDescription = goalItem.findViewById(R.id.goal_description);
            Button claimButton = goalItem.findViewById(R.id.claim_button);

            goalDescription.setText(description);

            // Enable claim button only if COMPLETED
            claimButton.setEnabled(state == GoalState.COMPLETED);
            claimButton.setText(state == GoalState.CLAIMED ? "Claimed" : "Claim");

            claimButton.setOnClickListener(v -> {
                if (state != GoalState.COMPLETED) return;

                // Example reward: 50 coins, 20 XP
                avatar.addCoins(50);
                avatar.addXp(20);
                avatar.setGoalState(goalId, GoalState.CLAIMED);

                Toast.makeText(context, "Claimed reward for: " + description, Toast.LENGTH_SHORT).show();

                // Refresh UI
                populateGoals(context);
            });

            goalsList.addView(goalItem);
        }
    }

    private String getGoalDescription(String goalId) {
        for (String[] g : GOALS) {
            if (g[0].equals(goalId)) return g[1];
        }
        return goalId;
    }

    public void show() {
        dialog.show();
    }
}
