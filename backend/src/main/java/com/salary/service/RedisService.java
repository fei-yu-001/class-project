package com.salary.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Token blacklist
    public void blacklistToken(String token, long expirationMillis) {
        redisTemplate.opsForValue().set("token:blacklist:" + token, "1", expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("token:blacklist:" + token));
    }

    // Rate limiting - login attempts
    public boolean isLoginBlocked(String username) {
        String key = "login:attempts:" + username;
        String count = redisTemplate.opsForValue().get(key);
        return count != null && Integer.parseInt(count) >= 5;
    }

    public void recordLoginAttempt(String username) {
        String key = "login:attempts:" + username;
        String count = redisTemplate.opsForValue().get(key);
        int newCount = (count == null) ? 1 : Integer.parseInt(count) + 1;
        redisTemplate.opsForValue().set(key, String.valueOf(newCount), 30, TimeUnit.MINUTES);
    }

    public void clearLoginAttempts(String username) {
        redisTemplate.delete("login:attempts:" + username);
    }

    // API rate limiting
    public boolean isRateLimited(String key, int maxRequests, int windowSeconds) {
        String redisKey = "rate:" + key;
        String count = redisTemplate.opsForValue().get(redisKey);
        if (count == null) {
            redisTemplate.opsForValue().set(redisKey, "1", windowSeconds, TimeUnit.SECONDS);
            return false;
        }
        int current = Integer.parseInt(count);
        if (current >= maxRequests) {
            return true;
        }
        redisTemplate.opsForValue().increment(redisKey);
        return false;
    }

    // Get remaining lockout time
    public long getLoginLockoutRemaining(String username) {
        String key = "login:attempts:" + username;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}
