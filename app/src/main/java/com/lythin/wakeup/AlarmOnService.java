package com.lythin.wakeup;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class AlarmOnService extends Service {

    public static final String KILL_SERVICE_INTENT = "KILL_SERVICE_INTENT";
    static MediaPlayer mediaPlayer;
    AudioManager am;
    Handler handler;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int res = super.onStartCommand(intent, flags, startId);

        Log.i("Test", "Service 1");
        //Register the kill register
        registerReceiver(killServiceReceiver, new IntentFilter(KILL_SERVICE_INTENT));
        Log.i("Test", "Service 2");
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        playSound();
        Log.i("Test", "Service 3");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                playSound();
            }

        };

        new Thread(new Runnable() {
            public void run() {
                while (MainActivity.DB.isAlarmInProgress()) {
                    try {
                        handler.sendEmptyMessage(0);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
                stopAlarm();
            }
        }).start();
        Log.i("Test", "Service 4");

        return res;
    }

    private void stopAlarm() {
        //Stop media player and release resources and memory
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = null;

    }

    final BroadcastReceiver killServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopAlarm();
            unregisterReceiver(killServiceReceiver);
            stopSelf();
        }
    };

    private void playSound() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.willrock);
            mediaPlayer.setLooping(true);
        }
        am.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        mediaPlayer.start();
        mediaPlayer.setScreenOnWhilePlaying(true);
    }

    private void ensureActivity() {
        handler = new Handler() {
            ActivityManager am = (ActivityManager) MainActivity.DB.getContext().getSystemService(Activity.ACTIVITY_SERVICE);

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName().toLowerCase();
                if (!packageName.equalsIgnoreCase("com.lythin.wakeup")) {
                    Toast.makeText(getApplicationContext(), packageName,
                            Toast.LENGTH_SHORT).show();
                    Log.e("abcd", packageName);
//                    startActivity(intentAlarmActivity);
                }

            }

        };

        new Thread(new Runnable() {
            public void run() {
                while (MainActivity.DB.isAlarmInProgress()) {
                    try {
                        handler.sendEmptyMessage(0);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }


}
