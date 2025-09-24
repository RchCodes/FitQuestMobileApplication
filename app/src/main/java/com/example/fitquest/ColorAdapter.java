package com.example.fitquest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitquest.R;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    public interface OnColorClickListener {
        void onColorSelected(String hexColor);
    }

    private final Context context;
    private final List<String> colorList; // hex codes like "#FF0000"
    private final OnColorClickListener listener;

    public ColorAdapter(Context context, List<String> colorList, OnColorClickListener listener) {
        this.context = context;
        this.colorList = colorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        String hex = colorList.get(position);

        GradientDrawable bg = (GradientDrawable) holder.colorView.getBackground();
        bg.setColor(Color.parseColor(hex));

        holder.itemView.setOnClickListener(v -> {
            listener.onColorSelected(hex);
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        View colorView;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.color_circle);
        }
    }
}
