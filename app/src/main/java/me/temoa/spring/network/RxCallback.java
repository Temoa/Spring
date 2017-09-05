package me.temoa.spring.network;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import me.temoa.spring.MyApp;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

/**
 * Created by Temoa
 * on 2017/2/6 20:39
 */

public abstract class RxCallback<T> extends Subscriber<T> {

    public abstract void onSuccess(T t);

    public abstract void onFinished();

    @Override
    public void onCompleted() {
        onFinished();
    }

    @Override
    public void onError(Throwable e) {
        String errorMsg;
        if (e instanceof IOException) {
            errorMsg = "Please check your network status";
        } else if (e instanceof HttpException) {
            errorMsg = ((HttpException) e).response().message();
        } else {
            errorMsg = !TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "unknown error";
        }

        Log.e("RxCallback", "onError() called with: e = [" + e + "]," + errorMsg);
        Toast.makeText(MyApp.getInstance(), "请求失败", Toast.LENGTH_SHORT).show();
        onFinished();
    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }
}
