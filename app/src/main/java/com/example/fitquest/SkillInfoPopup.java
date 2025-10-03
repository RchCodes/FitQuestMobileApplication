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

/**
 * Reusable popup that displays Skill info and appears above the anchor view.
 */
public class SkillInfoPopup {

    private PopupWindow popupWindow;

    /**
     * Show the popup above the anchor view (centered horizontally on the anchor).
     * If there's not enough space above, it will show below the anchor.
     */
    public void show(View anchor, SkillModel skill) {
        if (anchor == null || skill == null) return;

        // Inflate content
        View content = LayoutInflater.from(anchor.getContext())
                .inflate(R.layout.dialog_skill_info, null);

        // Bind views and fill data
        ShapeableImageView icon = content.findViewById(R.id.skillIcon);
        TextView name = content.findViewById(R.id.skillName);
        TextView type = content.findViewById(R.id.skillType);
        TextView desc = content.findViewById(R.id.skillDescription);
        TextView scaling = content.findViewById(R.id.skillScaling);
        TextView cooldown = content.findViewById(R.id.skillCooldown);
        TextView effect = content.findViewById(R.id.skillEffect);
        TextView unlock = content.findViewById(R.id.skillUnlock);

        // fill fields (use getUnlock() to match the provided Skill model)
        if (skill.getIconRes() != 0) icon.setImageResource(skill.getIconRes());
        name.setText(skill.getName());
        type.setText("Type: " + skill.getType());
        desc.setText("Description: " + skill.getDescription());
        scaling.setText("Scaling: " + skill.getScaling());
        cooldown.setText("Cooldown: " + skill.getCooldown());
        effect.setText("Effect: " + skill.getEffect());
        unlock.setText("Level Unlock: " + skill.getLevelUnlock());

        // Create PopupWindow using ViewGroup.LayoutParams constants (WRAP_CONTENT)
        popupWindow = new PopupWindow(
                content,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // focusable - lets back button dismiss it
        );

        // Transparent outside area
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setOutsideTouchable(true);
        // If you want the popup to be able to extend outside the screen area, you can:
        // popupWindow.setClippingEnabled(false);

        // Measure content to compute position
        content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = content.getMeasuredWidth();
        int popupHeight = content.getMeasuredHeight();

        // Get anchor location on screen
        int[] loc = new int[2];
        anchor.getLocationOnScreen(loc);
        int anchorX = loc[0];
        int anchorY = loc[1];
        int anchorW = anchor.getWidth();
        int anchorH = anchor.getHeight();

        // Calculate x such that popup is centered above anchor
        int x = anchorX + (anchorW / 2) - (popupWidth / 2);
        if (x < 8) x = 8; // small padding from left edge

        // Try to place above anchor
        int yAbove = anchorY - popupHeight - 10; // 10px gap
        int yBelow = anchorY + anchorH + 10;

        // Ensure popup doesn't go off the top of the screen:
        Rect visibleFrame = new Rect();
        anchor.getWindowVisibleDisplayFrame(visibleFrame);

        int y;
        if (yAbove >= visibleFrame.top + 8) {
            y = yAbove; // enough room above
        } else {
            y = yBelow; // fallback to below anchor
        }

        // Show popup
        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }
}
