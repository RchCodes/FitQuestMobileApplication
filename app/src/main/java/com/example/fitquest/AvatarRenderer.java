package com.example.fitquest;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;

/**
 * Small helper that applies AvatarModel data to ImageView layers.
 * It does not compose bitmaps; it sets image resources and applies tint to fill layers.
 *
 * Expected ImageView IDs (passed from activity):
 * - baseBodyView
 * - hairOutlineView (outline)
 * - hairFillView (fill; will be tinted)
 * - eyesOutlineView
 * - eyesFillView (fill; will be tinted)
 * - noseView
 * - lipsView
 */
public class AvatarRenderer {

    public static void applyToViews(Context ctx,
                                    AvatarModel model,
                                    ImageView baseBodyView,
                                    ImageView hairOutlineView,
                                    ImageView hairFillView,
                                    ImageView eyesOutlineView,
                                    ImageView eyesFillView,
                                    ImageView noseView,
                                    ImageView lipsView) {

        if (baseBodyView != null) {
            if (model.bodyRes != 0) baseBodyView.setImageResource(model.bodyRes);
            else baseBodyView.setImageDrawable(null);
        }

        // Hair: outline + fill
        if (hairOutlineView != null) {
            if (model.hairOutlineRes != 0) hairOutlineView.setImageResource(model.hairOutlineRes);
            else hairOutlineView.setImageDrawable(null);
        }
        if (hairFillView != null) {
            if (model.hairFillRes != 0) hairFillView.setImageResource(model.hairFillRes);
            else hairFillView.setImageDrawable(null);

            if (model.hairColor != 0) {
                hairFillView.setColorFilter(model.hairColor, PorterDuff.Mode.SRC_IN);
            } else {
                hairFillView.clearColorFilter();
            }
        }

        // Eyes: outline + fill
        if (eyesOutlineView != null) {
            if (model.eyesOutlineRes != 0) eyesOutlineView.setImageResource(model.eyesOutlineRes);
            else eyesOutlineView.setImageDrawable(null);
        }
        if (eyesFillView != null) {
            if (model.eyesFillRes != 0) eyesFillView.setImageResource(model.eyesFillRes);
            else eyesFillView.setImageDrawable(null);

            if (model.eyesColor != 0) {
                eyesFillView.setColorFilter(model.eyesColor, PorterDuff.Mode.SRC_IN);
            } else {
                eyesFillView.clearColorFilter();
            }
        }

        // Nose
        if (noseView != null) {
            if (model.noseRes != 0) noseView.setImageResource(model.noseRes);
            else noseView.setImageDrawable(null);
        }

        // Lips (lips color is applied to the fill layer if you want; here we tint lips image)
        if (lipsView != null) {
            if (model.lipsRes != 0) lipsView.setImageResource(model.lipsRes);
            else lipsView.setImageDrawable(null);

            if (model.lipsColor != 0) {
                lipsView.setColorFilter(model.lipsColor, PorterDuff.Mode.SRC_IN);
            } else {
                lipsView.clearColorFilter();
            }
        }
    }
}
