package me.temoa.spring.network;

import me.temoa.spring.bean.Jiandan;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Lai
 * on 2017/8/31 21:52
 */

public interface JiandanApi {

    @GET("/?oxwlxojflwblxbsapi=jandan.get_ooxx_comments")
    Observable<Jiandan> get(@Query("page") int page);
}
