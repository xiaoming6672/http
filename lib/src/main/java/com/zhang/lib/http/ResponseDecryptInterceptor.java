package com.zhang.lib.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zhang.lib.http.constant.RetrofitConstant;
import com.zhang.library.utils.LogUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 请求参数AES解密拦截器
 *
 * @author ZhangXiaoMing 2023-05-15 22:23 周一
 */
class ResponseDecryptInterceptor implements Interceptor {

    private static final String TAG = "ResponseDecryptInterceptor";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if (!RetrofitSDK.getInstance().hasDecryptor())
            return chain.proceed(chain.request());

        LogUtils.info(RetrofitConstant.TAG, "ResponseDecryptInterceptor>>>intercept()");

        Request request = chain.request();
        Response response = chain.proceed(request);
        if (!response.isSuccessful())
            return response;

        ResponseBody body = response.body();
        if (body == null)
            return response;

        String url = request.url().toString().trim();
        String originResponse = body.string();

        String result = RetrofitSDK.getInstance().decrypt(url, originResponse);
        if (TextUtils.isEmpty(result)
            /*|| TextUtils.equals(result, originResponse)*/)
            return response;

        LogUtils.debug(TAG, "intercept()>>>before decrypt:" + originResponse);
        LogUtils.debug(TAG, "intercept()>>>after decrypt:" + result);

        ResponseBody newBody = ResponseBody.create(result, body.contentType());
        return response.newBuilder()
                .body(newBody)
                .build();
    }
}
