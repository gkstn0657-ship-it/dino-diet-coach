package com.ssafy.nyamnyam.domain.ai;

import com.ssafy.nyamnyam.common.ApiResponse;
import com.ssafy.nyamnyam.common.CustomException;
import com.ssafy.nyamnyam.security.LoginMember;
import com.ssafy.nyamnyam.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    public record MessageRequest(String message) {}

    /**
     * coach: powerrex(벌크업) | slimdino(다이어트) | balanceno(유지). 미지정 시 회원 목표 기반 자동 매핑.
     * 대화 히스토리는 서버 DB에 저장·관리되므로 클라이언트가 보낼 필요 없음.
     */
    public record CoachRequest(String message, String coach) {}

    /** F116. AI 식단 종합 분석 (period: today | week) */
    @PostMapping("/diet-analysis")
    public ApiResponse<Map<String, Object>> dietAnalysis(@LoginMember LoginUser user,
                                                         @RequestBody(required = false) Map<String, Object> body) {
        String period = (body != null && body.get("period") != null)
                ? String.valueOf(body.get("period")) : "today";
        return ApiResponse.ok(aiService.analyzeDiet(user.email(), period));
    }

    /** F116(심화). 사진 → 음식 인식·영양 분석 (비전) */
    @PostMapping("/analyze-photo")
    public ApiResponse<Map<String, Object>> analyzePhoto(@RequestParam MultipartFile photo) {
        if (photo == null || photo.isEmpty()) throw CustomException.badRequest("사진이 필요합니다.");
        try {
            return ApiResponse.ok(aiService.analyzePhoto(photo.getBytes(), photo.getContentType()));
        } catch (java.io.IOException e) {
            throw CustomException.badRequest("사진을 읽지 못했습니다.");
        }
    }

    public record EstimateRequest(String name) {}

    /** 음식명 → 1인분 영양 추정 (검색 결과가 없을 때 보조 등록용) */
    @PostMapping("/estimate-nutrition")
    public ApiResponse<Map<String, Object>> estimateNutrition(@RequestBody EstimateRequest req) {
        return ApiResponse.ok(aiService.estimateNutrition(req.name()));
    }

    /** F117. AI 운동 코칭 (3종 코치 에이전트, 대화는 DB에 저장되어 멀티턴 유지) */
    @PostMapping("/workout-coach")
    public ApiResponse<String> workoutCoach(@LoginMember LoginUser user, @RequestBody CoachRequest req) {
        return ApiResponse.ok(aiService.workoutCoach(user.email(), req.message(), req.coach()));
    }

    /** 코치별 저장된 대화 조회 (채팅방 입장/코치 전환 시 복원) */
    @GetMapping("/chat-history")
    public ApiResponse<java.util.List<ChatMessage>> chatHistory(@LoginMember LoginUser user,
                                                                @RequestParam(required = false) String coach) {
        return ApiResponse.ok(aiService.getChatHistory(user.email(), coach));
    }

    /** 가이드 챗봇 */
    @PostMapping("/guide")
    public ApiResponse<String> guide(@RequestBody MessageRequest req) {
        return ApiResponse.ok(aiService.guide(req.message()));
    }
}
