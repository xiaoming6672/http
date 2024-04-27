package com.zhang.lib.http;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.util.ObjectsCompat;

import com.zhang.lib.http.ca.TrustCerts;
import com.zhang.lib.http.ca.TrustHostnameVerifier;
import com.zhang.lib.http.factory.XMConverterFactory;
import com.zhang.lib.http.inteceptor.HeaderInterceptor;
import com.zhang.lib.http.inteceptor.RequestBodyTransformationInterceptor;
import com.zhang.lib.http.inteceptor.RequestEncryptionInterceptor;
import com.zhang.lib.http.inteceptor.ResponseDecryptInterceptor;
import com.zhang.lib.http.inteceptor.RetrofitLogInterceptor;
import com.zhang.lib.http.interfaces.IDecryptor;
import com.zhang.lib.http.interfaces.IEncryptor;
import com.zhang.lib.http.interfaces.IHeaderBuilder;
import com.zhang.lib.http.interfaces.INewRequestBodyBuilder;
import com.zhang.lib.http.interfaces.IResponseContentAnalyzer;
import com.zhang.library.utils.CollectionUtils;
import com.zhang.library.utils.LogUtils;
import com.zhang.library.utils.context.ContextUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

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

    /** 请求结果转换器工厂列表 */
    private final List<Converter.Factory> mConverterFactoryList;
    /** 请求适配器工厂列表 */
    private final List<CallAdapter.Factory> mAdapterFactoryList;

    /** {@link Retrofit}对象合集 */
    private final Map<String, Retrofit> mRetrofitMap;
    /** Retrofit接口api示例对象集合 */
    private final Map<String, List<Object>> mApiMap;

    /** 信任域名列表 */
    @NonNull
    private final List<String> mTrustUrlList;
    private OkHttpClient mHttpClient;
    private String mBaseUrl;
    /** 是否是正式版本 */
    private boolean isRelease;

    /** Header构造者 */
    private IHeaderBuilder mHeaderBuilder;
    /** 加密器 */
    private IEncryptor mEncryptor;
    /** 解密器 */
    private IDecryptor mDecryptor;
    /** 请求体构造者 */
    private INewRequestBodyBuilder mRequestBodyBuilder;
    /** 请求结果分析器 */
    private IResponseContentAnalyzer mResponseAnalyzer;

    private RetrofitSDK() {
        mConverterFactoryList = new ArrayList<>();
        mAdapterFactoryList = new ArrayList<>();

        mRetrofitMap = new HashMap<>();
        mApiMap = new HashMap<>();

        mTrustUrlList = new ArrayList<>();
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

        if (param.trustUrlList != null)
            mTrustUrlList.addAll(param.trustUrlList);
        if (!mTrustUrlList.contains(host))
            mTrustUrlList.add(host);

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

        builder.sslSocketFactory(getSSLSocketFactory(), new TrustCerts());
        builder.hostnameVerifier(new TrustHostnameVerifier());

        mHttpClient = builder.build();

        mConverterFactoryList.add(XMConverterFactory.create());
        if (param.converterFactoryList != null)
            mConverterFactoryList.addAll(param.converterFactoryList);

        mAdapterFactoryList.add(RxJava3CallAdapterFactory.create());
        if (param.adapterFactoryList != null)
            mAdapterFactoryList.addAll(param.adapterFactoryList);

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
     * 设置请求结果分析器
     *
     * @param analyzer 分析器
     */
    public RetrofitSDK setResponseAnalyzer(IResponseContentAnalyzer analyzer) {
        mResponseAnalyzer = analyzer;
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
    @SuppressWarnings("unchecked")
    public <T> T create(String baseUrl, Class<T> clazz) {
        List<Object> apiList = mApiMap.get(baseUrl);
        if (apiList == null) {
            apiList = new ArrayList<>();
            mApiMap.put(baseUrl, apiList);
        }

        if (!apiList.isEmpty()) {
            for (Object item : apiList) {
                if (clazz.isAssignableFrom(item.getClass()))
                    return (T) item;
            }
        }

        Retrofit retrofit = mRetrofitMap.get(baseUrl);
        if (retrofit != null) {
            T api = retrofit.create(clazz);
            apiList.add(api);
            return api;
        }

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(mHttpClient);
//                .addConverterFactory(GsonConverterFactory.create())
//                .addConverterFactory(XMGsonConverterFactory.create())
//                .addConverterFactory(XMConverterFactory.create())
//                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())

        for (Converter.Factory factory : mConverterFactoryList)
            builder.addConverterFactory(factory);

        for (CallAdapter.Factory factory : mAdapterFactoryList)
            builder.addCallAdapterFactory(factory);

        retrofit = builder.build();


        mRetrofitMap.put(baseUrl, retrofit);

        T api = retrofit.create(clazz);
        apiList.add(api);
        return api;
    }


    /** 请求主域名地址 */
    public String getHost() {
        return mBaseUrl;
    }

    @NonNull
    public List<String> getTrustUrlList() {
        return mTrustUrlList;
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

    private SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new TrustCerts()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (SSLSocketFactory) SSLSocketFactory.getDefault();
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

    /**
     * 设置请求结果分析器
     *
     * @param responseContent 分析器
     */
    public void analyseResponseContent(@NonNull String responseContent) {
        if (mResponseAnalyzer != null) {
            mResponseAnalyzer.analyseResponseContent(responseContent);
        }
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
        /** 信任域名列表 */
        List<String> trustUrlList;
        /** cookie */
        CookieJar cookieJar;
        /** 请求结果转换器工厂列表 */
        List<Converter.Factory> converterFactoryList;
        /** 请求适配器工厂列表 */
        List<CallAdapter.Factory> adapterFactoryList;

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

        /**
         * 设置缓存大小，单位：B
         *
         * @param cacheSize 缓存大小
         */
        public BuildParam setCacheSize(long cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        /**
         * 设置cookie配置
         *
         * @param cookieJar cookie
         */
        public BuildParam setCookieJar(CookieJar cookieJar) {
            this.cookieJar = cookieJar;
            return this;
        }

        /**
         * 添加拦截器
         *
         * @param interceptor 拦截器
         */
        public BuildParam addInterceptor(Interceptor interceptor) {
            if (interceptor == null)
                return this;

            if (interceptorList == null)
                interceptorList = new ArrayList<>();

            if (!interceptorList.contains(interceptor))
                interceptorList.add(interceptor);

            return this;
        }

        public BuildParam addInterceptor(Interceptor... interceptors) {
            if (interceptors != null) {
                if (interceptorList == null)
                    interceptorList = new ArrayList<>();

                for (Interceptor interceptor : interceptors) {
                    if (!interceptorList.contains(interceptor))
                        interceptorList.add(interceptor);
                }
            }

            return this;
        }

        /**
         * 添加加解密白名单链接
         *
         * @param trustUrl 信任链接
         */
        public BuildParam addTrustUrl(String trustUrl) {
            if (TextUtils.isEmpty(trustUrl))
                return this;

            if (trustUrlList == null)
                trustUrlList = new ArrayList<>();

            if (!trustUrlList.contains(trustUrl))
                trustUrlList.add(trustUrl);

            return this;
        }

        /**
         * 添加加解密白名单链接
         *
         * @param trustUrls 信任链接
         */
        public BuildParam addTrustUrl(String... trustUrls) {
            if (trustUrls != null) {
                if (trustUrlList == null)
                    trustUrlList = new ArrayList<>();

                for (String url : trustUrls) {
                    if (!trustUrlList.contains(url))
                        trustUrlList.add(url);
                }
            }

            return this;
        }

        /**
         * 添加请求结果转换器工厂
         *
         * @param factories 转换器工厂
         */
        public BuildParam addConverterFactory(Converter.Factory... factories) {
            if (factories != null) {
                if (converterFactoryList == null)
                    converterFactoryList = new ArrayList<>();

                for (Converter.Factory factory : factories) {
                    if (!converterFactoryList.contains(factory))
                        converterFactoryList.add(factory);
                }
            }

            return this;
        }

        /**
         * 添加请求适配器工厂
         *
         * @param factories 工厂
         */
        public BuildParam addAdapterFactory(CallAdapter.Factory... factories) {
            if (factories != null) {
                if (adapterFactoryList == null)
                    adapterFactoryList = new ArrayList<>();

                for (CallAdapter.Factory factory : factories) {
                    if (!adapterFactoryList.contains(factory))
                        adapterFactoryList.add(factory);
                }
            }

            return this;
        }

    }
}
