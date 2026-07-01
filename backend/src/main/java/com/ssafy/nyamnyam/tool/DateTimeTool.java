package com.ssafy.nyamnyam.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 현재 날짜/시간 조회 Tool (서버 기준).
 * "오늘/내일/이번 주" 같은 상대 날짜 질문을 LLM 이 정확히 해석하도록 기준 시각을 제공한다.
 * 사용자 데이터와 무관하므로 싱글톤(@Component).
 */
@Slf4j
@Component
public class DateTimeTool {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE HH:mm", Locale.KOREAN);

    @Tool(description = """
            [역할]
            - 서버 기준 현재 날짜·요일·시간·표준시(timezone)를 반환한다.
            [입력]
            - 없음
            [출력]
            - "[현재 날짜/시간] 2026-06-22 월요일 14:30 (Asia/Seoul)"
            [정책]
            - '오늘 날짜', '내일 기준', '이번 주', '오늘 식단' 등 상대 날짜·시간 질문을 처리하기 전에 호출한다.
            - 날짜를 추측하지 말고 이 도구의 값을 사용한다.
            """)
    public String getCurrentDateTime() {
        log.debug("### TOOL CALLED: getCurrentDateTime");
        return "[현재 날짜/시간] " + ZonedDateTime.now(ZONE).format(FMT) + " (Asia/Seoul)";
    }
}
