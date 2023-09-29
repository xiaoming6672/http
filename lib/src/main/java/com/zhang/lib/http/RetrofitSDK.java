package com.zhang.lib.http;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.ObjectsCompat;

import com.zhang.lib.http.inteceptor.HeaderInterceptor;
import com.zhang.lib.http.inteceptor.RequestBodyTransformationInterceptor;
import com.zhang.lib.http.inteceptor.RequestEncryptionInterceptor;
import com.zhang.lib.http.inteceptor.ResponseDecryptInterceptor;
import com.zhang.lib.http.inteceptor.RetrofitLogInterceptor;
import com.zhang.lib.http.interfaces.IDecryptor;
import com.zhang.lib.http.interfaces.IEncryptor;
import com.zhang.lib.http.interfaces.IHeaderBuilder;
import com.zhang.lib.http.interfaces.INewRequestBodyBuilder;
import com.zhang.library.utils.CollectionUtils;
import com.zhang.library.utils.LogUtils;
import com.zhang.library.utils.context.ContextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit接口请求封装类
 *
 * @author ZhangXiaoMing 2023-05-16 9:33 周二
 */
public class RetrofitSDK {

    private static final String TAG = "RetrofitSDK";

    /** 默认连接超时时间，单位：秒 */
    private static final int DEFAULT_CONNECT_TIMEOUT = 15;
    /** 默认读取超时时间 */
    private static final int DEFAULT_READ_TIMEOUT = 15;
    /** 默认写入超时时间 */
    private static final int DEFAULT_WRITE_TIMEOUT = 15;


    private static volatile RetrofitSDK instance;

    private OkHttpClient mHttpClient;
    private String mBaseUrl;
    /** 是否是正式版本 */
    private boolean isRelease;

    private IHeaderBuilder mHeaderBuilder;
    private IEncryptor mEncryptor;
    private IDecryptor mDecryptor;
    private INewRequestBodyBuilder mRequestBodyBuilder;

    private RetrofitSDK() {
    }

    public static RetrofitSDK getInstance() {
        if (instance == null) {
            synchronized (RetrofitSDK.class) {
                if (instance == null) {
                    instance = new RetrofitSDK();
                }
            }
        }

        return instance;
    }


