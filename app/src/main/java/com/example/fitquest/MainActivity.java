package com.example.fitquest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity implements QuestManager.QuestProgressListener {

    private static final String PREFS_NAME = "fitquest_prefs";
    private static final String KEY_FIT_AUTHORIZED = "fit_authorized";

    private AvatarModel avatar;
    private AvatarDisplayManager avatarHelper;
    private Profile profile;

    private ImageView userIcon;
    private TextView playerName, playerLevel, coins, expText, rankName;
    private ImageView rankIcon;
    private ProgressBar expBar;

    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001;

    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    private Handler idleHandler = new Handler(Looper.getMainLooper());
    private Runnable idleRunnable;
    private boolean isIdleAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupAvatarHelper();
        setupLaunchers();
        loadAvatarWithOnlineFallback();
        loadProfileInfo();
        setupButtons();
        initGoogleFit();
        MusicManager.start(this);
        startPeriodicRefresh();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "step_quest_update",
                ExistingPeriodicWorkPolicy.REPLACE,
                new PeriodicWorkRequest.Builder(StepQuestWorker.class, 15, TimeUnit.MINUTES)
                        .build()
        );
    }

    private void bindViews() {
        userIcon = findViewById(R.id.user_icon);
        playerName = findViewById(R.id.player_name);
        playerLevel = findViewById(R.id.player_level);
        coins = findViewById(R.id.coins);
        expBar = findViewById(R.id.exp_bar);
        expText = findViewById(R.id.exp_text_overlay);
        rankName = findViewById(R.id.rank_name);
        rankIcon = findViewById(R.id.rank_icon);
    }

    private void setupAvatarHelper() {
        avatarHelper = new AvatarDisplayManager(
                this,
                findViewById(R.id.baseBodyLayer),
                findViewById(R.id.outfitLayer),
                findViewById(R.id.weaponLayer),
                findViewById(R.id.hairOutlineLayer),
                findViewById(R.id.hairFillLayer),
                findViewById(R.id.eyesOutlineLayer),
                findViewById(R.id.eyesFillLayer),
                findViewById(R.id.noseLayer),
                findViewById(R.id.lipsLayer)
        );

    }

    private void setupLaunchers() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            GoogleSignInAccount account = GoogleSignIn
                                    .getSignedInAccountFromIntent(result.getData())
                                    .getResult();
                            if (account != null && profile != null) {
                                profile.handleGoogleLink(account);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void loadAvatarWithOnlineFallback() {
        // Use ProgressSyncManager for intelligent loading
        ProgressSyncManager.loadProgress(this, new ProgressSyncManager.AvatarLoadCallback() {
            @Override
            public void onLoaded(AvatarModel loadedAvatar) {
                runOnUiThread(() -> {
                    avatar = loadedAvatar;
                    avatarHelper.loadAvatar(avatar);
                    loadProfileInfo();

                    avatar.setProfileChangeListener(updatedAvatar -> {
                        avatar = updatedAvatar;
                        avatarHelper.loadAvatar(avatar); // update avatar image
                        loadProfileInfo();               // refresh all profile UI
                    });

                    // Add idle animation for the avatar once it's loaded
                    startAvatarIdleAnimation();
                    startEyeBlinkAnimation();

                    // Save progress using sync manager
                    ProgressSyncManager.saveProgress(MainActivity.this, avatar, false); // Save offline first
                    
                    // If online is available, also save online
                    if (isNetworkAvailable()) {
                        ProgressSyncManager.saveProgress(MainActivity.this, avatar, true);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Log.e("MainActivity", "Avatar load failed: " + message);
                    Toast.makeText(MainActivity.this, "No avatar found. Redirecting to creation.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, AvatarCreationActivity.class));
                    finish();
                });
            }
        });
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }


    // ------------------ PROFILE INFO ------------------
    private void loadProfileInfo() {
        if (avatar == null) return;

        playerName.setText(avatar.getUsername());
        playerLevel.setText("LV. " + avatar.getLevel());
        coins.setText(String.valueOf(avatar.getCoins()));
        
        // Update rank display
        rankName.setText(avatar.getRankName());
        rankIcon.setImageResource(avatar.getRankDrawableRes());

        int currentLevel = avatar.getLevel();
        int maxLevel = LevelProgression.getMaxLevel();
        
        if (currentLevel >= maxLevel) {
            // User is at max level
            expBar.setMax(100);
            expBar.setProgress(100);
            expText.setText("MAX LEVEL");
        } else {
            int xpForNextLevel = LevelProgression.getMaxXpForLevel(currentLevel);
            int currentXp = Math.min(avatar.getXp(), xpForNextLevel);
            expBar.setMax(xpForNextLevel);
            expBar.setProgress(currentXp);
            expText.setText(currentXp + "/" + xpForNextLevel);
        }
    }

    private void setupButtons() {
        SoundManager.setOnClickListenerWithSound(findViewById(R.id.profile_section), v -> {
            profile = new Profile(this, googleSignInLauncher);
            profile.show();
        });
        SoundManager.setOnClickListenerWithSound(findViewById(R.id.settings_button), v -> new Settings(this).show());
        SoundManager.setOnClickListenerWithSound(findViewById(R.id.store_button), v -> startActivity(new Intent(this, StoreActivity.class)));
        SoundManager.setOnClickListenerWithSound(findViewById(R.id.quest_button), v -> new Quest(this).show());
        SoundManager.setOnClickListenerWithSound(findViewById(R.id.goals_button), v -> {
            Goals goalsDialog = new Goals(this, avatar);
            goalsDialog.refreshGoals(this); // Refresh with latest data
            goalsDialog.show();
        });
        SoundManager.setOnClickListenerWithSound(findViewById(R.id.gear_button), v -> startActivity(new Intent(this, GearActivity.class)));
        SoundManager.setOnClickListenerWithSound(findViewById(R.id.friends_button), v -> startActivity(new Intent(this, AvatarOverviewActivity.class)));
        SoundManager.setOnClickListenerWithSound(findViewById(R.id.arena_button), v -> startActivity(new Intent(this, ArenaActivity.class)));
        SoundManager.setOnClickListenerWithSound(findViewById(R.id.challenge_button), v -> startActivity(new Intent(this, LevelSelectActivity.class)));
        SoundManager.setOnClickListenerWithSound(findViewById(R.id.stats_button), v -> new CharacterStats(this, avatar).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        QuestManager.setQuestProgressListener(this);
        MusicManager.onActivityResume(this);
        if (avatar != null) startAvatarIdleAnimation();

        QuestManager.resetDailyStepCounterIfNeeded(this);
        
        // Load profile info and refresh
        loadProfileInfo();
        refreshProfile();
        updateStepQuestRealtime();

        // Check for pending sync if avatar exists
        if (avatar != null && isNetworkAvailable()) {
            ProgressSyncManager.forceSync(this, new ProgressSyncManager.AvatarSyncCallback() {
                @Override
                public void onSyncComplete(AvatarModel syncedAvatar, String message) {
                    runOnUiThread(() -> {
                        avatar = syncedAvatar;
                        avatarHelper.loadAvatar(avatar);
                        loadProfileInfo();
                        Log.d("MainActivity", "Sync completed: " + message);
                    });
                }
                
                @Override
                public void onSyncError(String message) {
                    Log.e("MainActivity", "Sync failed: " + message);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (avatar != null) {
            // Save offline always
            ProgressSyncManager.saveProgress(this, avatar, false);

            // Save online only if internet is available
            if (isNetworkAvailable()) {
                ProgressSyncManager.saveProgress(this, avatar, true);
            }
        }

        QuestManager.setQuestProgressListener(null);
        MusicManager.onActivityPause();
        stopAvatarIdleAnimation();

    }

    // ------------------ GOOGLE FIT ------------------
    private boolean isFitAuthorized() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_FIT_AUTHORIZED, false);
    }

    private void setFitAuthorized(boolean authorized) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_FIT_AUTHORIZED, authorized)
                .apply();
    }

    private void initGoogleFit() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account == null) return;

        if (!isFitAuthorized() && !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    fitnessOptions
            );
        } else {
            readDailySteps();
            startRealtimeStepTracking(); // ✅ Start live updates
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                setFitAuthorized(true);
                readDailySteps();
                startRealtimeStepTracking();
            } else {
                Toast.makeText(this, "Google Fit permissions denied", Toast.LENGTH_SHORT).show();
            }
        }

        if (profile != null) {
            CallbackManager fbManager = profile.getFacebookCallbackManager();
            if (fbManager != null) fbManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void readDailySteps() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) return;

        Fitness.getHistoryClient(this, account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(dataSet -> {
                    int totalSteps = dataSet.isEmpty() ? 0 :
                            dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                    QuestManager.reportExerciseResult(this, "steps", totalSteps);
                })
                .addOnFailureListener(e -> Log.e("FitQuest", "Failed to read steps", e));
    }

    private void updateStepQuestRealtime() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) return;

        QuestManager.resetDailyStepCounterIfNeeded(this);

        Fitness.getHistoryClient(this, account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(dataSet -> {
                    int totalSteps = 0;
                    if (dataSet != null && !dataSet.isEmpty()) {
                        totalSteps = dataSet.getDataPoints().get(0)
                                .getValue(Field.FIELD_STEPS)
                                .asInt();
                    }

                    // Report progress to QuestManager
                    QuestManager.reportSteps(this, totalSteps);

                })
                .addOnFailureListener(e -> Log.e("FitQuest", "Failed to read steps", e));
    }


    private void startRealtimeStepTracking() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) return;



        Fitness.getSensorsClient(this, account)
                .add(new com.google.android.gms.fitness.request.SensorRequest.Builder()
                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .setSamplingRate(5, java.util.concurrent.TimeUnit.SECONDS)
                                .build(),
                        dataPoint -> {
                            QuestManager.resetDailyStepCounterIfNeeded(this);

                            int steps = dataPoint.getValue(Field.FIELD_STEPS).asInt();

                            // Add to quest directly, using delta-safe method
                            QuestManager.addToStepQuest(steps);

                            Log.d("FitQuest", "Realtime steps: +" + steps);
                        })
                .addOnSuccessListener(unused -> Log.d("FitQuest", "Realtime step tracking started"))
                .addOnFailureListener(e -> Log.e("FitQuest", "Failed to start realtime step tracking", e));
    }


    // ------------------ QUEST LISTENER ------------------
    @Override
    public void onAvatarUpdated(AvatarModel updatedAvatar) {
        if (updatedAvatar == null) return;
        this.avatar = updatedAvatar;
        avatarHelper.loadAvatar(avatar);
        loadProfileInfo();
    }

    @Override
    public void onQuestProgressUpdated(QuestModel quest) {}

    @Override
    public void onQuestCompleted(QuestModel quest, boolean leveledUp) {
        avatar = AvatarManager.loadAvatarOffline(this);
        refreshProfile();
    }

    public void refreshProfile() {
        loadProfileInfo();
    }

    private void startPeriodicRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Refresh avatar data from storage
                if (avatar != null) {
                    AvatarModel updatedAvatar = AvatarManager.loadAvatarOffline(MainActivity.this);
                    if (updatedAvatar != null) {
                        avatar = updatedAvatar;
                        loadProfileInfo();
                    }
                }
                
                // Schedule next refresh in 2 seconds
                refreshHandler.postDelayed(this, 2000);
            }
        };
        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    private void startAvatarIdleAnimation() {
        if (isIdleAnimating) return;
        isIdleAnimating = true;

        // Collect all non-face avatar parts
        ImageView[] animatedLayers = new ImageView[]{
                findViewById(R.id.baseBodyLayer),
                findViewById(R.id.outfitLayer),
                findViewById(R.id.weaponLayer),
                findViewById(R.id.hairOutlineLayer),
                findViewById(R.id.hairFillLayer)
        };

        // Use same target Y for all parts so entire body moves together
        idleRunnable = new Runnable() {
            boolean goingUp = true;

            @Override
            public void run() {
                float targetShift = goingUp ? -6f : 6f;

                for (ImageView layer : animatedLayers) {
                    if (layer != null) {
                        // animate smoothly, same shift for all
                        layer.animate()
                                .translationY(targetShift)
                                .setDuration(1000)
                                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                                .start();
                    }
                }

                goingUp = !goingUp;
                idleHandler.postDelayed(this, 1000);
            }
        };

        idleHandler.post(idleRunnable);
    }

    private void stopAvatarIdleAnimation() {
        isIdleAnimating = false;
        if (idleHandler != null && idleRunnable != null) {
            idleHandler.removeCallbacks(idleRunnable);
        }

        int[] layerIds = {
                R.id.baseBodyLayer,
                R.id.outfitLayer,
                R.id.weaponLayer,
                R.id.hairOutlineLayer,
                R.id.hairFillLayer
        };

        for (int id : layerIds) {
            ImageView layer = findViewById(id);
            if (layer != null) {
                layer.animate().translationY(0).setDuration(300).start();
            }
        }
    }

    private void startEyeBlinkAnimation() {
        ImageView eyesFill = findViewById(R.id.eyesFillLayer);
        ImageView eyesOutline = findViewById(R.id.eyesOutlineLayer);

        if (eyesFill == null || eyesOutline == null) return;

        // Make sure scaling happens in place (no movement)
        eyesFill.post(() -> {
            eyesFill.setPivotX(eyesFill.getWidth() / 2f);
            eyesFill.setPivotY(eyesFill.getHeight() / 2f);
        });
        eyesOutline.post(() -> {
            eyesOutline.setPivotX(eyesOutline.getWidth() / 2f);
            eyesOutline.setPivotY(eyesOutline.getHeight() / 2f);
        });

        Handler blinkHandler = new Handler(Looper.getMainLooper());
        Runnable blinkRunnable = new Runnable() {
            @Override
            public void run() {
                if (eyesFill != null && eyesOutline != null) {
                    // Blink close
                    eyesFill.animate().scaleY(0.1f).alpha(0.8f).setDuration(120).start();
                    eyesOutline.animate().scaleY(0.1f).alpha(0.8f).setDuration(120)
                            .withEndAction(() -> {
                                // Blink open
                                eyesFill.animate().scaleY(1f).alpha(1f).setDuration(150).start();
                                eyesOutline.animate().scaleY(1f).alpha(1f).setDuration(150).start();
                            }).start();
                }

                blinkHandler.postDelayed(this, 4000 + (long) (Math.random() * 2000));
            }
        };

        blinkHandler.postDelayed(blinkRunnable, 3000);
    }

}
