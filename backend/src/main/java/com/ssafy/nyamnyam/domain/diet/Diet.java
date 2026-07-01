package com.ssafy.nyamnyam.domain.diet;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Diet {
    private Integer dno;
    private Integer mno;
    private String meal;          // breakfast/lunch/dinner/snack
    private LocalDate eatenDate;
    private String title;
    private String photoUrl;
    private Integer totalKcal;
    private Integer protein;
    private Integer carbs;
    private Integer fat;
    private LocalDateTime createdAt;

    // 조회 시 구성 음식 (조인 결과)
    private List<DietFood> foods;
}
