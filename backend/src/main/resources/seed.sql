-- ============================================================
-- 초기 시드 (DataSeeder가 members 가 비어있을 때 1회만 실행)
-- 비밀번호는 모두 "1234" 의 BCrypt 해시 ($2b$10$...)
-- ============================================================

INSERT INTO members (email, password, name, role, height, weight, disease, goal) VALUES
('admin@ssafy.com', '$2b$10$syscggn3gTr5.rFl06AFyOxef7drZgkEbyZ6/MFUaYN6I1B6O3JLq', 'DinoDiet 운영팀', 'ADMIN', 175, 70, NULL,        '건강 유지'),
('marvin@ssafy.com','$2b$10$6AOTKFCt2TwBNscQ/2GwyefNFY/0aW1vrEmv8LdyGROPkQ3R7W6rm', '김민재',     'USER',  178, 72, NULL,        '근육 증가'),
('sarah@ssafy.com', '$2b$10$5Bn3HSwDFJypoGMe7cWnMeaftlDwz8CGaNUfkLJn47feClUBe5Fvq', '이서연',     'USER',  165, 55, '고혈압',    '체중 감량'),
('rex@ssafy.com',   '$2b$10$XsxLOwhiBpxnKIpWT9.Hy.e5zUGtRydCZ2w0PF4wv1sN.Y/gRsIdq', '박준호',     'USER',  180, 78, NULL,        '체력 향상');

-- 음식 데이터는 seed-foods.sql 로 일원화한다(DataSeeder 가 foods 가 비어 있을 때 적재).
-- 여기서 따로 넣지 않는다 — 중복/임시 데이터 방지.

-- 챌린지(시드) : 데모용 공개 챌린지 3개
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES
((SELECT mno FROM members WHERE email='rex@ssafy.com'),   'Water Raptor',  '매일 물 2L 마시기에 도전하세요. 인증 사진을 올리면 달성으로 기록됩니다.', '💧', '#77C0B3', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), NULL, NULL),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),   'Dino Strength', '주 4회 근력 운동 + 매일 단백질 100g 챙기기 챌린지', '🦖', '#D9C3A3', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'DAILY_PROTEIN_MIN', 100),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'), '1500kcal 클린식단', '하루 총 섭취 1,500kcal 이하의 식단을 기록하면 인증할 수 있어요.', '🥗', '#D83D52', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 'DAILY_KCAL_MAX', 1500);

-- 게시글(시드)
INSERT INTO posts (mno, board, title, content, likes) VALUES
((SELECT mno FROM members WHERE email='sarah@ssafy.com'), 'review', '아보카도 토스트 일주일 후기', '매일 아침 아보카도 토스트로 시작했더니 포만감이 오래 가서 좋았어요. 칼로리도 적당하고 추천합니다!', 24),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),   'expert', '단백질 보충 타이밍 정리', '운동 직후 30분 이내 단백질 20g 섭취가 근합성에 유리합니다. 식단과 함께 챙겨보세요.', 56),
((SELECT mno FROM members WHERE email='marvin@ssafy.com'),'free',   '다들 물 얼마나 드세요?', '워터 랩터 챌린지 하는데 2L 생각보다 힘드네요 ㅎㅎ', 9);

-- 댓글(시드)
INSERT INTO comments (bno, mno, content) VALUES
((SELECT bno FROM (SELECT bno FROM posts ORDER BY bno LIMIT 1) p), (SELECT mno FROM members WHERE email='marvin@ssafy.com'), '저도 해봐야겠어요!'),
((SELECT bno FROM (SELECT bno FROM posts ORDER BY bno LIMIT 1) p), (SELECT mno FROM members WHERE email='rex@ssafy.com'), '단백질도 같이 챙기면 더 좋아요 🦕');

-- 팔로우(시드)
INSERT INTO follows (follower_mno, following_mno) VALUES
((SELECT mno FROM members WHERE email='marvin@ssafy.com'), (SELECT mno FROM members WHERE email='sarah@ssafy.com')),
((SELECT mno FROM members WHERE email='marvin@ssafy.com'), (SELECT mno FROM members WHERE email='rex@ssafy.com')),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'), (SELECT mno FROM members WHERE email='marvin@ssafy.com'));
