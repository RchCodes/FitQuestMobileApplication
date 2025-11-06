package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GearActivity - shows owned gear (no price) and allows equipping.
 */
public class GearActivity extends BaseActivity {

    private AvatarModel avatar;
    private AvatarDisplayManager avatarHelper;

    // UI
    private ImageView btnBack, btnSave;
    private Button  btnEquip;
    private GridView gridGear;
    private ImageView tabAll, tabWeapon, tabArmor, tabPants, tabBoots, tabAccessory;
    private TextView itemName, itemDesc, itemBoosts, itemSkill;

    // Data
    private List<GearModel> ownedGearList = new ArrayList<>();
    private List<GearModel> filteredGear = new ArrayList<>();
    private GearAdapter gearAdapter;
    private GearModel selectedGear;
    private GearType currentFilter = null; // null = ALL
    
    // Temporary equipping state
    private boolean isPreviewMode = false;
    private Map<String, String> originalEquippedGear = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gear);

        // --- avatar preview helper (same layering as Store/Main) ---
        setupAvatarHelper();
        loadAvatarIfExists();

        // --- bind UI ---
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        btnEquip = findViewById(R.id.btnEquip);

        gridGear = findViewById(R.id.gridGear);

        tabAll = findViewById(R.id.tabAll);
        tabWeapon = findViewById(R.id.tabWeapon);
        tabArmor = findViewById(R.id.tabArmor);
        tabPants = findViewById(R.id.tabPants);
        tabBoots = findViewById(R.id.tabBoots);
        tabAccessory = findViewById(R.id.tabAccessory);

        itemName = findViewById(R.id.itemName);
        itemDesc = findViewById(R.id.itemDesc);
        itemBoosts = findViewById(R.id.itemBoosts);
        itemSkill = findViewById(R.id.itemSkill);

        MusicManager.start(this);

        // --- load owned gear from avatar & repository ---
        loadOwnedGear();

        // --- adapter & grid ---
        filteredGear = new ArrayList<>(ownedGearList);
        gearAdapter = new GearAdapter(this, filteredGear, avatar, gear -> onGearClicked(gear));

        gridGear.setAdapter(gearAdapter);

        // --- handlers ---
        // Back button handler is set later with cancel functionality

        tabAll.setOnClickListener(v -> applyFilter(null));
        tabWeapon.setOnClickListener(v -> applyFilter(GearType.WEAPON));
        tabArmor.setOnClickListener(v -> applyFilter(GearType.ARMOR));
        tabPants.setOnClickListener(v -> applyFilter(GearType.PANTS));
        tabBoots.setOnClickListener(v -> applyFilter(GearType.BOOTS));
        tabAccessory.setOnClickListener(v -> applyFilter(GearType.ACCESSORY));

        btnEquip.setOnClickListener(v -> {
            try {
                if (selectedGear == null) {
                    Toast.makeText(this, "Select gear to equip.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (avatar == null) {
                    Toast.makeText(this, "Avatar not loaded.", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean currentlyEquipped = avatar.isEquipped(selectedGear);

                if (currentlyEquipped) {
                    // Unequip temporarily
                    avatar.tempUnequipGear(selectedGear.getType());
                    Toast.makeText(this, "Unequipped " + selectedGear.getName() + " (Preview)", Toast.LENGTH_SHORT).show();
                } else {
                    // Equip temporarily for preview
                    if (!isPreviewMode) {
                        // Save original state when entering preview mode
                        originalEquippedGear.clear();
                        originalEquippedGear.putAll(avatar.getEquippedGear());
                        isPreviewMode = true;
                    }
                    avatar.tempEquipGear(selectedGear.getType(), selectedGear.getId());
                    Toast.makeText(this, "Equipped " + selectedGear.getName() + " (Preview)", Toast.LENGTH_SHORT).show();
                }

                // Update avatar display with temporary gear and preview weapon sprites
                avatarHelper.loadAvatar(avatar);
                if (selectedGear.getType() == GearType.WEAPON) {
                    avatarHelper.previewGear(selectedGear);
                }
                gearAdapter.updateData(filteredGear); // refresh highlighting
                updateEquipButtonText();
            } catch (Exception e) {
                android.util.Log.e("GearActivity", "Error in equip/unequip", e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> {
            try {
                if (isPreviewMode) {
                    // Save the temporary changes permanently
                    avatar.checkOutfitCompletion();
                    
                    // Apply weapon sprite if a weapon is equipped
                    if (selectedGear != null && selectedGear.getType() == GearType.WEAPON) {
                        avatarHelper.autoEquipGear(selectedGear);
                    } else {
                        AvatarManager.saveAvatarOffline(this, avatar);
                        AvatarManager.saveAvatarOnline(avatar);
                    }
                    
                    Toast.makeText(this, "Gear saved permanently!", Toast.LENGTH_SHORT).show();
                    isPreviewMode = false;
                    originalEquippedGear.clear();
                } else {
                    // No changes to save
                    Toast.makeText(this, "No changes to save.", Toast.LENGTH_SHORT).show();
                }
                finish();
            } catch (Exception e) {
                android.util.Log.e("GearActivity", "Error saving gear", e);
                Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Add cancel functionality to back button
        btnBack.setOnClickListener(v -> {
            if (isPreviewMode) {
                // Revert to original state
                avatar.getEquippedGear().clear();
                avatar.getEquippedGear().putAll(originalEquippedGear);
                avatarHelper.loadAvatar(avatar);
                isPreviewMode = false;
                originalEquippedGear.clear();
                Toast.makeText(this, "Changes reverted.", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private void updateEquipButtonText() {
        try {
            if (selectedGear == null) {
                btnEquip.setText("Equip");
                btnEquip.setEnabled(false);
            } else if (avatar != null && avatar.isEquipped(selectedGear)) {
                btnEquip.setText("Unequip (Preview)");
                btnEquip.setEnabled(true);
            } else {
                btnEquip.setText("Equip (Preview)");
                btnEquip.setEnabled(true);
            }
        } catch (Exception e) {
            android.util.Log.e("GearActivity", "Error in updateEquipButtonText", e);
            btnEquip.setText("Error");
            btnEquip.setEnabled(false);
        }
    }

    // Call updateEquipButtonText() whenever selection changes

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

    private void loadAvatarIfExists() {
        avatar = AvatarManager.loadAvatarOffline(this);
        if (avatar != null) {
            avatarHelper.loadAvatar(avatar);
        } else {
            // If avatar is missing, send to creation
            startActivity(new Intent(this, AvatarCreationActivity.class));
            finish();
        }
    }

    private void loadOwnedGear() {
        ownedGearList.clear();
        if (avatar == null) {
            android.util.Log.d("GearActivity", "Avatar is null, cannot load owned gear");
            return;
        }

        Set<String> ownedIds = avatar.getOwnedGear();
        android.util.Log.d("GearActivity", "Loading owned gear, count: " + (ownedIds != null ? ownedIds.size() : 0));
        
        if (ownedIds == null || ownedIds.isEmpty()) {
            android.util.Log.d("GearActivity", "No owned gear found");
            return;
        }

        for (String id : ownedIds) {
            GearModel g = GearRepository.getGearById(id);
            if (g != null) {
                ownedGearList.add(g);
                android.util.Log.d("GearActivity", "Loaded gear: " + g.getName() + " (ID: " + id + ")");
            } else {
                android.util.Log.w("GearActivity", "Could not find gear with ID: " + id);
            }
        }
        
        android.util.Log.d("GearActivity", "Total owned gear loaded: " + ownedGearList.size());
    }

    private void applyFilter(GearType type) {
        currentFilter = type;
        filteredGear.clear();
        for (GearModel g : ownedGearList) {
            if (g == null) continue;
            if (type == null) {
                filteredGear.add(g);
            } else {
                if (g.getType() == type) filteredGear.add(g);
            }
        }
        // clear selection on filter change
        selectedGear = null;
        gearAdapter.setSelectedGear(null);
        gearAdapter.updateData(filteredGear);
        clearDetailsPane();
    }

    private void onGearClicked(GearModel gear) {
        try {
            selectedGear = gear;
            gearAdapter.setSelectedGear(gear);
            showDetails(gear);
            updateEquipButtonText();
        } catch (Exception e) {
            android.util.Log.e("GearActivity", "Error in onGearClicked", e);
            Toast.makeText(this, "Error selecting gear: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDetails(GearModel gear) {
        if (gear == null) {
            clearDetailsPane();
            return;
        }
        itemName.setText(gear.getName() + " (" + gear.getClassRestriction() + ")");
        itemDesc.setText(gear.getDesc() != null ? gear.getDesc() : "");
        itemBoosts.setText(gear.getBoostsString());
        itemSkill.setText(gear.getSkillString());
    }

    private void clearDetailsPane() {
        itemName.setText("");
        itemDesc.setText("");
        itemBoosts.setText("");
        itemSkill.setText("");
    }

    private void equipSelectedGear() {
        if (avatar == null || selectedGear == null) return;

        // Must own the item (guard)
        if (!avatar.ownsGear(selectedGear.getId())) {
            Toast.makeText(this, "You don't own this item.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Equip via existing AvatarModel method (one per GearType is enforced by the map)
        avatar.equipGear(selectedGear.getType(), selectedGear.getId());

        // Check outfit completion and other side effects
        avatar.checkOutfitCompletion();

        // Save and refresh avatar preview
        AvatarManager.saveAvatarOffline(this, avatar);
        avatarHelper.loadAvatar(avatar);

        Toast.makeText(this, "Equipped " + selectedGear.getName(), Toast.LENGTH_SHORT).show();

        // update UI (if you want to show equipped state somewhere)
        gearAdapter.notifyDataSetChanged();
    }

    public GearModel getSelectedGear() {
        return selectedGear;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh owned gear list in case purchases happened elsewhere
        loadAvatarIfExists();
        loadOwnedGear();
        applyFilter(currentFilter); // reapply same filter (or ALL if null)
        avatarHelper.loadAvatar(avatar);
    }
}
