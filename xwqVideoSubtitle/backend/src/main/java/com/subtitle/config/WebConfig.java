package com.subtitle.config;

import com.subtitle.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Value("${app.upload-path}")
    private String uploadPath;

    // CORS 配置由 SecurityConfig 统一管理，这里不再配置

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传文件目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",         // 登录接口
                        "/auth/register",      // 注册接口
                        "/auth/check-username", // 检查用户名
                        "/auth/check-email",    // 检查邮箱
                        "/auth/forgot-password/**", // 忘记密码接口
                        "/uploads/**",         // 上传文件访问
                        "/captcha/**",         // 验证码接口
                        "/actuator/**",        // 健康检查接口
                        "/error"               // 错误页面
                );
    }
}