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

    // Frame and transform info
    private int imageWidth = 0;
    private int imageHeight = 0;
    private int rotationDegrees = 0;
    private boolean isFrontFacing = true;
    private int viewWidth = 0;
    private int viewHeight = 0;

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
        landmarkPaint.setColor(Color.WHITE); // Changed to white to match the image
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setStrokeWidth(8f);

        connectionPaint = new Paint();
        connectionPaint.setColor(Color.WHITE); // Changed to white to match the image
        connectionPaint.setStyle(Paint.Style.STROKE);
        connectionPaint.setStrokeWidth(4f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(20f); // Smaller text to avoid clutter
        textPaint.setStyle(Paint.Style.FILL);
    }

    public void setPose(Pose pose) {
        this.pose = pose;
        invalidate();
    }

    public void setFrameInfo(int imageWidth, int imageHeight, int rotationDegrees, boolean isFrontFacing, int viewWidth, int viewHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.rotationDegrees = rotationDegrees;
        this.isFrontFacing = isFrontFacing;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (pose == null || imageWidth == 0 || imageHeight == 0) return;

        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        
        // Draw connections first
        drawConnections(canvas);
        
        // Draw landmarks
        for (PoseLandmark landmark : landmarks) {
            if (landmark.getInFrameLikelihood() > 0.5f) {
                PointF mappedPoint = mapPoint(landmark.getPosition());
                canvas.drawCircle(mappedPoint.x, mappedPoint.y, 6f, landmarkPaint);
            }
        }
    }

    private PointF mapPoint(PointF imagePoint) {
        if (imageWidth == 0 || imageHeight == 0 || viewWidth == 0 || viewHeight == 0) {
            return imagePoint;
        }

        float x = imagePoint.x;
        float y = imagePoint.y;

        // For front camera, mirror horizontally
        if (isFrontFacing) {
            x = imageWidth - x;
        }

        // Calculate scale factors for fitCenter
        float scaleX = (float) viewWidth / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY);

        // Calculate offsets to center the image
        float scaledWidth = imageWidth * scale;
        float scaledHeight = imageHeight * scale;
        float offsetX = (viewWidth - scaledWidth) / 2f;
        float offsetY = (viewHeight - scaledHeight) / 2f;

        // Apply scaling and centering
        float mappedX = x * scale + offsetX;
        float mappedY = y * scale + offsetY;

        return new PointF(mappedX, mappedY);
    }

    private void drawConnections(Canvas canvas) {
        if (pose == null) return;

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
                
                PointF firstPoint = mapPoint(first.getPosition());
                PointF secondPoint = mapPoint(second.getPosition());
                
                canvas.drawLine(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y, connectionPaint);
            }
        }
    }
}
