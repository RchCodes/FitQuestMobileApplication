package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProgressSyncManager {
    
    private static final String TAG = "ProgressSyncManager";
    private static final String PREFS_NAME = "fitquest_progress_sync";
    private static final String KEY_LAST_OFFLINE_SAVE = "last_offline_save";
    private static final String KEY_LAST_ONLINE_SAVE = "last_online_save";
    private static final String KEY_LAST_SYNC = "last_sync";
    private static final String KEY_CONFLICT_RESOLVED = "conflict_resolved";
    
    private static final Gson gson = new Gson();
    
    /**
     * Save avatar progress with intelligent conflict resolution
     */
    public static void saveProgress(Context context, AvatarModel avatar, boolean isOnline) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long currentTime = System.currentTimeMillis();

        if (isOnline) {
            // Save online and update timestamp only after success callback
            AvatarManager.saveAvatarOnline(avatar, new AvatarManager.SaveCallback() {
                @Override
                public void onSuccess() {
                    prefs.edit().putLong(KEY_LAST_ONLINE_SAVE, currentTime).apply();
                    Log.d(TAG, "Avatar saved online at " + currentTime);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Avatar online save failed", e);
                    // Optionally schedule retry
                }
            });
        } else {
            AvatarManager.saveAvatarOffline(context, avatar);
            prefs.edit().putLong(KEY_LAST_OFFLINE_SAVE, currentTime).apply();
            Log.d(TAG, "Avatar saved offline at " + currentTime);
        }


        // Schedule sync if there's a potential conflict
        scheduleSyncIfNeeded(context);
    }
    
    /**
     * Load avatar with intelligent conflict resolution
     */
    public static void loadProgress(Context context, AvatarLoadCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastOfflineSave = prefs.getLong(KEY_LAST_OFFLINE_SAVE, 0);
        long lastOnlineSave = prefs.getLong(KEY_LAST_ONLINE_SAVE, 0);
        
        // If no saves exist, load offline first
        if (lastOfflineSave == 0 && lastOnlineSave == 0) {
            AvatarModel offlineAvatar = AvatarManager.loadAvatarOffline(context);
            if (offlineAvatar != null) {
                callback.onLoaded(offlineAvatar);
            } else {
                // No avatar exists, create new one
                callback.onError("No avatar found");
            }
            return;
        }
        
        // Determine which save is more recent
        if (lastOfflineSave > lastOnlineSave) {
            // Offline is more recent, load it
            AvatarModel offlineAvatar = AvatarManager.loadAvatarOffline(context);
            if (offlineAvatar != null) {
                callback.onLoaded(offlineAvatar);
                // Schedule sync to update online
                scheduleSyncIfNeeded(context);
            } else {
                callback.onError("Failed to load offline avatar");
            }
        } else {
            // Online is more recent or equal, load online
            AvatarManager.loadAvatarOnline(new AvatarManager.AvatarLoadCallback() {
                @Override
                public void onLoaded(AvatarModel avatar) {
                    // Update offline with online data
                    AvatarManager.saveAvatarOffline(context, avatar);
                    callback.onLoaded(avatar);
                }
                
                @Override
                public void onError(String message) {
                    // Fallback to offline
                    AvatarModel offlineAvatar = AvatarManager.loadAvatarOffline(context);
                    if (offlineAvatar != null) {
                        callback.onLoaded(offlineAvatar);
                    } else {
                        callback.onError("Failed to load avatar: " + message);
                    }
                }
            });
        }
    }
    
    /**
     * Schedule sync work if there's a potential conflict
     */
    private static void scheduleSyncIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastOfflineSave = prefs.getLong(KEY_LAST_OFFLINE_SAVE, 0);
        long lastOnlineSave = prefs.getLong(KEY_LAST_ONLINE_SAVE, 0);
        long lastSync = prefs.getLong(KEY_LAST_SYNC, 0);
        
        // Check if we need to sync
        boolean needsSync = false;
        
        if (lastOfflineSave > 0 && lastOnlineSave > 0) {
            // Both saves exist, check if they're different
            long timeDifference = Math.abs(lastOfflineSave - lastOnlineSave);
            if (timeDifference > 60000) { // More than 1 minute difference
                needsSync = true;
            }
        } else if (lastOfflineSave > lastSync || lastOnlineSave > lastSync) {
            // One save is newer than last sync
            needsSync = true;
        }
        
        if (needsSync) {
            scheduleSyncWork(context);
        }
    }
    
    /**
     * Schedule background sync work
     */
    private static void scheduleSyncWork(Context context) {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
            
        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(ProgressSyncWorker.class)
            .setConstraints(constraints)
            .setInitialDelay(5, TimeUnit.SECONDS) // Wait 5 seconds before syncing
            .build();
            
        WorkManager.getInstance(context).enqueue(syncWork);
        Log.d(TAG, "Scheduled sync work");
    }
    
    /**
     * Force sync between online and offline
     */
    public static void forceSync(Context context, AvatarSyncCallback callback) {
        // Load both versions and compare
        AvatarModel offlineAvatar = AvatarManager.loadAvatarOffline(context);
        AvatarManager.loadAvatarOnline(new AvatarManager.AvatarLoadCallback() {
            @Override
            public void onLoaded(AvatarModel onlineAvatar) {
                if (offlineAvatar == null) {
                    // No offline avatar, use online
                    AvatarManager.saveAvatarOffline(context, onlineAvatar);
                    callback.onSyncComplete(onlineAvatar, "Loaded from online");
                    return;
                }
                
                // Compare timestamps to determine which is newer
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                long lastOfflineSave = prefs.getLong(KEY_LAST_OFFLINE_SAVE, 0);
                long lastOnlineSave = prefs.getLong(KEY_LAST_ONLINE_SAVE, 0);
                
                AvatarModel finalAvatar;
                String syncMessage;
                
                if (lastOfflineSave > lastOnlineSave) {
                    // Offline is newer, sync to online
                    AvatarManager.saveAvatarOnline(offlineAvatar);
                    finalAvatar = offlineAvatar;
                    syncMessage = "Synced offline to online";
                } else {
                    // Online is newer, sync to offline
                    AvatarManager.saveAvatarOffline(context, onlineAvatar);
                    finalAvatar = onlineAvatar;
                    syncMessage = "Synced online to offline";
                }
                
                // Update sync timestamp
                prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply();
                callback.onSyncComplete(finalAvatar, syncMessage);
            }
            
            @Override
            public void onError(String message) {
                callback.onSyncError("Failed to load online avatar: " + message);
            }
        });
    }
    
    // Callback interfaces
    public interface AvatarLoadCallback {
        void onLoaded(AvatarModel avatar);
        void onError(String message);
    }
    
    public interface AvatarSyncCallback {
        void onSyncComplete(AvatarModel avatar, String message);
        void onSyncError(String message);
    }
    
    /**
     * Background worker for syncing progress
     */
    public static class ProgressSyncWorker extends Worker {
        
        public ProgressSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }
        
        @NonNull
        @Override
        public Result doWork() {
            Log.d(TAG, "Starting background sync");
            
            try {
                // Load both versions
                AvatarModel offlineAvatar = AvatarManager.loadAvatarOffline(getApplicationContext());
                
                // Load online avatar
                final boolean[] syncComplete = {false};
                final Result[] result = {Result.failure()};
                
                AvatarManager.loadAvatarOnline(new AvatarManager.AvatarLoadCallback() {
                    @Override
                    public void onLoaded(AvatarModel onlineAvatar) {
                        if (offlineAvatar == null) {
                            // No offline avatar, sync online to offline
                            AvatarManager.saveAvatarOffline(getApplicationContext(), onlineAvatar);
                            result[0] = Result.success();
                            syncComplete[0] = true;
                            return;
                        }
                        
                        // Compare and sync
                        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        long lastOfflineSave = prefs.getLong(KEY_LAST_OFFLINE_SAVE, 0);
                        long lastOnlineSave = prefs.getLong(KEY_LAST_ONLINE_SAVE, 0);
                        
                        if (lastOfflineSave > lastOnlineSave) {
                            // Offline is newer
                            AvatarManager.saveAvatarOnline(offlineAvatar);
                        } else {
                            // Online is newer or equal
                            AvatarManager.saveAvatarOffline(getApplicationContext(), onlineAvatar);
                        }
                        
                        // Update sync timestamp
                        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply();
                        result[0] = Result.success();
                        syncComplete[0] = true;
                    }
                    
                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Sync failed: " + message);
                        result[0] = Result.failure();
                        syncComplete[0] = true;
                    }
                });
                
                // Wait for async operation to complete
                int attempts = 0;
                while (!syncComplete[0] && attempts < 100) { // 10 second timeout
                    try {
                        Thread.sleep(100);
                        attempts++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return Result.failure();
                    }
                }
                
                return result[0];
                
            } catch (Exception e) {
                Log.e(TAG, "Sync worker failed", e);
                return Result.failure();
            }
        }
    }
}
