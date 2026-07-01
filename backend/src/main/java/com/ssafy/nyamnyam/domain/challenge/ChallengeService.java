package com.ssafy.nyamnyam.domain.challenge;

import com.ssafy.nyamnyam.common.CustomException;
import com.ssafy.nyamnyam.common.InputSanitizer;
import com.ssafy.nyamnyam.domain.diet.Diet;
import com.ssafy.nyamnyam.domain.diet.DietMapper;
import com.ssafy.nyamnyam.domain.water.WaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeMapper challengeMapper;
    private final DietMapper dietMapper;
    private final WaterService waterService;

    /** 시간 의존 로직(일일 인증·상태)을 테스트할 수 있도록 교체 가능한 시계. 운영은 시스템 시계 */
    private java.time.Clock clock = java.time.Clock.systemDefaultZone();

    private int totalDays(Challenge c) {
        if (c.getStartDate() != null && c.getEndDate() != null) {
            return (int) Math.max(1, ChronoUnit.DAYS.between(c.getStartDate(), c.getEndDate()));
        }
        return 7;
    }

    private String period(Challenge c) {
        return totalDays(c) + "일";
    }

    private String status(Challenge c) {
        LocalDate today = LocalDate.now(clock);
        if (c.getStartDate() != null && today.isBefore(c.getStartDate())) return "모집중";
        if (c.getEndDate() != null && today.isAfter(c.getEndDate())) return "종료";
        return "LIVE";
    }

    // ===== 식단 연동 인증 조건 =====

    /** 조건 라벨 (목록·상세 표시용). 조건 없으면 null */
    private String condLabel(Challenge c) {
        if (c.getCondType() == null || c.getCondType().isBlank() || c.getCondValue() == null) return null;
        return switch (c.getCondType()) {
            case "DAILY_KCAL_MAX" -> "일일 " + c.getCondValue() + "kcal 이하";
            case "DAILY_PROTEIN_MIN" -> "일일 단백질 " + c.getCondValue() + "g 이상";
            case "WEEKLY_KCAL_MAX" -> "최근 7일 합 " + c.getCondValue() + "kcal 이하";
            case "DAILY_WATER_MIN" -> "일일 물 " + c.getCondValue() + "잔 이상";
            default -> null;
        };
    }

    /**
     * 오늘(또는 최근 7일) 식단 집계로 조건 충족 여부 평가.
     * 칼로리 '이하' 조건은 식단 기록이 1건 이상 있어야 충족 (미기록=통과 꼼수 방지)
     */
    private Map<String, Object> evalCond(Challenge c, Integer mno) {
        Map<String, Object> r = new LinkedHashMap<>();
        if (condLabel(c) == null) {
            r.put("met", true);
            r.put("status", null);
            return r;
        }
        // 물 섭취 조건: 식단과 무관하게 water_logs 로 평가
        if ("DAILY_WATER_MIN".equals(c.getCondType())) {
            int cups = waterService.cupsOn(mno, LocalDate.now(clock).toString());
            int target = c.getCondValue();
            boolean ok = cups >= target;
            int left = target - cups;
            r.put("met", ok);
            r.put("status", "오늘 물 " + cups + " / " + target + "잔");
            r.put("remainingText", left > 0 ? "목표까지 물 " + left + "잔 더 필요" : "물 목표 달성 (+" + (-left) + "잔)");
            return r;
        }
        LocalDate today = LocalDate.now(clock);
        List<Diet> diets;
        if (c.getCondType().startsWith("WEEKLY")) {
            LocalDate from = today.minusDays(6);
            diets = dietMapper.findByMember(mno, null, null).stream()
                    .filter(d -> d.getEatenDate() != null
                            && !d.getEatenDate().isBefore(from) && !d.getEatenDate().isAfter(today))
                    .toList();
        } else {
            diets = dietMapper.findByMember(mno, null, today.toString());
        }
        int kcal = diets.stream().mapToInt(d -> nz(d.getTotalKcal())).sum();
        int protein = diets.stream().mapToInt(d -> nz(d.getProtein())).sum();
        boolean hasMeal = !diets.isEmpty();
        int v = c.getCondValue();

        boolean met;
        String status;
        // remainingText: '인증까지 남은 여유'를 서버가 직접 계산 (LLM 이 산수하지 않도록).
        String remainingText;
        switch (c.getCondType()) {
            case "DAILY_KCAL_MAX" -> {
                met = hasMeal && kcal <= v;
                status = hasMeal ? "오늘 " + kcal + " / " + v + "kcal" : "오늘 식단 기록이 필요해요";
                remainingText = remainingForMax(hasMeal, v, kcal, "오늘");
            }
            case "DAILY_PROTEIN_MIN" -> {
                met = protein >= v;
                status = "오늘 단백질 " + protein + " / " + v + "g";
                int left = v - protein;
                remainingText = left > 0 ? "목표까지 단백질 " + left + "g 더 필요" : "단백질 목표 달성 (+" + (-left) + "g)";
            }
            case "WEEKLY_KCAL_MAX" -> {
                met = hasMeal && kcal <= v;
                status = hasMeal ? "7일 합 " + kcal + " / " + v + "kcal" : "최근 식단 기록이 필요해요";
                remainingText = remainingForMax(hasMeal, v, kcal, "7일 합");
            }
            default -> {
                met = true;
                status = null;
                remainingText = null;
            }
        }
        r.put("met", met);
        r.put("status", status);
        r.put("remainingText", remainingText);
        return r;
    }

    /** '이하' 조건의 인증까지 남은 여유 문구 (한도-현재). 초과면 초과량 표시 */
    private String remainingForMax(boolean hasMeal, int limit, int current, String scope) {
        if (!hasMeal) return scope + " 식단 기록이 필요해요";
        int left = limit - current;
        return left >= 0
                ? "인증까지 " + left + "kcal 여유 (한도 " + limit + " - " + scope + " " + current + ")"
                : "한도 " + limit + "kcal를 " + (-left) + "kcal 초과 (현재 " + current + ")";
    }

    /** 모집중→start까지, 진행중→end까지 남은 일수. 종료면 null */
    private String dday(Challenge c) {
        LocalDate today = LocalDate.now(clock);
        if (c.getStartDate() != null && today.isBefore(c.getStartDate())) {
            return "D-" + ChronoUnit.DAYS.between(today, c.getStartDate());
        }
        if (c.getEndDate() != null && !today.isAfter(c.getEndDate())) {
            long left = ChronoUnit.DAYS.between(today, c.getEndDate());
            return left == 0 ? "D-DAY" : "D-" + left;
        }
        return null;
    }

    /**
     * 공개 챌린지 목록(승인+노출만). status: recruiting|live|ended|all(기본 recruiting), sort: popular|latest(기본 popular)
     */
    public List<Map<String, Object>> list(Integer mno, String statusFilter, String sort) {
        Set<Integer> joined = new HashSet<>(challengeMapper.findJoinedCnos(mno));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Challenge c : challengeMapper.findPublic("latest".equals(sort) ? "latest" : "popular")) {
            String st = status(c); // 모집중 | LIVE | 종료
            if (!matchesFilter(statusFilter, st)) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("cno", c.getCno());
            m.put("emoji", c.getEmoji());
            m.put("color", c.getColor());
            m.put("status", st);
            m.put("dday", dday(c));
            m.put("period", period(c));
            m.put("title", c.getTitle());
            m.put("desc", c.getDescription());
            m.put("participants", nz(c.getParticipantCount()));
            m.put("joined", joined.contains(c.getCno()));
            m.put("condLabel", condLabel(c));
            result.add(m);
        }
        return result;
    }

    /** status 필터 매칭 (null/all → 전체) */
    private boolean matchesFilter(String f, String st) {
        if (f == null || f.isBlank() || "all".equals(f)) return true;
        return switch (f) {
            case "recruiting" -> "모집중".equals(st);
            case "live" -> "LIVE".equals(st);
            case "ended" -> "종료".equals(st);
            default -> true;
        };
    }

    public Map<String, Object> detail(Integer cno, Integer mno) {
        Challenge c = challengeMapper.findById(cno);
        if (c == null) throw CustomException.notFound("챌린지를 찾을 수 없습니다.");
        // 상세 조회는 인증 상태를 변경하지 않는다. 현재 조건 충족 여부(condMet/condStatus)만 표시하고,
        // 실제 인증(done_days 증가)은 하루 마감 배치(autoCheckAllParticipants)에서만 확정한다.
        ChallengeParticipant p = challengeMapper.findParticipant(cno, mno);

        boolean joined = p != null;
        boolean checkedToday = joined && LocalDate.now(clock).equals(p.getLastCheckDate());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("cno", c.getCno());
        m.put("emoji", c.getEmoji());
        m.put("color", c.getColor());
        m.put("status", joined ? "참여중" : status(c));
        m.put("phase", status(c)); // 실제 기간 단계(모집중|LIVE|종료) — 인증 가능 여부 판단용
        m.put("title", c.getTitle());
        m.put("period", period(c));
        m.put("participants", nz(c.getParticipantCount()));
        m.put("desc", c.getDescription());
        m.put("isJoined", joined);
        m.put("checkedToday", checkedToday);
        m.put("progress", joined ? nz(p.getProgress()) : 0);
        m.put("doneDays", joined ? nz(p.getDoneDays()) : 0);
        m.put("totalDays", joined ? nz(p.getTotalDays()) : totalDays(c));
        m.put("dday", dday(c));
        m.put("isOwner", c.getMno() != null && c.getMno().equals(mno));

        // 식단 연동 인증 조건 + 현재 충족 여부
        Map<String, Object> cond = evalCond(c, mno);
        m.put("condLabel", condLabel(c));
        m.put("condMet", cond.get("met"));
        m.put("condStatus", cond.get("status"));

        // 리더보드 (달성률 Top 10, 본인 표시)
        List<Map<String, Object>> board = new ArrayList<>();
        for (Map<String, Object> row : challengeMapper.leaderboard(cno)) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("name", row.get("name"));
            r.put("progress", row.get("progress"));
            r.put("doneDays", row.get("doneDays"));
            r.put("isMe", mno.equals(((Number) row.get("mno")).intValue()));
            board.add(r);
        }
        m.put("leaderboard", board);
        return m;
    }

    /** F113. 오늘 인증 → 달성일 +1, 진행률 재계산 (하루 1회 제한, 조건 없는 챌린지 전용) */
    @Transactional
    public Map<String, Object> checkIn(Integer cno, Integer mno) {
        ChallengeParticipant p = challengeMapper.findParticipant(cno, mno);
        if (p == null) throw CustomException.badRequest("먼저 챌린지에 참여하세요.");

        // 식단 조건 챌린지는 '하루 총합' 기준이므로 수동 인증을 막는다.
        // (하루 중간에 인증을 확정하면 이후 추가 식단으로 조건이 깨져도 되돌릴 수 없음 → 마감 배치로만 인증)
        Challenge c = challengeMapper.findById(cno);
        if (condLabel(c) != null) {
            throw CustomException.badRequest("식단 조건 챌린지는 하루 마감 시 식단 총합으로 자동 인증돼요.");
        }

        // 인증은 챌린지 기간(진행중)에만 가능 — 모집중(시작 전)/종료 후에는 불가
        String phase = status(c);
        if ("모집중".equals(phase)) {
            throw CustomException.badRequest("아직 시작 전이에요. 챌린지 시작일부터 인증할 수 있어요.");
        }
        if ("종료".equals(phase)) {
            throw CustomException.badRequest("이미 종료된 챌린지예요.");
        }

        LocalDate today = LocalDate.now(clock);
        if (today.equals(p.getLastCheckDate())) {
            throw CustomException.badRequest("오늘은 이미 인증했어요. 내일 다시 인증할 수 있어요!");
        }

        int total = (p.getTotalDays() == null || p.getTotalDays() == 0) ? 7 : p.getTotalDays();
        if (nz(p.getDoneDays()) >= total) {
            throw CustomException.badRequest("이미 챌린지를 완료했어요! 🎉");
        }

        int done = Math.min(total, nz(p.getDoneDays()) + 1);
        int progress = (int) Math.round(done * 100.0 / total);
        challengeMapper.updateParticipant(cno, mno, done, progress, today);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("doneDays", done);
        m.put("totalDays", total);
        m.put("progress", progress);
        m.put("checkedToday", true);
        return m;
    }

    private static final Set<String> COND_TYPES =
            Set.of("DAILY_KCAL_MAX", "DAILY_PROTEIN_MIN", "WEEKLY_KCAL_MAX", "DAILY_WATER_MIN");

    @Transactional
    public Integer create(Integer mno, String title, String description,
                          String startDate, String endDate, String imageUrl,
                          String condType, Integer condValue) {
        // 조건 검증: 허용된 타입 + 양수 기준값일 때만 저장
        boolean hasCond = condType != null && COND_TYPES.contains(condType);
        if (hasCond && (condValue == null || condValue <= 0)) {
            throw CustomException.badRequest("인증 조건의 기준값을 1 이상으로 입력해주세요.");
        }
        // 기간 검증: 시작일은 내일 이후, 종료일은 시작일 이후
        LocalDate start = parse(startDate);
        LocalDate end = parse(endDate);
        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);
        if (start == null || start.isBefore(tomorrow)) {
            throw CustomException.badRequest("시작일은 내일 이후로 선택해 주세요.");
        }
        if (end == null || !end.isAfter(start)) {
            throw CustomException.badRequest("종료일은 시작일 이후로 선택해 주세요.");
        }

        Challenge c = new Challenge();
        c.setMno(mno);
        c.setTitle(InputSanitizer.required(title, "챌린지 제목", 100));
        c.setDescription(InputSanitizer.optional(description, "챌린지 설명", 1000));
        c.setStartDate(start);
        c.setEndDate(end);
        c.setImageUrl(imageUrl);
        c.setCondType(hasCond ? condType : null);
        c.setCondValue(hasCond ? condValue : null);
        // 사용자 등록은 운영자 승인 전까지 비공개(PENDING)
        c.setApprovalStatus("PENDING");
        c.setVisibility("VISIBLE");
        challengeMapper.insert(c);
        return c.getCno();
    }

    // ===== 운영 콘솔: 승인/거부/숨김/삭제 =====
    private static final Set<String> APPROVALS = Set.of("PENDING", "APPROVED", "REJECTED");

    @Transactional
    public void setApproval(Integer cno, String status) {
        if (!APPROVALS.contains(status)) throw CustomException.badRequest("잘못된 승인 상태입니다.");
        if (challengeMapper.findById(cno) == null) throw CustomException.notFound("챌린지를 찾을 수 없습니다.");
        challengeMapper.setApproval(cno, status);
    }

    @Transactional
    public void setVisibility(Integer cno, boolean visible) {
        if (challengeMapper.findById(cno) == null) throw CustomException.notFound("챌린지를 찾을 수 없습니다.");
        challengeMapper.setVisibility(cno, visible ? "VISIBLE" : "HIDDEN");
    }

    /** 운영 삭제: 참가자가 있으면 hard delete 대신 숨김 처리(데이터 보존) */
    @Transactional
    public String opsDelete(Integer cno) {
        Challenge c = challengeMapper.findById(cno);
        if (c == null) throw CustomException.notFound("챌린지를 찾을 수 없습니다.");
        if (nz(c.getParticipantCount()) > 0) {
            challengeMapper.setVisibility(cno, "HIDDEN");
            return "참가자가 있어 삭제 대신 숨김 처리했어요.";
        }
        challengeMapper.delete(cno);
        return "삭제했어요.";
    }

    /** 참여: 조건·서약 없이 자유롭게. 종료된 챌린지만 불가 */
    @Transactional
    public void join(Integer cno, Integer mno) {
        if (challengeMapper.findParticipant(cno, mno) != null) {
            throw CustomException.badRequest("이미 참여 중인 챌린지입니다.");
        }
        Challenge c = challengeMapper.findById(cno);
        if (c == null) throw CustomException.notFound("챌린지를 찾을 수 없습니다.");
        // 참여는 모집 기간(시작 전)에만 가능
        String phase = status(c);
        if ("종료".equals(phase)) {
            throw CustomException.badRequest("이미 종료된 챌린지예요.");
        }
        if (!"모집중".equals(phase)) {
            throw CustomException.badRequest("이미 시작된 챌린지예요. 모집 기간에만 참여할 수 있어요.");
        }
        ChallengeParticipant p = new ChallengeParticipant();
        p.setCno(cno);
        p.setMno(mno);
        p.setProgress(0);
        p.setDoneDays(0);
        p.setTotalDays(totalDays(c));
        challengeMapper.insertParticipant(p);
        // 참여 시점에는 인증을 확정하지 않는다. 인증은 하루 마감 배치에서만 평가한다.
    }

    /** 참여 취소 — 모집중(시작 전)에만 가능. 시작 후엔 달성 기록 보호를 위해 불가 */
    @Transactional
    public void leave(Integer cno, Integer mno) {
        if (challengeMapper.findParticipant(cno, mno) == null) {
            throw CustomException.badRequest("참여 중인 챌린지가 아니에요.");
        }
        Challenge c = challengeMapper.findById(cno);
        if (c == null) throw CustomException.notFound("챌린지를 찾을 수 없습니다.");
        if (!"모집중".equals(status(c))) {
            throw CustomException.badRequest("이미 시작된 챌린지는 참여를 취소할 수 없어요.");
        }
        challengeMapper.deleteParticipant(cno, mno);
    }

    // ===== 자동 인증: 오늘 식단이 조건을 충족하면 시스템이 기록 =====

    /** 단일 챌린지 자동 인증 평가. 기록되면 true */
    private boolean autoCheckChallenge(Challenge c, Integer mno) {
        if (c == null || condLabel(c) == null || !"LIVE".equals(status(c))) return false;
        ChallengeParticipant p = challengeMapper.findParticipant(c.getCno(), mno);
        if (p == null) return false;
        LocalDate today = LocalDate.now(clock);
        if (today.equals(p.getLastCheckDate())) return false;          // 오늘 이미 기록됨
        int total = (p.getTotalDays() == null || p.getTotalDays() == 0) ? 7 : p.getTotalDays();
        if (nz(p.getDoneDays()) >= total) return false;                // 이미 완료
        if (!Boolean.TRUE.equals(evalCond(c, mno).get("met"))) return false;

        int done = Math.min(total, nz(p.getDoneDays()) + 1);
        challengeMapper.updateParticipant(c.getCno(), mno, done,
                (int) Math.round(done * 100.0 / total), today);
        return true;
    }

    /**
     * 회원 1명의 모든 조건 챌린지 자동 인증.
     * 식단 저장/수정 시점에는 호출하지 않는다(하루 중간 부분 총합 확정 방지).
     * 하루 마감 배치(autoCheckAllParticipants)가 사용하는 평가 로직과 동일하다.
     */
    @Transactional
    public void autoCheckIn(Integer mno) {
        for (Integer cno : challengeMapper.findJoinedCnos(mno)) {
            autoCheckChallenge(challengeMapper.findById(cno), mno);
        }
    }

    /**
     * 시작일이 도래(시작일 ≤ 오늘)했는데 참여자가 0명인 챌린지를 자동 삭제.
     * 참여는 모집중에만 가능하므로, 빈 채로 시작된 챌린지는 더 이상 참여자가 생길 수 없어 정리한다.
     * @return 삭제 건수
     */
    @Transactional
    public int purgeEmptyStartedChallenges() {
        int count = 0;
        LocalDate today = LocalDate.now(clock);
        for (Challenge c : challengeMapper.findAll()) {
            if (c.getStartDate() != null && !today.isBefore(c.getStartDate())
                    && nz(c.getParticipantCount()) == 0) {
                challengeMapper.delete(c.getCno());
                count++;
            }
        }
        return count;
    }

    /** 전체 참여자 일괄 자동 인증 (일일 마감 배치용). 기록 건수 반환 */
    @Transactional
    public int autoCheckAllParticipants() {
        int count = 0;
        for (Challenge c : challengeMapper.findAll()) {
            if (condLabel(c) == null || !"LIVE".equals(status(c))) continue;
            for (ChallengeParticipant p : challengeMapper.findParticipants(c.getCno())) {
                if (autoCheckChallenge(c, p.getMno())) count++;
            }
        }
        return count;
    }

    /**
     * AI 챌린지 Tool 용 조회 전용 상태 텍스트.
     * 참여 중인 챌린지의 진행률·조건·오늘 충족/인증 여부를 기존 평가 로직(condLabel/evalCond/status)으로 구성한다.
     * 상태를 변경하지 않는다.
     */
    public String myChallengeStatusText(Integer mno) {
        List<Integer> cnos = challengeMapper.findJoinedCnos(mno);
        if (cnos == null || cnos.isEmpty()) {
            return "[내 챌린지 상태]\n- 참여 중인 챌린지가 없습니다.";
        }
        LocalDate today = LocalDate.now(clock);
        StringBuilder sb = new StringBuilder("[내 챌린지 상태]\n");
        for (Integer cno : cnos) {
            Challenge c = challengeMapper.findById(cno);
            if (c == null) continue;
            ChallengeParticipant p = challengeMapper.findParticipant(cno, mno);
            if (p == null) continue;

            int total = (p.getTotalDays() == null || p.getTotalDays() == 0) ? totalDays(c) : p.getTotalDays();
            sb.append(String.format("- %s: %d/%d일, %d%% (%s)\n",
                    c.getTitle(), nz(p.getDoneDays()), total, nz(p.getProgress()), status(c)));

            String label = condLabel(c);
            if (label != null) {
                Map<String, Object> cond = evalCond(c, mno);
                sb.append("  조건: ").append(label).append("\n");
                sb.append("  오늘 상태: ").append(cond.get("status"))
                        .append(Boolean.TRUE.equals(cond.get("met")) ? " (충족)" : " (미충족)").append("\n");
                // '챌린지 통과까지 남은 값'을 서버가 계산해 명시 — 일반 목표(2000) 대비 남은 값과 혼동 금지
                sb.append("  챌린지 통과까지: ").append(cond.get("remainingText")).append("\n");
            }
            sb.append("  오늘 인증: ").append(today.equals(p.getLastCheckDate())
                    ? "완료" : "아직 (하루 마감 배치에서 자동 인증)").append("\n");
        }
        return sb.toString().trim();
    }

    /** 사용자 프로필: 내가 등록(신청)한 챌린지 — 승인상태 포함 */
    public List<Map<String, Object>> createdChallenges(Integer mno) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Challenge c : challengeMapper.findByCreator(mno)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("cno", c.getCno());
            m.put("emoji", c.getEmoji());
            m.put("title", c.getTitle());
            m.put("approvalStatus", c.getApprovalStatus()); // PENDING/APPROVED/REJECTED
            m.put("status", status(c));
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> myChallenges(Integer mno) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Challenge c : challengeMapper.findMyChallenges(mno)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("cno", c.getCno());
            m.put("emoji", c.getEmoji());
            m.put("title", c.getTitle());
            m.put("progress", nz(c.getProgress()));
            result.add(m);
        }
        return result;
    }

    private LocalDate parse(String s) {
        return (s != null && !s.isBlank()) ? LocalDate.parse(s) : null;
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
