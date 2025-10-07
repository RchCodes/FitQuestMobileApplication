package com.example.fitquest;

import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * CombatContext - orchestrates a 1v1 turn-based battle using an Action Bar (0..100).
 * Supports two modes:
 *  - ARENA (ghost PvP) : simulate another player's avatar offline (AI-controlled)
 *  - CHALLENGE (gauntlet) : sequential 1v1 fights vs AI opponents
 *
 * Notes:
 *  - UI should call tick(elapsedSeconds) regularly (e.g. from a game loop or handler).
 *  - When a player AB reaches 100, UI can call requestPlayerAction() to ask for a skill choice,
 *    and then call playerUseSkill(skillId). For offline play the context can auto-decide.
 */
public class CombatContext {

    public enum Mode { ARENA, CHALLENGE }
    public enum Result { ONGOING, PLAYER_WON, ENEMY_WON, DRAW }

    public interface CombatListener {
        // UI hooks
        void onCombatStarted(Character player, Character enemy, Mode mode);
        void onActionBarUpdated(Character c);

        void onCombatTick(double deltaTime);

        void onTurnStart(Character c, CombatContext ctx);
        void onSkillUsed(Character user, SkillModel skill, Character target, String logEntry);
        void onDamageApplied(Character attacker, Character defender, int amount, String logEntry);
        void onStatusApplied(Character target, StatusEffect effect, String logEntry);
        void onLog(String entry);
        void onCombatEnded(Result result, Character winner, Character loser);
        /**
         * When called and returns null, CombatContext will auto-select a skill for the player.
         * If implementing UI, return the SkillModel object the player chose (or null to auto).
         */
        @Nullable SkillModel onRequestPlayerSkillChoice(List<SkillModel> availableSkills, Character player, Character enemy);

        // --- CombatListener ---
        void onCombatTick();

        void onCombatEnd(boolean playerWon);
    }

    private final Character player;
    private final Character enemy;
    private final Mode mode;
    final CombatListener listener;
    private final Deque<String> log = new ArrayDeque<>();
    private final Random rng = new Random();

    // AB fill config (per second)
    private static final double BASE_AB_FILL_PER_SEC = 20.0; // default baseline: 20 AB per second
    // AB is 0..100. Skill AB costs are integers (e.g. 50 for buff, 100 for normal attack)

    // Internal state
    private long turnCounter = 0;
    private Result result = Result.ONGOING;
    private boolean awaitingPlayerChoice = false; // waiting for UI to pick
    private final List<Character> readyQueue = new ArrayList<>(); // order of who can act now
    private final List<Character> allCombatants = new ArrayList<>(2);

    public CombatContext(Character player, Character enemy, Mode mode, CombatListener listener) {
        this.player = player;
        this.enemy = enemy;
        this.mode = mode;
        this.listener = listener;

        allCombatants.add(player);
        allCombatants.add(enemy);
    }

    // --- Public API ----------------------------------------------------------------

    /** Start the combat and notify listener. */
    public void startCombat() {
        // Reset some combat state
        turnCounter = 0;
        result = Result.ONGOING;
        awaitingPlayerChoice = false;
        readyQueue.clear();
        log.clear();

        // Recalculate characters before starting (gear applied, etc)
        player.recalcStatsFromAvatar();
        enemy.recalcStatsFromAvatar();

        pushLog(String.format(Locale.US, "Combat started: %s vs %s", player.getAvatar().getUsername(), enemy.getAvatar().getUsername()));
        if (listener != null) listener.onCombatStarted(player, enemy, mode);
    }

