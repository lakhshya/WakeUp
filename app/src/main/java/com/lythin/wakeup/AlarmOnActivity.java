package com.lythin.wakeup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AlarmOnActivity extends Activity {

    public static final String KILL_ACTIVITY_INTENT = "KILL_ACTIVITY_INTENT";
    final BroadcastReceiver killActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    Button button;
    TextView textView;
    EditText editText;
    private String quote;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        //Register the kill register
        registerReceiver(killActivityReceiver, new IntentFilter(KILL_ACTIVITY_INTENT));

        //Get quote from intent
        quote = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        //Initialize GUI components
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        textView.setText(quote);
        editText = (EditText) findViewById(R.id.editText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().equals(quote)) {
                    Intent intent = new Intent(AlarmOnActivity.this, StopAlarmReceiver.class);
                    sendBroadcast(intent);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(killActivityReceiver);
        Log.i("Test", "AlarmOnActivity.onDestroy");
    }

}
