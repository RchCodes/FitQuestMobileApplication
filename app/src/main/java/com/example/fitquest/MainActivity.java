package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements QuestManager.QuestProgressListener {

    private AvatarDisplayManager avatarHelper;
    private AvatarModel avatar; // class-level avatar variable

    // Profile info views
    private ImageView userIcon;
    private TextView playerName, playerLevel, coins, expText;
    private ProgressBar expBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind profile views
        userIcon = findViewById(R.id.user_icon);
        playerName = findViewById(R.id.player_name);
        playerLevel = findViewById(R.id.player_level);
        coins = findViewById(R.id.coins);
        expBar = findViewById(R.id.exp_bar);
        expText = findViewById(R.id.exp_text); // add TextView for XP display

        // Initialize avatar helper with all layers
        avatarHelper = new AvatarDisplayManager(
                this,
                findViewById(R.id.baseBodyLayer),
                findViewById(R.id.outfitLayer),
                findViewById(R.id.weaponLayer),
                findViewById(R.id.hairOutlineLayer),
                findViewById(R.id.hairFillLayer),
                findViewById(R.id.eyesOutlineLayer),
                findViewById(R.id.eyesFillLayer),
                findViewById(R.id.noseLayer),
                findViewById(R.id.lipsLayer)
        );

        // Load avatar safely and redirect if none exists
        loadAvatarIfExists();

        // Load profile info into views
        loadProfileInfo();

        // Setup button listeners
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register as listener when activity resumes
        QuestManager.setQuestProgressListener(this);
        // Refresh profile in case anything changed while paused
        if (avatar != null) {
            avatar.setProfileChangeListener(updatedAvatar -> runOnUiThread(this::loadProfileInfo));
        }

        loadProfileInfo();
        refreshProfile();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear listener to avoid memory leaks
        QuestManager.setQuestProgressListener(null);
        if (avatar != null) {
            avatar.setProfileChangeListener(null); // avoid leaks
        }
    }

    @Override
    public void onAvatarUpdated(AvatarModel updatedAvatar) {
        if (updatedAvatar == null) return;

        // Update the class-level avatar
        this.avatar = updatedAvatar;

        // Reload avatar layers visually
        runOnUiThread(() -> {
            avatarHelper.loadAvatar(avatar);
            loadProfileInfo();
        });
    }


    @Override
    public void onQuestProgressUpdated(QuestModel quest) {
        // Optionally refresh UI for quest progress only
    }

    @Override
    public void onQuestCompleted(QuestModel quest, boolean leveledUp) {
        // Refresh profile info immediately
        avatar = AvatarManager.loadAvatarOffline(this); // reload updated avatar
        refreshProfile();
    }

    /** Load avatar from offline storage and redirect if missing */
    private void loadAvatarIfExists() {
        avatar = AvatarManager.loadAvatarOffline(this); // assign to class variable
        if (avatar != null) {
            avatarHelper.loadAvatar(avatar);
        } else {
            // No avatar exists → go to AvatarCreationActivity
            Intent intent = new Intent(this, AvatarCreationActivity.class);
            startActivity(intent);
            finish(); // prevent MainActivity from continuing
        }
    }

    /** Load profile info into UI elements using AvatarModel directly */
    private void loadProfileInfo() {
        if (avatar == null) return;

        // Update name and level
        playerName.setText(avatar.getUsername());
        playerLevel.setText("LV. " + avatar.getLevel());
        coins.setText(String.valueOf(avatar.getCoins()));

        // Update experience bar and text with MAX handling
        boolean isMaxLevel = avatar.getLevel() >= 30;
        int maxXp = isMaxLevel ? avatar.getXp() : avatar.getXpNeeded();

        expBar.setMax(maxXp);
        expBar.setProgress(Math.min(avatar.getXp(), maxXp));
        expText.setText(isMaxLevel ? "MAX" : avatar.getXp() + "/" + maxXp);
    }

    /** Refresh UI after gaining XP or coins */
    public void refreshProfile() {
        loadProfileInfo();
    }

    /** Set up onClick listeners for menu buttons */
    private void setupButtons() {
        findViewById(R.id.profile_section).setOnClickListener(v -> new Profile(this).show());
        findViewById(R.id.settings_button).setOnClickListener(v -> new Settings(this).show());
        findViewById(R.id.store_button).setOnClickListener(v -> new Store(this).show());
        findViewById(R.id.quest_button).setOnClickListener(v -> new Quest(this).show());
        findViewById(R.id.goals_button).setOnClickListener(v -> new Goals(this).show());
        findViewById(R.id.gear_button).setOnClickListener(v -> new Gear(this).show());
        findViewById(R.id.friends_button).setOnClickListener(v -> new Friends(this).show());
        findViewById(R.id.arena_button).setOnClickListener(v -> new Arena(this).show());
        findViewById(R.id.challenge_button).setOnClickListener(v -> new Challenge(this).show());
        findViewById(R.id.stats_button).setOnClickListener(v -> new CharacterStats(this,avatar).show());
    }

    /** Allow other activities to access the avatar helper */
    public AvatarDisplayManager getAvatarHelper() {
        return avatarHelper;
    }

    /** Allow other activities to access the avatar model */
    public AvatarModel getAvatar() {
        return avatar;
    }
}
