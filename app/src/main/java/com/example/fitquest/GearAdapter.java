package com.example.fitquest;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for showing owned gear (no price). Highlights equipped gear first.
 */
public class GearAdapter extends BaseAdapter {

    public interface OnGearSelected {
        void onSelected(GearModel gear);
    }

    private final Context context;
    private List<GearModel> items = new ArrayList<>();
    private GearModel selectedGear;
    private final AvatarModel avatar;
    private final OnGearSelected listener;

    public GearAdapter(Context context, List<GearModel> initialItems, AvatarModel avatar, OnGearSelected listener) {
        this.context = context;
        this.avatar = avatar;
        if (initialItems != null) this.items = new ArrayList<>(initialItems);
        this.listener = listener;
        sortEquippedFirst();
    }

    public void updateData(List<GearModel> newItems) {
        this.items = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        sortEquippedFirst();
        notifyDataSetChanged();
    }

    public void setSelectedGear(GearModel gear) {
        this.selectedGear = gear;
        notifyDataSetChanged();
    }

    public GearModel getSelectedGear() {
        return selectedGear;
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public GearModel getItem(int position) {
        return items == null ? null : items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class VH {
        View root;
        ImageView itemIcon;
        ImageView itemFrame;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_owned_slot, parent, false);
            vh = new VH();
            vh.root = convertView;
            vh.itemIcon = convertView.findViewById(R.id.itemIcon);
            vh.itemFrame = convertView.findViewById(R.id.itemFrame);
            convertView.setTag(vh);
        } else {
            vh = (VH) convertView.getTag();
        }

        GearModel g = getItem(position);

        if (g != null) {
            if (g.getIconRes() != 0) {
                vh.itemIcon.setImageResource(g.getIconRes());
            } else {
                vh.itemIcon.setImageResource(R.drawable.ic_store_weapon);
            }
        } else {
            vh.itemIcon.setImageResource(R.drawable.ic_store_weapon);
        }

        // Highlight equipped gear
        boolean isEquipped = avatar != null && avatar.isEquipped(g);

        if (selectedGear != null && g != null && selectedGear.getId().equals(g.getId())) {
            // Selected gear
            vh.itemFrame.setColorFilter(0xFF00FF00, PorterDuff.Mode.SRC_ATOP); // green
            vh.itemIcon.setAlpha(1f);
        } else if (isEquipped) {
            // Equipped but not selected
            vh.itemFrame.setColorFilter(0xFF2196F3, PorterDuff.Mode.SRC_ATOP); // blue
            vh.itemIcon.setAlpha(1f);
        } else {
            vh.itemFrame.clearColorFilter();
            vh.itemIcon.setAlpha(0.95f);
        }

        convertView.setOnClickListener(v -> {
            selectedGear = g;
            if (listener != null) listener.onSelected(g);
            notifyDataSetChanged();
        });

        return convertView;
    }

    // Put equipped items first
    private void sortEquippedFirst() {
        if (avatar == null || items == null) return;
        Collections.sort(items, (a, b) -> {
            boolean aEq = avatar.isEquipped(a);
            boolean bEq = avatar.isEquipped(b);
            if (aEq && !bEq) return -1;
            if (!aEq && bEq) return 1;
            return 0;
        });
    }
}
