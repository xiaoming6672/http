package com.zhang.lib.http.constant;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 接口请求中常用的MediaType枚举
 *
 * @author ZhangXiaoMing 2023-09-29 20:24 周五
 */

@Retention(RetentionPolicy.SOURCE)
@StringDef({
        RequestContentType.APPLICATION_ATOM_XML,
        RequestContentType.APPLICATION_BASE64,
        RequestContentType.APPLICATION_JAVASCRIPT,
        RequestContentType.APPLICATION_JSON,
        RequestContentType.APPLICATION_OCTET_STREAM,
        RequestContentType.APPLICATION_FORM_URL_ENCODED,
        RequestContentType.APPLICATION_XML,
        RequestContentType.MULTIPART_ALTERNATIVE,
        RequestContentType.MULTIPART_FORM_DATA,
        RequestContentType.MULTIPART_MIXED,
        RequestContentType.TEXT_CSS,
        RequestContentType.TEXT_HTML,
        RequestContentType.TEXT_PAINT,
})
public @interface RequestContentType {

    String APPLICATION_ATOM_XML = "application/atom+xml";
    String APPLICATION_BASE64 = "application/base64";
    String APPLICATION_JAVASCRIPT = "application/javascript";
    String APPLICATION_JSON = "application/json";
    String APPLICATION_OCTET_STREAM = "application/octet-stream";
    String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    String APPLICATION_XML = "application/xml";
    String MULTIPART_ALTERNATIVE = "multipart/alternative";
    String MULTIPART_FORM_DATA = "multipart/form-data";
    String MULTIPART_MIXED = "multipart/mixed";
    String TEXT_CSS = "text/css";
    String TEXT_HTML = "text/html";
    String TEXT_PAINT = "text/plain";
}
