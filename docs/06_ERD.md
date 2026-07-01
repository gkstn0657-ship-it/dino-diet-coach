# DB Modeling ERD — 냠냠코치

> 근거: `backend/src/main/resources/schema.sql` (MySQL 8, InnoDB, utf8mb4)

## 1. ERD

```mermaid
erDiagram
    MEMBERS ||--o{ DIETS : "기록"
    MEMBERS ||--o{ CHALLENGES : "생성"
    MEMBERS ||--o{ CHALLENGE_PARTICIPANTS : "참여"
    MEMBERS ||--o{ POSTS : "작성"
    MEMBERS ||--o{ COMMENTS : "작성"
    MEMBERS ||--o{ FOLLOWS : "팔로우"
    MEMBERS ||--o{ DAILY_NUTRITION_STATS : "집계"
    MEMBERS ||--o{ CHAT_MESSAGES : "대화"
    DIETS ||--o{ DIET_FOODS : "구성"
    FOODS |o--o{ DIET_FOODS : "참조"
    CHALLENGES ||--o{ CHALLENGE_PARTICIPANTS : "참여자"
    POSTS ||--o{ COMMENTS : "댓글"

    MEMBERS {
        int mno PK
        varchar email UK
        varchar password "해시"
        varchar name
        varchar role "USER/ADMIN"
        int height
        int weight
        varchar disease
        varchar goal "건강 목표"
        tinyint active
        timestamp created_at
    }

    DIETS {
        int dno PK
        int mno FK
        varchar meal "아침/점심/저녁/간식"
        date eaten_date
        varchar title
        varchar photo_url
        int total_kcal
        int protein
        int carbs
        int fat
        timestamp created_at
    }

    DIET_FOODS {
        int id PK
        int dno FK
        int fno FK "nullable"
        varchar name
        int kcal
    }

    FOODS {
        int fno PK
        varchar name
        int kcal
        int protein
        int carbs
        int fat
    }

    CHALLENGES {
        int cno PK
        int mno FK "생성자"
        varchar title
        text description
        varchar emoji
        varchar color
        date start_date
        date end_date
        varchar image_url
        timestamp created_at
    }

    CHALLENGE_PARTICIPANTS {
        int id PK
        int cno FK
        int mno FK
        int progress "달성률 %"
        int done_days
        int total_days
        date last_check_date "중복 인증 방지"
        timestamp joined_at
    }

    POSTS {
        int bno PK
        int mno FK
        varchar board "review/expert/free"
        varchar title
        text content
        int likes
        timestamp created_at
    }

    COMMENTS {
        int cno PK
        int bno FK
        int mno FK
        varchar content
        timestamp created_at
    }

    FOLLOWS {
        int id PK
        int follower_mno FK
        int following_mno FK
        timestamp created_at
    }

    DAILY_NUTRITION_STATS {
        int id PK
        int mno FK
        date stat_date
        int total_kcal
        int protein
        int carbs
        int fat
        int meal_count
        timestamp created_at
    }

    CHAT_MESSAGES {
        int cmno PK
        int mno FK
        varchar coach "powerrex/slimdino/balanceno"
        varchar role "user/assistant"
        text content
        timestamp created_at
    }
```

## 2. 설계 포인트

| 항목 | 내용 |
|------|------|
| 참조 무결성 | 회원 탈퇴 시 식단·챌린지·게시글·댓글·AI 대화 `ON DELETE CASCADE` 자동 정리 |
| 비정규화 | `diets`에 총 칼로리·매크로를 저장해 목록 조회 시 diet_foods 조인 생략 (조회 성능) |
| 유니크 제약 | `members.email`, `challenge_participants(cno,mno)` — 중복 참여 방지, `follows(follower,following)` — 중복 팔로우 방지, `daily_nutrition_stats(mno,stat_date)` — 일별 1행 |
| 인덱스 | `chat_messages(mno,coach,cmno)` 대화 복원 조회용, `chat_messages(created_at)` 90일 정리 배치용 |
| 데이터 수명 | `chat_messages`는 90일 보존 후 스케줄러가 삭제 — 저장 공간 상한 유지 |
| 통계 분리 | 일일 집계를 `daily_nutrition_stats`로 분리해 원본(diets) 스캔 없이 14일 추이 제공 |
