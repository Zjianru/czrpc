package com.cz.core.registry.channel;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.cz.core.ex.ExErrorCodes;
import com.cz.core.ex.RpcException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * HttpChannel类，实现了Channel接口，提供HTTP通信功能。
 *
 * @author Zjianru
 */
@Slf4j
public class HttpChannel implements Channel {

    private final OkHttpClient client;

    // 定义JSON的媒体类型
    MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    /**
     * 构造函数，初始化OkHttpClient客户端。
     *
     * @param timeout 连接、读写超时时间（单位：微秒）
     */
    public HttpChannel(int timeout) {
        // 配置OkHttpClient，包括连接池、超时设置、重试等
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS)) // 连接池
                .readTimeout(timeout, TimeUnit.MILLISECONDS) // 读超时时间
                .writeTimeout(timeout, TimeUnit.MILLISECONDS) // 写超时时间
                .connectTimeout(timeout, TimeUnit.MILLISECONDS) // 连接超时时间
                .retryOnConnectionFailure(true) // 是否重试连接失败
                .build();
    }

    /**
     * 使用POST方法进行通信。
     *
     * @param url   请求的URL
     * @param param 请求参数，JSON格式
     * @param clazz 返回结果的类型
     * @return 解析后的请求结果对象
     * @throws RpcException 如果发生网络异常或超时，则抛出RpcException
     */
    @Override
    @SneakyThrows
    public <T> T post(String url, String param, Class<T> clazz) {
        // 打印调试信息
        log.debug("[method]postConnect ==> url=={},param {}", url, param);
        try {
            // 创建请求体
            RequestBody requestBody = RequestBody.create(JSON_TYPE, param);
            log.debug("[method]postConnect ==> requestBody {}", requestBody);
            // 构建请求
            Request call = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            // 执行请求并获取响应
            String response = client.newCall(call).execute().body().string();
            log.debug("[method]postConnect ==> response {}", response);
            // 解析响应
            return clazz != null ? JSON.parseObject(response, clazz) : (T) response;
        } catch (Exception e) {
            // 抛出RPC异常
            throw new RpcException(e, ExErrorCodes.SOCKET_TIME_OUT);
        }
    }

    /**
     * 使用GET方法进行通信。
     *
     * @param url   请求的URL
     * @param clazz 返回结果的类型
     * @return 解析后的请求结果对象
     * @throws RpcException 如果发生网络异常或超时，则抛出RpcException
     */
    @Override
    @SneakyThrows
    public <T> T get(String url, Class<T> clazz) {
        // 打印调试信息
        log.debug("[method]getConnect ==> url {}", url);
        try {
            String response = getResponseWithGetMethod(url);
            log.debug("[method]getConnect ==> responseMsg {}", response);
            // 解析响应
            return JSON.parseObject(response, clazz);
        } catch (Exception e) {
            // 抛出RPC异常
            throw new RpcException(e, ExErrorCodes.SOCKET_TIME_OUT);
        }
    }


    /**
     * 只使用 url 通信
     *
     * @param url           url
     * @param typeReference target clazz
     * @return response
     */
    @Override
    @SneakyThrows
    public <T> T get(String url, TypeReference<T> typeReference) {
        // 打印调试信息
        log.debug("[method]getConnect return typeReference ==> url {}", url);
        try {
            String response = getResponseWithGetMethod(url);
            log.debug("[method]getConnect return typeReference ==> responseMsg {}", response);
            // 解析响应
            return JSON.parseObject(response, typeReference);
        } catch (Exception e) {
            // 抛出RPC异常
            throw new RpcException(e, ExErrorCodes.SOCKET_TIME_OUT);
        }
    }


    private String getResponseWithGetMethod(String url) throws IOException {
        // 构建请求
        Request call = new Request.Builder()
                .url(url)
                .get()
                .build();
        // 执行请求并获取响应
        return client.newCall(call).execute().body().string();
    }




}


