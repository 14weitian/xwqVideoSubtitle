package com.subtitle.service.impl;

import com.subtitle.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public boolean sendVerificationCode(String to, String code, String type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("【视频字幕生成系统】验证码");

            // 构建HTML邮件内容
            String content = buildEmailContent(code, type);
            helper.setText(content, true);  // true表示HTML格式

            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code, String type) {
        String title = "PASSWORD_RESET".equals(type) ? "重置密码" : "验证码";

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                "        .content { background: #f9f9f9; padding: 30px; border: 1px solid #ddd; }" +
                "        .code-box { background: white; border: 2px dashed #667eea; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }" +
                "        .code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; }" +
                "        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 10px 15px; margin: 20px 0; border-radius: 4px; }" +
                "        .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🎬 视频字幕生成系统</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h2>您好！</h2>" +
                "            <p>您正在进行 <strong>" + title + "</strong> 操作，请使用以下验证码：</p>" +
                "            <div class='code-box'>" +
                "                <div class='code'>" + code + "</div>" +
                "            </div>" +
                "            <div class='warning'>" +
                "                ⚠️ <strong>安全提示：</strong>" +
                "                <ul style='margin: 10px 0; padding-left: 20px;'>" +
                "                    <li>验证码有效期为 <strong>10分钟</strong></li>" +
                "                    <li>请勿将验证码告诉他人</li>" +
                "                    <li>如非本人操作，请忽略此邮件</li>" +
                "                </ul>" +
                "            </div>" +
                "            <p>如果这不是您的操作，请忽略此邮件。</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2026 视频字幕生成系统 | 此邮件为系统自动发送，请勿回复</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
