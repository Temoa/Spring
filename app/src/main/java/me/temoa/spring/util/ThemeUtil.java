package me.temoa.spring.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Lai
 * on 2017/9/5 0:12
 */

public class ThemeUtil {

    private static final String THEME_NIGHT = "theme_night";

    public static void setTheme(Context context, boolean isNight) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(THEME_NIGHT, isNight)
                .apply();
    }

    public static boolean getCurTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(THEME_NIGHT, false);
    }
}
