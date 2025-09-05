package com.example.fitquest;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

public class AvatarCreationActivity extends AppCompatActivity {

    // Character overlays
    private ImageView baseBody;
    private ImageView overlayHair, overlayEyes, overlayNose, overlayLips;

    // Gender icons
    private ImageView maleIcon, femaleIcon;

    // Class icons
    private ImageView btnWarrior, btnRogue, btnTank;
    private ImageView selectedClassIcon = null;

    // Username field
    private EditText editAvatarUsername;

    // Grids
    private GridLayout gridHair, gridEyes, gridNose, gridLips;

    // Tabs
    private ImageView tabHair, tabEyes, tabNose, tabLips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_creation);

        // base + overlays
        baseBody = findViewById(R.id.baseBody);
        overlayHair = findViewById(R.id.hairLayer);
        overlayEyes = findViewById(R.id.eyesLayer);
        overlayNose = findViewById(R.id.noseLayer);
        overlayLips = findViewById(R.id.lipsLayer);

        // username field
        editAvatarUsername = findViewById(R.id.edit_avatar_username);

        // gender icons
        maleIcon = findViewById(R.id.male_icon);
        femaleIcon = findViewById(R.id.female_icon);

        // class icons
        btnWarrior = findViewById(R.id.btn_warrior);
        btnRogue = findViewById(R.id.btn_rogue);
        btnTank = findViewById(R.id.btn_tank);

        // tabs
        tabHair = findViewById(R.id.tab_hair);
        tabEyes = findViewById(R.id.tab_eyes);
        tabNose = findViewById(R.id.tab_nose);
        tabLips = findViewById(R.id.tab_lips);

        // grids
        gridHair = findViewById(R.id.gridHair);
        gridEyes = findViewById(R.id.gridEyes);
        gridNose = findViewById(R.id.gridNose);
        gridLips = findViewById(R.id.gridLips);

        // Gender toggle
        maleIcon.setOnClickListener(v -> {
            baseBody.setImageResource(R.drawable.male2);
            showToast("Male selected");
        });

        femaleIcon.setOnClickListener(v -> {
            baseBody.setImageResource(R.drawable.female);
            showToast("Female selected");
        });

        // Class selection
        View.OnClickListener classClick = v -> setSelectedClass((ImageView) v);
        btnWarrior.setOnClickListener(classClick);
        btnRogue.setOnClickListener(classClick);
        btnTank.setOnClickListener(classClick);

        // Tab clicks
        tabHair.setOnClickListener(v -> showGrid(gridHair));
        tabEyes.setOnClickListener(v -> {
            showGrid(gridEyes);
            setupEyeColors(gridEyes); // fill with color boxes
        });
        tabNose.setOnClickListener(v -> showGrid(gridNose));
        tabLips.setOnClickListener(v -> showGrid(gridLips));

        // Wire sample grid
        wireGridItems(gridHair, Category.HAIR);

        // Create button
        findViewById(R.id.btn_create).setOnClickListener(v -> {
            String username = editAvatarUsername.getText().toString().trim();

            if (username.isEmpty()) {
                showToast("Please enter a username");
            } else {
                showToast("Avatar created for: " + username);
            }
        });
    }

    /** Show only the selected grid, hide others */
    private void showGrid(GridLayout target) {
        if (gridHair != null) gridHair.setVisibility(View.GONE);
        if (gridEyes != null) gridEyes.setVisibility(View.GONE);
        if (gridNose != null) gridNose.setVisibility(View.GONE);
        if (gridLips != null) gridLips.setVisibility(View.GONE);

        if (target != null) target.setVisibility(View.VISIBLE);
    }

    /** Create eye color boxes dynamically */
    private void setupEyeColors(GridLayout grid) {
        grid.removeAllViews(); // clear before adding

        int[] colors = {
                Color.BLUE, Color.GREEN, Color.BLACK,
                Color.DKGRAY, Color.GRAY, Color.CYAN,
                Color.MAGENTA, Color.RED
        };

        int size = (int) getResources().getDimensionPixelSize(R.dimen.eye_color_box);

        for (int color : colors) {
            View box = new View(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(8, 8, 8, 8);
            box.setLayoutParams(params);
            box.setBackgroundColor(color);

            box.setOnClickListener(v -> {
                overlayEyes.setColorFilter(color); // apply tint
                showToast("Eye color changed");
            });

            grid.addView(box);
        }
    }

    private void setSelectedClass(ImageView btn) {
        if (selectedClassIcon != null) {
            selectedClassIcon.setAlpha(1.0f);
        }
        selectedClassIcon = btn;
        selectedClassIcon.setAlpha(0.6f);
        showToast("Class selected");
    }

    private void wireGridItems(GridLayout grid, Category category) {
        if (grid == null) return;

        int childCount = grid.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = grid.getChildAt(i);
            ImageView iv = child.findViewById(R.id.customization_icon);

            if (iv == null && child instanceof ImageView) {
                iv = (ImageView) child;
            }

            if (iv != null) {
                final int resId = getDrawableIdFromImageView(iv);
                iv.setOnClickListener(v -> {
                    if (resId != 0) {
                        switch (category) {
                            case HAIR: applyHair(resId); break;
                            case EYES: applyEyes(resId); break;
                            case NOSE: applyNose(resId); break;
                            case LIPS: applyLips(resId); break;
                        }
                    }
                });
            }
        }
    }

    private int getDrawableIdFromImageView(ImageView iv) {
        Object tag = iv.getTag();
        if (tag instanceof String) {
            String tagStr = (String) tag;
            return getResources().getIdentifier(tagStr, "drawable", getPackageName());
        }
        return 0;
    }

    private void applyHair(@DrawableRes int resId) {
        overlayHair.setImageResource(resId);
    }

    private void applyEyes(@DrawableRes int resId) {
        overlayEyes.setImageResource(resId);
    }

    private void applyNose(@DrawableRes int resId) {
        overlayNose.setImageResource(resId);
    }

    private void applyLips(@DrawableRes int resId) {
        overlayLips.setImageResource(resId);
    }

    private void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private enum Category { HAIR, EYES, NOSE, LIPS }
}
