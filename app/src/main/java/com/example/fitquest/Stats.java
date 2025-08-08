package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Stats {

    private final Dialog dialog;

    private int freePhysiquePoints = 1;
    private int freeAttributePoints = 1;

    private ProgressBar barArms, barLegs, barChest, barBack;
    private TextView txtStrength, txtEndurance, txtAgility, txtFlexibility, txtStamina;
    private TextView physiqueTitle, attributesTitle;

    public Stats(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.stats, null);

        // Dialog setup
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // PHYSIQUE setup
        physiqueTitle = popupView.findViewById(R.id.physique_title);
        barArms = popupView.findViewById(R.id.bar_arms);
        barLegs = popupView.findViewById(R.id.bar_legs);
        barChest = popupView.findViewById(R.id.bar_chest);
        barBack = popupView.findViewById(R.id.bar_back);
        setupPhysiqueButton(popupView.findViewById(R.id.plus_arms), barArms, "Arms");
        setupPhysiqueButton(popupView.findViewById(R.id.plus_legs), barLegs, "Legs");
        setupPhysiqueButton(popupView.findViewById(R.id.plus_chest), barChest, "Chest");
        setupPhysiqueButton(popupView.findViewById(R.id.plus_back), barBack, "Back");

        // ATTRIBUTES setup
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

        // APPLY button
        Button apply = popupView.findViewById(R.id.apply_button);
        apply.setOnClickListener(v -> Toast.makeText(context, "Stats Applied!", Toast.LENGTH_SHORT).show());

        // CLOSE button
        Button close = popupView.findViewById(R.id.close_button);
        if (close != null) {
            close.setOnClickListener(v -> dialog.dismiss());
        }

        updateTitles();
    }

    private void setupPhysiqueButton(Button button, ProgressBar bar, String label) {
        button.setOnClickListener(v -> {
            if (freePhysiquePoints > 0) {
                int current = bar.getProgress();
                if (current < bar.getMax()) {
                    bar.setProgress(current + 1);
                    freePhysiquePoints--;
                    updateTitles();
                } else {
                    Toast.makeText(button.getContext(), label + " is maxed out", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(button.getContext(), "No physique points left", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAttributeButton(Button button, TextView textView, String label) {
        button.setOnClickListener(v -> {
            if (freeAttributePoints > 0) {
                int current = Integer.parseInt(textView.getText().toString());
                textView.setText(String.valueOf(current + 1));
                freeAttributePoints--;
                updateTitles();
            } else {
                Toast.makeText(button.getContext(), "No attribute points left", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTitles() {
        physiqueTitle.setText("PHYSIQUE: 99    " + freePhysiquePoints);
        attributesTitle.setText("ATTRIBUTES: 99    " + freeAttributePoints);
    }

    public void show() {
        dialog.show();
    }
}
