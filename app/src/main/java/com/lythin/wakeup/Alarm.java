package com.lythin.wakeup;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Lakhshya on 2/27/14.
 */
public class Alarm implements Comparable<Alarm>{
    /**
     * Time in minutes of the day
     */
    private int time;
    private String quote;
    private boolean enabled;
    private int hour, minute;
    private String ampm;

    Alarm(int time, String quote, boolean enabled) {
        setTime(time);
        setQuote(quote);
        setEnabled(enabled);


    }

    public long getTimeInMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        long timeM = c.getTimeInMillis();
        timeM += time * 60 * 1000;
        c = Calendar.getInstance();
        if (timeM <= c.getTimeInMillis()) {
            timeM += 25 * 60 * 60 * 1000;
        }
        return timeM;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getAmpm() {
        return ampm;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
        hour = (time / 60) % 12;
        minute = time % 60;
        if (hour == 0)
            hour = 12;
        if (time < 720)
            ampm = "AM";
        else
            ampm = "PM";
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public PendingIntent generatePI(Context context) {
        Intent intent = new Intent(context, StartAlarmReceiver.class);
        intent.putExtra(Intent.EXTRA_TEXT, quote);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, time, intent, 0);
        return pendingIntent;
    }

    @Override
    public int compareTo(Alarm alarm) {
        return this.getTime()-alarm.getTime();
    }
}