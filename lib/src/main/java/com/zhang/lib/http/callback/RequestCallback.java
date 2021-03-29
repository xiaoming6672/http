package com.zhang.lib.http.callback;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 网络请求回调
 *
 * @author ZhangXiaoMing 2021-03-29 20:57 星期一
 */
public abstract class RequestCallback<T> {

    public RequestCallback() {
    }

    public final Type getType() {
        Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        return types[0];
    }

    public abstract void onSuccess(T response);

    public abstract void onError(Exception error);

    public void onFinish() {
    }
}
