package com.cz.core.register.listener;

import com.cz.core.register.Event;

/**
 * 注册中心变化监听器
 *
 * @author Zjianru
 */
public interface ChangedListener {
    void fire(Event event);
}
