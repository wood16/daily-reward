package com.example.demo.service;

import com.example.demo.constant.CacheKeyEnum;
import com.example.demo.constant.LocalKey;
import com.example.demo.constant.ScoreTypeEnum;
import com.example.demo.dto.response.CheckInDayResponse;
import com.example.demo.dto.response.CheckInStatusResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.CheckInHistoryEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.mapper.CheckInHistoryMapper;
import com.example.demo.repository.CheckInHistoryRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckInHistoryService {

    UserRepository userRepository;
    CheckInHistoryRepository checkInHistoryRepository;
    RedissonClient redissonClient;
    DailyRewardConfigService dailyRewardConfigService;
    CheckInHistoryMapper checkInHistoryMapper;
    UserService userService;
    MessageSource messageSource;

    public List<CheckInStatusResponse> getCheckInStatus(long userId,
                                                        LocalDate startDate,
                                                        LocalDate endDate) {

        long numOfDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        Set<LocalDate> checkedDates = checkInHistoryRepository
                .findByUserIdAndDateBetweenAndType(userId, startDate, endDate, ScoreTypeEnum.ADD.getValue())
                .stream()
                .map(CheckInHistoryEntity::getDate)
                .collect(Collectors.toSet());

        List<CheckInStatusResponse> result = new ArrayList<>();

        Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(numOfDays)
                .forEach(date -> {
                    boolean checked = checkedDates.contains(date);

                    result.add(CheckInStatusResponse.builder()
                            .checkIn(checked)
                            .date(date)
                            .build());
                });

        return result;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public String checkIn(Long userId, Locale locale) throws ResponseStatusException, InterruptedException {

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, messageSource.getMessage(LocalKey.USER_NOT_FOUND, null, locale)));

        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalDateTime.now().toLocalTime();

        boolean validTime = checkValidTime(currentTime);

        if (!validTime) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage(LocalKey.INVALID_CHECK_IN_TIME, null, locale));
        }

        RLock lock = redissonClient.getLock(CacheKeyEnum.CHECK_IN_LOCK.genKey(userId));

        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        messageSource.getMessage(LocalKey.SYSTEM_PROCESS_CHECK_IN, null, locale));
            }

            RBucket<Boolean> bucket = redissonClient.getBucket(CacheKeyEnum.CHECK_IN.genKeyDate(userId, today));

            if (checkHaveCheckInDay(bucket, today, userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        messageSource.getMessage(LocalKey.CHECK_IN_ALREADY, null, locale));
            }

            LocalDate firstDay = today.withDayOfMonth(1);
            LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());
            List<CheckInHistoryEntity> checkInHistories = checkInHistoryRepository.findByUserIdAndDateBetweenAndType(
                    userId, firstDay, lastDay, ScoreTypeEnum.ADD.getValue());

            Map<Integer, Integer> rewardConfigMap = dailyRewardConfigService.findAllDailyRewardConfig();

            if (checkInHistories.size() >= rewardConfigMap.size()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        messageSource.getMessage(LocalKey.USER_EXCEEDED_CHECK_IN_MONTH, null, locale));
            }

            int rewardPoints = rewardConfigMap.getOrDefault(checkInHistories.size() + 1, 1);

            CheckInHistoryEntity reward = CheckInHistoryEntity.builder()
                    .date(today)
                    .rewardPoints(rewardPoints)
                    .user(user)
                    .type(ScoreTypeEnum.ADD.getValue())
                    .build();

            checkInHistoryRepository.save(reward);

            user.setRewardPoints(user.getRewardPoints() + rewardPoints);
            userRepository.save(user);
            userService.updateCache(redissonClient.getBucket(CacheKeyEnum.USER.genKey(userId)), user);

            bucket.set(true);
            bucket.expire(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            return messageSource.getMessage(LocalKey.CHECK_IN_SUCCESS, new Object[]{rewardPoints}, locale);
        } finally {
            lock.unlock();
        }
    }

    public Page<CheckInDayResponse> getCheckInHistory(Long userId,
                                                      int page,
                                                      int pageSize) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(pageSize, 1));

        Page<CheckInHistoryEntity> checkInHistoryEntities =
                checkInHistoryRepository.findByUserIdAndType(userId, ScoreTypeEnum.ADD.getValue(), pageable);

        return new PageImpl<>(
                checkInHistoryEntities.getContent().stream().map(checkInHistoryMapper::toCheckInDayResponse).toList(),
                pageable,
                checkInHistoryEntities.getTotalElements());
    }

    @Transactional(rollbackFor = Exception.class)
    public String subtractPoint(Long userId, int point, Locale locale) {

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, messageSource.getMessage(LocalKey.USER_NOT_FOUND, null, locale)));

        CheckInHistoryEntity subtract = CheckInHistoryEntity.builder()
                .date(LocalDate.now())
                .rewardPoints(point)
                .user(user)
                .type(ScoreTypeEnum.SUBTRACT.getValue())
                .build();

        checkInHistoryRepository.save(subtract);

        user.setRewardPoints(Math.max(user.getRewardPoints() - point, 0));
        userRepository.save(user);

        RBucket<UserResponse> bucket = redissonClient.getBucket(CacheKeyEnum.USER.genKey(userId));
        userService.updateCache(bucket, user);

        return messageSource.getMessage(LocalKey.SUBTRACT_POINT_SUCCESS, null, locale);
    }

    private boolean checkHaveCheckInDay(RBucket<Boolean> bucket, LocalDate date, Long userId) {

        return Boolean.TRUE.equals(bucket.get()) ||
                checkInHistoryRepository.findByUserIdAndDateAndType(userId, date, ScoreTypeEnum.ADD.getValue()).isPresent();
    }

    private boolean checkValidTime(LocalTime currentTime) {

        return (currentTime.isAfter(LocalTime.of(9, 0)) &&
                currentTime.isBefore(LocalTime.of(11, 0))) ||
                (currentTime.isAfter(LocalTime.of(19, 0)) &&
                        currentTime.isBefore(LocalTime.of(21, 0)));
    }
}
