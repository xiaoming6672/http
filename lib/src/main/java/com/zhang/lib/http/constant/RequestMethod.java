package com.zhang.lib.http.constant;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Http请求方式
 *
 * @author ZhangXiaoMing 2023-09-29 20:03 周五
 */

@Retention(RetentionPolicy.SOURCE)
@StringDef({
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.HEAD,
        RequestMethod.DELETE,
        RequestMethod.OPTIONS,
        RequestMethod.TRACE,
        RequestMethod.PATCH,
        RequestMethod.CONNECT,
        RequestMethod.MKCOL,
        RequestMethod.COPY,
        RequestMethod.MOVE,
        RequestMethod.LOCK,
        RequestMethod.UNLOCK,
})
public @interface RequestMethod {

    String GET = "GET";
    String POST = "POST";
    String PUT = "PUT";
    String HEAD = "HEAD";
    String DELETE = "DELETE";
    String OPTIONS = "OPTIONS";
    String TRACE = "TRACE";
    String PATCH = "PATCH";
    String CONNECT = "CONNECT";
    String MKCOL = "MKCOL";
    String COPY = "COPY";
    String MOVE = "MOVE";
    String LOCK = "LOCK";
    String UNLOCK = "UNLOCK";
}
