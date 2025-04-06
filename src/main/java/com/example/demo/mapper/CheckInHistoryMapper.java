package com.example.demo.mapper;

import com.example.demo.dto.response.CheckInDayResponse;
import com.example.demo.entity.CheckInHistoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CheckInHistoryMapper {

    CheckInDayResponse toCheckInDayResponse(CheckInHistoryEntity checkInHistoryEntity);
}
