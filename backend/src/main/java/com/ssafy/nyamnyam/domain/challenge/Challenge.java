package com.ssafy.nyamnyam.domain.challenge;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Challenge {
    private Integer cno;
    private Integer mno;
    private String title;
    private String description;
    private String emoji;
    private String color;
    private LocalDate startDate;
    private LocalDate endDate;
    private String imageUrl;
    private String condType;    // 식단 인증 조건: DAILY_KCAL_MAX | DAILY_PROTEIN_MIN | WEEKLY_KCAL_MAX | DAILY_WATER_MIN | null
    private Integer condValue;  // 기준값 (kcal/g/잔)
    private String approvalStatus; // PENDING | APPROVED | REJECTED
    private String visibility;     // VISIBLE | HIDDEN
    private LocalDateTime createdAt;

    // 조회 가공용 (조인/집계)
    private Integer participantCount;
    private Integer progress;
    private Integer doneDays;
    private Integer totalDays;
}
