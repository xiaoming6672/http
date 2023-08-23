package com.zhang.lib.http.inteceptor;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zhang.lib.http.RetrofitSDK;
import com.zhang.lib.http.constant.RetrofitConstant;
import com.zhang.library.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Retrofit日志打印拦截器
 *
 * @author ZhangXiaoMing 2023-05-16 10:08 周二
 */
public class RetrofitLogInterceptor implements Interceptor {

    private static final String TAG = "RetrofitHttp";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        LogUtils.info(RetrofitConstant.TAG, "RetrofitLogInterceptor>>>intercept()");

        if (RetrofitSDK.getInstance().isRelease())
            return chain.proceed(chain.request());

        Request request = chain.request();

        long before = System.currentTimeMillis();
        Response response = chain.proceed(request);
        long after = System.currentTimeMillis();
        long costTime = after - before;

        long responseCostTime = response.receivedResponseAtMillis() - response.sentRequestAtMillis();

        LogUtils.debug(TAG, "Retrofit请求>>>url = " + request.url().toString().trim()
                + "\n 整体请求耗时(包含拦截器耗时)=" + (costTime)
                + "  请求耗时=" + responseCostTime
                + "\n 请求参数=" + getRequestContent(request)
                + "\n 请求结果=" + getResponseContent(response)
        );

        return response;
    }


    /**
     * 打印请求头
     *
     * @param request 请求的对象
     */
    private String getRequestHeaders(Request request) {
        if (request == null)
            return "";

        return request.headers().toString();
    }

    /**
     * 打印请求消息
     *
     * @param request 请求的对象
     */
    private String getRequestContent(Request request) {
        if (request == null)
            return "无";

        RequestBody requestBody = request.body();
        if (requestBody == null)
            return "无";

        String content = "";

        try {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            content = buffer.readUtf8();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(content))
            content = "无";

        return content;
    }

    /**
     * 打印返回消息
     *
     * @param response 返回的对象
     */
    private String getResponseContent(Response response) {
        if (response == null || !response.isSuccessful())
            return "";

        ResponseBody responseBody = response.body();
        if (responseBody == null)
            return "";

        long contentLength = responseBody.contentLength();
        BufferedSource source = responseBody.source();

        try {
            source.request(Long.MAX_VALUE); // Buffer the entire body.
        } catch (IOException e) {
            e.printStackTrace();
        }

        String content = "";

        Buffer buffer = source.getBuffer();
        if (contentLength != 0)
            content = buffer.clone().readUtf8();

        try {
            if (isJsonFormat(content))
                content = new JSONTokener(content).nextValue().toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return content;
    }

    /**
     * 判断是否是Json格式
     *
     * @param content 请求数据
     */
    private boolean isJsonFormat(String content) {
        if (TextUtils.isEmpty(content))
            return false;

        return (content.startsWith("[") && content.endsWith("]"))
                || (content.startsWith("{") && content.endsWith("}"));
    }
}
