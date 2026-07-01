package com.ssafy.nyamnyam.tool;

import com.ssafy.nyamnyam.domain.diet.Diet;
import com.ssafy.nyamnyam.domain.diet.DietMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDate;
import java.util.List;

/**
 * 현재 로그인한 회원의 '오늘' 식단 조회 Tool (내부 데이터).
 *
 * 보안 설계: mno 를 LLM 입력값이 아닌 생성자 주입으로 받아 타인 식단 조회를 차단한다.
 * 실제 조회는 기존 {@link DietMapper} 에 위임한다. (F1201 / F1203)
 */
@Slf4j
public class DietTool {

    private final DietMapper dietMapper;
    private final Integer mno;

    public DietTool(DietMapper dietMapper, Integer mno) {
        this.dietMapper = dietMapper;
        this.mno = mno;
    }

    @Tool(description = """
            [역할]
            - 현재 로그인한 사용자가 '오늘' 기록한 식단과 총 섭취량을 조회한다.
            [입력]
            - 없음 (항상 본인의 오늘 식단만 조회한다)
            [출력]
            - 오늘 끼니 목록 + 총 섭취 칼로리/단백질/끼니 수
            [정책]
            - '오늘 먹어도 되는지', '얼마나 더 먹어도 되는지' 등 판단에 활용한다.
            - 기록이 없으면 "오늘 기록된 식단이 없습니다." 라고만 답한다.
            """)
    public String getTodayDiets() {
        log.debug("### TOOL CALLED: getTodayDiets, mno={}", mno);

        List<Diet> diets = dietMapper.findByMember(mno, null, LocalDate.now().toString());
        if (diets == null || diets.isEmpty()) {
            return "오늘 기록된 식단이 없습니다.";
        }

        int kcal = diets.stream().mapToInt(d -> nz(d.getTotalKcal())).sum();
        int protein = diets.stream().mapToInt(d -> nz(d.getProtein())).sum();

        StringBuilder sb = new StringBuilder("[오늘 식단 기록]\n");
        for (Diet d : diets) {
            sb.append("- ").append(d.getMeal()).append(": ").append(d.getTitle())
                    .append(" (").append(nz(d.getTotalKcal())).append("kcal)\n");
        }
        sb.append(String.format("→ 합계: %dkcal, 단백질 %dg (%d끼)", kcal, protein, diets.size()));
        return sb.toString();
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
