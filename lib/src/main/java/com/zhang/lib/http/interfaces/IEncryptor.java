package com.zhang.lib.http.interfaces;

/**
 * 加密接口类
 *
 * @author ZhangXiaoMing 2023-06-12 21:11 周一
 */
public interface IEncryptor {

    /**
     * 参数加密
     *
     * @param url    请求链接
     * @param params 请求参数
     *
     * @return 加密后的参数
     */
    String encryptParam(String url, String params);

}
