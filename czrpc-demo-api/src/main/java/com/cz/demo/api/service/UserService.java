package com.cz.demo.api.service;

import com.cz.demo.api.pojo.User;

/**
 * demo - user interface
 *
 * @author Zjianru
 */
public interface UserService {

    /**
     * find user by id
     *
     * @param id id
     * @return user info
     */
    User findById(Integer id);

    /**
     * find user by id and name
     *
     * @param id   id
     * @param name name
     * @return user info
     */
    User findById(Integer id, String name);

}
