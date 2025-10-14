package com.example.fitquest;

public class BaseGoal {
    private final String id;
    private final String description;
    private final int badgeDrawableResId;

    public BaseGoal(String id, String description, int badgeDrawableResId) {
        this.id = id;
        this.description = description;
        this.badgeDrawableResId = badgeDrawableResId;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getBadgeDrawableResId() {
        return badgeDrawableResId;
    }
}
