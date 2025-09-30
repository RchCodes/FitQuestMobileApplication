package com.example.fitquest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

public class GearAdapter extends BaseAdapter {
    private Context context;
    private List<GearModel> items;

    public GearAdapter(Context context, List<GearModel> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_slot, parent, false);
            holder = new ViewHolder();
            holder.itemFrame = convertView.findViewById(R.id.itemFrame);
            holder.itemIcon = convertView.findViewById(R.id.itemIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GearModel gear = items.get(position);

        // Set icon based on gear type
        switch (gear.type) {
            case WEAPON:
                holder.itemIcon.setImageResource(R.drawable.ic_store_weapon);
                break;
            case ARMOR:
                holder.itemIcon.setImageResource(R.drawable.ic_store_armor);
                break;
            case PANTS:
                holder.itemIcon.setImageResource(R.drawable.ic_store_pants);
                break;
            case BOOTS:
                holder.itemIcon.setImageResource(R.drawable.ic_store_boots);
                break;
            case ACCESSORY:
                holder.itemIcon.setImageResource(R.drawable.ic_store_accessory);
                break;
        }

        // Example: Color frame by class
        if (gear.allowedClass == ClassType.TANK) {
            //holder.itemFrame.setColorFilter(context.getResources().getColor(R.color.Blue));
        } else {
            holder.itemFrame.clearColorFilter();
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView itemFrame;
        ImageView itemIcon;
    }
}
