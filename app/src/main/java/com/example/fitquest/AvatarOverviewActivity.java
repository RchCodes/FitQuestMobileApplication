package com.example.fitquest;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class AvatarOverviewActivity extends BaseActivity {

    private AvatarModel avatar;
    private AvatarDisplayManager avatarHelper;

    // --- UI References ---
    private TextView tvName, tvLevel, tvClass, tvExp, tvAP, expTextOverlay;
    private ProgressBar expBar;

    // physique
    private TextView txtArms, txtLegs, txtChest, txtBack;

    // attributes
    private TextView txtStrength, txtEndurance, txtAgility, txtFlexibility, txtStamina;

    // avatar layers
    private ImageView baseBodyLayer, outfitLayer, weaponLayer, hairOutlineLayer,
            hairFillLayer, eyesOutlineLayer, eyesFillLayer, noseLayer, lipsLayer, skillLoadout;

    private ShapeableImageView skill1, skill2, skill3, skill4, skill5, passive1, passive2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_overview);

        bindViews();
        setupAvatarHelper();
        loadAvatarWithOnlineFallback();
        setupButtons();
        loadSkills();
    }

    private void bindViews() {
        // Basic info
        tvName = findViewById(R.id.tvName);
        tvLevel = findViewById(R.id.tvLevel);
        tvClass = findViewById(R.id.tvClass);
        tvExp = findViewById(R.id.tvExp);
        //tvAP = findViewById(R.id.tvAP);

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

        skill1 = findViewById(R.id.skill1);
        skill2 = findViewById(R.id.skill2);
        skill3 = findViewById(R.id.skill3);
        skill4 = findViewById(R.id.skill4);
        skill5 = findViewById(R.id.skill5);
        passive1 = findViewById(R.id.passive1);
        passive2 = findViewById(R.id.passive2);

        skillLoadout = findViewById(R.id.skillLoadout);

        skillLoadout.setOnClickListener(v -> {
            SkillLoadoutDialog dialog = new SkillLoadoutDialog(this, avatar);
            dialog.setOnDismissListener(d -> loadSkills()); // refresh after closing
            dialog.show();
        });


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
        // Use ProgressSyncManager for intelligent loading
        ProgressSyncManager.loadProgress(this, new ProgressSyncManager.AvatarLoadCallback() {
            @Override
            public void onLoaded(AvatarModel loadedAvatar) {
                runOnUiThread(() -> {
                    avatar = loadedAvatar;
                    avatarHelper.loadAvatar(avatar);
                    loadProfileInfo();
                    loadSkills();
                    
                    // Save progress using sync manager
                    ProgressSyncManager.saveProgress(AvatarOverviewActivity.this, avatar, false); // Save offline first
                    
                    // If online is available, also save online
                    if (isNetworkAvailable()) {
                        ProgressSyncManager.saveProgress(AvatarOverviewActivity.this, avatar, true);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Log.e("AvatarOverview", "Avatar load failed: " + message);
                    Toast.makeText(AvatarOverviewActivity.this, "No avatar found. Redirecting...", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AvatarOverviewActivity.this, AvatarCreationActivity.class));
                    finish();
                });
            }
        });
    }
    
    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
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

    private void loadSkills() {
        if (avatar == null) return;

        // Active
        List<SkillModel> active = avatar.getActiveSkills();
        ShapeableImageView[] activeSlots = { skill1, skill2, skill3, skill4, skill5 };

        SkillInfoPopup popup = new SkillInfoPopup();

        for (int i = 0; i < activeSlots.length; i++) {
            ShapeableImageView slot = activeSlots[i];
            if (i < active.size()) {
                SkillModel s = active.get(i);
                slot.setImageResource(s.getIconRes());
                slot.setAlpha(1f);
                slot.setOnClickListener(v -> popup.show(v, s));
            } else {
                slot.setImageResource(R.drawable.lock); // placeholder asset
                slot.setAlpha(0.35f);
                slot.setOnClickListener(null);
            }
        }

        // Passive
        List<PassiveSkill> passives = avatar.getPassiveSkills();
        ShapeableImageView[] passiveSlots = { passive1, passive2 };

        for (int i = 0; i < passiveSlots.length; i++) {
            ShapeableImageView slot = passiveSlots[i];
            if (i < passives.size()) {
                PassiveSkill p = passives.get(i);
                slot.setImageResource(p.getIconResId());
                slot.setAlpha(1f);
                slot.setOnClickListener(v -> popup.show(v, p));
            } else {
                slot.setImageResource(R.drawable.lock);
                slot.setAlpha(0.35f);
                slot.setOnClickListener(null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (avatar != null) {
            // Save progress using sync manager
            ProgressSyncManager.saveProgress(this, avatar, false); // Save offline
            
            // If online is available, also save online
            if (isNetworkAvailable()) {
                ProgressSyncManager.saveProgress(this, avatar, true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (avatar != null) {
            ProgressSyncManager.saveProgress(this, avatar, false);
            if (isNetworkAvailable()) {
                ProgressSyncManager.saveProgress(this, avatar, true);
            }
        }
    }
}
