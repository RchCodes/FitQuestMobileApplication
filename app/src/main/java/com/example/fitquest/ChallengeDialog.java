package com.example.fitquest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChallengeDialog {

    public static void show(Context context, ChallengeModel challenge, boolean alreadyCompleted, int challengeNumber) {
        if (context == null) return;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.challenge_dialog);
        dialog.setCancelable(true);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Views
        TextView tvNumber = dialog.findViewById(R.id.tvChallengeNumber);
        TextView tvTitle = dialog.findViewById(R.id.tvChallengeTitle);
        TextView tvTask = dialog.findViewById(R.id.tvChallengeTask);
        TextView tvTime = dialog.findViewById(R.id.tvChallengeTime);
        TextView tvReward = dialog.findViewById(R.id.tvChallengeReward);

        Button btnNotNow = dialog.findViewById(R.id.btnNotNow);
        Button btnDoIt = dialog.findViewById(R.id.btnDoIt);

        // Set Text
        tvNumber.setText("Challenge " + (challengeNumber + 1));
        tvTitle.setText(challenge.getName());
        tvTask.setText(challenge.getObjective());
        tvTime.setText("⏱ Time Limit: " + challenge.getTimeLimitSeconds());
        tvReward.setText("🏅 Reward: " + challenge.getRewardCoins() + " coins + badge");

        // Prevent multiple attempts
        if (alreadyCompleted) {
            btnDoIt.setEnabled(false);
            btnDoIt.setAlpha(0.6f);
            btnDoIt.setText("Completed");
        }

        // Button Events
        btnNotNow.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnDoIt.setOnClickListener(v -> {

            if (alreadyCompleted) {
                Toast.makeText(context, "You've already completed this challenge!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Launch FitnessChallengeActivity with the entire ChallengeModel (Serializable)
                Intent intent = new Intent(context, FitnessChallengeActivity.class);
                intent.putExtra("challengeId", challenge.getId());
                intent.putExtra("challenge", challenge); // ChallengeModel implements Serializable
                intent.putExtra("exercise", challenge.getExerciseType());
                intent.putExtra("objective", challenge.getObjective());
                intent.putExtra("timeLimit", challenge.getTimeLimitSeconds());
                intent.putExtra("strict", challenge.isStrictMode());
                intent.putExtra("target", challenge.getTargetReps());
                intent.putExtra("reward", challenge.getRewardCoins());
                intent.putExtra("badge", challenge.getRewardBadge());

                // If the provided context is not an Activity, we must set NEW_TASK flag
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                context.startActivity(intent);

            } catch (Exception ex) {
                // Fail gracefully
                Toast.makeText(context, "Failed to start challenge: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
        });

        dialog.show();
    }
}
