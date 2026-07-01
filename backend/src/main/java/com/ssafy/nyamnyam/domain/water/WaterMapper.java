package com.ssafy.nyamnyam.domain.water;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WaterMapper {
    /** 특정 날짜 물 섭취 잔 수 (없으면 null) */
    Integer findCups(@Param("mno") Integer mno, @Param("date") String date);

    /** 날짜별 잔 수 upsert (PK: mno+log_date) */
    int upsert(@Param("mno") Integer mno, @Param("date") String date, @Param("cups") int cups);
}
