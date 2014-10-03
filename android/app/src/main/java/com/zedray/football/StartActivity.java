package com.zedray.football;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


public class StartActivity extends Activity implements View.OnTouchListener {

    private final static String TAG = "Football";
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("7738b4bb-82a0-40f0-a0e1-a71c2017a98f");

    private enum KEYS {
        STATE,
        COUNTER,
    }

    private Button mALeft, mASelect, mARight, mBLeft, mBSelect, mBRight;
    private LinearLayout mBackdrop;
    private int mState = 4;

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.w(TAG, "handler() Do I need to do anything here to refresh the UI??");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mALeft = (Button) findViewById(R.id.a_left);
        mASelect = (Button) findViewById(R.id.a_select);
        mARight = (Button) findViewById(R.id.a_right);
        mALeft.setOnTouchListener(this);
        mASelect.setOnTouchListener(this);
        mARight.setOnTouchListener(this);
        mBLeft = (Button) findViewById(R.id.b_left);
        mBSelect = (Button) findViewById(R.id.b_select);
        mBRight = (Button) findViewById(R.id.b_right);
        mBackdrop = (LinearLayout) findViewById(R.id.backdrop);
        if (PebbleKit.isWatchConnected(this)) {
            mBackdrop.setBackgroundResource(R.color.green);
        } else {
            mBackdrop.setBackgroundResource(R.color.red);
        };
        // Listen for Pebble connections.
        PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.w(TAG, "PebbleConnectedReceiver.onReceive() Pebble connected!");
                mBackdrop.setBackgroundResource(R.color.green);
            }
        });
        PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.w(TAG, "PebbleConnectedReceiver.onReceive() Pebble disconnected!");
                mBackdrop.setBackgroundResource(R.color.red);
            }
        });

        // Handle incoming data.
        PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                mState = data.getInteger(KEYS.STATE.ordinal()).intValue();
                int counter = data.getInteger(KEYS.COUNTER.ordinal()).intValue();
                Log.w(TAG, "Received value " + mState + "  (" + counter + ")");
                PebbleKit.sendAckToPebble(context, transactionId);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUi();
                    }
                });
            }
        });

        updateUi();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case 0: // Button pressed.
                switch (view.getId()) {
                    case R.id.a_left:
                        mALeft.setBackgroundResource(R.color.red);
                        break;
                    case R.id.a_select:
                        mASelect.setBackgroundResource(R.color.red);
                        break;
                    case R.id.a_right:
                        mARight.setBackgroundResource(R.color.red);
                        break;
                    default:
                        break;
                }
                break;

            case 1: // Button released.
                switch (view.getId()) {
                    case R.id.a_left:
                        mALeft.setBackgroundResource(R.color.green);
                        break;
                    case R.id.a_select:
                        mASelect.setBackgroundResource(R.color.green);
                        break;
                    case R.id.a_right:
                        mARight.setBackgroundResource(R.color.green);
                        break;
                    default:
                        break;
                }
                break;
        }
        return true;
    }

    private void updateUi() {
        if ((mState & 1) != 0) {
            mBLeft.setBackgroundResource(R.color.blue);
        } else {
            mBLeft.setBackgroundResource(R.color.green);
        }
        if ((mState & 2) != 0) {
            mBSelect.setBackgroundResource(R.color.blue);
        } else {
            mBSelect.setBackgroundResource(R.color.green);
        }
        if ((mState & 4) != 0) {
            mBRight.setBackgroundResource(R.color.blue);
        } else {
            mBRight.setBackgroundResource(R.color.green);
        }
    }
}
