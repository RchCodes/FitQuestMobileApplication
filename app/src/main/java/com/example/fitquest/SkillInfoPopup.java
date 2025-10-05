package com.example.fitquest;

import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Locale;

public class SkillInfoPopup {

    private PopupWindow popupWindow;

    // --- Active skill
    public void show(View anchor, SkillModel skill) {
        if (anchor == null || skill == null) return;
        View content = LayoutInflater.from(anchor.getContext())
                .inflate(R.layout.dialog_skill_info, null);

        // Common
        ShapeableImageView icon = content.findViewById(R.id.skillIcon);
        TextView name = content.findViewById(R.id.skillName);
        TextView type = content.findViewById(R.id.skillType);
        TextView desc = content.findViewById(R.id.skillDescription);
        TextView scaling = content.findViewById(R.id.skillScaling);
        TextView abCost = content.findViewById(R.id.skillABImpact);
        TextView cooldown = content.findViewById(R.id.skillCooldown);
        TextView effect = content.findViewById(R.id.skillEffect);
        TextView unlock = content.findViewById(R.id.skillUnlock);
        TextView passiveDetails = content.findViewById(R.id.passiveDetails);

        // populate active fields
        if (skill.getIconRes() != 0) icon.setImageResource(skill.getIconRes());
        name.setText(skill.getName());
        type.setText("Type: " + (skill.getType() == null ? "Unknown" : skill.getType().name()));
        desc.setText("Description: " + (skill.getDescription() == null ? "—" : skill.getDescription()));

        // scaling: build a short row from available scaling floats
        String scalingText = String.format(Locale.US,
                "Scaling - STR: %.2f, END: %.2f, AGI: %.2f",
                skill.getStrScaling(), skill.getEndScaling(), skill.getAgiScaling());
        scaling.setText(scalingText);

        abCost.setText("AB Cost: " + skill.getAbCost());
        cooldown.setText("Cooldown: " + skill.getCooldown());
        // effect: use description + effects list if any
        List<?> effects = skill.getEffects();
        String effectsText = (effects == null || effects.isEmpty()) ? skill.getDescription() : effects.toString();
        effect.setText("Effect: " + (effectsText == null ? "—" : effectsText));

        unlock.setText("Level Unlock: " + skill.getLevelUnlock());

        // hide passive-only view
        passiveDetails.setVisibility(View.GONE);

        showPopupAtAnchor(anchor, content);
    }

    // --- Passive skill
    public void show(View anchor, PassiveSkill passive) {
        if (anchor == null || passive == null) return;
        View content = LayoutInflater.from(anchor.getContext())
                .inflate(R.layout.dialog_skill_info, null);

        ShapeableImageView icon = content.findViewById(R.id.skillIcon);
        TextView name = content.findViewById(R.id.skillName);
        TextView type = content.findViewById(R.id.skillType);
        TextView desc = content.findViewById(R.id.skillDescription);
        TextView scaling = content.findViewById(R.id.skillScaling);
        TextView abCost = content.findViewById(R.id.skillABImpact);
        TextView cooldown = content.findViewById(R.id.skillCooldown);
        TextView effect = content.findViewById(R.id.skillEffect);
        TextView unlock = content.findViewById(R.id.skillUnlock);
        TextView passiveDetails = content.findViewById(R.id.passiveDetails);

        if (passive.getIconResId() != 0) icon.setImageResource(passive.getIconResId());
        name.setText(getPassiveName(passive)); // fallback name extraction
        type.setText("Type: Passive");
        desc.setText("Description: " + (passive.getDescription() == null ? "—" : passive.getDescription()));

        // hide active-only fields
        scaling.setVisibility(View.GONE);
        abCost.setVisibility(View.GONE);
        cooldown.setVisibility(View.GONE);
        effect.setVisibility(View.GONE);
        unlock.setVisibility(View.GONE);

        // show passive-specific details
        StringBuilder sb = new StringBuilder();
        sb.append("Unlocked at: ").append(passive.getAllowedClass() == null ? "Any" : passive.getAllowedClass().name())
                .append(" (lvl ").append(getPassiveUnlockLevel(passive)).append(")\n");
        // add any stat bonuses if you want; we only have numeric fields available internally - show generic text:
        sb.append("Bonuses: ").append(getPassiveBonuses(passive));

        passiveDetails.setText(sb.toString());
        passiveDetails.setVisibility(View.VISIBLE);

        showPopupAtAnchor(anchor, content);
    }

    // Utility: create and show popup at calculated position above anchor (or below if not enough space)
    private void showPopupAtAnchor(View anchor, View content) {
        popupWindow = new PopupWindow(
                content,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // transparent outside
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setOutsideTouchable(true);

        // measure
        content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupW = content.getMeasuredWidth();
        int popupH = content.getMeasuredHeight();

        int[] loc = new int[2];
        anchor.getLocationOnScreen(loc);
        int ax = loc[0], ay = loc[1], aw = anchor.getWidth(), ah = anchor.getHeight();

        int x = ax + (aw / 2) - (popupW / 2);
        if (x < 8) x = 8;

        int yAbove = ay - popupH - 10;
        int yBelow = ay + ah + 10;

        Rect visible = new Rect();
        anchor.getWindowVisibleDisplayFrame(visible);

        int y = (yAbove >= visible.top + 8) ? yAbove : yBelow;

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
    }

    // helpers (you can replace these with actual PassiveSkill getters if you add them)
    private String getPassiveName(PassiveSkill p) {
        try {
            // reflectively try to get a name, fallback to class simple name
            java.lang.reflect.Field f = p.getClass().getDeclaredField("name");
            f.setAccessible(true);
            Object val = f.get(p);
            if (val != null) return val.toString();
        } catch (Exception ignored) {}
        return p.getClass().getSimpleName();
    }

    private int getPassiveUnlockLevel(PassiveSkill p) {
        try {
            java.lang.reflect.Field f = p.getClass().getDeclaredField("levelUnlock");
            f.setAccessible(true);
            Object val = f.get(p);
            if (val instanceof Integer) return (Integer) val;
        } catch (Exception ignored) {}
        return 1;
    }

    private String getPassiveBonuses(PassiveSkill p) {
        // attempt to read known numeric fields (fallback to description)
        StringBuilder sb = new StringBuilder();
        try {
            java.lang.reflect.Field f1 = p.getClass().getDeclaredField("critBonus");
            f1.setAccessible(true);
            Object vb = f1.get(p);
            if (vb != null) sb.append("critBonus=").append(vb).append(" ");
        } catch (Exception ignored) {}
        if (sb.length() == 0) sb.append(p.getDescription() == null ? "—" : p.getDescription());
        return sb.toString();
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) popupWindow.dismiss();
    }
}