    /**
     * Advance simulation by elapsedSeconds seconds.
     * Call this repeatedly (e.g. every 100-500ms) from UI/game loop.
     */
    public void tick(double elapsedSeconds) {
        if (result != Result.ONGOING) return;



        // Fill AB for each combatant
        for (Character c : allCombatants) {
            double fill = computeAbFillPerSecond(c) * elapsedSeconds;
            int intFill = Math.max(0, (int)Math.round(fill));
            if (intFill > 0) {
                c.increaseActionBar(intFill);
                if (listener != null) listener.onActionBarUpdated(c);
            }
        }

        // Build ready queue (who has AB full)
        readyQueue.clear();
        for (Character c : allCombatants) {
            if (c.isActionBarFull() && c.isAlive()) {
                readyQueue.add(c);
            }
        }

        // If both ready, order by AGI tiebreaker
        if (readyQueue.size() == 2) {
            Character first = readyQueue.get(0);
            Character second = readyQueue.get(1);
            int aAgi = first.getEffectiveAgility();
            int bAgi = second.getEffectiveAgility();
            if (bAgi > aAgi) {
                // swap so higher AGI acts first
                Character tmp = first;
                readyQueue.set(0, second);
                readyQueue.set(1, tmp);
            } else if (bAgi == aAgi) {
                // tie-break by random but deterministic-ish
                if (rng.nextBoolean()) {
                    Character tmp = first;
                    readyQueue.set(0, second);
                    readyQueue.set(1, tmp);
                }
            }
        }

        // Process actions for everyone ready (keep looping while actions available)
        for (Character actor : new ArrayList<>(readyQueue)) {
            if (!actor.isAlive()) continue;
            Character target = actor == player ? enemy : player;

            // If actor is player and we're in interactive mode, request UI choice
            if (actor == player && listener != null && mode != Mode.ARENA) {
                // Ask listener for a skill choice (UI can respond synchronously)
                List<SkillModel> available = getAvailableSkills(actor);
                SkillModel chosen = listener.onRequestPlayerSkillChoice(available, actor, target);
                if (chosen != null) {
                    playerUseSkill(chosen.getId());
                } else {
                    // Auto choose if null
                    autoUseBestSkill(actor, target);
                }
            } else {
                // AI-controlled actor (enemy in all cases; or player when AI/autoplay)
                autoUseBestSkill(actor, target);
            }

            // After each action check for end of combat
            checkCombatEnd();
            if (result != Result.ONGOING) return;
        }

        // On each tick, call onTurnStart hooks (passives + status effects)
        // We'll call them once per tick for both
        turnCounter++;
        for (Character c : allCombatants) {
            if (c.isAlive()) {
                c.onTurnStart(this);
                if (listener != null) listener.onTurnStart(c, this);
                if (c.isAlive()) {
                    c.processStatusEffects(this); // ensures durations tick down
                }
            }
        }

        if (readyQueue.isEmpty()) {
            // Optional: slowly drain AB or trigger "skip" mechanic
            for (Character c : allCombatants) {
                c.increaseActionBar(-1); // small decay to re-enter loop
            }
        }
    }

    private void checkCombatEnd() {
        if (!player.isAlive() && !enemy.isAlive()) {
            finalizeCombat(player, enemy);
        } else if (!player.isAlive()) {
            finalizeCombat(enemy, player);
        } else if (!enemy.isAlive()) {
            finalizeCombat(player, enemy);
        }
    }

    /** Let UI explicitly use a skill for player (by SkillModel id). Returns true if used. */
    public boolean playerUseSkill(String skillId) {
        SkillModel skill = findSkillById(player, skillId);
        if (skill == null) return false;
        if (!player.canUseSkill(skill)) {
            pushLog("Skill not ready or insufficient AB: " + skill.getName());
            return false;
        }
        useSkill(player, skill, enemy);
        return true;
    }

    /** Force the player to auto-act (for autoplay or ghost) */
    public void playerAutoAct() {
        if (player.isActionBarFull()) {
            autoUseBestSkill(player, enemy);
            checkCombatEnd();
        }
    }

    // --- Core combat orchestration helpers -----------------------------------------

    /** Compute per-second AB fill for a character based on STA (stamina) */
    private double computeAbFillPerSecond(Character c) {
        // Stamina mapping: +2% AB speed per 5 STA points -> multiplier = 1 + (STA / 5)*0.02
        int sta = c.getEffectiveStamina();
        double multiplier = 1.0 + ( (double)sta / 5.0 ) * 0.02;
        return BASE_AB_FILL_PER_SEC * multiplier;
    }

