package com.example.fitquest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitquest.R;

import java.util.List;

public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.OptionViewHolder> {

    public interface OnOptionClickListener {
        void onOptionSelected(String outline, String fill);
    }

    private final Context context;
    private final List<String[]> options;
    // Each entry: {outlineName, fillName} OR {singleDrawable, null}

    private final OnOptionClickListener listener;

    public OptionsAdapter(Context context, List<String[]> options, OnOptionClickListener listener) {
        this.context = context;
        this.options = options;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_option, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        String[] option = options.get(position);

        // Load preview image (use outline if available, else single drawable)
        String previewName = option[0];
        int resId = context.getResources().getIdentifier(previewName, "drawable", context.getPackageName());
        holder.optionImage.setImageResource(resId);

        holder.itemView.setOnClickListener(v -> {
            String outline = option[0];
            String fill = option.length > 1 ? option[1] : null;
            listener.onOptionSelected(outline, fill);
        });
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    static class OptionViewHolder extends RecyclerView.ViewHolder {
        ImageView optionImage;

        public OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            optionImage = itemView.findViewById(R.id.option_image);
        }
    }
}
