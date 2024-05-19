package com.cz.core.registry.channel;

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
}