    /** Return available (unlocked, off-cooldown) active skills for a character. */
    private List<SkillModel> getAvailableSkills(Character c) {
        List<SkillModel> res = new ArrayList<>();
        for (SkillModel s : c.getActiveSkills()) {
            if (s.getLevelUnlock() <= c.getAvatar().getLevel() && !s.isOnCooldown() && s.getAbCost() <= c.getActionBar()) {
                res.add(s);
            }
        }
        return res;
    }

    /** Auto-choose a skill for AI: prefer highest damage potential, fallback to buff/utility when appropriate. */
    private void autoUseBestSkill(Character actor, Character target) {
        List<SkillModel> choices = getAvailableSkills(actor);
        if (choices.isEmpty()) {
            // If no skills available, fill AB partially (allow to keep AB at 100 to wait)
            pushLog(actorName(actor) + " has no available skills.");
            return;
        }

        // Simple priority: if enemy HP low and an execution exists -> use execution; else highest estimated damage
        SkillModel best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (SkillModel s : choices) {
            double score = estimateSkillValue(actor, target, s);
            if (score > bestScore) {
                bestScore = score;
                best = s;
            }
        }

        if (best != null) {
            useSkill(actor, best, target);
        }
    }

    /**
     * Estimate skill value for AI (very simple heuristics).
     * Prioritize damage, execution, and useful buffs.
     */
    private double estimateSkillValue(Character actor, Character target, SkillModel skill) {
        double score = 0.0;
        // Damage estimate via STR scaling
        double dmgEstimate = (skill.getStrScaling() * actor.getEffectiveStrength()) + (skill.getEndScaling() * actor.getEffectiveEndurance())
                + (skill.getAgiScaling() * actor.getEffectiveAgility());
        score += dmgEstimate * 1.0;

        // Prefer execution if target low and skill name suggests execution
        if (skill.getName().toLowerCase().contains("execute") || skill.getName().toLowerCase().contains("finale")) {
            if (target.getCurrentHp() < target.getMaxHp() * 0.3) score += 200;
        }

        // Buffs are useful if actor HP low or lacking buffs
        if (skill.getType() == SkillType.BUFF) {
            if (actor.getCurrentHp() < actor.getMaxHp() * 0.5) score += 50;
        }

        // Slight randomness to vary behavior
        score += rng.nextDouble() * 5.0;
        return score;
    }

    /**
     * Central method to use/apply a skill.
     * This will:
     *  - Deduct AB
     *  - Reset skill cooldown
     *  - Apply skill's effects (damage, status effects) through applyDamage / applyStatusEffect
     *  - Log actions and call listener hooks
     */
    public void useSkill(Character user, SkillModel skill, Character target) {
        if (skill == null || user == null || target == null) return;
        if (!user.canUseSkill(skill)) {
            pushLog(userName(user) + " tried to use " + skill.getName() + " but couldn't.");
            return;
        }

        // Deduct AB & set cooldown
        int prevAb = user.getActionBar();
        int cost = skill.getAbCost();
        // Cap cost to AB
        if (cost > prevAb) cost = prevAb;
        // Reduce AB by cost (SkillModel.resetCooldown later)
        user.increaseActionBar(-cost); // use negative amount to subtract (helper method must allow; if not adjust)
        // Reset cooldown on skill instance
        skill.resetCooldown();

        // Log & notify
        String usageLog = String.format(Locale.US, "%s uses %s on %s", userName(user), skill.getName(), userName(target));
        pushLog(usageLog);
        if (listener != null) listener.onSkillUsed(user, skill, target, usageLog);

        // Execute skill logic (skill.execute should call CombatContext helpers for damage/status application)
        skill.execute(user, target, this);

        // After execution check for immediate kills & call onKill hooks
        if (!target.isAlive()) {
            // user killed target
            pushLog(userName(user) + " has defeated " + userName(target) + "!");
            user.onKill(target, this);
            // if in ARENA mode, we might want to record result or push ghost data
            finalizeCombat(user, target);
        }
    }

