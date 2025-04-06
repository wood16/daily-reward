package com.example.demo.service;

import com.example.demo.constant.CacheKeyEnum;
import com.example.demo.entity.DailyRewardConfigEntity;
import com.example.demo.repository.DailyRewardConfigRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DailyRewardConfigService {

    RedissonClient redissonClient;
    DailyRewardConfigRepository dailyRewardConfigRepository;

    public Map<Integer, Integer> findAllDailyRewardConfig() {

        RMapCache<Integer, Integer> rMapCache = redissonClient.getMapCache(CacheKeyEnum.CONFIG.getValue());
        if (!rMapCache.isEmpty()) {
            return new HashMap<>(rMapCache);
        }

        HashMap<Integer, Integer> rewardConfigMap = dailyRewardConfigRepository.findAll().stream()
                .collect(Collectors.toMap(DailyRewardConfigEntity::getDayOfStreak,
                        DailyRewardConfigEntity::getRewardPoints, (existing, replacement) -> existing, HashMap::new));

        rMapCache.putAll(rewardConfigMap);
        rMapCache.expire(Duration.ofDays(1));

        return rewardConfigMap;
    }
}
