package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "checkin_history", indexes = {
        @Index(name = "idx_checkin_user_date_type", columnList = "user_id, date, type")
})
public class CheckInHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDate date;

    int rewardPoints;

    String type;

    @ManyToOne
    @JoinColumn(name = "user_id")
    UserEntity user;
}
