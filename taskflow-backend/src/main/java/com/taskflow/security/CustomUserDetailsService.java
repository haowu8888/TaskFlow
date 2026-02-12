package com.taskflow.security;

import com.taskflow.entity.User;
import com.taskflow.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String userIdStr) throws UsernameNotFoundException {
        Long userId = Long.parseLong(userIdStr);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with id: " + userId);
        }
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()),
                user.getPasswordHash(),
                new ArrayList<>()
        );
    }
}
