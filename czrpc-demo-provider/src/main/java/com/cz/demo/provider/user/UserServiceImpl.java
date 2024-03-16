package com.cz.demo.provider.user;

import com.cz.czrpc.core.annotation.czProvider;
import com.cz.czrpcdemoapi.User;
import com.cz.czrpcdemoapi.UserService;
import org.springframework.stereotype.Service;

@Service
@czProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(Integer id) {
        return new User(id, "cz--" + System.currentTimeMillis());
    }
}
