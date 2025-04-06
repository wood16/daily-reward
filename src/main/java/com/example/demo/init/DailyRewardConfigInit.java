package com.example.demo.init;

import com.example.demo.entity.DailyRewardConfigEntity;
import com.example.demo.repository.DailyRewardConfigRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DailyRewardConfigInit implements CommandLineRunner {

    DailyRewardConfigRepository dailyRewardConfigRepository;

    @Override
    public void run(String... args) throws Exception {

//        Fibonacci

//        if (dailyRewardConfigRepository.count() == 0) {
//            List<DailyRewardConfigEntity> rewards = new ArrayList<>();
//
//            int prev1 = 1;
//            int prev2 = 0;
//
//            for (int day = 1; day <= 7; day++) {
//                int current = prev1 + prev2;
//                rewards.add(DailyRewardConfigEntity.builder()
//                        .rewardPoints(current)
//                        .dayOfStreak(day).description("Ngày " + day + " - " + current + " điểm")
//                        .build());
//
//                prev2 = prev1;
//                prev1 = current;
//            }
//
//            dailyRewardConfigRepository.saveAll(rewards);
//        }
    }
}
