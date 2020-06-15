package com.chakfong.blog.service;


import com.chakfong.blog.configuration.properties.EmailProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
@Slf4j
public class EmailService {

    @Autowired
    EmailProperties emailProperties;

    private static final String CONTENT_SEND_CAPTCHA = "您的验证码是：$code。请不要把验证码泄露给其他人。";

    private static final String TITLE_SEND_CAPTCHA = "邮箱验证码";

    private static volatile Session singletonSession;

    public Session getSession() {
        if (singletonSession == null) {
            synchronized (EmailService.class) {
                if (singletonSession == null) {
                    Properties p = new Properties();

                    p.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    p.setProperty("mail.smtp.socketFactory.port", "465");
                    p.setProperty("mail.transport.protocol", emailProperties.getProtocol());

                    p.setProperty("mail.smtp.auth", emailProperties.getAuth());
                    p.setProperty("mail.smtp.host", emailProperties.getHost());
                    p.setProperty("mail.smtp.port", emailProperties.getPort());

                    Session tmp = Session.getInstance(p, new Authenticator() {
                        @Override
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailProperties.getUsername(), emailProperties.getPassword());
                        }
                    });
                    tmp.setDebug(emailProperties.getDebug());
                    singletonSession = tmp;
                }
            }
        }
        return singletonSession;
    }

    public Boolean sendEmail(String target, String title, String content) {

        Session session = getSession();
        try {
            Message message = new MimeMessage(session);
            //设置发件人地址
            message.setFrom(new InternetAddress(emailProperties.getUsername()));
            //设置发件内容
            message.setText(content);
            //设置邮件标题
            message.setSubject(title);

            //通过session获得transport对象
            Transport tran = session.getTransport();

            tran.connect();
            //发送邮件并设置收件人地址
            InternetAddress internetAddress = new InternetAddress(target);

            tran.sendMessage(message, new Address[]{internetAddress});
            tran.close();
            return true;

        } catch (Exception e) {
            log.error("[EmailService] sendEmail " + e.getMessage());
            return false;
        }

    }

    public Boolean sendCaptcha(String target, String code) {
        String content = CONTENT_SEND_CAPTCHA.replace("$code", code);
        log.info("向{}发送验证码{}", target, code);
        return sendEmail(target, TITLE_SEND_CAPTCHA, content);
    }
}
