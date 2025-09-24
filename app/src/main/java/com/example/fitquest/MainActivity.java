package com.example.fitquest;

import android.os.Bundle;
import android.view.MotionEvent;
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

        // ===== Apply shrink effect to all your buttons =====
        int[] buttonIds = {
                R.id.quest_button,
                R.id.goals_button,
                R.id.store_button,
                R.id.friends_button,
                R.id.settings_button,
                R.id.challenge_button,
                R.id.profile_section,
                R.id.stats_button,
                R.id.gear_button,
                R.id.arena_button
        };

        for (int id : buttonIds) {
            View btn = findViewById(id);
            if (btn != null) {
                addShrinkEffect(btn);
            }
        }

        // ===== Show popup UIs for right-side buttons =====
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

        // ===== Show popup UIs for left-side major features =====
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
    }

    // ===== Reusable shrink effect method =====
    private void addShrinkEffect(final View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false; // keep normal click behavior
        });
    }
}
