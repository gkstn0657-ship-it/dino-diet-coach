package com.ssafy.nyamnyam.domain.member;

import com.ssafy.nyamnyam.common.CustomException;
import com.ssafy.nyamnyam.common.InputSanitizer;
import com.ssafy.nyamnyam.common.PasswordEncoder;
import com.ssafy.nyamnyam.domain.diet.Diet;
import com.ssafy.nyamnyam.domain.diet.DietMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final CalorieTargetCalculator calorieTargetCalculator;
    private final DietMapper dietMapper;
    private final PasswordResetMailService passwordResetMailService;

    @Transactional
    public Integer signup(Member member) {
        // 서버측 입력 검증 (클라이언트 값 그대로 신뢰하지 않음)
        String email = InputSanitizer.required(member.getEmail(), "이메일", 100);
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw CustomException.badRequest("올바른 이메일 형식이 아닙니다.");
        }
        member.setEmail(email);
        member.setName(InputSanitizer.required(member.getName(), "이름", 50));
        member.setDisease(InputSanitizer.optional(member.getDisease(), "질환", 255));
        String raw = member.getPassword();
        if (raw == null || raw.isBlank()) {
            throw CustomException.badRequest("비밀번호를 입력해 주세요.");
        }
        if (raw.length() > 72) { // BCrypt 한계 + 과대입력 방어
            throw CustomException.badRequest("비밀번호가 너무 깁니다.");
        }
        if (memberMapper.countByEmail(email) > 0) {
            throw CustomException.badRequest("이미 사용 중인 이메일입니다.");
        }
        member.setPassword(passwordEncoder.encode(raw));
        member.setRole("USER"); // 가입은 항상 일반 회원(권한 상승 차단)
        memberMapper.insert(member);
        return member.getMno();
    }

    public boolean canUseEmail(String email) {
        return memberMapper.countByEmail(email) == 0;
    }

    /** 로그인 검증 후 회원 반환 (정지 계정은 안내 후 차단) */
    public Member authenticate(String email, String rawPassword) {
        Member m = memberMapper.findByEmailAny(email);   // 정지 포함 조회
        if (m == null || !passwordEncoder.matches(rawPassword, m.getPassword())) {
            // 로그인 실패는 400 (401은 토큰 만료 처리 흐름과 구분)
            throw CustomException.badRequest("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (Boolean.FALSE.equals(m.getActive())) {
            throw CustomException.forbidden("정지된 계정입니다. 서비스 운영팀에 문의해 주세요.");
        }
        return m;
    }

    public Member getByEmail(String email) {
        Member m = memberMapper.findByEmail(email);
        if (m == null) throw CustomException.notFound("회원을 찾을 수 없습니다.");
        return m;
    }

    public List<Member> list(String keyword) {
        return memberMapper.findAll(keyword);
    }

    /** 운영 콘솔: 정지 포함 전체 회원 */
    public List<Member> listForOps(String keyword) {
        return memberMapper.findAllForOps(keyword);
    }

    /** 운영 콘솔: 회원 정지/해제 */
    @Transactional
    public void setActive(Integer mno, boolean active) {
        Member m = memberMapper.findByMno(mno);
        if (m == null) throw CustomException.notFound("회원을 찾을 수 없습니다.");
        memberMapper.updateActive(mno, active);
    }

    @Transactional
    public void updateInfo(Member member) {
        memberMapper.updateInfo(member);
    }

    @Transactional
    public void updateProfile(String email, Member profile) {
        profile.setEmail(email);
        profile.setName(InputSanitizer.optional(profile.getName(), "이름", 50));
        profile.setDisease(InputSanitizer.optional(profile.getDisease(), "질환", 255));
        // 신체/범위 검증
        InputSanitizer.range(profile.getHeight(), "키", 80, 250);
        InputSanitizer.range(profile.getWeight(), "몸무게", 20, 400);
        InputSanitizer.range(profile.getBirthYear(), "출생연도", 1900, LocalDate.now().getYear());
        // 성별/목표유형 정규화
        Gender gender = Gender.from(profile.getGender());
        profile.setGender(gender == null ? null : gender.code());
        GoalType goalType = GoalType.from(profile.getGoalType());
        profile.setGoalType(goalType == null ? null : goalType.name());
        if (goalType != null) profile.setGoal(goalType.label()); // 표시용 한글 목표 동기화
        // 활동량 설문 점수 범위 검증(직업활동량 1~4, 나머지 0~4)
        InputSanitizer.range(profile.getJobActivity(), "직업 활동량", 1, 4);
        InputSanitizer.range(profile.getExerciseFrequency(), "운동 빈도", 0, 4);
        InputSanitizer.range(profile.getExerciseIntensity(), "운동 강도", 0, 4);
        InputSanitizer.range(profile.getDailySteps(), "하루 걸음 수", 0, 4);
        InputSanitizer.range(profile.getWeeklyExerciseHours(), "주당 운동 시간", 0, 4);

        // 계산에 필요한 값이 모두 있으면 목표 칼로리 계산·저장 (없으면 미설정 → 기본 2000 폴백)
        if (canComputeTarget(profile, gender, goalType)) {
            int age = Math.max(1, LocalDate.now().getYear() - profile.getBirthYear());
            CalorieTargetCalculator.Result r = calorieTargetCalculator.calculate(
                    gender, age, profile.getHeight(), profile.getWeight(), goalType,
                    profile.getJobActivity(), profile.getExerciseFrequency(), profile.getExerciseIntensity(),
                    profile.getDailySteps(), profile.getWeeklyExerciseHours());
            profile.setTargetCalories(r.targetCalories());
            profile.setActivityLevel(r.activityLevel());
        } else {
            profile.setTargetCalories(null);
            profile.setActivityLevel(null);
        }
        memberMapper.updateProfile(profile);
    }

    /** 목표 칼로리를 계산할 수 있는 모든 입력이 갖춰졌는지 */
    private boolean canComputeTarget(Member p, Gender gender, GoalType goalType) {
        return gender != null && goalType != null
                && p.getHeight() != null && p.getWeight() != null && p.getBirthYear() != null
                && p.getJobActivity() != null && p.getExerciseFrequency() != null
                && p.getExerciseIntensity() != null && p.getDailySteps() != null
                && p.getWeeklyExerciseHours() != null;
    }

    @Transactional
    public void delete(Integer mno) {
        memberMapper.delete(mno);
    }

    /** 최소 비밀번호 길이 */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * 로그인 사용자의 비밀번호 변경.
     * 현재 비밀번호 일치 + 새 비밀번호 길이/확인/중복 검증 후 암호화 저장.
     */
    @Transactional
    public void changePassword(String email, String currentPassword,
                               String newPassword, String newPasswordConfirm) {
        Member m = getByEmail(email);

        if (currentPassword == null || !passwordEncoder.matches(currentPassword, m.getPassword())) {
            throw CustomException.badRequest("현재 비밀번호가 올바르지 않습니다.");
        }
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw CustomException.badRequest("새 비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            throw CustomException.badRequest("새 비밀번호 확인이 일치하지 않습니다.");
        }
        if (passwordEncoder.matches(newPassword, m.getPassword())) {
            throw CustomException.badRequest("새 비밀번호가 기존 비밀번호와 같습니다.");
        }

        memberMapper.updatePassword(m.getMno(), passwordEncoder.encode(newPassword));
    }

    // ===== 개인 목표 칼로리 (저장된 계산값 기반) =====
    private static final int DEFAULT_TARGET_KCAL = 2000;

    /** 대시보드용: 목표/오늘 섭취/남은 칼로리 + 프로필 설정 여부 */
    public Map<String, Object> targetInfo(String email) {
        Member m = getByEmail(email);
        int target = targetKcalFor(m);
        int consumed = consumedToday(m.getMno());
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("targetCalories", target);
        r.put("consumedCalories", consumed);
        r.put("remainingCalories", target - consumed);
        r.put("hasProfile", m.getTargetCalories() != null); // 프로필 설정 완료 여부
        return r;
    }

    /** AI/내부용: 저장된 목표 칼로리(미설정이면 기본 2000) — 여기서 재계산하지 않는다. */
    public int targetKcalFor(Member m) {
        return (m != null && m.getTargetCalories() != null) ? m.getTargetCalories() : DEFAULT_TARGET_KCAL;
    }

    /** 오늘 섭취 칼로리 합계 */
    private int consumedToday(Integer mno) {
        List<Diet> diets = dietMapper.findByMember(mno, null, LocalDate.now().toString());
        if (diets == null) return 0;
        return diets.stream().mapToInt(d -> d.getTotalKcal() == null ? 0 : d.getTotalKcal()).sum();
    }

    // ===== 비밀번호 재설정 (이메일 링크 기반) =====
    private static final long RESET_TTL_MINUTES = 30;            // 30분 유효
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 재설정 요청: 계정/상태를 노출하지 않고, 활성 회원이면 토큰 발급 후 메일 발송.
     * 보안: 원본 토큰은 메일로만 전달하고 DB 에는 SHA-256 해시만 저장(유출 시 재사용 방지).
     * 기존 미사용 토큰은 무효화해 이전 링크 재사용을 막는다.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null) return;
        Member m = memberMapper.findByEmailAny(email.trim());
        // 계정 없음/정지 계정은 아무 작업 없이 동일 응답 (존재·상태 비노출)
        if (m == null || Boolean.FALSE.equals(m.getActive())) return;

        memberMapper.invalidateActiveTokens(m.getMno()); // 이전 미사용 토큰 무효화
        String rawToken = generateRawToken();
        memberMapper.insertResetToken(m.getMno(), sha256(rawToken),
                LocalDateTime.now().plusMinutes(RESET_TTL_MINUTES));
        passwordResetMailService.sendResetLink(m.getEmail(), rawToken);
    }

    /** 재설정 실행: 토큰(해시 조회) 검증 + 새 비밀번호 정책 + 1회용 처리 */
    @Transactional
    public void resetPassword(String token, String newPassword, String newPasswordConfirm) {
        PasswordResetToken t = (token == null || token.isBlank())
                ? null : memberMapper.findResetToken(sha256(token.trim()));
        if (t == null || Boolean.TRUE.equals(t.getUsed()) || t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw CustomException.badRequest("유효하지 않거나 만료된 재설정 링크입니다.");
        }
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw CustomException.badRequest("새 비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            throw CustomException.badRequest("새 비밀번호 확인이 일치하지 않습니다.");
        }
        Member m = memberMapper.findByMno(t.getMno());
        if (m == null) throw CustomException.badRequest("회원을 찾을 수 없습니다.");
        if (passwordEncoder.matches(newPassword, m.getPassword())) {
            throw CustomException.badRequest("기존 비밀번호와 동일합니다.");
        }
        memberMapper.updatePassword(t.getMno(), passwordEncoder.encode(newPassword));
        memberMapper.markResetTokenUsed(t.getId());
    }

    /** 예측 불가능한 원본 토큰(32바이트 난수 → 64 hex) */
    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /** 토큰 해시(SHA-256) — DB 저장/조회는 항상 이 값으로 */
    private String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 미지원 환경", e);
        }
    }
}
