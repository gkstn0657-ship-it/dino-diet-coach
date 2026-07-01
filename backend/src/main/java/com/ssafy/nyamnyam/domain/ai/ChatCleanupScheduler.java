package com.ssafy.nyamnyam.domain.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AI 코치 대화 보존 정책 배치.
 * 매일 새벽 2시에 90일 지난 대화를 삭제해 저장 공간을 일정 수준으로 유지한다.
 * (1만 명 기준 하루 약 200MB 적재 → 90일 보존 시 최대 약 18GB에서 안정화)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatCleanupScheduler {

    private static final int RETENTION_DAYS = 90;

    private final AiService aiService;

    @Scheduled(cron = "0 0 2 * * *")
    public void purgeOldChats() {
        int deleted = aiService.purgeOldChats(RETENTION_DAYS);
        log.info("[스케줄] AI 대화 정리 배치 완료 - {}일 경과 {}건 삭제", RETENTION_DAYS, deleted);
    }
}
