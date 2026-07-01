package com.ssafy.nyamnyam.domain.diet;

import com.ssafy.nyamnyam.common.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DietService {

    private final DietMapper dietMapper;

    private static final Map<String, String[]> MEAL = Map.of(
            "breakfast", new String[]{"아침", "🥣"},
            "lunch", new String[]{"점심", "🥗"},
            "dinner", new String[]{"저녁", "🍗"},
            "snack", new String[]{"간식", "🍎"}
    );

    private String mealLabel(String meal) {
        return MEAL.getOrDefault(meal, new String[]{"식사", "🍽️"})[0];
    }

    private String mealThumb(String meal) {
        return MEAL.getOrDefault(meal, new String[]{"식사", "🍽️"})[1];
    }

    /** 식단 작성 (F101) - DB에서 선택한 음식 목록(foods) 저장, 없으면 kcal 사용 */
    @Transactional
    public Integer create(Integer mno, String title, String meal, LocalDate date,
                          Integer kcal, String photoUrl, List<DietFood> foods) {
        LocalDate eaten = date != null ? date : LocalDate.now();

        // 같은 날짜+끼니는 하나의 기록만 — 이미 있으면 자동 병합하지 않고
        // 기존 dno 와 함께 409 로 막아 수정 화면으로 유도한다.
        Diet existing = dietMapper.findByMemberAndDateAndMeal(mno, eaten.toString(), meal);
        if (existing != null) {
            throw CustomException.conflict(
                    "이미 " + eaten + " " + mealLabel(meal) + " 식단이 등록되어 있어요. 기존 기록을 수정해 주세요.",
                    existing.getDno());
        }

        boolean hasFoods = foods != null && !foods.isEmpty();

        Diet diet = new Diet();
        diet.setMno(mno);
        diet.setTitle(resolveTitle(title, meal, foods, hasFoods));
        diet.setMeal(meal);
        diet.setEatenDate(eaten);
        diet.setPhotoUrl(photoUrl);
        // 음식이 선택됐으면 음식별 영양값 합산, 아니면 입력 kcal 기반 추정
        applyMacros(diet, hasFoods, foods, kcal);
        dietMapper.insert(diet);

        if (hasFoods) {
            for (DietFood f : foods) {
                f.setDno(diet.getDno());
                normalizeFood(f);
                dietMapper.insertFood(f);
            }
        } else {
            // 음식 미선택 시 대표 음식 1건(자동 생성된 식단명) — 식단 추정 매크로를 그대로 부여
            DietFood f = new DietFood();
            f.setDno(diet.getDno());
            f.setName(diet.getTitle());
            f.setKcal(diet.getTotalKcal());
            f.setProtein(diet.getProtein());
            f.setCarbs(diet.getCarbs());
            f.setFat(diet.getFat());
            dietMapper.insertFood(f);
        }
        // 챌린지 자동 인증은 식단 저장 시점이 아니라 '하루 마감 배치'에서만 평가한다.
        // (하루 중간 부분 총합으로 인증을 확정하면 이후 추가 식단이 반영되지 않는 문제 방지)
        return diet.getDno();
    }

    /** 식단 목록 (F102) - 프론트 카드용 가공 */
    public List<Map<String, Object>> list(Integer mno, String meal, String date) {
        DateTimeFormatter md = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> result = new ArrayList<>();
        for (Diet d : dietMapper.findByMember(mno, meal, date)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("dno", d.getDno());
            m.put("thumb", mealThumb(d.getMeal()));
            m.put("mealLabel", mealLabel(d.getMeal()));
            m.put("date", d.getEatenDate().format(md));
            m.put("meal", d.getMeal());
            m.put("title", d.getTitle());
            m.put("kcal", d.getTotalKcal());
            m.put("protein", nz(d.getProtein()));
            m.put("carbs", nz(d.getCarbs()));
            m.put("fat", nz(d.getFat()));
            result.add(m);
        }
        return result;
    }

    /** 식단 상세 (F102) + 영양 분석(F105) */
    public Map<String, Object> detail(Integer dno) {
        Diet d = dietMapper.findById(dno);
        if (d == null) throw CustomException.notFound("식단을 찾을 수 없습니다.");
        List<DietFood> foods = dietMapper.findFoods(dno);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dno", d.getDno());
        m.put("title", d.getTitle());
        m.put("meal", d.getMeal());
        m.put("mealLabel", mealLabel(d.getMeal()));
        m.put("date", d.getEatenDate().toString());
        m.put("kcal", d.getTotalKcal());

        List<Map<String, Object>> foodList = new ArrayList<>();
        for (DietFood f : foods) {
            Map<String, Object> fm = new LinkedHashMap<>();
            fm.put("name", f.getName());
            fm.put("kcal", nz(f.getKcal()));
            fm.put("protein", nz(f.getProtein()));
            fm.put("carbs", nz(f.getCarbs()));
            fm.put("fat", nz(f.getFat()));
            fm.put("source", f.getSource() == null ? "DB" : f.getSource());
            foodList.add(fm);
        }
        m.put("foods", foodList);

        int p = nz(d.getProtein()), c = nz(d.getCarbs()), fa = nz(d.getFat());
        int total = Math.max(1, p + c + fa);
        m.put("macros", List.of(
                macro("단백질", p, total),
                macro("탄수화물", c, total),
                macro("지방", fa, total)
        ));
        return m;
    }

    private Map<String, Object> macro(String label, int value, int total) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("label", label);
        m.put("value", value);
        m.put("pct", Math.round(value * 100.0 / total));
        return m;
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }

    /**
     * 식단의 총 칼로리·매크로를 산정한다.
     * - 음식 목록이 있으면: 음식별 kcal/protein/carbs/fat 합계를 우선 사용
     * - 음식 없이 kcal만 입력했으면: kcal 기반 추정치(P 20% / C 50% / F 30%) 사용
     */
    private void applyMacros(Diet diet, boolean hasFoods, List<DietFood> foods, Integer kcal) {
        if (hasFoods) {
            diet.setTotalKcal(foods.stream().mapToInt(f -> nz(f.getKcal())).sum());
            diet.setProtein(foods.stream().mapToInt(f -> nz(f.getProtein())).sum());
            diet.setCarbs(foods.stream().mapToInt(f -> nz(f.getCarbs())).sum());
            diet.setFat(foods.stream().mapToInt(f -> nz(f.getFat())).sum());
        } else {
            int k = kcal != null ? kcal : 0;
            diet.setTotalKcal(k);
            diet.setProtein((int) Math.round(k * 0.2 / 4));
            diet.setCarbs((int) Math.round(k * 0.5 / 4));
            diet.setFat((int) Math.round(k * 0.3 / 9));
        }
    }

    /**
     * 식단 제목 자동 생성 (제목은 선택 입력).
     * - 입력 제목이 있으면 그대로 사용
     * - 음식 1개: 음식명 / 여러 개: "대표음식 외 N개"
     * - 음식 없이 kcal만: "점심 식단" 처럼 끼니 기반 자동 생성
     */
    private String resolveTitle(String title, String meal, List<DietFood> foods, boolean hasFoods) {
        if (title != null && !title.isBlank()) return title.trim();
        if (hasFoods) {
            String first = foods.get(0).getName();
            return foods.size() == 1 ? first : first + " 외 " + (foods.size() - 1) + "개";
        }
        return mealLabel(meal) + " 식단";
    }

    /** 저장 전 음식의 null 영양값을 0으로 정규화 (NPE·NULL 컬럼 방지) + 출처 기본값 */
    private void normalizeFood(DietFood f) {
        f.setKcal(nz(f.getKcal()));
        f.setProtein(nz(f.getProtein()));
        f.setCarbs(nz(f.getCarbs()));
        f.setFat(nz(f.getFat()));
        // 출처 화이트리스트(DB/AI/PHOTO) 외 값은 DB 로 정규화 — 클라이언트 값 신뢰 방지
        String s = f.getSource();
        f.setSource(("AI".equals(s) || "PHOTO".equals(s)) ? s : "DB");
    }

    /** 식단 수정 (F103) — foods 목록이 전달되면 음식 구성을 통째로 교체하고 칼로리를 합산 */
    @Transactional
    public void update(Integer dno, String title, String meal, String dateStr, Integer kcal,
                       List<DietFood> foods) {
        boolean hasFoods = foods != null && !foods.isEmpty();

        Diet diet = new Diet();
        diet.setDno(dno);
        diet.setTitle(resolveTitle(title, meal, foods, hasFoods));
        diet.setMeal(meal);
        diet.setEatenDate((dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr) : LocalDate.now());
        applyMacros(diet, hasFoods, foods, kcal);
        dietMapper.update(diet);

        // 음식 구성 교체 (foods 가 전달된 경우에만 — null이면 기존 구성 유지)
        if (foods != null) {
            dietMapper.deleteFoods(dno);
            if (hasFoods) {
                for (DietFood f : foods) {
                    f.setDno(dno);
                    normalizeFood(f);
                    dietMapper.insertFood(f);
                }
            } else {
                DietFood f = new DietFood();
                f.setDno(dno);
                f.setName(diet.getTitle());
                f.setKcal(diet.getTotalKcal());
                f.setProtein(diet.getProtein());
                f.setCarbs(diet.getCarbs());
                f.setFat(diet.getFat());
                dietMapper.insertFood(f);
            }
        }
        // 자동 인증은 하루 마감 배치에서만 평가한다 (create() 주석 참고).
    }

    @Transactional
    public void delete(Integer dno) {
        dietMapper.delete(dno);
    }
}
