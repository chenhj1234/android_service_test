package com.shadowroast.servicetest;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class TastService extends Service {
    final String TAG = getClass().getSimpleName();
    public static final int MSG_SAY_HELLO = 1000;
    public static final int MSG_MUSIC_PLAY = 1001;
    public static final int MSG_MUSIC_STOP = 1002;

    public class LocalBinder extends Binder {
        public TastService getService() {
            return  TastService.this;
        }
        public void binder_action() {
            Log.e(TAG, "binder_action");
        }
    }
    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return mMessenger.getBinder();
    }
    private MediaPlayer mp;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        handler.postDelayed(runnable, 2000);
//        mp = new MediaPlayer();
    }
    @Override
    public int onStartCommand (Intent intent,
                               int flags,
                               int startId) {
        Log.e(TAG, "onStartCommand");
//        String songPath= intent.getExtras().getString("songPath");
//        try{
//            mp.setDataSource(songPath);
//            mp.prepare();
//            mp.start();
//        }catch(Exception e){};
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onStart(Intent intent, int startId) {
        Log.e(TAG, "onStart");
        super.onStart(intent, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainService onDestroy");
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "MainService onUnbind");
        return super.onUnbind(intent);
    }
    // Action Tester
    public void service_test() {
        Log.e(TAG, "service test");
    }
    // Action Worker
    int count = 0;
    Handler handler = new Handler();
    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            count ++;
            Log.e(TAG, "Count:" + count);
            handler.postDelayed(this, 2000);
        }
    };
    private void startPlay() {
        Log.e(TAG, "Action Play music");
    }
    private void stopPlay() {
        Log.e(TAG, "Action Stop music");
    }
    private Messenger mActivityMessenger = null;
    public void sendMessageToActivity(int what) {
        if(mActivityMessenger != null) {
            Message messg = Message.obtain(null, what);
            try {
                //向客户端发送消息
                mActivityMessenger.send(messg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    // Communication handler and messenger
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "msg.what="+msg.what);
            Log.i(TAG, "mHandler Thread ="+Thread.currentThread());
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "hello,remote service", Toast.LENGTH_SHORT).show();
                    //通过message对象获取客户端传递过来的Messenger对象。
                    mActivityMessenger = msg.replyTo;
                    sendMessageToActivity(MSG_SAY_HELLO);
//                    if(mActivityMessenger != null){
//                        Message messg = Message.obtain(null, MSG_SAY_HELLO);
//                        try {
//                            //向客户端发送消息
//                            mActivityMessenger.send(messg);
//                        } catch (RemoteException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    break;
                case MSG_MUSIC_PLAY:
                    //播放音乐
                    startPlay();
                    break;
                case MSG_MUSIC_STOP:
                    //停止播放
                    stopPlay();
                    break;
                default:
                    break;
            }
        }
    };
    Messenger mMessenger = new Messenger(mHandler);
}
