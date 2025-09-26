package com.example.fitquest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

public class PoseOverlayView extends View {
    private Pose pose;
    private Paint landmarkPaint, connectionPaint, textPaint;
    private int imageWidth, imageHeight, rotationDegrees, viewWidth, viewHeight;
    private boolean isFrontFacing;
    private Matrix transformMatrix = null;

    public PoseOverlayView(Context context) { super(context); init(); }
    public PoseOverlayView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        landmarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        landmarkPaint.setColor(Color.CYAN);
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setStrokeWidth(8f);

        connectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        connectionPaint.setColor(Color.CYAN);
        connectionPaint.setStyle(Paint.Style.STROKE);
        connectionPaint.setStrokeWidth(4f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        textPaint.setStyle(Paint.Style.FILL);
    }

    public void setPose(Pose pose) { this.pose = pose; postInvalidate(); }

    public void setFrameInfo(int imageWidth, int imageHeight, int rotationDegrees,
                             boolean isFrontFacing, int viewWidth, int viewHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.rotationDegrees = rotationDegrees;
        this.isFrontFacing = isFrontFacing;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.transformMatrix = null;
        postInvalidate();
    }

    public void setTransformMatrix(Matrix matrix) { this.transformMatrix = matrix; postInvalidate(); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pose == null || imageWidth == 0 || imageHeight == 0 || viewWidth == 0 || viewHeight == 0) return;

        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks == null || landmarks.isEmpty()) return;

        drawConnections(canvas);

        for (PoseLandmark lm : landmarks) {
            if (lm == null || lm.getInFrameLikelihood() < 0.2f) continue;
            PointF mapped = mapPoint(lm.getPosition());
            canvas.drawCircle(mapped.x, mapped.y, 8f, landmarkPaint);
        }
    }

    private PointF mapPoint(PointF point) {
        float scaleX = (float) viewWidth / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY);

        float scaledWidth = imageWidth * scale;
        float scaledHeight = imageHeight * scale;
        float offsetX = (viewWidth - scaledWidth) / 2f;
        float offsetY = (viewHeight - scaledHeight) / 2f;

        float x = point.x * scale + offsetX;
        float y = point.y * scale + offsetY;

        if (isFrontFacing) x = viewWidth - x;

        if (rotationDegrees != 0) {
            float cx = viewWidth / 2f;
            float cy = viewHeight / 2f;
            double rad = Math.toRadians(rotationDegrees);
            float dx = x - cx;
            float dy = y - cy;
            x = (float) (dx * Math.cos(rad) - dy * Math.sin(rad)) + cx;
            y = (float) (dx * Math.sin(rad) + dy * Math.cos(rad)) + cy;
        }
        return new PointF(x, y);
    }

    private void drawConnections(Canvas canvas) {
        if (pose == null) return;
        int[][] connections = {
                {PoseLandmark.LEFT_EYE, PoseLandmark.RIGHT_EYE},
                {PoseLandmark.NOSE, PoseLandmark.LEFT_EYE},
                {PoseLandmark.NOSE, PoseLandmark.RIGHT_EYE},
                {PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER},
                {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP},
                {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP},
                {PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP},
                {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW},
                {PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST},
                {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW},
                {PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST},
                {PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE},
                {PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE},
                {PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE},
                {PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE}
        };

        for (int[] conn : connections) {
            PoseLandmark a = pose.getPoseLandmark(conn[0]);
            PoseLandmark b = pose.getPoseLandmark(conn[1]);
            if (a == null || b == null) continue;
            if (a.getInFrameLikelihood() < 0.2f || b.getInFrameLikelihood() < 0.2f) continue;
            PointF pa = mapPoint(a.getPosition());
            PointF pb = mapPoint(b.getPosition());
            canvas.drawLine(pa.x, pa.y, pb.x, pb.y, connectionPaint);
        }
    }
}
