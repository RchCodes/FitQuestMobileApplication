package com.example.fitquest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

public class ArenaActivity extends BaseActivity {

    private static final String PREF_NAME = "FitQuestPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_LEVEL = "level";

    // Avatar helper
    private AvatarDisplayManager avatarHelper;

    // Profile info
    private TextView playerName, playerLevel;
    private ImageView rankIcon;
    private TextView rankLabel;
    private ImageButton backBtn, fightButton, leaderboardButton, battleHistoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arena);

        // Bind profile views
        playerName = findViewById(R.id.tvName);
        playerLevel = findViewById(R.id.tvLevel);
        rankIcon = findViewById(R.id.rank_icon);
        rankLabel = findViewById(R.id.rank_label);

        // Bind buttons
        backBtn = findViewById(R.id.back_button);
        fightButton = findViewById(R.id.start_combat);
        leaderboardButton = findViewById(R.id.leaderboards);
        battleHistoryButton = findViewById(R.id.battle_history);

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

        MusicManager.start(this);

        // Load avatar safely and redirect if none exists
        loadAvatarIfExists();

        // Load profile info into views
        loadProfileInfo();

        // Setup button listeners
        setupButtons();
    }

    /** Load avatar from offline storage and redirect if missing */
    private void loadAvatarIfExists() {
        AvatarModel avatar = AvatarManager.loadAvatarOffline(this);
        if (avatar != null) {
            avatarHelper.loadAvatar(avatar);

            // Set name and level
            playerName.setText(avatar.getUsername());
            playerLevel.setText("LV. " + avatar.getLevel());
        } else {
            // Redirect to avatar creation
            Intent intent = new Intent(this, AvatarCreationActivity.class); // this causes problem if the data is not available
            startActivity(intent);
            finish();
        }
    }

    /** Load profile info into UI elements */
    private void loadProfileInfo() {
        AvatarModel avatar = AvatarManager.loadAvatarOffline(this);
        if (avatar == null) return;

        // Set name & level
        playerName.setText(avatar.getUsername());
        playerLevel.setText("LV. " + avatar.getLevel());

        // Set rank (default)
        rankLabel.setText("CHAMPION");
        rankIcon.setImageResource(R.drawable.highest_rank);
    }

    /** Setup button listeners */
    private void setupButtons() {
        backBtn.setOnClickListener(v -> finish());

        leaderboardButton.setOnClickListener(v -> {
            LeaderboardDialog dialog = new LeaderboardDialog();
            dialog.show(getSupportFragmentManager(), "LeaderboardDialog");
        });

        battleHistoryButton.setOnClickListener(v -> {
            BattleHistoryDialog dialog = new BattleHistoryDialog(avatarHelper.getBattleHistory());
            dialog.show(getSupportFragmentManager(), "BattleHistoryDialog");
        });

        fightButton.setOnClickListener(v ->
                startActivity(
                        new Intent(this, ArenaCombatActivity.class)
                )
                //Toast.makeText(this, "Combat Started!", Toast.LENGTH_SHORT).show()
        );
    }

    /** Launch method */
    public static void start(Context context) {
        Intent intent = new Intent(context, ArenaActivity.class);
        context.startActivity(intent);
    }
}
