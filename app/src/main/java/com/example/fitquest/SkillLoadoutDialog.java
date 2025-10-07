package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

public class SkillLoadoutDialog extends Dialog {

    private AvatarModel avatar;
    private LinearLayout activeContainer, passiveContainer;
    private GridLayout availableGrid;
    private SkillModel[] activeSlots = new SkillModel[5]; // max 5
    private ImageView[] activeSlotViews = new ImageView[5];

    public SkillLoadoutDialog(@NonNull Context context, AvatarModel avatar) {
        super(context);
        this.avatar = avatar;

        View root = LayoutInflater.from(context).inflate(R.layout.dialog_skill_loadout, null);
        setContentView(root);

        activeContainer = root.findViewById(R.id.activeSkillsContainer);
        passiveContainer = root.findViewById(R.id.passiveSkillsContainer);
        availableGrid = root.findViewById(R.id.availableSkillsGrid);

        Button saveBtn = root.findViewById(R.id.saveButton);
        Button cancelBtn = root.findViewById(R.id.cancelButton);

        setupActiveSlots(context);
        setupPassiveSkills(context);
        setupAvailableSkills(context);

        saveBtn.setOnClickListener(v -> {
            // Save the skill loadout
            avatar.getActiveSkills().clear(); // reset
            for (SkillModel sm : activeSlots) {
                if (sm != null) avatar.addActiveSkill(sm);
            }
            
            // Save avatar with new skill loadout
            AvatarManager.saveAvatarOffline(context, avatar);
            if (isNetworkAvailable()) {
                AvatarManager.saveAvatarOnline(avatar);
            }
            
            Toast.makeText(context, "Skill loadout saved!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        cancelBtn.setOnClickListener(v -> dismiss());
    }

    private void setupActiveSlots(Context context) {
        List<SkillModel> current = avatar.getActiveSkills();
        activeContainer.removeAllViews();

        for (int i = 0; i < 5; i++) {
            ImageView slot = new ImageView(context);
            slot.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
            slot.setPadding(8, 8, 8, 8);
            slot.setBackgroundResource(R.drawable.skill_slot_border);

            if (i < current.size()) {
                activeSlots[i] = current.get(i);
                slot.setImageResource(current.get(i).getIconRes());
            }

            int index = i;
            slot.setOnClickListener(v -> {
                if (activeSlots[index] != null) {
                    activeSlots[index] = null;
                    slot.setImageResource(android.R.color.transparent);
                    slot.setAlpha(0.3f);
                }
            });

            activeSlotViews[i] = slot;
            activeContainer.addView(slot);
        }
    }

    private void setupPassiveSkills(Context context) {
        passiveContainer.removeAllViews();
        for (PassiveSkill ps : avatar.getPassiveSkills()) {
            ImageView view = new ImageView(context);
            view.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
            view.setPadding(8, 8, 8, 8);
            view.setBackgroundResource(R.drawable.skill_slot_border);
            view.setImageResource(ps.getIconResId());
            passiveContainer.addView(view);
        }
    }

    private void setupAvailableSkills(Context context) {
        availableGrid.removeAllViews();
        List<SkillModel> all = SkillRepository.getSkillsForClass(avatar.getClassType());

        for (SkillModel sm : all) {
            ImageView skillIcon = new ImageView(context);
            skillIcon.setLayoutParams(new GridLayout.LayoutParams());
            skillIcon.setAdjustViewBounds(true);
            skillIcon.setPadding(8, 8, 8, 8);

            // unlocked check
            if (avatar.getLevel() >= sm.getLevelUnlock()) {
                skillIcon.setImageResource(sm.getIconRes());

                skillIcon.setOnClickListener(v -> {
                    if (sm.isUltimate() && avatar.hasUltimateSkill()) {
                        Toast.makeText(context, "You already have an Ultimate equipped!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // find free slot
                    for (int i = 0; i < activeSlots.length; i++) {
                        if (activeSlots[i] == null) {
                            activeSlots[i] = sm;
                            activeSlotViews[i].setImageResource(sm.getIconRes());
                            break;
                        }
                    }
                });
            } else {
                skillIcon.setImageResource(R.drawable.lock_2); // locked skill placeholder
                skillIcon.setAlpha(0.5f);
            }

            availableGrid.addView(skillIcon);
        }
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
