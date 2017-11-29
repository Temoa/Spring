package me.temoa.spring.network;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.temoa.spring.bean.Jiandan;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Lai
 * on 2017/8/31 22:10
 */

public class JiandanRetrofitClient {

    private static JiandanRetrofitClient mInstance;

    private JiandanApi mJiandanApi;

    private JiandanRetrofitClient() {
        initClient();
    }

    public static JiandanRetrofitClient getInstance() {
        if (mInstance == null) {
            synchronized (JiandanRetrofitClient.class) {
                if (mInstance == null) {
                    mInstance = new JiandanRetrofitClient();
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
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://i.jandan.net/")
                .build();

        mJiandanApi = retrofit.create(JiandanApi.class);
    }

    public void get(int page, RxCallback<Jiandan> callback) {
        mJiandanApi.get(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback);
    }
}
