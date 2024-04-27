package com.zhang.lib.http.interfaces;

import androidx.annotation.NonNull;

/**
 * 接口请求结果分析器
 *
 * @author ZhangXiaoMing 2024-04-27 21:23 周六
 */
public interface IResponseContentAnalyzer {

    /**
     * 分析请求结果
     *
     * @param responseContent 请求结果内容
     */
    void analyseResponseContent(@NonNull String responseContent);
}
