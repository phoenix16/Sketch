package com.sketchapp.prakriti.sketch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    private DrawingView drawView;
    private ImageButton currPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = (DrawingView) findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.topMenu);
        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
    }

    public void clearCanvas(View v) {
        drawView.clearCanvas();
    }

    public void paintClicked(View v) {
        if (v != currPaint) {
            ImageButton imgView = (ImageButton) v;
            String color = v.getTag().toString();
            drawView.setColor(color);

            // Update the UI to reflect the new chosen color and update currPaint button
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton)v;
        }
    }
}
