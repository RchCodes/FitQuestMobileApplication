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
    private ImageView overlayHead, overlayHair, overlayEyes, overlayNose, overlayLips;

    // Gender icons
    private ImageView maleIcon, femaleIcon;

    // Class icons
    private ImageView btnWarrior, btnRogue, btnTank;
    private ImageView selectedClassIcon = null;

    // Username field
    private EditText editAvatarUsername;

    // Grids
    private GridLayout gridHead, gridHair, gridEyes, gridNose, gridLips;

    // Tabs
    private ImageView tabHead, tabHair, tabEyes, tabNose, tabLips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_creation);

        // base + overlays
        baseBody = findViewById(R.id.baseBody);
        overlayHead = findViewById(R.id.headLayer);
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
        tabHead = findViewById(R.id.tab_head);
        tabHair = findViewById(R.id.tab_hair);
        tabEyes = findViewById(R.id.tab_eyes);
        tabNose = findViewById(R.id.tab_nose);
        tabLips = findViewById(R.id.tab_lips);

        // grids
        gridHead = findViewById(R.id.grid_head_group);
        gridEyes = findViewById(R.id.gridEyes);
        // (optional: define gridHair, gridNose, gridLips if you add them later)

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
        tabHead.setOnClickListener(v -> showGrid(gridHead));
        tabEyes.setOnClickListener(v -> {
            showGrid(gridEyes);
            setupEyeColors(gridEyes); // fill with color boxes
        });

        // Wire head grid only (others optional)
        wireGridItems(gridHead, Category.HEAD);

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
        if (gridHead != null) gridHead.setVisibility(View.GONE);
        if (gridEyes != null) gridEyes.setVisibility(View.GONE);
        // (later: hide gridHair, gridNose, gridLips too)

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
                            case HEAD: applyHead(resId); break;
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
            int id = getResources().getIdentifier(tagStr, "drawable", getPackageName());
            return id;
        }
        return 0;
    }

    private void applyHead(@DrawableRes int resId) {
        overlayHead.setImageResource(resId);
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

    private enum Category { HEAD, HAIR, EYES, NOSE, LIPS }
}
