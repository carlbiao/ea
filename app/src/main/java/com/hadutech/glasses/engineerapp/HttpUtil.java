package com.hadutech.glasses.engineerapp;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author wenyb
 * Http请求工具类
 */
public class HttpUtil {
    private static final String TAG = "HttpUtil";

    private static ExecutorService executorService = null;

    /**
     * 全局的okHttpClient
     */
    private static OkHttpClient sOkHttpClient = new OkHttpClient.Builder().cookieJar(new CookieJar() {

        private Map<String, List<Cookie>> cookieStore = new HashMap<String, List<Cookie>>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            //根据请求的host保存cookies到内存，达到session的保存
            Log.d(TAG, "host: " + url.host());
            cookieStore.put(url.host(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }
    }).connectTimeout(15, TimeUnit.SECONDS)//设置连接超时时间
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build();

    private synchronized static ExecutorService getExecutorService(){
        if(executorService == null){
            int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
            int KEEP_ALIVE_TIME = 1;
            TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
            BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
            executorService = new ThreadPoolExecutor(NUMBER_OF_CORES,
                    NUMBER_OF_CORES*2, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, taskQueue);
        }
        return executorService;
    }

    /**
     * 发送Get请求
     * @param url
     * @param callback
     */
    public static void doGet(final String url,final Callback callback){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Call call = sOkHttpClient.newCall(request);
                call.enqueue(callback);
            }
        };
        getExecutorService().execute(runnable);
    }

    /**
     * 发送Post请求
     * @param url
     * @param callback
     */
    public static void doPost(final String url,final Map<String,Object> postParams,final Callback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                FormBody.Builder builder = new FormBody.Builder();
                Set<Map.Entry<String, Object>> entrySet = postParams.entrySet();

                for (Map.Entry<String, Object> entry : entrySet) {
                    builder.add(entry.getKey(), String.valueOf(entry.getValue()));
                }
                FormBody requestBody = builder.build();
                Request request = new Request.Builder().url(url).post(requestBody).build();
                Call call = sOkHttpClient.newCall(request);
                call.enqueue(callback);
            }

        };
        getExecutorService().execute(runnable);
    }
}
