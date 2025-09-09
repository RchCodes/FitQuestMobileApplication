package com.example.fitquest;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AvatarCreationActivity extends AppCompatActivity {

    private boolean isMale = true;
    private String chosenClass = "warrior";

    private ImageView baseBody, hairLayer, eyesLayer, noseLayer, lipsLayer, btn_create;

    public enum Category { HAIR, EYES, NOSE, LIPS }
    private Category currentCategory = Category.HAIR;

    private OptionsAdapter optionsAdapter;
    private RecyclerView recyclerColors;
    private ColorAdapter colorAdapter;

    private int currentHairColor = -1;
    private int currentEyesColor = -1;
    private int currentLipsColor = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_creation);

        // Layers
        baseBody = findViewById(R.id.baseBody);
        hairLayer = findViewById(R.id.hairLayer);
        eyesLayer = findViewById(R.id.eyesLayer);
        noseLayer = findViewById(R.id.noseLayer);
        lipsLayer = findViewById(R.id.lipsLayer);
        btn_create = findViewById(R.id.btn_create);

        // Options RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerOptions);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);
        optionsAdapter = new OptionsAdapter((drawableRes, category) -> applyCustomization(category, drawableRes));
        recyclerView.setAdapter(optionsAdapter);

        // Colors RecyclerView
        recyclerColors = findViewById(R.id.recyclerColors);
        recyclerColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        int[] colorPalette = {Color.BLACK, Color.DKGRAY, Color.GRAY, Color.WHITE,
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA};
        colorAdapter = new ColorAdapter(colorPalette, selectedColor -> {
            switch (currentCategory) {
                case HAIR:
                    hairLayer.setColorFilter(selectedColor);
                    currentHairColor = selectedColor;
                    break;
                case EYES:
                    eyesLayer.setColorFilter(selectedColor);
                    currentEyesColor = selectedColor;
                    break;
                case LIPS:
                    lipsLayer.setColorFilter(selectedColor);
                    currentLipsColor = selectedColor;
                    break;
            }
        });
        recyclerColors.setAdapter(colorAdapter);

        // Gender buttons
        findViewById(R.id.male_icon).setOnClickListener(v -> {
            isMale = true;
            updateBaseBody();
            loadOptions(currentCategory);
        });
        findViewById(R.id.female_icon).setOnClickListener(v -> {
            isMale = false;
            updateBaseBody();
            loadOptions(currentCategory);
        });

        // Class buttons
        findViewById(R.id.btn_warrior).setOnClickListener(v -> {
            chosenClass = "warrior";
            updateBaseBody();
        });
        findViewById(R.id.btn_rogue).setOnClickListener(v -> {
            chosenClass = "rogue";
            updateBaseBody();
        });
        findViewById(R.id.btn_tank).setOnClickListener(v -> {
            chosenClass = "tank";
            updateBaseBody();
        });

        // Category tabs
        findViewById(R.id.tab_hair).setOnClickListener(v -> {
            currentCategory = Category.HAIR;
            loadOptions(Category.HAIR);
        });
        findViewById(R.id.tab_eyes).setOnClickListener(v -> {
            currentCategory = Category.EYES;
            loadOptions(Category.EYES);
        });
        findViewById(R.id.tab_nose).setOnClickListener(v -> {
            currentCategory = Category.NOSE;
            loadOptions(Category.NOSE);
        });
        findViewById(R.id.tab_lips).setOnClickListener(v -> {
            currentCategory = Category.LIPS;
            loadOptions(Category.LIPS);
        });

        // ✅ Create button
        btn_create.setOnClickListener(v -> saveAvatar());

        // Load defaults
        updateBaseBody();
        loadOptions(Category.HAIR);
        setDefaultFeatures();
    }

    private void updateBaseBody() {
        int resId = R.drawable.warrior_male;
        switch (chosenClass) {
            case "rogue": resId = isMale ? R.drawable.rogue_male : R.drawable.rogue_female; break;
            case "tank": resId = isMale ? R.drawable.tank_male : R.drawable.tank_female; break;
            case "warrior": default: resId = isMale ? R.drawable.warrior_male : R.drawable.warrior_female; break;
        }
        baseBody.setImageResource(resId);
    }

    private void loadOptions(Category category) {
        List<Integer> options = new ArrayList<>();
        switch (category) {
            case HAIR:
                if (isMale) {
                    options.add(R.drawable.hair_male_1);
                    options.add(R.drawable.hair_male_2);
                    options.add(R.drawable.hair_male_3);
                } else {
                    options.add(R.drawable.hair_female_1);
                    options.add(R.drawable.hair_female_2);
                    options.add(R.drawable.hair_female_3);
                }
                recyclerColors.setVisibility(View.VISIBLE);
                break;
            case EYES:
                if (isMale) {
                    options.add(R.drawable.eyes_male_1);
                    options.add(R.drawable.eyes_male_2);
                    options.add(R.drawable.eyes_male_3);
                } else {
                    options.add(R.drawable.eyes_female_1);
                    options.add(R.drawable.eyes_female_2);
                    options.add(R.drawable.eyes_female_3);
                }
                recyclerColors.setVisibility(View.VISIBLE);
                break;
            case NOSE:
                for (int i = 1; i <= 8; i++)
                    options.add(getResources().getIdentifier("nose_" + i, "drawable", getPackageName()));
                recyclerColors.setVisibility(View.GONE);
                break;
            case LIPS:
                for (int i = 1; i <= 8; i++)
                    options.add(getResources().getIdentifier("lips_" + i, "drawable", getPackageName()));
                recyclerColors.setVisibility(View.GONE);
                break;
        }
        optionsAdapter.setOptions(options, category);
    }

    private void applyCustomization(Category category, int drawableRes) {
        switch (category) {
            case HAIR:
                hairLayer.setImageResource(drawableRes);
                hairLayer.setTag(drawableRes);
                break;
            case EYES:
                eyesLayer.setImageResource(drawableRes);
                eyesLayer.setTag(drawableRes);
                break;
            case NOSE:
                noseLayer.setImageResource(drawableRes);
                noseLayer.setTag(drawableRes);
                break;
            case LIPS:
                lipsLayer.setImageResource(drawableRes);
                lipsLayer.setTag(drawableRes);
                break;
        }
    }

    // ✅ Assign default features
    private void setDefaultFeatures() {
        if (isMale) {
            hairLayer.setImageResource(R.drawable.hair_male_1);
            hairLayer.setTag(R.drawable.hair_male_1);

            eyesLayer.setImageResource(R.drawable.eyes_male_1);
            eyesLayer.setTag(R.drawable.eyes_male_1);

            noseLayer.setImageResource(R.drawable.nose_1);
            noseLayer.setTag(R.drawable.nose_1);

            lipsLayer.setImageResource(R.drawable.lips_1);
            lipsLayer.setTag(R.drawable.lips_1);
        } else {
            hairLayer.setImageResource(R.drawable.hair_female_1);
            hairLayer.setTag(R.drawable.hair_female_1);

            eyesLayer.setImageResource(R.drawable.eyes_female_1);
            eyesLayer.setTag(R.drawable.eyes_female_1);

            noseLayer.setImageResource(R.drawable.nose_1);
            noseLayer.setTag(R.drawable.nose_1);

            lipsLayer.setImageResource(R.drawable.lips_1);
            lipsLayer.setTag(R.drawable.lips_1);
        }
    }

    private void saveAvatar() {
        AvatarModel avatar = new AvatarModel(isMale, chosenClass);
        avatar.isMale = isMale;
        avatar.chosenClass = chosenClass;

        // Features (default already assigned)
        avatar.hairOutlineRes = (Integer) hairLayer.getTag();
        avatar.hairFillRes = (Integer) hairLayer.getTag();
        avatar.eyesOutlineRes = (Integer) eyesLayer.getTag();
        avatar.eyesFillRes = (Integer) eyesLayer.getTag();
        avatar.noseRes = (Integer) noseLayer.getTag();
        avatar.lipsRes = (Integer) lipsLayer.getTag();

        // Colors
        avatar.hairColor = currentHairColor != -1 ? currentHairColor : Color.BLACK;
        avatar.eyesColor = currentEyesColor != -1 ? currentEyesColor : Color.BLACK;
        avatar.lipsColor = currentLipsColor != -1 ? currentLipsColor : Color.RED;

        // Validate
        if (avatar.hairOutlineRes == 0 || avatar.eyesOutlineRes == 0 ||
                avatar.noseRes == 0 || avatar.lipsRes == 0) {
            Toast.makeText(this, "Please complete your avatar before creating.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save using GsonHelper
        SharedPreferences prefs = getSharedPreferences("avatar_pref", MODE_PRIVATE);
        String avatarJson = GsonHelper.toJson(avatar);
        prefs.edit().putString("saved_avatar", avatarJson).apply();

        // Mark avatar as created
        SharedPreferences gamePrefs = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE);
        gamePrefs.edit().putBoolean("avatar_created", true).apply();

        Toast.makeText(this, "Avatar created successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
