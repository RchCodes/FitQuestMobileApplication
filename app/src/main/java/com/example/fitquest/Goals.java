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
    private final ChallengeManager challengeManager;

    // --- Base Goals (non-challenge) ---
    private static final List<BaseGoal> BASE_GOALS = List.of(
            new BaseGoal("LEVEL_10", "Reach Level 10", R.drawable.badge_level_10),
            new BaseGoal("LEVEL_25", "Reach Level 25", R.drawable.badge_level_25),
            new BaseGoal("LEVEL_50", "Reach Level 50", R.drawable.badge_level_50),
            new BaseGoal("LEVEL_100", "Reach Level 100", R.drawable.badge_level_100),

            // Push-ups
            new BaseGoal("PUSHUP_100", "Complete 100 Push-ups", R.drawable.badge_pushup_100),
            new BaseGoal("PUSHUP_200", "Complete 200 Push-ups", R.drawable.badge_pushup_100),
            new BaseGoal("PUSHUP_500", "Complete 500 Push-ups", R.drawable.badge_pushup_100),

            // Squats
            new BaseGoal("SQUAT_100", "Complete 100 Squats", R.drawable.badge_squat_100),
            new BaseGoal("SQUAT_200", "Complete 200 Squats", R.drawable.badge_squat_100),
            new BaseGoal("SQUAT_500", "Complete 500 Squats", R.drawable.badge_squat_100),

            // Steps
            new BaseGoal("STEPS_25000", "Complete 25,000 Steps", R.drawable.badge_steps_25000),
            new BaseGoal("STEPS_50000", "Complete 50,000 Steps", R.drawable.badge_steps_50000),
            new BaseGoal("STEPS_100000", "Complete 100,000 Steps", R.drawable.badge_steps_100000),

            // Jumping Jacks
            new BaseGoal("JJ_100", "Complete 100 Jumping Jacks", R.drawable.badge_jumping_jacks_100),
            new BaseGoal("JJ_500", "Complete 500 Jumping Jacks", R.drawable.badge_jumping_jacks_100),
            new BaseGoal("JJ_1000", "Complete 1,000 Jumping Jacks", R.drawable.badge_jumping_jacks_100),

            // Tree Pose
            new BaseGoal("TREE_60", "Hold Tree Pose 60s", R.drawable.badge_tree_pose_60),
            new BaseGoal("TREE_300", "Hold Tree Pose 300s", R.drawable.badge_tree_pose_60),
            new BaseGoal("TREE_600", "Hold Tree Pose 600s", R.drawable.badge_tree_pose_60),

            // Sit-ups
            new BaseGoal("SITUP_100", "Complete 100 Sit-ups", R.drawable.badge_situp_100),
            new BaseGoal("SITUP_400", "Complete 400 Sit-ups", R.drawable.badge_situp_100),
            new BaseGoal("SITUP_800", "Complete 800 Sit-ups", R.drawable.badge_situp_100),

            // Lunges
            new BaseGoal("LUNGE_50", "Complete 50 Lunges", R.drawable.badge_lunges_100),
            new BaseGoal("LUNGE_200", "Complete 200 Lunges", R.drawable.badge_lunges_100),
            new BaseGoal("LUNGE_500", "Complete 500 Lunges", R.drawable.badge_lunges_100),

            new BaseGoal("STEPS_25000", "Complete 25,000 Steps", R.drawable.badge_steps_25000),
            new BaseGoal("STEPS_50000", "Complete 50,000 Steps", R.drawable.badge_steps_50000),
            new BaseGoal("STEPS_100000", "Complete 100,000 Steps", R.drawable.badge_steps_100000)
    );

    public Goals(Context context, AvatarModel avatar) {
        this.avatar = avatar;
        this.challengeManager = new ChallengeManager();

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

    public static void updateGoalProgress(Context context, String goalId) {
        AvatarModel avatar = AvatarManager.loadAvatarOffline(context);
        if (avatar == null) return;

        GoalState state = avatar.getGoalProgress().get(goalId);
        if (state != null && state != GoalState.CLAIMED) {
            avatar.setGoalState(goalId, GoalState.COMPLETED);
            AvatarManager.saveAvatarOffline(context, avatar);
            AvatarManager.saveAvatarOnline(avatar);
        }
    }




    /**
     * Initializes all default goals (base + challenge-linked).
     */
    private void initDefaultGoals() {
        // Base goals
        for (BaseGoal goal : BASE_GOALS) {
            if (!avatar.getGoalProgress().containsKey(goal.getId())) {
                avatar.setGoalState(goal.getId(), GoalState.PENDING);
            }
        }


        // Challenge-linked goals
        for (ChallengeModel challenge : challengeManager.getDefaultChallenges()) {
            String goalId = challenge.getLinkedGoalId();
            if (goalId != null && !goalId.isEmpty()) {
                if (!avatar.getGoalProgress().containsKey(goalId)) {
                    avatar.setGoalState(goalId, GoalState.PENDING);
                }
            }
        }
    }

    /**
     * Displays all goals: base + challenge-based, with proper separation and hiding completed items.
     */
    private void populateGoals(Context context) {
        goalsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        updateChallengeGoalsProgress();

        // Add Base Goals section header
        addSectionHeader(inflater, "📋 Base Goals");

        // Display base goals first (only pending and completed, not claimed)
        for (BaseGoal baseGoal : BASE_GOALS) {
            String goalId = baseGoal.getId();
            GoalState state = avatar.getGoalProgress().get(goalId);
            
            // Skip if not set or if claimed (hide claimed goals)
            if (state == null || state == GoalState.CLAIMED) continue;
            
            addGoalItem(inflater, goalId, state, baseGoal.getDescription(), false);
        }

        // Add Challenges section header
        addSectionHeader(inflater, "🏆 Challenges");

        // Display challenge goals (only pending and completed, not claimed)
        for (ChallengeModel challenge : challengeManager.getDefaultChallenges()) {
            String goalId = challenge.getLinkedGoalId();
            if (goalId == null || goalId.isEmpty()) continue;
            
            GoalState state = avatar.getGoalProgress().get(goalId);
            
            // Skip if not set or if claimed (hide claimed goals)
            if (state == null || state == GoalState.CLAIMED) continue;
            
            addGoalItem(inflater, goalId, state, "🏆 " + challenge.getObjective(), true);
        }
    }

    private void addSectionHeader(LayoutInflater inflater, String title) {
        TextView header = new TextView(inflater.getContext());
        header.setText(title);
        header.setTextSize(16);
        header.setTextColor(android.graphics.Color.parseColor("#2196F3"));
        header.setPadding(16, 24, 16, 8);
        goalsList.addView(header);
    }

    private void addGoalItem(LayoutInflater inflater, String goalId, GoalState state, String description, boolean isChallenge) {
        View goalItem = inflater.inflate(R.layout.goal_items, goalsList, false);
        TextView goalDescription = goalItem.findViewById(R.id.goal_description);
        Button claimButton = goalItem.findViewById(R.id.claim_button);

        goalDescription.setText(description);

        claimButton.setEnabled(state == GoalState.COMPLETED);
        claimButton.setText(state == GoalState.CLAIMED ? "Claimed" : "Claim");

        SoundManager.setOnClickListenerWithSound(claimButton, v -> {
            if (state != GoalState.COMPLETED) return;

            // Prevent duplicate rewards
            if (state == GoalState.CLAIMED) return;

            // Find the linked challenge (if any)
            ChallengeModel linkedChallenge = challengeManager.getChallengeByGoalId(goalId);

            if (linkedChallenge != null) {
                avatar.addCoins(linkedChallenge.getRewardCoins());
                avatar.addAvatarBadge(linkedChallenge.getRewardBadge());
            } else {
                // Base goal: find its badge
                int badgeResId = R.drawable.lock;
                for (BaseGoal g : BASE_GOALS) {
                    if (g.getId().equals(goalId)) {
                        badgeResId = g.getBadgeDrawableResId();
                        break;
                    }
                }
                avatar.addCoins(50);             // default coins
                avatar.addAvatarBadge(badgeResId); // reward badge drawable
            }

            avatar.setGoalState(goalId, GoalState.CLAIMED);
            AvatarManager.saveAvatarOffline(inflater.getContext(), avatar);
            AvatarManager.saveAvatarOnline(avatar);
            Toast.makeText(inflater.getContext(), "Claimed reward for: " + description, Toast.LENGTH_SHORT).show();
            populateGoals(inflater.getContext());
        });

        goalsList.addView(goalItem);
    }

    private void updateChallengeGoalsProgress() {
        for (ChallengeModel challenge : challengeManager.getDefaultChallenges()) {
            String goalId = challenge.getLinkedGoalId();
            if (goalId == null || goalId.isEmpty()) continue;

            GoalState currentState = avatar.getGoalProgress().get(goalId);

            // Skip if already claimed
            if (currentState == GoalState.CLAIMED) continue;

            // Check if challenge is completed
            if (challenge.isCompletedByAvatar(avatar)) {
                avatar.setGoalState(goalId, GoalState.COMPLETED);
            } else {
                // Optional: keep as pending or update dynamically
                if (currentState == null) {
                    avatar.setGoalState(goalId, GoalState.PENDING);
                }
            }
        }
    }




    /**
     * Returns readable goal description from base or challenge list.
     */
    private String getGoalDescription(String goalId) {
        // Base goals
        for (BaseGoal g : BASE_GOALS) {
            if (g.getId().equals(goalId)) return g.getDescription();
        }

        // Challenge goals
        ChallengeModel challenge = challengeManager.getChallengeByGoalId(goalId);
        if (challenge != null) {
            return "🏆 " + challenge.getObjective();
        }

        return goalId;
    }


    public void show() {
        dialog.show();
    }
}
