package com.example.fitquest;

public class RankLeaderboardEntry extends LeaderboardEntry {
    private int rankPoints;
    private int level;
    private int rank;
    
    public RankLeaderboardEntry() {
        // Default constructor for Firebase
        super();
    }
    
    public RankLeaderboardEntry(String username, String userId, int rankPoints, int level, int rank, long lastUpdateTime) {
        super(username, userId, lastUpdateTime);
        this.rankPoints = rankPoints;
        this.level = level;
        this.rank = rank;
    }
    
    public int getRankPoints() { return rankPoints; }
    public int getLevel() { return level; }
    public int getRank() { return rank; }
    
    public void setRankPoints(int rankPoints) { this.rankPoints = rankPoints; }
    public void setLevel(int level) { this.level = level; }
    public void setRank(int rank) { this.rank = rank; }
    
    @Override
    public int getScore() {
        return rankPoints;
    }
    
    @Override
    public String getScoreLabel() {
        return rankPoints + " RP";
    }
    
    public String getRankName() {
        switch (rank) {
            case 0: return "Novice";
            case 1: return "Veteran";
            case 2: return "Elite";
            case 3: return "Hero";
            case 4: return "Legendary";
            default: return "Unknown";
        }
    }
    
    public int getRankDrawableRes() {
        switch (rank) {
            case 0: return R.drawable.rank_novice;
            case 1: return R.drawable.rank_veteran;
            case 2: return R.drawable.rank_elite;
            case 3: return R.drawable.rank_hero;
            case 4: return R.drawable.rank_legendary;
            default: return R.drawable.rank_novice;
        }
    }
}
