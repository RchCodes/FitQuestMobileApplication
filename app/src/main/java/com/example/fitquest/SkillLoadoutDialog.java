package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class SkillLoadoutDialog extends Dialog {

    private final AvatarModel avatar;
    private final SkillInfoPopup skillInfoPopup = new SkillInfoPopup();

    private LinearLayout activeContainer, passiveContainer;
    private GridLayout availableGrid;
    private Button saveButton, cancelButton;

    private final SkillModel[] activeSlots = new SkillModel[5];
    private final PassiveSkill[] passiveSlots = new PassiveSkill[2];

    private final int SLOT_SIZE = 120; // px
    private final int SLOT_MARGIN = 8; // px

    public SkillLoadoutDialog(@NonNull Context context, AvatarModel avatar) {
        super(context, com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        this.avatar = avatar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_skill_loadout);

        activeContainer = findViewById(R.id.activeSkillsContainer);
        passiveContainer = findViewById(R.id.passiveSkillsContainer);
        availableGrid = findViewById(R.id.availableSkillsGrid);

        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        loadActiveSlots();
        loadPassiveSlots();
        loadAvailableSkills();

        saveButton.setOnClickListener(v -> {
            // Build non-null active skills list
            List<SkillModel> activeList = new ArrayList<>();
            for (SkillModel s : activeSlots) {
                if (s != null) activeList.add(s);
            }

            // Warn if no active skills equipped
            if (activeList.isEmpty()) {
                Toast.makeText(getContext(), "You must equip at least one active skill.", Toast.LENGTH_SHORT).show();
                return; // stop execution
            }

            // Build non-null passive skills list
            List<PassiveSkill> passiveList = new ArrayList<>();
            for (PassiveSkill p : passiveSlots) {
                if (p != null) passiveList.add(p);
            }

            // Save back to avatar
            avatar.setActiveSkills(activeList);
            avatar.setPassiveSkills(passiveList);

            dismiss();
        });


        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void loadActiveSlots() {
        List<SkillModel> active = avatar.getActiveSkills();
        activeContainer.removeAllViews();

        for (int i = 0; i < 5; i++) {
            ShapeableImageView slot = createSlotView();

            if (i < active.size()) {
                SkillModel skill = active.get(i);
                slot.setImageResource(skill.getIconRes());
                activeSlots[i] = skill;
            } else {
                slot.setImageResource(android.R.color.transparent);
                slot.setAlpha(0.3f);
            }

            int index = i;
            slot.setOnClickListener(v -> {
                if (activeSlots[index] != null) {
                    activeSlots[index] = null;
                    slot.setImageResource(android.R.color.transparent);
                    slot.setAlpha(0.3f);
                }
            });

            // Long press to show info
            slot.setOnLongClickListener(v -> {
                if (activeSlots[index] != null) {
                    skillInfoPopup.show(v, activeSlots[index]);
                }
                return true;
            });


            activeContainer.addView(slot);
        }
    }

    private void loadPassiveSlots() {
        List<PassiveSkill> passives = avatar.getPassiveSkills();
        passiveContainer.removeAllViews();

        for (int i = 0; i < 2; i++) {
            ShapeableImageView slot = createSlotView();

            if (i < passives.size()) {
                PassiveSkill passive = passives.get(i);
                slot.setImageResource(passive.getIconResId());
                passiveSlots[i] = passive;
            } else {
                slot.setImageResource(android.R.color.transparent);
                slot.setAlpha(0.3f);
            }

            int index = i;
            slot.setOnClickListener(null);

            slot.setOnLongClickListener(v -> {
                if (passiveSlots[index] != null) {
                    skillInfoPopup.show(v, passiveSlots[index]);
                }
                return true;
            });

            passiveContainer.addView(slot);
        }
    }

    private void loadAvailableSkills() {
        List<SkillModel> available = avatar.getUnlockedSkills(); // <-- use unlocked skills
        availableGrid.removeAllViews();

        int playerLevel = avatar.getLevel(); // get player level

        for (SkillModel skill : available) {
            if (skill.getLevelUnlock() > playerLevel) continue; // skip if somehow higher than level

            ShapeableImageView slot = createSlotView();
            slot.setImageResource(skill.getIconRes());

            slot.setOnClickListener(v -> {
                if (!canEquipSkill(skill)) return; // skip if cannot equip

                // Add skill to first empty active slot
                for (int i = 0; i < activeSlots.length; i++) {
                    if (activeSlots[i] == null) {
                        activeSlots[i] = skill;
                        updateActiveSlotImage(i, skill.getIconRes());
                        break;
                    }
                }
            });

            // Long press to show info
            slot.setOnLongClickListener(v -> {
                skillInfoPopup.show(v, skill);
                return true;
            });

            availableGrid.addView(slot);
        }
    }

    private boolean canEquipSkill(SkillModel skill) {
        if (skill == null) return false;

        // Prevent duplicates
        for (SkillModel s : activeSlots) {
            if (s != null && s.getId().equals(skill.getId())) {
                return false;
            }
        }

        // Only allow 1 ultimate
        if (skill.isUltimate()) {
            for (SkillModel s : activeSlots) {
                if (s != null && s.isUltimate()) {
                    return false;
                }
            }
        }

        return true;
    }


    private void updateActiveSlotImage(int index, int resId) {
        ShapeableImageView slot = (ShapeableImageView) activeContainer.getChildAt(index);
        if (slot != null) {
            slot.setImageResource(resId);
            slot.setAlpha(1f);
        }
    }

    private ShapeableImageView createSlotView() {
        ShapeableImageView slot = new ShapeableImageView(getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(SLOT_SIZE, SLOT_SIZE);
        params.setMargins(SLOT_MARGIN, SLOT_MARGIN, SLOT_MARGIN, SLOT_MARGIN);
        slot.setLayoutParams(params);

        slot.setScaleType(ShapeableImageView.ScaleType.CENTER_CROP);

        // Circular shape + gold stroke
        slot.setShapeAppearanceModel(
                slot.getShapeAppearanceModel()
                        .toBuilder()
                        .setAllCornerSizes(SLOT_SIZE / 2f) // half width = circle
                        .build()
        );
        slot.setStrokeColorResource(android.R.color.holo_orange_light);
        slot.setStrokeWidth(2.5f);

        return slot;
    }
}
