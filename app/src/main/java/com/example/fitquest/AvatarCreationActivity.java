package com.example.fitquest;

import android.os.Bundle;
import android.view.View;
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

    // Grids
    private GridLayout gridHead, gridHair, gridEyes, gridNose, gridLips;

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

        // gender icons
        maleIcon = findViewById(R.id.male_icon);
        femaleIcon = findViewById(R.id.female_icon);

        // class icons
        btnWarrior = findViewById(R.id.btn_warrior);
        btnRogue = findViewById(R.id.btn_rogue);
        btnTank = findViewById(R.id.btn_tank);

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

        // Wire grids
        wireGridItems(gridHead, Category.HEAD);
        wireGridItems(gridHair, Category.HAIR);
        wireGridItems(gridEyes, Category.EYES);
        wireGridItems(gridNose, Category.NOSE);
        wireGridItems(gridLips, Category.LIPS);

        // Create button
        findViewById(R.id.btn_create).setOnClickListener(v ->
                showToast("Create clicked — implement save logic"));
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
