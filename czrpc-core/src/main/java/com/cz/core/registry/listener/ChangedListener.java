package com.cz.core.registry.listener;

import com.cz.core.registry.Event;

/**
 * 注册中心变化监听器
 *
 * @author Zjianru
 */
public interface ChangedListener {
    void fire(Event event);
}
