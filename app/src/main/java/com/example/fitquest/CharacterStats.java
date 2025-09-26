package com.example.fitquest;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class CharacterStats {

    private final Dialog dialog;
    private final AvatarModel avatar;

    // UI elements
    private TextView txtArm, txtLeg, txtChest, txtBack;
    private TextView txtStrength, txtEndurance, txtAgility, txtFlexibility, txtStamina;
    private TextView physiqueTitle, attributesTitle;

    public CharacterStats(Activity activity, AvatarModel avatar) {
        this.avatar = avatar;

        View popupView = LayoutInflater.from(activity).inflate(R.layout.character_stats, null);

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Titles
        physiqueTitle = popupView.findViewById(R.id.physique_title);
        attributesTitle = popupView.findViewById(R.id.attributes_title);

        // Physique TextViews
        txtArm = popupView.findViewById(R.id.txt_arms_value);
        txtLeg = popupView.findViewById(R.id.txt_legs_value);
        txtChest = popupView.findViewById(R.id.txt_chest_value);
        txtBack = popupView.findViewById(R.id.txt_back_value);

        setupPhysiqueButton(popupView.findViewById(R.id.plus_arms), "arm");
        setupPhysiqueButton(popupView.findViewById(R.id.plus_legs), "leg");
        setupPhysiqueButton(popupView.findViewById(R.id.plus_chest), "chest");
        setupPhysiqueButton(popupView.findViewById(R.id.plus_back), "back");

        // Attribute TextViews
        txtStrength = popupView.findViewById(R.id.txt_strength);
        txtEndurance = popupView.findViewById(R.id.txt_endurance);
        txtAgility = popupView.findViewById(R.id.txt_agility);
        txtFlexibility = popupView.findViewById(R.id.txt_flexibility);
        txtStamina = popupView.findViewById(R.id.txt_stamina);

        setupAttributeButton(popupView.findViewById(R.id.plus_strength), "strength");
        setupAttributeButton(popupView.findViewById(R.id.plus_endurance), "endurance");
        setupAttributeButton(popupView.findViewById(R.id.plus_agility), "agility");
        setupAttributeButton(popupView.findViewById(R.id.plus_flexibility), "flexibility");
        setupAttributeButton(popupView.findViewById(R.id.plus_stamina), "stamina");

        // Apply & Close buttons
        View apply = popupView.findViewById(R.id.apply_button);
        if (apply != null) apply.setOnClickListener(v -> {
            AvatarManager.saveAvatarOffline(activity, avatar);
            AvatarManager.saveAvatarOnline(avatar);
            Toast.makeText(activity, "Stats Applied!", Toast.LENGTH_SHORT).show();
        });

        View close = popupView.findViewById(R.id.close_button);
        if (close != null) close.setOnClickListener(v -> dialog.dismiss());

        updateUI();
    }

    private void setupPhysiqueButton(View button, String type) {
        if (button == null) return;
        button.setOnClickListener(v -> {
            if (avatar.getFreePhysiquePoints() <= 0) {
                Toast.makeText(dialog.getContext(), "No physique points left", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (type) {
                case "arm": avatar.addArmPoints(1); break;
                case "leg": avatar.addLegPoints(1); break;
                case "chest": avatar.addChestPoints(1); break;
                case "back": avatar.addBackPoints(1); break;
            }
            avatar.addFreePhysiquePoints(-1);
            updateUI();
        });
    }

    private void setupAttributeButton(View button, String type) {
        if (button == null) return;
        button.setOnClickListener(v -> {
            if (avatar.getFreeAttributePoints() <= 0) {
                Toast.makeText(dialog.getContext(), "No attribute points left", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (type) {
                case "strength": avatar.addStrength(1); break;
                case "endurance": avatar.addEndurance(1); break;
                case "agility": avatar.addAgility(1); break;
                case "flexibility": avatar.addFlexibility(1); break;
                case "stamina": avatar.addStamina(1); break;
            }
            avatar.addFreeAttributePoints(-1);
            updateUI();
        });
    }

    /** Refresh all UI fields from avatar */
    private void updateUI() {
        txtArm.setText(String.valueOf(avatar.getArmPoints()));
        txtLeg.setText(String.valueOf(avatar.getLegPoints()));
        txtChest.setText(String.valueOf(avatar.getChestPoints()));
        txtBack.setText(String.valueOf(avatar.getBackPoints()));

        txtStrength.setText(String.valueOf(avatar.getStrength()));
        txtEndurance.setText(String.valueOf(avatar.getEndurance()));
        txtAgility.setText(String.valueOf(avatar.getAgility()));
        txtFlexibility.setText(String.valueOf(avatar.getFlexibility()));
        txtStamina.setText(String.valueOf(avatar.getStamina()));

        physiqueTitle.setText("PHYSIQUE    Free: " + avatar.getFreePhysiquePoints());
        attributesTitle.setText("ATTRIBUTES    Free: " + avatar.getFreeAttributePoints());
    }

    /** Show dialog */
    public void show() {
        updateUI();
        dialog.show();
    }
}
