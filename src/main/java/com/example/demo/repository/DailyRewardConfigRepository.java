package com.example.demo.repository;

import com.example.demo.entity.DailyRewardConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DailyRewardConfigRepository extends JpaRepository<DailyRewardConfigEntity, Long> {

    Optional<DailyRewardConfigEntity> findByDayOfStreak(int dayOfStreak);
}
