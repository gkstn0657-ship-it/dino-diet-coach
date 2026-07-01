package com.ssafy.nyamnyam.domain.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Member {
    private Integer mno;
    private String email;

    // 입력은 받되(WRITE) 출력 응답에는 노출하지 않음
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String name;
    private String role;
    private Integer height;
    private Integer weight;
    private String disease;
    private String goal;
    // 목표 칼로리 계산용 개인화 입력값
    private String gender;          // male | female
    private Integer birthYear;      // 출생연도 (나이 계산)
    private String activityLevel;   // 활동량 설문으로 산출된 수준(SEDENTARY..VERY_ACTIVE) — 표시/디버깅용
    private String goalType;        // DIET | MAINTAIN | BULK_UP (목표 칼로리 계수)
    // 활동량 설문 점수 (운동 항목 0~4, 직업활동량 1~4)
    private Integer jobActivity;
    private Integer exerciseFrequency;
    private Integer exerciseIntensity;
    private Integer dailySteps;
    private Integer weeklyExerciseHours;
    private Integer targetCalories; // 서버가 계산해 저장하는 개인 목표 칼로리(없으면 미설정 → 기본 2000)
    private Boolean active;
    private LocalDateTime createdAt;
}
