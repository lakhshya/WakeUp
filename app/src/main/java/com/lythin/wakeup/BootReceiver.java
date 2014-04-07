package com.lythin.wakeup;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            MainActivity.DB = Database.getInstance();
            MainActivity.DB.setAlarmInProgress(false, null);
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            MainActivity.DB.registerAlarmsWithManager(alarmMgr, context);
        }
    }
}
