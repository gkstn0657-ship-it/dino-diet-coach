# Use-Case Diagram — 냠냠코치

## 1. Actor 정의

| Actor | 설명 |
|-------|------|
| 비회원 (Guest) | 가입 전 사용자. 랜딩 열람, 회원가입, 로그인만 가능 |
| 회원 (Member) | 로그인한 사용자. 식단·AI·챌린지·커뮤니티·소셜 전 기능 사용 |
| 시스템 스케줄러 (System) | 매일 자동 실행 — 영양 통계 집계(01시), AI 대화 정리(02시) |
| GMS AI (외부 시스템) | GPT-4o-mini — 식단 분석, 비전 인식, 운동 코칭 응답 생성 |
| 식품영양 API (외부 시스템) | 식약처 식품영양성분 DB — 음식 검색 |

## 2. Use-Case Diagram

```mermaid
graph LR
    Guest(["👤 비회원"])
    Member(["👤 회원"])
    Scheduler(["⏰ 시스템 스케줄러"])
    GMS(["🤖 GMS AI"])
    FoodAPI(["🍱 식품영양 API"])

    subgraph 회원·인증
        UC1([회원가입 F106])
        UC2([로그인/로그아웃 F110])
        UC3([회원정보 조회 F107])
        UC4([회원정보 수정 F108])
        UC5([회원 탈퇴 F109])
    end

    subgraph 식단 관리
        UC6([식단 기록 F101])
        UC7([식단 목록/상세 조회 F102])
        UC8([식단 수정 F103])
        UC9([식단 삭제 F104])
        UC10([영양 분석 F105])
        UC11([음식 DB 검색])
    end

    subgraph AI 에이전트
        UC12([AI 식단 종합 분석 F116])
        UC13([사진 비전 분석 F116심화])
        UC14([AI 운동 코칭 - 3종 코치 F117])
        UC15([서비스 가이드 챗봇])
    end

    subgraph 챌린지
        UC16([챌린지 목록/상세/생성 F112])
        UC17([챌린지 참여/일일 인증 F113])
    end

    subgraph 커뮤니티·소셜
        UC18([게시글 CRUD F114])
        UC19([댓글 CRUD F115])
        UC20([팔로우/프로필 F111])
    end

    subgraph 배치
        UC21([일일 영양 통계 집계])
        UC22([90일 경과 AI 대화 삭제])
    end

    Guest --> UC1
    Guest --> UC2
    Member --> UC3
    Member --> UC4
    Member --> UC5
    Member --> UC6
    Member --> UC7
    Member --> UC8
    Member --> UC9
    Member --> UC10
    Member --> UC12
    Member --> UC13
    Member --> UC14
    Member --> UC15
    Member --> UC16
    Member --> UC17
    Member --> UC18
    Member --> UC19
    Member --> UC20

    UC6 -.include.-> UC11
    UC6 -.extend.-> UC13
    UC11 --> FoodAPI
    UC12 --> GMS
    UC13 --> GMS
    UC14 --> GMS
    UC15 --> GMS

    Scheduler --> UC21
    Scheduler --> UC22
```

## 3. 주요 Use-Case 명세

### UC6. 식단 기록 (F101)

- **Actor**: 회원 / **사전조건**: 로그인 상태
- **기본 흐름**: 사진 업로드 → (선택) AI 비전 분석으로 음식·칼로리 자동 인식 → 음식 DB 검색으로 보완 → 끼니·날짜 지정 → 저장
- **대안 흐름**: AI 미설정 시 수동 입력만으로 저장
- **사후조건**: diets·diet_foods 테이블에 기록, 영양 통계 집계 대상에 포함

### UC14. AI 운동 코칭 (F117)

- **Actor**: 회원, GMS AI / **사전조건**: 로그인 상태
- **기본 흐름**: 목표에 맞는 코치 선택(PowerRex/SlimDino/BalanceNo) → 저장된 대화 복원 → 질문 입력 → 서버가 프로필+오늘 식단+최근 10턴 히스토리로 컨텍스트 구성 → GMS 호출 → 응답 표시·DB 저장
- **대안 흐름**: AI 미설정/호출 실패 시 코치별 fallback 응답
- **사후조건**: chat_messages에 질문·응답 저장 (90일 보존)

### UC17. 챌린지 참여/인증 (F113)

- **Actor**: 회원 / **사전조건**: 로그인, 챌린지 존재
- **기본 흐름**: 챌린지 상세 → 참여 → 매일 1회 인증 → 달성도(%) 증가 → 100% 시 완료
- **예외 흐름**: 같은 날 중복 인증 차단 (last_check_date 검사)
