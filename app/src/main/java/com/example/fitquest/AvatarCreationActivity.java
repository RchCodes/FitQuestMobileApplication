package com.example.fitquest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// import androidx.annotation.DrawableRes; // Keep if direct drawable manipulation is re-added
import androidx.appcompat.app.AppCompatActivity;

// import android.graphics.Color; // No longer used by setupEyeColors
// import android.widget.GridLayout; // No longer used

public class AvatarCreationActivity extends AppCompatActivity {

    // Character Image
    private ImageView imgCharacter;
    // private ImageView overlayHair, overlayEyes, overlayNose, overlayLips; // Old overlay system

    // Gender Buttons
    private Button btnMale, btnFemale;

    // Class Buttons
    private Button btnWarrior, btnRogue, btnTank;
    private Button selectedClassButton = null; // Changed from ImageView

    // Username field
    private EditText etUsername; // Renamed from editAvatarUsername

    // New Customization UI Elements
    private ImageButton btnHairLeft, btnHairRight;
    private TextView tvHair;
    private ImageButton btnEyesLeft, btnEyesRight;
    private TextView tvEyes;
    private ImageButton btnNoseLeft, btnNoseRight;
    private TextView tvNose;
    private ImageButton btnLipsLeft, btnLipsRight;
    private TextView tvLips;

    // Grids - No longer in the new XML
    // private GridLayout gridHair, gridEyes, gridNose, gridLips;

    // Tabs - No longer in the new XML
    // private ImageView tabHair, tabEyes, tabNose, tabLips;

