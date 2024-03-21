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
        return new User(id, "findById--" + System.currentTimeMillis(), 0L);
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
        return new User(id, "findById--" + name + "--" + System.currentTimeMillis(), 0L);
    }

    /**
     * check user info
     *
     * @param user user info
     * @return user
     */
    @Override
    public User ObjectParamCheck(User user) {
        return user;
    }

    /**
     * find user by identity
     *
     * @param ident ident
     * @return user info
     */
    @Override
    public User findByIdentity(Long ident) {
        return new User(0, "cz--" + ident, ident);
    }
}
