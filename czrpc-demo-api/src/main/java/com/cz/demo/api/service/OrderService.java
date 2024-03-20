package com.cz.demo.api.service;

import com.cz.demo.api.pojo.Order;

/**
 * demo - order interface
 *
 * @author Zjianru
 */
public interface OrderService {
    Order findById(int id);
}
