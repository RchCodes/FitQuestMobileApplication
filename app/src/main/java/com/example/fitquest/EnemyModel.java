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

    // Persisted in parcel / repository: IDs only
    private final List<String> skillIds;
    private final List<String> passiveIds;

    // Runtime: reconstructed from ids via SkillRepository
    private transient List<SkillModel> skills;
    private transient List<PassiveSkill> passives;

    private final int spriteResId;

    public EnemyModel(String id, String name, int baseHp, int baseStr, int baseAgi, int baseEnd,
                      List<String> skillIds, List<String> passiveIds, int spriteResId) {
        this.id = id;
        this.name = name;
        this.baseHp = baseHp;
        this.baseStr = baseStr;
        this.baseAgi = baseAgi;
        this.baseEnd = baseEnd;
        this.skillIds = skillIds != null ? new ArrayList<>(skillIds) : new ArrayList<>();
        this.passiveIds = passiveIds != null ? new ArrayList<>(passiveIds) : new ArrayList<>();
        this.spriteResId = spriteResId;

        // Build runtime lists
        loadSkillsFromRepo();
    }

    protected EnemyModel(Parcel in) {
        id = in.readString();
        name = in.readString();
        baseHp = in.readInt();
        baseStr = in.readInt();
        baseAgi = in.readInt();
        baseEnd = in.readInt();
        spriteResId = in.readInt();

        skillIds = in.createStringArrayList();
        passiveIds = in.createStringArrayList();

        // Rebuild runtime objects
        loadSkillsFromRepo();
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

    // Reconstruct SkillModel / PassiveSkill lists from repository using ids
    public void loadSkillsFromRepo() {
        skills = new ArrayList<>();
        passives = new ArrayList<>();

        if (skillIds != null) {
            for (String sid : skillIds) {
                SkillModel s = EnemySkillRepository.getEnemySkillById(sid);
                if (s == null) s = SkillRepository.getSkillById(sid); // fallback to player skills
                if (s != null) skills.add(s);
            }
        }
        if (passiveIds != null) {
            for (String pid : passiveIds) {
                PassiveSkill p = EnemySkillRepository.getEnemyPassiveById(pid);
                if (p == null) p = SkillRepository.getPassiveById(pid); // fallback to player passives
                if (p != null) passives.add(p);
            }
        }
    }

    // --- spawn: return a fresh instance that contains runtime skill/passive objects ---
    public EnemyModel spawn() {
        EnemyModel base = EnemyRepository.getEnemy(id);
        if (base == null) return this;
        // Create a new instance copying ids (so the new instance will rebuild runtime lists)
        return new EnemyModel(
                base.id,
                base.name,
                base.baseHp,
                base.baseStr,
                base.baseAgi,
                base.baseEnd,
                base.skillIds != null ? new ArrayList<>(base.skillIds) : null,
                base.passiveIds != null ? new ArrayList<>(base.passiveIds) : null,
                base.spriteResId
        );
    }

    // --- Getters ---
    public int getBaseHp() { return baseHp; }
    public int getBaseStr() { return baseStr; }
    public int getBaseAgi() { return baseAgi; }
    public int getBaseEnd() { return baseEnd; }
    public String getId() { return id; }
    public String getName() { return name; }
    public int getSpriteResId() { return spriteResId; }
    public List<SkillModel> getSkills() {
        if (skills == null) loadSkillsFromRepo();
        return skills;
    }
    public List<PassiveSkill> getPassives() {
        if (passives == null) loadSkillsFromRepo();
        return passives;
    }
    public List<String> getSkillIds() { return skillIds; }
    public List<String> getPassiveIds() { return passiveIds; }

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

        dest.writeStringList(skillIds);
        dest.writeStringList(passiveIds);
    }
}
