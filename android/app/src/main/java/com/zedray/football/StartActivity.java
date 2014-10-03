package com.zedray.football;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


public class StartActivity extends Activity implements View.OnClickListener {

    private final static String TAG = "Football";
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("7738b4bb-82a0-40f0-a0e1-a71c2017a98f");

    private enum KEYS {
        STATE,
    }

    private Button mLeft;
    private Button mSelect;
    private Button mRight;
    private int mState = 0;

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.w(TAG, "handler() Do I need to do anything here to refresh the UI??");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mLeft = (Button) findViewById(R.id.left);
        mSelect = (Button) findViewById(R.id.select);
        mRight = (Button) findViewById(R.id.right);
        mLeft.setOnClickListener(this);
        mSelect.setOnClickListener(this);
        mRight.setOnClickListener(this);

        // Listen for Pebble connections.
        PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.w(TAG, "PebbleConnectedReceiver.onReceive() Pebble connected!");
            }
        });
        PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.w(TAG, "PebbleConnectedReceiver.onReceive() Pebble disconnected!");
            }
        });


        // Handle incoming data.
        PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                mState = data.getUnsignedInteger(KEYS.STATE.ordinal()).intValue();
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.left:
                Log.w(TAG, "StartActivity.onClick() Left selected");
                break;
            case R.id.select:
                Log.w(TAG, "StartActivity.onClick() Select selected");
                break;
            case R.id.right:
                Log.w(TAG, "StartActivity.onClick() Right selected");
                break;
            default:
                break;
        }
    }

    private void updateUi() {
        if ((mState & 0) != 0) {
            Log.w(TAG, "StartActivity.updateUi() 0 selected");
        }
        if ((mState & 2) != 0) {
            Log.w(TAG, "StartActivity.updateUi() 2 selected");
        }
        if ((mState & 4) != 0) {
            Log.w(TAG, "StartActivity.updateUi() 4 selected");
        }
    }
}
