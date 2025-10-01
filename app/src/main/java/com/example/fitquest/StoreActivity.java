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
import java.util.List;

public class StoreActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        // Avatar + avatar preview helper (same approach used in MainActivity)
        setupAvatarHelper();
        loadAvatar();

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

        // Grid click -> show details + mark selected
        gridStore.setOnItemClickListener((parent, view, position, id) -> {
            GearModel gear = filteredGear.get(position);
            selectedGear = gear;
            showGearDetails(gear);
            storeAdapter.setSelectedPosition(position);
        });

        // Buy button
        btnBuy.setOnClickListener(v -> {
            if (selectedGear == null) {
                Toast.makeText(this, "Select an item first!", Toast.LENGTH_SHORT).show();
                return;
            }
            showPurchaseDialog(selectedGear);
        });

        // Back
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(StoreActivity.this, MainActivity.class));
            finish();
        });

        // Tabs
        tabAll.setOnClickListener(v -> filterAndRefresh("ALL"));
        tabWeapons.setOnClickListener(v -> filterAndRefresh("WEAPON"));
        tabArmor.setOnClickListener(v -> filterAndRefresh("ARMOR"));
        tabLeggings.setOnClickListener(v -> filterAndRefresh("PANTS"));
        tabBoots.setOnClickListener(v -> filterAndRefresh("BOOTS"));
        tabNecklace.setOnClickListener(v -> filterAndRefresh("ACCESSORY"));
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
            // hide already owned
            if (avatar != null && avatar.ownsGear(gear.getId())) continue;
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
                    // Save avatar
                    AvatarManager.saveAvatarOffline(StoreActivity.this, avatar);
                    // Refresh UI
                    refreshCoinsText();
                    filterAndRefresh(currentFilter);
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
}
