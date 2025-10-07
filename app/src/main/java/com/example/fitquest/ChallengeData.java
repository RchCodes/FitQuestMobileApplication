package com.example.fitquest;

import java.io.Serializable;
import java.util.List;

public class ChallengeData implements Serializable {
    private int levelNumber;
    private List<EnemyModel> enemies;

    public ChallengeData(int levelNumber, List<EnemyModel> enemies) {
        this.levelNumber = levelNumber;
        this.enemies = enemies;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public List<EnemyModel> getEnemies() {
        return enemies;
    }
}
