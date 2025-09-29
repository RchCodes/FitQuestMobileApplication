package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

public class Profile {

    private static final String PREF_NAME = "FitQuestPrefs";
    private static final String AVATAR_KEY = "avatar_data";

    private final Dialog dialog;
    private AvatarModel avatar; // class-level avatar

    public Profile(Context context) {
        // Inflate layout
        View popupView = LayoutInflater.from(context).inflate(R.layout.profile, null);

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Load offline avatar (assign to class-level variable)
        avatar = AvatarManager.loadAvatarOffline(context);
        if (avatar == null) avatar = new AvatarModel(); // fallback

        // Bind views
        ImageView profileImage = popupView.findViewById(R.id.profile_image);
        TextView usernameView = popupView.findViewById(R.id.username);
        TextView classView = popupView.findViewById(R.id.classView);
        TextView levelView = popupView.findViewById(R.id.level);
        ProgressBar expBar = popupView.findViewById(R.id.exp_bar);
        TextView expText = popupView.findViewById(R.id.progressText);
        ImageView rankIcon = popupView.findViewById(R.id.rank_icon);
        TextView rankName = popupView.findViewById(R.id.rank_name);

        Button bindButton = popupView.findViewById(R.id.bind_button);
        Button switchButton = popupView.findViewById(R.id.switch_button);
        ImageButton btnClose = popupView.findViewById(R.id.btn_close_profile);
        TextView playerIdView = popupView.findViewById(R.id.player_id);

        // Close button
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Display player ID
        playerIdView.setText("ID: " + (avatar.getPlayerId() != null ? avatar.getPlayerId() : "00000000"));

        // Set avatar info
        usernameView.setText(avatar.getUsername() != null ? avatar.getUsername() : "Player");
        classView.setText(avatar.getPlayerClass().substring(0, 1).toUpperCase() +
                avatar.getPlayerClass().substring(1));
        levelView.setText("LV. " + avatar.getLevel());

        // Set XP bar and text with MAX handling
        boolean isMaxLevel = avatar.getLevel() >= 30;
        int maxXp = isMaxLevel ? avatar.getXp() : avatar.getXpNeeded();
        expBar.setMax(maxXp);
        expBar.setProgress(Math.min(avatar.getXp(), maxXp));
        expText.setText(isMaxLevel ? "MAX" : avatar.getXp() + "/" + maxXp);

        // Determine rank icon and text
        @DrawableRes int rankRes;
        String rankText;
        int level = avatar.getLevel();

        if (level < 10) {
            rankRes = R.drawable.rank_novice;
            rankText = "Novice";
        } else if (level < 25) {
            rankRes = R.drawable.rank_warrior;
            rankText = "Warrior";
        } else if (level < 50) {
            rankRes = R.drawable.rank_elite;
            rankText = "Elite";
        } else {
            rankRes = R.drawable.rank_hero;
            rankText = "Hero";
        }

        rankIcon.setImageResource(rankRes);
        rankName.setText(rankText);

        // Buttons (implement functionality as needed)
        bindButton.setOnClickListener(v -> {
            // Binding logic here
        });

        switchButton.setOnClickListener(v -> {
            // Switching logic here

        });
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
