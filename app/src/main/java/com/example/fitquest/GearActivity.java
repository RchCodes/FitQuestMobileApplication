package com.example.fitquest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class GearActivity extends BaseActivity {

    private AvatarDisplayManager avatarHelper;
    private AvatarModel avatar;

    // UI
    private Button btnBack, btnSave;
    private GridView gridGear;
    private ImageButton tabAll, tabWeapon, tabArmor, tabPants, tabBoots, tabAccessory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gear); // your new XML

        // Bind buttons
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);

        // Bind tabs
        tabAll = findViewById(R.id.tabAll);
        tabWeapon = findViewById(R.id.tabWeapon);
        tabArmor = findViewById(R.id.tabArmor);
        tabPants = findViewById(R.id.tabPants);
        tabBoots = findViewById(R.id.tabBoots);
        tabAccessory = findViewById(R.id.tabAccessory);

        // Bind gear grid
        gridGear = findViewById(R.id.gridGear);

        // Initialize avatar helper (same as MainActivity)
        avatarHelper = new AvatarDisplayManager(
                this,
                findViewById(R.id.baseBodyLayer),
                findViewById(R.id.outfitLayer),
                findViewById(R.id.weaponLayer),
                findViewById(R.id.hairOutlineLayer),
                findViewById(R.id.hairFillLayer),
                findViewById(R.id.eyesOutlineLayer),
                findViewById(R.id.eyesFillLayer),
                findViewById(R.id.noseLayer),
                findViewById(R.id.lipsLayer)
        );

        // Load avatar
        loadAvatarIfExists();

        // Setup button listeners
        setupListeners();
    }

    /** Load avatar from offline storage */
    private void loadAvatarIfExists() {
        avatar = AvatarManager.loadAvatarOffline(this);
        if (avatar != null) {
            avatarHelper.loadAvatar(avatar);
        }
    }

    /** Setup back, save, and tab listeners */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (avatar != null) {
                AvatarManager.saveAvatarOffline(this, avatar);
            }
            finish();
        });

        // TODO: filtering logic for tabs
        tabAll.setOnClickListener(v -> filterGear(GearType.values()));
        tabWeapon.setOnClickListener(v -> filterGear(GearType.WEAPON));
        tabArmor.setOnClickListener(v -> filterGear(GearType.ARMOR));
        tabPants.setOnClickListener(v -> filterGear(GearType.PANTS));
        tabBoots.setOnClickListener(v -> filterGear(GearType.BOOTS));
        tabAccessory.setOnClickListener(v -> filterGear(GearType.ACCESSORY));
    }

    /** Filter gear in grid */
    private void filterGear(GearType... types) {
        // Wire this later to your GridView adapter
        // Example: gearAdapter.filter(types, avatar.getClassType());
    }

    /** Expose avatar + helper if needed */
    public AvatarDisplayManager getAvatarHelper() {
        return avatarHelper;
    }

    public AvatarModel getAvatar() {
        return avatar;
    }
}
