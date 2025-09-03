package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;

public class Profile {

    private static final String PREF_NAME = "FitQuestPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_GENDER = "gender"; // "male" or "female"

    private final Dialog dialog;

    public Profile(Context context) {
        // Inflate layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.profile, null);

        // Create dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true); // ✅ dismiss on outside tap
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Bind views
        ImageView profileImage = popupView.findViewById(R.id.profile_image);
        TextView usernameView = popupView.findViewById(R.id.username);
        Button bindButton = popupView.findViewById(R.id.bind_button);
        Button switchButton = popupView.findViewById(R.id.switch_button);

        // Load saved data
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "Player");
        String gender = prefs.getString(KEY_GENDER, "male");

        // Set username
        usernameView.setText(username);

        // Set profile image based on gender
        if ("female".equalsIgnoreCase(gender)) {
            profileImage.setImageResource(R.drawable.female);
        } else {
            profileImage.setImageResource(R.drawable.male2);
        }

        // Button click actions
        bindButton.setOnClickListener(v ->
                Toast.makeText(context, "Bind Account Clicked", Toast.LENGTH_SHORT).show()
        );

        switchButton.setOnClickListener(v ->
                Toast.makeText(context, "Switch Account Clicked", Toast.LENGTH_SHORT).show()
        );
    }

    public void show() {
        dialog.show();
    }
}
