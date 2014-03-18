package com.lythin.wakeup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopAlarmReceiver extends BroadcastReceiver {
    Intent intentService;
    Intent intentActivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.DB.isAlarmInProgress()) {
            //Disable DB.alarmOn
            MainActivity.DB.setAlarmInProgress(false,null);

            //Send killService broadcast
            intentService = new Intent(AlarmOnService.KILL_SERVICE_INTENT);
            context.sendBroadcast(intentService);

            //Send killActivity broadcast
            intentActivity = new Intent(AlarmOnActivity.KILL_ACTIVITY_INTENT);
            context.sendBroadcast(intentActivity);
        }
    }
}
