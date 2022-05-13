package com.ywl01.bing.net;


import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by ywl01 on 2017/1/21.
 */

public interface DataBaseServices {
    @FormUrlEncoded
    @POST(Urls.SQL_URL)
    Observable<String> getResult(@Field("action") String action, @Field("sql") String sql);
}
