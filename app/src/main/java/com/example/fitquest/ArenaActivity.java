package com.example.fitquest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ArenaActivity extends BaseActivity {

    // Profile info
    private TextView playerName, playerLevel;
    private ImageView rankIcon;
    private TextView rankLabel;
    private ImageButton backBtn, fightButton;
    // private ImageButton leaderboardButton, battleHistoryButton; // Disabled features

    // Avatar helper
    private AvatarDisplayManager avatarHelper;
    private AvatarModel avatar;

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
        // leaderboardButton = findViewById(R.id.leaderboards); // Disabled feature
        // battleHistoryButton = findViewById(R.id.battle_history); // Disabled feature

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

        // Load avatar safely and initialize UI
        loadAvatar();

        // Setup button listeners
        setupButtons();
    }

    /** Load avatar and initialize profile views */
    private void loadAvatar() {
        avatar = AvatarManager.loadAvatarOffline(this);
        if (avatar == null) {
            // Redirect to avatar creation if missing
            startActivity(new Intent(this, AvatarCreationActivity.class));
            finish();
            return;
        }

        // Load avatar into helper
        avatarHelper.loadAvatar(avatar);

        // Set profile info
        playerName.setText(avatar.getUsername());
        playerLevel.setText("LV. " + avatar.getLevel());
        rankLabel.setText(avatar.getRankName());
        int drawableId = avatar.getRankDrawableRes();
        rankIcon.setImageResource(drawableId != 0 ? drawableId : R.drawable.block);
    }

    /** Setup button listeners */
    private void setupButtons() {
        SoundManager.setOnClickListenerWithSound(backBtn, v -> finish());

        // Leaderboard and battle history buttons are commented out and disabled
        // leaderboardButton and battleHistoryButton are not initialized

        SoundManager.setOnClickListenerWithSound(fightButton, v ->
                startActivity(new Intent(this, ArenaCombatActivity.class))
        );
    }

    /** Launch method */
    public static void start(Context context) {
        Intent intent = new Intent(context, ArenaActivity.class);
        context.startActivity(intent);
    }
}
