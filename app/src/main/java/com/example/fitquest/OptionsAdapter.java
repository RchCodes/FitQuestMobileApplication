package com.example.fitquest;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {

    public interface OnOptionClickListener {
        void onOptionClick(int option, AvatarCreationActivity.Category category);
    }

    private List<Integer> options = new ArrayList<>();
    private AvatarCreationActivity.Category currentCategory;
    private final OnOptionClickListener listener;

    public OptionsAdapter(OnOptionClickListener listener) {
        this.listener = listener;
    }

    public void setOptions(List<Integer> newOptions, AvatarCreationActivity.Category category) {
        this.options = newOptions;
        this.currentCategory = category;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customization, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int option = options.get(position);

        if (currentCategory == AvatarCreationActivity.Category.EYES) {
            // Example: eye colors as solid colors
            holder.icon.setBackgroundColor(option);
            holder.icon.setImageDrawable(null);
        } else {
            holder.icon.setBackgroundColor(Color.TRANSPARENT);
            holder.icon.setImageResource(option);
        }

        holder.itemView.setOnClickListener(v -> listener.onOptionClick(option, currentCategory));
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.customization_icon);
        }
    }
}
