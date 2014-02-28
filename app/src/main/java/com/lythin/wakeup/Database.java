package com.lythin.wakeup;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class Database {

    public static final String PREFS_NAME = "DATABASE_PREFS";
    public static final String CHECK_FIRST_RUN = "CHECK_FIRST_RUN";
    public static final String QUOTE = "QUOTE";
    public static final String NO_OF_QUOTES = "NO_OF_QUOTES";
    public static final String ALARM_TIME = "ALARM_TIME";
    public static final String ALARM_QUOTE = "ALARM_QUOTE";
    public static final String ALARM_ENABLED = "ALARM_ENABLED";
    public static final String NO_OF_ALARMS = "NO_OF_ALARMS";

    private ArrayList<String> quotes;
    private Context context;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private ArrayList<Alarm> alarms;
    private boolean alarmInProgress;

    private Database(Context ctx) {
        setContext(ctx);
        settings = context.getSharedPreferences(PREFS_NAME, 0);
        editor = settings.edit();

        boolean firstRun = settings.getBoolean(CHECK_FIRST_RUN, true);
        quotes = new ArrayList<String>();
        alarms = new ArrayList<Alarm>();
        alarmInProgress = false;


        if (firstRun) {
            editor.putBoolean(CHECK_FIRST_RUN, false);
            editor.putInt(NO_OF_QUOTES, 0);
            editor.putInt(NO_OF_ALARMS, 0);
            editor.commit();
            initializeQuotesAndAlarms();

        } else {
            int num = settings.getInt(NO_OF_QUOTES, 0);
            for (int i = 0; i < num; i++)
                quotes.add(settings.getString(QUOTE + i, null));
            num = settings.getInt(NO_OF_ALARMS, 0);
            for (int i = 0; i < num; i++) {
                alarms.add(new Alarm(settings.getInt(ALARM_TIME + i, 0), settings.getString(ALARM_QUOTE + i, null), settings.getBoolean(ALARM_ENABLED + i, false)));
            }
        }
    }

    public static Database revive(Database db, Context context) {
        if (db == null)
            return new Database(context);
        db.setContext(context);
        return db;
    }

    public ArrayList<Alarm> getAlarms() {
        return alarms;
    }

    public Alarm getAlarm(int position) {
        if (position < 0)
            return alarms.get(alarms.size() - 1);
        return alarms.get(position);
    }

    public boolean addAlarm(int time, String quote, boolean enabled) {
        for (Alarm alarm : alarms) {
            if (alarm.getTime() == time) {
                return false;
            }
        }
        int num = settings.getInt(NO_OF_ALARMS, 0);
        editor.putInt(ALARM_TIME + num, time);
        editor.putString(ALARM_QUOTE + num, quote);
        editor.putBoolean(ALARM_ENABLED + num, enabled);
        editor.putInt(NO_OF_ALARMS, num + 1);
        boolean res = editor.commit();
        alarms.add(new Alarm(time, quote, enabled));
        return res;
    }

    public void registerAlarmsWithManager(AlarmManager alarmMgr, Context context) {
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), 24 * 60 * 60 * 1000, alarm.generatePI(context));
            }
        }
    }

    public boolean isAlarmInProgress() {
        return alarmInProgress;
    }

    public void setAlarmInProgress(boolean alarmInProgress) {
        this.alarmInProgress = alarmInProgress;
    }

    public boolean setAlarmEnabled(int position, boolean enabled) {
        editor.putBoolean(ALARM_ENABLED + position, enabled);
        boolean res = editor.commit();
        alarms.get(position).setEnabled(enabled);
        return res;
    }

    public boolean editAlarm(int position, int time, String quote, boolean enabled) {
        editor.putInt(ALARM_TIME + position, time);
        editor.putString(ALARM_QUOTE + position, quote);
        editor.putBoolean(ALARM_ENABLED + position, enabled);
        boolean res = editor.commit();
        Alarm alarm = alarms.get(position);
        alarm.setQuote(quote);
        alarm.setTime(time);
        alarm.setEnabled(enabled);
        return res;
    }

    public boolean removeAlarm(int position) {
        int num = settings.getInt(NO_OF_ALARMS, 0);
        editor.putInt(ALARM_TIME + position, settings.getInt(ALARM_TIME + (num - 1), 0));
        editor.putString(ALARM_QUOTE + position, settings.getString(ALARM_QUOTE + (num - 1), null));
        editor.remove(ALARM_QUOTE + (num - 1));
        editor.remove(ALARM_TIME + (num - 1));
        editor.putInt(NO_OF_ALARMS, num - 1);
        boolean res = editor.commit();
        alarms.remove(position);
        return res;
    }

    private void initializeQuotesAndAlarms() {
        addNewQuote("A");
        addNewQuote("B");
        addNewQuote("C");
        addAlarm(500, quotes.get(0), false);
        addAlarm(1000, quotes.get(2), true);
    }

    public ArrayList<String> getQuotes() {
        return (ArrayList<String>) quotes.clone();
    }

    public ArrayList<String> getQuotesRef() {
        return quotes;
    }

    public boolean addNewQuote(String quote) {
        int num = settings.getInt(NO_OF_QUOTES, 0);
        editor.putString(QUOTE + num, quote);
        editor.putInt(NO_OF_QUOTES, num + 1);
        boolean res = editor.commit();
        quotes.add(quote);
        return res;
    }

    public boolean editQuote(int position, String quote) {
        editor.putString(QUOTE + position, quote);
        boolean res = editor.commit();
        quotes.set(position, quote);
        return res;
    }

    public boolean removeQuote(int position) {
        int num = settings.getInt(NO_OF_QUOTES, 0);
        editor.putString(QUOTE + position, settings.getString(QUOTE + (num - 1), null));
        editor.remove(QUOTE + (num - 1));
        editor.putInt(NO_OF_QUOTES, num - 1);
        boolean res = editor.commit();
        quotes.remove(position);
        return res;
    }

    public Context getContext() {
        return context;
    }

    private void setContext(Context context) {
        this.context = context;
    }


}
