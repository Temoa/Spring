package me.temoa.spring;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Lai
 * on 2017/9/5 0:12
 */

public class ThemeUtil {

    private static final String THEME_NIGHT = "theme_night";

    public static void setTheme(Context context, boolean isNight) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (isNight) {
            sharedPreferences
                    .edit()
                    .putBoolean(THEME_NIGHT, true)
                    .apply();
        } else {
            sharedPreferences
                    .edit()
                    .putBoolean(THEME_NIGHT, false)
                    .apply();
        }
    }

    public static boolean getCurTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(THEME_NIGHT, false);
    }
}
