package me.temoa.spring.network;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import me.temoa.spring.bean.Gank;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Temoa
 * on 2017/2/6 19:35
 */

public class GankRetrofitClient {

    private static GankRetrofitClient mInstance;

    private GankApi mGankApi;

    private GankRetrofitClient() {
        initClient();
    }

    public static GankRetrofitClient getInstance() {
        if (mInstance == null) {
            synchronized (GankRetrofitClient.class) {
                if (mInstance == null) {
                    mInstance = new GankRetrofitClient();
                }
            }
        }
        return mInstance;
    }

    private void initClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                // 默认打开错误重连和支持https
                .retryOnConnectionFailure(true)
                .connectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS));

        builder.addInterceptor(new HeaderInterceptor());

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://gank.io/api/")
                .build();

        mGankApi = retrofit.create(GankApi.class);
    }

    public void get(int page, RxCallback<Gank> callback) {
        mGankApi.get(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback);
    }
}
