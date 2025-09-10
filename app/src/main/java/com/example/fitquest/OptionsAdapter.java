package com.example.fitquest;

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
        void onOptionClick(int drawableRes, AvatarCreationActivity.Category category);
    }

    private List<Integer> options = new ArrayList<>();
    private AvatarCreationActivity.Category currentCategory;
    private final OnOptionClickListener listener;

    public OptionsAdapter(OnOptionClickListener listener) {
        this.listener = listener;
    }

    public void setOptions(List<Integer> options, AvatarCreationActivity.Category category) {
        this.options = options;
        this.currentCategory = category;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());

        int size = (int) (96 * parent.getContext().getResources().getDisplayMetrics().density / 2); // ~96dp
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(size, size);
        params.setMargins(8, 8, 8, 8);

        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Background grid (gray checkerboard)
        imageView.setBackgroundResource(R.drawable.grid_background);

        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int drawableRes = options.get(position);
        holder.imageView.setImageResource(drawableRes);

        holder.imageView.setOnClickListener(v ->
                listener.onOptionClick(drawableRes, currentCategory)
        );
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            imageView = itemView;
        }
    }
}

