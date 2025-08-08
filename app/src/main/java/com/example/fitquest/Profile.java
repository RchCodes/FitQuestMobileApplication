package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import android.view.ViewGroup;

public class Profile {

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

        // Get buttons
        Button bindButton = popupView.findViewById(R.id.bind_button);
        Button switchButton = popupView.findViewById(R.id.switch_button);

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
