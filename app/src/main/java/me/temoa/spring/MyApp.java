package me.temoa.spring;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Lai
 * on 2017/9/5 14:37
 */

public class MyApp extends Application {

    private static MyApp Instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Instance = this;

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }

    public static MyApp getInstance() {
        return Instance;
    }
}
