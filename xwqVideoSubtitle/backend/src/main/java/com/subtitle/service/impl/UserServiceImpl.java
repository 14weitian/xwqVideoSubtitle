package com.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.subtitle.entity.User;
import com.subtitle.mapper.UserMapper;
import com.subtitle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User register(String username, String email, String password, String nickname) {
        // 检查用户名是否已存在
        if (isUsernameExists(username)) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (isEmailExists(email)) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null && !nickname.trim().isEmpty() ? nickname : username);
        user.setStatus(1);

        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
        // 查询用户
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }

        return user;
    }

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User findByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return findByUsername(username) != null;
    }

    @Override
    public boolean isEmailExists(String email) {
        return findByEmail(email) != null;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户
        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码（如果提供了旧密码）
        if (oldPassword != null && !oldPassword.trim().isEmpty()) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new RuntimeException("旧密码错误");
            }
        }

        // 验证新密码强度（至少6位）
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("新密码长度至少为6位");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public User updateUserInfo(Long userId, String nickname, String email) {
        // 查询用户
        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 如果修改邮箱，检查是否已被使用
        if (email != null && !email.equals(user.getEmail())) {
            if (isEmailExists(email)) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
            user.setEmail(email);
        }

        // 更新昵称
        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname);
        }

        userMapper.updateById(user);
        return user;
    }

    @Override
    public User updateAvatar(Long userId, String avatarPath) {
        // 查询用户
        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新头像路径
        user.setAvatar(avatarPath);
        userMapper.updateById(user);
        return user;
    }
}
