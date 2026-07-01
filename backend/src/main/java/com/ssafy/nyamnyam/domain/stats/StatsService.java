package com.ssafy.nyamnyam.domain.stats;

import com.ssafy.nyamnyam.common.CustomException;
import com.ssafy.nyamnyam.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsMapper statsMapper;
    private final MemberService memberService;

    /** 배치 본체: 특정 날짜 식단을 회원별로 집계해 통계 테이블에 적재 (재실행 시 갱신) */
    @Transactional
    public int aggregate(LocalDate date) {
        String d = date.toString();
        statsMapper.deleteByDate(d);
        List<DailyStat> stats = statsMapper.aggregateByDate(d);
        for (DailyStat s : stats) {
            s.setStatDate(date);
            statsMapper.insert(s);
        }
        log.info("[배치] {} 영양 통계 집계: {}건", d, stats.size());
        return stats.size();
    }

    /** 내 최근 통계 조회 */
    public List<DailyStat> myStats(String email) {
        Integer mno = memberService.getByEmail(email).getMno();
        return statsMapper.findByMember(mno);
    }

    /** 기간별(from~to) 일별 칼로리/매크로 추이 — diets 원본 직접 집계 */
    public List<DailyStat> range(String email, String from, String to) {
        LocalDate f, t;
        try {
            f = LocalDate.parse(from);
            t = LocalDate.parse(to);
        } catch (Exception e) {
            throw CustomException.badRequest("날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)");
        }
        if (f.isAfter(t)) { LocalDate tmp = f; f = t; t = tmp; }   // 역순이면 교정
        if (f.isBefore(t.minusDays(366))) f = t.minusDays(366);    // 과도한 범위 제한(최대 약 1년)
        Integer mno = memberService.getByEmail(email).getMno();
        return statsMapper.rangeByMember(mno, f.toString(), t.toString());
    }
}
