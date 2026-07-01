package com.ssafy.nyamnyam.domain.food;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FoodMapper {
    List<Food> search(@Param("keyword") String keyword);

    // 식품 DB 갱신(로컬 적재)용 — 음식명 기준 upsert로 중복 누적 방지
    Food findByName(@Param("name") String name);
    int insert(Food food);
    int updateNutrition(Food food);
}
