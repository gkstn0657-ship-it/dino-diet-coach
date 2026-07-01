package com.ssafy.nyamnyam.domain.ai;

import com.ssafy.nyamnyam.domain.challenge.ChallengeService;
import com.ssafy.nyamnyam.domain.diet.Diet;
import com.ssafy.nyamnyam.domain.diet.DietMapper;
import com.ssafy.nyamnyam.domain.member.Member;
import com.ssafy.nyamnyam.domain.member.MemberService;
import com.ssafy.nyamnyam.tool.ChallengeTool;
import com.ssafy.nyamnyam.tool.DateTimeTool;
import com.ssafy.nyamnyam.tool.DietTool;
import com.ssafy.nyamnyam.tool.FoodTool;
import com.ssafy.nyamnyam.tool.MemberTool;
import com.ssafy.nyamnyam.tool.NutritionTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * 생성형 AI(GMS, OpenAI 호환) 연동 — Spring AI ChatClient 기반.
 * ai.gms.enabled=false 또는 키 미설정 시 고정 목업 응답을 돌려 동작을 확인할 수 있다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final DietMapper dietMapper;
    private final MemberService memberService;
    private final ObjectMapper objectMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final FoodTool foodTool;
    private final DateTimeTool dateTimeTool;
    private final ChallengeService challengeService;

    @Value("${ai.gms.enabled}")
    private boolean enabled;
    @Value("${ai.gms.api-key}")
    private String apiKey;

    private boolean aiOn() {
        return enabled && apiKey != null && !apiKey.isBlank() && !"CHANGEME".equals(apiKey);
    }

    /**
     * 히스토리 + Tool 포함 호출 (Spring AI ChatClient).
     * 일반 chat()과 달리 LLM 에 Tool 을 쥐여줘, 모델이 필요한 Tool 을 스스로 선택·연쇄 호출하게 한다. (F1203~F1205)
     */
    private String chatWithTools(String system, List<ChatTurn> history, String user,
                                 String fallback, Object... tools) {
        if (!aiOn()) return fallback;
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(system));
            for (ChatTurn t : sanitizeHistory(history)) {
                messages.add("assistant".equals(t.role())
                        ? new AssistantMessage(t.content())
                        : new UserMessage(t.content()));
            }
            messages.add(new UserMessage(user));

            String text = chatClient.prompt().messages(messages).tools(tools).call().content();
            return (text == null || text.isBlank()) ? fallback : text.trim();
        } catch (Exception e) {
            log.warn("GMS(tool) 호출 실패, fallback 사용: {}", e.getMessage());
            return fallback;
        }
    }

    /** 멀티턴 대화 히스토리 1턴 (role: user | assistant) */
    public record ChatTurn(String role, String content) {}

    /** Sliding window: 최근 10턴(20개 메시지)만 LLM에 전달해 토큰 사용량 제한 */
    private static final int MAX_HISTORY_MESSAGES = 20;
    /** 히스토리 메시지 1건당 최대 길이 (비정상적으로 긴 입력 방어) */
    private static final int MAX_TURN_LENGTH = 2000;

    /** GMS Chat Completions(/chat/completions) 호출 → 응답 텍스트 (실패/비활성 시 fallback) */
    private String chat(String system, String user, String fallback) {
        return chat(system, List.of(), user, fallback);
    }

    /** 히스토리 포함 멀티턴 호출 (Spring AI ChatClient). history는 sliding window로 최근 N개만 사용 */
    private String chat(String system, List<ChatTurn> history, String user, String fallback) {
        if (!aiOn()) return fallback;
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(system));
            for (ChatTurn t : sanitizeHistory(history)) {
                messages.add("assistant".equals(t.role())
                        ? new AssistantMessage(t.content())
                        : new UserMessage(t.content()));
            }
            messages.add(new UserMessage(user));

            String text = chatClient.prompt().messages(messages).call().content();
            return (text == null || text.isBlank()) ? fallback : text.trim();
        } catch (Exception e) {
            log.warn("GMS 호출 실패, fallback 사용: {}", e.getMessage());
            return fallback;
        }
    }

    /** 히스토리 정제: 허용 role만 통과, 빈 내용 제거, 길이 제한, 최근 N개만 유지 */
    private List<ChatTurn> sanitizeHistory(List<ChatTurn> history) {
        if (history == null || history.isEmpty()) return List.of();
        List<ChatTurn> valid = history.stream()
                .filter(t -> t != null && t.content() != null && !t.content().isBlank())
                .filter(t -> "user".equals(t.role()) || "assistant".equals(t.role()))
                .map(t -> t.content().length() > MAX_TURN_LENGTH
                        ? new ChatTurn(t.role(), t.content().substring(0, MAX_TURN_LENGTH))
                        : t)
                .toList();
        return valid.size() <= MAX_HISTORY_MESSAGES
                ? valid
                : valid.subList(valid.size() - MAX_HISTORY_MESSAGES, valid.size());
    }

    // ===== F116. AI 식단 종합 분석 (period: today | week) =====
    public Map<String, Object> analyzeDiet(String email, String period) {
        Member me = memberService.getByEmail(email);
        int target = memberService.targetKcalFor(me); // 개인 목표 칼로리(프로필 기반, 미입력 시 2000)
        boolean weekly = "week".equalsIgnoreCase(period);
        int days = weekly ? 7 : 1;
        java.time.LocalDate from = java.time.LocalDate.now().minusDays(days - 1);

        // 기간 내 식단만 집계 (오늘 또는 최근 7일)
        List<Diet> diets = dietMapper.findByMember(me.getMno(), null, null).stream()
                .filter(d -> d.getEatenDate() != null && !d.getEatenDate().isBefore(from))
                .toList();

        int kcal = diets.stream().mapToInt(d -> nz(d.getTotalKcal())).sum();
        int protein = diets.stream().mapToInt(d -> nz(d.getProtein())).sum();
        int carbs = diets.stream().mapToInt(d -> nz(d.getCarbs())).sum();
        int fat = diets.stream().mapToInt(d -> nz(d.getFat())).sum();
        int avgKcal = (int) Math.round(kcal / (double) days);
        String periodLabel = weekly ? "최근 7일" : "오늘";

        // ===== 객관적 점수: 칼로리 적정성 40% + 영양 균형 40% + 끼니 충실도 20% =====
        // (기존: 칼로리 단일 지표 + 최저 40점 보장 → 한 끼만 기록해도 고득점 가능했던 문제 수정)
        double mealsPerDay = diets.isEmpty() ? 0 : diets.size() / (double) days;
        int kcalScore = (int) Math.max(0, 100 - Math.abs(target - avgKcal) * 100.0 / target);
        int macroScore = (closeness(protein / days, 60) + closeness(carbs / days, 250) + closeness(fat / days, 60)) / 3;
        int mealScore = (int) Math.min(100, Math.round(mealsPerDay / 3.0 * 100));
        int score = diets.isEmpty() ? 0
                : (int) Math.round(kcalScore * 0.4 + macroScore * 0.4 + mealScore * 0.2);
        String grade = diets.isEmpty() ? "NO_DATA"
                : score >= 85 ? "GREAT" : score >= 70 ? "GOOD" : score >= 50 ? "SOSO" : "LOW";
        boolean lowData = !diets.isEmpty() && mealsPerDay < 2; // 기록 부족 → 평가 신뢰도 낮음

        // 매크로는 하루 평균 기준 권장량 대비
        List<Map<String, Object>> macros = List.of(
                macro("단백질", protein / days, 60),
                macro("탄수화물", carbs / days, 250),
                macro("지방", fat / days, 60)
        );

        String summary;
        List<Map<String, Object>> feedback = new ArrayList<>();
        if (diets.isEmpty()) {
            summary = periodLabel + " 기록된 식단이 없어요. 식단을 먼저 기록하면 분석해드릴게요! 🦕";
            feedback.add(fb(false, "아직 분석할 식단이 없습니다."));
        } else {
            int dailyProtein = protein / days, dailyCarbs = carbs / days, dailyFat = fat / days;

            // 1) 끼니 충실도 — 기록이 적으면 가장 먼저 지적
            if (mealsPerDay < 2) {
                feedback.add(fb(false, String.format(
                        "기록된 끼니가 하루 평균 %.1f끼뿐이에요. 세 끼를 모두 기록해야 정확한 평가가 가능해요. (점수에 감점 반영)",
                        mealsPerDay)));
            } else {
                feedback.add(fb(true, String.format("하루 평균 %.1f끼 기록 — 충실한 기록이에요.", mealsPerDay)));
            }
            // 2) 칼로리 적정성 (개인 목표 ±30%)
            int lowBound = (int) Math.round(target * 0.7);
            int highBound = (int) Math.round(target * 1.3);
            if (avgKcal < lowBound) {
                feedback.add(fb(false, "하루 평균 " + avgKcal + "kcal — 목표(" + target + "kcal)보다 부족해요. 결식 여부를 확인하세요."));
            } else if (avgKcal > highBound) {
                feedback.add(fb(false, "하루 평균 " + avgKcal + "kcal — 목표보다 " + (avgKcal - target) + "kcal 초과예요."));
            } else {
                feedback.add(fb(true, "하루 평균 " + avgKcal + "kcal — 목표 범위 안이에요."));
            }
            // 3) 단백질 (권장 60g)
            if (dailyProtein < 48) {
                feedback.add(fb(false, "단백질 " + dailyProtein + "g — 권장(60g) 대비 " + (60 - dailyProtein) + "g 부족해요."));
            } else {
                feedback.add(fb(true, "단백질 " + dailyProtein + "g — 권장(60g) 수준을 충족해요."));
            }
            // 4) 탄수·지방은 과다 시에만 경고
            if (dailyCarbs > 325) feedback.add(fb(false, "탄수화물 " + dailyCarbs + "g — 권장(250g)을 크게 초과해요."));
            if (dailyFat > 78) feedback.add(fb(false, "지방 " + dailyFat + "g — 권장(60g)을 초과해요."));

            String context = String.format(
                    "분석 범위:%s, 목표:%s, 기록 %d끼(하루 평균 %.1f끼), 하루 평균 %dkcal(단백질 %dg/탄수 %dg/지방 %dg), 산정 점수 %d점(%s).",
                    periodLabel, me.getGoal(), diets.size(), mealsPerDay, avgKcal, dailyProtein, dailyCarbs, dailyFat, score, grade);
            summary = chat(
                    "너는 객관적이고 솔직한 영양사다. 과장된 칭찬은 금지. 주어진 데이터·점수와 일관되게 한국어 한두 문장으로 평가하고, "
                            + "기록이 부족하면(하루 2끼 미만) 그 한계를 먼저 언급한다.",
                    context + "\n위 데이터를 근거로 냉정하게 평가해줘.",
                    "기록을 바탕으로 균형 잡힌 식단을 이어가 보세요!");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("period", periodLabel);
        result.put("mealCount", diets.size());
        result.put("avgKcal", avgKcal);
        result.put("score", score);
        result.put("grade", grade);
        result.put("lowData", lowData);
        result.put("macros", macros);
        result.put("summary", summary);
        result.put("feedback", feedback);
        return result;
    }

    // ===== F117. AI 운동 코칭 (3종 코치 에이전트, 오늘 식단 기준) =====

    /** 코치 식별자: powerrex(벌크업) | slimdino(다이어트) | balanceno(유지) */
    private static final String COACH_POWERREX = "powerrex";
    private static final String COACH_SLIMDINO = "slimdino";
    private static final String COACH_BALANCENO = "balanceno";

    private static final String POWERREX_PROMPT = """
            [역할]
            너는 PowerRex 🦖.
            10년 경력의 근비대 전문 PT이자 헬스장 관장이다.
            [우선순위]
            1. 정확성
            2. 데이터 기반 분석
            3. 전문성
            4. 친근한 말투
            [컨텍스트]
            {context}
            [답변 원칙]
            - 운동생리학과 근비대 원리에 기반해 설명한다.
            - 모든 추천에는 이유와 기대 효과를 포함한다.
            - 컨텍스트에 없는 정보는 추정하지 않는다.
            - 실제 수치를 근거로 분석한다.
            - 단순 응원보다 전문 코칭을 우선한다.
            [답변 흐름]
            오늘 상태 분석 → 운동 제안 → 식단 평가 → 한 줄 코칭
            [말투]
            - 한국어 반말
            - 친근하고 자신감 있음
            - 과도한 감탄사 금지
            - 이모지 최대 1개
            [길이]
            - 기본 600에서 1000자
            - 복잡한 질문은 더 길게 가능
            [제약]
            - 컨텍스트 외 정보는 추정하지 않는다.
            - 의료 진단·부상 치료·보충제 용량은 안내하지 않는다.
            - 필요한 경우 "전문의와 상담해 보세요"라고 안내한다.
            - 숫자 범위는 "에서", "부터"를 사용한다.
            - 반드시 한국어만 사용한다.
            """;

    private static final String SLIMDINO_PROMPT = """
            [역할]
            너는 SlimDino 🦕.
            지속 가능한 체지방 감량을 돕는 다이어트 전문 코치다.
            [전문성]
            - 체지방 감량과 식습관 개선 전문가다.
            - 에너지 균형, 포만감 관리, 단백질 섭취, 행동 습관 형성을 중요하게 본다.
            - 단기 체중 감소보다 장기적인 감량 성공을 우선한다.
            - 극단적인 식단 제한이나 무리한 감량을 권하지 않는다.
            [우선순위]
            1. 정확성
            2. 데이터 기반 분석
            3. 전문성
            4. 지속 가능성
            5. 따뜻한 격려
            [컨텍스트]
            {context}
            [답변 원칙]
            - 컨텍스트의 실제 수치만 사용한다.
            - 모르는 정보는 추정하지 않는다.
            - 모든 식단·운동 제안에는 반드시 이유와 기대 효과를 설명한다.
            - 단순 평가보다 개선 방향을 제시한다.
            - 사용자를 비난하거나 죄책감을 유도하지 않는다.
            [답변 흐름]
            1. 오늘 상태 분석
            - 섭취 칼로리
            - 단백질 섭취량
            - 기록 여부
            - 확인 가능한 패턴
            2. 감량 관점 평가
            - 과다 섭취 여부
            - 단백질 충분 여부
            - 포만감 유지 가능성
            - 지속 가능성 평가
            3. 개선 제안
            - 오늘 가장 우선적으로 개선할 1에서 3가지
            - 왜 중요한지
            - 기대 효과
            4. 활동 제안
            - 걷기
            - 유산소
            - 근력운동
            중 현재 데이터 기반으로 적절한 방향 제안
            5. 한 줄 코칭
            [말투]
            - 한국어 존댓말
            - 따뜻하고 공감적
            - 과도한 감탄 금지
            - 이모지 최대 1개
            - 응원보다 분석을 우선
            [길이]
            - 기본 600에서 1000자
            - 분석이 필요한 경우 더 길게 작성 가능
            [제약]
            - 컨텍스트 외 정보는 사용하지 않는다.
            - 의료 진단·부상 치료·보충제 용량은 안내하지 않는다.
            - 필요한 경우 "전문의와 상담해 보세요"라고 안내한다.
            - 숫자 범위는 "에서", "부터"를 사용한다.
            - 반드시 한국어만 사용한다.
            """;

    private static final String BALANCENO_PROMPT = """
            [역할]
            너는 BalanceNo 🐢.
            건강한 체중과 생활 습관 유지를 돕는 스포츠 영양사이자 웰니스 코치다.
            [전문성]
            - 영양 균형과 생활 습관 관리 전문가다.
            - 체중 증량이나 감량보다 건강 유지와 꾸준함을 중요하게 본다.
            - 영양, 활동량, 회복의 균형을 우선한다.
            - 무리한 목표보다 지속 가능한 습관 형성을 지향한다.
            [우선순위]
            1. 정확성
            2. 데이터 기반 분석
            3. 전문성
            4. 균형
            5. 안정감
            [컨텍스트]
            {context}
            [답변 원칙]
            - 컨텍스트의 실제 수치만 사용한다.
            - 모르는 정보는 추정하지 않는다.
            - 모든 제안에는 이유와 기대 효과를 설명한다.
            - 건강 유지 관점에서 분석한다.
            - 과도한 운동이나 식단 제한을 권하지 않는다.
            [답변 흐름]
            1. 오늘 상태 분석
            - 칼로리 섭취
            - 단백질 섭취
            - 기록 상태
            - 확인 가능한 패턴
            2. 영양 균형 평가
            - 탄수화물
            - 단백질
            - 지방
            - 전체 식사 균형
            3. 회복 및 활동 제안
            - 운동
            - 휴식
            - 수면
            - 활동량
            4. 유지 전략
            - 현재 유지할 점
            - 개선할 점
            - 기대 효과
            5. 한 줄 코칭
            [말투]
            - 한국어 존댓말
            - 침착하고 신뢰감 있음
            - 선생님 같은 설명형 톤
            - 과도한 감탄사 금지
            - 이모지 최대 1개
            [길이]
            - 기본 600에서 1000자
            - 복잡한 질문은 더 길게 가능
            [제약]
            - 컨텍스트 외 정보는 추정하지 않는다.
            - 의료 진단·부상 치료·보충제 용량은 안내하지 않는다.
            - 필요한 경우 "전문의와 상담해 보세요"라고 안내한다.
            - 숫자 범위는 "에서", "부터"를 사용한다.
            - 반드시 한국어만 사용한다.
            """;

    public String workoutCoach(String email, String message, String coach) {
        Member me = memberService.getByEmail(email);
        String coachKey = normalizeCoach(coach, me.getGoal());

        // 사용자 전용 Tool: mno/Member 를 LLM 입력이 아닌 서버가 주입 → 타인 정보 조회 차단
        MemberTool memberTool = new MemberTool(me);
        DietTool dietTool = new DietTool(dietMapper, me.getMno());
        NutritionTool nutritionTool = new NutritionTool(dietMapper, me.getMno(), memberService.targetKcalFor(me));
        ChallengeTool challengeTool = new ChallengeTool(challengeService, me.getMno());

        // 1) LLM Intent Router → 2) sanitize → 3) 필요한 데이터를 '서버가 계산한 검증 컨텍스트'로 구성
        IntentResult intent = aiOn() ? routeIntent(message) : DEFAULT_INTENT;
        String verifiedContext = aiOn()
                ? buildVerifiedContext(intent, memberTool, nutritionTool, challengeTool) : "";

        // 검증 컨텍스트 + Tool 안내 + 코칭 원칙을 페르소나 프롬프트의 {context} 자리에 주입
        String ctxBlock = (verifiedContext.isBlank() ? "" :
                "[검증된 컨텍스트 — 서버가 계산한 사실. 최우선 근거로 사용]\n" + verifiedContext + "\n\n")
                + COACH_TOOL_GUIDE + "\n\n" + COACH_PRINCIPLES;
        String system = promptFor(coachKey).replace("{context}", ctxBlock);

        // 단순 질문(날짜/음식조회/앱안내/일반)이면 응답 모드를 '맨 끝'에 덧붙여 고정 출력 형식을 무력화
        String responseHint = responseHint(intent.primaryIntent());
        if (!responseHint.isBlank()) system = system + "\n\n" + responseHint;

        // DB가 대화 히스토리의 단일 출처 — 최근 N건 조회 후 sliding window로 멀티턴 컨텍스트 구성
        List<ChatTurn> history = chatMessageMapper.findRecent(me.getMno(), coachKey, MAX_HISTORY_MESSAGES).stream()
                .map(m -> new ChatTurn(m.getRole(), m.getContent()))
                .toList();
        // 코치가 프로필·영양요약·식단·음식영양·챌린지·날짜 Tool 을 스스로 선택·연쇄 호출 (검증 컨텍스트는 이미 제공됨)
        String reply = chatWithTools(system, history, message, fallbackFor(coachKey),
                foodTool, memberTool, dietTool, nutritionTool, challengeTool, dateTimeTool);

        // 질문·응답 저장 (브라우저를 닫아도 대화 유지)
        saveChatMessage(me.getMno(), coachKey, "user", message);
        saveChatMessage(me.getMno(), coachKey, "assistant", reply);
        return reply;
    }

    // ===== Intent Router: 사용자 의도를 LLM 으로 유연하게 해석하고 서버가 sanitize =====

    /** 라우터 출력 (sanitize 후). 허용 목록 밖 값은 서버에서 걸러진다. */
    public record IntentResult(String primaryIntent, List<String> secondaryIntents,
                               List<String> neededData, String emotionalTone, boolean needsAction) {}

    private static final Set<String> ALLOWED_INTENTS = Set.of(
            "date_time", "food_lookup", "diet_summary", "meal_advice",
            "workout_advice", "challenge_check", "app_guide", "general_chat");
    private static final Set<String> ALLOWED_NEEDED = Set.of(
            "current_datetime", "profile", "today_nutrition", "date_nutrition",
            "weekly_nutrition", "active_challenges", "food_nutrition");

    private static final IntentResult DEFAULT_INTENT =
            new IntentResult("meal_advice", List.of(), List.of(), "neutral", false);

    private static final String ROUTER_PROMPT = """
            너는 DinoDiet AI 코치의 '의도 분석기'다. 사용자 메시지를 읽고 아래 JSON 객체 '하나만' 출력한다(설명/코드펜스 금지).
            {
              "primaryIntent": "<intent>",
              "secondaryIntents": ["<intent>", ...],
              "neededData": ["<data>", ...],
              "emotionalTone": "<짧은 한 단어, 예: neutral, concerned, motivated>",
              "needsAction": false
            }
            [allowed intent] date_time, food_lookup, diet_summary, meal_advice, workout_advice, challenge_check, app_guide, general_chat
            [allowed neededData] current_datetime, profile, today_nutrition, date_nutrition, weekly_nutrition, active_challenges, food_nutrition
            [규칙]
            - 애매하거나 감정 섞인 식사 고민("오늘 망한 듯", "저녁 뭐 먹지")은 meal_advice 로 본다.
            - 날짜/요일/시간 질문은 date_time + current_datetime.
            - 특정 음식 먹어도 되는지는 food_lookup + food_nutrition + today_nutrition.
            - 챌린지/인증/조건 질문은 challenge_check + active_challenges.
            - 이번 단계에서는 상태 변경을 하지 않으므로 needsAction 은 항상 false.
            - 위 목록에 없는 값은 절대 쓰지 않는다.
            """;

    /** LLM 라우팅 → 허용 목록으로 sanitize. 실패 시 기본 의도(meal_advice)로 fallback. */
    IntentResult routeIntent(String message) {
        if (!aiOn()) return DEFAULT_INTENT;
        try {
            String raw = chat(ROUTER_PROMPT, message, "");
            if (raw == null || raw.isBlank()) return DEFAULT_INTENT;
            return sanitizeIntent(parseJsonObject(raw));
        } catch (Exception e) {
            log.warn("Intent 라우팅 실패, 기본 의도 사용: {}", e.getMessage());
            return DEFAULT_INTENT;
        }
    }

    /**
     * 의도별 '응답 모드' 지시.
     * 날짜/음식조회/앱안내/일반 질문은 코치 페르소나의 고정 출력 형식(운동·식단 3단)을 따르지 않고 곧바로 답하게 한다.
     */
    private String responseHint(String primary) {
        return switch (primary) {
            case "date_time", "food_lookup", "app_guide", "general_chat" -> """
                    [응답 모드 — 중요]
                    이번 질문은 단순 정보/안내성 질문이다. 위 페르소나의 고정 출력 형식(1.운동 2.식단 3.응원)을 따르지 마라.
                    질문에만 한두 문장으로 곧바로 답하고, 묻지 않은 운동·식단 조언은 덧붙이지 않는다.
                    - 날짜/시간 질문이면 [현재 날짜/시간] 값으로 날짜만 답한다.
                    - 음식 조회 질문이면 영양 수치 위주로 답한다.""";
            default -> "";
        };
    }

    /** 라우터 JSON → 허용 목록 기반 정제 (LLM 이 이상한 값을 줘도 안전) */
    IntentResult sanitizeIntent(Map<String, Object> j) {
        if (j == null) return DEFAULT_INTENT;
        String primary = asStr(j.get("primaryIntent"));
        if (!ALLOWED_INTENTS.contains(primary)) primary = "meal_advice";
        List<String> sec = asStrList(j.get("secondaryIntents")).stream()
                .filter(ALLOWED_INTENTS::contains).distinct().toList();
        List<String> need = asStrList(j.get("neededData")).stream()
                .filter(ALLOWED_NEEDED::contains).distinct().toList();
        String tone = asStr(j.get("emotionalTone"));
        if (tone == null || tone.isBlank()) tone = "neutral";
        if (tone.length() > 30) tone = tone.substring(0, 30);
        boolean act = Boolean.TRUE.equals(j.get("needsAction"));
        return new IntentResult(primary, sec, need, tone, act);
    }

    private String asStr(Object o) {
        return o == null ? null : String.valueOf(o).trim();
    }

    @SuppressWarnings("unchecked")
    private List<String> asStrList(Object o) {
        if (!(o instanceof List<?> list)) return List.of();
        List<String> out = new ArrayList<>();
        for (Object e : list) if (e != null) out.add(String.valueOf(e).trim());
        return out;
    }

    /**
     * 의도에 따라 '서버가 계산한 검증 컨텍스트'를 구성한다.
     * 날짜/시간과 오늘 영양은 거의 모든 코칭의 기준이라 항상 포함하고, 나머지는 neededData 에 따라 추가한다.
     * LLM 이 Tool 을 안 불러도 핵심 사실이 답변에 반영되게 하는 것이 목적.
     */
    private String buildVerifiedContext(IntentResult intent, MemberTool memberTool,
                                        NutritionTool nutritionTool, ChallengeTool challengeTool) {
        StringBuilder sb = new StringBuilder();
        try {
            // 날짜/시간은 비용이 작고 상대 날짜 질문에 필요하므로 항상 포함
            sb.append(dateTimeTool.getCurrentDateTime()).append('\n');

            Set<String> need = new HashSet<>(intent.neededData());
            String primary = intent.primaryIntent();
            // 오늘 영양 요약은 '영양/코칭 의도'일 때만 포함 (날짜·앱안내·일반 질문에 식단이 끼어드는 문제 방지)
            boolean nutritionWanted = need.contains("today_nutrition")
                    || "meal_advice".equals(primary) || "diet_summary".equals(primary)
                    || "workout_advice".equals(primary);
            if (nutritionWanted) {
                sb.append(nutritionTool.getTodayNutritionSummary()).append('\n');
            }

            // 챌린지 컨텍스트는 챌린지 질문뿐 아니라 '먹어도 되나/저녁 뭐 먹지' 같은 식사 결정에도 필요
            // (챌린지 한도가 일반 목표보다 더 빡빡한 제약일 수 있으므로)
            boolean challengeWanted = need.contains("active_challenges")
                    || "challenge_check".equals(primary)
                    || "meal_advice".equals(primary)
                    || "food_lookup".equals(primary)
                    || "diet_summary".equals(primary)
                    || intent.secondaryIntents().contains("challenge_check");

            if (need.contains("weekly_nutrition")) {
                sb.append(nutritionTool.getWeeklyNutritionSummary()).append('\n');
            }
            if (challengeWanted) {
                sb.append(challengeTool.getMyChallengeStatus()).append('\n');
            }
            if (need.contains("profile")) {
                sb.append(memberTool.getMyProfile()).append('\n');
            }
        } catch (Exception e) {
            log.warn("검증 컨텍스트 구성 실패(부분 사용): {}", e.getMessage());
        }
        return sb.toString().trim();
    }

    /** 코치별 저장된 대화 조회 (입장 시 복원용, 오래된 순) */
    public List<ChatMessage> getChatHistory(String email, String coach) {
        Member me = memberService.getByEmail(email);
        String coachKey = normalizeCoach(coach, me.getGoal());
        return chatMessageMapper.findRecent(me.getMno(), coachKey, MAX_HISTORY_MESSAGES);
    }

    /** 90일 경과 대화 삭제 (스케줄러에서 호출) */
    public int purgeOldChats(int retentionDays) {
        return chatMessageMapper.deleteOlderThan(retentionDays);
    }

    private void saveChatMessage(Integer mno, String coach, String role, String content) {
        try {
            ChatMessage m = new ChatMessage();
            m.setMno(mno);
            m.setCoach(coach);
            m.setRole(role);
            m.setContent(content);
            chatMessageMapper.insert(m);
        } catch (Exception e) {
            // 저장 실패가 코칭 응답 자체를 막지 않도록 로그만 남김
            log.warn("대화 저장 실패 (mno={}, coach={}): {}", mno, coach, e.getMessage());
        }
    }

    /** 잘못된/누락된 coach 값은 회원 목표 기반으로 기본 코치 매핑 */
    private String normalizeCoach(String coach, String goal) {
        if (coach != null) {
            String c = coach.trim().toLowerCase();
            if (COACH_POWERREX.equals(c) || COACH_SLIMDINO.equals(c) || COACH_BALANCENO.equals(c)) return c;
        }
        String g = goal == null ? "" : goal;
        if (g.contains("감량") || g.contains("다이어트")) return COACH_SLIMDINO;
        if (g.contains("근육") || g.contains("증량") || g.contains("벌크")) return COACH_POWERREX;
        return COACH_BALANCENO;
    }

    private String promptFor(String coachKey) {
        return switch (coachKey) {
            case COACH_POWERREX -> POWERREX_PROMPT;
            case COACH_SLIMDINO -> SLIMDINO_PROMPT;
            default -> BALANCENO_PROMPT;
        };
    }

    private String fallbackFor(String coachKey) {
        return switch (coachKey) {
            case COACH_POWERREX -> "오늘 식단 기록이 아직 없네! 일단 잘 챙겨 먹고, 스쿼트·벤치·데드 3대 운동 5×5부터 가보자! 🦖";
            case COACH_SLIMDINO -> "오늘 식단 기록이 아직 없어요. 가벼운 유산소 30분과 근력운동을 병행하고, 무리한 절식은 피해 주세요! 🦕";
            default -> "오늘 식단 기록이 아직 없습니다. 가벼운 걷기 30분과 전신 스트레칭으로 꾸준함을 지켜보세요. 🐢";
        };
    }

    /** 코치 프롬프트의 {context} 자리에 들어가는 'Tool 사용 지시'. 실제 데이터는 검증 컨텍스트 + 코치의 Tool 조회로 확보한다. */
    private static final String COACH_TOOL_GUIDE = """
            [사용 가능한 도구 — 추측 금지, 필요한 데이터는 도구로 조회]
            - getCurrentDateTime() : 현재 날짜·요일·시간 (상대 날짜 질문 전 호출)
            - getMyProfile() : 키·몸무게·목표·질환
            - getTodayNutritionSummary() : 오늘 총kcal/남은kcal/단백질·탄수·지방/끼니 상태
            - getNutritionSummaryByDate("yyyy-MM-dd") : 특정 날짜 영양 요약
            - getWeeklyNutritionSummary() : 최근 7일 평균·패턴
            - getMyChallengeStatus() : 참여 챌린지 진행률·조건·오늘 충족/인증 여부 (조회 전용)
            - getTodayDiets() : 오늘 끼니 목록(상세)
            - searchFoodNutrition(foodName) : 특정 음식 1인분 영양정보(식약처 DB)
            - 위 [검증된 컨텍스트]에 이미 있는 값은 다시 조회하지 말고 그대로 신뢰한다.
            - DB에 사용자가 물은 음식과 맞는 항목이 없으면 "정확한 값은 없지만 일반적으로 약 ○○kcal(추정치)"처럼
              '추정치'임을 반드시 밝힌 뒤 일반 상식으로 답한다.""";

    /** 코칭 원칙 — 모든 코치 페르소나에 공통 적용 */
    private static final String COACH_PRINCIPLES = """
            [코칭 원칙]
            - 문장을 기능 키워드로만 보지 말고, 사용자가 어떤 '결정'을 하려는지 파악한다.
            - 사용자가 불안·실패감을 보이면 먼저 안심시키되, 반드시 실제 데이터를 근거로 조정안을 제시한다.
            - 검증된 컨텍스트와 도구 결과를 최우선 근거로 삼는다. 모르는 데이터는 모른다고 말한다.
            - 추정치는 반드시 '추정치'라고 표시한다.
            - 칼로리·남은 양 등 숫자는 절대 직접 빼고 더하지 말고, 컨텍스트에 주어진 값을 그대로 인용한다.
            - '남은 칼로리'는 두 종류다: ① 일반 목표 기준 남은(remainingCalories = targetCalories - consumedCalories) ② 챌린지 한도까지 남은(challengeRemainingAllowance). 절대 혼동하지 말 것.
            - targetCalories(하루 목표 칼로리)는 서버가 사용자별로 계산해 제공한다. 2000 같은 고정값을 전제하지 말고, 컨텍스트/도구가 준 값을 그대로 사용한다.
              챌린지 통과/인증 이야기에는 반드시 '챌린지 통과까지' 값을 사용한다.
            - 챌린지가 '이하(kcal)' 조건이면, 추천 음식의 칼로리 합이 '챌린지 통과까지' 남은 여유를 넘지 않게 한다.
              (일반 목표가 더 넉넉해도 챌린지 한도가 더 빡빡하면 챌린지 한도를 기준으로 조언한다.)
            - 챌린지 상태·남은 칼로리·영양 부족/초과는 숫자와 함께 설명한다.
            - 날짜/시간 질문은 현재 날짜/시간 컨텍스트를 사용한다.
            - 식단/운동/챌린지와 무관한 범용 질문은 짧게 답하거나 서비스 범위로 정중히 안내한다.
            - 의료 진단·질병 치료·극단적 감량 조언은 금지한다.""";

    // ===== 가이드 챗봇 (하이브리드: FAQ 분류 → 사전 작성 답변, 그 외 → 문서 그라운딩 생성) =====

    private static final String GUIDE_FALLBACK =
            "왼쪽 위 '식단' 메뉴에서 사진을 올리면 AI가 영양을 분석해 기록해줘요. 더 궁금한 게 있으면 물어보세요! 🦕";
    private static final String GUIDE_UNKNOWN =
            "그건 제가 가진 가이드에 없는 내용이에요. 식단 기록, AI 코칭, 챌린지, 커뮤니티 사용법을 물어봐 주세요! 🦕";

    /** 가이드 문서·FAQ 캐시 (최초 1회 로드) */
    private volatile String guideDoc;
    private volatile List<Map<String, String>> guideFaq;

    @SuppressWarnings("unchecked")
    private synchronized void loadGuideResources() {
        if (guideDoc != null) return;
        try {
            guideDoc = new String(
                    new org.springframework.core.io.ClassPathResource("guide/guide.md")
                            .getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            guideFaq = objectMapper.readValue(
                    new org.springframework.core.io.ClassPathResource("guide/guide-faq.json").getInputStream(),
                    List.class);
        } catch (Exception e) {
            log.warn("가이드 리소스 로드 실패: {}", e.getMessage());
            guideDoc = "";
            guideFaq = List.of();
        }
    }

    /**
     * 환각 방지 하이브리드 구조.
     * 1) LLM은 질문이 FAQ에 해당하면 id만 분류 → 사람이 작성한 답변을 그대로 반환 (환각 0%)
     * 2) FAQ 밖 자유 질문이면 가이드 문서에 근거해서만 생성, 문서에 없으면 고정 안내문
     */
    public String guide(String message) {
        if (!aiOn()) return GUIDE_FALLBACK;
        loadGuideResources();

        StringBuilder faqList = new StringBuilder();
        for (Map<String, String> f : guideFaq) {
            faqList.append("- ").append(f.get("id")).append(": ").append(f.get("question")).append('\n');
        }

        String system = """
                너는 '냠냠코치' 서비스 이용 가이드 봇이다. 반드시 JSON 객체 한 개만 출력한다(설명·코드펜스 금지).

                [FAQ 목록]
                {faqList}
                [서비스 가이드 문서 — 유일한 답변 근거]
                {doc}

                [출력 규칙]
                1. 사용자 질문이 FAQ 항목과 같은 의미라면: {"faq":"<해당 id>"}
                2. 그 외 질문은 위 가이드 문서에 적힌 내용에 근거해서만 한국어로 2~3문장 이내로 답한다: {"answer":"<답변>"}
                3. 문서에 없는 기능·메뉴·경로·정책은 절대 지어내지 말고 이렇게 답한다: {"answer":"{unknown}"}
                """
                .replace("{faqList}", faqList)
                .replace("{doc}", guideDoc)
                .replace("{unknown}", GUIDE_UNKNOWN);

        String raw = chat(system, message, GUIDE_FALLBACK);
        return resolveGuideReply(raw);
    }

    /** 모델 출력(JSON)을 최종 답변으로 변환. FAQ 매칭 시 사전 작성 답변을 그대로 반환 */
    private String resolveGuideReply(String raw) {
        if (raw == null || raw.isBlank()) return GUIDE_FALLBACK;
        try {
            Map<String, Object> res = parseJsonObject(raw);
            Object faqId = res.get("faq");
            if (faqId instanceof String id) {
                for (Map<String, String> f : guideFaq) {
                    if (id.equals(f.get("id"))) return f.get("answer");
                }
            }
            if (res.get("answer") instanceof String s && !s.isBlank()) return s;
            return GUIDE_FALLBACK;
        } catch (Exception e) {
            // JSON 형식을 어기고 평문으로 답한 경우 — 문서 그라운딩된 생성 결과로 간주
            return raw;
        }
    }

    // ===== F116(심화). 사진 → 음식 인식·영양 분석 (비전) =====
    /**
     * 음식명 → 1인분 기준 영양성분 추정(검색 실패 시 사용). 결과엔 estimated=true 표시.
     * AI 미설정이면 대략값 목업.
     */
    public Map<String, Object> estimateNutrition(String foodName) {
        String name = foodName == null ? "" : foodName.trim();
        if (name.isEmpty()) throw com.ssafy.nyamnyam.common.CustomException.badRequest("음식 이름을 입력해 주세요.");
        if (name.length() > 50) throw com.ssafy.nyamnyam.common.CustomException.badRequest("음식 이름이 너무 깁니다.");

        if (!aiOn()) {
            Map<String, Object> mock = new LinkedHashMap<>();
            mock.put("name", name);
            mock.put("kcal", 300);
            mock.put("protein", 15);
            mock.put("carbs", 35);
            mock.put("fat", 10);
            mock.put("estimated", true);
            return mock;
        }
        String system = "너는 영양사다. 한국 음식에 익숙하다. 반드시 JSON만 출력한다.";
        String user = "'" + name + "' 1인분 기준 영양성분을 추정해서 아래 JSON 형식으로만 답해줘(설명/코드펜스 금지):\n"
                + "{\"kcal\":정수,\"protein\":정수,\"carbs\":정수,\"fat\":정수}\n"
                + "값은 일반적인 1인분 추정치(정수, 단위 kcal/g).";
        String raw;
        try {
            raw = chatClient.prompt()
                    .messages(List.of(new SystemMessage(system), new UserMessage(user)))
                    .call().content();
        } catch (Exception e) {
            log.warn("AI 영양 추정 호출 실패: {}", e.getMessage());
            throw com.ssafy.nyamnyam.common.CustomException.badRequest("AI 영양 추정에 실패했어요. 잠시 후 다시 시도해 주세요.");
        }
        if (raw == null) throw com.ssafy.nyamnyam.common.CustomException.badRequest("AI 영양 추정에 실패했어요.");
        Map<String, Object> parsed = parseJsonObject(raw);
        parsed.put("name", name);       // 모델이 이름을 바꾸지 못하게 강제
        parsed.put("estimated", true);  // 추정치 플래그
        return parsed;
    }

    public Map<String, Object> analyzePhoto(byte[] image, String contentType) {
        if (!aiOn()) {
            // AI 미설정 시 목업
            Map<String, Object> mock = new LinkedHashMap<>();
            mock.put("title", "분석된 식단");
            mock.put("foods", List.of(Map.of("name", "음식(목업)", "kcal", 500)));
            mock.put("totalKcal", 500);
            mock.put("comment", "AI 키 미설정 상태(목업 응답)입니다.");
            return mock;
        }
        // GMS는 큰 요청 본문을 거부하므로 사진을 작게 리사이즈 후 전송
        byte[] resized;
        try {
            resized = resizeForVision(image);
        } catch (Exception e) {
            log.warn("이미지 리사이즈 실패, 원본 사용: {}", e.getMessage());
            resized = image;
        }
        String system = "너는 음식 사진을 분석하는 영양사다. 한국 음식에 익숙하다. 반드시 JSON만 출력한다.";
        String user = "이 사진 속 음식들을 인식해서 아래 JSON 형식으로만 답해줘(설명/코드펜스 금지):\n"
                + "{\"title\":\"대표 식단명\",\"foods\":[{\"name\":\"음식명\",\"kcal\":정수,\"protein\":정수,\"carbs\":정수,\"fat\":정수}],\"comment\":\"한 줄 영양 코멘트\"}\n"
                + "각 음식은 1인분 기준 추정치, 음식명은 한국어로.";

        String raw = chatWithImage(system, user, resized);
        if (raw == null) throw com.ssafy.nyamnyam.common.CustomException.badRequest("AI 사진 분석에 실패했습니다.");

        Map<String, Object> parsed = parseJsonObject(raw);
        // totalKcal 보강
        int total = 0;
        Object foods = parsed.get("foods");
        if (foods instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> f && f.get("kcal") instanceof Number n) total += n.intValue();
            }
        }
        parsed.put("totalKcal", total);
        return parsed;
    }

    /** 비전 전송용으로 이미지를 가로 최대 512px JPEG 로 축소 (GMS 본문 크기 제한 회피) */
    private byte[] resizeForVision(byte[] input) throws Exception {
        BufferedImage src = ImageIO.read(new ByteArrayInputStream(input));
        if (src == null) return input;
        int maxW = 512;
        int w = src.getWidth(), h = src.getHeight();
        int nw = Math.min(maxW, w);
        int nh = (int) Math.round(h * ((double) nw / w));
        BufferedImage dst = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(dst, "jpg", baos);
        return baos.toByteArray();
    }

    /** 비전: 이미지(Media) 첨부 호출 (Spring AI ChatClient) */
    private String chatWithImage(String system, String userText, byte[] imageBytes) {
        try {
            Media media = Media.builder()
                    .mimeType(MimeTypeUtils.IMAGE_JPEG)
                    .data(new ByteArrayResource(imageBytes))
                    .build();
            UserMessage userMessage = UserMessage.builder()
                    .text(userText)
                    .media(media)
                    .build();
            return chatClient.prompt()
                    .messages(List.of(new SystemMessage(system), userMessage))
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("GMS 비전 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    /** 모델 응답에서 JSON 객체 추출(코드펜스/잡텍스트 제거) */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonObject(String raw) {
        try {
            String s = raw.trim();
            int a = s.indexOf('{');
            int b = s.lastIndexOf('}');
            if (a >= 0 && b > a) s = s.substring(a, b + 1);
            return objectMapper.readValue(s, Map.class);
        } catch (Exception e) {
            throw com.ssafy.nyamnyam.common.CustomException.badRequest("AI 응답을 해석하지 못했습니다.");
        }
    }

    /** 권장량 대비 근접도 0~100 — 초과·미달 모두 감점 */
    private int closeness(int actual, int target) {
        return (int) Math.max(0, 100 - Math.abs(actual - target) * 100.0 / target);
    }

    /** 피드백 항목: ok(충족 여부) + text — 프론트가 ✅/⚠️ 아이콘 분기 */
    private Map<String, Object> fb(boolean ok, String text) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", ok);
        m.put("text", text);
        return m;
    }

    private Map<String, Object> macro(String label, int value, int target) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("label", label);
        m.put("pct", clamp((int) Math.round(value * 100.0 / target), 0, 100));
        return m;
    }

    private int nz(Integer v) { return v == null ? 0 : v; }
    private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
}
