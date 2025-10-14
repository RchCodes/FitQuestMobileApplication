package com.example.fitquest;

import android.content.Context;
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

import java.net.CookieHandler;
import java.util.List;

public class LevelSelectActivity extends BaseActivity {

    private GridLayout gridLevels;
    private ImageView btnBack;
    private static final int TOTAL_CHALLENGES = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        gridLevels = findViewById(R.id.gridLevels);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Initialize Challenge Data
        ChallengeManager.init(this);

        generateChallengeButtons();
    }

    private void generateChallengeButtons() {
        gridLevels.removeAllViews();

        List<ChallengeModel> challenges = ChallengeManager.getAll(this);

        if (challenges.isEmpty()) {
            Toast.makeText(this, "No challenges available", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < TOTAL_CHALLENGES && i < challenges.size(); i++) {
            ChallengeModel challenge = challenges.get(i);

            Button btnChallenge = new Button(this);
            btnChallenge.setText(String.valueOf(i + 1));
            btnChallenge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
            btnChallenge.setTypeface(ResourcesCompat.getFont(this, R.font.bungee_regular), Typeface.BOLD);
            btnChallenge.setShadowLayer(4, 2, 3, Color.parseColor("#FAFAFA"));
            btnChallenge.setPadding(20, 10, 20, 10);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(75);
            params.height = dpToPx(75);
            params.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
            btnChallenge.setLayoutParams(params);

            if (!challenge.isCompleted()) {
                // Unlocked and available
                btnChallenge.setBackgroundResource(R.drawable.button_level_unlocked);
                btnChallenge.setTextColor(Color.parseColor("#AA5A1E"));

                int finalI = i;
                btnChallenge.setOnClickListener(v ->
                        ChallengeDialog.show(
                                this,
                                challenge,
                                false, // or true if this challenge uses strict mode
                                finalI
                        )
                );
            } else {
                // Completed / locked after done
                btnChallenge.setBackgroundResource(R.drawable.button_level_locked);
                btnChallenge.setTextColor(Color.GRAY);
                btnChallenge.setEnabled(false);
            }

            gridLevels.addView(btnChallenge);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
