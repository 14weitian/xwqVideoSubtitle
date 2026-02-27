package com.subtitle.service;

import com.subtitle.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    User register(String username, String email, String password, String nickname);

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    User findByEmail(String email);

    /**
     * 根据ID查询用户
     */
    User findById(Long id);

    /**
     * 检查用户名是否已存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查邮箱是否已存在
     */
    boolean isEmailExists(String email);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新用户信息
     */
    User updateUserInfo(Long userId, String nickname, String email);

    /**
     * 更新用户头像
     */
    User updateAvatar(Long userId, String avatarPath);
}
