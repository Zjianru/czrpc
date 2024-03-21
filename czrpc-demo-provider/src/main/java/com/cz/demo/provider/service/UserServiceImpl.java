package com.cz.demo.provider.service;

import com.cz.core.annotation.czProvider;
import com.cz.demo.api.pojo.User;
import com.cz.demo.api.service.UserService;
import org.springframework.stereotype.Service;

/**
 * czrpc - user demo service
 *
 * @author Zjianru
 */
@Service
@czProvider
public class UserServiceImpl implements UserService {
    /**
     * find user by id
     *
     * @param id id
     * @return user info
     */
    @Override
    public User findById(Integer id) {
        return new User(id, "cz--" + System.currentTimeMillis());
    }

    /**
     * find user by id and name
     *
     * @param id   id
     * @param name name
     * @return user info
     */
    @Override
    public User findById(Integer id, String name) {
        return new User(id, name + "--" + System.currentTimeMillis());
    }
}
