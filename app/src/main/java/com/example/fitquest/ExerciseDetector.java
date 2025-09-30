package com.example.fitquest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

    // Add this field near the top with other states
    private boolean distanceReady = false;

    private String difficultyLevel = "beginner";
    private int target = 10;
    private int currentReps = 0;
    private boolean completed = false;

    // states to ensure full cycle down->up
    private boolean downState = false; // generic "down" or "low" state
    private long lastRepTime = 0;
    private static final long DEFAULT_COOLDOWN = 800; // ms

    // plank
    private boolean isPlanking = false;
    private long plankStart = 0;
    private long plankAccumMs = 0;
    private long plankGraceStart = 0;
    private static final long PLANK_GRACE_MS = 2500;

    // smoothing & fallback constants
    private float smoothedDistance = -1f;
    private static final float DISTANCE_SMOOTHING_ALPHA = 0.18f; // 0 = no update, 1 = raw only

    // shoulder calibration (closer -> bigger px)
    private static final float MIN_SHOULDER_PX = 60f;      // ignore if too small
    private static final float SHOULDER_1_5M_PX = 90f;     // observed at ~1.5m
    private static final float SHOULDER_2_5M_PX = 80f;     // observed at ~2.0m

    // torso (left shoulder -> left hip) fallback (closer -> bigger px)
    private static final float TORSO_1_5M_PX = 180f;       // adjust with testing
    private static final float TORSO_2_5M_PX = 120f;       // adjust with testing



    private String feedback = "Ready";

    public ExerciseDetector(Context ctx, String difficulty, int targetReps) {
        this.context = ctx;
        this.difficultyLevel = difficulty == null ? "beginner" : difficulty;
        this.target = Math.max(1, targetReps);
    }

    public void setListener(ExerciseListener l) { this.listener = l; }

    public void updateExercise(String exercise, String difficulty, int targetReps) {
        this.exerciseType = exercise;
        this.difficultyLevel = difficulty;
        this.target = Math.max(1, targetReps);
        resetExercise();
    }

    public void resetExercise() {
        currentReps = 0;
        completed = false;
        downState = false;
        lastRepTime = 0;
        isPlanking = false;
        plankStart = 0;
        plankAccumMs = 0;
        plankGraceStart = 0;
        distanceReady = false; // 🔹 reset distance readiness
        feedback = "Step back until 1.5–2.5m from camera";
        if (listener != null) {
            listener.onRepCountChanged(currentReps, target);
            listener.onFeedbackUpdated(feedback);
        }
    }


    public void processPose(Pose pose, String exercise) {
        if (pose == null || completed) return;

        // Phase 1: Distance check (only until user is "ready")
        if (!distanceReady) {
            float distanceM = estimateDistanceMeters(pose);
            if (distanceM < 1.5f || distanceM > 2.5f) {
                feedback = "Move between 1.5m–2.5m from camera";
                if (listener != null) listener.onFeedbackUpdated(feedback);
                return;
            } else {
                distanceReady = true;
                feedback = "Good! Now get into starting position";
                if (listener != null) listener.onFeedbackUpdated(feedback);
                // 🚫 Don't return here — allow starting pose + detection to run
            }
        }


        // ✅ Phase 3: Actual exercise detection
        switch (exercise) {
            case "pushups": detectPushup(pose); break;
            case "squats": detectSquat(pose); break;
            case "plank": detectPlank(pose); break;
            case "crunches": detectCrunch(pose); break;
            case "lunges": detectLunge(pose); break;
            default: detectSquat(pose); break;
        }

        if (listener != null) listener.onFeedbackUpdated(feedback);
    }

    private long getCooldown() {
        return DEFAULT_COOLDOWN;
    }

    private void registerRepIfCooldown() {
        long now = System.currentTimeMillis();
        if (now - lastRepTime < getCooldown()) return;
        lastRepTime = now;
        currentReps++;
        if (listener != null) listener.onRepCountChanged(currentReps, target);
        if (currentReps >= target && !completed) {
            completed = true;
            if (listener != null) listener.onExerciseCompleted(currentReps + " reps done.");
        }
    }

    private void detectSquat(Pose pose) {
        PoseLandmark hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark ankle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        if (hip == null || knee == null || ankle == null) return;

        double angle = getAngle(hip, knee, ankle);
        double down = 140;
        double up = 150;
        if (angle < down && !downState) {
            downState = true;
            feedback = "Down";
        } else if (angle > up && downState) {
            downState = false;
            registerRepIfCooldown();
            feedback = "Nice squat!";
        }
    }

    private void detectPushup(Pose pose) {
        PoseLandmark shoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark elbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        if (shoulder == null || elbow == null || wrist == null) return;

        double angle = getAngle(shoulder, elbow, wrist);
        double down = 100;
        double up = 140;
        if (angle < down && !downState) {
            downState = true;
            feedback = "Down";
        } else if (angle > up && downState) {
            downState = false;
            registerRepIfCooldown();
            feedback = "Great pushup!";
        }
    }

    private void detectCrunch(Pose pose) {
        PoseLandmark shoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        if (shoulder == null || hip == null || knee == null) return;

        double angle = getAngle(shoulder, hip, knee);
        double down = 120;
        double up = 150;
        if (angle < down && !downState) {
            downState = true;
            feedback = "Up";
        } else if (angle > up && downState) {
            downState = false;
            registerRepIfCooldown();
            feedback = "Crunch!";
        }
    }

    private void detectLunge(Pose pose) {
        PoseLandmark hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark ankle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        if (hip == null || knee == null || ankle == null) return;

        double angle = getAngle(hip, knee, ankle);
        double down = 140;
        double up = 150;
        if (angle < down && !downState) {
            downState = true;
            feedback = "Down";
        } else if (angle > up && downState) {
            downState = false;
            registerRepIfCooldown();
            feedback = "Nice lunge!";
        }
    }

    private void detectPlank(Pose pose) {
        PoseLandmark s = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark h = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark a = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        if (s == null || h == null || a == null) return;

        double angle = getAngle(s, h, a);
        double threshold = 140;
        long now = System.currentTimeMillis();

        if (angle > threshold) {
            if (!isPlanking) {
                isPlanking = true;
                plankStart = now;
                plankGraceStart = 0;
                feedback = "Hold plank";
            } else {
                // add elapsed
                plankAccumMs += now - plankStart;
                plankStart = now;
            }
        } else {
            if (isPlanking) {
                if (plankGraceStart == 0) plankGraceStart = now;
                else if (now - plankGraceStart > PLANK_GRACE_MS) {
                    isPlanking = false;
                    plankStart = 0;
                    plankGraceStart = 0;
                } else {
                    // within grace period: do not add time yet
                }
            }
        }

        long secs = plankAccumMs / 1000;
        if (listener != null) listener.onPlankTimeUpdated(secs, target);

        // report delta seconds to QuestManager from Activity (Activity calls QuestManager)
        if (secs >= target && !completed) {
            completed = true;
            if (listener != null) listener.onExerciseCompleted("Plank completed! " + secs + "s held.");
        }
    }

    // utility
    private static double getAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        double result = Math.toDegrees(
                atan2(lastPoint.getPosition().y - midPoint.getPosition().y,
                        lastPoint.getPosition().x - midPoint.getPosition().x)
                        - atan2(firstPoint.getPosition().y - midPoint.getPosition().y,
                        firstPoint.getPosition().x - midPoint.getPosition().x));
        result = Math.abs(result);
        if (result > 180) {
            result = (360.0 - result);
        }
        return result;
    }

    public int getCurrentReps() { return currentReps; }
    public long getPlankSeconds() { return plankAccumMs / 1000; }
    public boolean isCompleted() { return completed; }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
        listener = null;
    }

    // Estimate distance in meters from shoulder width
    // Estimate distance in meters from shoulder width or fallback to left shoulder->hip height
    private float estimateDistanceMeters(Pose pose) {
        if (pose == null) return -1f;

        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        float rawDistance = -1f;

        // 1) Primary: shoulder horizontal width
        if (leftShoulder != null && rightShoulder != null) {
            float shoulderWidthPx = Math.abs(leftShoulder.getPosition().x - rightShoulder.getPosition().x);
            Log.d("DistanceCheck", "Shoulder px width: " + shoulderWidthPx);

            if (shoulderWidthPx >= MIN_SHOULDER_PX) {
                rawDistance = 1.5f + (SHOULDER_1_5M_PX - shoulderWidthPx)
                        * (2.5f - 1.5f) / (SHOULDER_1_5M_PX - SHOULDER_2_5M_PX);
                Log.d("DistanceCheck", "Using shoulders -> rawDist=" + rawDistance);
            }
        }

        // 2) Fallback: left shoulder -> left hip vertical distance (useful for side/partial views)
        if (rawDistance <= 0f) {
            PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
            if (leftShoulder != null && leftHip != null && TORSO_1_5M_PX > TORSO_2_5M_PX) {
                float torsoPx = Math.abs(leftShoulder.getPosition().y - leftHip.getPosition().y);
                Log.d("DistanceCheck", "Torso px height: " + torsoPx);

                // map torsoPx linearly between 1.5m and 2.5m (adjust constants via calibration)
                rawDistance = 1.5f + (TORSO_1_5M_PX - torsoPx) * (2.5f - 1.5f)
                        / (TORSO_1_5M_PX - TORSO_2_5M_PX);
                Log.d("DistanceCheck", "Using torso -> rawDist=" + rawDistance);
            }
        }

        // If still couldn't compute, return -1 to indicate "unknown"
        if (rawDistance <= 0f || Float.isNaN(rawDistance) || Float.isInfinite(rawDistance)) {
            return -1f;
        }

        // 3) Smooth the distance to reduce flicker
        if (smoothedDistance < 0f) smoothedDistance = rawDistance;
        else smoothedDistance = smoothedDistance * (1f - DISTANCE_SMOOTHING_ALPHA)
                + rawDistance * DISTANCE_SMOOTHING_ALPHA;

        Log.d("DistanceCheck", "Smoothed distance: " + smoothedDistance + " m");
        return smoothedDistance;
    }

}
