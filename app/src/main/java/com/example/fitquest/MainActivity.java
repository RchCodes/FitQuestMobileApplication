package com.example.fitquest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

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
            expBar.setMax(1);
            expBar.setProgress(1);
            expText.setText("MAX");
        } else {
            int prevLevelXp = currentLevel > 1 ? LevelProgression.getMaxXpForLevel(currentLevel - 1) : 0;
            int currentLevelMaxXp = LevelProgression.getMaxXpForLevel(currentLevel);
            int xpInLevel = Math.max(avatar.getXp() - prevLevelXp, 0);
            int xpNeeded = Math.max(currentLevelMaxXp - prevLevelXp, 1);

            expBar.setMax(xpNeeded);
            expBar.setProgress(Math.min(xpInLevel, xpNeeded));
            expText.setText(xpInLevel + "/" + xpNeeded);
        }
    }

    private void setupButtons() {
        findViewById(R.id.profile_section).setOnClickListener(v -> {
            profile = new Profile(this, googleSignInLauncher);
            profile.show();
        });
        findViewById(R.id.settings_button).setOnClickListener(v -> new Settings(this).show());
        findViewById(R.id.store_button).setOnClickListener(v -> startActivity(new Intent(this, StoreActivity.class)));
        findViewById(R.id.quest_button).setOnClickListener(v -> new Quest(this).show());
        findViewById(R.id.goals_button).setOnClickListener(v -> new Goals(this, avatar).show());
        findViewById(R.id.gear_button).setOnClickListener(v -> startActivity(new Intent(this, GearActivity.class)));
        findViewById(R.id.friends_button).setOnClickListener(v -> startActivity(new Intent(this, AvatarOverviewActivity.class)));
        findViewById(R.id.arena_button).setOnClickListener(v -> startActivity(new Intent(this, ArenaActivity.class)));
        findViewById(R.id.challenge_button).setOnClickListener(v -> startActivity(new Intent(this, LevelSelectActivity.class)));
        findViewById(R.id.stats_button).setOnClickListener(v -> new CharacterStats(this, avatar).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        QuestManager.setQuestProgressListener(this);
        MusicManager.onActivityResume(this);
        
        // Load profile info and refresh
        loadProfileInfo();
        refreshProfile();
        
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                setFitAuthorized(true);
                readDailySteps();
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
}
