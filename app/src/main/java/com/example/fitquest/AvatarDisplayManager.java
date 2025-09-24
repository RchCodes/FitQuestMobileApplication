package com.example.fitquest;

import android.content.Context;
import android.widget.ImageView;

public class AvatarDisplayManager {

    private AvatarModel avatar;

    // Layers
    private ImageView ivBody, ivOutfit, ivWeapon,
            ivHairOutline, ivHairFill, ivEyesOutline, ivEyesIris,
            ivNose, ivLips;

    private Context context;

    public AvatarDisplayManager(Context context,
                                ImageView ivBody,
                                ImageView ivOutfit,
                                ImageView ivWeapon,
                                ImageView ivHairOutline,
                                ImageView ivHairFill,
                                ImageView ivEyesOutline,
                                ImageView ivEyesIris,
                                ImageView ivNose,
                                ImageView ivLips) {
        this.context = context;
        this.ivBody = ivBody;
        this.ivOutfit = ivOutfit;
        this.ivWeapon = ivWeapon;
        this.ivHairOutline = ivHairOutline;
        this.ivHairFill = ivHairFill;
        this.ivEyesOutline = ivEyesOutline;
        this.ivEyesIris = ivEyesIris;
        this.ivNose = ivNose;
        this.ivLips = ivLips;

        // Load avatar
        this.avatar = AvatarManager.loadAvatarOffline(context);
        if (this.avatar != null) {
            refreshDisplay();
        }
    }

    public void refreshDisplay() {
        if (avatar == null) return;

        ivBody.setImageResource(getDrawableByName(avatar.getBodyStyle()));
        ivOutfit.setImageResource(getDrawableByName(avatar.getOutfit()));
        ivWeapon.setImageResource(getDrawableByName(avatar.getWeapon()));

        ivHairOutline.setImageResource(getDrawableByName(avatar.getHairOutline()));
        ivHairFill.setImageResource(getDrawableByName(avatar.getHairFill()));
        ivHairFill.setColorFilter(android.graphics.Color.parseColor(avatar.getHairColor()));

        ivEyesOutline.setImageResource(getDrawableByName(avatar.getEyesOutline()));
        ivEyesIris.setImageResource(getDrawableByName(avatar.getEyesFill()));
        ivEyesIris.setColorFilter(android.graphics.Color.parseColor(avatar.getEyesColor()));

        ivNose.setImageResource(getDrawableByName(avatar.getNose()));
        ivLips.setImageResource(getDrawableByName(avatar.getLips()));
    }

    // --- Gear Setters ---
    public void setOutfit(String outfitResName) {
        if (avatar == null) return;
        avatar.setOutfit(outfitResName);
        ivOutfit.setImageResource(getDrawableByName(outfitResName));
        saveAvatar();
    }

    public void setWeapon(String weaponResName) {
        if (avatar == null) return;
        avatar.setWeapon(weaponResName);
        ivWeapon.setImageResource(getDrawableByName(weaponResName));
        saveAvatar();
    }

    // --- Avatar getters ---
    public AvatarModel getAvatar() {
        return avatar;
    }

    // Save offline & online
    private void saveAvatar() {
        AvatarManager.saveAvatarOffline(context, avatar);
        AvatarManager.saveAvatarOnline(avatar);
    }

    // --- Utility ---
    private int getDrawableByName(String name) {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    // --- Optional: full avatar replacement ---
    public void setAvatar(AvatarModel newAvatar) {
        this.avatar = newAvatar;
        refreshDisplay();
        saveAvatar();
    }
}
