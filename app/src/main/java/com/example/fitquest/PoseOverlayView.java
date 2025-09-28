package com.example.fitquest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.Arrays;
import java.util.List;

public class PoseOverlayView extends View {

    private Pose pose;
    private int imageWidth;
    private int imageHeight;
    private int viewWidth;
    private int viewHeight;
    private boolean isFrontFacing;

    private final Paint paintLandmark;
    private final Paint paintConnection;

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paintLandmark = new Paint();
        paintLandmark.setColor(Color.WHITE);
        paintLandmark.setStyle(Paint.Style.FILL);
        paintLandmark.setStrokeWidth(12f);

        paintConnection = new Paint();
        paintConnection.setColor(Color.WHITE);
        paintConnection.setStyle(Paint.Style.STROKE);
        paintConnection.setStrokeWidth(8f);
    }

    public void setFrameInfo(int imageWidth, int imageHeight, int rotation, boolean isFrontFacing, int viewWidth, int viewHeight) {
        // Swap width/height if the buffer is rotated 90/270
        if (rotation == 90 || rotation == 270) {
            this.imageWidth = imageHeight;
            this.imageHeight = imageWidth;
        } else {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
        }
        this.isFrontFacing = isFrontFacing;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pose == null) return;

        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) return;

        // Draw connections first, then landmarks on top
        drawConnections(canvas, landmarks);
        drawLandmarks(canvas, landmarks);
    }

    private void drawLandmarks(Canvas canvas, List<PoseLandmark> landmarks) {
        for (PoseLandmark landmark : landmarks) {
            PointF point = mapPoint(landmark.getPosition());
            canvas.drawCircle(point.x, point.y, 12f, paintLandmark);
        }
    }

    private void drawConnections(Canvas canvas, List<PoseLandmark> landmarks) {
        int[][] bodyJoints = {
                {PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER},
                {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW},
                {PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST},
                {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW},
                {PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST},
                {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP},
                {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP},
                {PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP},
                {PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE},
                {PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE},
                {PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE},
                {PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE}
        };

        for (int[] pair : bodyJoints) {
            PoseLandmark first = pose.getPoseLandmark(pair[0]);
            PoseLandmark second = pose.getPoseLandmark(pair[1]);
            if (first != null && second != null) {
                PointF point1 = mapPoint(first.getPosition());
                PointF point2 = mapPoint(second.getPosition());
                canvas.drawLine(point1.x, point1.y, point2.x, point2.y, paintConnection);
            }
        }
    }

    private PointF mapPoint(PointF originalPoint) {
        float x = originalPoint.x;
        float y = originalPoint.y;

        // Scale factors (fill mode, not fit)
        float scaleX = (float) viewWidth / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;

        x *= scaleX;
        y *= scaleY;

        // Mirror horizontally if front-facing (PreviewView already mirrors)
        if (isFrontFacing) {
            x = viewWidth - x;
        }

        return new PointF(x, y);
    }
}
