package com.example.fitquest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

public class LevelSelectActivity extends BaseActivity {

    private GridLayout gridLevels;
    private ImageView btnBack;

    private static final int TOTAL_LEVELS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        gridLevels = findViewById(R.id.gridLevels);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Load player progress (example)
        SharedPreferences prefs = getSharedPreferences("PlayerPrefs", Context.MODE_PRIVATE);
        int playerLevel = prefs.getInt("playerLevel", 1); // Default: level 1

        generateLevelButtons(playerLevel);
    }

    private void generateLevelButtons(int playerLevel) {
        gridLevels.removeAllViews();

        for (int i = 1; i <= TOTAL_LEVELS; i++) {
            Button btnLevel = new Button(this);
            btnLevel.setText(String.valueOf(i));
            btnLevel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            btnLevel.setTypeface(ResourcesCompat.getFont(this, R.font.bungee_regular), Typeface.BOLD);
            btnLevel.setShadowLayer(4, 2, 3, Color.parseColor("#FAFAFA"));
            btnLevel.setPadding(20, 10, 20, 10);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(50);
            params.height = dpToPx(50);
            params.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
            btnLevel.setLayoutParams(params);

            if (i <= playerLevel) {
                // Unlocked
                btnLevel.setBackgroundResource(R.drawable.button_level_unlocked);
                btnLevel.setTextColor(Color.parseColor("#AA5A1E"));
                int level = i;
                btnLevel.setOnClickListener(v -> openChallenge(level));
            } else {
                // Locked
                btnLevel.setBackgroundResource(R.drawable.button_level_locked);
                btnLevel.setTextColor(Color.GRAY);
                btnLevel.setEnabled(false);
            }

            gridLevels.addView(btnLevel);
        }
    }

    private void openChallenge(int level) {
        List<EnemyModel> enemiesForLevel = getEnemiesForLevel(level);
        if (enemiesForLevel.isEmpty()) {
            Toast.makeText(this, "No enemies defined for this level", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ChallengeActivity.class);
        intent.putParcelableArrayListExtra("enemies", new ArrayList<>(enemiesForLevel));
        intent.putExtra("level", level);
        startActivity(intent);
    }

    private List<EnemyModel> getEnemiesForLevel(int level) {
        List<EnemyModel> enemies = new ArrayList<>();
        switch (level) {
            case 1:
                enemies.add(EnemyRepository.getEnemy("slime").spawn());
                break;
            case 2:
                enemies.add(EnemyRepository.getEnemy("slime").spawn());
                enemies.add(EnemyRepository.getEnemy("venopods").spawn());
                break;
            case 3:
                enemies.add(EnemyRepository.getEnemy("venopods").spawn());
                enemies.add(EnemyRepository.getEnemy("slime").spawn());
                break;
            case 4:
                enemies.add(EnemyRepository.getEnemy("flame_wolf").spawn());
                break;
            case 5:
                enemies.add(EnemyRepository.getEnemy("slime").spawn());
                enemies.add(EnemyRepository.getEnemy("flame_wolf").spawn());
                break;
            case 6:
                enemies.add(EnemyRepository.getEnemy("venopods").spawn());
                enemies.add(EnemyRepository.getEnemy("flame_wolf").spawn());
                break;
            case 7:
                enemies.add(EnemyRepository.getEnemy("flame_wolf").spawn());
                enemies.add(EnemyRepository.getEnemy("flame_wolf").spawn());
                break;
            case 8:
                enemies.add(EnemyRepository.getEnemy("slime_king").spawn());
                break;
            case 9:
                enemies.add(EnemyRepository.getEnemy("flame_wolf").spawn());
                enemies.add(EnemyRepository.getEnemy("slime_king").spawn());
                break;
            case 10:
                enemies.add(EnemyRepository.getEnemy("slime_king").spawn());
                enemies.add(EnemyRepository.getEnemy("flame_wolf").spawn());
                enemies.add(EnemyRepository.getEnemy("venopods").spawn());
                break;
            default:
                break;
        }
        return enemies;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
