package com.example.demo.service;

import com.example.demo.constant.CacheKeyEnum;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.UserEntity;
import com.example.demo.mapper.UserMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CacheService {

    RedissonClient redissonClient;
    UserMapper userMapper;

    public void updateCacheCheckInAfterCommit(Long userId, LocalDate date, UserEntity user) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                RBucket<UserResponse> userBucket = redissonClient.getBucket(CacheKeyEnum.USER.genKey(userId));
                updateCacheUser(userBucket, user);

                RBucket<Boolean> checkInBucket = redissonClient.getBucket(CacheKeyEnum.CHECK_IN.genKeyDate(userId, date));
                updateCacheChecked(checkInBucket, date);
            }
        });
    }

    public void updateCacheChecked(RBucket<Boolean> checkInBucket, LocalDate date) {

        checkInBucket.set(true);
        checkInBucket.expire(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public UserResponse updateCacheUser(RBucket<UserResponse> bucket,
                                        UserEntity user) {

        UserResponse response = userMapper.toUserResponse(user);

        bucket.set(response);
        bucket.expire(Duration.ofHours(1));

        return response;
    }
}