package me.temoa.spring.network;

import me.temoa.spring.bean.Gank;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Lai
 * on 2017/8/31 21:44
 */

public interface GankApi {

    @GET("data/福利/10/{page}")
    Observable<Gank> get(@Path("page") int page);
}
