package com.ssafy.nyamnyam.tool;

import com.ssafy.nyamnyam.domain.challenge.ChallengeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

/**
 * 현재 로그인한 회원의 챌린지 상태 조회 Tool (조회 전용).
 *
 * 보안 설계: mno 를 생성자 주입으로 받아 본인 챌린지만 조회한다.
 * 조건 평가/진행률 계산은 기존 {@link ChallengeService} 로직을 그대로 재사용한다(단일 출처).
 * 인증·참여·삭제 등 상태 변경은 하지 않는다.
 */
@Slf4j
public class ChallengeTool {

    private final ChallengeService challengeService;
    private final Integer mno;

    public ChallengeTool(ChallengeService challengeService, Integer mno) {
        this.challengeService = challengeService;
        this.mno = mno;
    }

    @Tool(description = """
            [역할]
            - 현재 로그인한 사용자가 참여 중인 챌린지의 진행 상태를 조회한다.
            [입력]
            - 없음 (항상 본인 챌린지만 조회)
            [출력]
            - 챌린지별 제목, 진행률(doneDays/totalDays), 조건, 오늘 조건 충족 여부, 오늘 인증 여부
            [정책]
            - 조회 전용. 인증/참여/수정/삭제 같은 상태 변경은 하지 않는다.
            - "챌린지 어때?", "오늘 인증돼?", "조건 충족했어?" 같은 질문에 사용한다.
            """)
    public String getMyChallengeStatus() {
        log.debug("### TOOL CALLED: getMyChallengeStatus, mno={}", mno);
        return challengeService.myChallengeStatusText(mno);
    }
}
