package com.example.fitquest;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class EnemyModel implements Parcelable {

    private final String id;
    private final String name;
    private final int baseHp;
    private final int baseStr;
    private final int baseAgi;
    private final int baseEnd;
    private final List<SkillModel> skills;
    private final List<PassiveSkill> passives;
    private final int spriteResId;

    public EnemyModel(String id, String name, int baseHp, int baseStr, int baseAgi, int baseEnd,
                      List<SkillModel> skills, List<PassiveSkill> passives, int spriteResId) {
        this.id = id;
        this.name = name;
        this.baseHp = baseHp;
        this.baseStr = baseStr;
        this.baseAgi = baseAgi;
        this.baseEnd = baseEnd;
        this.skills = skills;
        this.passives = passives;
        this.spriteResId = spriteResId;
    }

    protected EnemyModel(Parcel in) {
        id = in.readString();
        name = in.readString();
        baseHp = in.readInt();
        baseStr = in.readInt();
        baseAgi = in.readInt();
        baseEnd = in.readInt();
        spriteResId = in.readInt();

        // For skills/passives, you can either implement Parcelable on SkillModel and PassiveSkill
        // or skip them for now if ChallengeActivity doesn't need them immediately.
        skills = new ArrayList<>();
        passives = new ArrayList<>();
    }

    public static final Creator<EnemyModel> CREATOR = new Creator<EnemyModel>() {
        @Override
        public EnemyModel createFromParcel(Parcel in) {
            return new EnemyModel(in);
        }

        @Override
        public EnemyModel[] newArray(int size) {
            return new EnemyModel[size];
        }
    };

    // --- Getters ---
    public int getBaseHp() { return baseHp; }
    public int getBaseStr() { return baseStr; }
    public int getBaseAgi() { return baseAgi; }
    public int getBaseEnd() { return baseEnd; }
    public String getId() { return id; }
    public String getName() { return name; }
    public int getSpriteResId() { return spriteResId; }

    public EnemyModel spawn() {
        return new EnemyModel(id, name, baseHp, baseStr, baseAgi, baseEnd, skills, passives, spriteResId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(baseHp);
        dest.writeInt(baseStr);
        dest.writeInt(baseAgi);
        dest.writeInt(baseEnd);
        dest.writeInt(spriteResId);
        // skip skills/passives for simplicity if they are not Parcelable
    }
}
