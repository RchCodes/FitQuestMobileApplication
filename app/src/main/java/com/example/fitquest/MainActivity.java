package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private AvatarDisplayManager avatarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Example: buttons (profile, settings, challenge, etc.)
        findViewById(R.id.profile_section).setOnClickListener(v -> {
            // Open profile activity
            startActivity(new Intent(this, Profile.class));
        });

        findViewById(R.id.settings_button).setOnClickListener(v -> {
            startActivity(new Intent(this, Settings.class));
        });

        findViewById(R.id.challenge_button).setOnClickListener(v -> {
            startActivity(new Intent(this, Challenge.class));
        });

        // Left buttons
        findViewById(R.id.stats_button).setOnClickListener(v -> {
            startActivity(new Intent(this, CharacterStats.class));
        });
        findViewById(R.id.gear_button).setOnClickListener(v -> {
            startActivity(new Intent(this, Gear.class));
        });
        findViewById(R.id.arena_button).setOnClickListener(v -> {
            startActivity(new Intent(this, Arena.class));
        });

        // Right buttons
        findViewById(R.id.quest_button).setOnClickListener(v -> {
            startActivity(new Intent(this, Quest.class));
        });
        findViewById(R.id.goals_button).setOnClickListener(v -> {
            startActivity(new Intent(this, Goals.class));
        });
        findViewById(R.id.store_button).setOnClickListener(v -> {
            startActivity(new Intent(this, Store.class));
        });
        findViewById(R.id.friends_button).setOnClickListener(v -> {
            startActivity(new Intent(this, Friends.class));
        });

        // Optional: refresh avatar display in case of changes (gear, outfit)
        avatarHelper.refreshDisplay();
    }

    // Public method to allow other activities to update the avatar layers
    public AvatarDisplayManager getAvatarHelper() {
        return avatarHelper;
    }
}
