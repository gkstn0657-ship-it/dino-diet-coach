package com.ssafy.nyamnyam.domain.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 영양 통계 배치 스케줄러.
 * 매일 새벽 1시에 '어제' 식단을 집계한다. (배운 batch_lab의 스케줄 잡 패턴)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NutritionScheduler {

    private final StatsService statsService;

    @Scheduled(cron = "0 0 1 * * *")
    public void aggregateYesterday() {
        LocalDate target = LocalDate.now().minusDays(1);
        int count = statsService.aggregate(target);
        log.info("[스케줄] 일일 영양 통계 배치 완료 - {} ({}건)", target, count);
    }
}
