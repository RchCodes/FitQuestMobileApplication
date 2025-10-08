package com.example.fitquest;

public abstract class LeaderboardEntry {
    protected String username;
    protected String userId;
    protected long lastUpdateTime;
    
    public LeaderboardEntry() {
        // Default constructor for Firebase
    }
    
    public LeaderboardEntry(String username, String userId, long lastUpdateTime) {
        this.username = username;
        this.userId = userId;
        this.lastUpdateTime = lastUpdateTime;
    }
    
    public String getUsername() { return username; }
    public String getUserId() { return userId; }
    public long getLastUpdateTime() { return lastUpdateTime; }
    
    public void setUsername(String username) { this.username = username; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    
    public abstract int getScore();
    public abstract String getScoreLabel();

    public int getRankPoints() {
        return getScore();
    }

    public int getQuestsCompleted() {
        return getScore();
    }
}
