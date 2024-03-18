package com.cz.demo.provider.service;

import com.cz.core.annotation.czProvider;
import com.cz.demo.api.pojo.Order;
import com.cz.demo.api.service.OrderService;
import org.springframework.stereotype.Service;

/**
 * czrpc - order demo service
 *
 * @author Zjianru
 */
@Service
@czProvider
public class OrderServiceImpl implements OrderService {
    /**
     * find order
     *
     * @param id order id
     * @return order pojo
     */
    @Override
    public Order findById(Long id) {
        return new Order(id, 99.9f);
    }
}
