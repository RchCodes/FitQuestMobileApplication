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
        ImageView iv = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_option_image, parent, false)
                .findViewById(R.id.image_option);
        return new ViewHolder(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int resId = options.get(position);
        holder.imageView.setImageResource(resId);
        holder.imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOptionClick(resId, currentCategory);
            }
        });
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}
