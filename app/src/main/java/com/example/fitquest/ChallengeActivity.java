package com.example.fitquest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class ChallengeActivity extends BaseActivity implements CombatContext.CombatListener {
    // ...existing fields...

    private AlertDialog rewardDialog;
    private boolean isRewardDialogShown = false;
    private boolean isCombatEnded = false;
    private boolean isAnimatingTurn = false;
    private long lastSaveTime = 0;
    private static final long SAVE_COOLDOWN = 2000; // 2 seconds
    private boolean waitingForPlayerInput = false;

    private static final String TAG = "ChallengeActivity";

    // --- UI Components ---
    private ImageView imgEnemy;
    private FrameLayout effectsLayer;
    private ProgressBar playerHpBar, enemyHpBar, playerActionBar, enemyActionBar;
    private TextView playerHpText, enemyHpText, playerNameText, enemyNameText;
    private LinearLayout playerStatusEffects, enemyStatusEffects;
    private Button[] skillButtons = new Button[5];
    private TextView combatLog, challengeInfo;
    private ImageView btnPause;

    // Skill icons (ShapeableImageView in layout)
    private ShapeableImageView[] skillImages;

    // --- Combat System ---
    private Character playerCharacter;
    private Character enemyCharacter;
    private CombatContext combatContext;
    private Handler combatHandler;
    private Runnable combatTickRunnable;

    private boolean animationPlaying = false;

    // --- Gauntlet / Challenge ---
    private List<AvatarModel> challengeEnemies = new ArrayList<>(); // avatars used for CombatContext
    private List<EnemyModel> enemyModelsReceived = new ArrayList<>(); // original enemy models from LevelSelect (if any)
    private int currentEnemyIndex = 0;
    private int levelNumber = 1;
    private boolean combatActive = false;
    private boolean combatPaused = false;
    private Random random = new Random();
    private AvatarDisplayManager avatarHelper;

    private int totalEnemies;
    private int enemiesDefeated = 0;

    // Skill popup and pauseDialog are referenced in your original ChallengeActivity; keep optional
    private SkillInfoPopup skillPopup;
    private PauseDialog pauseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        // Read intent extras (LevelSelect sends enemies list + level)
        Intent intent = getIntent();
        if (intent != null) {
            // --- New preferred way (using enemyIds instead of direct Parcelable list) ---
            ArrayList<String> enemyIds = intent.getStringArrayListExtra("enemyIds");
            List<EnemyModel> enemies = new ArrayList<>();

            if (enemyIds != null) {
                for (String id : enemyIds) {
                    EnemyModel enemy = EnemyRepository.getEnemy(id);
                    if (enemy != null) {
                        enemies.add(enemy.spawn());
                    }
                }
            }

            // Fallback to old direct enemy list (for backward compatibility)
            ArrayList<EnemyModel> list = intent.getParcelableArrayListExtra("enemies");
            if (list != null && !list.isEmpty()) {
                enemyModelsReceived = list;
            } else if (!enemies.isEmpty()) {
                enemyModelsReceived = enemies;
            }

            levelNumber = intent.getIntExtra("level", 1);
        }


        skillPopup = new SkillInfoPopup();

        initializeUI();
        setupAvatarDisplay();    // initialize player avatar display manager and load avatar
        setupCombatHandler();
        loadPlayerCharacter();
        createChallengeEnemies(); // create challengeEnemies (converted from enemyModelsReceived or dynamic)
        startCombat();

    }

    private void initializeUI() {
        imgEnemy = findViewById(R.id.imgEnemy);
        effectsLayer = findViewById(R.id.effectsLayer);
        playerHpBar = findViewById(R.id.player_hp_bar);
        enemyHpBar = findViewById(R.id.enemy_hp_bar);
        playerActionBar = findViewById(R.id.playerActionBar);
        enemyActionBar = findViewById(R.id.enemyActionBar);
        playerHpText = findViewById(R.id.player_hp_text_overlay);
        enemyHpText = findViewById(R.id.enemy_hp_text_overlay);
        playerNameText = findViewById(R.id.tvPlayerName);
        enemyNameText = findViewById(R.id.tvEnemyName);
        playerStatusEffects = findViewById(R.id.playerStatusEffects);
        enemyStatusEffects = findViewById(R.id.enemyStatusEffects);

        skillButtons[0] = findViewById(R.id.skill1);
        skillButtons[1] = findViewById(R.id.skill2);
        skillButtons[2] = findViewById(R.id.skill3);
        skillButtons[3] = findViewById(R.id.skill4);
        skillButtons[4] = findViewById(R.id.skill5);

        skillImages = new ShapeableImageView[] {
                findViewById(R.id.imgSkill1),
                findViewById(R.id.imgSkill2),
                findViewById(R.id.imgSkill3),
                findViewById(R.id.imgSkill4),
                findViewById(R.id.imgSkill5)
        };

        combatLog = findViewById(R.id.combat_log);
        challengeInfo = findViewById(R.id.challenge_info);
        btnPause = findViewById(R.id.btnSettings);

        // Attach listeners to skill buttons and icons
        for (int i = 0; i < skillButtons.length; i++) {
            final int index = i;
            if (skillButtons[i] != null) {
                skillButtons[i].setOnClickListener(v -> {
                    showSkillPopup(v, index);
                    if (waitingForPlayerInput) {
                        List<SkillModel> skills = getAvailableSkills(playerCharacter);
                        if (index < skills.size()) {
                            SkillModel chosen = skills.get(index);
                            combatContext.onPlayerChosenSkill(chosen);
                            waitingForPlayerInput = false;
                            setSkillButtonsEnabled(false);
                        }
                    }
                });
            }
            if (skillImages.length > i && skillImages[i] != null) {
                final int idx = i;
                skillImages[i].setOnClickListener(v -> showSkillPopup(v, idx));
            }
        }

    }

    private void setSkillButtonsEnabled(boolean enabled) {
        for (Button btn : skillButtons) {
            if (btn != null) btn.setEnabled(enabled);
        }

        // Pause/Settings button
        if (btnPause != null) {
            btnPause.setOnClickListener(v -> {
                if (pauseDialog == null) {
                    pauseDialog = new PauseDialog(this, () -> {
                        // End combat safely and exit
                        if (combatActive && combatContext != null) {
                            combatContext.endCombat(false); // mark as lost
                            combatActive = false;
                        }
                        finish();
                    });

                    pauseDialog.dialog.setOnDismissListener(d -> combatPaused = false);
                }
                combatPaused = true;
                pauseDialog.show();
            });
        }

        updateChallengeInfo();
    }

    private void setupAvatarDisplay() {
        // Create AvatarDisplayManager for player (layered)
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

        // If avatar exists offline, load it
        AvatarModel avatar = AvatarManager.loadAvatarOffline(this);
        if (avatar != null) {
            avatarHelper.loadAvatar(avatar);
        } else {
            Toast.makeText(this, "No avatar found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCombatHandler() {
        combatHandler = new Handler(Looper.getMainLooper());
        combatTickRunnable = new Runnable() {
            @Override
            public void run() {
                // Only tick when combat active and not paused
                if (combatActive && combatContext != null && !combatPaused && !animationPlaying) {
                    try {
                        combatContext.tick(0.1); // 100ms tick
                        // keep UI in sync by calling the listener tick hook
                        try {
                            onCombatTick(0.1);
                        } catch (Exception e) {
                            Log.w(TAG, "onCombatTick(double) threw: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error during combat tick", e);
                    }
                }
                // schedule next tick regardless (keeps loop alive)
                combatHandler.postDelayed(this, 100);
            }
        };
    }

    private void loadPlayerCharacter() {
        AvatarModel playerAvatar = AvatarManager.loadAvatarOffline(this);
        if (playerAvatar == null) {
            Toast.makeText(this, "No avatar found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        playerCharacter = new Character(playerAvatar);
        updatePlayerUI();

        // ensure avatarHelper shows the player
        if (avatarHelper != null) {
            avatarHelper.loadAvatar(playerAvatar);
        }
    }

    /**
     * Create challenge enemies list.
     * If LevelSelect supplied EnemyModel list via intent, convert those into AvatarModel (light conversion).
     * Otherwise fallback to dynamic creation by levelNumber (your original logic).
     */
    private void createChallengeEnemies() {
        challengeEnemies.clear();
        currentEnemyIndex = 0;
        enemiesDefeated = 0;

        if (enemyModelsReceived != null && !enemyModelsReceived.isEmpty()) {
            // Convert supplied EnemyModel list to AvatarModel for Character consumption
            for (EnemyModel e : enemyModelsReceived) {
                if (e != null) {
                    e.loadSkillsFromRepo(); // Ensure skills/passives are loaded!
                    AvatarModel av = convertEnemyToAvatar(e);
                    challengeEnemies.add(av);
                }
            }
        } else {
            // fallback to dynamic generator you had previously
            totalEnemies = 3 + (levelNumber * 2);
            for (int i = 0; i < totalEnemies; i++) {
                AvatarModel enemy = createChallengeEnemy(i + 1);
                challengeEnemies.add(enemy);
            }
        }

        // If we have enemyModelsReceived but need to set totalEnemies for UI
        if (enemyModelsReceived != null && !enemyModelsReceived.isEmpty()) {
            totalEnemies = enemyModelsReceived.size();
        } else {
            totalEnemies = challengeEnemies.size();
        }

        selectNextEnemy();
    }

    /**
     * Convert EnemyModel -> AvatarModel (basic mapping)
     * This method fills username, level and core stats so Character and CombatContext work.
     */
    private AvatarModel convertEnemyToAvatar(EnemyModel enemy) {
        AvatarModel av = new AvatarModel();
        av.setUsername(enemy.getName());
        // set level: roughly scale by baseHp / 50 (heuristic) or use levelNumber
        int estimatedLevel = Math.max(1, levelNumber + (enemy.getBaseHp() / 100));
        av.setLevel(estimatedLevel);
        av.setPlayerClass("enemy");

        // Map stats roughly from EnemyModel base stats
        av.addStrength(enemy.getBaseStr());
        av.addEndurance(enemy.getBaseEnd());
        av.addAgility(enemy.getBaseAgi());
        // add reasonable defaults for other stats if methods exist
        av.addFlexibility(2);
        av.addStamina(5 + enemy.getBaseHp() / 50);

        // No layered gear for enemy — optional: set outfit to null
        return av;
    }

    private AvatarModel createChallengeEnemy(int enemyIndex) {
        AvatarModel enemyAvatar = new AvatarModel();

        int enemyLevel = levelNumber + enemyIndex - 1 + random.nextInt(3); // ±1-2 variation
        enemyAvatar.setLevel(enemyLevel);
        enemyAvatar.setUsername(getEnemyName(enemyIndex, enemyLevel));
        enemyAvatar.setPlayerClass(getRandomClass());

        int baseStat = 5 + enemyLevel * 3;
        enemyAvatar.addStrength(baseStat + random.nextInt(3));
        enemyAvatar.addEndurance(baseStat + random.nextInt(3));
        enemyAvatar.addAgility(baseStat + random.nextInt(3));
        enemyAvatar.addFlexibility(baseStat + random.nextInt(3));
        enemyAvatar.addStamina(baseStat + random.nextInt(3));

        addGearToEnemy(enemyAvatar, enemyLevel);

        return enemyAvatar;
    }

    private String getEnemyName(int index, int level) {
        String[] prefixes = {"Brutal", "Fierce", "Mighty", "Savage", "Relentless"};
        String[] suffixes = {"Warrior", "Champion", "Gladiator", "Berserker"};
        return prefixes[random.nextInt(prefixes.length)] + " " +
                suffixes[random.nextInt(suffixes.length)] + " (Lv." + level + ")";
    }

    private String getRandomClass() {
        String[] classes = {"warrior", "rogue", "tank"};
        return classes[random.nextInt(classes.length)];
    }

    private void addGearToEnemy(AvatarModel enemy, int level) {
        List<GearModel> allGear = GearRepository.getAllGear();
        Collections.shuffle(allGear);
        int gearCount = Math.min(3 + (level / 3), 5);
        int added = 0;
        for (GearModel gear : allGear) {
            if (added >= gearCount) break;
            if (gear.getClassRestriction().equals("UNIVERSAL") ||
                    gear.getClassRestriction().equalsIgnoreCase(enemy.getPlayerClass())) {
                enemy.addGear(gear.getId());
                enemy.equipGear(gear.getType(), gear.getId());
                added++;
            }
        }
    }

    private void selectNextEnemy() {
        if (challengeEnemies.isEmpty()) return;
        if (currentEnemyIndex < 0) currentEnemyIndex = 0;
        if (currentEnemyIndex >= challengeEnemies.size()) return;

        AvatarModel next = challengeEnemies.get(currentEnemyIndex);
        enemyCharacter = new Character(next);
        updateEnemyUI();

        // If enemyModelsReceived supplies sprite resources, set imgEnemy accordingly
        if (enemyModelsReceived != null && currentEnemyIndex < enemyModelsReceived.size()) {
            EnemyModel em = enemyModelsReceived.get(currentEnemyIndex);
            if (imgEnemy != null && em != null && em.getSpriteResId() != 0) {
                imgEnemy.setImageResource(em.getSpriteResId());
            }
        } else {
            // otherwise hide/clear imgEnemy or set default
            // (leave whatever is in XML if you prefer)
        }
    }

    private void startCombat() {
        if (playerCharacter == null || enemyCharacter == null) return;

        combatContext = new CombatContext(playerCharacter, enemyCharacter, CombatContext.Mode.CHALLENGE, this);
        combatContext.startCombat();

        combatActive = true;

        // ensure we do not post multiple runnables
        if (combatHandler != null && combatTickRunnable != null) {
            combatHandler.removeCallbacks(combatTickRunnable);
            combatHandler.post(combatTickRunnable);
        }
    }

    private void useSkill(int skillIndex) {
        if (playerCharacter == null || combatContext == null) return;
        List<SkillModel> skills = getAvailableSkills(playerCharacter);
        if (skillIndex < skills.size()) {
            combatContext.playerUseSkill(skills.get(skillIndex).getId());
        }
    }

    private List<SkillModel> getAvailableSkills(Character character) {
        List<SkillModel> available = new ArrayList<>();
        for (SkillModel skill : character.getActiveSkills()) {
            if (skill.getLevelUnlock() <= character.getAvatar().getLevel() &&
                    !skill.isOnCooldown() &&
                    skill.getAbCost() <= character.getActionBar()) {
                available.add(skill);
            }
        }
        return available;
    }

    private void updatePlayerUI() {
        if (playerCharacter == null) return;
        if (playerNameText != null) playerNameText.setText(playerCharacter.getName());
        if (playerHpBar != null && playerHpText != null) updateHealthBar(playerHpBar, playerHpText, playerCharacter);
        if (playerActionBar != null) updateActionBar(playerActionBar, playerCharacter);
        updateSkillButtons();
    }

    private void updateEnemyUI() {
        if (enemyCharacter == null) return;
        if (enemyNameText != null) enemyNameText.setText(enemyCharacter.getName());
        if (enemyHpBar != null && enemyHpText != null) updateHealthBar(enemyHpBar, enemyHpText, enemyCharacter);
        if (enemyActionBar != null) updateActionBar(enemyActionBar, enemyCharacter);
    }

    private void updateHealthBar(ProgressBar hpBar, TextView hpText, Character character) {
        int currentHp = character.getCurrentHp();
        int maxHp = character.getMaxHp();
        if (maxHp <= 0) maxHp = 1;
        if (currentHp < 0) currentHp = 0;
        if (currentHp > maxHp) currentHp = maxHp;
        hpBar.setMax(maxHp);
        hpBar.setProgress(currentHp);
        hpText.setText(currentHp + "/" + maxHp);
    }

    private void updateActionBar(ProgressBar actionBar, Character character) {
        actionBar.setMax(100);
        int ab = character.getActionBar();
        if (ab < 0) ab = 0;
        if (ab > 100) ab = 100;
        actionBar.setProgress(ab);
    }

    private void updateSkillButtons() {
        if (playerCharacter == null) return;

        List<SkillModel> skills = getAvailableSkills(playerCharacter);
        for (int i = 0; i < skillButtons.length; i++) {
            Button btn = skillButtons[i];
            if (btn == null) continue;

            if (i < skills.size()) {
                SkillModel s = skills.get(i);
                btn.setText(s.getName());
                btn.setEnabled(true);
                btn.setVisibility(View.VISIBLE);

                // update icon if available
                if (skillImages != null && i < skillImages.length && skillImages[i] != null) {
                    if (s.getIconRes() != 0) {
                        skillImages[i].setImageResource(s.getIconRes());
                        skillImages[i].setVisibility(View.VISIBLE);
                    } else {
                        skillImages[i].setImageResource(R.drawable.lock_2);
                        skillImages[i].setVisibility(View.VISIBLE);
                    }
                }
            } else {
                btn.setText("");
                btn.setEnabled(false);
                btn.setVisibility(View.GONE);
                if (skillImages != null && i < skillImages.length && skillImages[i] != null) {
                    skillImages[i].setVisibility(View.GONE);
                }
            }
        }
    }

    private void addCombatLog(String message) {
        if (combatLog != null) {
            String currentLog = combatLog.getText() != null ? combatLog.getText().toString() : "";
            combatLog.setText(message + "\n" + currentLog);
        } else {
            Log.i(TAG, message);
        }
    }

    private void updateChallengeInfo() {
        if (challengeInfo != null) {
            challengeInfo.setText("Level " + levelNumber + " - Enemy " + (currentEnemyIndex + 1) + "/" + Math.max(1, totalEnemies));
        }
    }

    private void showSkillPopup(View anchor, int skillIndex) {
        if (playerCharacter == null) return;
        List<SkillModel> skills = getAvailableSkills(playerCharacter);
        if (skillIndex < skills.size()) {
            skillPopup.show(anchor, skills.get(skillIndex));
        }
    }

    // ---------------- CombatListener callbacks ----------------

    @Override
    public void onCombatStarted(Character player, Character enemy, CombatContext.Mode mode) {
        runOnUiThread(() -> {
            addCombatLog("Combat started: " + player.getName() + " vs " + enemy.getName());
            updatePlayerUI();
            updateEnemyUI();
        });
    }

    @Override
    public void onActionBarUpdated(Character c) {
        runOnUiThread(() -> {
            if (c == playerCharacter) {
                if (playerActionBar != null) updateActionBar(playerActionBar, c);
                updateSkillButtons();
            } else if (c == enemyCharacter) {
                if (enemyActionBar != null) updateActionBar(enemyActionBar, c);
            }
        });
    }

    @Override
    public void onCombatTick(double deltaTime) {
        runOnUiThread(() -> {
            if (playerCharacter != null && playerHpBar != null && playerHpText != null) {
                updateHealthBar(playerHpBar, playerHpText, playerCharacter);
                updateActionBar(playerActionBar, playerCharacter);
            }
            if (enemyCharacter != null && enemyHpBar != null && enemyHpText != null) {
                updateHealthBar(enemyHpBar, enemyHpText, enemyCharacter);
                updateActionBar(enemyActionBar, enemyCharacter);
            }
        });
    }

    // no-arg variant requested by some listeners -> call double-arg version
    @Override
    public void onCombatTick() {
        onCombatTick(0.0);
    }

    @Override
    public void onTurnStart(Character c, CombatContext ctx) {
        // status effects processed in CombatContext tick; UI updates if needed
    }

    @Override
    public void onSkillUsed(Character user, SkillModel skill, Character target, String logEntry) {
        runOnUiThread(() -> addCombatLog(logEntry));
    }

    @Override
    public void onDamageApplied(Character attacker, Character defender, int amount, String logEntry) {
        runOnUiThread(() -> {
            addCombatLog(logEntry);

            if (defender == playerCharacter) {
                updateHealthBar(playerHpBar, playerHpText, playerCharacter);
                playHitAnimation(findViewById(R.id.imgPlayer), null);

                if (!playerCharacter.isAlive()) {
                    playPlayerDeathAnimation(findViewById(R.id.imgPlayer), null);
                }

            } else if (defender == enemyCharacter) {
                updateHealthBar(enemyHpBar, enemyHpText, enemyCharacter);
                playHitAnimation(findViewById(R.id.imgEnemy), null);

                if (!enemyCharacter.isAlive()) {
                    playDeathAnimation(findViewById(R.id.imgEnemy), null);
                }
            }
        });
    }


    @Override
    public void onStatusApplied(Character target, StatusEffect effect, String logEntry) {
        runOnUiThread(() -> addCombatLog(logEntry));
    }

    @Override
    public void onLog(String entry) {
        runOnUiThread(() -> addCombatLog(entry));
    }

    /**
     * Called by CombatContext when combat finishes (normal path).
     * We'll reward player on victory and move to next enemy or finish challenge.
     */
    @Override
    public void onCombatEnded(CombatContext.Result result, Character winner, Character loser) {
        runOnUiThread(() -> {
            if (isCombatEnded) return; // Prevent duplicate triggers
            isCombatEnded = true;
            combatActive = false;

            if (combatHandler != null) {
                combatHandler.removeCallbacks(combatTickRunnable);
            }

            AvatarModel playerAvatar = playerCharacter.getAvatar();
            boolean playerWon = (result == CombatContext.Result.PLAYER_WON);
            String resultMessage;

            if (playerWon) {
                resultMessage = "Victory! You defeated " + loser.getName();

                // --- Rewards are handled ONLY here ---
                int xpGained = 20 + (levelNumber * 5);
                int coinsGained = 50 + (levelNumber * 10);
                List<String> itemsGained = new ArrayList<>();

                boolean leveledUp = playerAvatar.addXp(xpGained);
                playerAvatar.addCoins(coinsGained);

                // Save progress locally & remotely
                ProgressSyncManager.saveProgress(this, playerAvatar, false);
                if (isNetworkAvailable()) ProgressSyncManager.saveProgress(this, playerAvatar, true);

                showCombatRewardsDialog(xpGained, coinsGained, itemsGained, () -> {
                    if (leveledUp) {
                        QuestRewardManager.showLevelUpPopup(this, playerAvatar.getLevel(), playerAvatar.getRank());
                    }

                    // Next enemy or finish
                    currentEnemyIndex++;
                    if (currentEnemyIndex < challengeEnemies.size()) {
                        selectNextEnemy();
                        new Handler(Looper.getMainLooper()).postDelayed(this::startCombat, 1500);
                    } else {
                        addCombatLog("Challenge cleared!");
                        Toast.makeText(this, "Challenge cleared!", Toast.LENGTH_LONG).show();
                        onLevelCompleted(levelNumber);
                    }
                });

            } else {
                resultMessage = "Defeat! You were defeated by " + winner.getName();

                int xpGained = 5;
                boolean leveledUp = playerAvatar.addXp(xpGained);

                ProgressSyncManager.saveProgress(this, playerAvatar, false);
                if (isNetworkAvailable()) ProgressSyncManager.saveProgress(this, playerAvatar, true);

                if (leveledUp) {
                    QuestRewardManager.showLevelUpPopup(this, playerAvatar.getLevel(), playerAvatar.getRank());
                }

                showPlayerDefeatDialog();
            }

            addCombatLog(resultMessage);
        });
    }

    /**
     * Called by CombatContext.endCombat(playerWon)
     * This is a lightweight fallback hook — no reward logic here.
     * It simply routes to onCombatEnded() safely if not already triggered.
     */
    @Override
    public void onCombatEnd(boolean playerWon) {
        runOnUiThread(() -> {
            if (isCombatEnded) return; // Prevent duplicate calls
            isCombatEnded = true;

            combatActive = false;
            if (combatHandler != null) combatHandler.removeCallbacks(combatTickRunnable);

            addCombatLog("Combat forcibly ended: " + (playerWon ? "PLAYER_WON" : "PLAYER_LOST"));

            // Delegate to onCombatEnded() for unified logic
            CombatContext.Result result = playerWon ?
                    CombatContext.Result.PLAYER_WON :
                    CombatContext.Result.ENEMY_WON;

            // Pass dummy winner/loser safely
            onCombatEnded(result, playerWon ? enemyCharacter : playerCharacter,
                    playerWon ? playerCharacter : enemyCharacter);
        });
    }

    // Manual skill selection: enable buttons and wait for user input
    @Override
    public SkillModel onRequestPlayerSkillChoice(List<SkillModel> availableSkills, Character player, Character enemy) {
        runOnUiThread(() -> {
            waitingForPlayerInput = false;
            setSkillButtonsEnabled(false);
        });
        return null; // return null so CombatContext waits for onPlayerChosenSkill
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    private void playHitAnimation(View view, Runnable onEnd) {
        if (view == null) {
            if (onEnd != null) onEnd.run();
            return;
        }

        animationPlaying = true; // pause combat ticks

        // --- Shake animation ---
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, 20, -20, 15, -15, 10, -10, 5, -5, 0);
        shake.setDuration(400);

        // --- Flash (color overlay / alpha blink) ---
        ValueAnimator flash = ValueAnimator.ofFloat(0f, 1f);
        flash.setDuration(200);
        flash.setRepeatMode(ValueAnimator.REVERSE);
        flash.setRepeatCount(1);
        flash.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setAlpha(1f - (0.3f * value));
            if (view instanceof ImageView) {
                ((ImageView) view).setColorFilter(
                        android.graphics.Color.argb((int) (150 * value), 255, 0, 0),
                        android.graphics.PorterDuff.Mode.SRC_ATOP
                );
            }
        });

        flash.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setAlpha(1f);
                if (view instanceof ImageView) ((ImageView) view).clearColorFilter();
            }
        });

        // --- Combine both animations ---
        AnimatorSet set = new AnimatorSet();
        set.playTogether(shake, flash);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationPlaying = false; // resume ticks
                if (onEnd != null) onEnd.run();
            }
        });
        set.start();
    }


    private void playDeathAnimation(View view, Runnable onEnd) {
        if (view == null) {
            if (onEnd != null) onEnd.run();
            return;
        }

        animationPlaying = true;

        // Fade out + fall down + shrink
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setDuration(800);

        ObjectAnimator fallDown = ObjectAnimator.ofFloat(view, "translationY", 0f, 200f);
        fallDown.setDuration(800);
        fallDown.setInterpolator(new android.view.animation.AccelerateInterpolator());

        ObjectAnimator shrinkX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.6f);
        ObjectAnimator shrinkY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.6f);
        shrinkX.setDuration(800);
        shrinkY.setDuration(800);

        AnimatorSet deathSet = new AnimatorSet();
        deathSet.playTogether(fadeOut, fallDown, shrinkX, shrinkY);
        deathSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                animationPlaying = false; // resume ticks
                if (onEnd != null) onEnd.run();
            }
        });
        deathSet.start();
    }


    private void playPlayerDeathAnimation(View view, Runnable onEnd) {
        if (view == null) {
            if (onEnd != null) onEnd.run();
            return;
        }

        animationPlaying = true;

        // Step 1: Flash faintly
        ValueAnimator flash = ValueAnimator.ofFloat(0f, 1f);
        flash.setDuration(200);
        flash.setRepeatMode(ValueAnimator.REVERSE);
        flash.setRepeatCount(2);
        flash.addUpdateListener(animator -> {
            float value = (float) animator.getAnimatedValue();
            view.setAlpha(1f - (0.4f * value));
        });

        // Step 2: Fall back, fade out, scale down
        ObjectAnimator fallBack = ObjectAnimator.ofFloat(view, "translationY", 0f, 100f);
        fallBack.setInterpolator(new android.view.animation.AccelerateInterpolator());
        fallBack.setDuration(1000);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setDuration(1000);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f);
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);

        AnimatorSet flashSet = new AnimatorSet();
        flashSet.playSequentially(flash);

        AnimatorSet fallFadeScale = new AnimatorSet();
        fallFadeScale.playTogether(fallBack, scaleX, scaleY, fadeOut);

        AnimatorSet deathSet = new AnimatorSet();
        deathSet.playSequentially(flashSet, fallFadeScale);
        deathSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                animationPlaying = false;
                if (onEnd != null) onEnd.run();
                showPlayerDefeatDialog();
            }
        });
        deathSet.start();

    }


    private void showPlayerDefeatDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Defeat")
                .setMessage("You have been defeated... Try again?")
                .setPositiveButton("Retry", (dialog, which) -> {
                    recreate(); // reloads activity (simple reset)
                })
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showCombatRewardsDialog(int xpGained, int coinsGained, List<String> itemsGained, Runnable onDismiss) {
        runOnUiThread(() -> {
            StringBuilder message = new StringBuilder();
            message.append("You earned:\n")
                    .append(xpGained).append(" XP\n")
                    .append(coinsGained).append(" Coins\n");

            if (itemsGained != null && !itemsGained.isEmpty()) {
                message.append("Items:\n");
                for (String item : itemsGained) {
                    message.append("- ").append(item).append("\n");
                }
            }

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Rewards")
                    .setMessage(message.toString())
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        if (onDismiss != null) onDismiss.run();
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    private void onLevelCompleted(int level) {
        // Update player progress
        SharedPreferences prefs = getSharedPreferences("PlayerPrefs", Context.MODE_PRIVATE);
        int currentLevel = prefs.getInt("playerLevel", 1);
        if (level >= currentLevel) {
            prefs.edit().putInt("playerLevel", level + 1).apply(); // unlock next level
        }

        // Return result to LevelSelectActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("levelCompleted", level);
        setResult(RESULT_OK, resultIntent);
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        MusicManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        combatActive = false;
        if (combatHandler != null) {
            combatHandler.removeCallbacks(combatTickRunnable);
        }
    }
}
