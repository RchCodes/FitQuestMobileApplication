package com.example.fitquest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Avatar creation activity using AvatarModel + adapters + AvatarManager.
 */
public class AvatarCreationActivity extends AppCompatActivity {
    private static final String TAG = "AvatarCreation";

    // UI
    private EditText editAvatarName;
    private ImageView baseBodyView;
    private ImageView hairOutlineView, hairFillView, eyesOutlineView, eyesFillView, noseView, lipsView;
    private ImageView btnCreate;
    private FrameLayout progressOverlay;

    private RecyclerView recyclerOptions;
    private RecyclerView recyclerColors;

    // Adapters
    private OptionsAdapter optionsAdapter;
    private ColorAdapter colorAdapter;

    // The model holding the current choices
    private AvatarModel avatarModel = new AvatarModel();

    // current feature type strings
    private static final String FEATURE_HAIR = "hair";
    private static final String FEATURE_EYES = "eyes";
    private static final String FEATURE_NOSE = "nose";
    private static final String FEATURE_LIPS = "lips";
    private static final String FEATURE_BODY = "body";

    // currently active category
    private String currentFeature = FEATURE_HAIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_creation); // your XML

        // Bind views (ensure these IDs match your layout)
        editAvatarName = findViewById(R.id.edit_avatar_name);

        baseBodyView = findViewById(R.id.baseBody);

        // NOTE: make sure your XML has separate ImageViews for outline + fill.
        // If not, you can use the same view for outline and tint the same drawable (but separate is preferred).
        hairOutlineView = findViewById(R.id.hairLayer); // treat as outline
        hairFillView = findViewById(R.id.hairFillLayer); // you may need to add this id to XML
        eyesOutlineView = findViewById(R.id.eyesLayer); // outline
        eyesFillView = findViewById(R.id.eyesFillLayer); // fill (tintable)
        noseView = findViewById(R.id.noseLayer);
        lipsView = findViewById(R.id.lipsLayer);

        btnCreate = findViewById(R.id.btn_create);
        progressOverlay = findViewById(R.id.progressOverlay); // recommended to add overlay to this layout

        recyclerOptions = findViewById(R.id.recyclerOptions);
        recyclerColors = findViewById(R.id.recyclerColors);

        // Layout managers
        recyclerOptions.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // OptionsAdapter (listener updates model)
        optionsAdapter = new OptionsAdapter((outlineRes, fillRes, featureType) -> {
            switch (featureType) {
                case FEATURE_HAIR:
                    avatarModel.hairOutlineRes = outlineRes;
                    avatarModel.hairFillRes = fillRes;
                    break;
                case FEATURE_EYES:
                    avatarModel.eyesOutlineRes = outlineRes;
                    avatarModel.eyesFillRes = fillRes;
                    break;
                case FEATURE_NOSE:
                    avatarModel.noseRes = outlineRes;
                    break;
                case FEATURE_LIPS:
                    avatarModel.lipsRes = outlineRes;
                    break;
                case FEATURE_BODY:
                    avatarModel.bodyRes = outlineRes;
                    break;
            }
            AvatarRenderer.applyToViews(this, avatarModel, baseBodyView,
                    hairOutlineView, hairFillView, eyesOutlineView, eyesFillView, noseView, lipsView);
        });
        recyclerOptions.setAdapter(optionsAdapter);

        // Color palette (colors array — you can customize)
        int[] palette = new int[] {
                Color.BLACK, Color.DKGRAY, Color.GRAY, Color.WHITE,
                Color.parseColor("#2C3E50"), Color.parseColor("#E74C3C"),
                Color.parseColor("#3498DB"), Color.parseColor("#2ECC71"),
                Color.parseColor("#F1C40F"), Color.parseColor("#9B59B6")
        };

        colorAdapter = new ColorAdapter(palette, FEATURE_HAIR, (color, featureType) -> {
            applyColorToFeature(featureType, color);
        });
        recyclerColors.setAdapter(colorAdapter);

        // Wire UI controls (gender/class)
        findViewById(R.id.male_icon).setOnClickListener(v -> {
            avatarModel.isMale = true;
            updateBodyForClass();
        });
        findViewById(R.id.female_icon).setOnClickListener(v -> {
            avatarModel.isMale = false;
            updateBodyForClass();
        });

        findViewById(R.id.btn_warrior).setOnClickListener(v -> {
            avatarModel.chosenClass = "warrior";
            updateBodyForClass();
        });
        findViewById(R.id.btn_rogue).setOnClickListener(v -> {
            avatarModel.chosenClass = "rogue";
            updateBodyForClass();
        });
        findViewById(R.id.btn_tank).setOnClickListener(v -> {
            avatarModel.chosenClass = "tank";
            updateBodyForClass();
        });

        // Tabs for feature categories
        findViewById(R.id.tab_hair).setOnClickListener(v -> {
            currentFeature = FEATURE_HAIR;
            loadOptionsFor(FEATURE_HAIR);
            recyclerColors.setVisibility(View.VISIBLE);
            // change color adapter feature type
            recyclerColors.setAdapter(new ColorAdapter(palette, FEATURE_HAIR, (color, ft) -> applyColorToFeature(ft, color)));
        });
        findViewById(R.id.tab_eyes).setOnClickListener(v -> {
            currentFeature = FEATURE_EYES;
            loadOptionsFor(FEATURE_EYES);
            recyclerColors.setVisibility(View.VISIBLE);
            recyclerColors.setAdapter(new ColorAdapter(palette, FEATURE_EYES, (color, ft) -> applyColorToFeature(ft, color)));
        });
        findViewById(R.id.tab_nose).setOnClickListener(v -> {
            currentFeature = FEATURE_NOSE;
            loadOptionsFor(FEATURE_NOSE);
            recyclerColors.setVisibility(View.GONE);
        });
        findViewById(R.id.tab_lips).setOnClickListener(v -> {
            currentFeature = FEATURE_LIPS;
            loadOptionsFor(FEATURE_LIPS);
            recyclerColors.setVisibility(View.VISIBLE);
            recyclerColors.setAdapter(new ColorAdapter(palette, FEATURE_LIPS, (color, ft) -> applyColorToFeature(ft, color)));
        });

        // Initial state
        updateBodyForClass();
        loadOptionsFor(FEATURE_HAIR);

        // Create button saves avatar (local + RTDB)
        btnCreate.setOnClickListener(v -> {
            // basic validation: ensure mandatory parts exist
            if (avatarModel.hairOutlineRes == 0 || avatarModel.hairFillRes == 0
                    || avatarModel.eyesOutlineRes == 0 || avatarModel.eyesFillRes == 0
                    || avatarModel.noseRes == 0 || avatarModel.lipsRes == 0) {
                Toast.makeText(this, "Please complete all avatar parts before saving.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Set body resource again (safety)
            updateBodyForClass();

            progressOverlay.setVisibility(View.VISIBLE);

            // Save via AvatarManager
            AvatarManager.saveAvatar(this, avatarModel, new AvatarManager.SaveCallback() {
                @Override
                public void onSuccess() {
                    progressOverlay.setVisibility(View.GONE);
                    Toast.makeText(AvatarCreationActivity.this, "Avatar saved!", Toast.LENGTH_SHORT).show();
                    // optional: save avatar name under users node
                    saveAvatarNameToUser();
                    // proceed to main
                    startActivity(new Intent(AvatarCreationActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    progressOverlay.setVisibility(View.GONE);
                    Toast.makeText(AvatarCreationActivity.this, "Failed to save avatar: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void saveAvatarNameToUser() {
        String name = editAvatarName.getText() != null ? editAvatarName.getText().toString().trim() : "";
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u != null && !name.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("users").child(u.getUid()).child("displayName").setValue(name);
        }
    }

    private void applyColorToFeature(String featureType, int color) {
        switch (featureType) {
            case FEATURE_HAIR:
                avatarModel.hairColor = color;
                break;
            case FEATURE_EYES:
                avatarModel.eyesColor = color;
                break;
            case FEATURE_LIPS:
                avatarModel.lipsColor = color;
                break;
        }
        AvatarRenderer.applyToViews(this, avatarModel, baseBodyView,
                hairOutlineView, hairFillView, eyesOutlineView, eyesFillView, noseView, lipsView);
    }

    /**
     * Update body image depending on gender + chosen class.
     */
    private void updateBodyForClass() {
        int res;
        switch (avatarModel.chosenClass) {
            case "rogue":
                res = avatarModel.isMale ? R.drawable.rogue_male : R.drawable.rogue_female;
                break;
            case "tank":
                res = avatarModel.isMale ? R.drawable.tank_male : R.drawable.tank_female;
                break;
            case "warrior":
            default:
                res = avatarModel.isMale ? R.drawable.warrior_male : R.drawable.warrior_female;
                break;
        }
        avatarModel.bodyRes = res;
        AvatarRenderer.applyToViews(this, avatarModel, baseBodyView,
                hairOutlineView, hairFillView, eyesOutlineView, eyesFillView, noseView, lipsView);
    }

    /**
     * Load option items into adapter for the feature requested.
     * For hair/eyes we create OptionItem pairs (outline + fill).
     */
    private void loadOptionsFor(String featureType) {
        List<OptionsAdapter.OptionItem> options = new ArrayList<>();

        switch (featureType) {
            case FEATURE_HAIR:
                if (avatarModel.isMale) {
                    options.add(new OptionsAdapter.OptionItem(R.drawable.hair_male_1, R.drawable.hair_male_fill_1, FEATURE_HAIR));
                    options.add(new OptionsAdapter.OptionItem(R.drawable.hair_male_2, R.drawable.hair_male_fill_2, FEATURE_HAIR));
                    options.add(new OptionsAdapter.OptionItem(R.drawable.hair_male_3, R.drawable.hair_male_fill_3, FEATURE_HAIR));
                } else {
                    options.add(new OptionsAdapter.OptionItem(R.drawable.hair_female_1, R.drawable.hair_female_fill_1, FEATURE_HAIR));
                    options.add(new OptionsAdapter.OptionItem(R.drawable.hair_female_2, R.drawable.hair_female_fill_2, FEATURE_HAIR));
                    options.add(new OptionsAdapter.OptionItem(R.drawable.hair_female_3, R.drawable.hair_female_fill_3, FEATURE_HAIR));
                }
                break;

            case FEATURE_EYES:
                if (avatarModel.isMale) {
                    options.add(new OptionsAdapter.OptionItem(R.drawable.eyes_male_1, R.drawable.eyes_male_fill_1, FEATURE_EYES));
                    options.add(new OptionsAdapter.OptionItem(R.drawable.eyes_male_2, R.drawable.eyes_male_fill_2, FEATURE_EYES));
                    options.add(new OptionsAdapter.OptionItem(R.drawable.eyes_male_3, R.drawable.eyes_male_fill_3, FEATURE_EYES));
                } else {
                    options.add(new OptionsAdapter.OptionItem(R.drawable.eyes_female_1, R.drawable.eyes_female_fill_1, FEATURE_EYES));
                    options.add(new OptionsAdapter.OptionItem(R.drawable.eyes_female_2, R.drawable.eyes_female_fill_2, FEATURE_EYES));
                    options.add(new OptionsAdapter.OptionItem(R.drawable.eyes_female_3, R.drawable.eyes_female_fill_3, FEATURE_EYES));
                }
                break;

            case FEATURE_NOSE:
                for (int i = 1; i <= 8; i++) {
                    int id = getResources().getIdentifier("nose_" + i, "drawable", getPackageName());
                    if (id != 0) options.add(new OptionsAdapter.OptionItem(id, 0, FEATURE_NOSE));
                }
                break;

            case FEATURE_LIPS:
                for (int i = 1; i <= 8; i++) {
                    int id = getResources().getIdentifier("lips_" + i, "drawable", getPackageName());
                    if (id != 0) options.add(new OptionsAdapter.OptionItem(id, 0, FEATURE_LIPS));
                }
                break;

            case FEATURE_BODY:
                // add body variants if you want (class/gender combos)
                break;
        }

        optionsAdapter.setOptions(options);
    }
}
