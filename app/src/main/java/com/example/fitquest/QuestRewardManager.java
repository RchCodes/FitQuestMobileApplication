package com.example.fitquest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class QuestRewardManager {

    private static final String TAG = "QuestRewardManager";

    /**
     * Apply quest rewards directly to the AvatarModel
     * without using CharacterStats.
     * @param ctx Context
     * @param quest Quest to apply rewards for
     * @return true if avatar leveled up
     */
    public static boolean applyRewards(Context ctx, QuestModel quest) {
        AvatarModel avatar = AvatarManager.loadAvatarOffline(ctx);
        if (avatar == null) {
            Log.e(TAG, "No avatar found! Cannot apply rewards.");
            return false;
        }

        QuestReward reward = quest.getReward();
        if (reward == null) {
            Log.e(TAG, "Quest has no reward!");
            return false;
        }

        // Apply coins and XP
        avatar.setCoins(avatar.getCoins() + reward.getCoins());
        boolean leveledUp = avatar.addXp(reward.getXp());

        // Apply allocated body-part points directly
        avatar.addArmPoints(reward.getArmPoints());
        avatar.addLegPoints(reward.getLegPoints());
        avatar.addChestPoints(reward.getChestPoints());
        avatar.addBackPoints(reward.getBackPoints());

        // Add free points for user allocation
        avatar.addFreePhysiquePoints(reward.getPhysiquePoints());
        avatar.addFreeAttributePoints(reward.getAttributePoints());

        // Save avatar immediately
        AvatarManager.saveAvatarOffline(ctx, avatar);
        AvatarManager.saveAvatarOnline(avatar);

        // Mark quest as completed
        quest.complete();

        // Update quest storage
        List<QuestModel> quests = QuestStorage.loadQuestsOffline(ctx);
        for (int i = 0; i < quests.size(); i++) {
            if (quests.get(i).getId().equals(quest.getId())) {
                quests.set(i, quest);
                break;
            }
        }
        QuestStorage.saveQuestsOffline(ctx, quests);
        QuestStorage.saveQuestsOnline(quests);

        Log.d(TAG, "Rewards applied: +" + reward.getCoins() + " coins, +" +
                reward.getXp() + " XP, +" + reward.getArmPoints() + " Arms, +" +
                reward.getLegPoints() + " Legs, +" + reward.getChestPoints() + " Chest, +" +
                reward.getBackPoints() + " Back, +" + reward.getPhysiquePoints() +
                " Free Physique Points, +" + reward.getAttributePoints() + " Free Attribute Points");

        return leveledUp;
    }

    /** Show rewards summary popup */
    public static void showRewardPopup(Context ctx, QuestReward reward) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_rewards, null);

        TextView tvSummary = view.findViewById(R.id.tvRewardSummary);
        Button btnOk = view.findViewById(R.id.btnOk);

        StringBuilder summary = new StringBuilder();
        if (reward.getXp() > 0) summary.append("+ ").append(reward.getXp()).append(" XP\n");
        if (reward.getCoins() > 0) summary.append("+ ").append(reward.getCoins()).append(" Coins\n");
        if (reward.getArmPoints() > 0) summary.append("+ ").append(reward.getArmPoints()).append(" Arm Point(s)\n");
        if (reward.getLegPoints() > 0) summary.append("+ ").append(reward.getLegPoints()).append(" Leg Point(s)\n");
        if (reward.getChestPoints() > 0) summary.append("+ ").append(reward.getChestPoints()).append(" Chest Point(s)\n");
        if (reward.getBackPoints() > 0) summary.append("+ ").append(reward.getBackPoints()).append(" Back Point(s)\n");
        if (reward.getPhysiquePoints() > 0) summary.append("+ ").append(reward.getPhysiquePoints()).append(" Free Physique Point(s)\n");
        if (reward.getAttributePoints() > 0) summary.append("+ ").append(reward.getAttributePoints()).append(" Free Attribute Point(s)\n");

        tvSummary.setText(summary.toString().trim());

        AlertDialog dialog = builder.setView(view).create();
        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /** Show level-up popup */
    public static void showLevelUpPopup(Context ctx, int newLevel, int newRank) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_levelup, null);

        TextView tvTitle = view.findViewById(R.id.tvLevelUpTitle);
        TextView tvDetails = view.findViewById(R.id.tvLevelUpDetails);
        Button btnOk = view.findViewById(R.id.btnOk);

        tvTitle.setText("🎉 Level Up! 🎉");
        tvDetails.setText("You reached Level " + newLevel +
                (newRank > 0 ? "\nNew Rank: " + rankName(newRank) : ""));

        AlertDialog dialog = builder.setView(view).create();
        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /** Convert rank number to string */
    private static String rankName(int rank) {
        switch (rank) {
            case 1: return "Warrior";
            case 2: return "Elite";
            case 3: return "Hero";
            default: return "Novice";
        }
    }
}
