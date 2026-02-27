package com.subtitle.controller;

import com.subtitle.entity.User;
import com.subtitle.service.UserService;
import com.subtitle.service.VerificationCodeService;
import com.subtitle.utils.JwtUtils;
import com.subtitle.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Value("${app.upload-path}")
    private String uploadPath;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String nickname = request.get("nickname");

        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error("邮箱不能为空");
        }
        if (password == null || password.length() < 6) {
            return ApiResponse.error("密码长度至少为6位");
        }

        try {
            User user = userService.register(username, email, password, nickname);

            // 生成 Token
            String token = jwtUtils.generateToken(user);

            // 返回用户信息和 Token
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", buildUserInfo(user));

            return ApiResponse.success(data, "注册成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return ApiResponse.error("密码不能为空");
        }

        try {
            User user = userService.login(username, password);

            // 生成 Token
            String token = jwtUtils.generateToken(user);

            // 返回用户信息和 Token
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", buildUserInfo(user));

            return ApiResponse.success(data,"登录成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/check-username")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.isUsernameExists(username);
        return ApiResponse.success(exists);
    }

    /**
     * 检查邮箱是否已存在
     */
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);
        return ApiResponse.success(exists);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentUser(@RequestAttribute("userId") Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(buildUserInfo(user));
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        // 参数验证
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return ApiResponse.error("请输入旧密码");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ApiResponse.error("新密码长度至少为6位");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ApiResponse.error("两次输入的新密码不一致");
        }

        try {
            userService.changePassword(userId, oldPassword, newPassword);
            return ApiResponse.success(null, "密码修改成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        String email = request.get("email");

        // 参数验证
        if (email != null && !email.trim().isEmpty()) {
            // 简单的邮箱格式验证
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return ApiResponse.error("邮箱格式不正确");
            }
        }

        try {
            User user = userService.updateUserInfo(userId, nickname, email);
            return ApiResponse.success(buildUserInfo(user), "信息更新成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ApiResponse<Map<String, Object>> uploadAvatar(
            @RequestAttribute("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.error("只能上传图片文件");
        }

        // 验证文件大小（最大 2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return ApiResponse.error("图片大小不能超过 2MB");
        }

        try {
            // 创建头像存储目录
            String avatarDir = uploadPath + "/avatars";
            Path avatarPath = Paths.get(avatarDir);
            if (!Files.exists(avatarPath)) {
                Files.createDirectories(avatarPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            File destFile = new File(avatarDir + "/" + filename);
            file.transferTo(destFile);

            // 更新用户头像路径（相对路径）
            String avatarUrl = "/uploads/avatars/" + filename;
            User user = userService.updateAvatar(userId, avatarUrl);

            return ApiResponse.success(buildUserInfo(user), "头像上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 发送验证码（忘记密码）
     */
    @PostMapping("/forgot-password/send-code")
    public ApiResponse<Void> sendResetCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // 参数验证
        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error("请输入邮箱地址");
        }

        // 验证邮箱格式
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ApiResponse.error("邮箱格式不正确");
        }

        // 检查邮箱是否已注册
        User user = userService.findByEmail(email);
        if (user == null) {
            return ApiResponse.error("该邮箱未注册");
        }

        // 发送验证码
        boolean success = verificationCodeService.sendCode(email, "PASSWORD_RESET");
        if (success) {
            return ApiResponse.success(null, "验证码已发送到您的邮箱");
        } else {
            return ApiResponse.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/forgot-password/reset")
    public ApiResponse<Void> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        // 参数验证
        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error("请输入邮箱地址");
        }
        if (code == null || code.trim().isEmpty()) {
            return ApiResponse.error("请输入验证码");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ApiResponse.error("新密码长度至少为6位");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ApiResponse.error("两次输入的密码不一致");
        }

        // 验证验证码
        boolean valid = verificationCodeService.verifyCode(email, code, "PASSWORD_RESET");
        if (!valid) {
            return ApiResponse.error("验证码无效或已过期");
        }

        // 查找用户
        User user = userService.findByEmail(email);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        // 更新密码
        try {
            userService.changePassword(user.getId(), null, newPassword);
            return ApiResponse.success(null, "密码重置成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 构建用户信息（不包含敏感信息）
     */
    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("status", user.getStatus());
        userInfo.put("createdAt", user.getCreatedAt());
        return userInfo;
    }
}
