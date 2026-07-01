package com.ssafy.nyamnyam.domain.challenge;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ChallengeParticipant {
    private Integer id;
    private Integer cno;
    private Integer mno;
    private Integer progress;
    private Integer doneDays;
    private Integer totalDays;
    private LocalDate lastCheckDate;
    private String pledge;   // 참여 시 공개 각오 한마디 (최대 100자)
}
