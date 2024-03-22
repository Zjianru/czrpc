package com.cz.demo.provider.service;

import com.cz.core.annotation.czProvider;
import com.cz.demo.api.pojo.User;
import com.cz.demo.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * czrpc - user demo service
 *
 * @author Zjianru
 */
@Service
@czProvider
public class UserServiceImpl implements UserService {
    @Autowired
    Environment environment;

    @Override
    public User findById(int id) {
        return new User(id, environment.getProperty("server.port", "") + "cz-" + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, environment.getProperty("server.port", "") + "cz-" + name + "_" + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return user.getId().longValue();
    }

    @Override
    public long getId(float id) {
        return 1L;
    }

    @Override
    public String getName() {
        return environment.getProperty("server.port", "") + "cz123";
    }

    @Override
    public String getName(int id) {
        return environment.getProperty("server.port", "") + "cz-" + id;
    }

    @Override
    public int[] getIds() {
        return new int[]{100, 200, 300};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{1, 2, 3};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }

    @Override
    public List<User> getList(List<User> userList) {
        return userList;
    }

    @Override
    public Map<String, User> getMap(Map<String, User> userMap) {
        return userMap;
    }

}
