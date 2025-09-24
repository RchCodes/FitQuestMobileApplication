package com.example.fitquest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "FitQuestPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR_CREATED = "avatar_created";
    private static final String KEY_GENDER = "gender"; // "male" or "female"

    private ImageView baseBodyLayer, hairLayer, eyesLayer, noseLayer, lipsLayer;

    /**
     * Called when the activity is first created. Sets up the UI and listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize layers
        baseBodyLayer = findViewById(R.id.base_body_layer);
        hairLayer = findViewById(R.id.hair_layer);
        eyesLayer = findViewById(R.id.eyes_layer);
        noseLayer = findViewById(R.id.nose_layer);
        lipsLayer = findViewById(R.id.lips_layer);

        // Check account and avatar
        checkUserAccount();
    }

    private void checkUserAccount() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, null);
        boolean avatarCreated = prefs.getBoolean(KEY_AVATAR_CREATED, false);

        if (username == null) {
            startActivity(new Intent(this, AccountCreation.class));
            finish();
            return;
        }

        if (!avatarCreated) {
            startActivity(new Intent(this, AvatarCreationActivity.class));
            finish();
            return;
        }

        // Load main UI
        setupMainUI();
    }

    private void setupMainUI() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "Player");
        String gender = prefs.getString(KEY_GENDER, "male");

        TextView playerName = findViewById(R.id.player_name);
        if (playerName != null) playerName.setText(username);

        // Load avatar
        AvatarModel avatar = AvatarManager.loadAvatar(this);
        if (avatar != null) {
            // --- Base body ---
            int bodyRes;
            if (avatar.chosenClass != null) {
                if (avatar.isMale) {
                    switch (avatar.chosenClass) {
                        case "rogue": bodyRes = R.drawable.rogue_male; break;
                        case "tank": bodyRes = R.drawable.tank_male; break;
                        default: bodyRes = R.drawable.warrior_male; break;
                    }
                } else {
                    switch (avatar.chosenClass) {
                        case "rogue": bodyRes = R.drawable.rogue_female; break;
                        case "tank": bodyRes = R.drawable.tank_female; break;
                        default: bodyRes = R.drawable.warrior_female; break;
                    }
                }
            } else {
                bodyRes = avatar.isMale ? R.drawable.warrior_male : R.drawable.warrior_female;
            }
            baseBodyLayer.setImageResource(bodyRes);

            // --- Hair (outline + colorable fill) ---
            Bitmap hairBmp = AvatarRenderer.renderPart(this,
                    avatar.hairOutlineRes, avatar.hairFillRes, avatar.hairColor);
            if (hairBmp != null) {
                hairLayer.setImageBitmap(hairBmp);
            } else {
                hairLayer.setImageDrawable(null);
            }

            // --- Eyes (outline + colorable fill) ---
            Bitmap eyesBmp = AvatarRenderer.renderPart(this,
                    avatar.eyesOutlineRes, avatar.eyesFillRes, avatar.eyesColor);
            if (eyesBmp != null) {
                eyesLayer.setImageBitmap(eyesBmp);
            } else {
                eyesLayer.setImageDrawable(null);
            }

            // --- Nose ---
            if (avatar.noseRes != 0) {
                noseLayer.setImageResource(avatar.noseRes);
            } else {
                noseLayer.setImageDrawable(null);
            }

            // --- Lips ---
            if (avatar.lipsRes != 0) {
                lipsLayer.setImageResource(avatar.lipsRes);
                if (avatar.lipsColor != -1) {
                    lipsLayer.setColorFilter(
                            new android.graphics.PorterDuffColorFilter(
                                    avatar.lipsColor, android.graphics.PorterDuff.Mode.SRC_IN
                            )
                    );
                }
            } else {
                lipsLayer.setImageDrawable(null);
            }
        }

        // --- Popup UI button listeners (unchanged) ---
        findViewById(R.id.quest_button).setOnClickListener(v ->
                new Quest(MainActivity.this).show()
        );

        findViewById(R.id.goals_button).setOnClickListener(v ->
                new Goals(MainActivity.this).show()
        );

        findViewById(R.id.store_button).setOnClickListener(v ->
                new Store(MainActivity.this).show()
        );

        findViewById(R.id.friends_button).setOnClickListener(v ->
                new Friends(MainActivity.this).show()
        );

        findViewById(R.id.settings_button).setOnClickListener(v ->
                new Settings(MainActivity.this).show()
        );

        findViewById(R.id.challenge_button).setOnClickListener(v ->
                new Challenge(MainActivity.this).show()
        );

        findViewById(R.id.profile_section).setOnClickListener(v ->
                new Profile(MainActivity.this).show()
        );

        findViewById(R.id.stats_button).setOnClickListener(v ->
                new CharacterStats(MainActivity.this).show()
        );

        findViewById(R.id.gear_button).setOnClickListener(v ->
                new Gear(MainActivity.this).show()
        );

        findViewById(R.id.arena_button).setOnClickListener(v ->
                new Arena(MainActivity.this).show()
        );

        findViewById(R.id.user_settings_activity_button).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UserSettingsActivity.class))
        );

        findViewById(R.id.reset_account_button).setOnClickListener(v -> {
            SharedPreferences p = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = p.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(this, AccountCreation.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

}

