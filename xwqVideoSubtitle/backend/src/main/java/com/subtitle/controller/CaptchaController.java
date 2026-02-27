package com.subtitle.controller;

import com.subtitle.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.subtitle.utils.CaptchaUtils;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/captcha")
@CrossOrigin(origins = "*")
public class CaptchaController {

    /**
     * 生成验证码
     */
    @GetMapping("/generate")
    public ApiResponse<Map<String, String>> generateCaptcha(HttpSession session) {
        try {
            // 生成验证码
            String code = CaptchaUtils.generateRandomCode();
            BufferedImage image = CaptchaUtils.generateCaptchaImage(code);

            // 将验证码保存到 session
            session.setAttribute("captcha", code);

            // 转换为 Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

            Map<String, String> data = new HashMap<>();
            data.put("image", "data:image/png;base64," + base64Image);
            data.put("captchaId", session.getId());

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("生成验证码失败: " + e.getMessage());
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify")
    public ApiResponse<Boolean> verifyCaptcha(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        String inputCode = request.get("code");
        String savedCode = (String) session.getAttribute("captcha");

        if (savedCode == null) {
            return ApiResponse.error("验证码已过期，请重新获取");
        }

        // 验证后立即清除
        session.removeAttribute("captcha");

        // 忽略大小写
        boolean isValid = savedCode.equalsIgnoreCase(inputCode);
        return ApiResponse.success(isValid);
    }
}
