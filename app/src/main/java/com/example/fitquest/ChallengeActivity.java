package com.example.fitquest;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class ChallengeActivity extends AppCompatActivity {

    private ImageView imgPlayer, imgEnemy;
    private FrameLayout effectsLayer;
    private ProgressBar playerHpBar, enemyHpBar, playerActionBar, enemyActionBar;
    private TextView playerHpText, enemyHpText;
    private LinearLayout playerStatusEffects, enemyStatusEffects;
    private Button[] skillButtons = new Button[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge); // ← use your XML name here

        // --- Initialize Core Views ---
        //imgPlayer = findViewById(R.id.imgPlayer);
        imgEnemy = findViewById(R.id.imgEnemy);
        effectsLayer = findViewById(R.id.effectsLayer);

        playerHpBar = findViewById(R.id.player_hp_bar);
        enemyHpBar = findViewById(R.id.enemy_hp_bar);
        playerActionBar = findViewById(R.id.playerActionBar);
        enemyActionBar = findViewById(R.id.enemyActionBar);

        playerHpText = findViewById(R.id.player_hp_text_overlay);
        enemyHpText = findViewById(R.id.enemy_hp_text_overlay);

        playerStatusEffects = findViewById(R.id.playerStatusEffects);
        enemyStatusEffects = findViewById(R.id.enemyStatusEffects);

        skillButtons[0] = findViewById(R.id.skill1);
        skillButtons[1] = findViewById(R.id.skill2);
        skillButtons[2] = findViewById(R.id.skill3);
        skillButtons[3] = findViewById(R.id.skill4);
        skillButtons[4] = findViewById(R.id.skill5);

        // Example Skill Click Action
        for (Button skill : skillButtons) {
            skill.setOnClickListener(v -> performAttack());
        }
    }

    private void performAttack() {
        // Example: Animate damage effect on enemy
        showFloatingText(imgEnemy, "-35", 0xFFFF5555); // red text for damage
        //playLottieEffect(imgEnemy, R.raw.hit_impact); // optional hit animation
    }

    // --- FLOATING DAMAGE TEXT ---
    private void showFloatingText(View target, String text, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(18);
        tv.setShadowLayer(4, 2, 2, 0xAA000000);
        tv.setGravity(Gravity.CENTER);

        int[] loc = new int[2];
        target.getLocationOnScreen(loc);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = loc[0] + target.getWidth() / 2 - 30;
        params.topMargin = loc[1] - 60; // above target
        tv.setLayoutParams(params);

        effectsLayer.addView(tv);

        // Animate upward float + fade
        tv.animate()
                .translationYBy(-150)
                .alpha(0f)
                .setDuration(1000)
                .withEndAction(() -> effectsLayer.removeView(tv))
                .start();
    }

    // --- LOTTIE EFFECTS ---
    private void playLottieEffect(View target, int animationRes) {
        LottieAnimationView lottie = new LottieAnimationView(this);
        lottie.setAnimation(animationRes);
        lottie.playAnimation();

        int[] loc = new int[2];
        target.getLocationOnScreen(loc);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(200, 200);
        params.leftMargin = loc[0] + target.getWidth() / 2 - 100;
        params.topMargin = loc[1] - 50;
        lottie.setLayoutParams(params);

        effectsLayer.addView(lottie);

        lottie.addAnimatorListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                effectsLayer.removeView(lottie);
            }
        });
    }
}
