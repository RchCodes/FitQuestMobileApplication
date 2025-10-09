package com.example.fitquest;

import android.content.Context;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import static java.lang.Math.atan2;

public class ExerciseDetector {
    public interface ExerciseListener {
        void onRepCountChanged(int currentReps, int target);
        void onPlankTimeUpdated(long seconds, long requiredSeconds);
        void onFeedbackUpdated(String feedback);
        void onExerciseCompleted(String summaryMessage);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ExerciseListener listener;
    private Context context;

    private String exerciseType = "squats";
    private String difficultyLevel = "beginner";
    private int target = 10;

    // states
    private int currentReps = 0;
    private boolean completed = false;
    private boolean downState = false;
    private long lastRepTime = 0;

    // cooldown & thresholds
    private static final long DEFAULT_COOLDOWN_MS = 800;
    private long cooldownMs = DEFAULT_COOLDOWN_MS;

    // plank timing
    private boolean isPlanking = false;
    private long lastPlankTimestamp = 0L; // last timestamp when plank timing was updated
    private long plankAccumMs = 0L;
    private long plankGraceStart = 0L;
    private long lastReportedPlankSecond = -1L;

    // distance smoothing & calibration
    private float smoothedDistance = -1f;
    private static final float DISTANCE_SMOOTHING_ALPHA = 0.18f;
    private static final float MIN_DISTANCE_M = 0.6f;
    private static final float MAX_DISTANCE_M = 5.0f;

    // expected px constants (tweak with real calibration)
    private static final float MIN_SHOULDER_PX = 40f;      // ignore if too small
    private static final float SHOULDER_1_5M_PX = 90f;
    private static final float SHOULDER_2_5M_PX = 50f;     // note: mapping expects SHOULDER_1_5M_PX > SHOULDER_2_5M_PX

    private static final float TORSO_1_5M_PX = 180f;
    private static final float TORSO_2_5M_PX = 90f;

    private static final int PLANK_GRACE_MS = 1500; // grace period for short tracking loss (1.5s)
    private static final float ANGLE_TOLERANCE = 15f; // ±15° grace for noisy landmarks

    private float smoothBodyAngle = 0f;
    private float smoothArmAngle = 0f;
    private float smoothKneeAngle = 0f;
    private static final float SMOOTHING_ALPHA = 0.25f; // moving average smoothing factor

    private String feedback = "Ready";
    private boolean distanceReady = false;

    public ExerciseDetector(Context ctx, String difficulty, int targetReps) {
        this.context = ctx;
        this.difficultyLevel = difficulty == null ? "beginner" : difficulty;
        this.target = Math.max(1, targetReps);
        applyDifficultySettings();
    }

    public void setListener(ExerciseListener l) { this.listener = l; }

    public void updateExercise(String exercise, String difficulty, int targetReps) {
        this.exerciseType = exercise;
        this.difficultyLevel = difficulty;
        this.target = Math.max(1, targetReps);
        resetExercise();
        applyDifficultySettings();
    }

    private void applyDifficultySettings() {
        // Example: difficulty could tune cooldown and sensitivity; tweak as needed
        switch (difficultyLevel) {
            case "beginner":
                cooldownMs = 700;
                break;
            case "intermediate":
                cooldownMs = 450;
                break;
            case "advanced":
                cooldownMs = 300;
                break;
            default:
                cooldownMs = DEFAULT_COOLDOWN_MS;
        }
    }

    public void resetExercise() {
        currentReps = 0;
        completed = false;
        downState = false;
        lastRepTime = 0;
        isPlanking = false;
        lastPlankTimestamp = 0;
        plankAccumMs = 0;
        plankGraceStart = 0;
        lastReportedPlankSecond = -1;
        distanceReady = false;
        smoothedDistance = -1f;
        feedback = "Step back until 1.5–2.5m from camera";
        if (listener != null) {
            listener.onRepCountChanged(currentReps, target);
            listener.onFeedbackUpdated(feedback);
        }
    }

