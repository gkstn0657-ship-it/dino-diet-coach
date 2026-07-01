package com.ssafy.nyamnyam.domain.food;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 식품 데이터 갱신 구조.
 *
 * 식약처 식품영양성분 API에는 '수정일자/버전'을 안정적으로 제공하는 필드가 없어
 * 증분 동기화가 어렵다. 그래서 명세 허용대로 '월 1회 정기 갱신 + 수동 갱신 API' 방식으로 구성한다.
 *
 * 설계 원칙
 * - 로컬 우선 검색: 평소 검색은 로컬 foods 테이블을 먼저 본다(FoodController).
 * - 중복 방지: 음식명(name) 기준 upsert 로 같은 음식이 계속 쌓이지 않게 한다.
 * - 스냅샷 보존: 과거 식단 기록(diet_foods)은 저장 시점 영양정보를 따로 갖고 있어 갱신 영향 없음.
 * - 안전성: API 미설정/실패 시 기존 DB를 건드리지 않으며, 일부 키워드 실패가 전체 실패로 번지지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodSyncService {

    private final FoodApiService foodApiService;
    private final FoodMapper foodMapper;
    private final JdbcTemplate jdbcTemplate;

    /** 정기 갱신에 사용할 대표 음식 키워드 (자주 검색되는 카테고리) */
    private static final String[] SYNC_KEYWORDS = {
            "밥", "김치", "닭", "계란", "두부", "우유", "빵", "고구마", "감자", "바나나",
            "사과", "샐러드", "연어", "참치", "오트밀", "요거트", "아몬드", "두유", "국", "찌개",
    };

    private static final String META_KEY = "food_last_sync"; // 마지막 갱신일 저장 키

    /** 매월 1일 00:00(서버 시간) 정기 갱신 */
    @Scheduled(cron = "0 0 0 1 * *")
    public void scheduledSync() {
        if (!foodApiService.isConfigured()) {
            log.info("[식품갱신] API 키 미설정 - 정기 갱신 생략(로컬 데이터 유지)");
            return;
        }
        Map<String, Object> r = sync();
        log.info("[식품갱신] 매월 정기 갱신 완료: {}", r);
    }

    /**
     * 식약처 API 로 대표 키워드를 조회해 로컬 foods 에 반영(upsert).
     * @return { configured, inserted, updated, failedKeywords }
     */
    public Map<String, Object> sync() {
        Map<String, Object> summary = new LinkedHashMap<>();
        if (!foodApiService.isConfigured()) {
            summary.put("configured", false);
            summary.put("message", "식약처 API 키가 설정되지 않아 갱신을 건너뜁니다. 로컬 데이터는 유지됩니다.");
            return summary;
        }

        int inserted = 0, updated = 0, failed = 0;
        for (String keyword : SYNC_KEYWORDS) {
            try {
                List<Food> items = foodApiService.search(keyword);
                if (items == null) { // API 오류 → 이 키워드만 건너뜀(기존 DB 보존)
                    failed++;
                    continue;
                }
                int[] c = upsertAll(items);
                inserted += c[0];
                updated += c[1];
            } catch (Exception e) {
                // 부분 실패가 전체 갱신 실패로 이어지지 않도록 방어
                failed++;
                log.warn("[식품갱신] '{}' 키워드 처리 실패: {}", keyword, e.getMessage());
            }
        }
        recordLastSync(); // 마지막 갱신 시점 기록(운영 가시성용)
        summary.put("configured", true);
        summary.put("inserted", inserted);
        summary.put("updated", updated);
        summary.put("failedKeywords", failed);
        return summary;
    }

    /** 마지막 갱신일을 오늘로 기록(upsert) — 운영 가시성용 */
    private void recordLastSync() {
        try {
            jdbcTemplate.update(
                    "INSERT INTO app_meta (meta_key, meta_value) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE meta_value = VALUES(meta_value)",
                    META_KEY, LocalDate.now().toString());
        } catch (Exception e) {
            log.warn("[식품갱신] 마지막 갱신일 기록 실패: {}", e.getMessage());
        }
    }

    /**
     * 음식 목록을 로컬에 upsert. 이름 기준으로 있으면 영양정보만 갱신, 없으면 추가.
     * @return [inserted, updated]
     */
    public int[] upsertAll(List<Food> foods) {
        int inserted = 0, updated = 0;
        if (foods == null) return new int[]{0, 0};
        for (Food f : foods) {
            try {
                if (f == null || f.getName() == null || f.getName().isBlank()) continue;
                Food exist = foodMapper.findByName(f.getName().trim());
                if (exist == null) {
                    f.setName(f.getName().trim());
                    foodMapper.insert(f);
                    inserted++;
                } else {
                    exist.setKcal(f.getKcal());
                    exist.setProtein(f.getProtein());
                    exist.setCarbs(f.getCarbs());
                    exist.setFat(f.getFat());
                    foodMapper.updateNutrition(exist);
                    updated++;
                }
            } catch (Exception e) {
                // 개별 음식 실패는 무시(나머지는 계속 반영)
                log.debug("[식품갱신] 음식 upsert 실패: {} - {}", f == null ? null : f.getName(), e.getMessage());
            }
        }
        return new int[]{inserted, updated};
    }
}
