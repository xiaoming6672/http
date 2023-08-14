package com.zhang.lib.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zhang.lib.http.constant.RetrofitConstant;
import com.zhang.library.utils.LogUtils;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * 请求加密拦截器
 *
 * @author ZhangXiaoMing 2023-05-19 20:27 周五
 */
class RequestEncryptionInterceptor implements Interceptor {

    private static final String TAG = "RequestEncryptionInterceptor";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if (!RetrofitSDK.getInstance().hasEncryptor())
            return chain.proceed(chain.request());

        LogUtils.info(RetrofitConstant.TAG, "RequestEncryptionInterceptor>>>intercept()");

        Request request = chain.request();

        RequestBody body = request.body();
        if (body == null)
            return chain.proceed(request);

        Buffer bufferedSink = new Buffer();
        body.writeTo(bufferedSink);
        String params = bufferedSink.readUtf8();

        String url = request.url().toString().trim();
        String result = RetrofitSDK.getInstance().encrypt(url, params);
        LogUtils.debug(TAG, "RequestEncryptionInterceptor>>>加密前：%s ， 加密后：%s", params, result);

        if (TextUtils.isEmpty(result) || TextUtils.equals(result, params))
            return chain.proceed(request);

        RequestBody newBody = FormBody.create(result, body.contentType());
        return chain.proceed(request.newBuilder()
                .method(request.method(), newBody)
                .build());
    }
}
