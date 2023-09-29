package com.zhang.lib.http.interfaces;

import java.util.Map;

import okhttp3.RequestBody;

/**
 * 接口请求-新的请求体构造
 *
 * @author ZhangXiaoMing 2023-09-29 20:19 周五
 */
public interface INewRequestBodyBuilder {

    /**
     * 创建新的请求体
     *
     * @param paramMap 参数合集
     */
    RequestBody createRequestBody(Map<String, Object> paramMap);
}
