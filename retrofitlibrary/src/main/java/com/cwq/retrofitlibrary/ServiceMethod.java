package com.cwq.retrofitlibrary;

import com.cwq.retrofitlibrary.http.Field;
import com.cwq.retrofitlibrary.http.GET;
import com.cwq.retrofitlibrary.http.POST;
import com.cwq.retrofitlibrary.http.Query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import okhttp3.Call;
import okhttp3.HttpUrl;

/**
 * @author CWQ
 * @date 2019-10-11
 * 请求方法属性封装类
 */
public class ServiceMethod {
    //OKHTTPClient唯一实现接口
    private final Call.Factory callFactory;

    //接口请求的地址
    private final HttpUrl baseUrl;

    //方法请求方式
    private final String httpMethod;

    //方法的注解的值（"/ip/ipNew"）
    private final String relativeUrl;

    //方法参数的数组
    private final ParameterHandler[] parameterHandlers;

    //是否有请求体
    private final boolean hasBody;


    private ServiceMethod(Builder builder) {
        this.callFactory = builder.retrofit.getCallFactory();
        this.baseUrl = builder.retrofit.getBaseUrl();
        this.httpMethod = builder.httpMethod;
        this.relativeUrl = builder.relativeUrl;
        this.parameterHandlers = builder.parameterHandlers;
        this.hasBody = builder.hasBody;
    }


    okhttp3.Call toCall(Object... args) {
        RequestBuilder requestBuilder = new RequestBuilder(httpMethod, baseUrl, relativeUrl, hasBody);

        ParameterHandler[] handlers = this.parameterHandlers;
        int argumentCount = args != null ? args.length : 0;
        //方法真实的参数个数是否等于收集的参数个数
        if (argumentCount != handlers.length) {
            throw new IllegalArgumentException("");
        }

        //循环拼接每个参数名+参数值
        for (int i = 0; i < argumentCount; i++) {
            handlers[i].apply(requestBuilder, args[i].toString());
        }

        //创建请求
        return callFactory.newCall(requestBuilder.build());
    }


    static final class Builder {

        final Retrofit retrofit;
        //带注解的方法
        final Method method;
        //方法的所有注解
        final Annotation[] methodAnnotations;
        //方法参数的所有注解
        final Annotation[][] parameterAnnotationsArray;
        //方法的请求方式get post
        private String httpMethod;
        //方法注解的值
        private String relativeUrl;
        //方法参数的数组（每个对象包含：参数注解值、参数值）
        private ParameterHandler[] parameterHandlers;
        //是否有请求体
        private boolean hasBody;


        public Builder(Retrofit retrofit, Method method) {
            this.retrofit = retrofit;
            this.method = method;

            this.methodAnnotations = method.getAnnotations();
            this.parameterAnnotationsArray = method.getParameterAnnotations();

        }

        ServiceMethod build() {
            //遍历方法的每个注解
            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }

            //定义方法参数的数组长度
            int parameterCount = parameterAnnotationsArray.length;
            //初始化方法参数的数组
            parameterHandlers = new ParameterHandler[parameterCount];
            //遍历方法的参数
            for (int i = 0; i < parameterCount; i++) {
                //获取方法的每个参数的多个注解
                Annotation[] parameterAnnotations = parameterAnnotationsArray[i];
                //如果参数没有任何注解
                if (parameterAnnotations == null) {
                    throw new NullPointerException("参数无注解");
                }

                //获取参数的注解值、参数值 @Field("ip") String ip
                parameterHandlers[i] = parseParameter(i, parameterAnnotations);

            }

            return new ServiceMethod(this);
        }

        //解析参数的所有注解 嵌套循环
        private ParameterHandler parseParameter(int i, Annotation[] annotations) {
            ParameterHandler result = null;
            //遍历参数的注解
            for (Annotation annotation : annotations) {
                ParameterHandler annotationAction = parseParameterAnnotation(annotation);

                if (annotationAction == null) {
                    continue;
                }
                result = annotationAction;
            }
            if (result == null) {
                throw new IllegalArgumentException("没有Retrofit注解的支持");
            }
            return result;
        }

        //解析参数的注解
        private ParameterHandler parseParameterAnnotation(Annotation annotation) {
            if (annotation instanceof Query) {
                Query query = (Query) annotation;
                //参数注解的值
                String name = query.value();
                return new ParameterHandler.Query(name);
            } else if (annotation instanceof Field) {
                Field field = (Field) annotation;
                String name = field.value();
                return new ParameterHandler.Field(name);
            }
            return null;
        }

        /**
         * 解析方法的注解 GET/POST
         *
         * @param annotation 注解
         */
        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof GET) {
                parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
            } else if (annotation instanceof POST) {
                parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
            }
        }


        private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {
            //方法请求的方式 GET/POST
            this.httpMethod = httpMethod;
            //方法注解的值
            this.relativeUrl = value;
            //是否有请求体
            this.hasBody = hasBody;
        }
    }
}
