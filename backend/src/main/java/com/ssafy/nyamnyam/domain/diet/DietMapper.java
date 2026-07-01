package com.ssafy.nyamnyam.domain.diet;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DietMapper {
    int insert(Diet diet);
    int insertFood(DietFood food);
    List<Diet> findByMember(@Param("mno") Integer mno,
                            @Param("meal") String meal,
                            @Param("date") String date);
    /** 같은 날짜+끼니 단일 기록 정책: 슬롯 중복 검사용 (없으면 null) */
    Diet findByMemberAndDateAndMeal(@Param("mno") Integer mno,
                                    @Param("date") String date,
                                    @Param("meal") String meal);
    Diet findById(@Param("dno") Integer dno);
    List<DietFood> findFoods(@Param("dno") Integer dno);
    int update(Diet diet);
    int delete(@Param("dno") Integer dno);
    int deleteFoods(@Param("dno") Integer dno);   // 수정 시 음식 구성 교체용
    int countAll();                                // 운영 콘솔 요약 통계용 (전체 식단 기록 수)
}
