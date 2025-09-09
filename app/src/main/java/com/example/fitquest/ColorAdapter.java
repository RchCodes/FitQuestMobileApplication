package com.example.fitquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    public interface OnColorClickListener {
        void onColorClick(int color);
    }

    private final int[] colors;
    private final OnColorClickListener listener;

    public ColorAdapter(int[] colors, OnColorClickListener listener) {
        this.colors = colors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        int color = colors[position];
        holder.colorView.setBackgroundColor(color);
        holder.colorView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onColorClick(color);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        ImageView colorView;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.image_color);
        }
    }
}
