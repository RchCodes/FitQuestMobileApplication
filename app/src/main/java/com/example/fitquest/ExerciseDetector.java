package com.example.fitquest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import static java.lang.Math.atan2;

public class ExerciseDetector {
    private final Context context;
    private final Handler handler;
    
    // Exercise state
    private String exerciseType = "squats";
    private String difficultyLevel = "beginner";
    private int targetReps = 10;
    private int currentReps = 0;
    private boolean exerciseCompleted = false;
    
    // Exercise-specific states
    private boolean isSquatting = false;
    private boolean isPlanking = false;
    private boolean isPushupDown = false;
    private boolean isCrunching = false;
    private boolean isLungingDown = false;
    
    // Plank timing
    private long plankStartTime = 0;
    private long plankTotalTime = 0;
    
    // Cooldown
    private long lastRepTime = 0;
    private static final long REP_COOLDOWN_MS = 1500;
    
    // Feedback text
    private String feedbackText = "Ready to start!";

    public ExerciseDetector(Context context, String difficultyLevel, int targetReps) {
        this.context = context;
        this.difficultyLevel = difficultyLevel;
        this.targetReps = targetReps;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void updateExercise(String exerciseType, String difficultyLevel, int targetReps) {
        this.exerciseType = exerciseType;
        this.difficultyLevel = difficultyLevel;
        this.targetReps = targetReps;
        resetExercise();
    }

    public void processPose(Pose pose, String exerciseType) {
        switch (exerciseType) {
            case "squats":
                detectSquat(pose);
                break;
            case "pushups":
                detectPushup(pose);
                break;
            case "plank":
                detectPlank(pose);
                break;
            case "crunches":
                detectCrunch(pose);
                break;
            case "lunges":
                detectLunge(pose);
                break;
            default:
                detectSquat(pose);
                break;
        }
    }

    public boolean hasEnoughBodyLandmarks(Pose pose) {
        // Get all required landmarks
        PoseLandmark nose = pose.getPoseLandmark(PoseLandmark.NOSE);
        PoseLandmark lShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark lElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark rElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark lWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark lHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark lKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark lAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        boolean hasAllLandmarks = nose != null &&
                lShoulder != null && rShoulder != null &&
                lElbow != null && rElbow != null &&
                lWrist != null && rWrist != null &&
                lHip != null && rHip != null &&
                lKnee != null && rKnee != null &&
                lAnkle != null && rAnkle != null;

        if (!hasAllLandmarks) {
            return false;
        }

        boolean hasGoodVisibility =
                (nose.getInFrameLikelihood() > 0.5f) &&
                (lShoulder.getInFrameLikelihood() > 0.5f && rShoulder.getInFrameLikelihood() > 0.5f) &&
                (lElbow.getInFrameLikelihood() > 0.5f && rElbow.getInFrameLikelihood() > 0.5f) &&
                (lWrist.getInFrameLikelihood() > 0.5f && rWrist.getInFrameLikelihood() > 0.5f) &&
                (lHip.getInFrameLikelihood() > 0.5f && rHip.getInFrameLikelihood() > 0.5f) &&
                (lKnee.getInFrameLikelihood() > 0.5f && rKnee.getInFrameLikelihood() > 0.5f) &&
                (lAnkle.getInFrameLikelihood() > 0.5f && rAnkle.getInFrameLikelihood() > 0.5f);

        return hasGoodVisibility;
    }

    private void detectSquat(Pose pose) {
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (leftHip != null && leftKnee != null && leftAnkle != null) {
            double kneeAngle = getAngle(leftHip, leftKnee, leftAnkle);
            double downThreshold = getSquatDownThreshold();
            double upThreshold = getSquatUpThreshold();

            if (kneeAngle < downThreshold && !isSquatting) {
                isSquatting = true;
                feedbackText = "Good! Keep going down";
            } else if (kneeAngle > upThreshold && isSquatting) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRepTime > REP_COOLDOWN_MS) {
                    currentReps++;
                    lastRepTime = currentTime;
                    feedbackText = "Great rep! Keep it up!";
                    
                    if (currentReps >= targetReps) {
                        exerciseCompleted = true;
                        return;
                    }
                }
                isSquatting = false;
            }
        }
    }

    private void detectPushup(Pose pose) {
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);

        if (leftShoulder != null && leftElbow != null && leftWrist != null) {
            double elbowAngle = getAngle(leftShoulder, leftElbow, leftWrist);
            double downThreshold = getPushupDownThreshold();
            double upThreshold = getPushupUpThreshold();

            if (elbowAngle < downThreshold && !isPushupDown) {
                isPushupDown = true;
                feedbackText = "Good! Lower your body";
            } else if (elbowAngle > upThreshold && isPushupDown) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRepTime > REP_COOLDOWN_MS) {
                    currentReps++;
                    lastRepTime = currentTime;
                    feedbackText = "Excellent pushup! Keep going!";
                    
                    if (currentReps >= targetReps) {
                        exerciseCompleted = true;
                        return;
                    }
                }
                isPushupDown = false;
            }
        }
    }

    private void detectCrunch(Pose pose) {
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);

        if (leftShoulder != null && leftHip != null && leftKnee != null) {
            double hipAngle = getAngle(leftShoulder, leftHip, leftKnee);
            double upThreshold = getCrunchUpThreshold();
            double downThreshold = getCrunchDownThreshold();

            if (hipAngle < downThreshold && !isCrunching) {
                isCrunching = true;
                feedbackText = "Crunch up";
            } else if (hipAngle > upThreshold && isCrunching) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRepTime > REP_COOLDOWN_MS) {
                    currentReps++;
                    lastRepTime = currentTime;
                    feedbackText = "Good crunch!";
                    
                    if (currentReps >= targetReps) {
                        exerciseCompleted = true;
                        return;
                    }
                }
                isCrunching = false;
            }
        }
    }

    private void detectLunge(Pose pose) {
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (leftHip != null && leftKnee != null && leftAnkle != null) {
            double kneeAngle = getAngle(leftHip, leftKnee, leftAnkle);
            double downThreshold = getLungeDownThreshold();
            double upThreshold = getLungeUpThreshold();

            if (kneeAngle < downThreshold && !isLungingDown) {
                isLungingDown = true;
                feedbackText = "Go down into lunge";
            } else if (kneeAngle > upThreshold && isLungingDown) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRepTime > REP_COOLDOWN_MS) {
                    currentReps++;
                    lastRepTime = currentTime;
                    feedbackText = "Nice lunge!";
                    
                    if (currentReps >= targetReps) {
                        exerciseCompleted = true;
                        return;
                    }
                }
                isLungingDown = false;
            }
        }
    }

    private void detectPlank(Pose pose) {
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (leftShoulder != null && leftHip != null && leftAnkle != null) {
            double bodyAngle = getAngle(leftShoulder, leftHip, leftAnkle);
            double plankThreshold = getPlankThreshold();

            if (bodyAngle > plankThreshold && !isPlanking) {
                isPlanking = true;
                plankStartTime = System.currentTimeMillis();
                feedbackText = "Perfect plank position! Hold it!";
            } else if (bodyAngle < plankThreshold - 20 && isPlanking) {
                isPlanking = false;
                feedbackText = "Get back into plank position";
            }

            if (plankTotalTime >= targetReps * 1000) {
                exerciseCompleted = true;
            }
        }
    }

    public void startPlankTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPlanking && !exerciseCompleted) {
                    plankTotalTime += 1000;
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    public void resetExercise() {
        currentReps = 0;
        plankTotalTime = 0;
        exerciseCompleted = false;
        isSquatting = false;
        isPlanking = false;
        isPushupDown = false;
        isCrunching = false;
        isLungingDown = false;
        feedbackText = "Exercise reset! Ready to start.";
    }

    // Getters
    public int getCurrentReps() { return currentReps; }
    public long getPlankTimeSeconds() { return plankTotalTime / 1000; }
    public boolean isExerciseCompleted() { return exerciseCompleted; }
    public String getFeedbackText() { return feedbackText; }

    // Difficulty-based threshold methods
    private double getSquatDownThreshold() {
        switch (difficultyLevel) {
            case "beginner": return 140;
            case "advanced": return 130;
            case "expert": return 120;
            case "master": return 110;
            default: return 140;
        }
    }

    private double getSquatUpThreshold() {
        switch (difficultyLevel) {
            case "beginner": return 150;
            case "advanced": return 155;
            case "expert": return 160;
            case "master": return 165;
            default: return 150;
        }
    }

    private double getPushupDownThreshold() {
        switch (difficultyLevel) {
            case "beginner": return 120;
            case "advanced": return 110;
            case "expert": return 100;
            case "master": return 90;
            default: return 120;
        }
    }

    private double getPushupUpThreshold() {
        switch (difficultyLevel) {
            case "beginner": return 140;
            case "advanced": return 150;
            case "expert": return 160;
            case "master": return 165;
            default: return 140;
        }
    }

    private double getPlankThreshold() {
        switch (difficultyLevel) {
            case "beginner": return 140;
            case "advanced": return 150;
            case "expert": return 160;
            case "master": return 165;
            default: return 140;
        }
    }

    private double getCrunchDownThreshold() {
        switch (difficultyLevel) {
            case "beginner": return 120;
            case "advanced": return 110;
            case "expert": return 100;
            case "master": return 95;
            default: return 120;
        }
    }

    private double getCrunchUpThreshold() {
        switch (difficultyLevel) {
            case "beginner": return 150;
            case "advanced": return 155;
            case "expert": return 160;
            case "master": return 165;
            default: return 150;
        }
    }

    private double getLungeDownThreshold() {
        switch (difficultyLevel) {
            case "beginner": return 140;
            case "advanced": return 130;
            case "expert": return 120;
            case "master": return 110;
            default: return 140;
        }
    }

    private double getLungeUpThreshold() {
        switch (difficultyLevel) {
            case "beginner": return 150;
            case "advanced": return 155;
            case "expert": return 160;
            case "master": return 165;
            default: return 150;
        }
    }

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

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
    }
}
