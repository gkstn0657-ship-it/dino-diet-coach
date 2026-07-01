package com.ssafy.nyamnyam.domain.stats;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyStat {
    private Integer id;
    private Integer mno;
    private LocalDate statDate;
    private Integer totalKcal;
    private Integer protein;
    private Integer carbs;
    private Integer fat;
    private Integer mealCount;
}
