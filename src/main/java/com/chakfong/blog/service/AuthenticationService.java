package com.chakfong.blog.service;

import com.chakfong.blog.entity.ImageCode;
import com.chakfong.blog.exception.ErrorCode;
import com.chakfong.blog.security.RequestIPHolder;
import com.chakfong.blog.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.chakfong.blog.utils.RedisKeys.*;
import static com.chakfong.blog.utils.ServiceExceptionUtils.*;

@Service
@Transactional
@Slf4j
public class AuthenticationService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private CaptchaService captchaService;

    public boolean isOverLogin() {
        String ip = RequestIPHolder.getContext();
        String key = RedisUtils.keySplice(FAIL_LOGIN_COUNT_KEY, ip);
        RAtomicLong failLoginCountBucket = redissonClient.getAtomicLong(key);
        Long count = failLoginCountBucket.get();
        if (count >= 5)
            return true;
        return false;
    }

    public String createCaptcha() {
        String ip = RequestIPHolder.getContext();
        ImageCode imageCode = captchaService.createImageCode();
        log.info("[AuthenticationService.createCaptcha()] 登录图片验证码为 {}",imageCode.getCode());
        saveCaptchaCode(imageCode, ip);
        return imageCode.getImageBase64();
    }

    public boolean validateCaptcha(String code) {
        String ip = RequestIPHolder.getContext();
        String key = RedisUtils.keySplice(LOGIN_CAPTCHA_KEY, ip);
        RBucket<String> captchaBucket = redissonClient.getBucket(key);
        String realCode = captchaBucket.get();
        throwIfNull(realCode, ErrorCode.CAPTCHA_EXPIRED,"验证码过期");
        throwIfNotEquals(realCode, code, ErrorCode.CAPTCHA_UNCORRECTED,  "验证码不正确");
        return true;
    }

    private void saveCaptchaCode(ImageCode imageCode, String ip) {
        String key = RedisUtils.keySplice(LOGIN_CAPTCHA_KEY, ip);
        RBucket<String> captchaBucket = redissonClient.getBucket(key);
        captchaBucket.set(imageCode.getCode(), imageCode.getExpireTime(), TimeUnit.MINUTES);
    }


}
