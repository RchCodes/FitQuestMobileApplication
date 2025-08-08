package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Quest {

    private final Dialog dialog;
    private final LinearLayout questList;

    public Quest(Context context) {
        // Inflate quest layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.quest, null);

        // Set up the dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setCancelable(true); // dismiss when clicking outside
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Reference the quest list
        questList = view.findViewById(R.id.quest_list);

        // Add quest items
        addQuest(context, "Execute 10 Push-ups.", 10);
        addQuest(context, "Hold a 10-second Plank.", 1);
        addQuest(context, "Execute 10 Squats.", 10);
        addQuest(context, "Execute 10 Tricep Dips.", 10);
        addQuest(context, "Do 20 Jumping Jacks.", 20);
    }

    private void addQuest(Context context, String title, int maxProgress) {
        View questItem = LayoutInflater.from(context).inflate(R.layout.quest_items, questList, false);

        TextView titleText = questItem.findViewById(R.id.quest_title);
        TextView progressText = questItem.findViewById(R.id.progress_text);
        ProgressBar progressBar = questItem.findViewById(R.id.quest_progress_bar);
        Button doItButton = questItem.findViewById(R.id.do_it_button);

        titleText.setText(title);
        progressBar.setMax(maxProgress);
        progressBar.setProgress(0);
        progressText.setText("0/" + maxProgress);

        doItButton.setOnClickListener(v -> {
            // Simulate camera open
            context.startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

            // Simulate quest progress
            int current = progressBar.getProgress();
            if (current < maxProgress) {
                current++;
                progressBar.setProgress(current);
                progressText.setText(current + "/" + maxProgress);
            }
        });

        questList.addView(questItem);
    }

    public void show() {
        dialog.show();
    }
}
