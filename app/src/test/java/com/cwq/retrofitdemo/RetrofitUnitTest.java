package com.cwq.retrofitdemo;

import com.cwq.retrofitlibrary.Retrofit;
import com.cwq.retrofitlibrary.http.Field;
import com.cwq.retrofitlibrary.http.GET;
import com.cwq.retrofitlibrary.http.POST;
import com.cwq.retrofitlibrary.http.Query;

import org.junit.Test;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author CWQ
 * @date 2019-10-12
 */
public class RetrofitUnitTest {

    private final static String BASE_URL = "http://apis.juhe.cn/";
    private final static String IP = "144.34.161.97";
    private final static String KEY = "aa205eeb45aa76c6afe3c52151b52160";

    interface API {
        @GET("/ip/ipNew")
        Call get(@Query("ip") String ip, @Query("key") String key);

        @POST("/ip/ipNew")
        Call post(@Field("ip") String ip, @Field("key") String key);
    }


    @Test
    public void testRetrofit() throws Exception {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).build();
        API api = retrofit.create(API.class);

        //GET请求
        {
            Call call = api.get(IP, KEY);
            Response response = call.execute();
            if (response != null && response.body() != null) {
                System.out.println("GET请求：" + response.body().string());
            }
        }

        //POST请求
        {
            Call call = api.post(IP, KEY);
            Response response = call.execute();
            if (response != null && response.body() != null) {
                System.out.println("POST请求：" + response.body().string());
            }
        }
    }
}
