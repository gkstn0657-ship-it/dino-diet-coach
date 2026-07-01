package com.ssafy.nyamnyam.domain.diet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.nyamnyam.common.ApiResponse;
import com.ssafy.nyamnyam.common.FileStorage;
import com.ssafy.nyamnyam.domain.member.MemberService;
import com.ssafy.nyamnyam.security.LoginMember;
import com.ssafy.nyamnyam.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diets")
@RequiredArgsConstructor
public class DietController {

    private final DietService dietService;
    private final MemberService memberService;
    private final FileStorage fileStorage;
    private final ObjectMapper objectMapper;

    private Integer mno(LoginUser user) {
        return memberService.getByEmail(user.email()).getMno();
    }

    /** 식단 목록 (F102) */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(
            @LoginMember LoginUser user,
            @RequestParam(required = false) String meal,
            @RequestParam(required = false) String date) {
        return ApiResponse.ok(dietService.list(mno(user), meal, date));
    }

    /** 식단 상세 (F102) */
    @GetMapping("/{dno}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Integer dno) {
        return ApiResponse.ok(dietService.detail(dno));
    }

    /** 식단 작성 (F101) - 사진 멀티파트 */
    @PostMapping
    public ApiResponse<Integer> create(
            @LoginMember LoginUser user,
            @RequestParam(required = false) String title,   // 선택 입력 — 비우면 서버가 자동 생성
            @RequestParam(defaultValue = "lunch") String meal,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Integer kcal,
            @RequestParam(required = false) String foods,   // DB에서 선택한 음식 목록(JSON 배열)
            @RequestParam(required = false) MultipartFile photo) {
        String photoUrl = fileStorage.save(photo);
        LocalDate d = (date != null && !date.isBlank()) ? LocalDate.parse(date) : null;
        List<DietFood> foodList = parseFoods(foods);
        Integer dno = dietService.create(mno(user), title, meal, d, kcal, photoUrl, foodList);
        return ApiResponse.ok("식단 저장 완료", dno);
    }

    private List<DietFood> parseFoods(String foodsJson) {
        if (foodsJson == null || foodsJson.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(foodsJson, new TypeReference<List<DietFood>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public record UpdateRequest(String title, String meal, String date, Integer kcal,
                                List<DietFood> foods) {}

    /** 식단 수정 (F103) — foods 전달 시 음식 구성 교체 */
    @PutMapping("/{dno}")
    public ApiResponse<Void> update(@PathVariable Integer dno, @RequestBody UpdateRequest req) {
        dietService.update(dno, req.title(), req.meal(), req.date(), req.kcal(), req.foods());
        return ApiResponse.ok("수정 완료", null);
    }

    /** 식단 삭제 (F104) */
    @DeleteMapping("/{dno}")
    public ApiResponse<Void> delete(@PathVariable Integer dno) {
        dietService.delete(dno);
        return ApiResponse.ok("삭제 완료", null);
    }
}
