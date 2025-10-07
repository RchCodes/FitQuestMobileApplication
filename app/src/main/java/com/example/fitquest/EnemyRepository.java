package com.example.fitquest;

import java.util.HashMap;
import java.util.Map;

public class EnemyRepository {
    private static final Map<String, EnemyModel> enemies = new HashMap<>();

    static {
        // SLIME
        enemies.put("slime", new EnemyModel(
                "slime",
                "Slime",
                100, 5, 2, 3,
                EnemySkillRepository.getSlimeSkills(),
                EnemySkillRepository.getSlimePassives(),
                R.drawable.enemy_slime
        ));

        // VENOPODS
        enemies.put("venopods", new EnemyModel(
                "venopods",
                "Venopods",
                150, 8, 3, 4,
                EnemySkillRepository.getVenopodsSkills(),
                EnemySkillRepository.getVenopodsPassives(),
                R.drawable.enemy_venopod
        ));

        // FLAME WOLF
        enemies.put("flame_wolf", new EnemyModel(
                "flame_wolf",
                "Flame Wolf",
                200, 10, 6, 5,
                EnemySkillRepository.getFlameWolfSkills(),
                EnemySkillRepository.getFlameWolfPassives(),
                R.drawable.enemy_venopod
        ));

        // SLIME KING
        enemies.put("slime_king", new EnemyModel(
                "slime_king",
                "Slime King",
                500, 20, 5, 10,
                EnemySkillRepository.getSlimeKingSkills(),
                EnemySkillRepository.getSlimeKingPassives(),
                R.drawable.enemy_slimeking
        ));
    }

    public static EnemyModel getEnemy(String id) {
        return enemies.get(id);
    }
}
