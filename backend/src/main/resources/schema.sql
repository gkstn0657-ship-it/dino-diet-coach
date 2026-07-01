-- ============================================================
-- 냠냠코치 스키마 (영속형: 없으면 생성, 있으면 유지 → 재시작해도 데이터 보존)
-- ============================================================

CREATE TABLE IF NOT EXISTS members (
    mno         INT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    height      INT,
    weight      INT,
    disease     VARCHAR(255),
    goal        VARCHAR(255),
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS foods (
    fno      INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    kcal     INT NOT NULL,
    protein  INT NOT NULL DEFAULT 0,
    carbs    INT NOT NULL DEFAULT 0,
    fat      INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 정책: 같은 회원의 (eaten_date, meal) 슬롯은 1개만 둔다.
-- 지금은 애플리케이션 레벨(DietService)에서만 검사한다.
-- TODO(마이그레이션): 기존 중복 데이터 정리 후 별도 단계로
--   ALTER TABLE diets ADD CONSTRAINT uq_diet_slot UNIQUE (mno, eaten_date, meal);
--   추가를 검토할 것.
CREATE TABLE IF NOT EXISTS diets (
    dno         INT AUTO_INCREMENT PRIMARY KEY,
    mno         INT NOT NULL,
    meal        VARCHAR(20) NOT NULL,
    eaten_date  DATE NOT NULL,
    title       VARCHAR(100) NOT NULL,
    photo_url   VARCHAR(500),
    total_kcal  INT NOT NULL DEFAULT 0,
    protein     INT NOT NULL DEFAULT 0,
    carbs       INT NOT NULL DEFAULT 0,
    fat         INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_diet_member FOREIGN KEY (mno) REFERENCES members(mno) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS diet_foods (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    dno     INT NOT NULL,
    fno     INT,
    name    VARCHAR(100) NOT NULL,
    kcal    INT NOT NULL DEFAULT 0,
    protein INT NOT NULL DEFAULT 0,
    carbs   INT NOT NULL DEFAULT 0,
    fat     INT NOT NULL DEFAULT 0,
    source  VARCHAR(20),                 -- 영양정보 출처: DB | AI | PHOTO
    CONSTRAINT fk_dietfood_diet FOREIGN KEY (dno) REFERENCES diets(dno) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS challenges (
    cno         INT AUTO_INCREMENT PRIMARY KEY,
    mno         INT NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    emoji       VARCHAR(10) DEFAULT '🏆',
    color       VARCHAR(20) DEFAULT '#77C0B3',
    start_date  DATE,
    end_date    DATE,
    image_url   VARCHAR(500),
    cond_type   VARCHAR(30),                -- 식단 인증 조건: DAILY_KCAL_MAX | DAILY_PROTEIN_MIN | WEEKLY_KCAL_MAX | DAILY_WATER_MIN | NULL
    cond_value  INT,                        -- 조건 기준값 (kcal/g/잔)
    approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',  -- PENDING | APPROVED | REJECTED (사용자 신청은 PENDING)
    visibility      VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',   -- VISIBLE | HIDDEN (운영 숨김)
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_challenge_member FOREIGN KEY (mno) REFERENCES members(mno) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS challenge_participants (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    cno             INT NOT NULL,
    mno             INT NOT NULL,
    progress        INT NOT NULL DEFAULT 0,
    done_days       INT NOT NULL DEFAULT 0,
    total_days      INT NOT NULL DEFAULT 7,
    pledge          VARCHAR(100),
    last_check_date DATE,
    joined_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cp_challenge FOREIGN KEY (cno) REFERENCES challenges(cno) ON DELETE CASCADE,
    CONSTRAINT fk_cp_member FOREIGN KEY (mno) REFERENCES members(mno) ON DELETE CASCADE,
    UNIQUE KEY uq_cp (cno, mno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS posts (
    bno         INT AUTO_INCREMENT PRIMARY KEY,
    mno         INT NOT NULL,
    board       VARCHAR(20) NOT NULL,
    title       VARCHAR(200) NOT NULL,
    content     TEXT NOT NULL,
    likes       INT NOT NULL DEFAULT 0,
    hidden      TINYINT(1) NOT NULL DEFAULT 0,   -- 운영 숨김 처리(목록/상세/TOP3 비노출)
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_member FOREIGN KEY (mno) REFERENCES members(mno) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comments (
    cno         INT AUTO_INCREMENT PRIMARY KEY,
    bno         INT NOT NULL,
    mno         INT NOT NULL,
    content     VARCHAR(500) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_post FOREIGN KEY (bno) REFERENCES posts(bno) ON DELETE CASCADE,
    CONSTRAINT fk_comment_member FOREIGN KEY (mno) REFERENCES members(mno) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS follows (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    follower_mno  INT NOT NULL,
    following_mno INT NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_follow (follower_mno, following_mno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 물 섭취 기록 (사용자·날짜별 잔 수) — 메인 물섭취 + 물 챌린지 자동 인증 근거
CREATE TABLE IF NOT EXISTS water_logs (
    mno        INT NOT NULL,
    log_date   DATE NOT NULL,
    cups       INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (mno, log_date),
    CONSTRAINT fk_water_member FOREIGN KEY (mno) REFERENCES members(mno) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 앱 메타(키-값) — 식품 데이터 마지막 갱신일 등 운영 상태 저장
CREATE TABLE IF NOT EXISTS app_meta (
    meta_key    VARCHAR(50) PRIMARY KEY,
    meta_value  VARCHAR(200),
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 비밀번호 재설정 토큰 (이메일 기반 재설정 흐름, 만료/1회용)
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    mno         INT NOT NULL,
    token       VARCHAR(100) NOT NULL,
    expires_at  DATETIME NOT NULL,
    used        TINYINT(1) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prt_member FOREIGN KEY (mno) REFERENCES members(mno) ON DELETE CASCADE,
    KEY idx_prt_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 게시글 좋아요 (사용자별 1회 — uq_post_like 로 중복 방지)
CREATE TABLE IF NOT EXISTS post_likes (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    bno         INT NOT NULL,
    mno         INT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_like_post   FOREIGN KEY (bno) REFERENCES posts(bno)   ON DELETE CASCADE,
    CONSTRAINT fk_like_member FOREIGN KEY (mno) REFERENCES members(mno) ON DELETE CASCADE,
    UNIQUE KEY uq_post_like (mno, bno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- AI 코치 대화 기록 (브라우저를 닫아도 대화 유지, 90일 경과분은 배치로 삭제)
CREATE TABLE IF NOT EXISTS chat_messages (
    cmno       INT AUTO_INCREMENT PRIMARY KEY,
    mno        INT NOT NULL,
    coach      VARCHAR(20) NOT NULL,            -- powerrex | slimdino | balanceno
    role       VARCHAR(10) NOT NULL,            -- user | assistant
    content    TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_member FOREIGN KEY (mno) REFERENCES members(mno) ON DELETE CASCADE,
    KEY idx_chat_member_coach (mno, coach, cmno),
    KEY idx_chat_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_nutrition_stats (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    mno         INT NOT NULL,
    stat_date   DATE NOT NULL,
    total_kcal  INT NOT NULL DEFAULT 0,
    protein     INT NOT NULL DEFAULT 0,
    carbs       INT NOT NULL DEFAULT 0,
    fat         INT NOT NULL DEFAULT 0,
    meal_count  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_stat (mno, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
