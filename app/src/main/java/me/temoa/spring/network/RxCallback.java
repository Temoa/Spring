package me.temoa.spring.network;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import me.temoa.spring.MyApp;
import retrofit2.HttpException;

/**
 * Created by Temoa
 * on 2017/2/6 20:39
 */

public abstract class RxCallback<T> implements Observer<T> {

    public abstract void onSuccess(T t);

    public abstract void onFinished();

    public abstract void getDisposable(Disposable d);

    @Override
    public void onSubscribe(Disposable d) {
        getDisposable(d);
    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
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
    public void onComplete() {
        onFinished();
    }
}
