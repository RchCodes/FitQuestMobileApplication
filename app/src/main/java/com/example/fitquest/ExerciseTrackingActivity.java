package com.example.fitquest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.List;

import static java.lang.Math.atan2;

public class ExerciseTrackingActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView repCounter;
    private TextView feedbackText;
    private TextView instructionText;
    private TextView timerText;
    private PoseOverlayView poseOverlay;
    private LinearLayout permissionLayout;
    private Button requestPermissionButton;
    private Button settingsButton;
    private PoseDetector poseDetector;
    private int squatReps = 0;
    private int targetReps = 10;
    private boolean isSquatting = false;
    private boolean isPlanking = false;
    private boolean isPushupDown = false;
    private String exerciseType = "squats";
    private String difficultyLevel = "beginner"; // beginner, advanced, expert, master
    private long plankStartTime = 0;
    private long plankTotalTime = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean exerciseCompleted = false;
    private boolean isTrackingActive = false; // Voice control for tracking
    
    // Cooldown to prevent double counting (now configurable)
    private long lastRepTime = 0;
    
    // Voice recognition
    private SpeechRecognizer speechRecognizer;
    private Button voiceButton;

    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO"};
    private static final int REQUEST_CODE_PERMISSIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_tracking);

        previewView = findViewById(R.id.previewView);
        repCounter = findViewById(R.id.repCounter);
        feedbackText = findViewById(R.id.feedbackText);
        instructionText = findViewById(R.id.instructionText);
        timerText = findViewById(R.id.timerText);
        poseOverlay = findViewById(R.id.poseOverlay);
        permissionLayout = findViewById(R.id.permission_layout);
        requestPermissionButton = findViewById(R.id.request_permission_button);

        // Settings button (overlay) to tweak level quickly
        settingsButton = new Button(this);
        settingsButton.setText("Settings");
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, UserSettingsActivity.class)));
        addContentView(settingsButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Voice activation button
        voiceButton = new Button(this);
        voiceButton.setText("🎤 Start Tracking");
        voiceButton.setOnClickListener(v -> toggleVoiceTracking());
        LinearLayout.LayoutParams voiceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        voiceParams.topMargin = 150; // Position below settings button
        addContentView(voiceButton, voiceParams);

        // Read difficulty from user level by default
        difficultyLevel = User.getDifficultyLevel(this);

        // Allow override from intent (for testing)
        if (getIntent().hasExtra("EXERCISE_TYPE")) {
            exerciseType = getIntent().getStringExtra("EXERCISE_TYPE");
        }
        if (getIntent().hasExtra("MAX_PROGRESS")) {
            targetReps = getIntent().getIntExtra("MAX_PROGRESS", 10);
        }
        if (getIntent().hasExtra("DIFFICULTY_LEVEL")) {
            difficultyLevel = getIntent().getStringExtra("DIFFICULTY_LEVEL");
        }

        // Setup pose detector with standard options
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                        .build();
        poseDetector = PoseDetection.getClient(options);

        // Set up permission button click listener
        requestPermissionButton.setOnClickListener(v -> {
            requestCameraPermission();
            requestMicPermission();
        });


        // Initialize speech recognizer
        initSpeechRecognizer();

        // Check and request camera permissions
        if (allPermissionsGranted()) {
            hidePermissionUI();
            startCamera();
            setupExerciseUI();
        } else {
            showPermissionUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When returning from settings, refresh difficulty and UI
        difficultyLevel = User.getDifficultyLevel(this);
        setupExerciseUI();
    }

    private void setupExerciseUI() {
        String difficultyText = "Difficulty: " + difficultyLevel.substring(0, 1).toUpperCase() + difficultyLevel.substring(1);
        
        switch (exerciseType) {
            case "squats":
                instructionText.setText("Stand with feet shoulder-width apart\nBend your knees and lower your body\nKeep your back straight\n" + difficultyText);
                repCounter.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.GONE);
                break;
            case "pushups":
                instructionText.setText("Start in plank position\nLower your body by bending elbows\nPush back up to starting position\n" + difficultyText);
                repCounter.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.GONE);
                break;
            case "plank":
                instructionText.setText("Hold your body in a straight line\nKeep your core tight\nDon't let your hips sag\n" + difficultyText);
                repCounter.setVisibility(View.GONE);
                timerText.setVisibility(View.VISIBLE);
                startPlankTimer();
                break;
        }
    }

    private void startPlankTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPlanking && !exerciseCompleted) {
                    plankTotalTime += 1000; // Add 1 second
                    updateTimerDisplay();
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void updateTimerDisplay() {
        long seconds = plankTotalTime / 1000;
        runOnUiThread(() -> timerText.setText("Plank Time: " + seconds + "s"));
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }
    }


    private void showPermissionUI() {
        permissionLayout.setVisibility(View.VISIBLE);
    }

    private void hidePermissionUI() {
        permissionLayout.setVisibility(View.GONE);
    }

    private void requestMicPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Microphone access is needed for voice tracking.", Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        } else {
            startVoiceListening();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) { // Camera
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 101) { // Mic
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceListening();
            } else {
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO);
                if (!showRationale) {
                    Toast.makeText(this, "Enable microphone permission in Settings", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void showPermissionInstructions() {
        Toast.makeText(this, "Go to Settings > Apps > FitQuest > Permissions > Camera & Microphone and enable them", Toast.LENGTH_LONG).show();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyzeImage);

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        @SuppressWarnings("UnsafeOptInUsageError")
        InputImage inputImage = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        poseDetector.process(inputImage)
                .addOnSuccessListener(this::detectExercise)
                .addOnFailureListener(e -> Log.e("PoseDetection", "Failed: " + e.getMessage()))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void detectExercise(Pose pose) {
        // Update pose overlay for joint visualization
        if (poseOverlay != null) {
            poseOverlay.setPose(pose);
        }

        // Check if tracking is active (voice controlled)
        if (!isTrackingActive) {
            runOnUiThread(() -> feedbackText.setText("Say 'Start' to begin tracking"));
            return;
        }

        // Check if we have enough body landmarks for accurate tracking
        if (!hasEnoughBodyLandmarks(pose)) {
            runOnUiThread(() -> feedbackText.setText("Please stand back to see your full body"));
            return;
        }

        // Debug logging if enabled (commented out for now - method not implemented)
        // if (UserSettingsActivity.isDebugMode(this)) {
        //     Log.d("ExerciseTracking", "Pose detected with " + pose.getAllPoseLandmarks().size() + " landmarks");
        // }

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
            default:
                detectSquat(pose); // default to squats
                break;
        }
    }

    private boolean hasEnoughBodyLandmarks(Pose pose) {
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

        // Check if all essential landmarks are present
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

        // Additional check: ensure landmarks have good visibility scores
        // ML Kit provides visibility scores to indicate landmark confidence
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
            // Calculate knee angle
            double kneeAngle = getAngle(leftHip, leftKnee, leftAnkle);
            
            // Show angle if enabled in settings (commented out for now - method not implemented)
            // if (UserSettingsActivity.shouldShowAngles(this)) {
            //     runOnUiThread(() -> instructionText.setText("Knee Angle: " + String.format("%.1f°", kneeAngle)));
            // }
            
            // Get difficulty-based thresholds
            double downThreshold = getSquatDownThreshold();
            double upThreshold = getSquatUpThreshold();

            // Improved squat detection using angle with difficulty levels
            if (kneeAngle < downThreshold && !isSquatting) {
                // Going down - knee angle is less than threshold
                isSquatting = true;
                runOnUiThread(() -> feedbackText.setText("Good! Keep going down"));
                Log.d("Squat", "Going down - Angle: " + kneeAngle + " (Threshold: " + downThreshold + ")");
            } else if (kneeAngle > upThreshold && isSquatting) {
                // Stood up - knee angle is greater than threshold
                long currentTime = System.currentTimeMillis();
                long repCooldown = 1500; // Default cooldown, will be configurable later
                if (currentTime - lastRepTime > repCooldown) {
                squatReps++;
                    lastRepTime = currentTime;
                    runOnUiThread(() -> {
                        repCounter.setText("Squat Reps: " + squatReps + "/" + targetReps);
                        feedbackText.setText("Great rep! Keep it up!");
                    });
                    Log.d("Squat", "Rep completed - Angle: " + kneeAngle + " (Threshold: " + upThreshold + ")");
                    
                    // Check if target reached
                    if (squatReps >= targetReps) {
                        completeExercise();
                        return;
                    }
                }
                isSquatting = false;
            } else if (kneeAngle > downThreshold && kneeAngle < upThreshold) {
                runOnUiThread(() -> feedbackText.setText("Hold the squat position"));
            }
        }
    }

    private void detectPushup(Pose pose) {
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);

        if (leftShoulder != null && leftElbow != null && leftWrist != null) {
            // Calculate elbow angle
            double elbowAngle = getAngle(leftShoulder, leftElbow, leftWrist);
            
            // Get difficulty-based thresholds
            double downThreshold = getPushupDownThreshold();
            double upThreshold = getPushupUpThreshold();

            // Pushup detection with difficulty levels
            if (elbowAngle < downThreshold && !isPushupDown) {
                // Going down
                isPushupDown = true;
                runOnUiThread(() -> feedbackText.setText("Good! Lower your body"));
                Log.d("Pushup", "Going down - Angle: " + elbowAngle + " (Threshold: " + downThreshold + ")");
            } else if (elbowAngle > upThreshold && isPushupDown) {
                // Pushed up
                long currentTime = System.currentTimeMillis();
                long repCooldown = 1500; // Default cooldown, will be configurable later
                if (currentTime - lastRepTime > repCooldown) {
                    squatReps++;
                    lastRepTime = currentTime;
                    runOnUiThread(() -> {
                        repCounter.setText("Pushup Reps: " + squatReps + "/" + targetReps);
                        feedbackText.setText("Excellent pushup! Keep going!");
                    });
                    Log.d("Pushup", "Rep completed - Angle: " + elbowAngle + " (Threshold: " + upThreshold + ")");
                    
                    // Check if target reached
                    if (squatReps >= targetReps) {
                        completeExercise();
                        return;
                    }
                }
                isPushupDown = false;
            } else if (elbowAngle > downThreshold && elbowAngle < upThreshold) {
                runOnUiThread(() -> feedbackText.setText("Hold the pushup position"));
            }
        }
    }

    private void detectPlank(Pose pose) {
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (leftShoulder != null && leftHip != null && leftAnkle != null) {
            // Calculate body alignment angle
            double bodyAngle = getAngle(leftShoulder, leftHip, leftAnkle);
            
            // Get difficulty-based threshold
            double plankThreshold = getPlankThreshold();

            // Plank detection - body should be relatively straight
            if (bodyAngle > plankThreshold && !isPlanking) {
                // Good plank position
                isPlanking = true;
                plankStartTime = System.currentTimeMillis();
                runOnUiThread(() -> feedbackText.setText("Perfect plank position! Hold it!"));
                Log.d("Plank", "Good position - Angle: " + bodyAngle + " (Threshold: " + plankThreshold + ")");
            } else if (bodyAngle < plankThreshold - 20 && isPlanking) {
                // Lost plank position (with some tolerance)
                isPlanking = false;
                runOnUiThread(() -> feedbackText.setText("Get back into plank position"));
                Log.d("Plank", "Position lost - Angle: " + bodyAngle + " (Threshold: " + plankThreshold + ")");
            } else if (bodyAngle >= plankThreshold - 20 && bodyAngle <= plankThreshold) {
                runOnUiThread(() -> feedbackText.setText("Keep your body straight"));
            }
            
            // Check if target time reached (convert targetReps to seconds for plank)
            if (plankTotalTime >= targetReps * 1000) {
                completeExercise();
            }
        }
    }

    // Difficulty-based threshold methods
    private double getSquatDownThreshold() {
        switch (difficultyLevel) {
            case "beginner": return 140; // forgiving
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

    private void completeExercise() {
        if (exerciseCompleted) return; // Prevent multiple completions
        
        exerciseCompleted = true;
        final String message;
        if (exerciseType.equals("plank")) {
            long seconds = plankTotalTime / 1000;
            message = "Plank completed! " + seconds + " seconds held.";
        } else {
            message = "Exercise completed! " + squatReps + " reps done.";
        }
        
        runOnUiThread(() -> feedbackText.setText("🎉 " + message));
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // Return to main activity after a short delay
        handler.postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 3000);
    }

    /**
     * Calculate the angle between three points
     * @param firstPoint First point
     * @param midPoint Middle point (vertex of the angle)
     * @param lastPoint Last point
     * @return Angle in degrees
     */
    private static double getAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        double result = Math.toDegrees(
                atan2(lastPoint.getPosition().y - midPoint.getPosition().y,
                        lastPoint.getPosition().x - midPoint.getPosition().x)
                        - atan2(firstPoint.getPosition().y - midPoint.getPosition().y,
                        firstPoint.getPosition().x - midPoint.getPosition().x));
        result = Math.abs(result); // Angle should never be negative
        if (result > 180) {
            result = (360.0 - result); // Always get the acute representation of the angle
        }
        return result;
    }

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    runOnUiThread(() -> voiceButton.setText("🎤 Listening..."));
                }

                @Override
                public void onBeginningOfSpeech() {}

                @Override
                public void onRmsChanged(float rmsdB) {}

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {
                    runOnUiThread(() -> voiceButton.setText(isTrackingActive ? "🎤 Stop Tracking" : "🎤 Start Tracking"));
                }

                @Override
                public void onError(int error) {
                    runOnUiThread(() -> {
                        voiceButton.setText(isTrackingActive ? "🎤 Stop Tracking" : "🎤 Start Tracking");
                        Toast.makeText(ExerciseTrackingActivity.this, "Voice recognition error", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String command = matches.get(0).toLowerCase();
                        processVoiceCommand(command);
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {}

                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        }
    }

    private void toggleVoiceTracking() {
        if (isTrackingActive) {
            stopTracking();
        } else {
            startVoiceListening();
        }
    }

    private void startVoiceListening() {
        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            speechRecognizer.startListening(intent);
        }
    }


    private void processVoiceCommand(String command) {
        Log.d("VoiceCommand", "Received: " + command);
        
        if (command.contains("start") || command.contains("begin") || command.contains("go")) {
            startTracking();
        } else if (command.contains("stop") || command.contains("pause") || command.contains("end")) {
            stopTracking();
        } else if (command.contains("reset")) {
            resetExercise();
        }
        
        // Continue listening for more commands
        handler.postDelayed(this::startVoiceListening, 1000);
    }

    private void startTracking() {
        isTrackingActive = true;
        runOnUiThread(() -> {
            voiceButton.setText("🎤 Stop Tracking");
            feedbackText.setText("Tracking started! Get into position.");
        });
        Toast.makeText(this, "Exercise tracking started!", Toast.LENGTH_SHORT).show();
    }

    private void stopTracking() {
        isTrackingActive = false;
        runOnUiThread(() -> {
            voiceButton.setText("🎤 Start Tracking");
            feedbackText.setText("Tracking stopped. Say 'Start' to resume.");
        });
        Toast.makeText(this, "Exercise tracking stopped!", Toast.LENGTH_SHORT).show();
    }

    private void resetExercise() {
        squatReps = 0;
        plankTotalTime = 0;
        isSquatting = false;
        isPlanking = false;
        isPushupDown = false;
        runOnUiThread(() -> {
            repCounter.setText(exerciseType.substring(0, 1).toUpperCase() + exerciseType.substring(1) + " Reps: 0/" + targetReps);
            timerText.setText("Plank Time: 0s");
            feedbackText.setText("Exercise reset! Ready to start.");
        });
        Toast.makeText(this, "Exercise reset!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (poseDetector != null) {
            poseDetector.close();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
