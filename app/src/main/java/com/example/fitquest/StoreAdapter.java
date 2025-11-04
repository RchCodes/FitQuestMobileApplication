package com.example.fitquest;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class StoreAdapter extends BaseAdapter {

    private final Context context;
    private List<GearModel> items;

    private int selectedPosition = -1; // -1 = nothing selected
    private final AvatarModel avatar;

    public StoreAdapter(Context context, List<GearModel> items, AvatarModel avatar) {
        this.context = context;
        this.items = items;
        this.avatar = avatar;
    }

    public void updateData(List<GearModel> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Object getItem(int position) {
        return items == null ? null : items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static String formatCoins(long n) {
        if (n >= 1_000_000_000L) return (n / 1_000_000_000L) + "B";
        if (n >= 1_000_000L) return (n / 1_000_000L) + "M";
        if (n >= 1_000L) return (n / 1_000L) + "K";
        return String.valueOf(n);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        GearModel gear = items.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_slot, parent, false);
            holder = new ViewHolder();
            holder.itemIcon = convertView.findViewById(R.id.itemIcon);
            holder.itemFrame = convertView.findViewById(R.id.itemFrame);
            holder.itemPrice = convertView.findViewById(R.id.itemPrice);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == selectedPosition) {
            convertView.setBackgroundResource(R.drawable.bg_selected); // highlight
        } else {
            convertView.setBackgroundResource(R.drawable.bg_item_frame); // normal
        }

        // icon
        if (gear.getIconRes() != 0) {
            holder.itemIcon.setImageResource(gear.getIconRes());
        } else {
            holder.itemIcon.setImageResource(R.drawable.ic_store_weapon); // fallback
        }

        // Check if owned and show different UI
        boolean isOwned = avatar != null && avatar.ownsGear(gear.getId());
        
        if (isOwned) {
            holder.itemPrice.setText(" OWNED");
            holder.itemFrame.setColorFilter(0xFF4CAF50, PorterDuff.Mode.SRC_ATOP); // Green for owned
        } else {
            holder.itemPrice.setText(" " + formatCoins(gear.getPrice()));
            holder.itemFrame.clearColorFilter();
        }

        return convertView;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }


    private static class ViewHolder {
        ImageView itemIcon;
        ImageView itemFrame;
        TextView itemPrice;
    }
}