    /** Apply damage via central path so shields/counters/passives/gear can intervene. */
    public void applyDamage(Character attacker, Character defender, int rawDamage, @Nullable SkillModel sourceSkill) {
        if (rawDamage <= 0) return;

        int damage = rawDamage;

        // 0) Outgoing damage modifiers (attacker passives or effects)
        if (attacker != null) {
            for (StatusEffect e : new ArrayList<>(attacker.getStatusEffects())) {
                if (e instanceof OnOutgoingDamageHook) {
                    OnOutgoingDamageHook hook = (OnOutgoingDamageHook) e;
                    rawDamage = hook.onOutgoingDamage(attacker, defender, rawDamage, this, sourceSkill);
                }
            }
        }

        // 1) Let defender status effects potentially absorb or modify damage
        for (StatusEffect e : new ArrayList<>(defender.getStatusEffects())) {
            // Shield handling: if effect has absorbDamage method, call it
            if (e instanceof ShieldEffect) {
                ShieldEffect sh = (ShieldEffect) e;
                int remaining = sh.absorbDamage(damage);
                // shield absorbed part: compute actually absorbed = damage - remaining
                int absorbed = damage - remaining;
                damage = remaining;
                pushLog(String.format(Locale.US, "%s's shield absorbs %d damage.", userName(defender), absorbed));
                if (listener != null) {
                    listener.onLog(String.format(Locale.US, "%s's shield absorbs %d damage.", userName(defender), absorbed));
                }
                // If shield consumed fully, remove it
                if (sh.isExpired() || sh.absorbDamage(0) == 0) {
                    defender.getStatusEffects().remove(sh);
                }
                if (damage <= 0) {
                    // all absorbed
                    return;
                }
            }
            // Optional hook: OnIncomingDamageHook implementations can alter damage
            if (e instanceof OnIncomingDamageHook) {
                OnIncomingDamageHook hook = (OnIncomingDamageHook) e;
                damage = hook.onIncomingDamage(defender, attacker, damage, this, sourceSkill);
            }
        }

        // 2) Passive hooks that respond to damage about to be taken
    defender.onDamageTaken(damage, this);

        // 3) Apply damage to HP (after all reductions)
        defender.takeDamage(damage);
        if (listener != null) {
            String s = String.format(Locale.US, "%s deals %d to %s", userName(attacker), damage, userName(defender));
            listener.onDamageApplied(attacker, defender, damage, s);
        }
        pushLog(String.format(Locale.US, "%s deals %d to %s", userName(attacker), damage, userName(defender)));

        // 4) If defender has counter-type effects, trigger counter damage back (hook onAttacked)
        for (StatusEffect e : new ArrayList<>(defender.getStatusEffects())) {
            if (e instanceof OnAttackedHook) {
                OnAttackedHook hook = (OnAttackedHook) e;
                int counterDamage = hook.onAttacked(defender, attacker, damage, this, sourceSkill);
                if (counterDamage > 0) {
                    pushLog(String.format(Locale.US, "%s counterattacks dealing %d to %s", userName(defender), counterDamage, userName(attacker)));
                    applyDamage(defender, attacker, counterDamage, null);
                }
            }
        }

        // 5) If defender died, call onKill and finalize
        if (!defender.isAlive()) {
            attacker.onKill(defender, this);
            finalizeCombat(attacker, defender);
        }
    }

    /**
     * Apply a status effect to a character (skill or gear or passive calls this).
     * The effect should be a NEW instance (not shared).
     */
    public void applyStatusEffect(Character who, StatusEffect effect) {
        if (who == null || effect == null) return;
        who.addStatusEffect(effect);
        int turns = effect.getRemainingTurns();
        if (turns < 0) turns = -1;
        pushLog(String.format(Locale.US, "%s is afflicted with %s for %d turns", userName(who), effect.getName(), turns));
        if (listener != null) listener.onStatusApplied(who, effect, effect.getName());
    }

