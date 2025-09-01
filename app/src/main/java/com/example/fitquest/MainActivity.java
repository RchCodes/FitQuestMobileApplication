package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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
    }
}
