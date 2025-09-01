package com.example.fitquest;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UserSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(this);
        title.setText("User Settings");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, padding);
        root.addView(title);

        TextView currentLevel = new TextView(this);
        currentLevel.setTextSize(18);
        updateLevelText(currentLevel);
        root.addView(currentLevel);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, padding, 0, padding);

        Button minus = new Button(this);
        minus.setText("-");
        minus.setOnClickListener(v -> {
            int level = User.getLevel(this);
            if (level > 1) {
                User.setLevel(this, level - 1);
                updateLevelText(currentLevel);
            }
        });
        row.addView(minus);

        Button plus = new Button(this);
        plus.setText("+");
        plus.setOnClickListener(v -> {
            int level = User.getLevel(this);
            if (level < 30) {
                User.setLevel(this, level + 1);
                updateLevelText(currentLevel);
            }
        });
        row.addView(plus);

        root.addView(row);

        TextView diff = new TextView(this);
        diff.setTextSize(16);
        diff.setPadding(0, 0, 0, padding);
        root.addView(diff);
        diff.post(() -> diff.setText("Difficulty: " + User.getDifficultyLevel(this)));

        Button close = new Button(this);
        close.setText("Close");
        close.setOnClickListener(v -> finish());
        root.addView(close);

        setContentView(root);
    }

    private void updateLevelText(TextView tv) {
        int level = User.getLevel(this);
        tv.setText("Level: " + level + " (" + User.getDifficultyLevel(this) + ")");
    }
}
