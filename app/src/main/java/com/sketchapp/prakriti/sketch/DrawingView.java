package com.sketchapp.prakriti.sketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DrawingView extends View {
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;
    // Array of paths drawn
    private ArrayList<Path> paths = new ArrayList<Path>();
    // Hashmap to map each path with its color
    private Map<Path, Integer> colorsMap = new HashMap<Path, Integer>();
    private Map<Path, Integer> strokeWidthMap = new HashMap<Path, Integer>();
    public static int selectedColor;
    public static int strokeWidth;
    Context context;

    public DrawingView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setColor(Color.BLACK);
        selectedColor = Color.BLACK;
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeWidth(25);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        for (Path p : paths) {
            drawPaint.setColor(colorsMap.get(p));
            drawPaint.setStrokeWidth(strokeWidthMap.get(p));
            canvas.drawPath(p, drawPaint);
        }

        drawPaint.setColor(selectedColor);
        drawPaint.setStrokeWidth(strokeWidth);
        canvas.drawPath(drawPath, drawPaint);
    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    // when ACTION_UP stop touch
    private void upTouch() {
        drawPath.lineTo(mX, mY);
        paths.add(drawPath);
        colorsMap.put(drawPath, selectedColor);
        strokeWidthMap.put(drawPath, strokeWidth);
        drawPath = new Path();
        drawPath.reset();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                break;
        }
        invalidate();
        return true;
    }

    // Button Actions
    public void clearCanvas() {
        if (drawPath != null) {
            paths.clear();
        }
        invalidate();
    }

    public void eraser() {
        selectedColor = 0xFFFFFFFF;
        strokeWidth = 75;
    }

    public void setColor(String newColor) {
        selectedColor = Color.parseColor(newColor);
        strokeWidth = 25;
    }
}