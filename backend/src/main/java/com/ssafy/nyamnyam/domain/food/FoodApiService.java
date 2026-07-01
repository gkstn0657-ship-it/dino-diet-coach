package com.ssafy.nyamnyam.domain.food;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 식약처 식품영양성분 DB(data.go.kr) 연동.
 * FOOD_NM_KR 로 검색하여 1인분(SERVING_SIZE, 보통 100g) 기준 영양정보를 반환.
 * 필드: AMT_NUM1=칼로리, AMT_NUM3=단백질, AMT_NUM4=지방, AMT_NUM6=탄수화물
 */
@Slf4j
@Service
public class FoodApiService {

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String serviceKey;
    private final String baseUrl;

    public FoodApiService(@Value("${food.api.base-url}") String baseUrl,
                          @Value("${food.api.key}") String serviceKey,
                          ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // data.go.kr 공공 API는 느리고 들쭉날쭉해서 읽기 타임아웃을 넉넉히 둔다.
        // (결과 많은 검색어는 3초를 자주 초과 → null 폴백으로 떨어지던 문제. 프론트 식단검색 15s·AI 코칭 120s 내 충분)
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(10000);
        this.client = RestClient.builder().requestFactory(factory).build();
        this.serviceKey = serviceKey;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    /** 식약처 API 키가 실제로 설정되어 있는지(더미/미설정이면 false) */
    public boolean isConfigured() {
        return serviceKey != null && !serviceKey.isBlank()
                && !serviceKey.equalsIgnoreCase("CHANGEME")
                && !serviceKey.equalsIgnoreCase("local-dummy");
    }

    @SuppressWarnings("unchecked")
    public List<Food> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return new ArrayList<>();
        try {
            // 키워드를 직접 UTF-8 percent-encoding 해서 절대 URI 로 보낸다.
            // (Spring 의 .queryParam() 자동 인코딩이 일부 한글에서 식약처와 안 맞던 문제 회피 — curl 과 동일하게 전송)
            String url = baseUrl
                    + "?serviceKey=" + serviceKey
                    + "&pageNo=1&numOfRows=20&type=json"
                    + "&FOOD_NM_KR=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            byte[] bytes = client.get()
                    .uri(URI.create(url)) // 절대 URI → 추가 인코딩 없이 그대로 전송
                    .retrieve()
                    .body(byte[].class);

            Map<String, Object> res = objectMapper.readValue(bytes, Map.class);
            Map<String, Object> header = (Map<String, Object>) res.get("header");
            if (header != null && !"00".equals(header.get("resultCode"))) {
                log.warn("식약처 API 오류: {}", header.get("resultMsg"));
                return null; // 컨트롤러에서 로컬 폴백
            }
            Map<String, Object> body = (Map<String, Object>) res.get("body");
            if (body == null) return new ArrayList<>();
            Object itemsObj = body.get("items");

            List<Map<String, Object>> items = new ArrayList<>();
            if (itemsObj instanceof List<?> list) {
                for (Object o : list) if (o instanceof Map<?, ?> m) items.add((Map<String, Object>) m);
            } else if (itemsObj instanceof Map<?, ?> m) {
                items.add((Map<String, Object>) m);
            }

            log.debug("[식약처] keyword='{}' resultCode={} totalCount={} parsedItems={}",
                    keyword, header == null ? "?" : header.get("resultCode"),
                    body.get("totalCount"), items.size());

            List<Food> result = new ArrayList<>();
            for (Map<String, Object> it : items) {
                Food f = new Food();
                f.setName((String) it.get("FOOD_NM_KR"));
                f.setKcal(amt(it.get("AMT_NUM1")));
                f.setProtein(amt(it.get("AMT_NUM3")));
                f.setFat(amt(it.get("AMT_NUM4")));
                f.setCarbs(amt(it.get("AMT_NUM6")));
                result.add(f);
            }
            return result;
        } catch (Exception e) {
            log.warn("식약처 API 호출 실패, 로컬 폴백: {}", e.getMessage());
            return null;
        }
    }

    /** "137.000" 같은 문자열 → 반올림 정수, 빈값/오류 → 0 */
    private int amt(Object v) {
        if (v == null) return 0;
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) return 0;
        try {
            return (int) Math.round(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
