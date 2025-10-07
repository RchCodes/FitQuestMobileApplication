package com.example.fitquest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AvatarCreationActivity extends BaseActivity {

    // Texts for current selection
    private TextView tvHair, tvEyes, tvNose, tvLips;

    private EditText etAvatarName;

    // Arrows
    private ImageButton btnHairLeft, btnHairRight,
            btnEyesLeft, btnEyesRight,
            btnNoseLeft, btnNoseRight,
            btnLipsLeft, btnLipsRight;

    // RecyclerViews for colors
    private RecyclerView rvHairOptions, rvEyeOptions;

    // Avatar preview layers
    private ImageView ivBody, ivOutfit, ivHairOutline, ivHairFill, ivEyesOutline, ivEyesIris, ivNose, ivLips;

    // Class buttons
    private Button btnWarrior, btnRogue, btnTank, btnMale, btnFemale;

    // Index trackers
    private int currentHairIndex = 0;
    private int currentEyesIndex = 0;
    private int currentNoseIndex = 0;
    private int currentLipsIndex = 0;

    // Gender
    private boolean isMale = true;

    // Resource arrays
    private int[] maleHairOutlines = {
            R.drawable.hair_male_1_outline, R.drawable.hair_male_2_outline,
            R.drawable.hair_male_3_outline, R.drawable.hair_male_4_outline};
    private int[] maleHairFills = {
            R.drawable.hair_male_fill_1, R.drawable.hair_male_fill_2,
            R.drawable.hair_male_fill_3, R.drawable.hair_male_fill_4};

    private int[] femaleHairOutlines = {
            R.drawable.hair_female_1_outline, R.drawable.hair_female_2_outline,
            R.drawable.hair_female_3_outline, R.drawable.hair_female_4_outline};
    private int[] femaleHairFills = {
            R.drawable.hair_female_fill_1, R.drawable.hair_female_fill_2,
            R.drawable.hair_female_fill_3, R.drawable.hair_female_fill_4};

    private int[] eyesOutlines = {
            R.drawable.eyes_1_outline, R.drawable.eyes_2_outline,
            R.drawable.eyes_3_outline, R.drawable.eyes_4_outline, R.drawable.eyes_5_outline};
    private int[] eyesIrises = {
            R.drawable.eyes_iris_1, R.drawable.eyes_iris_2,
            R.drawable.eyes_iris_3, R.drawable.eyes_iris_4, R.drawable.eyes_iris_5};

    private int[] noses = {
            R.drawable.nose_1, R.drawable.nose_2,
            R.drawable.nose_3, R.drawable.nose_4,
            R.drawable.nose_5, R.drawable.nose_6,
            R.drawable.nose_7, R.drawable.nose_8};

    private int[] lips = {
            R.drawable.lips_1, R.drawable.lips_2,
            R.drawable.lips_3, R.drawable.lips_4,
            R.drawable.lips_5, R.drawable.lips_6,
            R.drawable.lips_7, R.drawable.lips_8};

    private int[] maleOutfits = {
            R.drawable.outfit_male_warrior,
            R.drawable.outfit_male_rogue,
            R.drawable.outfit_male_tank};
    private int[] femaleOutfits = {
            R.drawable.outfit_female_warrior,
            R.drawable.outfit_female_rogue,
            R.drawable.outfit_female_tank};

    // Currently selected class index (0 = warrior, 1 = rogue, 2 = tank)
    private int currentClassIndex = 0;

    private int currentHairColor = Color.WHITE; // default hair color
    private int currentEyesColor = Color.WHITE; // default eyes color


    // Color palette (8 fixed colors)
    // Custom Color Palette
    private final int[] colors = {
            Color.parseColor("#303030"), // BLACK
            Color.parseColor("#AFBBBC"), // GRAY
            Color.parseColor("#00B000"), // GREEN
            Color.parseColor("#001089"), // BLUE
            Color.parseColor("#FFDD46"), // YELLOW
            Color.parseColor("#3B0059"), // VIOLET
            Color.parseColor("#5E0000"), // RED
            Color.parseColor("#B000B0"), // PINK
            Color.parseColor("#CF4F00"), // ORANGE
            Color.parseColor("#D88C00"), // ORANGE
            Color.parseColor("#613D00"), // BROWN
            Color.parseColor("#00AC95")  // CYAN
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_creation);

        bindViews();
        setupArrows();
        setupColorPickers();
        setupClassButtons();
        setupGenderButtons();

        updateHair();
        updateEyes();
        updateNose();
        updateLips();
        updateOutfit();
        updateBody();
        MusicManager.start(this);
    }

    private void bindViews() {

        etAvatarName = findViewById(R.id.etAvatarName);
        // TextViews
        tvHair = findViewById(R.id.tvHair);
        tvEyes = findViewById(R.id.tvEyes);
        tvNose = findViewById(R.id.tvNose);
        tvLips = findViewById(R.id.tvLips);

        // Buttons
        btnHairLeft = findViewById(R.id.btnHairLeft);
        btnHairRight = findViewById(R.id.btnHairRight);
        btnEyesLeft = findViewById(R.id.btnEyesLeft);
        btnEyesRight = findViewById(R.id.btnEyesRight);
        btnNoseLeft = findViewById(R.id.btnNoseLeft);
        btnNoseRight = findViewById(R.id.btnNoseRight);
        btnLipsLeft = findViewById(R.id.btnLipsLeft);
        btnLipsRight = findViewById(R.id.btnLipsRight);

        btnWarrior = findViewById(R.id.btnWarrior);
        btnRogue = findViewById(R.id.btnRogue);
        btnTank = findViewById(R.id.btnTank);
        btnMale = findViewById(R.id.btnMale);
        btnFemale = findViewById(R.id.btnFemale);

        // RecyclerViews
        rvHairOptions = findViewById(R.id.rvHairOptions);
        rvEyeOptions = findViewById(R.id.rvEyeOptions);

        // Avatar preview layers
        ivBody = findViewById(R.id.baseBody);
        ivOutfit = findViewById(R.id.classOutfit);
        ivHairOutline = findViewById(R.id.hairOutlineLayer);
        ivHairFill = findViewById(R.id.hairFillLayer);
        ivEyesOutline = findViewById(R.id.eyesOutlineLayer);
        ivEyesIris = findViewById(R.id.eyesFillLayer);
        ivNose = findViewById(R.id.noseLayer);
        ivLips = findViewById(R.id.lipsLayer);
    }

    private void setupArrows() {
        btnHairLeft.setOnClickListener(v -> {
            currentHairIndex = (currentHairIndex - 1 + 4) % 4;
            updateHair();
        });
        btnHairRight.setOnClickListener(v -> {
            currentHairIndex = (currentHairIndex + 1) % 4;
            updateHair();
        });

        btnEyesLeft.setOnClickListener(v -> {
            currentEyesIndex = (currentEyesIndex - 1 + 5) % 5;
            updateEyes();
        });
        btnEyesRight.setOnClickListener(v -> {
            currentEyesIndex = (currentEyesIndex + 1) % 5;
            updateEyes();
        });

        btnNoseLeft.setOnClickListener(v -> {
            currentNoseIndex = (currentNoseIndex - 1 + 8) % 8;
            updateNose();
        });
        btnNoseRight.setOnClickListener(v -> {
            currentNoseIndex = (currentNoseIndex + 1) % 8;
            updateNose();
        });

        btnLipsLeft.setOnClickListener(v -> {
            currentLipsIndex = (currentLipsIndex - 1 + 8) % 8;
            updateLips();
        });
        btnLipsRight.setOnClickListener(v -> {
            currentLipsIndex = (currentLipsIndex + 1) % 8;
            updateLips();
        });

        // Inside AvatarCreationActivity.java

        Button btnCreate = findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(v -> {
            String username = etAvatarName.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a name for your avatar", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determine body style, outfit & weapon based on gender & class
            String bodyStyle = isMale ? "body_male" : "body_female";
            String outfit = getOutfitResourceName(isMale, currentClassIndex);
            String weapon = getWeaponResourceName(isMale, currentClassIndex);

            AvatarModel avatar = new AvatarModel(
                    username,
                    isMale ? "male" : "female",
                    getClassName(currentClassIndex), // "warrior", "rogue", "tank"
                    bodyStyle,
                    outfit,
                    weapon,
                    getHairOutlineResName(isMale, currentHairIndex),
                    getHairFillResName(isMale, currentHairIndex),
                    getHairColorHex(),
                    getEyesOutlineResName(currentEyesIndex),
                    getEyesFillResName(currentEyesIndex),
                    getEyesColorHex(),
                    getNoseResName(currentNoseIndex),
                    getLipsResName(currentLipsIndex)
            );

            // Initialize default stats
            avatar.setCoins(0);
            avatar.setXp(0);
            avatar.setLevel(1);
            avatar.setRank(0);

            avatar.getPassiveSkills();


            // Save using ProgressSyncManager for intelligent saving
            ProgressSyncManager.saveProgress(this, avatar, false); // Save offline first
            ProgressSyncManager.saveProgress(this, avatar, true);  // Save online

            // Save username to SharedPreferences
            getSharedPreferences("FitQuestPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("username", username)
                    .apply();

            // Go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });



    }

    private void setupColorPickers() {
        // Hair color
        rvHairOptions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHairOptions.setAdapter(new ColorAdapter(colors, color -> {
            currentHairColor = color; // save selected hair color
            ivHairFill.setColorFilter(color);
        }));

        // Eye color
        rvEyeOptions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvEyeOptions.setAdapter(new ColorAdapter(colors, color -> {
            currentEyesColor = color; // save selected eye color
            ivEyesIris.setColorFilter(color);
        }));
    }


    private void setupClassButtons() {
        btnWarrior.setOnClickListener(v -> {
            currentClassIndex = 0;
            updateOutfit();
        });
        btnRogue.setOnClickListener(v -> {
            currentClassIndex = 1;
            updateOutfit();
        });
        btnTank.setOnClickListener(v -> {
            currentClassIndex = 2;
            updateOutfit();
        });
    }

    private void setupGenderButtons() {
        btnMale.setOnClickListener(v -> {
            isMale = true;
            updateBody();
            updateHair();
            updateOutfit();
        });
        btnFemale.setOnClickListener(v -> {
            isMale = false;
            updateBody();
            updateHair();
            updateOutfit();
        });
    }

    private void updateHair() {
        tvHair.setText("Hair #" + (currentHairIndex + 1));
        if (isMale) {
            ivHairOutline.setImageResource(maleHairOutlines[currentHairIndex]);
            ivHairFill.setImageResource(maleHairFills[currentHairIndex]);
        } else {
            ivHairOutline.setImageResource(femaleHairOutlines[currentHairIndex]);
            ivHairFill.setImageResource(femaleHairFills[currentHairIndex]);
        }
    }

    private void updateEyes() {
        tvEyes.setText("Eyes #" + (currentEyesIndex + 1));
        ivEyesOutline.setImageResource(eyesOutlines[currentEyesIndex]);
        ivEyesIris.setImageResource(eyesIrises[currentEyesIndex]);
    }

    private void updateNose() {
        tvNose.setText("Nose #" + (currentNoseIndex + 1));
        ivNose.setImageResource(noses[currentNoseIndex]);
    }

    private void updateLips() {
        tvLips.setText("Lips #" + (currentLipsIndex + 1));
        ivLips.setImageResource(lips[currentLipsIndex]);
    }

    private void updateOutfit() {
        if (isMale) {
            ivOutfit.setImageResource(maleOutfits[currentClassIndex]);
        } else {
            ivOutfit.setImageResource(femaleOutfits[currentClassIndex]);
        }
    }

    private void updateBody() {
        if (isMale) {
            ivBody.setImageResource(R.drawable.body_male);
        } else {
            ivBody.setImageResource(R.drawable.body_female);
        }
    }


    // Utility method to get drawable resource name
    private String getDrawableName(int resId) {
        return getResources().getResourceEntryName(resId);
    }

    // Map currentClassIndex to class name
    private String getClassName(int index) {
        switch (index) {
            case 0: return "warrior";
            case 1: return "rogue";
            case 2: return "tank";
            default: return "warrior";
        }
    }

    // Outfit resource name based on gender and class
    private String getOutfitResourceName(boolean isMale, int classIndex) {
        if (isMale) {
            switch (classIndex) {
                case 0: return "outfit_male_warrior";
                case 1: return "outfit_male_rogue";
                case 2: return "outfit_male_tank";
            }
        } else {
            switch (classIndex) {
                case 0: return "outfit_female_warrior";
                case 1: return "outfit_female_rogue";
                case 2: return "outfit_female_tank";
            }
        }
        return isMale ? "outfit_male_warrior" : "outfit_female_warrior";
    }

    // Weapon resource name based on gender and class
    private String getWeaponResourceName(boolean isMale, int classIndex) {
        if (isMale) {
            switch (classIndex) {
                case 0: return "weapon_male_sword";
                case 1: return "weapon_male_dagger";
                case 2: return "weapon_male_hammer";
            }
        } else {
            switch (classIndex) {
                case 0: return "weapon_female_sword";
                case 1: return "weapon_female_dagger";
                case 2: return "weapon_female_hammer";
            }
        }
        return isMale ? "weapon_male_sword" : "weapon_female_sword";
    }

    // Hair drawable names
    private String getHairOutlineResName(boolean isMale, int hairIndex) {
        if (isMale) return "hair_male_" + (hairIndex + 1) + "_outline";
        else return "hair_female_" + (hairIndex + 1) + "_outline";
    }

    private String getHairFillResName(boolean isMale, int hairIndex) {
        if (isMale) return "hair_male_fill_" + (hairIndex + 1);
        else return "hair_female_fill_" + (hairIndex + 1);
    }

    // Hair color in HEX
    private String getHairColorHex() {
        return String.format("#%06X", (0xFFFFFF & currentHairColor));
    }

    // Hair color in HEX
    private String getEyesColorHex() {
        return String.format("#%06X", (0xFFFFFF & currentEyesColor));
    }


    // Eyes drawable names
    private String getEyesOutlineResName(int index) {
        return "eyes_" + (index + 1) + "_outline";
    }

    private String getEyesFillResName(int index) {
        return "eyes_iris_" + (index + 1);
    }

    // Nose & Lips
    private String getNoseResName(int index) {
        return "nose_" + (index + 1);
    }

    private String getLipsResName(int index) {
        return "lips_" + (index + 1);
    }

}
