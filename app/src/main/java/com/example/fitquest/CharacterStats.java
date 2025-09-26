package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CharacterStats {

    private final Dialog dialog;
    private final Context context;

    private int freePhysiquePoints = 1;
    private int freeAttributePoints = 1;

    private int armPoints = 0;
    private int legPoints = 0;
    private int chestPoints = 0;
    private int backPoints = 0;

    private ProgressBar barArms, barLegs, barChest, barBack;
    private TextView txtStrength, txtEndurance, txtAgility, txtFlexibility, txtStamina;
    private TextView physiqueTitle, attributesTitle;

    public CharacterStats(Context context) {
        this.context = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.character_stats, null);

        // Dialog setup
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // --- PHYSIQUE ---
        physiqueTitle = popupView.findViewById(R.id.physique_title);
        barArms = popupView.findViewById(R.id.bar_arms);
        barLegs = popupView.findViewById(R.id.bar_legs);
        barChest = popupView.findViewById(R.id.bar_chest);
        barBack = popupView.findViewById(R.id.bar_back);

        setupPhysiqueButton(popupView.findViewById(R.id.plus_arms), barArms, "Arms", "arm");
        setupPhysiqueButton(popupView.findViewById(R.id.plus_legs), barLegs, "Legs", "leg");
        setupPhysiqueButton(popupView.findViewById(R.id.plus_chest), barChest, "Chest", "chest");
        setupPhysiqueButton(popupView.findViewById(R.id.plus_back), barBack, "Back", "back");

        // --- ATTRIBUTES ---
        attributesTitle = popupView.findViewById(R.id.attributes_title);
        txtStrength = popupView.findViewById(R.id.txt_strength);
        txtEndurance = popupView.findViewById(R.id.txt_endurance);
        txtAgility = popupView.findViewById(R.id.txt_agility);
        txtFlexibility = popupView.findViewById(R.id.txt_flexibility);
        txtStamina = popupView.findViewById(R.id.txt_stamina);

        setupAttributeButton(popupView.findViewById(R.id.plus_strength), txtStrength, "Strength");
        setupAttributeButton(popupView.findViewById(R.id.plus_endurance), txtEndurance, "Endurance");
        setupAttributeButton(popupView.findViewById(R.id.plus_agility), txtAgility, "Agility");
        setupAttributeButton(popupView.findViewById(R.id.plus_flexibility), txtFlexibility, "Flexibility");
        setupAttributeButton(popupView.findViewById(R.id.plus_stamina), txtStamina, "Stamina");

        loadProfile();

        // Buttons
        Button apply = popupView.findViewById(R.id.apply_button);
        if (apply != null) apply.setOnClickListener(v -> {
            saveStats();
            Toast.makeText(context, "Stats Applied!", Toast.LENGTH_SHORT).show();
        });

        Button close = popupView.findViewById(R.id.close_button);
        if (close != null) close.setOnClickListener(v -> dialog.dismiss());

        updateTitles();
    }

    private void setupPhysiqueButton(Button button, ProgressBar bar, String label, String bodyPart) {
        button.setOnClickListener(v -> {
            if (freePhysiquePoints <= 0) {
                Toast.makeText(context, "No physique points left", Toast.LENGTH_SHORT).show();
                return;
            }
            int current = bar.getProgress();
            if (current >= bar.getMax()) {
                Toast.makeText(context, label + " is maxed out", Toast.LENGTH_SHORT).show();
                return;
            }
            bar.setProgress(current + 1);
            freePhysiquePoints--;
            // Update internal fields
            switch (bodyPart) {
                case "arm": armPoints++; break;
                case "leg": legPoints++; break;
                case "chest": chestPoints++; break;
                case "back": backPoints++; break;
            }
            updateTitles();
        });
    }

    private void setupAttributeButton(Button button, TextView textView, String label) {
        button.setOnClickListener(v -> {
            if (freeAttributePoints <= 0) {
                Toast.makeText(context, "No attribute points left", Toast.LENGTH_SHORT).show();
                return;
            }
            int current = Integer.parseInt(textView.getText().toString());
            textView.setText(String.valueOf(current + 1));
            freeAttributePoints--;
            updateTitles();
        });
    }

    public void addArmPoints(int points) { armPoints += points; saveStats(); }
    public void addLegPoints(int points) { legPoints += points; saveStats(); }
    public void addChestPoints(int points) { chestPoints += points; saveStats(); }
    public void addBackPoints(int points) { backPoints += points; saveStats(); }

    public void addPhysiquePoints(int points) { freePhysiquePoints += points; updateTitles(); }
    public void addAttributePoints(int points) { freeAttributePoints += points; updateTitles(); }

    public int getArmPoints() { return armPoints; }
    public int getLegPoints() { return legPoints; }
    public int getChestPoints() { return chestPoints; }
    public int getBackPoints() { return backPoints; }

    private void loadProfile() {
        SharedPreferences prefs = context.getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE);
        armPoints = prefs.getInt("arms", 0);
        legPoints = prefs.getInt("legs", 0);
        chestPoints = prefs.getInt("chest", 0);
        backPoints = prefs.getInt("back", 0);

        barArms.setProgress(armPoints);
        barLegs.setProgress(legPoints);
        barChest.setProgress(chestPoints);
        barBack.setProgress(backPoints);

        txtStrength.setText(String.valueOf(prefs.getInt("strength", 0)));
        txtEndurance.setText(String.valueOf(prefs.getInt("endurance", 0)));
        txtAgility.setText(String.valueOf(prefs.getInt("agility", 0)));
        txtFlexibility.setText(String.valueOf(prefs.getInt("flexibility", 0)));
        txtStamina.setText(String.valueOf(prefs.getInt("stamina", 0)));
    }

    private void updateTitles() {
        physiqueTitle.setText("PHYSIQUE    Free: " + freePhysiquePoints);
        attributesTitle.setText("ATTRIBUTES    Free: " + freeAttributePoints);
    }

    private void saveStats() {
        SharedPreferences prefs = context.getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("arms", armPoints);
        editor.putInt("legs", legPoints);
        editor.putInt("chest", chestPoints);
        editor.putInt("back", backPoints);

        editor.putInt("strength", Integer.parseInt(txtStrength.getText().toString()));
        editor.putInt("endurance", Integer.parseInt(txtEndurance.getText().toString()));
        editor.putInt("agility", Integer.parseInt(txtAgility.getText().toString()));
        editor.putInt("flexibility", Integer.parseInt(txtFlexibility.getText().toString()));
        editor.putInt("stamina", Integer.parseInt(txtStamina.getText().toString()));

        editor.apply();
    }

    public void show() {
        dialog.show();
    }
}
