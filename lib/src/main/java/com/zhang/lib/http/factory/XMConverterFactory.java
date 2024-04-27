package com.zhang.lib.http.factory;

import androidx.annotation.NonNull;

import com.zhang.lib.http.RetrofitSDK;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 解析工厂
 *
 * @author ZhangXiaoMing 2024-04-27 10:12 周六
 */
public class XMConverterFactory extends Converter.Factory {


    private final Converter.Factory mFactory;

    private XMConverterFactory() {
        mFactory = GsonConverterFactory.create();
    }

    private XMConverterFactory(Converter.Factory factory) {
        this.mFactory = factory;
    }

    public static XMConverterFactory create() {
        return new XMConverterFactory();
    }

    public static XMConverterFactory create(Converter.Factory factory) {
        return new XMConverterFactory(factory);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(@NonNull Type type,
                                                            @NonNull Annotation[] annotations,
                                                            @NonNull Retrofit retrofit) {
        Converter<ResponseBody, ?> converter = mFactory.responseBodyConverter(type, annotations, retrofit);

        return (Converter<ResponseBody, Object>) responseBody -> {
            analyseResponseBody(responseBody);

            if (type == String.class || converter == null)
                return responseBody.string();

            return converter.convert(responseBody);
        };
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(@NonNull Type type,
                                                          @NonNull Annotation[] parameterAnnotations,
                                                          @NonNull Annotation[] methodAnnotations,
                                                          @NonNull Retrofit retrofit) {
        return mFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }

    /** 分析请求结果 */
    private void analyseResponseBody(ResponseBody body) {
        if (body == null)
            return;

        String content;
        try {
            content = body.string();
        } catch (Exception e) {
            e.printStackTrace();
            content = "";
        }

        RetrofitSDK.getInstance().analyseResponseContent(content);
    }


}
