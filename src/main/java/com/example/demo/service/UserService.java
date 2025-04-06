package com.example.demo.service;

import com.example.demo.constant.CacheKeyEnum;
import com.example.demo.constant.LocalKey;
import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.UserEntity;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    RedissonClient redissonClient;
    MessageSource messageSource;

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> createUser(CreateUserRequest createUserRequest, Locale locale) {

        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(messageSource.getMessage(LocalKey.USER_ALREADY_EXIST, null, locale));
        }

        UserEntity userEntity = UserEntity.builder()
                .username(createUserRequest.getUsername())
                .avatar(createUserRequest.getAvatar())
                .displayName(createUserRequest.getDisplayName())
                .rewardPoints(0)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toUserResponse(userRepository.save(userEntity)));
    }

    public ResponseEntity<?> getUserById(Long id, Locale locale) {
        RBucket<UserResponse> bucket = redissonClient.getBucket(CacheKeyEnum.USER.genKey(id));
        UserResponse cachedUser = bucket.get();

        if (cachedUser != null)
            return ResponseEntity.ok(cachedUser);

        Optional<UserEntity> userEntityOptional = userRepository.findById(id);
        if (userEntityOptional.isEmpty()) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(messageSource.getMessage(LocalKey.USER_NOT_FOUND, null, locale));
        }

        return ResponseEntity.ok(updateCache(bucket, userEntityOptional.get()));
    }

    public UserResponse updateCache(RBucket<UserResponse> bucket,
                                    UserEntity user) {

        UserResponse response = userMapper.toUserResponse(user);

        bucket.set(response);
        bucket.expire(Duration.ofHours(1));

        return response;
    }
}
