package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Store {

    private static final String PREF_NAME = "FitQuestPrefs";
    private static final String KEY_GENDER = "gender"; // "male" or "female"

    private final Dialog dialog;

    private final int[] itemPrices = {999, 1499, 2499, 4999};
    private final int[] itemIcons = {
            R.drawable.lock,
            R.drawable.lock,
            R.drawable.lock,
            R.drawable.lock
    };

    public Store(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.store, null);

        // Dialog setup
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Character preview based on gender
        ImageView characterPreview = view.findViewById(R.id.character_preview);
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String gender = prefs.getString(KEY_GENDER, "male");
        int drawableId = "female".equalsIgnoreCase(gender) ? R.drawable.female : R.drawable.male2;
        characterPreview.setImageResource(drawableId);

        // Populate store items
        LinearLayout storeItemsContainer = view.findViewById(R.id.store_container);

        for (int i = 0; i < itemPrices.length; i++) {
            View itemView = inflater.inflate(R.layout.store_item, storeItemsContainer, false);

            ImageView lockIcon = itemView.findViewById(R.id.lock_icon);
            lockIcon.setImageResource(itemIcons[i]);

            ImageView currencyIcon = itemView.findViewById(R.id.currency_icon);
            currencyIcon.setImageResource(R.drawable.currency);

            TextView priceText = itemView.findViewById(R.id.item_price);
            priceText.setText(String.valueOf(itemPrices[i]));
            priceText.setTag(itemPrices[i]); // Optional: for logic later

            storeItemsContainer.addView(itemView);
        }
    }

    public void show() {
        dialog.show();
    }
}
