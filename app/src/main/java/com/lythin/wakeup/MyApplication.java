package com.lythin.wakeup;

import android.app.Application;

/**
 * Created by Lakhshya on 4/7/14.
 */
public class MyApplication extends Application {
    private static MyApplication singleton;

    public static MyApplication getInstance() {
        return singleton;
    }

    @Override
    public final void onCreate() {
        super.onCreate();
        singleton=this;
    }
}
