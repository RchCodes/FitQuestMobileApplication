package com.example.fitquest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EnemyRepository stores canonical enemy templates.
 * Each EnemyModel stores skill/passive ID lists (not the full objects) so parceling/Intents are lightweight.
 * The actual SkillModel / PassiveSkill objects are recreated at runtime from SkillRepository.
 */
public class EnemyRepository {
    private static final Map<String, EnemyModel> enemies = new HashMap<>();

    static {
    // Ensure enemy skills/passives are registered before creating any enemies
    EnemySkillRepository.init();

    // SLIME
    enemies.put("slime", new EnemyModel(
        "slime",
        "Slime",
        100, 5, 2, 3,
        toSkillIdList(EnemySkillRepository.getSlimeSkills()),
        toPassiveIdList(EnemySkillRepository.getSlimePassives()),
        R.drawable.enemy_slime
    ));

    // VENOPODS
    enemies.put("venopods", new EnemyModel(
        "venopods",
        "Venopods",
        150, 8, 3, 4,
        toSkillIdList(EnemySkillRepository.getVenopodsSkills()),
        toPassiveIdList(EnemySkillRepository.getVenopodsPassives()),
        R.drawable.enemy_venopod
    ));

    // FLAME WOLF
    enemies.put("flame_wolf", new EnemyModel(
        "flame_wolf",
        "Flame Wolf",
        200, 10, 6, 5,
        toSkillIdList(EnemySkillRepository.getFlameWolfSkills()),
        toPassiveIdList(EnemySkillRepository.getFlameWolfPassives()),
        R.drawable.enemy_flamewolf
    ));

    // SLIME KING
    enemies.put("slime_king", new EnemyModel(
        "slime_king",
        "Slime King",
        500, 20, 5, 10,
        toSkillIdList(EnemySkillRepository.getSlimeKingSkills()),
        toPassiveIdList(EnemySkillRepository.getSlimeKingPassives()),
        R.drawable.enemy_slimeking
    ));
    }

    public static EnemyModel getEnemy(String id) {
        EnemyModel template = enemies.get(id);
        // return the template itself; callers should call spawn() to get a fresh instance
        return template;
    }

    // Helpers to convert concrete SkillModel / PassiveSkill lists into ID lists
    private static List<String> toSkillIdList(List<SkillModel> skills) {
        List<String> out = new ArrayList<>();
        if (skills == null) return out;
        for (SkillModel s : skills) {
            if (s != null && s.getId() != null) out.add(s.getId());
        }
        return out;
    }

    private static List<String> toPassiveIdList(List<PassiveSkill> passives) {
        List<String> out = new ArrayList<>();
        if (passives == null) return out;
        for (PassiveSkill p : passives) {
            if (p != null && p.getId() != null) out.add(p.getId());
        }
        return out;
    }
}
