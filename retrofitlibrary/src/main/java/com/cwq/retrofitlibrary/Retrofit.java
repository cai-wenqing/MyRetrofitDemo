package com.cwq.retrofitlibrary;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * @author CWQ
 * @date 2019-10-11
 * 自定义Retrofit
 */
public class Retrofit {

    private HttpUrl baseUrl;
    private Call.Factory callFactory;
    /**
     * 缓存请求的方法
     *
     * @param builder
     */
    private final Map<Method, ServiceMethod> serviceMethodCache = new ConcurrentHashMap<>();

    private Retrofit(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.callFactory = builder.callFactory;
    }


    public HttpUrl getBaseUrl() {
        return baseUrl;
    }

    public Call.Factory getCallFactory() {
        return callFactory;
    }


    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                ServiceMethod serviceMethod = loadServiceMethod(method);
                return new OKHttpCall(serviceMethod, args);
            }
        });
    }

    /**
     * 获得方法中所有内容，包括方法名、注解、参数、参数注解等
     *
     * @param method
     * @return
     */
    private ServiceMethod loadServiceMethod(Method method) {
        ServiceMethod result = serviceMethodCache.get(method);
        if (result != null) {
            return result;
        }
        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new ServiceMethod.Builder(this, method).build();
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }


    public static class Builder {

        private HttpUrl baseUrl;

        private Call.Factory callFactory;


        public Builder baseUrl(String baseUrl) {
            if (baseUrl.isEmpty()) {
                throw new NullPointerException("baseUrl == null");
            }
            this.baseUrl = HttpUrl.parse(baseUrl);
            return this;
        }


        public Builder baseUrl(HttpUrl baseUrl) {
            if (baseUrl == null) {
                throw new NullPointerException("baseUrl == null");
            }
            this.baseUrl = baseUrl;
            return this;
        }


        public Builder callFactory(Call.Factory callFactory) {
            this.callFactory = callFactory;
            return this;
        }

        public Retrofit build() {
            if (this.baseUrl == null) {
                throw new IllegalStateException("BaseUrl required.");
            }

            if (this.callFactory == null) {
                callFactory = new OkHttpClient();
            }

            return new Retrofit(this);
        }

    }
}
