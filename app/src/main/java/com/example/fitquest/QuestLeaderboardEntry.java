package com.example.fitquest;

public class QuestLeaderboardEntry extends LeaderboardEntry {
    private int questsCompleted;
    
    public QuestLeaderboardEntry() {
        // Default constructor for Firebase
        super();
    }
    
    public QuestLeaderboardEntry(String username, String userId, int questsCompleted, long lastUpdateTime) {
        super(username, userId, lastUpdateTime);
        this.questsCompleted = questsCompleted;
    }
    
    public int getQuestsCompleted() { return questsCompleted; }
    public void setQuestsCompleted(int questsCompleted) { this.questsCompleted = questsCompleted; }
    
    public void incrementQuestsCompleted() {
        this.questsCompleted++;
    }
    
    public void setLastQuestTime(long time) {
        this.lastUpdateTime = time;
    }
    
    @Override
    public int getScore() {
        return questsCompleted;
    }
    
    @Override
    public String getScoreLabel() {
        return questsCompleted + " Quests";
    }

    public long getLastQuestTime() {
        return lastUpdateTime;
    }
}
