package com.chakfong.blog.security;

import com.chakfong.blog.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.chakfong.blog.utils.RedisKeys.FAIL_LOGIN_COUNT_KEY;

@Component
@Slf4j
public class AuthenticationFailureBadCredentialsEventListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    RedissonClient redissonClient;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent authenticationFailureBadCredentialsEvent) {
        String ip = RequestIPHolder.getContext();
        String key = RedisUtils.keySplice(FAIL_LOGIN_COUNT_KEY, ip);
        RAtomicLong failLoginCountBucket = redissonClient.getAtomicLong(key);
        Long count = failLoginCountBucket.incrementAndGet();
        failLoginCountBucket.expire(5, TimeUnit.MINUTES);
        log.info("IP："+ip+" 失败次数为" + count );
    }

}