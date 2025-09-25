package com.example.fitquest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private AvatarDisplayManager avatarHelper;

    // Profile info
    private ImageView userIcon;
    private TextView playerName, playerLevel, coins;
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

        // Load profile info from local storage or database
        loadProfileInfo();


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

        // Load avatar automatically
        loadAvatarIfExists();

        // Setup other buttons
        setupButtons();
    }

    /** Load avatar from offline storage and refresh display */
    private void loadAvatarIfExists() {
        AvatarModel avatar = AvatarManager.loadAvatarOffline(this);
        if (avatar != null) {
            avatarHelper.loadAvatar(avatar);
        }
    }

    private void setupButtons() {
        // Dialog-style buttons
        findViewById(R.id.profile_section).setOnClickListener(v -> new Profile(this).show());
        findViewById(R.id.settings_button).setOnClickListener(v -> new Settings(this).show());
        findViewById(R.id.store_button).setOnClickListener(v -> new Store(this).show());
        findViewById(R.id.quest_button).setOnClickListener(v -> new Quest(this).show());
        findViewById(R.id.goals_button).setOnClickListener(v -> new Goals(this).show());
        findViewById(R.id.gear_button).setOnClickListener(v -> new Gear(this).show());
        findViewById(R.id.friends_button).setOnClickListener(v -> new Friends(this).show());
        findViewById(R.id.arena_button).setOnClickListener(v -> new Arena(this).show());
        findViewById(R.id.challenge_button).setOnClickListener(v -> new Challenge(this).show());
        findViewById(R.id.stats_button).setOnClickListener(v -> new CharacterStats(this).show());

    }
    // Allow other activities to access the avatar helper
    public AvatarDisplayManager getAvatarHelper() {
        return avatarHelper;
    }

    private void loadProfileInfo() {
        // Example: load from SharedPreferences (offline)
        SharedPreferences prefs = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE);

        String username = prefs.getString("username", "Player");
        int level = prefs.getInt("level", 1);
        int coinCount = prefs.getInt("coins", 0);
        int exp = prefs.getInt("exp", 0);
        int expMax = prefs.getInt("expMax", 1000); // max for current level

        // Set the views
        playerName.setText(username);
        playerLevel.setText("LV. " + level);
        coins.setText(String.valueOf(coinCount));

        expBar.setMax(expMax);
        expBar.setProgress(exp);

    }



}
