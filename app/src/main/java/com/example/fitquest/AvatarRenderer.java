package com.example.fitquest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for rendering avatar parts (hair, eyes, etc.)
 * Combines an outline + a tintable fill into one Bitmap.
 * Uses caching to avoid recomputing the same (outline, fill, color) combo.
 */
public class AvatarRenderer {

    // Simple cache: key = outline+fill+color string, value = Bitmap
    private static final Map<String, Bitmap> cache = new HashMap<>();

    /**
     * Render and cache a part with outline + tintable fill.
     *
     * @param ctx        Context
     * @param outlineRes Outline drawable resource ID
     * @param fillRes    Fill drawable resource ID
     * @param tintColor  Color to tint the fill (use -1 for none)
     * @return Combined Bitmap (outline + tinted fill), or null if failed
     */
    public static Bitmap renderPart(Context ctx, int outlineRes, int fillRes, int tintColor) {
        String key = outlineRes + "_" + fillRes + "_" + tintColor;

        // Return cached if exists
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        try {
            Drawable outline = outlineRes != 0 ? AppCompatResources.getDrawable(ctx, outlineRes) : null;
            Drawable fill = fillRes != 0 ? AppCompatResources.getDrawable(ctx, fillRes) : null;

            if (outline == null && fill == null) return null;

            // If only one exists:
            if (outline == null) {
                Bitmap bmp = drawableToBitmap(ctx, fill, tintColor);
                cache.put(key, bmp);
                return bmp;
            }
            if (fill == null) {
                Bitmap bmp = drawableToBitmap(ctx, outline, -1);
                cache.put(key, bmp);
                return bmp;
            }

            // Both exist
            int w = Math.max(outline.getIntrinsicWidth(), fill.getIntrinsicWidth());
            int h = Math.max(outline.getIntrinsicHeight(), fill.getIntrinsicHeight());
            if (w <= 0) w = (int) (180 * ctx.getResources().getDisplayMetrics().density);
            if (h <= 0) h = (int) (280 * ctx.getResources().getDisplayMetrics().density);

            // Tint fill
            fill = fill.mutate();
            if (tintColor != -1) {
                fill.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
            }
            fill.setBounds(0, 0, w, h);
            outline.setBounds(0, 0, w, h);

            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);

            fill.draw(canvas);
            outline.draw(canvas);

            cache.put(key, bmp);
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap drawableToBitmap(Context ctx, Drawable d, int tintColor) {
        if (d == null) return null;

        int w = d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() :
                (int) (180 * ctx.getResources().getDisplayMetrics().density);
        int h = d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() :
                (int) (280 * ctx.getResources().getDisplayMetrics().density);

        d = d.mutate();
        if (tintColor != -1) {
            d.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        }

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        d.setBounds(0, 0, w, h);
        d.draw(c);
        return bmp;
    }

    /** Optional: clear the cache (e.g., on logout) */
    public static void clearCache() {
        cache.clear();
    }
}