    // Helper to get effect duration if available
    private int getEffectDuration(StatusEffect e) {
        // Prefer explicit API on StatusEffect; fallback to -1 when not available
        try {
            return e.getRemainingTurns();
        } catch (Exception ex) {
            return -1;
        }
    }

    // --- Utility / small helpers --------------------------------------------------

    private String userName(Character c) {
        String n = c.getAvatar().getUsername();
        return (n == null || n.isEmpty()) ? (c == player ? "Player" : "Enemy") : n;
    }

    private String actorName(Character c) {
        return userName(c);
    }

    private SkillModel findSkillById(Character c, String id) {
        for (SkillModel s : c.getActiveSkills()) {
            if (s.getId().equals(id)) return s;
        }
        return null;
    }

    /** Push message to log and notify listener. */
    void pushLog(String entry) {
        log.addFirst(String.format(Locale.US, "[%d] %s", turnCounter, entry));
        if (listener != null) listener.onLog(entry);
    }

    /** Finalize combat when someone dies and notify listener with results. */
    private void finalizeCombat(Character winner, Character loser) {
        if (!winner.isAlive() && !loser.isAlive()) {
            result = Result.DRAW;
        } else if (winner.isAlive()) {
            result = (winner == player) ? Result.PLAYER_WON : Result.ENEMY_WON;
        } else {
            result = (loser == player) ? Result.ENEMY_WON : Result.PLAYER_WON;
        }
        pushLog("Combat ended: " + result);
        if (listener != null) listener.onCombatEnded(result, winner, loser);
    }

    /** Returns the combat log as a list (most recent first). */
    public List<String> getLog() {
        return new ArrayList<>(log);
    }

    public Result getResult() {
        return result;
    }

    // --- Interfaces that StatusEffect implementations may optionally provide ---
    // These are checked via instanceof so existing effects don't need to implement them
    public interface OnIncomingDamageHook {
        /**
         * Called before raw damage is applied to defender. Return modified damage.
         * defender - the character taking damage
         * attacker - attacker
         * damage - proposed damage
         * ctx - combat context
         * skill - skill source if any
         */
        int onIncomingDamage(Character defender, Character attacker, int damage, CombatContext ctx, @Nullable SkillModel skill);
    }

    public interface OnOutgoingDamageHook {
        /**
         * Called when attacker is about to deal damage; can modify outgoing damage.
         * Return modified damage.
         */
        int onOutgoingDamage(Character attacker, Character defender, int damage, CombatContext ctx, @Nullable SkillModel skill);
    }

    public interface OnAttackedHook {
        /**
         * Called after defender was hit. Return counter damage amount (0 if none).
         */
        int onAttacked(Character defender, Character attacker, int damageTaken, CombatContext ctx, @Nullable SkillModel skill);
    }

    public void endCombat(boolean playerWon) {
        // Stop further ticking/actions
        result = playerWon ? Result.PLAYER_WON : Result.ENEMY_WON;

        // Push log
        pushLog("Combat forcibly ended: " + result);

        // Notify listener
        if (listener != null) {
            // Determine winner and loser objects
            Character winner = playerWon ? player : enemy;
            Character loser = playerWon ? enemy : player;

            listener.onCombatEnded(result, winner, loser);
            listener.onCombatEnd(playerWon); // optional hook
        }

        // Optionally reset or clear any queues
        readyQueue.clear();
        awaitingPlayerChoice = false;
    }

    public void reset(Character newPlayer, Character newEnemy) {
        allCombatants.clear();
        readyQueue.clear();
        log.clear();

        allCombatants.add(newPlayer);
        allCombatants.add(newEnemy);

        // Reset internal state
        turnCounter = 0;
        result = Result.ONGOING;
        awaitingPlayerChoice = false;

        // Update references
        newPlayer.recalcStatsFromAvatar();
        newEnemy.recalcStatsFromAvatar();

        pushLog(String.format(Locale.US, "Combat reset: %s vs %s",
                newPlayer.getAvatar().getUsername(),
                newEnemy.getAvatar().getUsername()));
    }


}
