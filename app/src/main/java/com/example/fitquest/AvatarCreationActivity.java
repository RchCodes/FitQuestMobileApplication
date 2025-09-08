package com.example.fitquest;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AvatarCreationActivity extends AppCompatActivity {

    // Gender + Class
    private boolean isMale = true; // default male
    private String chosenClass = "warrior"; // default class

    // UI references
    private ImageView baseBody, hairLayer, eyesLayer, noseLayer, lipsLayer;

    public enum Category { HAIR, EYES, NOSE, LIPS }

    private Category currentCategory = Category.HAIR;
    private OptionsAdapter optionsAdapter;

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

        EditText username = findViewById(R.id.edit_avatar_username);

        // RecyclerView setup
        RecyclerView recyclerView = findViewById(R.id.recyclerOptions);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        optionsAdapter = new OptionsAdapter((drawableRes, category) -> {
            applyCustomization(category, drawableRes);
        });
        recyclerView.setAdapter(optionsAdapter);

        // Gender buttons
        findViewById(R.id.male_icon).setOnClickListener(v -> {
            isMale = true;
            updateBaseBody();
            loadOptions(currentCategory); // reload gendered options
        });
        findViewById(R.id.female_icon).setOnClickListener(v -> {
            isMale = false;
            updateBaseBody();
            loadOptions(currentCategory); // reload gendered options
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

        // Tabs for customization
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

        // Load defaults
        updateBaseBody();
        loadOptions(Category.HAIR);
    }

    /**
     * Updates the base body depending on class + gender
     */
    private void updateBaseBody() {
        int resId;

        switch (chosenClass) {
            case "rogue":
                resId = isMale ? R.drawable.rogue_male : R.drawable.rogue_female;
                break;
            case "tank":
                resId = isMale ? R.drawable.tank_male : R.drawable.tank_female;
                break;
            default: // warrior
                resId = isMale ? R.drawable.warrior_male : R.drawable.warrior_female;
                break;
        }

        baseBody.setImageResource(resId);
    }

    /**
     * Loads customization options for the current category (gender-specific)
     */
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
                break;

            case NOSE: // shared assets
                options.add(R.drawable.nose_1);
                options.add(R.drawable.nose_2);
                options.add(R.drawable.nose_3);
                options.add(R.drawable.nose_4);
                options.add(R.drawable.nose_5);
                options.add(R.drawable.nose_6);
                options.add(R.drawable.nose_7);
                options.add(R.drawable.nose_8);
                options.add(R.drawable.nose_9);
                options.add(R.drawable.nose_10);
                break;

            case LIPS:
                options.add(R.drawable.lips_1);
                options.add(R.drawable.lips_2);
                options.add(R.drawable.lips_3);
                options.add(R.drawable.lips_4);
                options.add(R.drawable.lips_5);
                options.add(R.drawable.lips_6);
                options.add(R.drawable.lips_7);
                options.add(R.drawable.lips_8);
                options.add(R.drawable.lips_9);
                options.add(R.drawable.lips_10);
                break;
        }

        optionsAdapter.setOptions(options, category);
    }

    /**
     * Applies selected customization to the avatar layers
     */
    private void applyCustomization(Category category, int drawableRes) {
        switch (category) {
            case HAIR:
                hairLayer.setImageResource(drawableRes);
                break;
            case EYES:
                eyesLayer.setImageResource(drawableRes);
                break;
            case NOSE:
                noseLayer.setImageResource(drawableRes);
                break;
            case LIPS:
                lipsLayer.setImageResource(drawableRes);
                break;
        }
    }
}
