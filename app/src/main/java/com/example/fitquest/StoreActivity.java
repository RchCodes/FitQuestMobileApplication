package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreActivity extends BaseActivity {

    private AvatarModel avatar;
    private AvatarDisplayManager avatarHelper;

    private TextView txtCoins;
    private GridView gridStore;
    private ImageView btnBack, btnBuy, tabAll, tabWeapons, tabArmor, tabLeggings, tabBoots, tabNecklace;

    private List<GearModel> allGear = new ArrayList<>();
    private List<GearModel> filteredGear = new ArrayList<>();
    private StoreAdapter storeAdapter;

    private GearModel selectedGear;
    private String currentFilter = "ALL"; // "ALL", "WEAPON", "ARMOR", "PANTS", "BOOTS", "ACCESSORY"
    
    // Store temporary gear preview
    private Map<String, String> originalEquippedGear;
    private boolean isPreviewMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        // Avatar + avatar preview helper (same approach used in MainActivity)
        setupAvatarHelper();
        loadAvatar();
        MusicManager.start(this);

        // UI refs
        txtCoins = findViewById(R.id.txtCoins);
        gridStore = findViewById(R.id.gridStore);
        btnBack = findViewById(R.id.btnBack);
        btnBuy = findViewById(R.id.btnBuy);

        tabAll = findViewById(R.id.tabAll);
        tabWeapons = findViewById(R.id.tabWeapons);
        tabArmor = findViewById(R.id.tabArmor);
        tabLeggings = findViewById(R.id.tabLeggings);
        tabBoots = findViewById(R.id.tabBoots);
        tabNecklace = findViewById(R.id.tabNecklace);

        // Load master gear list (repository)
        allGear = GearRepository.getAllGear();

        // Prepare adapter & grid
        filteredGear = new ArrayList<>();
        storeAdapter = new StoreAdapter(this, filteredGear, avatar);
        gridStore.setAdapter(storeAdapter);

        // Initial UI
        refreshCoinsText();
        filterAndRefresh(currentFilter);

        // Grid click -> show details + mark selected + preview gear
        gridStore.setOnItemClickListener((parent, view, position, id) -> {
            SoundManager.playButtonClick();
            GearModel gear = filteredGear.get(position);
            selectedGear = gear;
            showGearDetails(gear);
            storeAdapter.setSelectedPosition(position);
            previewGearOnAvatar(gear);
        });

        // Buy button
        SoundManager.setOnClickListenerWithSound(btnBuy, v -> {
            if (selectedGear == null) {
                Toast.makeText(this, "Select an item first!", Toast.LENGTH_SHORT).show();
                return;
            }
            showPurchaseDialog(selectedGear);
        });

        // Back
        SoundManager.setOnClickListenerWithSound(btnBack, v -> {
            startActivity(new Intent(StoreActivity.this, MainActivity.class));
            finish();
        });

        // Tabs
        SoundManager.setOnClickListenerWithSound(tabAll, v -> {
            clearGearPreview();
            filterAndRefresh("ALL");
        });
        SoundManager.setOnClickListenerWithSound(tabWeapons, v -> {
            clearGearPreview();
            filterAndRefresh("WEAPON");
        });
        SoundManager.setOnClickListenerWithSound(tabArmor, v -> {
            clearGearPreview();
            filterAndRefresh("ARMOR");
        });
        SoundManager.setOnClickListenerWithSound(tabLeggings, v -> {
            clearGearPreview();
            filterAndRefresh("PANTS");
        });
        SoundManager.setOnClickListenerWithSound(tabBoots, v -> {
            clearGearPreview();
            filterAndRefresh("BOOTS");
        });
        SoundManager.setOnClickListenerWithSound(tabNecklace, v -> {
            clearGearPreview();
            filterAndRefresh("ACCESSORY");
        });
    }

    private void setupAvatarHelper() {
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
    }

    private void loadAvatar() {
        avatar = AvatarManager.loadAvatarOffline(this);
        if (avatar != null) {
            avatarHelper.loadAvatar(avatar);
        } else {
            // If no avatar, go back to creation (same behaviour as MainActivity)
            startActivity(new Intent(this, AvatarCreationActivity.class));
            finish();
        }
    }

    private void refreshCoinsText() {
        if (avatar != null && txtCoins != null) {
            txtCoins.setText(avatar.getFormattedCoins());
        }
    }

    private void filterAndRefresh(String type) {
        currentFilter = type;
        filterStoreGear(type);
        storeAdapter.updateData(filteredGear);
        selectedGear = null; // clear selection when switching tabs
        // clear details UI if you're displaying it; for brevity we leave that to showGearDetails when clicked
    }

    /**
     * Filter allGear into filteredGear by:
     *  - hiding items the avatar already owns,
     *  - enforcing classRestriction (UNIVERSAL or matches avatar class),
     *  - applying the type filter.
     */
    private void filterStoreGear(String type) {
        filteredGear.clear();
        if (allGear == null) return;
        String playerClass = avatar != null ? avatar.getPlayerClass() : "UNIVERSAL";
        for (GearModel gear : allGear) {
            if (gear == null) continue;
            // Don't hide already owned items - let user see what they own
            // if (avatar != null && avatar.ownsGear(gear.getId())) continue;
            // class restriction
            if (gear.getClassRestriction() != null &&
                    !gear.getClassRestriction().equalsIgnoreCase("UNIVERSAL") &&
                    avatar != null &&
                    !gear.getClassRestriction().equalsIgnoreCase(avatar.getPlayerClass())) {
                continue;
            }
            // type filter
            if (!"ALL".equalsIgnoreCase(type)) {
                if (!gear.getType().name().equalsIgnoreCase(type)) continue;
            }
            filteredGear.add(gear);
        }
    }

    private void showGearDetails(GearModel gear) {
        if (gear == null) return;
        TextView itemName = findViewById(R.id.itemName);
        TextView itemDesc = findViewById(R.id.itemDesc);
        TextView itemBoosts = findViewById(R.id.itemBoosts);
        TextView itemSkill = findViewById(R.id.itemSkill);

        itemName.setText(gear.getName() + " (" + gear.getClassRestriction() + ")");
        itemDesc.setText(gear.getDesc() != null ? gear.getDesc() : "");
        itemBoosts.setText(gear.getBoostsString());
        itemSkill.setText(gear.getSkillString());
    }

    private void showPurchaseDialog(GearModel gear) {
        if (gear == null || avatar == null) return;

        // Check if already owned
        if (avatar.ownsGear(gear.getId())) {
            Toast.makeText(this, "You already own this item!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check affordability first
        if (avatar.getCoins() < gear.getPrice()) {
            Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Purchase")
                .setMessage("Buy " + gear.getName() + " for " + formatCoins(gear.getPrice()) + " ?")
                .setPositiveButton("Buy", (dialog, which) -> {
                    // Deduct coins
                    avatar.setCoins(avatar.getCoins() - gear.getPrice());
                    // Add gear to avatar inventory
                    avatar.addGear(gear.getId());
                    
                    // Log before saving
                    android.util.Log.d("StoreActivity", "Before save - Gear purchased: " + gear.getName() + ", ID: " + gear.getId());
                    android.util.Log.d("StoreActivity", "Before save - Avatar owned gear count: " + avatar.getOwnedGear().size());
                    
                    // Save avatar both offline and online
                    AvatarManager.saveAvatarOffline(StoreActivity.this, avatar);
                    AvatarManager.saveAvatarOnline(avatar);
                    
                    // Log after saving
                    android.util.Log.d("StoreActivity", "After save - Avatar owned gear count: " + avatar.getOwnedGear().size());
                    
                    // Refresh UI
                    refreshCoinsText();
                    filterAndRefresh(currentFilter);
                    // Update store adapter
                    storeAdapter.notifyDataSetChanged();
                    Toast.makeText(StoreActivity.this, "Purchased " + gear.getName() + "!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Utility: format price for UI like 1.2K / 3M / 1B
    private static String formatCoins(long n) {
        if (n >= 1_000_000_000L) return (n / 1_000_000_000L) + "B";
        if (n >= 1_000_000L) return (n / 1_000_000L) + "M";
        if (n >= 1_000L) return (n / 1_000L) + "K";
        return String.valueOf(n);
    }
    
    /**
     * Preview gear on avatar temporarily without saving
     */
    private void previewGearOnAvatar(GearModel gear) {
        if (gear == null || avatar == null) return;
        
        // Save original equipped gear if not already in preview mode
        if (!isPreviewMode) {
            originalEquippedGear = new HashMap<>(avatar.getEquippedGear());
            isPreviewMode = true;
        }
        
        // Temporarily equip the selected gear
        avatar.equipGear(gear.getType(), gear.getId());
        
        // Update avatar display with temporary gear
        updateAvatarDisplayWithGear(gear);
    }
    
    /**
     * Clear gear preview and restore original equipped gear
     */
    private void clearGearPreview() {
        if (isPreviewMode && originalEquippedGear != null && avatar != null) {
            // Restore original equipped gear
            avatar.getEquippedGear().clear();
            avatar.getEquippedGear().putAll(originalEquippedGear);
            
            // Refresh avatar display
            avatarHelper.loadAvatar(avatar);
            
            isPreviewMode = false;
            originalEquippedGear = null;
        }
    }
    
    /**
     * Update avatar display with specific gear changes
     */
    private void updateAvatarDisplayWithGear(GearModel gear) {
        if (gear == null || avatar == null) return;
        
        // Update specific gear layer based on type
        switch (gear.getType()) {
            case WEAPON:
                updateWeaponDisplay(gear);
                break;
            case ARMOR:
            case PANTS:
            case BOOTS:
                updateOutfitDisplay();
                break;
            case ACCESSORY:
                // Accessories might not have visual representation
                break;
        }
    }
    
    /**
     * Update weapon display
     */
    private void updateWeaponDisplay(GearModel weapon) {
        if (weapon == null) return;
        
        String gender = avatar.getGender();
        int weaponSpriteRes;
        
        if ("male".equalsIgnoreCase(gender)) {
            weaponSpriteRes = weapon.getMaleSpriteRes();
        } else {
            weaponSpriteRes = weapon.getFemaleSpriteRes();
        }
        
        if (weaponSpriteRes != 0) {
            avatarHelper.getWeaponImageView().setImageResource(weaponSpriteRes);
        }
    }
    
    /**
     * Update outfit display based on equipped gear
     */
    private void updateOutfitDisplay() {
        // Check if we have a complete outfit set
        avatar.checkOutfitCompletion();
        
        // Refresh the outfit layer
        String outfitResName = avatar.getOutfit();
        if (outfitResName != null && !outfitResName.isEmpty()) {
            int resId = getDrawableByName(outfitResName);
            if (resId != 0) {
                avatarHelper.getOutfitImageView().setImageResource(resId);
            }
        }
    }
    
    /**
     * Get drawable resource ID by name
     */
    private int getDrawableByName(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
    
    @Override
    protected void onDestroy() {
        // Clear any preview when leaving store
        clearGearPreview();
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        clearGearPreview();
        super.onBackPressed();
    }
}