    /**
     * 初始化
     *
     * @param context 上下文
     * @param host    主域名
     */
    public RetrofitSDK init(Context context, @NonNull String host) {
        BuildParam buildParam = BuildParam.newBuilder()
                .setDebug(false)
                .setRetryOnConnectionFailure(true)
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT * 1000)
                .setReadTimeout(DEFAULT_READ_TIMEOUT * 1000)
                .setWriteTimeout(DEFAULT_WRITE_TIMEOUT * 1000)
                .setCacheSize(10 * 1024 * 1024)
                .addInterceptor(new RequestBodyTransformationInterceptor())
                .addInterceptor(new RetrofitLogInterceptor())
                .addInterceptor(new HeaderInterceptor())
                .addInterceptor(new RequestEncryptionInterceptor())
                .addInterceptor(new ResponseDecryptInterceptor());
        return init(context, host, buildParam);
    }

    /**
     * 初始化
     *
     * @param context 上下文
     * @param host    主域名
     * @param param   构造参数
     */
    public RetrofitSDK init(@NonNull Context context, @NonNull String host, BuildParam param) {
        this.mBaseUrl = ObjectsCompat.requireNonNull(host, "Host is Null");
        this.isRelease = !param.isDebug;

        ContextUtils.set(context);
        LogUtils.init(param.isDebug, param.isDebug);

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(message -> LogUtils.debug(TAG, message))
                .setLevel(isRelease ? HttpLoggingInterceptor.Level.NONE : HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(param.retryOnConnectionFailure)
                .connectTimeout(param.connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(param.readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(param.writeTimeout, TimeUnit.MILLISECONDS);

        if (param.cacheSize > 0)
            builder.cache(new Cache(ContextUtils.get().getCacheDir(), param.cacheSize));

        builder.addNetworkInterceptor(httpLoggingInterceptor);
        if (!CollectionUtils.isEmpty(param.interceptorList)) {
            for (Interceptor interceptor : param.interceptorList) {
                builder.addInterceptor(interceptor);
            }
        }

        mHttpClient = builder.build();

        return this;
    }

    /**
     * 添加请求头构造器
     *
     * @param builder 构造器
     */
    public RetrofitSDK setHeaderBuilder(IHeaderBuilder builder) {
        this.mHeaderBuilder = builder;
        return this;
    }

    /**
     * 设置加密器
     *
     * @param encryptor 加密器
     */
    public RetrofitSDK setEncryptor(IEncryptor encryptor) {
        this.mEncryptor = encryptor;
        return this;
    }

    /**
     * 设置解密器
     *
     * @param decryptor 解密器
     */
    public RetrofitSDK setDecryptor(IDecryptor decryptor) {
        this.mDecryptor = decryptor;
        return this;
    }

    /**
     * 设置解密器
     *
     * @param builder 构造者
     */
    public RetrofitSDK setNewRequestBodyBuilder(INewRequestBodyBuilder builder) {
        mRequestBodyBuilder = builder;
        return this;
    }


    /**
     * 生成请求接口类对象
     *
     * @param clazz 请求接口类
     * @param <T>   请求接口类
     */
    public <T> T create(Class<T> clazz) {
        return create(mBaseUrl, clazz);
    }

    /**
     * 生成请求接口类对象
     *
     * @param baseUrl 请求域名
     * @param clazz   请求接口类
     * @param <T>     请求接口类
     */
    public <T> T create(String baseUrl, Class<T> clazz) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(mHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
        return retrofit.create(clazz);
    }


    /** 请求主域名地址 */
    public String getHost() {
        return mBaseUrl;
    }

    /** 是否是正式版本 */
    public boolean isRelease() {
        return isRelease;
    }

    /** 生成请求的Header */
    public Map<String, String> generateRequestHeaders() {
        return getHeaderBuilder().generateRequestHeaders();
    }

    /**
     * 参数加密
     *
     * @param url    请求链接
     * @param params 请求参数
     *
     * @return 加密后的参数
     */
    public String encrypt(String url, String params) {
        return getEncryptor().encryptParam(url, params);
    }

    /**
     * 解密请求结果
     *
     * @param url     请求链接
     * @param content 请求结果
     *
     * @return 解密后的结果
     */
    public String decrypt(String url, String content) {
        return getDecryptor().decryptResponse(url, content);
    }


    private IHeaderBuilder getHeaderBuilder() {
        if (mHeaderBuilder == null) {
            mHeaderBuilder = HashMap::new;
        }

        return mHeaderBuilder;
    }

    public boolean hasEncryptor() {
        return mEncryptor != null && mEncryptor != mDefaultEncryptor;
    }

    public boolean hasDecryptor() {
        return mDecryptor != null && mDecryptor != mDefaultDecryptor;
    }

    private IEncryptor getEncryptor() {
        if (mEncryptor == null)
            mEncryptor = mDefaultEncryptor;

        return mEncryptor;
    }

    private IDecryptor getDecryptor() {
        if (mDecryptor == null)
            mDecryptor = mDefaultDecryptor;

        return mDecryptor;
    }

    /** 是否有新请求体构造器 */
    public boolean hasNewRequestBodyBuilder() {
        return mRequestBodyBuilder != null;
    }

    /**
     * 创建新的请求体
     *
     * @param paramMap 参数集合
     */
    public RequestBody newRequestBody(Map<String, Object> paramMap) {
        return mRequestBodyBuilder.createRequestBody(paramMap);
    }


    //<editor-fold desc="默认加密器">
    private final IEncryptor mDefaultEncryptor;

    {
        mDefaultEncryptor = (url, params) -> params;
    }
    //</editor-fold>

    //<editor-fold desc="默认解密器">
    private final IDecryptor mDefaultDecryptor;

    {
        mDefaultDecryptor = (url, responseContent) -> responseContent;
    }
    //</editor-fold>


    /** 构造参数 */
    public static class BuildParam {

        /** 是否debug模式 */
        boolean isDebug;
        /** 连接失败的时候是否默认重试一次，若需要重试N次，需要拦截器实现 */
        boolean retryOnConnectionFailure;
        /** 连接超时时间，单位：毫秒 */
        long connectTimeout;
        /** 读取超时时间，单位：毫秒 */
        long readTimeout;
        /** 写入超时时间，单位：毫秒 */
        long writeTimeout;
        /** 缓存内存大小，单位：B */
        long cacheSize;
        /** 拦截器列表 */
        List<Interceptor> interceptorList;

        private BuildParam() {
        }

        public static BuildParam newBuilder() {
            return new BuildParam();
        }

        public BuildParam setDebug(boolean debug) {
            isDebug = debug;
            return this;
        }

        public BuildParam setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        public BuildParam setConnectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public BuildParam setReadTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public BuildParam setWriteTimeout(long writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public BuildParam setCacheSize(long cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        public BuildParam setInterceptorList(List<Interceptor> interceptorList) {
            this.interceptorList = interceptorList;
            return this;
        }

        public BuildParam addInterceptor(Interceptor interceptor) {
            if (interceptor == null)
                return this;

            if (interceptorList == null)
                interceptorList = new ArrayList<>();

            if (!interceptorList.contains(interceptor))
                interceptorList.add(interceptor);

            return this;
        }
    }
}
