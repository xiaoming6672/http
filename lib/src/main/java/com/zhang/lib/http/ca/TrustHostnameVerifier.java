package com.zhang.lib.http.ca;

import android.net.Uri;
import android.text.TextUtils;

import com.zhang.lib.http.RetrofitSDK;
import com.zhang.lib.http.constant.RetrofitConstant;
import com.zhang.library.utils.LogUtils;

import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * 信任主机名验证程序
 *
 * @author ZhangXiaoMing 2023-10-18 21:20 周三
 */
public class TrustHostnameVerifier implements HostnameVerifier {


    /**
     * Verify that the host name is an acceptable match with the server's authentication scheme.
     *
     * @param hostname the host name
     * @param session  SSLSession used on the connection to host
     *
     * @return true if the host name is acceptable
     */
    @Override
    public boolean verify(String hostname, SSLSession session) {
        LogUtils.debug(RetrofitConstant.TAG, "TrustHostnameVerifier>>>verify()>>>hostname=%s", hostname);
        List<String> trustUrlList = RetrofitSDK.getInstance().getTrustUrlList();
        for (String url : trustUrlList) {
            Uri uri = Uri.parse(url);
            LogUtils.debug(RetrofitConstant.TAG, "TrustHostnameVerifier>>>verify()>>>trustUrlList.url=%s , host=%s", url, uri.getHost());
            if (TextUtils.equals(hostname, uri.getHost()))
                return true;
        }

        return false;
    }
}
