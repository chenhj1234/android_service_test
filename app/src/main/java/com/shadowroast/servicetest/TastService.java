package com.shadowroast.servicetest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.List;

public class TastService extends Service {
    final String TAG = getClass().getSimpleName();
    public static final int MSG_SAY_HELLO = 1000;
    public static final int MSG_MUSIC_PLAY = 1001;
    public static final int MSG_MUSIC_STOP = 1002;

    public class LocalBinder extends Binder {
        public TastService getService() {
            return  TastService.this;
        }
        public IBinder getMsgBinder() {
            if(mMessenger != null) {
                return mMessenger.getBinder();
            }
            return null;
        }
        public void binder_action() {
            Log.e(TAG, "binder_action");
        }
    }
    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
//        return mMessenger.getBinder();
        return mBinder;
    }
    private MediaPlayer mp;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
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
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        NotificationCompat.Builder builder;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            String CHANNEL_ID = "alex_channel";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "AlexChannel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Alex channel description");
            manager.createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        }
        else
        {
            builder = new NotificationCompat.Builder(this);
        }
        Notification notification = builder.setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();
        startForeground(1337, notification);
        handler.postDelayed(runnable, 2000);
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
                    Log.e(TAG, "hello,remote service");
                    mActivityMessenger = msg.replyTo;
                    sendMessageToActivity(MSG_SAY_HELLO);
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
                    Log.e(TAG, "Receive message " + msg.what + " , response " + (msg.what + 1));
                    mActivityMessenger = msg.replyTo;
                    sendMessageToActivity(msg.what + 1);
                    break;
            }
        }
    };
    Messenger mMessenger = new Messenger(mHandler);

    MediaRecorder mMediaRecorder = null;
    Camera mServiceCamera = null;
    SurfaceHolder mSurfaceHolder = null;
    boolean mRecordingStatus = false;
    public boolean starMediaRecording(){
        mServiceCamera = Camera.open(1);
        Camera.Parameters params = mServiceCamera.getParameters();
        mServiceCamera.setParameters(params);
        Camera.Parameters p = mServiceCamera.getParameters();

        final List<Size> listSize = p.getSupportedPreviewSizes();
        Size mPreviewSize = listSize.get(2);
        p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
        mServiceCamera.setParameters(p);
        SurfaceView sv = new SurfaceView(this);
        mSurfaceHolder = sv.getHolder();

        try {
            mServiceCamera.setPreviewDisplay(mSurfaceHolder);
            mServiceCamera.startPreview();
        }
        catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        mServiceCamera.unlock();

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(mServiceCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mMediaRecorder.setOutputFile("/sdcard/filenamevideo.mp4");
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mPreviewSize.width, mPreviewSize.height);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecordingStatus = true;

        return true;

    }

    public void stopMediaRecorder() {
        try {
            mServiceCamera.reconnect();

            mMediaRecorder.stop();
            mMediaRecorder.reset();

            mServiceCamera.stopPreview();
            mMediaRecorder.release();

            mServiceCamera.release();
            mServiceCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
