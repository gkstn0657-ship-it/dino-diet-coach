package com.ssafy.nyamnyam.domain.challenge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 식단 조건 챌린지 자동 인증 마감 배치.
 * 매일 23:50 — 그날 '하루 총합'이 조건을 충족한 참여자를 일괄 자동 기록한다.
 * 인증 확정은 오직 이 마감 배치(또는 개발용 /auto-check-run)에서만 일어난다.
 * 식단 저장·수정·상세 조회·참여 시점에는 인증 상태를 변경하지 않는다
 * (하루 중간 부분 총합으로 확정하면 이후 추가 식단이 반영되지 않기 때문).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeAutoCheckScheduler {

    private final ChallengeService challengeService;

    @Scheduled(cron = "0 59 23 * * *")
    public void autoCheckAll() {
        // 시작했는데 참여자 0명인 챌린지 자동 삭제
        int purged = challengeService.purgeEmptyStartedChallenges();
        if (purged > 0) log.info("[스케줄] 참여자 0명으로 시작된 챌린지 {}개 자동 삭제", purged);
        int recorded = challengeService.autoCheckAllParticipants();
        log.info("[스케줄] 챌린지 자동 인증 마감 배치 완료 - {}건 기록", recorded);
    }
}
