package com.example.fitquest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ArenaCombatActivity extends BaseActivity implements CombatContext.CombatListener {

    private static final String TAG = "ArenaCombatActivity";

    private ShapeableImageView[] skillIcons;
    private Button[] skillButtons;
    private SkillInfoPopup popup;

    // UI Components
    private ProgressBar playerHpBar, enemyHpBar, playerActionBar, enemyActionBar;
    private TextView playerHpText, enemyHpText, playerNameText, enemyNameText;
    private LinearLayout playerStatusEffects, enemyStatusEffects;
    private TextView combatLog;

    // Avatar display managers (fields so we can update when avatars change)
    private AvatarDisplayManager playerDisplay;
    private AvatarDisplayManager enemyDisplay;

    // Combat System
    private Character playerCharacter;
    private Character enemyCharacter;
    private CombatContext combatContext;
    private Handler combatHandler;
    private Runnable combatTickRunnable;

    // AI Ghost System
    private List<AvatarModel> aiGhosts = new ArrayList<>();
    private AvatarModel currentEnemyGhost;
    private int currentGhostIndex = 0;
    private final int MAX_GHOSTS = 3;

    // Combat state
    private boolean combatActive = false;
    private Random random = new Random();

    private boolean waitingForPlayerInput = false;
    private SkillModel chosenSkill = null;

    private boolean isPaused = false;
    private PauseDialog pauseDialog;

    private ImageView btnPause;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arena_combat);

        initializeUI();
        initAvatarDisplays();         // prepare AvatarDisplayManager instances (they'll load when avatars are available)
        setupCombatHandler();
        loadPlayerCharacter();
        loadAIGhosts();               // this will select next enemy and start combat when ready

        MusicManager.start(this);
    }

    private List<SkillModel> getPlayerSkills() {
        if (playerCharacter == null || playerCharacter.getActiveSkills() == null) {
            return new ArrayList<>();
        }
        return playerCharacter.getActiveSkills();
    }

    private void initializeUI() {
        // Bind UI components
        playerHpBar = findViewById(R.id.player_hp_bar);
        enemyHpBar = findViewById(R.id.enemy_hp_bar);
        playerActionBar = findViewById(R.id.playerActionBar);
        enemyActionBar = findViewById(R.id.enemyActionBar);

        playerHpText = findViewById(R.id.player_hp_text_overlay);
        enemyHpText = findViewById(R.id.enemy_hp_text_overlay);
        playerNameText = findViewById(R.id.tvPlayerName);
        enemyNameText = findViewById(R.id.tvEnemyName);

        // These may be missing depending on layout — keep findViewById but guard usage
        playerStatusEffects = findViewById(R.id.playerStatusEffects);
        enemyStatusEffects = findViewById(R.id.enemyStatusEffects);

        skillButtons = new Button[5];

        skillButtons[0] = findViewById(R.id.skill1);
        skillButtons[1] = findViewById(R.id.skill2);
        skillButtons[2] = findViewById(R.id.skill3);
        skillButtons[3] = findViewById(R.id.skill4);
        skillButtons[4] = findViewById(R.id.skill5);

        popup = new SkillInfoPopup();

        // Initialize arrays
        skillIcons = new ShapeableImageView[]{
                findViewById(R.id.imgSkill1),
                findViewById(R.id.imgSkill2),
                findViewById(R.id.imgSkill3),
                findViewById(R.id.imgSkill4),
                findViewById(R.id.imgSkill5)
        };

        List<SkillModel> skills = getPlayerSkills();

        for (int i = 0; i < skillIcons.length; i++) {
            if (i < skills.size()) {
                SkillModel skill = skills.get(i);

                // Set icon and name
                skillIcons[i].setImageResource(skill.getIconRes());
                skillButtons[i].setText(skill.getName());

                // Show popup when image clicked
                skillIcons[i].setOnClickListener(v -> popup.show(v, skill));

            } else {
                // Hide unused slots
                skillIcons[i].setVisibility(View.INVISIBLE);
                skillButtons[i].setVisibility(View.INVISIBLE);
            }
        }

        combatLog = findViewById(R.id.combat_log);
        btnPause = findViewById(R.id.btnSettings);

        // Setup skill button listeners (guard nulls)
        for (int i = 0; i < skillButtons.length; i++) {
            final int skillIndex = i;
            if (skillButtons[i] != null) {
                skillButtons[i].setOnClickListener(v -> useSkill(skillIndex));
            }
        }

        // No btnBack here per your instruction (pause/exit handled elsewhere)
    }

    /**
     * Initialize AvatarDisplayManager instances for player and enemy.
     * If some IDs don't exist (e.g., typo in XML), we handle gracefully.
     */
    private void initAvatarDisplays() {
        // Player display
        try {
            ImageView pBody = findViewById(R.id.baseBodyLayer);
            ImageView pOutfit = findViewById(R.id.outfitLayer);
            ImageView pWeapon = findViewById(R.id.weaponLayer);
            ImageView pHairOutline = findViewById(R.id.hairOutlineLayer);
            ImageView pHairFill = findViewById(R.id.hairFillLayer);
            ImageView pEyesOutline = findViewById(R.id.eyesOutlineLayer);
            ImageView pEyesFill = findViewById(R.id.eyesFillLayer); // your AvatarDisplayManager expects "eyesIris", we use eyesFill id
            ImageView pNose = findViewById(R.id.noseLayer);
            ImageView pLips = findViewById(R.id.lipsLayer);

            if (pBody != null && pOutfit != null && pWeapon != null) {
                playerDisplay = new AvatarDisplayManager(
                        this,
                        pBody, pOutfit, pWeapon,
                        pHairOutline, pHairFill, pEyesOutline, pEyesFill, pNose, pLips
                );
            } else {
                Log.w(TAG, "Player avatar layers missing in layout - playerDisplay not created.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to init player display: " + e.getMessage(), e);
        }

        // Enemy display - include fallback for possible 'enmyWeaponLayer' typo
        try {
            ImageView eBody = findViewById(R.id.enemyBaseBodyLayer);
            ImageView eOutfit = findViewById(R.id.enemyOutfitLayer);

            // try canonical id first
            ImageView eWeapon = findViewById(R.id.enemyWeaponLayer);
            if (eWeapon == null) {
                // fallback for typo "enmyWeaponLayer" seen in some XML
                eWeapon = findViewById(getResources().getIdentifier("enmyWeaponLayer", "id", getPackageName()));
                if (eWeapon != null) {
                    Log.w(TAG, "Found fallback id 'enmyWeaponLayer' - consider fixing XML to 'enemyWeaponLayer'.");
                }
            }

            ImageView eHairOutline = findViewById(R.id.enemyHairOutlineLayer);
            ImageView eHairFill = findViewById(R.id.enemyHairFillLayer);
            ImageView eEyesOutline = findViewById(R.id.enemyEyesOutlineLayer);
            ImageView eEyesFill = findViewById(R.id.enemyEyesFillLayer);
            ImageView eNose = findViewById(R.id.enemyNoseLayer);
            ImageView eLips = findViewById(R.id.enemyLipsLayer);

            if (eBody != null && eOutfit != null && eWeapon != null) {
                enemyDisplay = new AvatarDisplayManager(
                        this,
                        eBody, eOutfit, eWeapon,
                        eHairOutline, eHairFill, eEyesOutline, eEyesFill, eNose, eLips
                );
            } else {
                Log.w(TAG, "Enemy avatar layers missing in layout - enemyDisplay not created.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to init enemy display: " + e.getMessage(), e);
        }
    }

    private void setupCombatHandler() {
        combatHandler = new Handler(Looper.getMainLooper());
        combatTickRunnable = new Runnable() {
            @Override
            public void run() {
                if (combatActive && combatContext != null) {
                    try {
                        // advance simulation by 0.1s
                        combatContext.tick(0.1);

                        // Inform listener (UI) about the tick — CombatContext currently doesn't call the tick callback,
                        // so we call it here to keep UI synced (this is safe because we're on the main looper).
                        try {
                            onCombatTick(0.1);
                        } catch (Exception e) {
                            Log.w(TAG, "onCombatTick(double) threw: " + e.getMessage());
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Error during combat tick", e);
                    }
                    combatHandler.postDelayed(this, 100);
                }
            }
        };
    }

    private void createEnemyMirrorDisplay(AvatarModel enemyAvatar) {
        if (playerDisplay == null) return;

        // Create ImageView layers for enemy if missing
        ImageView eBody = findViewById(R.id.enemyBaseBodyLayer);
        ImageView eOutfit = findViewById(R.id.enemyOutfitLayer);
        ImageView eWeapon = findViewById(R.id.enemyWeaponLayer);
        ImageView eHairOutline = findViewById(R.id.enemyHairOutlineLayer);
        ImageView eHairFill = findViewById(R.id.enemyHairFillLayer);
        ImageView eEyesOutline = findViewById(R.id.enemyEyesOutlineLayer);
        ImageView eEyesFill = findViewById(R.id.enemyEyesFillLayer);
        ImageView eNose = findViewById(R.id.enemyNoseLayer);
        ImageView eLips = findViewById(R.id.enemyLipsLayer);

        if (eBody == null || eOutfit == null || eWeapon == null) return;

        // Load same layers as player avatar
        enemyDisplay = new AvatarDisplayManager(
                this,
                eBody, eOutfit, eWeapon,
                eHairOutline, eHairFill, eEyesOutline, eEyesFill, eNose, eLips
        );

        // Load a “mirror” of player avatar
        AvatarModel playerAvatar = playerCharacter.getAvatar();
        enemyDisplay.loadAvatar(playerAvatar);

        // Apply tint to all layers to differentiate
        float tintFactor = 0.5f; // 0 = black, 1 = normal
        int tintColor = 0x88FF0000; // semi-transparent red overlay, adjust color

        eBody.setColorFilter(tintColor);
        eOutfit.setColorFilter(tintColor);
        eWeapon.setColorFilter(tintColor);
        if (eHairOutline != null) eHairOutline.setColorFilter(tintColor);
        if (eHairFill != null) eHairFill.setColorFilter(tintColor);
        if (eEyesOutline != null) eEyesOutline.setColorFilter(tintColor);
        if (eEyesFill != null) eEyesFill.setColorFilter(tintColor);
        if (eNose != null) eNose.setColorFilter(tintColor);
        if (eLips != null) eLips.setColorFilter(tintColor);
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

        // Make sure playerDisplay shows the loaded player avatar
        if (playerDisplay != null) {
            playerDisplay.loadAvatar(playerAvatar);
        }
    }

    private void loadAIGhosts() {
        // Load AI ghosts from Firebase RTDB
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Safeguard: if playerCharacter is not yet loaded, create local defaults and return (load will proceed)
        if (playerCharacter == null) {
            createDefaultAIEnemies();
            selectNextEnemy();
            maybeStartCombat(); // start only if both sides ready
            return;
        }

        // For players level 10+, get 3 random avatars from database
        if (playerCharacter.getAvatar().getLevel() >= 10) {
            database.getReference("users")
                    .limitToFirst(50) // Get a sample to choose from
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<AvatarModel> availableAvatars = new ArrayList<>();

                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                try {
                                    AvatarModel avatar = userSnapshot.getValue(AvatarModel.class);
                                    if (avatar != null && !avatar.getPlayerId().equals(playerCharacter.getAvatar().getPlayerId())) {
                                        availableAvatars.add(avatar);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing avatar: " + e.getMessage());
                                }
                            }

                            // Select 3 random avatars
                            Collections.shuffle(availableAvatars);
                            if (!availableAvatars.isEmpty()) {
                                aiGhosts = new ArrayList<>(availableAvatars.subList(0, Math.min(3, availableAvatars.size())));
                            } else {
                                createDefaultAIEnemies();
                            }



                            if (aiGhosts.isEmpty()) {
                                createDefaultAIEnemies();
                            } else {
                                selectNextEnemy();
                                maybeStartCombat();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Failed to load AI ghosts: " + databaseError.getMessage());
                            createDefaultAIEnemies();
                            selectNextEnemy();
                            maybeStartCombat();
                        }
                    });
        } else {
            // For players under level 10, create AI enemies that match their stats
            createMatchingAIEnemies();
            selectNextEnemy();
            maybeStartCombat();
        }
    }

    private void createMatchingAIEnemies() {
        AvatarModel playerAvatar = playerCharacter.getAvatar();

        // Create 3 AI enemies with similar stats to the player
        for (int i = 0; i < 3; i++) {
            AvatarModel aiAvatar = createMatchingAIAvatar(playerAvatar, i + 1);
            aiGhosts.add(aiAvatar);
        }
    }

    private void createDefaultAIEnemies() {
        // Create default AI enemies if no real players found
        for (int i = 0; i < 3; i++) {
            AvatarModel aiAvatar = createDefaultAIAvatar(i + 1);
            aiGhosts.add(aiAvatar);
        }
    }

    private AvatarModel createMatchingAIAvatar(AvatarModel playerAvatar, int index) {
        AvatarModel aiAvatar = new AvatarModel();
        aiAvatar.setUsername("AI Challenger " + index);
        aiAvatar.setLevel(playerAvatar.getLevel() + random.nextInt(3) - 1); // ±1 level
        aiAvatar.setPlayerClass(playerAvatar.getPlayerClass());

        // Match stats with some variation
        aiAvatar.addStrength(playerAvatar.getStrength() + random.nextInt(5) - 2);
        aiAvatar.addEndurance(playerAvatar.getEndurance() + random.nextInt(5) - 2);
        aiAvatar.addAgility(playerAvatar.getAgility() + random.nextInt(5) - 2);
        aiAvatar.addFlexibility(playerAvatar.getFlexibility() + random.nextInt(5) - 2);
        aiAvatar.addStamina(playerAvatar.getStamina() + random.nextInt(5) - 2);

        // Add some basic gear
        addBasicGearToAI(aiAvatar);

        return aiAvatar;
    }

    private AvatarModel createDefaultAIAvatar(int index) {
        AvatarModel aiAvatar = new AvatarModel();
        aiAvatar.setUsername("AI Warrior " + index);
        aiAvatar.setLevel(random.nextInt(5) + 1);
        aiAvatar.setPlayerClass("warrior");

        // Random stats
        aiAvatar.addStrength(random.nextInt(10) + 5);
        aiAvatar.addEndurance(random.nextInt(10) + 5);
        aiAvatar.addAgility(random.nextInt(10) + 5);
        aiAvatar.addFlexibility(random.nextInt(10) + 5);
        aiAvatar.addStamina(random.nextInt(10) + 5);

        addBasicGearToAI(aiAvatar);

        return aiAvatar;
    }

    private void addBasicGearToAI(AvatarModel aiAvatar) {
        // Add some basic gear to make AI more interesting
        List<GearModel> allGear = GearRepository.getAllGear();
        Collections.shuffle(allGear);

        int gearCount = 0;
        for (GearModel gear : allGear) {
            if (gearCount >= 3) break;
            if (gear.getClassRestriction().equals("UNIVERSAL") ||
                    gear.getClassRestriction().equalsIgnoreCase(aiAvatar.getPlayerClass())) {
                aiAvatar.addGear(gear.getId());
                aiAvatar.equipGear(gear.getType(), gear.getId());
                gearCount++;
            }
        }
    }

    private void selectNextEnemy() {
        if (aiGhosts.isEmpty()) return;

        currentEnemyGhost = aiGhosts.get(currentGhostIndex % aiGhosts.size());
        currentGhostIndex++;

        enemyCharacter = new Character(currentEnemyGhost);
        updateEnemyUI();

        if (enemyDisplay != null && currentEnemyGhost != null) {
            if (currentEnemyGhost.hasSprite()) {
                // load enemy’s own sprite
                enemyDisplay.loadAvatar(currentEnemyGhost);
            } else {
                // fallback: mirror of player with tint
                createEnemyMirrorDisplay(currentEnemyGhost);
            }
        }
    }


    /**
     * Start combat only when both playerCharacter and enemyCharacter are available.
     */
    private void maybeStartCombat() {
        if (playerCharacter == null || enemyCharacter == null) return;
        startCombat();
    }

    private void startCombat() {
        if (playerCharacter == null || enemyCharacter == null) return;

        if (combatContext == null) {
            combatContext = new CombatContext(playerCharacter, enemyCharacter, CombatContext.Mode.ARENA, this);
        } else {
            // reset method exists in CombatContext
            combatContext.reset(playerCharacter, enemyCharacter);
        }
        combatContext.startCombat();

        combatActive = true;
        if (combatHandler != null && combatTickRunnable != null) {
            // ensure no duplicate posts
            combatHandler.removeCallbacks(combatTickRunnable);
            combatHandler.post(combatTickRunnable);
        }
    }

    /**
     * Manual skill selection: enable buttons and wait for user input
     */
    @Override
    public SkillModel onRequestPlayerSkillChoice(List<SkillModel> availableSkills, Character player, Character enemy) {
        runOnUiThread(() -> {
            waitingForPlayerInput = true;
            setSkillButtonsEnabled(true);
        });
        return null; // return null so CombatContext waits for onPlayerChosenSkill
    }

    private void setSkillButtonsEnabled(boolean enabled) {
        if (skillButtons != null) {
            for (Button btn : skillButtons) {
                if (btn != null) btn.setEnabled(enabled);
            }
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

                    pauseDialog.dialog.setOnDismissListener(d -> isPaused = false);
                }
                isPaused = true;
                pauseDialog.show();
            });
        }
    }

    private void useSkill(int skillIndex) {
        if (!waitingForPlayerInput) return; // ignore clicks outside your turn

        List<SkillModel> availableSkills = getAvailableSkills(playerCharacter);
        if (skillIndex < availableSkills.size()) {
            SkillModel chosen = availableSkills.get(skillIndex);
            waitingForPlayerInput = false;
            setSkillButtonsEnabled(false);
            if (combatContext != null) {
                combatContext.onPlayerChosenSkill(chosen);
            }
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

        // Guard against invalid values
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

        List<SkillModel> availableSkills = getAvailableSkills(playerCharacter);

        for (int i = 0; i < skillButtons.length; i++) {
            Button button = skillButtons[i];
            if (button == null) continue;

            ShapeableImageView iconView = skillIcons[i];

            if (i < availableSkills.size()) {
                SkillModel skill = availableSkills.get(i);

                // Set skill name and enable button
                button.setText(skill.getName());
                button.setEnabled(true);
                button.setVisibility(View.VISIBLE);

                // --- Dynamic skill icon loading ---
                if (iconView != null) {
                    if (skill.getIconRes() != 0) {
                        iconView.setImageResource(skill.getIconRes());
                        iconView.setVisibility(View.VISIBLE);
                    } else {
                        iconView.setImageResource(R.drawable.lock_2); // fallback icon
                        iconView.setVisibility(View.VISIBLE);
                    }
                }

            } else {
                button.setEnabled(false);
                button.setVisibility(View.GONE);

                if (iconView != null) {
                    iconView.setVisibility(View.GONE);
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

    // CombatContext.CombatListener implementation
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
        // Called every tick (e.g., 100ms). Keep UI in sync.
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

    // --- Required no-arg tick (CombatListener declares both). ---
    @Override
    public void onCombatTick() {
        // Keep behavior identical to the double-arg version.
        onCombatTick(0.0);
    }

    @Override
    public void onTurnStart(Character c, CombatContext ctx) {
        // Called each tick, can be used for status effect processing
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
                playHitAnimation(findViewById(R.id.baseBodyLayer));

                if (!playerCharacter.isAlive()) {
                    playPlayerDeathAnimation(findViewById(R.id.baseBodyLayer));
                }

            } else if (defender == enemyCharacter) {
                updateHealthBar(enemyHpBar, enemyHpText, enemyCharacter);
                playHitAnimation(findViewById(R.id.enemyBaseBodyLayer));

                if (!enemyCharacter.isAlive()) {
                    playDeathAnimation(findViewById(R.id.enemyBaseBodyLayer));
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

    @Override
    public void onCombatEnded(CombatContext.Result result, Character winner, Character loser) {
        runOnUiThread(() -> {
            combatActive = false;
            if (combatHandler != null) {
                combatHandler.removeCallbacks(combatTickRunnable);
            }

            String resultMessage;
            boolean playerWon = (result == CombatContext.Result.PLAYER_WON);

            AvatarModel playerAvatar = playerCharacter.getAvatar();

            if (playerWon) {
                resultMessage = "Victory! You defeated " + loser.getName();
                boolean leveledUp = playerAvatar.addXp(50);
                playerAvatar.addCoins(100);

                int rankPointsEarned = 10 + (playerAvatar.getLevel() / 2);
                playerAvatar.addRankPoints(rankPointsEarned);

                BattleHistoryModel battleEntry = new BattleHistoryModel(
                        playerAvatar.getUsername(),
                        playerAvatar.getLevel(),
                        playerAvatar.getRankDrawableRes(),
                        loser.getName(),
                        loser.getLevel(),
                        R.drawable.rank_novice,
                        rankPointsEarned
                );
                playerAvatar.addBattleHistory(battleEntry);

                LeaderboardManager.updateRankLeaderboard(this, playerAvatar);

                ProgressSyncManager.saveProgress(this, playerAvatar, false);
                if (isNetworkAvailable()) {
                    ProgressSyncManager.saveProgress(this, playerAvatar, true);
                }

                if (leveledUp) {
                    QuestRewardManager.showLevelUpPopup(this, playerAvatar.getLevel(), playerAvatar.getRank());
                }

                // Next enemy
                if (currentGhostIndex < aiGhosts.size()) {
                    selectNextEnemy();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> startCombat(), 3000);
                } else {
                    addCombatLog("All enemies defeated! Arena complete!");
                }
            } else {
                resultMessage = "Defeat! You were defeated by " + winner.getName();
                boolean leveledUp = playerAvatar.addXp(10);

                int rankPointsLost = Math.min(5, playerAvatar.getRankPoints() / 10);
                if (rankPointsLost > 0) {
                    playerAvatar.setRankPoints(playerAvatar.getRankPoints() - rankPointsLost);
                }

                BattleHistoryModel battleEntry = new BattleHistoryModel(
                        playerAvatar.getUsername(),
                        playerAvatar.getLevel(),
                        playerAvatar.getRankDrawableRes(),
                        winner.getName(),
                        winner.getLevel(),
                        R.drawable.rank_novice,
                        -rankPointsLost
                );
                playerAvatar.addBattleHistory(battleEntry);

                ProgressSyncManager.saveProgress(this, playerAvatar, false);
                if (isNetworkAvailable()) {
                    ProgressSyncManager.saveProgress(this, playerAvatar, true);
                }

                if (leveledUp) {
                    QuestRewardManager.showLevelUpPopup(this, playerAvatar.getLevel(), playerAvatar.getRank());
                }
            }

            addCombatLog(resultMessage);
            Toast.makeText(this, resultMessage, Toast.LENGTH_LONG).show();
        });
    }

    // --- Optional hook called when CombatContext.endCombat(playerWon) is used ---
    @Override
    public void onCombatEnd(boolean playerWon) {
        runOnUiThread(() -> {
            // Stop ticking and log
            combatActive = false;
            if (combatHandler != null) combatHandler.removeCallbacks(combatTickRunnable);
            addCombatLog("Combat forcibly ended: " + (playerWon ? "PLAYER_WON" : "PLAYER_LOST"));
        });
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

    private void playHitAnimation(View view) {
        if (view == null) return;

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
            view.setAlpha(1f - (0.3f * value)); // briefly fade to simulate flash
            if (view instanceof ImageView) {
                ((ImageView) view).setColorFilter(
                        android.graphics.Color.argb((int) (150 * value), 255, 0, 0),
                        android.graphics.PorterDuff.Mode.SRC_ATOP
                );
            }
        });

        flash.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setAlpha(1f);
                if (view instanceof ImageView) {
                    ((ImageView) view).clearColorFilter();
                }
            }
        });

        // --- Combine both animations ---
        AnimatorSet set = new AnimatorSet();
        set.playTogether(shake, flash);
        set.start();
    }

    private void playDeathAnimation(View view) {
        if (view == null) return;

        // Fade out + fall down
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setDuration(800);

        ObjectAnimator fallDown = ObjectAnimator.ofFloat(view, "translationY", 0f, 200f);
        fallDown.setDuration(800);
        fallDown.setInterpolator(new android.view.animation.AccelerateInterpolator());

        ObjectAnimator shrink = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.6f);
        shrink.setDuration(800);

        ObjectAnimator shrinkY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.6f);
        shrinkY.setDuration(800);

        AnimatorSet deathSet = new AnimatorSet();
        deathSet.playTogether(fadeOut, fallDown, shrink, shrinkY);
        deathSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
            }
        });
        deathSet.start();
    }

    private void playPlayerDeathAnimation(View view) {
        if (view == null) return;

        // Step 1: Flash faintly before collapse
        ValueAnimator flash = ValueAnimator.ofFloat(0f, 1f);
        flash.setDuration(200);
        flash.setRepeatMode(ValueAnimator.REVERSE);
        flash.setRepeatCount(2);
        flash.addUpdateListener(animator -> {
            float value = (float) animator.getAnimatedValue();
            view.setAlpha(1f - (0.4f * value)); // subtle flicker
        });

        // Step 2: Slight backward fall
        ObjectAnimator fallBack = ObjectAnimator.ofFloat(view, "translationY", 0f, 100f);
        fallBack.setInterpolator(new android.view.animation.AccelerateInterpolator());
        fallBack.setDuration(1000);

        // Step 3: Gradual fade-out
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setDuration(1000);

        // Step 4: Scale down slightly as if collapsing
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f);
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);

        // Combine everything
        AnimatorSet deathSet = new AnimatorSet();
        deathSet.playSequentially(flash, fadeOut);
        deathSet.playTogether(fallBack, scaleX, scaleY, fadeOut);
        deathSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                // Optionally trigger game over UI or message
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
        try { popup.dismiss(); } catch (Exception ignored) {}
        combatActive = false;
        if (combatHandler != null) {
            combatHandler.removeCallbacks(combatTickRunnable);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        MusicManager.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MusicManager.pause();
    }
}
