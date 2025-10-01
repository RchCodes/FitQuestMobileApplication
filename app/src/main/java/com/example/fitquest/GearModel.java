package com.example.fitquest;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class GearModel {
    private String id;
    private String name;
    private int price;
    private GearType type;
    private int iconRes;
    private String classRestriction; // e.g., "WARRIOR", "ROGUE", "TANK", "UNIVERSAL"

    // Gameplay effects
    private Map<String, Float> statBoosts;   // {"STR": 10f}
    private List<GearEffects> skillAugments; // list of GearEffects
    private String desc;

    // Outfit / set (armor) - weapons may leave null
    private String setId;
    private boolean completesOutfit;
    private int outfitSpriteRes;

    // gender-specific weapon sprites (if applicable)
    private int maleSpriteRes;
    private int femaleSpriteRes;

    public GearModel(String id,
                     String name,
                     int price,
                     GearType type,
                     int iconRes,
                     String classRestriction,
                     Map<String, Float> statBoosts,
                     List<GearEffects> skillAugments,
                     String desc,
                     String setId,
                     boolean completesOutfit,
                     int outfitSpriteRes,
                     int maleSpriteRes,
                     int femaleSpriteRes) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.type = type;
        this.iconRes = iconRes;
        this.classRestriction = classRestriction;
        this.statBoosts = statBoosts;
        this.skillAugments = skillAugments;
        this.desc = desc;
        this.setId = setId;
        this.completesOutfit = completesOutfit;
        this.outfitSpriteRes = outfitSpriteRes;
        this.maleSpriteRes = maleSpriteRes;
        this.femaleSpriteRes = femaleSpriteRes;
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public GearType getType() { return type; }
    public void setType(GearType type) { this.type = type; }

    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }

    public String getClassRestriction() { return classRestriction; }
    public void setClassRestriction(String classRestriction) { this.classRestriction = classRestriction; }

    public Map<String, Float> getStatBoosts() { return statBoosts; }
    public void setStatBoosts(Map<String, Float> statBoosts) { this.statBoosts = statBoosts; }

    public List<GearEffects> getSkillAugments() { return skillAugments; }
    public void setSkillAugments(List<GearEffects> skillAugments) { this.skillAugments = skillAugments; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public boolean isCompletesOutfit() { return completesOutfit; }
    public void setCompletesOutfit(boolean completesOutfit) { this.completesOutfit = completesOutfit; }

    public int getOutfitSpriteRes() { return outfitSpriteRes; }
    public void setOutfitSpriteRes(int outfitSpriteRes) { this.outfitSpriteRes = outfitSpriteRes; }

    public int getMaleSpriteRes() { return maleSpriteRes; }
    public void setMaleSpriteRes(int maleSpriteRes) { this.maleSpriteRes = maleSpriteRes; }

    public int getFemaleSpriteRes() { return femaleSpriteRes; }
    public void setFemaleSpriteRes(int femaleSpriteRes) { this.femaleSpriteRes = femaleSpriteRes; }


    // UI helpers
    public String getBoostsString() {
        if (statBoosts == null || statBoosts.isEmpty()) return "Boosts:\nNone";
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Boosts:");
        for (Map.Entry<String, Float> e : statBoosts.entrySet()) {
            String sign = e.getValue() > 0 ? "+" : "";
            sj.add(sign + e.getValue() + "% " + e.getKey());
        }
        return sj.toString();
    }

    public String getSkillString() {
        if (skillAugments == null || skillAugments.isEmpty()) return "Skill Effects:\nNone";
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Skill Effects:");
        for (GearEffects ge : skillAugments) {
            sj.add(ge.toString());
        }
        return sj.toString();
    }
}
