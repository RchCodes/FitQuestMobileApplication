package com.example.fitquest;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class CharacterStats {

    private final Dialog dialog;
    private final AvatarModel avatar;       // original avatar
    private final AvatarModel tempAvatar;   // temporary copy for modifications

    // UI elements
    private TextView txtArm, txtLeg, txtChest, txtBack;
    private TextView txtStrength, txtEndurance, txtAgility, txtFlexibility, txtStamina;
    private TextView physiqueTitle, attributesTitle;

    public CharacterStats(Activity activity, AvatarModel avatar) {
        this.avatar = avatar;

        // Create a temporary copy for modifications
        this.tempAvatar = new AvatarModel(avatar);

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

        // Apply button
        View apply = popupView.findViewById(R.id.apply_button);
        if (apply != null) apply.setOnClickListener(v -> {
            // Copy temp stats to the original avatar
            avatar.copyFrom(tempAvatar);
            AvatarManager.saveAvatarOffline(activity, avatar);
            AvatarManager.saveAvatarOnline(avatar);
            Toast.makeText(activity, "Stats Applied!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Close button (reverts changes)
        View close = popupView.findViewById(R.id.close_button);
        if (close != null) close.setOnClickListener(v -> {
            // Reset tempAvatar to original in case of reopening
            tempAvatar.copyFrom(avatar);
            dialog.dismiss();
        });

        updateUI();
    }

    /** Setup physique increment buttons */
    private void setupPhysiqueButton(View button, String type) {
        if (button == null) return;
        button.setOnClickListener(v -> {
            if (tempAvatar.getFreePhysiquePoints() <= 0) {
                Toast.makeText(dialog.getContext(), "No physique points left", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (type) {
                case "arm": tempAvatar.addArmPoints(1); break;
                case "leg": tempAvatar.addLegPoints(1); break;
                case "chest": tempAvatar.addChestPoints(1); break;
                case "back": tempAvatar.addBackPoints(1); break;
            }
            tempAvatar.addFreePhysiquePoints(-1);
            updateUI();
        });
    }

    /** Setup attribute increment buttons */
    private void setupAttributeButton(View button, String type) {
        if (button == null) return;
        button.setOnClickListener(v -> {
            if (tempAvatar.getFreeAttributePoints() <= 0) {
                Toast.makeText(dialog.getContext(), "No attribute points left", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (type) {
                case "strength": tempAvatar.addStrength(1); break;
                case "endurance": tempAvatar.addEndurance(1); break;
                case "agility": tempAvatar.addAgility(1); break;
                case "flexibility": tempAvatar.addFlexibility(1); break;
                case "stamina": tempAvatar.addStamina(1); break;
            }
            tempAvatar.addFreeAttributePoints(-1);
            updateUI();
        });
    }

    /** Refresh UI to display temporary avatar stats */
    private void updateUI() {
        txtArm.setText(String.valueOf(tempAvatar.getArmPoints()));
        txtLeg.setText(String.valueOf(tempAvatar.getLegPoints()));
        txtChest.setText(String.valueOf(tempAvatar.getChestPoints()));
        txtBack.setText(String.valueOf(tempAvatar.getBackPoints()));

        txtStrength.setText(String.valueOf(tempAvatar.getStrength()));
        txtEndurance.setText(String.valueOf(tempAvatar.getEndurance()));
        txtAgility.setText(String.valueOf(tempAvatar.getAgility()));
        txtFlexibility.setText(String.valueOf(tempAvatar.getFlexibility()));
        txtStamina.setText(String.valueOf(tempAvatar.getStamina()));

        physiqueTitle.setText("PHYSIQUE    Free: " + tempAvatar.getFreePhysiquePoints());
        attributesTitle.setText("ATTRIBUTES    Free: " + tempAvatar.getFreeAttributePoints());
    }

    /** Show the dialog */
    public void show() {
        tempAvatar.copyFrom(avatar); // reset temporary copy every time dialog opens
        updateUI();
        dialog.show();
    }
}
