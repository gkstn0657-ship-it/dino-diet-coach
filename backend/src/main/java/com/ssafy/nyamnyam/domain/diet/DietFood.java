package com.ssafy.nyamnyam.domain.diet;

import lombok.Data;

@Data
public class DietFood {
    private Integer id;
    private Integer dno;
    private Integer fno;
    private String name;
    private Integer kcal;
    private Integer protein;
    private Integer carbs;
    private Integer fat;
    private String source; // DB | AI | PHOTO (영양정보 출처)
}
