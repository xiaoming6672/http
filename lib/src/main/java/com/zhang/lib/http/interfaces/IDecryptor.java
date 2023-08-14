package com.zhang.lib.http.interfaces;

/**
 * 解密接口类
 *
 * @author ZhangXiaoMing 2023-06-12 21:12 周一
 */
public interface IDecryptor {

    /**
     * 解密请求结果
     *
     * @param url             请求链接
     * @param responseContent 请求结果
     *
     * @return 解密后的结果
     */
    String decryptResponse(String url, String responseContent);

}
