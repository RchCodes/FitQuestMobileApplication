package com.example.fitquest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

        // Check if user has an account
        checkUserAccount();
    }

    private void checkUserAccount() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, null);
        boolean avatarCreated = prefs.getBoolean(KEY_AVATAR_CREATED, false);

        if (username == null) {
            // No account exists, go to account creation
            Intent intent = new Intent(this, AccountCreation.class);
            startActivity(intent);
            finish(); // Close MainActivity
            return;
        }

        if (!avatarCreated) {
            // Account exists but no avatar, go to avatar creation
            Intent intent = new Intent(this, AvatarCreationActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity
            return;
        }

        // User has both account and avatar, setup the main UI
        setupMainUI();
    }

    private void setupMainUI() {
        // Populate header username and avatar image
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "Player");
        String gender = prefs.getString(KEY_GENDER, "male");

        TextView playerName = findViewById(R.id.player_name);
        ImageView userIcon = findViewById(R.id.user_icon);
        ImageView characterView = findViewById(R.id.character_view);

        if (playerName != null) {
            playerName.setText(username);
        }
        int genderDrawable = "female".equalsIgnoreCase(gender) ? R.drawable.female : R.drawable.male2;
        if (userIcon != null) {
            userIcon.setImageResource(genderDrawable);
        }
        if (characterView != null) {
            characterView.setImageResource(genderDrawable);
        }

        // Show popup UIs for right-side buttons
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

        // Show popup UIs for left-side major features
        findViewById(R.id.profile_section).setOnClickListener(v ->
                new Profile(MainActivity.this).show()
        );

        findViewById(R.id.stats_button).setOnClickListener(v ->
                new Stats(MainActivity.this).show()
        );

        findViewById(R.id.gear_button).setOnClickListener(v ->
                new Gear(MainActivity.this).show()
        );

        findViewById(R.id.arena_button).setOnClickListener(v ->
                new Arena(MainActivity.this).show()
        );

        // New: Open UserSettingsActivity to tweak user level/difficulty
        findViewById(R.id.user_settings_activity_button).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UserSettingsActivity.class))
        );

        // New: Reset account button for testing
        findViewById(R.id.reset_account_button).setOnClickListener(v -> {
            // Clear user data
            SharedPreferences p = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = p.edit();
            editor.clear();
            editor.apply();

            // Restart the app flow
            Intent intent = new Intent(this, AccountCreation.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
