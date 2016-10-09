package com.sketchapp.prakriti.sketch;

// Imports for Drawing App
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

// Imports for Fingerprint API
import java.util.ArrayList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;
import com.samsung.android.sdk.pass.SpassInvalidStateException;

public class MainActivity extends Activity {

    // ========================== Drawing App variables ==========================================//
    private DrawingView drawView;
    private ImageButton currPaint;

    // ====================== Fingerprint related variables=======================================//

    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private Context mContext;
    private ArrayList<Integer> designatedFingers = null;
    private boolean needRetryIdentify = false;
    private boolean onReadyIdentify = false;
    private boolean isFeatureEnabled_fingerprint = false;
    private boolean isFeatureEnabled_index = false;

    // ====================== Set up Broadcast Receiver ==========================================//

    private BroadcastReceiver mPassReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SpassFingerprint.ACTION_FINGERPRINT_RESET.equals(action)) {
                Toast.makeText(mContext, "all fingerprints are removed", Toast.LENGTH_SHORT).show();
            } else if (SpassFingerprint.ACTION_FINGERPRINT_REMOVED.equals(action)) {
                int fingerIndex = intent.getIntExtra("fingerIndex", 0);
                Toast.makeText(mContext, fingerIndex + " fingerprints is removed", Toast.LENGTH_SHORT).show();
            } else if (SpassFingerprint.ACTION_FINGERPRINT_ADDED.equals(action)) {
                int fingerIndex = intent.getIntExtra("fingerIndex", 0);
                Toast.makeText(mContext, fingerIndex + " fingerprints is added", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_RESET);
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_REMOVED);
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_ADDED);
        mContext.registerReceiver(mPassReceiver, filter);
    }

    private void unregisterBroadcastReceiver() {
        try {
            if (mContext != null) {
                mContext.unregisterReceiver(mPassReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetAll() {
        designatedFingers = null;
        needRetryIdentify = false;
        onReadyIdentify = false;
    }

    // ====================== Set up Spass Fingerprint listener Object ===========================//

    private SpassFingerprint.IdentifyListener mIdentifyListener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            int FingerprintIndex = 0;
            String FingerprintGuideText = null;
            try {
                FingerprintIndex = mSpassFingerprint.getIdentifiedFingerprintIndex();
            } catch (IllegalStateException ise) {
            }
            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                if (FingerprintIndex == 1)
                {
                    drawView.clearCanvas();
                }
                else if (FingerprintIndex == 2)
                {
                    drawView.eraser();
                }
            }
            else if (eventStatus == SpassFingerprint.STATUS_TIMEOUT_FAILED) {
            }
            else if (eventStatus == SpassFingerprint.STATUS_QUALITY_FAILED) {
                needRetryIdentify = true;
                FingerprintGuideText = mSpassFingerprint.getGuideForPoorQuality();
                Toast.makeText(mContext, FingerprintGuideText, Toast.LENGTH_SHORT).show();
            }
            else {
                needRetryIdentify = true;
            }
            if (!needRetryIdentify) {
                resetIdentifyIndex();
            }
        }

        @Override
        public void onReady() {
        }

        @Override
        public void onStarted() {
        }

        @Override
        public void onCompleted() {
            onReadyIdentify = false;
            if (needRetryIdentify) {
                needRetryIdentify = false;
            }
        }
    };

    // ==================================== Activity functions ================================== //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Drawing App onCreate()
        drawView = (DrawingView) findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.topMenu);
        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        // Fingerprint API onCreate()
        mContext = this;
        mSpass = new Spass();

        try {
            mSpass.initialize(MainActivity.this);
        } catch (SsdkUnsupportedException e) {
        } catch (UnsupportedOperationException e) {
        }
        isFeatureEnabled_fingerprint = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);

        if (isFeatureEnabled_fingerprint) {
            mSpassFingerprint = new SpassFingerprint(MainActivity.this);
        } else {
            return;
        }

        isFeatureEnabled_index = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_FINGER_INDEX);

        registerBroadcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
        resetAll();
    }

    // ============================ Fingerprint functions ====================== //

    private void startIdentify() {
        if (onReadyIdentify == false) {
            try {
                onReadyIdentify = true;
                if (mSpassFingerprint != null) {
                    setIdentifyIndex();
                    mSpassFingerprint.startIdentify(mIdentifyListener);
                }
            } catch (SpassInvalidStateException ise) {
                onReadyIdentify = false;
                resetIdentifyIndex();
            } catch (IllegalStateException e) {
                onReadyIdentify = false;
                resetIdentifyIndex();
            }
        }
    }

    private void setIdentifyIndex() {
        if (isFeatureEnabled_index) {
            if (mSpassFingerprint != null && designatedFingers != null) {
                mSpassFingerprint.setIntendedFingerprintIndex(designatedFingers);
            }
        }
    }

    private void resetIdentifyIndex() {
        designatedFingers = null;
    }

    // ============================= Drawing App button functions =============================== //
    public void clearCanvas(View v) {
        drawView.clearCanvas();
    }

    public void eraserBrush(View v) {
        drawView.eraser();
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

    public void fingerprintRead(View v) {
        startIdentify();
    }
}
