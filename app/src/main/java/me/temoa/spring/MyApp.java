package me.temoa.spring;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

/**
 * Created by Lai
 * on 2017/9/5 14:37
 */

public class MyApp extends Application {

    private static MyApp Instance;

    private BroadcastReceiver mSavePhotoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isSucceed = intent.getBooleanExtra("isSucceed", false);
            if (isSucceed) {
                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Instance = this;
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(mSavePhotoReceiver, new IntentFilter("SAVE_PHOTO"));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(mSavePhotoReceiver);
    }

    public static MyApp getInstance() {
        return Instance;
    }
}
