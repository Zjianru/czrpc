package com.cz.demo.api.service;

import com.cz.demo.api.pojo.Order;

/**
 * demo - order interface
 *
 * @author Zjianru
 */
public interface OrderService {
    /**
     * find order by id
     *
     * @param id id
     * @return order info
     */
    Order findById(int id);
}
