package com.cz.core.registry.channel;

import com.alibaba.fastjson2.TypeReference;

/**
 * code desc
 *
 * @author Zjianru
 */
public interface Channel {
    /**
     * 携带参数方式通信
     *
     * @param url   url
     * @param param param
     * @param clazz target clazz
     * @return response
     */
    <T> T post(String url, String param, Class<T> clazz);

    /**
     * 只使用 url 通信
     *
     * @param url   url
     * @param clazz target clazz
     * @return response
     */
    <T> T get(String url, Class<T> clazz);

    /**
     * 只使用 url 通信
     *
     * @param url           url
     * @param typeReference target clazz
     * @return response
     */
    <T> T get(String url, TypeReference<T> typeReference);


    Channel defaultChannel = new HttpChannel(5000);

    static <T> T httpGet(String url, Class<T> clazz) {
        return defaultChannel.get(url, clazz);
    }

    static <T> T httpGet(String url, TypeReference<T> typeReference) {
        return defaultChannel.get(url, typeReference);
    }

    static <T> T httpPost(String url, String param, Class<T> clazz) {
        return defaultChannel.post(url, param, clazz);
    }


}
