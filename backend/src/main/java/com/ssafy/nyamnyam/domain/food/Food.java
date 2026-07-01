package com.ssafy.nyamnyam.domain.food;

import lombok.Data;

@Data
public class Food {
    private Integer fno;
    private String name;
    private Integer kcal;
    private Integer protein;
    private Integer carbs;
    private Integer fat;
}
