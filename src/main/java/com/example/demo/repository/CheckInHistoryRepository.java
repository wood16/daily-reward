package com.example.demo.repository;

import com.example.demo.entity.CheckInHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInHistoryRepository extends JpaRepository<CheckInHistoryEntity, Long> {
    List<CheckInHistoryEntity> findByUserIdAndDateBetweenAndType(Long userId, LocalDate from, LocalDate to, String type);
    Optional<CheckInHistoryEntity> findByUserIdAndDateAndType(Long userId, LocalDate date, String type);
    Page<CheckInHistoryEntity> findByUserIdAndType(Long userId, String type, Pageable pageable);
}
