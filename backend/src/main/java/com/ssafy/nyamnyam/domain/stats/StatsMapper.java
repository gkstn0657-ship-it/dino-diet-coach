package com.ssafy.nyamnyam.domain.stats;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StatsMapper {
    /** 특정 날짜의 식단을 회원별로 집계 (diets → 통계 행) */
    List<DailyStat> aggregateByDate(@Param("date") String date);

    int deleteByDate(@Param("date") String date);

    int insert(DailyStat stat);

    /** 회원의 최근 통계 */
    List<DailyStat> findByMember(@Param("mno") Integer mno);

    /** 회원의 기간별 일자 집계 (diets 원본에서 직접, 날짜 오름차순) */
    List<DailyStat> rangeByMember(@Param("mno") Integer mno,
                                  @Param("from") String from,
                                  @Param("to") String to);
}
