package com.example.fitquest;

public class BattleHistoryModel {
    private String leftName;
    private int leftLevel;
    private int leftIconRes;
    private String rightName;
    private int rightLevel;
    private int rightIconRes;
    private int scoreChange; // +25 or -15

    public BattleHistoryModel(String leftName, int leftLevel, int leftIconRes,
                              String rightName, int rightLevel, int rightIconRes,
                              int scoreChange) {
        this.leftName = leftName;
        this.leftLevel = leftLevel;
        this.leftIconRes = leftIconRes;
        this.rightName = rightName;
        this.rightLevel = rightLevel;
        this.rightIconRes = rightIconRes;
        this.scoreChange = scoreChange;
    }

    public String getLeftName() { return leftName; }
    public int getLeftLevel() { return leftLevel; }
    public int getLeftIconRes() { return leftIconRes; }
    public String getRightName() { return rightName; }
    public int getRightLevel() { return rightLevel; }
    public int getRightIconRes() { return rightIconRes; }
    public int getScoreChange() { return scoreChange; }
}
