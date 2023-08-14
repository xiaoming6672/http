package com.zhang.lib.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zhang.lib.http.constant.RetrofitConstant;
import com.zhang.library.utils.LogUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 统一添加Header拦截器
 *
 * @author ZhangXiaoMing 2023-06-12 21:02 周一
 */
class HeaderInterceptor implements Interceptor {


    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        LogUtils.info(RetrofitConstant.TAG, "HeaderInterceptor>>>intercept()");

        Request request = processAddHeader(chain.request());
        return chain.proceed(request);
    }


    /**
     * 添加统一Header
     *
     * @param request 请求
     */
    private Request processAddHeader(Request request) {
        Map<String, String> headerMap = RetrofitSDK.getInstance().generateRequestHeaders();

        Request.Builder builder = request.newBuilder();

        Set<Map.Entry<String, String>> entrySet = headerMap.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            String key = entry.getKey();
            String value = entry.getValue();

            //异常数据，不处理
            if (TextUtils.isEmpty(key)
                    || TextUtils.isEmpty(value))
                continue;

            //接口已经指定了 那么不设置
            if (!TextUtils.isEmpty(request.header(key)))
                continue;

            builder.addHeader(key, value);
        }

        return builder.build();
    }
}
