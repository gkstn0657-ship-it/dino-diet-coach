package com.ssafy.nyamnyam.domain.water;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** 물 섭취 기록 (사용자·날짜별 잔 수). 메인 물섭취 저장 + 물 챌린지 자동 인증의 근거. */
@Service
@RequiredArgsConstructor
public class WaterService {

    private final WaterMapper waterMapper;

    private static final int MAX_CUPS = 30; // 과대 입력 방어

    /** 오늘 잔 수 */
    public int getToday(Integer mno) {
        return cupsOn(mno, LocalDate.now().toString());
    }

    /** 오늘 잔 수 저장(0~30 범위로 정규화) */
    @Transactional
    public int setToday(Integer mno, int cups) {
        int c = Math.max(0, Math.min(MAX_CUPS, cups));
        waterMapper.upsert(mno, LocalDate.now().toString(), c);
        return c;
    }

    /** 특정 날짜 잔 수 (챌린지 평가용) */
    public int cupsOn(Integer mno, String date) {
        Integer c = waterMapper.findCups(mno, date);
        return c == null ? 0 : c;
    }
}
