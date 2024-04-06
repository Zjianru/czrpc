package com.cz.demo.provider.service;

import com.cz.core.annotation.CzProvider;
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
@CzProvider
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
    public User[] findUsers(User[] users) {
        return users;
    }

    @Override
    public List<User> getList(List<User> userList) {
        return userList;
    }

    @Override
    public Map<String, User> getMap(Map<String, User> userMap) {
        return userMap;
    }

    @Override
    public Boolean getFlag(boolean flag) {
        return !flag;
    }

    @Override
    public User findById(long id) {
        return new User(Long.valueOf(id).intValue(), "KK");
    }

    @Override
    public User ex(boolean flag) {
        if (flag) throw new RuntimeException("just throw an exception");
        return new User(100, "KK100");
    }

    /**
     * 模拟超时
     *
     * @param sleepTime 超时时间
     * @return user
     */
    @Override
    public User mockTimeOut(int sleepTime) {
        String currentPort = environment.getProperty("server.port", "");
        if ("8081".equals(currentPort)) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new User(1001, "timeout-KK100");
    }
}
