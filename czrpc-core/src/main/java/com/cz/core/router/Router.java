package com.cz.core.router;

import java.util.List;

/**
 * 路由 - 机器集合
 *
 * @author Zjianru
 */
public interface Router<T> {
    Router Default = route -> route;

    List<T> selectRoute(List<T> providers);

}
