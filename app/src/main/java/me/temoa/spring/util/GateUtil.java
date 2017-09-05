package me.temoa.spring.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Lai
 * on 2017/9/5 12:23
 */

public class GateUtil {

    private static final String GATE_NEW = "gate_new";

    public static void setGate(Context context, boolean isNewWorld) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(GATE_NEW, isNewWorld)
                .apply();
    }

    public static boolean getCurGate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(GATE_NEW, false);
    }
}
