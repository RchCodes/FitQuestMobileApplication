package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreActivity extends AppCompatActivity {

    private GridView gridStore;
    private TextView itemName, itemDesc, itemBoosts, itemSkill, txtCoins;
    private ImageView btnBuy, btnBack;

    private List<GearModel> storeItems;
    private int playerCoins = 999000000; // example coins

    // Avatar handling
    private AvatarModel avatar;
    private AvatarDisplayManager avatarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        // ==== Avatar ====
        setupAvatarHelper();
        loadAvatar();

        // ==== UI refs ====
        gridStore = findViewById(R.id.gridStore);
        itemName = findViewById(R.id.itemName);
        itemDesc = findViewById(R.id.itemDesc);
        itemBoosts = findViewById(R.id.itemBoosts);
        itemSkill = findViewById(R.id.itemSkill);
        txtCoins = findViewById(R.id.txtCoins);
        btnBuy = findViewById(R.id.btnBuy);
        btnBack = findViewById(R.id.btnBack);

        txtCoins.setText(playerCoins / 1000000 + "M");

        // build store items
        buildStoreItems();

        // adapter for grid
        GearAdapter adapter = new GearAdapter(this, storeItems);
        gridStore.setAdapter(adapter);

        // click handler for grid
        gridStore.setOnItemClickListener((parent, view, position, id) -> {
            GearModel gear = storeItems.get(position);
            showGearDetails(gear);
        });

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(StoreActivity.this, MainActivity.class));
        });

        btnBuy.setOnClickListener(v -> {
            // TODO: handle purchase logic
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
            startActivity(new Intent(this, AvatarCreationActivity.class));
            finish();
        }
    }

    private void showGearDetails(GearModel gear) {
        // Set name with class
        itemName.setText(gear.name + " (" + gear.allowedClass.name() + ")");

        // Description (if you want per-item descriptions, add a field to GearModel)
        if (gear.description != null) {
            itemDesc.setText(gear.description);
        } else {
            itemDesc.setText("A fine piece of gear.");
        }

        // Build stat boosts
        StringBuilder boostText = new StringBuilder("Boosts:\n");
        if (gear.statBoosts != null && !gear.statBoosts.isEmpty()) {
            for (Map.Entry<String, Float> entry : gear.statBoosts.entrySet()) {
                boostText.append("+")
                        .append(entry.getValue())
                        .append("% ")
                        .append(entry.getKey())
                        .append("\n");
            }
        } else {
            boostText.append("None");
        }
        itemBoosts.setText(boostText.toString().trim());

        // Build skill augments
        StringBuilder skillText = new StringBuilder("Skill Effect:\n");
        if (gear.skillAugments != null && !gear.skillAugments.isEmpty()) {
            for (Effect effect : gear.skillAugments) {
                skillText.append(effect.getName())
                        .append(" ")
                        .append(effect.getValue())
                        .append("\n");
            }
        } else {
            skillText.append("None");
        }
        itemSkill.setText(skillText.toString().trim());
    }


    private void buildStoreItems() {
        storeItems = new ArrayList<>();

        // Example 1: Phantom Leggings
        Map<String, Float> boosts1 = new HashMap<>();
        boosts1.put("AGI", 15f);

        List<Effect> effects1 = new ArrayList<>();
        //effects1.add(new Effect("Dodge Chance", "+10%"));

        GearModel leggings = new GearModel();
        leggings.name = "Phantom Leggings";
        leggings.type = GearType.PANTS;
        leggings.allowedClass = ClassType.TANK;
        leggings.statBoosts = boosts1;
        leggings.skillAugments = effects1;
        leggings.description = "Light as mist, perfect for evasion.";
        storeItems.add(leggings);

        // Example 2: Iron Sword
        Map<String, Float> boosts2 = new HashMap<>();
        boosts2.put("ATK", 20f);

        GearModel sword = new GearModel();
        sword.name = "Iron Sword";
        sword.type = GearType.WEAPON;
        sword.allowedClass = ClassType.WARRIOR;
        sword.statBoosts = boosts2;
        sword.skillAugments = new ArrayList<>();
        sword.description = "A sturdy blade forged from iron.";
        storeItems.add(sword);

        // Example 3: Mystic Robe
        Map<String, Float> boosts3 = new HashMap<>();
        boosts3.put("INT", 25f);

        GearModel robe = new GearModel();
        robe.name = "Mystic Robe";
        robe.type = GearType.ARMOR;
        robe.allowedClass = ClassType.ROGUE;
        robe.statBoosts = boosts3;
        robe.skillAugments = new ArrayList<>();
        robe.description = "A robe infused with arcane power.";
        storeItems.add(robe);
    }

    // ==== rest of your store methods (buildStoreItems, showGearDetails, GearAdapter) unchanged ====
}

