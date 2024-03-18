package com.cz.demo.provider.user;

import com.cz.core.annotation.czProvider;
import com.cz.demo.api.User;
import com.cz.demo.api.UserService;
import org.springframework.stereotype.Service;

/**
 * czrpc - demo service
 *
 * @author Zjianru
 */
@Service
@czProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(Integer id) {
        return new User(id, "cz--" + System.currentTimeMillis());
    }
}
