package com.lythin.wakeup;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;

public class Database {

    public static final String PREFS_NAME = "com.lythin.wakeup.DATABASE_PREFS";
    public static final String CHECK_FIRST_RUN = "com.lythin.wakeup.CHECK_FIRST_RUN";
    public static final String QUOTE = "com.lythin.wakeup.QUOTE";
    public static final String NO_OF_QUOTES = "com.lythin.wakeup.NO_OF_QUOTES";
    public static final String ALARM_TIME = "com.lythin.wakeup.ALARM_TIME";
    public static final String ALARM_QUOTE = "com.lythin.wakeup.ALARM_QUOTE";
    public static final String ALARM_ENABLED = "com.lythin.wakeup.ALARM_ENABLED";
    public static final String NO_OF_ALARMS = "com.lythin.wakeup.NO_OF_ALARMS";
    public static final String PRESENT_QUOTE = "com.lythin.wakeup.PRESENT_QUOTE";
    private static Database instance = null;
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
            editor.putInt(NO_OF_QUOTES, 0);
            editor.putInt(NO_OF_ALARMS, 0);
            editor.commit();
            initializeQuotesAndAlarms();
            registerAlarmsWithManager((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE), ctx);
            editor.putBoolean(CHECK_FIRST_RUN, false);
            editor.commit();
        } else {
            int num = settings.getInt(NO_OF_QUOTES, 0);
            for (int i = 0; i < num; i++) {
                quotes.add(settings.getString(QUOTE + i, null));
            }
            num = settings.getInt(NO_OF_ALARMS, 0);
            for (int i = 0; i < num; i++) {
                alarms.add(new Alarm(settings.getInt(ALARM_TIME + i, 0), settings.getString(ALARM_QUOTE + i, null), settings.getBoolean(ALARM_ENABLED + i, false)));
            }
        }
    }

    public static synchronized Database getInstance(Context context) {
        if (instance == null)
            instance = new Database(context);
        else {
            instance.setContext(context);
        }
        return instance;
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

    public void setAlarmInProgress(boolean alarmInProgress, String quote) {
        this.alarmInProgress = alarmInProgress;
        if (alarmInProgress) {
            editor.putString(PRESENT_QUOTE, quote);
            editor.commit();
        }
    }

    public String getPresentQuote() {
        return settings.getString(PRESENT_QUOTE, "");
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

    public ArrayList<String> getQuotesRef() {
        return quotes;
    }

    public boolean addNewQuote(String quote) {
        boolean res = false;
        int num = settings.getInt(NO_OF_QUOTES, 0);
        int pos = Collections.binarySearch(quotes, quote);
        if (pos < 0) {
            pos = -pos - 1;
            quotes.add(pos, quote);
            for (int i = pos; i < num + 1; i++) {
                editor.putString(QUOTE + i, quotes.get(i));
            }
            editor.putInt(NO_OF_QUOTES, num + 1);
            res = editor.commit();
        }
        return res;
    }

    public boolean editQuote(int position, String quote) {
        boolean res = false;
        int posIn = Collections.binarySearch(quotes, quote);
        if (posIn < 0) {
            posIn = -posIn - 1;
            quotes.add(posIn, quote);
            quotes.remove(position);
            if (posIn > position) {
                for (int i = position; i <= posIn; i++) {
                    editor.putString(QUOTE + i, quotes.get(i));
                }
            } else {
                for (int i = posIn; i <= position; i++) {
                    editor.putString(QUOTE + i, quotes.get(i));
                }
            }
            res = editor.commit();
        }
        return res;
    }

    public boolean removeQuote(int position) {
        int num = settings.getInt(NO_OF_QUOTES, 0);
        boolean res = false;
        quotes.remove(position);
        for (int i = position; i < num - 1; i++) {
            editor.putString(QUOTE + i, quotes.get(i));
        }
        editor.putInt(NO_OF_QUOTES, num - 1);
        editor.remove(QUOTE + (num - 1));
        res = editor.commit();
        return res;
    }

    public Context getContext() {
        return context;
    }

    private void setContext(Context context) {
        this.context = context;
    }


}
