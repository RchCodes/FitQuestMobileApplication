package com.example.fitquest;

import android.content.Context;
import android.util.Log;

import java.util.List;

public class QuestProgressManager {
    
    private static final String TAG = "QuestProgressManager";
    
    /**
     * Update progress for accumulated quests when a daily quest is completed
     */
    public static void updateAccumulatedQuests(Context context, String exerciseType, int progressAmount) {
        // Get all quests
        List<QuestModel> allQuests = QuestManager.getAllQuests(context);
        
        for (QuestModel quest : allQuests) {
            // Only update accumulated quests (weekly/monthly) that match the exercise type
            if (quest.getCompletionType() == QuestCompletionType.ACCUMULATED 
                && quest.getExerciseType().equals(exerciseType)
                && !quest.isCompleted()) {
                
                // Add progress to the accumulated quest
                boolean completed = quest.addProgress(progressAmount);
                
                if (completed) {
                    Log.d(TAG, "Accumulated quest completed: " + quest.getTitle());
                }
            }
        }
        
        // Save updated quests
        QuestStorage.saveQuestsOffline(context, allQuests);
        QuestStorage.saveQuestsOnline(allQuests);
    }
    
    /**
     * Check if a daily quest completion should contribute to weekly/monthly quests
     */
    public static void handleDailyQuestCompletion(Context context, QuestModel completedQuest) {
        if (completedQuest.getCompletionType() == QuestCompletionType.SINGLE 
            && completedQuest.isCompleted()) {
            
            // Update accumulated quests with the progress from this daily quest
            updateAccumulatedQuests(context, completedQuest.getExerciseType(), completedQuest.getTarget());
        }
    }
}
