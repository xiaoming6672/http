package com.zhang.lib.http.inteceptor;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zhang.lib.http.RetrofitSDK;
import com.zhang.lib.http.bean.RequestParamVo;
import com.zhang.lib.http.constant.RetrofitConstant;
import com.zhang.library.utils.LogUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 将请求表单转换成JSON格式的表单
 *
 * @author ZhangXiaoMing 2023-05-21 17:48 周日
 */
public class RequestBodyTransformationInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        LogUtils.info(RetrofitConstant.TAG, "RequestBodyTransformationInterceptor>>>intercept()");

        Request request = chain.request();
        RequestBody requestBody = request.body();

        if ("get".equalsIgnoreCase(request.method())
                || !(requestBody instanceof FormBody))
            return chain.proceed(request);

        FormBody body = (FormBody) requestBody;
        RequestBody newBody = RetrofitSDK.getInstance().hasNewRequestBodyBuilder() ? getNewBody(body) : getNewBodyDefault(body);

        return chain.proceed(request.newBuilder()
                .method(request.method(), newBody)
                .build());
    }

    /**
     * 获取新的请求体方法
     *
     * @param body 请求体
     */
    private RequestBody getNewBody(FormBody body) {
        Map<String, Object> map = new HashMap<>();

        for (int index = 0; index < body.size(); index++) {
            String key = body.name(index);
            String value = body.value(index);

            if (TextUtils.isEmpty(key)
                    || TextUtils.isEmpty(value))
                continue;

            map.put(key, value);
        }

        return RetrofitSDK.getInstance().newRequestBody(map);
    }

    /**
     * 默认获取新的请求体方法
     *
     * @param body 请求体
     */
    private RequestBody getNewBodyDefault(FormBody body) {
        RequestParamVo params = RequestParamVo.obtain();

        Map<String, Object> map = new HashMap<>();
        for (int index = 0; index < body.size(); index++) {
            String key = body.name(index);
            String value = body.value(index);

            if (TextUtils.isEmpty(key)
                    || TextUtils.isEmpty(value))
                continue;

            if (key.equals("page") || key.equals("pageSize")) {
                if (key.equals("page"))
                    params.setPage(Integer.parseInt(value));
                else
                    params.setPageSize(Integer.parseInt(value));

                continue;
            }

            map.put(key, value);
        }

        params.setData(map);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        return FormBody.create(params.toJsonString(), mediaType);
    }
}
