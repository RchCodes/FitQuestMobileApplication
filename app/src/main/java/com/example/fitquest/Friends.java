package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Friends {

    private final Dialog dialog;

    public Friends(Context context) {
        // Inflate the friends layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.friends, null);

        // Set up the dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Find the container for friend items
        LinearLayout friendList = popupView.findViewById(R.id.friends_list);

        // Sample data
        String[][] sampleFriends = {
                {"TRIPPI", "LVL. 99", "SPARTAN"},
                {"TRALALELO", "LVL. 99", "IMMORTALS"},
                {"TUNGSAHUR", "LVL. 99", "HOPLITES"}
        };

        // Populate the list
        for (String[] friend : sampleFriends) {
            View friendItem = inflater.inflate(R.layout.friends_items, friendList, false);

            ((TextView) friendItem.findViewById(R.id.friend_name)).setText(friend[0]);
            ((TextView) friendItem.findViewById(R.id.friend_level)).setText(friend[1]);
            ((TextView) friendItem.findViewById(R.id.friend_team)).setText(friend[2]);

            friendList.addView(friendItem);
        }
    }

    public void show() {
        dialog.show();
    }
}
