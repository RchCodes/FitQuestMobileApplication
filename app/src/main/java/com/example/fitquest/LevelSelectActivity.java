package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LevelSelectActivity extends AppCompatActivity {

    private ImageView btnBack;
    private GridLayout gridLevels;
    private ImageButton btnPrev, btnNext;

    private Button btnLevel1, btnLevel2;
    private ImageButton btnLocked;

    private int currentPage = 1; // for multi-page level selection (future use)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        bindViews();
        setupListeners();
        updateLevelGrid();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        gridLevels = findViewById(R.id.gridLevels);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        // Example levels (you can expand this)
        btnLevel1 = findViewById(R.id.btnLevel1);
        btnLevel2 = findViewById(R.id.btnLevel2);
        btnLocked = findViewById(R.id.btnLocked);
    }

    private void setupListeners() {
        // 🔙 Back button
        btnBack.setOnClickListener(v -> finish());

        // 🟡 Unlocked levels
        //btnLevel1.setOnClickListener(v -> openLevel(1));
        //btnLevel2.setOnClickListener(v -> openLevel(2));

        btnLevel1.setOnClickListener(v -> startActivity(
                new Intent(this, ChallengeActivity.class)
                        .putExtra("LEVEL_NUMBER", 1)
                ));

        // 🔒 Locked level (show message)
        btnLocked.setOnClickListener(v ->
                Toast.makeText(this, "This level is locked!", Toast.LENGTH_SHORT).show()
        );

        // ⬅️➡️ Page navigation
        btnPrev.setOnClickListener(v -> changePage(-1));
        btnNext.setOnClickListener(v -> changePage(1));
    }

    private void updateLevelGrid() {
        // This method can later update which levels are unlocked per page.
        // For now, it just ensures visible layout consistency.
        // You can add logic like:
        // if (playerLevel >= 2) unlockLevel(btnLevel2);
    }

    private void openLevel(int levelNumber) {
        Toast.makeText(this, "Opening Level " + levelNumber, Toast.LENGTH_SHORT).show();

        // Example: start the challenge activity for this level
        Intent intent = new Intent(this, ChallengeActivity.class);
        intent.putExtra("LEVEL_NUMBER", levelNumber);
        startActivity(intent);
    }

    private void changePage(int direction) {
        // Future pagination support (if you have more than 12 levels)
        currentPage += direction;
        if (currentPage < 1) currentPage = 1;

        Toast.makeText(this, "Page " + currentPage, Toast.LENGTH_SHORT).show();
        // TODO: update visible buttons per page
    }

    // Optional helper
    private void unlockLevel(Button levelButton) {
        levelButton.setEnabled(true);
        levelButton.setBackgroundResource(R.drawable.button_level_unlocked);
    }

    private void lockLevel(ImageButton lockButton) {
        lockButton.setEnabled(false);
        lockButton.setBackgroundResource(R.drawable.button_level_locked);
    }
}
