package com.lythin.wakeup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartAlarmReceiver extends BroadcastReceiver {
    Intent intentService;
    Intent intentActivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!MainActivity.DB.isAlarmInProgress()) {

            //Enabling DB.alarmOn
            MainActivity.DB.setAlarmInProgress(true);

            //Starting the Service
            intentService=new Intent(context,AlarmOnService.class);
            context.startService(intentService);

            //Starting the Activity
            intentActivity=new Intent(context,AlarmOnActivity.class);
            intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentActivity.putExtra(Intent.EXTRA_TEXT,intent.getStringExtra(Intent.EXTRA_TEXT));
            Log.i("Test", intent.getStringExtra(Intent.EXTRA_TEXT));
            context.startActivity(intentActivity);
            abortBroadcast();
        }
    }
}
