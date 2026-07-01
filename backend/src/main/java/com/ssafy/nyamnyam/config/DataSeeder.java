package com.ssafy.nyamnyam.config;

import com.ssafy.nyamnyam.domain.challenge.ChallengeService;
import com.ssafy.nyamnyam.domain.member.CalorieTargetCalculator;
import com.ssafy.nyamnyam.domain.member.Gender;
import com.ssafy.nyamnyam.domain.member.GoalType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 초기 시드: members 테이블이 비어 있을 때(=최초 1회)만 seed.sql 실행.
 * 재시작해도 기존 데이터를 보존한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final CalorieTargetCalculator calorieTargetCalculator;
    private final ChallengeService challengeService;

    @Override
    public void run(String... args) {
        // 1) 전체 시드: members 가 비어 있을 때(최초 1회)만
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM members", Integer.class);
        if (count == null || count == 0) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("seed.sql"));
            populator.execute(dataSource);
            log.info("[시드] 빈 DB 감지 - seed.sql 초기 데이터 적재 완료");
        } else {
            log.info("[시드] 기존 데이터 유지 (members {}명) - 전체 시드 생략", count);
        }

        // 0) 마이그레이션: 기존 DB에 pledge 컬럼이 없으면 추가
        //    (schema.sql 은 CREATE TABLE IF NOT EXISTS 라 기존 테이블엔 컬럼이 안 생김)
        Integer pledgeCol = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = 'challenge_participants' AND column_name = 'pledge'",
                Integer.class);
        if (pledgeCol != null && pledgeCol == 0) {
            jdbcTemplate.execute("ALTER TABLE challenge_participants ADD COLUMN pledge VARCHAR(100) NULL AFTER total_days");
            log.info("[마이그레이션] challenge_participants.pledge 컬럼 추가");
        }
        Integer condCol = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = 'challenges' AND column_name = 'cond_type'",
                Integer.class);
        if (condCol != null && condCol == 0) {
            jdbcTemplate.execute("ALTER TABLE challenges ADD COLUMN cond_type VARCHAR(30) NULL AFTER image_url");
            jdbcTemplate.execute("ALTER TABLE challenges ADD COLUMN cond_value INT NULL AFTER cond_type");
            log.info("[마이그레이션] challenges.cond_type / cond_value 컬럼 추가");
        }
        // 운영 숨김 / 개인 목표 칼로리 입력값 컬럼 (기존 DB 보강)
        addColumnIfMissing("posts", "hidden",
                "ALTER TABLE posts ADD COLUMN hidden TINYINT(1) NOT NULL DEFAULT 0 AFTER likes");
        addColumnIfMissing("members", "gender",
                "ALTER TABLE members ADD COLUMN gender VARCHAR(10) NULL AFTER goal");
        addColumnIfMissing("members", "birth_year",
                "ALTER TABLE members ADD COLUMN birth_year INT NULL AFTER gender");
        addColumnIfMissing("members", "activity_level",
                "ALTER TABLE members ADD COLUMN activity_level VARCHAR(20) NULL AFTER birth_year");
        // 목표 칼로리 개인화: 목표유형 + 활동량 설문 점수 + 계산값 저장
        addColumnIfMissing("members", "goal_type",
                "ALTER TABLE members ADD COLUMN goal_type VARCHAR(20) NULL AFTER activity_level");
        addColumnIfMissing("members", "job_activity",
                "ALTER TABLE members ADD COLUMN job_activity INT NULL AFTER goal_type");
        addColumnIfMissing("members", "exercise_frequency",
                "ALTER TABLE members ADD COLUMN exercise_frequency INT NULL AFTER job_activity");
        addColumnIfMissing("members", "exercise_intensity",
                "ALTER TABLE members ADD COLUMN exercise_intensity INT NULL AFTER exercise_frequency");
        addColumnIfMissing("members", "daily_steps",
                "ALTER TABLE members ADD COLUMN daily_steps INT NULL AFTER exercise_intensity");
        addColumnIfMissing("members", "weekly_exercise_hours",
                "ALTER TABLE members ADD COLUMN weekly_exercise_hours INT NULL AFTER daily_steps");
        addColumnIfMissing("members", "target_calories",
                "ALTER TABLE members ADD COLUMN target_calories INT NULL AFTER weekly_exercise_hours");
        // 식단 음식 영양정보 출처(식품DB/AI추정/사진분석)
        addColumnIfMissing("diet_foods", "source",
                "ALTER TABLE diet_foods ADD COLUMN source VARCHAR(20) NULL AFTER fat");
        // 챌린지 승인/노출 상태 (기존 챌린지는 APPROVED/VISIBLE 로 유지)
        addColumnIfMissing("challenges", "approval_status",
                "ALTER TABLE challenges ADD COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' AFTER cond_value");
        addColumnIfMissing("challenges", "visibility",
                "ALTER TABLE challenges ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'VISIBLE' AFTER approval_status");
        // 데모용: 모집중(미래 시작) 챌린지가 보이도록 일부를 미래 시작으로 1회 조정
        seedRecruitingDemo();
        // 불변식: 모집중(시작 전) 챌린지는 참여 달성률이 0이어야 한다(시작 전엔 인증 불가). 멱등 보정.
        resetRecruitingChallengeProgress();
        // 시작했는데 참여자 0명인 챌린지 자동 삭제(서버 가동 시 catch-up; 평소엔 일일 배치가 처리)
        try {
            int purged = challengeService.purgeEmptyStartedChallenges();
            if (purged > 0) log.info("[정리] 참여자 0명으로 시작된 챌린지 {}개 삭제", purged);
        } catch (Exception e) {
            log.warn("[정리] 빈 챌린지 정리 실패: {}", e.getMessage());
        }

        // 2) 음식 데이터는 더 이상 하드코딩으로 시드하지 않는다.
        //    음식은 전적으로 식약처 식품영양성분 API 를 원천으로 한다.
        //    - 검색 시 로컬에 없으면 API 조회 후 결과를 로컬에 적재(FoodController)
        //    - 월 1회/수동 갱신으로 로컬을 최신화(FoodSyncService)
        //    => foods 테이블에는 'API 에서 받아온 실제 데이터'만 쌓인다.

        // 3) 커뮤니티 게시글 시드: 게시글이 충분치 않으면(데모 콘텐츠 부족) 게시판별 10개씩 적재.
        //    기존 DB(초기 시드 3개)·신규 DB 모두 한 번만 채워지고, 이후 재시작 시엔 건너뛴다.
        Integer postCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts", Integer.class);
        if (postCount != null && postCount < 30) {
            ResourceDatabasePopulator postPopulator = new ResourceDatabasePopulator();
            postPopulator.addScript(new ClassPathResource("seed-posts.sql"));
            postPopulator.setSqlScriptEncoding("UTF-8"); // 한글·이모지 깨짐 방지
            postPopulator.execute(dataSource);
            log.info("[시드] 게시글 {}개 - seed-posts.sql 커뮤니티 게시글 적재 완료", postCount);
        }

        // 4) 비밀번호 정규화: 과거 SHA 방식으로 적재된 시드 계정을 BCrypt 로 맞춘다.
        //    (인증이 BCrypt 로 바뀐 뒤, 예전 DB의 시드 계정 로그인이 막히는 문제 해소)
        normalizeSeedPassword("admin@ssafy.com",  "$2b$10$syscggn3gTr5.rFl06AFyOxef7drZgkEbyZ6/MFUaYN6I1B6O3JLq");
        normalizeSeedPassword("marvin@ssafy.com", "$2b$10$6AOTKFCt2TwBNscQ/2GwyefNFY/0aW1vrEmv8LdyGROPkQ3R7W6rm");
        normalizeSeedPassword("sarah@ssafy.com",  "$2b$10$5Bn3HSwDFJypoGMe7cWnMeaftlDwz8CGaNUfkLJn47feClUBe5Fvq");
        normalizeSeedPassword("rex@ssafy.com",    "$2b$10$XsxLOwhiBpxnKIpWT9.Hy.e5zUGtRydCZ2w0PF4wv1sN.Y/gRsIdq");

        // 5) 표시명/노출 정리: 영어·더미 이름 현실화 + 운영자 표시명 통일 + 운영자 작성 글 재배정
        normalizeSeedName("marvin@ssafy.com", "Marvin",       "김민재");
        normalizeSeedName("sarah@ssafy.com",  "Sarah Miller", "이서연");
        normalizeSeedName("rex@ssafy.com",    "Coach Rex",    "박준호");
        normalizeSeedName("test@ssafy.com",   "테스트",        "정유진");
        cleanupOperatorExposure();

        // 5-1) 레거시 데모 더미 게시글 정리: 예전 demo-data.sql 이 적재했던 "…데모 게시글 #N" 글 제거.
        //      (소스는 이미 수정됐지만 기존 DB엔 남아 있어, 어느 환경이든 재시작 시 자동 정리되게 함. 멱등)
        int legacyDemoPosts = jdbcTemplate.update(
                "DELETE FROM posts WHERE content LIKE '%데모 게시글입니다%'");
        if (legacyDemoPosts > 0) {
            log.info("[정리] 레거시 데모 더미 게시글 {}건 삭제", legacyDemoPosts);
        }

        // 6) 데모 계정(test@ssafy.com) + 데모 데이터: 계정이 없을 때만 1회 적재(자기완결적 스크립트)
        Integer testCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM members WHERE email = 'test@ssafy.com'", Integer.class);
        if (testCount == null || testCount == 0) {
            ResourceDatabasePopulator demoPopulator = new ResourceDatabasePopulator();
            demoPopulator.addScript(new ClassPathResource("demo-data.sql"));
            demoPopulator.setSqlScriptEncoding("UTF-8");
            demoPopulator.execute(dataSource);
            log.info("[시드] demo-data.sql 데모 계정/데이터 적재 완료 (test@ssafy.com / 1234)");
        }

        // 7) marvin 100일 식단/통계 데이터: 식단 이력이 거의 없을 때만 1회 생성
        seedMarvinHistory();

        // 8) 커뮤니티 활성 사용자 50명 + 다양한 작성자의 게시글/댓글/팔로우(실서비스처럼)
        seedCommunityUsersAndPosts();

        // 9) 목표 칼로리 프로필 백필: 데모/시드 일반 계정에 활동량·목표유형·목표칼로리 부여(운영자 제외)
        backfillCalorieProfile("marvin@ssafy.com", "male",   1996, "BULK_UP",  2, 3, 2, 2, 2);
        backfillCalorieProfile("test@ssafy.com",   "male",   1997, "BULK_UP",  1, 2, 2, 2, 1);
        backfillCalorieProfile("sarah@ssafy.com",  "female", 1995, "DIET",     1, 1, 1, 1, 1);
        backfillCalorieProfile("rex@ssafy.com",    "male",   1988, "MAINTAIN", 3, 3, 3, 3, 3);
    }

    /** 알려진 일반 계정에 활동량 설문/목표유형/목표칼로리를 채운다(이미 target_calories 있으면 건너뜀). */
    private void backfillCalorieProfile(String email, String gender, int birthYear, String goalType,
                                        int job, int freq, int intensity, int steps, int hours) {
        List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT mno, height, weight, target_calories FROM members WHERE email = ?", email);
        if (rows.isEmpty()) return;
        java.util.Map<String, Object> row = rows.get(0);
        if (row.get("target_calories") != null) return;           // 이미 설정됨
        Object h = row.get("height"), w = row.get("weight");
        if (h == null || w == null) return;                       // 신체정보 없으면 계산 불가
        int height = ((Number) h).intValue(), weight = ((Number) w).intValue();
        int age = Math.max(1, LocalDate.now().getYear() - birthYear);
        CalorieTargetCalculator.Result r = calorieTargetCalculator.calculate(
                Gender.from(gender), age, height, weight, GoalType.from(goalType),
                job, freq, intensity, steps, hours);
        jdbcTemplate.update(
                "UPDATE members SET gender=?, birth_year=?, goal_type=?, goal=?, " +
                "job_activity=?, exercise_frequency=?, exercise_intensity=?, daily_steps=?, " +
                "weekly_exercise_hours=?, activity_level=?, target_calories=? WHERE email=?",
                gender, birthYear, goalType, GoalType.from(goalType).label(),
                job, freq, intensity, steps, hours, r.activityLevel(), r.targetCalories(), email);
        log.info("[목표칼로리] {} 프로필 백필 → {}kcal", email, r.targetCalories());
    }

    /** 시드 계정 비밀번호가 BCrypt 형식이 아니면(과거 SHA 등) 알려진 BCrypt 해시로 교체 */
    private void normalizeSeedPassword(String email, String bcryptHash) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM members WHERE email = ? AND password NOT LIKE '$2%'",
                Integer.class, email);
        if (n != null && n > 0) {
            jdbcTemplate.update("UPDATE members SET password = ? WHERE email = ?", bcryptHash, email);
            log.info("[마이그레이션] {} 비밀번호 BCrypt 정규화", email);
        }
    }

    /** 시드 계정 표시명이 옛 placeholder 그대로면 실제 이름으로 교체(사용자가 바꾼 이름은 보존) */
    private void normalizeSeedName(String email, String oldName, String newName) {
        int n = jdbcTemplate.update(
                "UPDATE members SET name = ? WHERE email = ? AND name = ?", newName, email, oldName);
        if (n > 0) log.info("[정리] {} 표시명 '{}' → '{}'", email, oldName, newName);
    }

    /** 운영자 표시명 통일('DinoDiet 운영팀') + 운영자 작성 글을 일반 회원에게 재배정(콘텐츠는 유지) */
    private void cleanupOperatorExposure() {
        int renamed = jdbcTemplate.update(
                "UPDATE members SET name = 'DinoDiet 운영팀' " +
                "WHERE role IN ('ADMIN','OPERATOR') AND name <> 'DinoDiet 운영팀'");
        // 운영팀이 일반 커뮤니티 글 작성자로 보이지 않도록, 운영자 작성 글은 일반 회원에게 넘긴다.
        Integer normalMno = firstNormalMemberMno();
        int moved = 0;
        if (normalMno != null) {
            moved = jdbcTemplate.update(
                    "UPDATE posts SET mno = ? " +
                    "WHERE mno IN (SELECT mno FROM (SELECT mno FROM members WHERE role IN ('ADMIN','OPERATOR')) t)",
                    normalMno);
        }
        if (renamed > 0 || moved > 0) {
            log.info("[정리] 운영자 표시명 {}건 · 운영자 작성 글 재배정 {}건", renamed, moved);
        }
    }

    /** 글 재배정 대상 일반 회원(코치 rex 우선, 없으면 가장 빠른 일반 회원) */
    private Integer firstNormalMemberMno() {
        List<Integer> ids = jdbcTemplate.queryForList(
                "SELECT mno FROM members WHERE role NOT IN ('ADMIN','OPERATOR') " +
                "ORDER BY (email = 'rex@ssafy.com') DESC, mno ASC LIMIT 1", Integer.class);
        return ids.isEmpty() ? null : ids.get(0);
    }

    // marvin 100일 식단 생성용 끼니 템플릿: {제목, kcal, 단백질, 탄수, 지방}
    private static final Object[][] BREAKFASTS = {
            {"현미밥과 계란후라이", 410, 13, 66, 10},
            {"오트밀과 바나나", 255, 6, 54, 3},
            {"그릭요거트와 아몬드", 260, 16, 12, 18},
            {"통밀빵과 아보카도", 330, 7, 29, 23},
    };
    private static final Object[][] LUNCHES = {
            {"닭가슴살 샐러드", 235, 34, 10, 6},
            {"연어구이와 현미밥", 590, 31, 65, 20},
            {"두부덮밥", 390, 16, 67, 7},
            {"고구마와 그릭요거트", 230, 12, 36, 4},
    };
    private static final Object[][] DINNERS = {
            {"닭가슴살과 채소", 235, 34, 10, 6},
            {"연어구이 샐러드", 350, 28, 10, 20},
            {"두부와 오트밀", 230, 13, 29, 8},
            {"현미밥과 두부", 390, 14, 67, 7},
    };
    private static final Object[][] SNACKS = {
            {"바나나", 105, 1, 27, 0},
            {"아몬드 한 줌", 160, 6, 6, 14},
            {"그릭요거트", 100, 10, 6, 4},
    };

    /** marvin 계정에 최근 100일치 식단/일일통계를 생성(이미 이력이 있으면 건너뜀). */
    private void seedMarvinHistory() {
        Integer mno = jdbcTemplate.queryForObject(
                "SELECT mno FROM members WHERE email = 'marvin@ssafy.com'", Integer.class);
        if (mno == null) return;
        Integer dietCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM diets WHERE mno = ?", Integer.class, mno);
        if (dietCount != null && dietCount >= 20) return; // 이미 충분 → 중복 생성 방지

        // 목표 칼로리 계산용 프로필 보강
        jdbcTemplate.update(
                "UPDATE members SET gender = 'male', birth_year = 1996, activity_level = 'moderate' " +
                "WHERE mno = ? AND (gender IS NULL OR birth_year IS NULL OR activity_level IS NULL)", mno);

        int days = 100;
        for (int d = days - 1; d >= 0; d--) {
            LocalDate date = LocalDate.now().minusDays(d);
            int k = 0, p = 0, c = 0, f = 0, meals = 0;

            Object[] b = BREAKFASTS[d % BREAKFASTS.length];
            insertDietWithFood(mno, "breakfast", date, b);
            k += (int)(Integer) b[1]; p += (int)(Integer) b[2]; c += (int)(Integer) b[3]; f += (int)(Integer) b[4]; meals++;

            Object[] l = LUNCHES[(d + 1) % LUNCHES.length];
            insertDietWithFood(mno, "lunch", date, l);
            k += (int)(Integer) l[1]; p += (int)(Integer) l[2]; c += (int)(Integer) l[3]; f += (int)(Integer) l[4]; meals++;

            Object[] dn = DINNERS[(d + 2) % DINNERS.length];
            insertDietWithFood(mno, "dinner", date, dn);
            k += (int)(Integer) dn[1]; p += (int)(Integer) dn[2]; c += (int)(Integer) dn[3]; f += (int)(Integer) dn[4]; meals++;

            if (d % 3 == 0) { // 3일에 한 번 간식
                Object[] s = SNACKS[d % SNACKS.length];
                insertDietWithFood(mno, "snack", date, s);
                k += (int)(Integer) s[1]; p += (int)(Integer) s[2]; c += (int)(Integer) s[3]; f += (int)(Integer) s[4]; meals++;
            }

            jdbcTemplate.update(
                    "INSERT IGNORE INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) " +
                    "VALUES (?,?,?,?,?,?,?)",
                    mno, date.toString(), k, p, c, f, meals);
        }
        log.info("[시드] marvin 100일 식단/통계 생성 완료");
    }

    /** 식단 1건 + 동일 합계의 음식 1건을 함께 삽입(자동 생성 dno 사용) */
    private void insertDietWithFood(int mno, String meal, LocalDate date, Object[] m) {
        String title = (String) m[0];
        int kcal = (int)(Integer) m[1], protein = (int)(Integer) m[2], carbs = (int)(Integer) m[3], fat = (int)(Integer) m[4];
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) " +
                    "VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, mno);
            ps.setString(2, meal);
            ps.setString(3, date.toString());
            ps.setString(4, title);
            ps.setInt(5, kcal);
            ps.setInt(6, protein);
            ps.setInt(7, carbs);
            ps.setInt(8, fat);
            return ps;
        }, kh);
        Number dno = kh.getKey();
        if (dno != null) {
            jdbcTemplate.update(
                    "INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (?,?,?,?,?,?)",
                    dno.intValue(), title, kcal, protein, carbs, fat);
        }
    }

    // 커뮤니티 활성 사용자 이름(50명) — 실제 사용자처럼 보이도록
    private static final String[] COMMUNITY_NAMES = {
            "김지훈", "이수민", "박서준", "최예은", "정우성", "강민지", "조태현", "윤하늘", "장서아", "임도현",
            "한지민", "오현석", "서유나", "신동욱", "권나래", "황재민", "안소희", "송민호", "류현진", "전지현",
            "김하준", "이준혁", "박소연", "최민석", "정다혜", "강시현", "조유빈", "윤성호", "장예진", "임채린",
            "한승우", "오지안", "서다은", "신유진", "권혁수", "황보름", "안태민", "송가영", "류진우", "전수아",
            "김연우", "이호철", "박지영", "최우진", "정해성", "강보경", "조성민", "윤다인", "장현수", "임수빈",
    };

    // 커뮤니티 게시글 풀 {게시판, 제목, 본문} — 사람이 쓴 듯한 자연스러운 내용
    private static final String[][] COMMUNITY_POSTS = {
            {"review", "닭가슴살 브랜드 5곳 비교 후기", "온라인에서 파는 닭가슴살 다섯 군데를 주문해서 먹어봤어요. 부드러운 정도랑 간 차이가 꽤 큰데, 저는 저염 제품이 안 질려서 제일 오래 먹게 되더라고요. 자세한 표는 댓글에 정리할게요."},
            {"free", "드디어 첫 5kg 감량 성공했어요!", "3개월 동안 식단 기록하면서 천천히 뺐는데 오늘 드디어 몸무게 앞자리가 바뀌었어요. 폭식한 날도 많았지만 포기 안 하길 잘했네요. 다들 화이팅입니다!"},
            {"qna", "아침을 도저히 못 먹겠는데 꼭 먹어야 하나요?", "원래 아침을 거의 안 먹는데 다이어트엔 아침이 중요하다고 해서요. 억지로라도 챙기는 게 나을까요, 아니면 안 맞으면 점심부터 잘 먹는 게 나을까요?"},
            {"expert", "감량할수록 단백질을 더 챙겨야 하는 이유", "칼로리를 줄이면 근육부터 빠지기 쉬워요. 그래서 감량기일수록 단백질 비중을 높이고 근력 운동을 병행해야 근육은 지키면서 체지방만 뺄 수 있습니다."},
            {"review", "오트밀 3종 식감 비교해봤어요", "퀵오트, 롤드오트, 스틸컷 다 먹어봤는데 식감이 완전히 달라요. 바쁜 아침엔 퀵오트, 여유 있는 주말엔 씹는 맛 좋은 롤드오트로 먹고 있습니다."},
            {"free", "야식 끊은 지 2주째, 변화 공유해요", "자기 전에 꼭 뭔가 먹었는데 2주 끊으니 아침에 덜 붓고 속도 편해요. 처음 사흘이 고비였고 그 뒤론 생각보다 할 만하네요."},
            {"qna", "운동 끝나고 바로 먹는 게 좋을까요?", "헬스 끝나고 집까지 한 시간쯤 걸리는데, 끝나자마자 단백질 챙기는 거랑 집에서 제대로 밥 먹는 거 차이가 클까요? 경험담 듣고 싶어요."},
            {"expert", "치팅데이를 죄책감 없이 쓰는 법", "치팅데이는 무너지는 날이 아니라 계획된 보상이에요. 한 주에 한 끼 정도로 정하고 다음 끼니에 바로 평소 식단으로 돌아오면 오히려 장기적으로 도움이 됩니다."},
            {"review", "두부면으로 비빔국수 만들어봤어요", "면 요리가 너무 당겨서 두부면으로 비빔국수 해봤는데, 칼로리는 절반인데 양념 덕에 만족도는 비슷했어요. 다이어트 중 면 생각날 때 강력 추천합니다."},
            {"free", "식단 같이 인증할 분 구해요", "혼자 하니까 자꾸 느슨해지네요. 매일 한 끼라도 같이 인증하면서 서로 자극받을 분 있으면 좋겠어요. 관심 있으면 댓글 주세요!"},
            {"review", "편의점 단백질 음료 비교", "편의점에서 파는 단백질 음료들 맛이랑 단백질량, 가격 기준으로 마셔봤어요. 운동 직후엔 당 적은 게, 식사 대용엔 단백질 20g 이상이 낫더라고요."},
            {"qna", "체중이 일주일째 그대로예요. 정체기일까요?", "식단도 지키고 운동도 하는데 체중이 멈췄어요. 정체기인지 그냥 수분 변동인지 헷갈리네요. 이럴 때 뭘 점검해야 할까요?"},
            {"expert", "단백질은 한 번에 몰아 먹어도 될까", "한 끼에 흡수되는 단백질에는 한계가 있어요. 하루 총량을 세 끼에서 네 끼로 나눠 끼니마다 일정량을 채우는 게 근합성에 더 효과적입니다."},
            {"free", "AI 코치한테 식단 분석 받아봤는데 신기하네요", "오늘 먹은 거 분석해 달라니까 칼로리랑 영양 균형까지 짚어주는데 꽤 정확해서 놀랐어요. 잔소리 안 듣고 피드백 받는 느낌이라 좋습니다 ㅎㅎ"},
            {"review", "고구마를 간식으로 바꿨더니", "오후에 단 거 당길 때 고구마 하나 먹으니 포만감이 오래가서 저녁 폭식이 확 줄었어요. 운동 전 탄수 보충으로도 괜찮더라고요."},
            {"qna", "근육 늘리면서 지방 안 찌는 거 가능할까요?", "벌크업하면 지방도 같이 붙는다는데 최대한 깔끔하게 늘리는 방법이 있을까요? 칼로리를 아주 조금만 잉여로 가져가는 게 핵심일까요?"},
            {"free", "물 2L 챌린지 일주일째 성공 중", "물을 잘 안 마시는 편이었는데 챌린지 덕분에 텀블러 들고 다니면서 채우고 있어요. 확실히 군것질 욕구가 줄어든 느낌입니다."},
            {"expert", "수면 부족이 식욕에 미치는 영향", "잠이 부족하면 식욕 호르몬은 늘고 포만 호르몬은 줄어 과식하기 쉬워요. 다이어트의 시작은 사실 충분한 수면이라는 말, 진짜입니다."},
            {"review", "에어프라이어 닭가슴살 꿀팁", "냉동 닭가슴살을 에어프라이어에 돌리니 겉은 바삭 속은 촉촉하게 살아나요. 후추랑 허브솔트만 살짝 뿌려도 충분히 맛있습니다."},
            {"free", "다들 점심 뭐 드세요? 메뉴 공유해요", "매일 점심 메뉴 고르는 게 제일 어렵네요. 저는 오늘 현미밥에 닭가슴살, 샐러드 먹었어요. 다들 점심 아이디어 좀 나눠주세요!"},
            {"qna", "저녁 단백질 목표를 자꾸 못 채워요", "아침 점심은 괜찮은데 저녁에 단백질이 부족하더라고요. 자기 전에 보충하는 거 괜찮을까요? 가벼운 추천 메뉴 있으면 알려주세요."},
            {"expert", "식이섬유와 포만감의 관계", "채소와 통곡물의 식이섬유는 위에서 천천히 비워져 포만감을 오래 유지시켜요. 같은 칼로리라도 가공식품보다 자연식이 덜 먹게 되는 이유입니다."},
            {"review", "그릭요거트 아침 대용 2주 후기", "바쁜 아침에 그릭요거트에 블루베리랑 견과류 조금 올려 먹는데 만족도 최고예요. 칼로리 대비 단백질이 높아서 다이어트에도 딱입니다."},
            {"free", "주말에 과식했는데 다시 페이스 찾는 중", "주말 모임에서 평소보다 많이 먹어서 자책했는데, 다음 끼니부터 다시 잡으면 된다는 말에 마음 편하게 회복 중이에요. 죄책감보다 꾸준함이죠."},
            {"qna", "공복 유산소 효과 진짜 있나요?", "아침 공복에 유산소 하면 지방이 더 잘 빠진다는 말 많던데, 실제로 해보신 분들 효과 어떠셨어요? 어지러우면 그냥 먹고 하는 게 나을까요?"},
            {"expert", "나트륨을 줄이는 식단 구성 팁", "국물, 가공식품, 소스에 나트륨이 숨어 있어요. 같은 메뉴라도 간을 줄이고 채소를 늘리면 붓기와 혈압 관리에 큰 도움이 됩니다."},
            {"free", "식단 기록 30일째, 습관이 되니 다르네요", "처음엔 귀찮았는데 한 달 매일 기록하니까 뭘 먹는지 의식하게 되고 군것질이 자연스럽게 줄었어요. 숫자로 보이니까 동기부여가 됩니다."},
            {"review", "냉동 다이어트 도시락 2주 솔직 후기", "요리할 시간이 없어서 냉동 도시락으로 2주 버텼어요. 칼로리 계산이 자동이라 편하고, 군것질만 참으면 효과는 확실하더라고요."},
            {"qna", "단백질 100g, 보충제 없이 채우는 법?", "챌린지 참여 중인데 하루 100g이 생각보다 빡세네요. 보충제 없이 자연식으로만 채우시는 분들 식단 좀 공유해 주세요!"},
            {"expert", "유산소와 근력, 지방 연소 차이", "유산소는 운동 중 소비가, 근력은 운동 후 기초대사 상승이 강점이에요. 체지방 감량엔 둘을 병행하는 게 가장 효율적입니다."},
            {"free", "공룡 코치 캐릭터 너무 귀엽지 않나요 🦕", "AI 코치 캐릭터들이 너무 귀여워서 자꾸 들어오게 돼요. 저는 파워렉스 말투가 제일 취향입니다 ㅋㅋ 다들 누구 좋아하세요?"},
            {"review", "단백질 쉐이크 입문 3종 비교", "처음 보충제 고를 때 헷갈려서 세 종류 비교해봤어요. 유당이 안 맞으면 분리유청이 속이 편하더라고요. 물에 타는 용해도도 꼭 체크하세요."},
            {"qna", "정체기 왔을 때 칼로리를 더 줄여야 하나요?", "감량이 멈췄는데 더 줄이는 게 맞는지 아니면 잠깐 유지하면서 몸을 적응시키는 게 맞는지 모르겠어요. 경험담 부탁드려요."},
            {"expert", "물 섭취량과 신진대사", "수분이 부족하면 대사가 떨어지고 배고픔과 갈증을 혼동하기 쉬워요. 끼니 전에 물 한 컵은 과식 예방에도 도움이 됩니다."},
            {"free", "운동 끝나고 다들 뭐 드세요?", "저는 보통 단백질 쉐이크에 바나나 하나인데, 다들 운동 후 식사 루틴이 궁금하네요. 맛있는 거 추천 받아요!"},
            {"review", "통밀빵 + 아보카도 토스트 일주일 후기", "아침을 통밀빵에 아보카도로 바꿨더니 포만감이 오래 가서 좋았어요. 칼로리는 좀 있지만 점심까지 든든합니다. 계란 하나 올리면 완벽해요."},
            {"qna", "다이어트 중 외식, 뭘 골라야 안전할까요?", "약속이 많은 주인데 외식할 때 그나마 덜 부담되는 메뉴가 뭐가 있을까요? 한식 위주로 먹으려는데 팁 있으면 알려주세요."},
            {"expert", "근성장을 위한 휴식의 중요성", "근육은 운동할 때가 아니라 쉬는 동안 자라요. 같은 부위는 48시간 정도 회복 시간을 주고, 수면과 단백질로 회복을 도와야 성장합니다."},
            {"free", "냠냠코치 덕분에 3kg 뺐어요", "두 달 동안 식단 기록이랑 챌린지를 꾸준히 했더니 3kg 감량 성공했어요. 작은 습관이 진짜 중요하더라고요. 다들 포기하지 마세요!"},
            {"review", "제로 음료 한 달 마셔본 후기", "단 음료를 제로로 바꾼 지 한 달. 처음엔 어색했는데 이제 일반 음료가 너무 달게 느껴져요. 식단 유지엔 확실히 도움이 됐습니다."},
            {"qna", "근력 운동 초보인데 식단부터 바꿔야 할까요?", "운동을 막 시작했는데 식단을 같이 바꾸는 게 나을지, 운동 습관부터 잡고 나서 식단을 손대는 게 나을지 고민이에요."},
            {"expert", "탄수화물 섭취 타이밍 정리", "운동 전에는 소화가 빠른 탄수, 운동 후에는 단백질과 함께 탄수를 보충하면 회복에 유리해요. 결국 하루 총량 안에서의 배분이 핵심입니다."},
            {"free", "같이 챌린지 하실 분 모집해요", "혼자 하면 자꾸 빼먹게 돼서요. 단백질 챌린지나 물 마시기 챌린지 같이 인증하실 분 있으면 댓글 주세요. 같이 꾸준히 해봐요!"},
            {"review", "닭가슴살 소시지 맛 순위 매겨봤어요", "시중 닭가슴살 소시지들 먹어보고 순위 매겨봤어요. 제품마다 식감이랑 간 세기 차이가 커요. 대부분 데워 먹으면 맛이 훨씬 살아납니다."},
            {"qna", "식단 기록, 매일 다 쓰기 너무 귀찮아요", "처음엔 열심히 쓰다가 며칠 빼먹으니 손을 놓게 되네요. 다들 어떻게 꾸준히 기록하세요? 동기 유지 팁이 필요합니다 ㅠㅠ"},
            {"expert", "다이어트 정체기, 원인부터 점검하세요", "정체기는 대부분 수분 변동이거나 무의식적 간식 때문이에요. 며칠 평균으로 보고, 기록을 다시 꼼꼼히 점검하면 원인이 보이는 경우가 많습니다."},
            {"free", "오늘 처음 가입했어요! 잘 부탁드려요", "건강하게 살 빼보려고 가입했습니다. 다들 어떻게 시작하셨는지 궁금하네요. 초보가 처음에 챙기면 좋은 거 있으면 알려주세요!"},
            {"review", "고구마 vs 현미밥 포만감 비교", "같은 칼로리로 일주일씩 먹어봤는데 저는 고구마가 포만감이 더 오래갔어요. 대신 현미밥이 끼니 구성은 더 편하더라고요. 결국 취향 차이인 듯합니다."},
            {"qna", "단백질 쉐이크는 식사 대용으로 괜찮을까요?", "바쁠 때 한 끼를 쉐이크로 때우는 게 영양적으로 괜찮은 건지 궁금해요. 매일은 아니고 가끔 점심 대용으로요."},
            {"expert", "감량 속도는 어느 정도가 적당할까", "주당 체중의 0.5~1% 정도 감량이 근손실 없이 지속 가능한 속도예요. 너무 빠른 감량은 요요와 근손실 위험이 커집니다."},
            {"free", "다이어트 중 가장 힘든 순간이 언제세요?", "저는 자기 전에 뭔가 먹고 싶을 때가 제일 힘들어요. 다들 그 고비 어떻게 넘기시나요? 꿀팁 공유 부탁드립니다!"},
            {"review", "오버나이트 오트밀 일주일 도전", "전날 밤에 미리 만들어두는 오버나이트 오트밀로 아침을 통일했더니 혈당이 천천히 올라서 그런지 오전에 덜 졸려요. 바나나랑 같이 먹으면 든든합니다."},
            {"qna", "헬스장 갈 시간이 없는데 홈트로도 될까요?", "야근이 잦아서 헬스장 등록을 못 하고 있어요. 집에서 맨몸 운동만으로도 효과 볼 수 있을까요? 추천 루틴 있으면 알려주세요."},
            {"expert", "간헐적 단식, 누구에게 맞을까", "간헐적 단식은 식사 시간을 줄여 총 섭취를 자연스럽게 낮추는 방법이에요. 다만 공복이 길어 폭식으로 이어지는 분께는 안 맞을 수 있으니 본인 패턴을 보세요."},
            {"free", "벌크업 시작 2주차 기록 공유", "근육 증가가 목표라 하루 칼로리를 조금 올려 먹고 있는데 깨끗하게 늘리는 게 생각보다 어렵네요. 다들 벌크업할 때 탄수 비중 어떻게 가져가세요?"},
            {"review", "닭가슴살 질려서 연어로 바꾼 후기", "점심을 매일 닭가슴살로 먹다 한 달 만에 질려서 연어구이로 바꿨어요. 지방이 좀 더 있어도 포만감이 좋고 맛있어서 식단 유지가 훨씬 쉬워졌습니다."},
            {"qna", "운동 전 공복이 나을까요, 먹는 게 나을까요?", "아침 운동을 하는데 공복으로 하면 힘이 안 나고 먹고 하면 더부룩해요. 가볍게 먹는다면 바나나 정도가 괜찮을까요?"},
            {"expert", "근육량을 늘리면 살이 덜 찌는 이유", "근육은 가만히 있어도 에너지를 쓰는 조직이라 근육량이 늘면 기초대사량이 올라가요. 그래서 같은 양을 먹어도 덜 찌는 체질로 바뀌는 겁니다."},
            {"free", "식단 14일 연속 기록 성공했어요!", "작심삼일이 늘 문제였는데 이번엔 2주 연속 매일 기록했어요. 기록하니까 확실히 덜 먹게 되네요. 같이 시작한 분들 다들 화이팅!"},
            {"review", "간식으로 아몬드 한 줌 추천", "단 거 당길 때 아몬드 한 줌 먹으면 고소하고 포만감도 있어서 좋아요. 다만 칼로리가 높아서 양 조절은 필수입니다. 소분해두면 편해요."},
    };

    // 댓글 풀 — 자연스러운 짧은 반응
    private static final String[] COMMUNITY_COMMENTS = {
            "좋은 정보 감사합니다!", "저도 오늘부터 도전해볼게요", "완전 공감돼요 ㅎㅎ", "꾸준함이 진짜 답인 것 같아요",
            "우와 대단하시네요, 자극받고 갑니다", "저는 반대로 아침을 꼭 챙기는 편이에요", "이거 저장해둘게요", "사진도 같이 올려주시면 좋을 것 같아요!",
            "따라 해봤는데 진짜 괜찮네요", "혹시 레시피 더 자세히 알 수 있을까요?", "저도 같은 고민이었는데 도움 됐어요", "화이팅입니다 🦕",
            "오 이런 방법이 있었군요", "공감 백 배요…", "저만 그런 게 아니었네요 ㅠㅠ", "정보 감사해요, 적용해볼게요",
            "역시 기록이 중요하네요", "멘탈 잡는 게 제일 어려운 것 같아요", "덕분에 동기부여 됐어요", "이번 주부터 같이 해봐요",
            "잘 읽었습니다, 좋은 글이네요", "저는 단백질 쉐이크로 해결했어요", "사진 보니 더 맛있어 보여요", "꿀팁 감사합니다!",
    };

    /** 커뮤니티 활성 사용자 50명 + 다양한 작성자의 게시글/댓글/팔로우 생성(이미 있으면 건너뜀) */
    private void seedCommunityUsersAndPosts() {
        Integer marker = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM members WHERE email = 'user01@nyamnyam.com'", Integer.class);
        if (marker != null && marker > 0) return;

        final String pw = "$2b$10$6AOTKFCt2TwBNscQ/2GwyefNFY/0aW1vrEmv8LdyGROPkQ3R7W6rm"; // "1234"
        final String[] genders = {"male", "female"};
        final GoalType[] goalTypes = {GoalType.DIET, GoalType.MAINTAIN, GoalType.BULK_UP};

        // 1) 활성 사용자 50명 생성 (성별·목표유형·활동량 설문·목표칼로리 다양하게)
        List<Integer> userMnos = new ArrayList<>();
        for (int i = 0; i < COMMUNITY_NAMES.length; i++) {
            final int idx = i;
            final String email = String.format("user%02d@nyamnyam.com", i + 1);
            final int height = 156 + (i * 11) % 34;
            final int weight = 47 + (i * 7) % 46;
            final int birth = 1982 + (i * 5) % 22;
            final Gender gender = Gender.from(genders[i % genders.length]);
            final GoalType goalType = goalTypes[i % goalTypes.length];
            // 활동량 설문 점수 (직업 1~4, 운동 항목 0~4) — index 기반으로 분산
            final int job = 1 + (i % 4);
            final int freq = i % 5;
            final int intensity = (i + 1) % 5;
            final int steps = (i + 2) % 5;
            final int hours = (i + 3) % 5;
            int age = Math.max(1, LocalDate.now().getYear() - birth);
            final CalorieTargetCalculator.Result calc = calorieTargetCalculator.calculate(
                    gender, age, height, weight, goalType, job, freq, intensity, steps, hours);
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO members (email,password,name,role,height,weight,goal,gender,birth_year," +
                        "goal_type,job_activity,exercise_frequency,exercise_intensity,daily_steps," +
                        "weekly_exercise_hours,activity_level,target_calories) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, email);
                ps.setString(2, pw);
                ps.setString(3, COMMUNITY_NAMES[idx]);
                ps.setString(4, "USER");
                ps.setInt(5, height);
                ps.setInt(6, weight);
                ps.setString(7, goalType.label());
                ps.setString(8, gender.code());
                ps.setInt(9, birth);
                ps.setString(10, goalType.name());
                ps.setInt(11, job);
                ps.setInt(12, freq);
                ps.setInt(13, intensity);
                ps.setInt(14, steps);
                ps.setInt(15, hours);
                ps.setString(16, calc.activityLevel());
                ps.setInt(17, calc.targetCalories());
                return ps;
            }, kh);
            if (kh.getKey() != null) userMnos.add(kh.getKey().intValue());
        }
        if (userMnos.isEmpty()) return;

        // 작성자 풀: 신규 50명 + 데모/시드 회원(운영자 제외)
        List<Integer> authors = new ArrayList<>(userMnos);
        for (String e : new String[]{"test@ssafy.com", "marvin@ssafy.com", "sarah@ssafy.com", "rex@ssafy.com"}) {
            List<Integer> r = jdbcTemplate.queryForList("SELECT mno FROM members WHERE email = ?", Integer.class, e);
            if (!r.isEmpty()) authors.add(r.get(0));
        }

        // 2) 게시글 + 댓글 (작성 시점을 과거 ~70일에 분포시켜 실제 운영처럼)
        int postCount = 0, commentCount = 0;
        for (int i = 0; i < COMMUNITY_POSTS.length; i++) {
            final String board = COMMUNITY_POSTS[i][0];
            final String title = COMMUNITY_POSTS[i][1];
            final String body = COMMUNITY_POSTS[i][2];
            final int author = authors.get((i * 17 + 5) % authors.size());
            final int likes = (i * 23 + 7) % 96;
            int daysAgo = (i % 6 == 0) ? 0 : ((i * 7 + 3) % 70) + 1; // 6개마다 오늘 글(오늘 TOP3용)
            final LocalDateTime created = LocalDate.now().minusDays(daysAgo).atTime((i * 5) % 24, (i * 13) % 60);
            final Timestamp ts = Timestamp.valueOf(created);
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO posts (mno,board,title,content,likes,created_at) VALUES (?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, author);
                ps.setString(2, board);
                ps.setString(3, title);
                ps.setString(4, body);
                ps.setInt(5, likes);
                ps.setTimestamp(6, ts);
                return ps;
            }, kh);
            if (kh.getKey() == null) continue;
            final int bno = kh.getKey().intValue();
            postCount++;

            int cc = i % 5; // 게시글당 댓글 0~4개
            for (int c = 0; c < cc; c++) {
                final int commenter = authors.get((i * 11 + c * 7 + 1) % authors.size());
                if (commenter == author && authors.size() > 1) continue;
                final String ctext = COMMUNITY_COMMENTS[(i * 3 + c) % COMMUNITY_COMMENTS.length];
                final Timestamp cts = Timestamp.valueOf(created.plusHours(c + 1).plusMinutes((c * 17) % 60));
                jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO comments (bno,mno,content,created_at) VALUES (?,?,?,?)");
                    ps.setInt(1, bno);
                    ps.setInt(2, commenter);
                    ps.setString(3, ctext);
                    ps.setTimestamp(4, cts);
                    return ps;
                });
                commentCount++;
            }
        }

        // 3) 팔로우 관계 (신규 사용자 간)
        int n = userMnos.size();
        for (int i = 0; i < n; i++) {
            for (int off : new int[]{3, 11, 23}) {
                int follower = userMnos.get(i);
                int following = userMnos.get((i + off) % n);
                if (follower != following) {
                    jdbcTemplate.update(
                            "INSERT IGNORE INTO follows (follower_mno, following_mno) VALUES (?,?)",
                            follower, following);
                }
            }
        }
        log.info("[시드] 커뮤니티 사용자 {}명 + 게시글 {}개 + 댓글 {}개 생성 완료", n, postCount, commentCount);
    }

    /** 데모용: 일부 챌린지를 미래 시작(모집중)으로 1회 조정 — '모집중' 필터 시연용 */
    private void seedRecruitingDemo() {
        Integer done = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_meta WHERE meta_key = 'challenge_recruiting_demo'", Integer.class);
        if (done != null && done > 0) return;
        try {
            // cno % 3 == 0 인 챌린지를 3일 뒤 시작 ~ 17일 뒤 종료로 (전체의 약 1/3을 모집중으로)
            int n = jdbcTemplate.update(
                    "UPDATE challenges SET start_date = DATE_ADD(CURDATE(), INTERVAL 3 DAY), " +
                    "end_date = DATE_ADD(CURDATE(), INTERVAL 17 DAY) WHERE cno % 3 = 0");
            jdbcTemplate.update(
                    "INSERT INTO app_meta (meta_key, meta_value) VALUES ('challenge_recruiting_demo', ?) " +
                    "ON DUPLICATE KEY UPDATE meta_value = VALUES(meta_value)",
                    LocalDate.now().toString());
            if (n > 0) log.info("[시드] 챌린지 {}개를 모집중(미래 시작)으로 조정", n);
        } catch (Exception e) {
            log.warn("[시드] 모집중 데모 조정 실패: {}", e.getMessage());
        }
    }

    /** 모집중(시작 전) 챌린지의 참여 달성률을 0으로 보정(시작 전엔 인증 불가하므로). 멱등 */
    private void resetRecruitingChallengeProgress() {
        try {
            int n = jdbcTemplate.update(
                    "UPDATE challenge_participants cp " +
                    "JOIN challenges c ON c.cno = cp.cno " +
                    "SET cp.progress = 0, cp.done_days = 0, cp.last_check_date = NULL " +
                    "WHERE c.start_date > CURDATE() " +
                    "AND (cp.done_days > 0 OR cp.progress > 0 OR cp.last_check_date IS NOT NULL)");
            if (n > 0) log.info("[정리] 모집중 챌린지 참여 달성률 {}건 초기화", n);
        } catch (Exception e) {
            log.warn("[정리] 모집중 참여 달성률 초기화 실패: {}", e.getMessage());
        }
    }

    /** 기존 테이블에 컬럼이 없으면 ALTER 로 추가 (schema.sql 의 CREATE IF NOT EXISTS 한계 보완) */
    private void addColumnIfMissing(String table, String column, String alterSql) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class, table, column);
        if (c != null && c == 0) {
            jdbcTemplate.execute(alterSql);
            log.info("[마이그레이션] {}.{} 컬럼 추가", table, column);
        }
    }
}
