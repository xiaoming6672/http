package com.zhang.lib.http.interfaces;

import java.util.Map;

/**
 * 请求Header添加器接口类
 *
 * @author ZhangXiaoMing 2023-06-12 21:28 周一
 */
public interface IHeaderBuilder {

    /** 生成请求的Header */
    Map<String, String> generateRequestHeaders();
}
