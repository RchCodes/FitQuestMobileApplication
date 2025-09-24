package com.example.fitquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private final int[] colors;
    private final OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorAdapter(int[] colors, OnColorSelectedListener listener) {
        this.colors = colors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item_color.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        int color = colors[position];

        // Safely tint the background
        if (holder.colorView.getBackground() != null) {
            holder.colorView.getBackground().setTint(color);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onColorSelected(color);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        View colorView;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            // item_color.xml root is the View itself
            colorView = itemView;
        }
    }
}
