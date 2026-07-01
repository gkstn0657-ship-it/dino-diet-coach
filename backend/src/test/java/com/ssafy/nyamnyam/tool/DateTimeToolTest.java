package com.ssafy.nyamnyam.tool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeToolTest {

    @Test
    @DisplayName("현재 날짜/시간 Tool은 서버 기준 날짜·timezone을 반환한다")
    void getCurrentDateTime_returnsServerDate() {
        String out = new DateTimeTool().getCurrentDateTime();

        String todaySeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate().toString();
        assertThat(out)
                .contains("[현재 날짜/시간]")
                .contains("(Asia/Seoul)")
                .contains(todaySeoul); // yyyy-MM-dd 가 들어있어야 함
        // 연도도 포함 (날짜 추측이 아닌 실제 서버 날짜)
        assertThat(out).contains(String.valueOf(LocalDate.now(ZoneId.of("Asia/Seoul")).getYear()));
    }
}
