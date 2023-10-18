package com.zhang.lib.http.inteceptor;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.zhang.lib.http.constant.RetrofitConstant;
import com.zhang.library.utils.LogUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 请求异常的时候发起重试
 *
 * @author ZhangXiaoMing 2023-09-20 15:07 周三
 */
public class RequestRetryInterceptor implements Interceptor {


    private final int mMaxRetryCount;

    public RequestRetryInterceptor(@IntRange(from = 1) int maxRetryCount) {
        this.mMaxRetryCount = Math.max(1, maxRetryCount);
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Response response = null;
        Exception exception = null;
        int count = 0;
        while (count < mMaxRetryCount) {
            try {
                response = chain.proceed(request);
                break;
            } catch (Exception e) {
                count++;
                exception = e;
            }
        }

        LogUtils.debug(RetrofitConstant.TAG, "RequestRetryInterceptor>>>%s请求1次，重试%d次，请求异常：%s",
                request.url().toString(), count, exception);

        if (response == null)
            throw new IOException(request.url() + "请求异常" + (exception == null ? "" : exception.toString()));

        return response;
    }
}