    // Sample data for customization cycling (you'll need to expand this)
    private String[] hairStyles = {"Hair #1", "Hair #2", "Hair #3"};
    // Add arrays for eyes, nose, lips styles and corresponding drawables if needed
    private int currentHairIndex = 0;
    // Add indices for eyes, nose, lips

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_creation);

        // Character Image
        imgCharacter = findViewById(R.id.imgCharacter); // Renamed from baseBody
        // overlayHair = findViewById(R.id.hairLayer); // Old
        // overlayEyes = findViewById(R.id.eyesLayer); // Old
        // overlayNose = findViewById(R.id.noseLayer); // Old
        // overlayLips = findViewById(R.id.lipsLayer); // Old

        // Username field
        etUsername = findViewById(R.id.etUsername); // Renamed

        // Gender Buttons
        btnMale = findViewById(R.id.btnMale);       // Renamed and type changed
        btnFemale = findViewById(R.id.btnFemale);   // Renamed and type changed

        // Class Buttons
        btnWarrior = findViewById(R.id.btnWarrior); // Type changed
        btnRogue = findViewById(R.id.btnRogue);   // Type changed
        btnTank = findViewById(R.id.btnTank);     // Type changed

        // New Customization UI Elements
        btnHairLeft = findViewById(R.id.btnHairLeft);
        btnHairRight = findViewById(R.id.btnHairRight);
        tvHair = findViewById(R.id.tvHair);

        btnEyesLeft = findViewById(R.id.btnEyesLeft);
        btnEyesRight = findViewById(R.id.btnEyesRight);
        tvEyes = findViewById(R.id.tvEyes);

        btnNoseLeft = findViewById(R.id.btnNoseLeft);
        btnNoseRight = findViewById(R.id.btnNoseRight);
        tvNose = findViewById(R.id.tvNose);

        btnLipsLeft = findViewById(R.id.btnLipsLeft);
        btnLipsRight = findViewById(R.id.btnLipsRight);
        tvLips = findViewById(R.id.tvLips);


        // Tabs - No longer in the new XML
        // tabHair = findViewById(R.id.tab_hair);
        // ...

        // Grids - No longer in the new XML
        // gridHair = findViewById(R.id.gridHair);
        // ...

        // Gender toggle
        btnMale.setOnClickListener(v -> {
            // imgCharacter.setImageResource(R.drawable.male_base_drawable); // Example: if you have a male base
            showToast("Male selected");
            // Potentially update character appearance or a data model
        });

        btnFemale.setOnClickListener(v -> {
            // imgCharacter.setImageResource(R.drawable.female_base_drawable); // Example: if you have a female base
            showToast("Female selected");
            // Potentially update character appearance or a data model
        });

        // Class selection
        View.OnClickListener classClickListener = v -> setSelectedClass((Button) v); // Type cast to Button
        btnWarrior.setOnClickListener(classClickListener);
        btnRogue.setOnClickListener(classClickListener);
        btnTank.setOnClickListener(classClickListener);

        // Tab clicks - No longer in the new XML
        // tabHair.setOnClickListener(v -> showGrid(gridHair));
        // ...

        // Wire sample grid - No longer in the new XML
        // wireGridItems(gridHair, Category.HAIR);

        // Setup for new customization UI
        setupCustomizationControls();


        // Create button
        findViewById(R.id.btnCreate).setOnClickListener(v -> { // ID updated
            String username = etUsername.getText().toString().trim();

            if (username.isEmpty()) {
                showToast("Please enter a username");
            } else {
                // TODO: Save avatar data (username, class, gender, customizations)
                showToast("Avatar created for: " + username);
                // Example: Intent to next activity or finish
            }
        });

        // Initialize display for customization text
        updateCustomizationText();
    }

    private void setupCustomizationControls() {
        // Hair
        btnHairLeft.setOnClickListener(v -> {
            currentHairIndex = (currentHairIndex - 1 + hairStyles.length) % hairStyles.length;
            updateCustomizationText();
            // TODO: Update character hair appearance (e.g., imgCharacter.setHairDrawable(hairDrawables[currentHairIndex]))
            showToast("Hair: " + hairStyles[currentHairIndex]);
        });
        btnHairRight.setOnClickListener(v -> {
            currentHairIndex = (currentHairIndex + 1) % hairStyles.length;
            updateCustomizationText();
            // TODO: Update character hair appearance
            showToast("Hair: " + hairStyles[currentHairIndex]);
        });

        // Eyes - Placeholder listeners
        btnEyesLeft.setOnClickListener(v -> showToast("Eyes Left Clicked"));
        btnEyesRight.setOnClickListener(v -> showToast("Eyes Right Clicked"));

        // Nose - Placeholder listeners
        btnNoseLeft.setOnClickListener(v -> showToast("Nose Left Clicked"));
        btnNoseRight.setOnClickListener(v -> showToast("Nose Right Clicked"));

        // Lips - Placeholder listeners
        btnLipsLeft.setOnClickListener(v -> showToast("Lips Left Clicked"));
        btnLipsRight.setOnClickListener(v -> showToast("Lips Right Clicked"));
    }

    private void updateCustomizationText() {
        if (tvHair != null && hairStyles.length > 0) {
            tvHair.setText(hairStyles[currentHairIndex]);
        }
        // tvEyes.setText(eyeStyles[currentEyeIndex]); // etc. for other categories
    }


    /** Show only the selected grid, hide others - No longer used by new XML */
    /*
    private void showGrid(GridLayout target) {
        if (gridHair != null) gridHair.setVisibility(View.GONE);
        if (gridEyes != null) gridEyes.setVisibility(View.GONE);
        if (gridNose != null) gridNose.setVisibility(View.GONE);
        if (gridLips != null) gridLips.setVisibility(View.GONE);

        if (target != null) target.setVisibility(View.VISIBLE);
    }
    */

    /** Create eye color boxes dynamically - No longer used by new XML */
    /*
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
                // overlayEyes.setColorFilter(color); // apply tint // Old
                showToast("Eye color changed");
            });

            grid.addView(box);
        }
    }
    */

    private void setSelectedClass(Button btn) { // Parameter type changed to Button
        if (selectedClassButton != null) {
            selectedClassButton.setAlpha(1.0f); // Reset previous selection
        }
        selectedClassButton = btn;
        if (selectedClassButton != null) {
            selectedClassButton.setAlpha(0.6f); // Indicate current selection
            // You might want to use a more robust selection indication, e.g., changing background via selector
            String selectedClassName = btn.getText().toString(); // Get class name from button text
            showToast(selectedClassName + " class selected");

            // Example of how you might change the character image based on class:
            // if (btn.getId() == R.id.btnWarrior) {
            //     imgCharacter.setImageResource(R.drawable.char_warrior);
            // } else if (btn.getId() == R.id.btnRogue) {
            //     imgCharacter.setImageResource(R.drawable.char_rogue); // Assuming you have these drawables
            // } else if (btn.getId() == R.id.btnTank) {
            //     imgCharacter.setImageResource(R.drawable.char_tank);   // Assuming you have these drawables
            // }
        }
    }

    /** Wires click listeners to items in a grid - No longer used by new XML */
    /*
    private enum Category { HAIR, EYES, NOSE, LIPS } // Old enum

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
    */

    /** Gets drawable ID from ImageView tag - Potentially no longer needed */
    /*
    private int getDrawableIdFromImageView(ImageView iv) {
        Object tag = iv.getTag();
        if (tag instanceof String) {
            String tagStr = (String) tag;
            return getResources().getIdentifier(tagStr, "drawable", getPackageName());
        }
        return 0;
    }
    */

    /** Apply cosmetic changes - No longer used with new XML structure */
    /*
    private void applyHair(@DrawableRes int resId) {
        // overlayHair.setImageResource(resId); // Old
    }

    private void applyEyes(@DrawableRes int resId) {
        // overlayEyes.setImageResource(resId); // Old
    }

    private void applyNose(@DrawableRes int resId) {
        // overlayNose.setImageResource(resId); // Old
    }

    private void applyLips(@DrawableRes int resId) {
        // overlayLips.setImageResource(resId); // Old
    }
    */

    private void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
