package com.cz.demo.consumer.controller;

import com.cz.core.annotation.CzConsumer;
import com.cz.demo.api.pojo.User;
import com.cz.demo.api.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * consumer api endpoint
 *
 * @author Zjianru
 */
@RestController
public class ConsumerController {
    @CzConsumer
    UserService userService;

    @RequestMapping("/findById")
    public User findById(int id) {
        return userService.findById(id);
    }
}