    public void processPose(Pose pose, String exercise) {
        if (pose == null || completed) return;

        // Distance check until user places themself in range
        if (!distanceReady) {
            float d = estimateDistanceMeters(pose);
            if (d < 0f) {
                feedback = "Move into camera frame";
                if (listener != null) listener.onFeedbackUpdated(feedback);
                return;
            }
            if (d < 1.5f || d > 2.5f) {
                feedback = "Move between 1.5m–2.5m from camera";
                if (listener != null) listener.onFeedbackUpdated(feedback);
                return;
            } else {
                distanceReady = true;
                feedback = "Good! Now get into starting position";
                if (listener != null) listener.onFeedbackUpdated(feedback);
                // continue into detection this frame
            }
        }

        // choose exercise
        switch (exercise != null ? exercise : this.exerciseType) {
            case "pushups": detectPushup(pose); break;
            case "squats": detectSquat(pose); break;
            case "plank": detectPlank(pose); break;
            case "crunches": detectCrunch(pose); break;
            case "lunges": detectLunge(pose); break;
            default: detectSquat(pose); break;
        }
    }

    private void registerRepIfCooldown() {
        long now = System.currentTimeMillis();
        if (now - lastRepTime < cooldownMs) return;
        lastRepTime = now;
        currentReps++;
        if (listener != null) listener.onRepCountChanged(currentReps, target);
        if (currentReps >= target && !completed) {
            completed = true;
            if (listener != null) listener.onExerciseCompleted(currentReps + " reps done.");
        }
    }

    // ---- Detection helpers that try both sides where appropriate ----

    private void detectSquat(Pose pose) {
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        if (leftHip == null || rightHip == null ||
                leftKnee == null || rightKnee == null ||
                leftAnkle == null || rightAnkle == null) return;

        // Calculate angles for both legs
        double leftAngle = getAngle(leftHip, leftKnee, leftAnkle);
        double rightAngle = getAngle(rightHip, rightKnee, rightAnkle);

        // Add small jitter grace range
        double downThreshold = 140; // deeper squat
        double upThreshold = 160;   // almost straight
        double grace = 5;

        // Check if both knees are bent sufficiently
        boolean bothDown = (leftAngle < downThreshold + grace) && (rightAngle < downThreshold + grace);

        // Check if both feet are level (prevent false squat when one leg lifted)
        float leftAnkleY = leftAnkle.getPosition().y;
        float rightAnkleY = rightAnkle.getPosition().y;
        boolean feetLevel = Math.abs(leftAnkleY - rightAnkleY) < 50; // adjust 30–60 depending on camera distance

        // Standing validation — both hips should be higher than knees when standing
        boolean standing = leftHip.getPosition().y < leftKnee.getPosition().y - 50 &&
                rightHip.getPosition().y < rightKnee.getPosition().y - 50;

        if (bothDown && feetLevel && !downState) {
            downState = true;
            feedback = "Down";
            if (listener != null) listener.onFeedbackUpdated(feedback);
        } else if (standing && downState) {
            downState = false;
            registerRepIfCooldown();
            feedback = "Nice squat!";
            if (listener != null) listener.onFeedbackUpdated(feedback);
        }
    }


