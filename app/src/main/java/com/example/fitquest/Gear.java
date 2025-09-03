package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Gear {
    private final Dialog dialog;

    private static final String PREF_NAME = "FitQuestPrefs";
    private static final String KEY_GENDER = "gender"; // "male" or "female"

    public Gear(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.gear, null);

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Set avatar image from saved gender
        ImageView avatarImage = popupView.findViewById(R.id.avatar_image);
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String gender = prefs.getString(KEY_GENDER, "male");
        int drawableId = "female".equalsIgnoreCase(gender) ? R.drawable.female : R.drawable.male2;
        avatarImage.setImageResource(drawableId);

        Button btnWeapon = popupView.findViewById(R.id.btn_weapon);
        Button btnArmor = popupView.findViewById(R.id.btn_armor);
        Button btnPants = popupView.findViewById(R.id.btn_pants);
        Button btnBoots = popupView.findViewById(R.id.btn_boots);
        Button btnAccessories = popupView.findViewById(R.id.btn_accessories);
        Button btnSave = popupView.findViewById(R.id.btn_save);

        btnWeapon.setOnClickListener(v -> showToast(context, "Weapon clicked"));
        btnArmor.setOnClickListener(v -> showToast(context, "Armor clicked"));
        btnPants.setOnClickListener(v -> showToast(context, "Pants clicked"));
        btnBoots.setOnClickListener(v -> showToast(context, "Boots clicked"));
        btnAccessories.setOnClickListener(v -> showToast(context, "Accessories clicked"));
        btnSave.setOnClickListener(v -> showToast(context, "Saved!"));
    }

    private void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void show() {
        dialog.show();
    }
}
