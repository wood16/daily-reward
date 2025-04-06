package com.example.demo.mapper;

import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.UserEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(UserEntity userEntity);
}
