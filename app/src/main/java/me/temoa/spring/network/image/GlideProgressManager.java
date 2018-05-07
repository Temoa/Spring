package me.temoa.spring.network.image;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Temoa
 * on 2018/5/7.
 */
public class GlideProgressManager {

    private GlideProgressManager() {

    }

    private static final List<WeakReference<OnProgressListener>> sListeners
            = Collections.synchronizedList(new ArrayList<WeakReference<OnProgressListener>>());

    public static final OnProgressListener sChiefListener = new OnProgressListener() {
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        @Override
        public void progress(String url, int percent, boolean finish) {
            int listenerCount = sListeners.size();
            if (listenerCount == 0) return;
            for (int i = 0; i < listenerCount; i++) {
                WeakReference<OnProgressListener> weakReference = sListeners.get(i);
                final OnProgressListener listener = weakReference.get();
                final String imgUrl = url;
                final int currentPercent = percent;
                final boolean isFinish = finish;
                if (listener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.progress(imgUrl, currentPercent, isFinish);
                        }
                    });
                } else {
                    sListeners.remove(i);
                    listenerCount--;
                    i--;
                }
            }
        }
    };

    public static void addListener(@NonNull OnProgressListener listener) {
        if (hasListener(listener) == null) sListeners.add(new WeakReference<>(listener));
    }

    public static void removeListener(@NonNull OnProgressListener listener) {
        WeakReference<OnProgressListener> weakReference = hasListener(listener);
        if (weakReference != null) {
            sListeners.remove(weakReference);
        }
    }

    private static WeakReference<OnProgressListener> hasListener(
            @NonNull OnProgressListener listener) {
        int listenerCount = sListeners.size();
        if (listenerCount == 0) return null;
        for (int i = 0; i < listenerCount; i++) {
            WeakReference<OnProgressListener> l = sListeners.get(i);
            if (l.get() == listener) {
                return l;
            }
        }
        return null;
    }
}
