package com.cwq.retrofitlibrary;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author CWQ
 * @date 2019-10-12
 */
public class RequestBuilder {

    private final String method;
    private final HttpUrl baseUrl;
    private String relativeUrl;
    private HttpUrl.Builder urlBuilder;
    private FormBody.Builder formBuilder;
    private final Request.Builder requestBuilder;


    public RequestBuilder(String method, HttpUrl baseUrl, String relativeUrl, boolean hasBody) {
        this.method = method;
        this.baseUrl = baseUrl;
        this.relativeUrl = relativeUrl;

        requestBuilder = new Request.Builder();
        if (hasBody) {
            formBuilder = new FormBody.Builder();
        }
    }

    public void addQueryParam(String name, String value) {
        if (relativeUrl != null) {
            //baseUrl + /ip/ipNew
            //www.juhe.cn/ip/ipNew
            urlBuilder = baseUrl.newBuilder(relativeUrl);

            relativeUrl = null;
        }

        urlBuilder.addQueryParameter(name, value);
    }

    public void addFormField(String name, String value) {
        formBuilder.add(name, value);
    }


    Request build() {
        //定义局部变量。1，保证每次值不一样 2，易回收
        HttpUrl url;
        if (urlBuilder != null) {
            url = urlBuilder.build();
        } else {
            url = baseUrl.resolve(relativeUrl);
            if (url == null) {
                throw new IllegalArgumentException("Malformed URL. Base: " + baseUrl + ",Relative:" + relativeUrl);
            }
        }

        //如果有请求体，构造方法中会初始化Form表单构建者，然后再实例化请求体
        RequestBody body = null;
        if (formBuilder != null) {
            body = formBuilder.build();
        }

        //构建完整请求
        return requestBuilder
                .url(url)
                .method(method, body)
                .build();
    }
}
