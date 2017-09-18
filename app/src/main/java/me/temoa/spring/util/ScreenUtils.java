package me.temoa.spring.util;

import me.temoa.spring.MyApp;

/**
 * Created by Lai
 * on 2017/9/18 12:02
 */

public class ScreenUtils {

    public static int getScreenWidth() {
        return MyApp.getInstance().getResources().getDisplayMetrics().widthPixels;
    }

    public static int dp2px(final float dpValue) {
        final float scale = MyApp.getInstance().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(final float pxValue) {
        final float scale = MyApp.getInstance().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
