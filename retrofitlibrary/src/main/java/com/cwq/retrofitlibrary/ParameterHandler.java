package com.cwq.retrofitlibrary;

/**
 * @author CWQ
 * @date 2019-10-12
 * 用来保存参数的注解值、参数值，用于拼接最终的请求
 * @Query("ip") String ip
 */
abstract class ParameterHandler {

    /**
     * 抽象方法 外部复制和调用，自己的内部类实现了
     *
     * @param builder 请求构建者（拼装者）
     * @param value   方法的参数值
     */
    abstract void apply(RequestBuilder builder, String value);


    static final class Query extends ParameterHandler{

        //参数名
        private String name;

        Query(String name){
            if (name.isEmpty()){
                throw new NullPointerException("");
            }
            this.name = name;
        }


        @Override
        void apply(RequestBuilder builder, String value) {
            //此处的value是参数值
            if (value == null){
                return;
            }
            builder.addQueryParam(name,value);
        }
    }


    static final class Field extends ParameterHandler{

        private final String name;

        Field(String name){
            if (name.isEmpty()){
                throw new NullPointerException("");
            }
            this.name = name;
        }

        @Override
        void apply(RequestBuilder builder, String value) {
            if (value == null){
                return;
            }
            //拼接Field参数，此处name为参数注解的值，value为参数值
            builder.addFormField(name,value);
        }
    }
}
