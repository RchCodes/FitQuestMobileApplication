package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AvatarOverviewActivity extends BaseActivity {

    private AvatarModel avatar;
    private AvatarDisplayManager avatarHelper;

    // --- UI References ---
    private TextView tvName, tvLevel, tvClass, tvHP, tvAP, expTextOverlay;
    private ProgressBar expBar;

    // physique
    private TextView txtArms, txtLegs, txtChest, txtBack;

    // attributes
    private TextView txtStrength, txtEndurance, txtAgility, txtFlexibility, txtStamina;

    // avatar layers
    private ImageView baseBodyLayer, outfitLayer, weaponLayer, hairOutlineLayer,
            hairFillLayer, eyesOutlineLayer, eyesFillLayer, noseLayer, lipsLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_overview);

        bindViews();
        setupAvatarHelper();
        loadAvatarWithOnlineFallback();
        setupButtons();
    }

    private void bindViews() {
        // Basic info
        tvName = findViewById(R.id.tvName);
        tvLevel = findViewById(R.id.tvLevel);
        tvClass = findViewById(R.id.tvClass);
        tvHP = findViewById(R.id.tvHP);
        tvAP = findViewById(R.id.tvAP);

        expBar = findViewById(R.id.exp_bar);
        expTextOverlay = findViewById(R.id.exp_text_overlay);

        // physique
        txtArms = findViewById(R.id.txt_arms);
        txtLegs = findViewById(R.id.txt_legs);
        txtChest = findViewById(R.id.txt_chest);
        txtBack = findViewById(R.id.txt_back);

        // attributes
        txtStrength = findViewById(R.id.txt_strength);
        txtEndurance = findViewById(R.id.txt_endurance);
        txtAgility = findViewById(R.id.txt_agility);
        txtFlexibility = findViewById(R.id.txt_flexibility);
        txtStamina = findViewById(R.id.txt_stamina);

        // avatar layers
        baseBodyLayer = findViewById(R.id.baseBodyLayer);
        outfitLayer = findViewById(R.id.outfitLayer);
        weaponLayer = findViewById(R.id.weaponLayer);
        hairOutlineLayer = findViewById(R.id.hairOutlineLayer);
        hairFillLayer = findViewById(R.id.hairFillLayer);
        eyesOutlineLayer = findViewById(R.id.eyesOutlineLayer);
        eyesFillLayer = findViewById(R.id.eyesFillLayer);
        noseLayer = findViewById(R.id.noseLayer);
        lipsLayer = findViewById(R.id.lipsLayer);
    }

    private void setupAvatarHelper() {
        avatarHelper = new AvatarDisplayManager(
                this,
                baseBodyLayer,
                outfitLayer,
                weaponLayer,
                hairOutlineLayer,
                hairFillLayer,
                eyesOutlineLayer,
                eyesFillLayer,
                noseLayer,
                lipsLayer
        );
    }

    private void setupButtons() {
        ImageButton backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadAvatarWithOnlineFallback() {
        AvatarModel offlineAvatar = AvatarManager.loadAvatarOffline(this);
        if (offlineAvatar != null) {
            avatar = offlineAvatar;
            avatarHelper.loadAvatar(avatar);
            loadProfileInfo();
        }

        AvatarManager.loadAvatarOnline(new AvatarManager.AvatarLoadCallback() {
            @Override
            public void onLoaded(AvatarModel onlineAvatar) {
                runOnUiThread(() -> {
                    if (onlineAvatar != null) {
                        avatar = onlineAvatar;
                        avatarHelper.loadAvatar(avatar);
                        loadProfileInfo();
                        AvatarManager.saveAvatarOffline(AvatarOverviewActivity.this, avatar);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Log.e("AvatarOverview", "Failed online load: " + message);
                    if (offlineAvatar == null && avatar == null) {
                        Toast.makeText(AvatarOverviewActivity.this, "No avatar found. Redirecting...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AvatarOverviewActivity.this, AvatarCreationActivity.class));
                        finish();
                    }
                });
            }
        });
    }

    private void loadProfileInfo() {
        if (avatar == null) return;

        // Basic Info
        tvName.setText(avatar.getUsername());
        tvLevel.setText("Lv. " + avatar.getLevel());
        tvClass.setText(avatar.getPlayerClass());
        //tvHP.setText("HP: " + avatar.getHp());
        //tvAP.setText("AP: " + avatar.getAp());

        // Exp handling
        int currentLevel = avatar.getLevel();
        int maxLevel = LevelProgression.getMaxLevel();

        if (currentLevel >= maxLevel) {
            expBar.setMax(1);
            expBar.setProgress(1);
            expTextOverlay.setText("MAX");
        } else {
            int prevLevelXp = currentLevel > 1 ? LevelProgression.getMaxXpForLevel(currentLevel - 1) : 0;
            int currentLevelMaxXp = LevelProgression.getMaxXpForLevel(currentLevel);
            int xpInLevel = Math.max(avatar.getXp() - prevLevelXp, 0);
            int xpNeeded = Math.max(currentLevelMaxXp - prevLevelXp, 1);

            expBar.setMax(xpNeeded);
            expBar.setProgress(Math.min(xpInLevel, xpNeeded));
            expTextOverlay.setText(xpInLevel + "/" + xpNeeded);
        }

        // Physique
        txtArms.setText(String.valueOf(avatar.getArmPoints()));
        txtLegs.setText(String.valueOf(avatar.getLegPoints()));
        txtChest.setText(String.valueOf(avatar.getChestPoints()));
        txtBack.setText(String.valueOf(avatar.getBackPoints()));

        // Attributes
        txtStrength.setText(String.valueOf(avatar.getStrength()));
        txtEndurance.setText(String.valueOf(avatar.getEndurance()));
        txtAgility.setText(String.valueOf(avatar.getAgility()));
        txtFlexibility.setText(String.valueOf(avatar.getFlexibility()));
        txtStamina.setText(String.valueOf(avatar.getStamina()));
    }
}
