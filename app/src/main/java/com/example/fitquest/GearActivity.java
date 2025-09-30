package com.example.fitquest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GearActivity extends BaseActivity {

    private AvatarDisplayManager avatarHelper;
    private AvatarModel avatar;

    // UI
    private ImageView btnBack;
    private Button btnSave, btnEquip;
    private GridView gridGear;
    private ImageButton tabAll, tabWeapon, tabArmor, tabPants, tabBoots, tabAccessory;

    private TextView itemName, itemDesc, itemBoosts, itemSkill;

    // Data
    private List<GearModel> allGear;
    private List<GearModel> filteredGear;
    private GearAdapter gearAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gear);

        // ==== Bind UI ====
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);

        tabAll = findViewById(R.id.tabAll);
        tabWeapon = findViewById(R.id.tabWeapon);
        tabArmor = findViewById(R.id.tabArmor);
        tabPants = findViewById(R.id.tabPants);
        tabBoots = findViewById(R.id.tabBoots);
        tabAccessory = findViewById(R.id.tabAccessory);

        gridGear = findViewById(R.id.gridGear);
        itemName = findViewById(R.id.itemName);
        itemDesc = findViewById(R.id.itemDesc);
        itemBoosts = findViewById(R.id.itemBoosts);
        itemSkill = findViewById(R.id.itemSkill);
        btnEquip = findViewById(R.id.btnEquip);

        // ==== Avatar ====
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
        loadAvatarIfExists();

        // ==== Data ====
        buildGearItems(); // fill allGear
        filteredGear = new ArrayList<>(allGear);

        gearAdapter = new GearAdapter(this, filteredGear);
        gridGear.setAdapter(gearAdapter);

        // ==== Handlers ====
        gridGear.setOnItemClickListener((parent, view, position, id) -> {
            GearModel gear = filteredGear.get(position);
            showGearDetails(gear);
        });

        btnEquip.setOnClickListener(v -> {
            // TODO: implement equipping logic
            // Example:
            // avatar.equip(selectedGear);
            // avatarHelper.loadAvatar(avatar);
        });

        setupListeners();
    }

    /** Load avatar */
    private void loadAvatarIfExists() {
        avatar = AvatarManager.loadAvatarOffline(this);
        if (avatar != null) {
            avatarHelper.loadAvatar(avatar);
        }
    }

    /** Populate some test gear (like in StoreActivity) */
    private void buildGearItems() {
        allGear = new ArrayList<>();

        Map<String, Float> boosts1 = new HashMap<>();
        boosts1.put("AGI", 15f);
        GearModel leggings = new GearModel();
        leggings.name = "Phantom Leggings";
        leggings.type = GearType.PANTS;
        leggings.allowedClass = ClassType.TANK;
        leggings.statBoosts = boosts1;
        leggings.skillAugments = new ArrayList<>();
        leggings.description = "Light as mist, perfect for evasion.";
        allGear.add(leggings);

        Map<String, Float> boosts2 = new HashMap<>();
        boosts2.put("ATK", 20f);
        GearModel sword = new GearModel();
        sword.name = "Iron Sword";
        sword.type = GearType.WEAPON;
        sword.allowedClass = ClassType.WARRIOR;
        sword.statBoosts = boosts2;
        sword.skillAugments = new ArrayList<>();
        sword.description = "A sturdy blade forged from iron.";
        allGear.add(sword);

        Map<String, Float> boosts3 = new HashMap<>();
        boosts3.put("INT", 25f);
        GearModel robe = new GearModel();
        robe.name = "Mystic Robe";
        robe.type = GearType.ARMOR;
        robe.allowedClass = ClassType.ROGUE;
        robe.statBoosts = boosts3;
        robe.skillAugments = new ArrayList<>();
        robe.description = "A robe infused with arcane power.";
        allGear.add(robe);
    }

    /** Show gear details */
    private void showGearDetails(GearModel gear) {
        itemName.setText(gear.name + " (" + gear.allowedClass.name() + ")");
        itemDesc.setText(gear.description != null ? gear.description : "No description");

        StringBuilder boostText = new StringBuilder("Boosts:\n");
        if (gear.statBoosts != null && !gear.statBoosts.isEmpty()) {
            for (Map.Entry<String, Float> entry : gear.statBoosts.entrySet()) {
                boostText.append("+").append(entry.getValue()).append("% ").append(entry.getKey()).append("\n");
            }
        } else {
            boostText.append("None");
        }
        itemBoosts.setText(boostText.toString().trim());

        StringBuilder skillText = new StringBuilder("Skill Effect:\n");
        if (gear.skillAugments != null && !gear.skillAugments.isEmpty()) {
            for (Effect effect : gear.skillAugments) {
                skillText.append(effect.getName()).append(" ").append(effect.getValue()).append("\n");
            }
        } else {
            skillText.append("None");
        }
        itemSkill.setText(skillText.toString().trim());
    }

    /** Tabs filtering */
    private void filterGear(GearType... types) {
        filteredGear.clear();
        for (GearModel gear : allGear) {
            for (GearType t : types) {
                if (gear.type == t) {
                    filteredGear.add(gear);
                }
            }
            if (types.length == GearType.values().length) {
                filteredGear.addAll(allGear);
                break;
            }
        }
        gearAdapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> {
            if (avatar != null) {
                AvatarManager.saveAvatarOffline(this, avatar);
            }
            finish();
        });

        tabAll.setOnClickListener(v -> filterGear(GearType.values()));
        tabWeapon.setOnClickListener(v -> filterGear(GearType.WEAPON));
        tabArmor.setOnClickListener(v -> filterGear(GearType.ARMOR));
        tabPants.setOnClickListener(v -> filterGear(GearType.PANTS));
        tabBoots.setOnClickListener(v -> filterGear(GearType.BOOTS));
        tabAccessory.setOnClickListener(v -> filterGear(GearType.ACCESSORY));
    }

    public AvatarDisplayManager getAvatarHelper() {
        return avatarHelper;
    }

    public AvatarModel getAvatar() {
        return avatar;
    }
}
