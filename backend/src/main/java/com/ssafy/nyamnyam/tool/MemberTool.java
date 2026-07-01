package com.ssafy.nyamnyam.tool;

import com.ssafy.nyamnyam.domain.member.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

/**
 * 현재 로그인한 회원 본인의 건강 프로필 조회 Tool (내부 데이터).
 *
 * 보안 설계: 회원 식별자(mno/email)를 LLM 입력값으로 받지 않는다.
 * 인증된 Member 를 요청 시점에 생성자로 주입받아, LLM 이 타인 정보를 조회하도록 유도하는 것을 원천 차단한다.
 * (그래서 @Component 싱글톤이 아니라 요청마다 새로 생성한다.)
 */
@Slf4j
public class MemberTool {

    private final Member me;

    public MemberTool(Member me) {
        this.me = me;
    }

    @Tool(description = """
            [역할]
            - 현재 로그인한 사용자 본인의 건강 프로필(키, 몸무게, 목표, 질환)을 조회한다.
            [입력]
            - 없음 (항상 본인 정보만 조회한다)
            [출력]
            - 키 / 몸무게 / 목표 / 질환 요약
            [정책]
            - 운동·식단 추천 등 사용자 맥락이 필요한 판단 전에 먼저 호출한다.
            """)
    public String getMyProfile() {
        log.debug("### TOOL CALLED: getMyProfile, mno={}", me.getMno());
        return String.format(
                "[내 프로필]\n- 키: %s\n- 몸무게: %s\n- 목표: %s\n- 질환: %s",
                me.getHeight() == null ? "미입력" : me.getHeight() + "cm",
                me.getWeight() == null ? "미입력" : me.getWeight() + "kg",
                val(me.getGoal()),
                val(me.getDisease()));
    }

    private String val(String s) {
        return (s == null || s.isBlank()) ? "미입력" : s;
    }
}
