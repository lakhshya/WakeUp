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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class AlarmOnService extends Service {

    public static final String KILL_SERVICE_INTENT = "KILL_SERVICE_INTENT";
    private MediaPlayer mMediaPlayer;
    private int mVolume;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int res = super.onStartCommand(intent, flags, startId);

        //Register the kill register
        registerReceiver(killServiceReceiver, new IntentFilter(KILL_SERVICE_INTENT));

        playSound(this, getAlarmUri());

        return res;
    }

    private void stopAlarm() {
        //Stop media player and release resources and memory
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        mMediaPlayer = null;

    }

    private void playSound(final Context context, final Uri alert) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (mVolume == 0)
            mVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(context, alert);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                mMediaPlayer.start();
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

    }

    //Get an alarm sound. Try for an alarm. If none set, try notification,
    //Otherwise, ringtone.
    private Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
    }

    final BroadcastReceiver killServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopAlarm();
            unregisterReceiver(killServiceReceiver);
            stopSelf();
        }
    };


}
