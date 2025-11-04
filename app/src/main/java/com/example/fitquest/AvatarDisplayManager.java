package com.example.fitquest;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;

import java.util.List;

public class AvatarDisplayManager {

    private AvatarModel avatar;

    // Layers
    private final ImageView ivBody, ivOutfit, ivWeapon,
            ivHairOutline, ivHairFill, ivEyesOutline, ivEyesIris,
            ivNose, ivLips;

    private final Context context;

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

        // Load avatar offline
        this.avatar = AvatarManager.loadAvatarOffline(context);
        if (this.avatar != null) {
            refreshDisplay();
        }
    }

    /** Refresh all layers based on the currently loaded avatar */
    /** Refresh all layers based on the currently loaded avatar */
    public void refreshDisplay() {
        if (avatar == null) return;

        // Body
        setLayerDrawable(ivBody, avatar.getBodyStyle(), null);

        // Outfit
        setLayerDrawable(ivOutfit, avatar.getOutfit(), null);

        // Weapon - get from equipped gear
        updateWeaponFromEquippedGear();

        // Hair Outline
        setLayerDrawable(ivHairOutline, avatar.getHairOutline(), null);

        // Hair Fill with color
        setLayerDrawable(ivHairFill, avatar.getHairFill(), avatar.getHairColor());

        // Eyes Outline
        setLayerDrawable(ivEyesOutline, avatar.getEyesOutline(), null);

        // Eyes Iris / Fill with color
        setLayerDrawable(ivEyesIris, avatar.getEyesFill(), avatar.getEyesColor());

        // Nose
        setLayerDrawable(ivNose, avatar.getNose(), null);

        // Lips
        setLayerDrawable(ivLips, avatar.getLips(), null);

        try {
            if (ivHairOutline.getDrawable() == null) {
                Log.d("AvatarDebug", "Hair outline missing: " + avatar.getHairOutline());
            }
            if (ivEyesOutline.getDrawable() == null) {
                Log.d("AvatarDebug", "Eyes outline missing: " + avatar.getEyesOutline());
            }
        } catch (Exception e) {
            Log.e("AvatarDebug", "Error checking drawables", e);
        }

    }

    /** Utility: set drawable to ImageView safely, apply optional color */
    private void setLayerDrawable(ImageView iv, String drawableName, String colorHex) {
        if (drawableName != null && !drawableName.isEmpty()) {
            int resId = getDrawableByName(drawableName);
            if (resId != 0) {
                iv.setImageResource(resId);
                if (colorHex != null && !colorHex.isEmpty()) {
                    try {
                        iv.setColorFilter(Color.parseColor(colorHex));
                    } catch (IllegalArgumentException e) {
                        iv.clearColorFilter(); // fallback if invalid color
                    }
                } else {
                    iv.clearColorFilter();
                }
            } else {
                iv.setImageDrawable(null); // drawable not found
            }
        } else {
            iv.setImageDrawable(null); // no drawable set
            iv.clearColorFilter();
        }
    }

    /** Replace avatar and refresh all layers */
    public void setAvatar(AvatarModel newAvatar) {
        this.avatar = newAvatar;
        refreshDisplay();
        saveAvatar();
    }

    /** Update avatar and refresh display */
    public void loadAvatar(AvatarModel avatar) {
        this.avatar = avatar;
        refreshDisplay();
    }

    /** Set outfit and save changes */
    public void setOutfit(String outfitResName) {
        if (avatar == null) return;
        avatar.setOutfit(outfitResName);
        ivOutfit.setImageResource(getDrawableByName(outfitResName));
        saveAvatar();
    }

    /** Set weapon and save changes */
    public void setWeapon(String weaponResName) {
        if (avatar == null) return;
        avatar.setWeapon(weaponResName);
        ivWeapon.setImageResource(getDrawableByName(weaponResName));
        saveAvatar();
    }

    /** Return current avatar */
    public AvatarModel getAvatar() {
        return avatar;
    }

    /** Save avatar offline and online */
    private void saveAvatar() {
        AvatarManager.saveAvatarOffline(context, avatar);
        AvatarManager.saveAvatarOnline(avatar);
    }

    /** Utility: get drawable resource ID by name */
    private int getDrawableByName(String name) {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    public List<BattleHistoryModel> getBattleHistory() {
        return avatar.getBattleHistory();
    }
    
    /**
     * Get weapon ImageView for direct manipulation
     */
    public ImageView getWeaponImageView() {
        return ivWeapon;
    }
    
    /**
     * Get outfit ImageView for direct manipulation
     */
    public ImageView getOutfitImageView() {
        return ivOutfit;
    }
    
    /**
     * Preview gear temporarily without saving
     */
    public void previewGear(GearModel gear) {
        if (gear == null || avatar == null) return;
        
        switch (gear.getType()) {
            case WEAPON:
                previewWeapon(gear);
                break;
            case ARMOR:
            case PANTS:
            case BOOTS:
                // Outfit changes are handled by checkOutfitCompletion
                avatar.checkOutfitCompletion();
                refreshDisplay();
                break;
            case ACCESSORY:
                // Accessories might not have visual representation
                break;
        }
    }
    
    /**
     * Preview weapon temporarily
     */
    private void previewWeapon(GearModel weapon) {
        if (weapon == null) return;
        
        String gender = avatar.getGender();
        int weaponSpriteRes;
        
        if ("male".equalsIgnoreCase(gender)) {
            weaponSpriteRes = weapon.getMaleSpriteRes();
        } else {
            weaponSpriteRes = weapon.getFemaleSpriteRes();
        }
        
        if (weaponSpriteRes != 0) {
            ivWeapon.setImageResource(weaponSpriteRes);
        }
    }
    
    /**
     * Auto-equip gear and update avatar display
     */
    public void autoEquipGear(GearModel gear) {
        if (gear == null || avatar == null) return;
        
        try {
            // Equip the gear
            avatar.equipGear(gear.getType(), gear.getId());
            
            // Update visual display based on gear type
            switch (gear.getType()) {
                case WEAPON:
                    updateWeaponDisplay(gear);
                    break;
                case ARMOR:
                case PANTS:
                case BOOTS:
                    // Update outfit if it completes a set
                    avatar.checkOutfitCompletion();
                    refreshDisplay();
                    break;
                case ACCESSORY:
                    // Accessories might not have visual representation
                    break;
            }
            
            // Save changes
            saveAvatar();
        } catch (Exception e) {
            Log.e("AvatarDisplayManager", "Error auto-equipping gear: " + gear.getName(), e);
        }
    }
    
    /**
     * Update weapon display based on equipped weapon
     */
    private void updateWeaponDisplay(GearModel weapon) {
        if (weapon == null) return;
        
        String gender = avatar.getGender();
        int weaponSpriteRes;
        
        if ("male".equalsIgnoreCase(gender)) {
            weaponSpriteRes = weapon.getMaleSpriteRes();
        } else {
            weaponSpriteRes = weapon.getFemaleSpriteRes();
        }
        
        if (weaponSpriteRes != 0) {
            // Update the weapon sprite resource name in avatar
            String weaponResName = context.getResources().getResourceEntryName(weaponSpriteRes);
            avatar.setWeapon(weaponResName);
            
            // Update the display
            ivWeapon.setImageResource(weaponSpriteRes);
        }
    }
    
    /**
     * Update weapon display from currently equipped weapon gear
     */
    private void updateWeaponFromEquippedGear() {
        if (avatar == null) return;
        
        try {
            // Get equipped weapon gear ID
            String weaponGearId = avatar.getEquippedGear().get(GearType.WEAPON.name());
            
            if (weaponGearId != null) {
                // Get the gear model
                GearModel weaponGear = GearRepository.getGearById(weaponGearId);
                
                if (weaponGear != null) {
                    // Get appropriate sprite based on gender
                    String gender = avatar.getGender();
                    int weaponSpriteRes;
                    
                    if ("male".equalsIgnoreCase(gender)) {
                        weaponSpriteRes = weaponGear.getMaleSpriteRes();
                    } else {
                        weaponSpriteRes = weaponGear.getFemaleSpriteRes();
                    }
                    
                    if (weaponSpriteRes != 0) {
                        // Update the weapon sprite resource name in avatar
                        String weaponResName = context.getResources().getResourceEntryName(weaponSpriteRes);
                        avatar.setWeapon(weaponResName);
                        
                        // Update the display
                        ivWeapon.setImageResource(weaponSpriteRes);
                        return;
                    }
                }
            }
            
            // No weapon equipped or weapon not found - clear weapon display
            ivWeapon.setImageDrawable(null);
            avatar.setWeapon(null);
            
        } catch (Exception e) {
            Log.e("AvatarDisplayManager", "Error updating weapon from equipped gear", e);
            // Fallback to clearing weapon
            ivWeapon.setImageDrawable(null);
            avatar.setWeapon(null);
        }
    }
}
