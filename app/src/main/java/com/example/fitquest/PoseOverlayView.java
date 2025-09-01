package com.example.fitquest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

public class PoseOverlayView extends View {
    private Pose pose;
    private Paint landmarkPaint;
    private Paint connectionPaint;
    private Paint textPaint;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    public PoseOverlayView(Context context) {
        super(context);
        init();
    }

    public PoseOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        landmarkPaint = new Paint();
        landmarkPaint.setColor(Color.RED);
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setStrokeWidth(8f);

        connectionPaint = new Paint();
        connectionPaint.setColor(Color.GREEN);
        connectionPaint.setStyle(Paint.Style.STROKE);
        connectionPaint.setStrokeWidth(4f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30f);
        textPaint.setStyle(Paint.Style.FILL);
    }

    public void setPose(Pose pose) {
        this.pose = pose;
        invalidate();
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (pose == null) return;

        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        
        // Draw landmarks
        for (PoseLandmark landmark : landmarks) {
            if (landmark.getInFrameLikelihood() > 0.5f) {
                PointF point = landmark.getPosition();
                float x = point.x * scaleX;
                float y = point.y * scaleY;
                
                // Draw landmark circle
                canvas.drawCircle(x, y, 8f, landmarkPaint);
                
                // Draw landmark name
                String landmarkName = getLandmarkName(landmark.getLandmarkType());
                canvas.drawText(landmarkName, x + 15, y - 15, textPaint);
            }
        }

        // Draw connections
        drawConnections(canvas);
    }

    private String getLandmarkName(int landmarkType) {
        switch (landmarkType) {
            case PoseLandmark.NOSE: return "Nose";
            case PoseLandmark.LEFT_EYE: return "LEye";
            case PoseLandmark.RIGHT_EYE: return "REye";
            case PoseLandmark.LEFT_EAR: return "LEar";
            case PoseLandmark.RIGHT_EAR: return "REar";
            case PoseLandmark.LEFT_SHOULDER: return "LShoulder";
            case PoseLandmark.RIGHT_SHOULDER: return "RShoulder";
            case PoseLandmark.LEFT_ELBOW: return "LElbow";
            case PoseLandmark.RIGHT_ELBOW: return "RElbow";
            case PoseLandmark.LEFT_WRIST: return "LWrist";
            case PoseLandmark.RIGHT_WRIST: return "RWrist";
            case PoseLandmark.LEFT_HIP: return "LHip";
            case PoseLandmark.RIGHT_HIP: return "RHip";
            case PoseLandmark.LEFT_KNEE: return "LKnee";
            case PoseLandmark.RIGHT_KNEE: return "RKnee";
            case PoseLandmark.LEFT_ANKLE: return "LAnkle";
            case PoseLandmark.RIGHT_ANKLE: return "RAnkle";
            default: return "";
        }
    }

    private void drawConnections(Canvas canvas) {
        // Define connections between landmarks
        int[][] connections = {
            // Face
            {PoseLandmark.LEFT_EYE, PoseLandmark.RIGHT_EYE},
            {PoseLandmark.LEFT_EYE, PoseLandmark.LEFT_EAR},
            {PoseLandmark.RIGHT_EYE, PoseLandmark.RIGHT_EAR},
            {PoseLandmark.NOSE, PoseLandmark.LEFT_EYE},
            {PoseLandmark.NOSE, PoseLandmark.RIGHT_EYE},
            
            // Torso
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER},
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP},
            {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP},
            {PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP},
            
            // Arms
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW},
            {PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST},
            {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW},
            {PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST},
            
            // Legs
            {PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE},
            {PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE},
            {PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE},
            {PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE}
        };

        for (int[] connection : connections) {
            PoseLandmark first = pose.getPoseLandmark(connection[0]);
            PoseLandmark second = pose.getPoseLandmark(connection[1]);
            
            if (first != null && second != null && 
                first.getInFrameLikelihood() > 0.5f && 
                second.getInFrameLikelihood() > 0.5f) {
                
                PointF firstPoint = first.getPosition();
                PointF secondPoint = second.getPosition();
                
                float x1 = firstPoint.x * scaleX;
                float y1 = firstPoint.y * scaleY;
                float x2 = secondPoint.x * scaleX;
                float y2 = secondPoint.y * scaleY;
                
                canvas.drawLine(x1, y1, x2, y2, connectionPaint);
            }
        }
    }
}
