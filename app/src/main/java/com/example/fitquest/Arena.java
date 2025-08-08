package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Arena {

    private final Dialog dialog;

    public Arena(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.arena, null);

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // UI Elements
        TextView playerName = popupView.findViewById(R.id.player_name);
        TextView playerLevel = popupView.findViewById(R.id.player_level);
        ImageView avatarImage = popupView.findViewById(R.id.avatar_image);
        ImageView rankIcon = popupView.findViewById(R.id.rank_icon);
        TextView rankLabel = popupView.findViewById(R.id.rank_label);
        Button startCombat = popupView.findViewById(R.id.start_combat);

        // Optional: Set default values
        playerName.setText("CHIAN");
        playerLevel.setText("LV. 99");
        rankLabel.setText("CHAMPION");

        startCombat.setOnClickListener(v ->
                Toast.makeText(context, "Combat Started!", Toast.LENGTH_SHORT).show()
        );
    }

    public void show() {
        dialog.show();
    }
}
