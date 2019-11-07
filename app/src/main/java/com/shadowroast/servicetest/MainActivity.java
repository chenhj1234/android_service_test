package com.shadowroast.servicetest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button mBtnMsgToServ;
    Button mBtnMsgToActi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, TastService.class);
        mBtnMsgToServ = findViewById(R.id.btnSendMsgToService);
        mBtnMsgToServ.setOnClickListener(msgToServListener);
        mBtnMsgToActi = findViewById(R.id.btnSendMsgToActivity);
        mBtnMsgToActi.setOnClickListener(msgToActiListener);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    Button.OnClickListener msgToServListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mRemoteMessenger != null) {
                sendMessageToService(TastService.MSG_SAY_HELLO);
            } else {
                Log.e(TAG, "Messenger not assigned");
            }
        }
    };
    Button.OnClickListener msgToActiListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mRemoteMessenger != null) {
                sendMessageToService(100);
            } else {
                Log.e(TAG, "Messenger not assigned");
            }
            if(mService != null) {
                mService.sendMessageToActivity(109);
            }
        }
    };
    TastService mService = null;
    TastService.LocalBinder mBinder;
    String TAG = getClass().getSimpleName();
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TastService.MSG_SAY_HELLO:
                    Log.i(TAG,"MSG_SAY_HELLO");
                    break;
                default:
                    Log.i(TAG,"Receive response message " + msg.what);
                    break;
            }
        };
    };
    private Messenger mMessenger = new Messenger(mHandler);

    Messenger mRemoteMessenger = null;
    public ServiceConnection connection = new ServiceConnection() {

        // 成功與 Service 建立連線
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (TastService.LocalBinder) service;
            mService = mBinder.getService();
            mRemoteMessenger = new Messenger(mBinder.getMsgBinder());
//            mBinder = (TastService.LocalBinder)service;
//            mService = ((TastService.LocalBinder) service).getService();
//            mBinder.binder_action();
//            mService.service_test();
            Log.d(TAG, "MainActivity onServiceConnected");
        }

        // 與 Service 建立連線失敗
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mRemoteMessenger = null;
            Log.d(TAG, "MainActivity onServiceFailed");
        }
    };

    // Communication code
    void sendMessageToService(int what) {
        if (mRemoteMessenger == null)
            return;
        Message msg = Message.obtain(null, what);
        msg.replyTo = mMessenger;
        try {
            mRemoteMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