    private void detectPushup(Pose pose) {
        PoseLandmark shoulder = firstAvailable(pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark elbow = firstAvailable(pose, PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW);
        PoseLandmark wrist = firstAvailable(pose, PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST);
        if (shoulder == null || elbow == null || wrist == null) return;

        // 🧍 Skip if user is standing or not horizontal
        if (isStanding(pose)) {
            feedback = "Go down to pushup position!";
            if (listener != null) listener.onFeedbackUpdated(feedback);
            return;
        }

        double angle = getAngle(shoulder, elbow, wrist);
        double downThreshold = 100;
        double upThreshold = 140;

        if (angle < downThreshold && !downState) {
            downState = true;
            feedback = "Down";
            if (listener != null) listener.onFeedbackUpdated(feedback);
        } else if (angle > upThreshold && downState) {
            downState = false;
            registerRepIfCooldown();
            feedback = "Great pushup!";
            if (listener != null) listener.onFeedbackUpdated(feedback);
        }
    }


    private void detectCrunch(Pose pose) {
        PoseLandmark shoulder = firstAvailable(pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark hip = firstAvailable(pose, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP);
        PoseLandmark knee = firstAvailable(pose, PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE);
        if (shoulder == null || hip == null || knee == null) return;

        // 🧍 Skip if user is standing (should be lying down)
        if (isStanding(pose)) {
            feedback = "Lie down to start crunches!";
            if (listener != null) listener.onFeedbackUpdated(feedback);
            return;
        }

        double angle = getAngle(shoulder, hip, knee);
        double down = 120;
        double up = 150;

        if (angle < down && !downState) {
            downState = true;
            feedback = "Up";
            if (listener != null) listener.onFeedbackUpdated(feedback);
        } else if (angle > up && downState) {
            downState = false;
            registerRepIfCooldown();
            feedback = "Crunch!";
            if (listener != null) listener.onFeedbackUpdated(feedback);
        }
    }

    private void detectLunge(Pose pose) {
        PoseLandmark hip = firstAvailable(pose, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP);
        PoseLandmark knee = firstAvailable(pose, PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE);
        PoseLandmark ankle = firstAvailable(pose, PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE);
        if (hip == null || knee == null || ankle == null) return;

        double angle = getAngle(hip, knee, ankle);
        double down = 140;
        double up = 150;
        if (angle < down && !downState) {
            downState = true;
            feedback = "Down";
            if (listener != null) listener.onFeedbackUpdated(feedback);
        } else if (angle > up && downState) {
            downState = false;
            registerRepIfCooldown();
            feedback = "Nice lunge!";
            if (listener != null) listener.onFeedbackUpdated(feedback);
        }
    }

    private void detectPlank(Pose pose) {
        PoseLandmark shoulder = firstAvailable(pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark elbow = firstAvailable(pose, PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW);
        PoseLandmark wrist = firstAvailable(pose, PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST);
        PoseLandmark hip = firstAvailable(pose, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP);
        PoseLandmark knee = firstAvailable(pose, PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE);
        PoseLandmark ankle = firstAvailable(pose, PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE);

        if (shoulder == null || elbow == null || wrist == null || hip == null || knee == null || ankle == null)
            return;

        // --- Standing detection using two points (shoulder & hip) ---
        float dx = Math.abs(shoulder.getPosition().x - hip.getPosition().x);
        float dy = Math.abs(shoulder.getPosition().y - hip.getPosition().y);
        boolean isStanding = dy > dx * 1.5f; // vertical alignment threshold

        // --- Raw Angles ---
        double bodyAngle = getAngle(shoulder, hip, ankle);
        double armAngle = getAngle(shoulder, elbow, wrist);
        double kneeAngle = getAngle(hip, knee, ankle);

        // --- Apply exponential smoothing (to reduce jitter) ---
        smoothBodyAngle = (float) (SMOOTHING_ALPHA * bodyAngle + (1 - SMOOTHING_ALPHA) * smoothBodyAngle);
        smoothArmAngle = (float) (SMOOTHING_ALPHA * armAngle + (1 - SMOOTHING_ALPHA) * smoothArmAngle);
        smoothKneeAngle = (float) (SMOOTHING_ALPHA * kneeAngle + (1 - SMOOTHING_ALPHA) * smoothKneeAngle);

        // --- Body tilt (vertical vs horizontal) ---
        double bodyTilt = Math.toDegrees(Math.atan2(
                shoulder.getPosition().y - ankle.getPosition().y,
                Math.abs(shoulder.getPosition().x - ankle.getPosition().x)
        ));

        // --- Elevation check (avoid lying flat) ---
        float shoulderY = shoulder.getPosition().y;
        float hipY = hip.getPosition().y;
        boolean elevated = (hipY - shoulderY) > 40;

        // --- Valid elbow plank conditions with tolerance ---
        boolean straightBody = (smoothBodyAngle > 160 - ANGLE_TOLERANCE);
        boolean elbowBent = (smoothArmAngle > 80 - ANGLE_TOLERANCE && smoothArmAngle < 110 + ANGLE_TOLERANCE);
        boolean kneesStraight = (smoothKneeAngle > 160 - ANGLE_TOLERANCE);
        boolean horizontal = (bodyTilt < 35 + ANGLE_TOLERANCE);

        // 🟨 Add standing filter — user must NOT be standing
        boolean validPlank = (!isStanding && straightBody && elbowBent && kneesStraight && horizontal && elevated);

        long now = System.currentTimeMillis();

        if (validPlank) {
            if (!isPlanking) {
                isPlanking = true;
                plankGraceStart = 0;
                lastPlankTimestamp = now;
                feedback = "Hold steady";
                if (listener != null) listener.onFeedbackUpdated(feedback);
            } else {
                long elapsed = now - Math.max(0, lastPlankTimestamp);
                plankAccumMs += elapsed;
                lastPlankTimestamp = now;
            }
        } else {
            if (isPlanking) {
                if (plankGraceStart == 0) {
                    plankGraceStart = now;
                } else if (now - plankGraceStart > PLANK_GRACE_MS) {
                    // lost plank for more than grace duration
                    isPlanking = false;
                    plankGraceStart = 0;
                    lastPlankTimestamp = 0;
                    feedback = "Plank lost";
                    if (listener != null) listener.onFeedbackUpdated(feedback);
                }
            }
        }

        long secs = plankAccumMs / 1000L;
        if (secs != lastReportedPlankSecond) {
            lastReportedPlankSecond = secs;
            if (listener != null) listener.onPlankTimeUpdated(secs, target);
        }

        if (secs >= target && !completed) {
            completed = true;
            if (listener != null)
                listener.onExerciseCompleted("Elbow plank completed! " + secs + "s held.");
        }
    }

    boolean isStanding(Pose pose) {
        PoseLandmark shoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);

        if (shoulder == null || hip == null) return false;

        float dx = Math.abs(shoulder.getPosition().x - hip.getPosition().x);
        float dy = Math.abs(shoulder.getPosition().y - hip.getPosition().y);

        return dy > dx * 1.5f; // standing if vertical distance >> horizontal distance
    }


    // --- Utility methods ---

    private PoseLandmark firstAvailable(Pose pose, int primary, int fallback) {
        PoseLandmark p = pose.getPoseLandmark(primary);
        if (p != null) return p;
        return pose.getPoseLandmark(fallback);
    }

    private static double getAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        double result = Math.toDegrees(
                atan2(lastPoint.getPosition().y - midPoint.getPosition().y,
                        lastPoint.getPosition().x - midPoint.getPosition().x)
                        - atan2(firstPoint.getPosition().y - midPoint.getPosition().y,
                        firstPoint.getPosition().x - midPoint.getPosition().x));
        result = Math.abs(result);
        if (result > 180) result = 360.0 - result;
        return result;
    }

    // Distance estimation with clamping & smoothing
    private float estimateDistanceMeters(Pose pose) {
        if (pose == null) return -1f;
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);

        float rawDistance = -1f;

        if (leftShoulder != null && rightShoulder != null) {
            float shoulderPx = Math.abs(leftShoulder.getPosition().x - rightShoulder.getPosition().x);
            Log.d("DistanceCheck", "shoulderPx=" + shoulderPx);
            if (shoulderPx >= MIN_SHOULDER_PX) {
                // linear mapping between measured px and meters, then clamp
                float t = (SHOULDER_1_5M_PX - shoulderPx) / (SHOULDER_1_5M_PX - SHOULDER_2_5M_PX);
                rawDistance = 1.5f + t * (2.5f - 1.5f);
            }
        }

        // fallback to torso height px
        if (rawDistance <= 0f) {
            PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
            if (leftShoulder != null && leftHip != null) {
                float torsoPx = Math.abs(leftShoulder.getPosition().y - leftHip.getPosition().y);
                Log.d("DistanceCheck", "torsoPx=" + torsoPx);
                float t = (TORSO_1_5M_PX - torsoPx) / (TORSO_1_5M_PX - TORSO_2_5M_PX);
                rawDistance = 1.5f + t * (2.5f - 1.5f);
            }
        }

        if (Float.isNaN(rawDistance) || Float.isInfinite(rawDistance) || rawDistance <= 0f) {
            return -1f;
        }

        // clamp and smooth
        rawDistance = Math.max(MIN_DISTANCE_M, Math.min(MAX_DISTANCE_M, rawDistance));
        if (smoothedDistance < 0f) smoothedDistance = rawDistance;
        else smoothedDistance = smoothedDistance * (1f - DISTANCE_SMOOTHING_ALPHA) + rawDistance * DISTANCE_SMOOTHING_ALPHA;

        Log.d("DistanceCheck", "smoothedDistance=" + smoothedDistance);
        return smoothedDistance;
    }

    public int getCurrentReps() { return currentReps; }
    public long getPlankSeconds() { return plankAccumMs / 1000L; }
    public boolean isCompleted() { return completed; }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
        listener = null;
    }
}
