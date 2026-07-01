-- ============================================================
-- 데모/테스트 데이터 (demo-data.sql)
-- 로그인 계정:  test@ssafy.com  /  1234
-- 이 계정을 직접 생성하고 모든 기능 데이터를 채운다 (자기완결적).
-- DataSeeder가 test 계정이 없을 때 1회 적재. LAST_INSERT_ID 회피(@dno).
-- ============================================================

-- 1) 데모 전용 회원 생성 (이미 있으면 무시)
INSERT IGNORE INTO members (email, password, name, role, height, weight, disease, goal, gender, birth_year, activity_level) VALUES ('test@ssafy.com', '$2b$10$6AOTKFCt2TwBNscQ/2GwyefNFY/0aW1vrEmv8LdyGROPkQ3R7W6rm', '정유진', 'USER', 178, 75, NULL, '근육 증가', 'male', 1997, 'moderate');
SET @uid = (SELECT mno FROM members WHERE email='test@ssafy.com');

-- 2) 식단: 최근 10일 × 4끼 — meal 은 영어 키
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'breakfast', DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '두부', 80, 8, 2, 5);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '두부', 80, 8, 2, 5);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'lunch', DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '연어구이', 280, 25, 0, 18);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '연어구이', 280, 25, 0, 18);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'dinner', DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '통밀빵 외', 160, 7, 27, 3);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '통밀빵', 90, 4, 17, 1);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '샐러드(채소)', 70, 3, 10, 2);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'snack', DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '통밀빵 외', 195, 5, 44, 1);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '통밀빵', 90, 4, 17, 1);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '바나나', 105, 1, 27, 0);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'breakfast', DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '현미밥', 310, 6, 65, 2);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '현미밥', 310, 6, 65, 2);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'lunch', DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '닭가슴살', 165, 31, 0, 4);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '닭가슴살', 165, 31, 0, 4);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'dinner', DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '닭가슴살 외', 270, 32, 27, 4);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '닭가슴살', 165, 31, 0, 4);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '바나나', 105, 1, 27, 0);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'breakfast', DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '아보카도 외', 320, 11, 14, 27);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '아보카도', 240, 3, 12, 22);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '두부', 80, 8, 2, 5);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'lunch', DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '그릭요거트', 100, 10, 6, 4);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '그릭요거트', 100, 10, 6, 4);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'dinner', DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '두부 외', 230, 13, 29, 8);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '두부', 80, 8, 2, 5);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '오트밀', 150, 5, 27, 3);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'snack', DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '오트밀', 150, 5, 27, 3);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '오트밀', 150, 5, 27, 3);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'breakfast', DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '닭가슴살 외', 235, 34, 10, 6);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '닭가슴살', 165, 31, 0, 4);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '샐러드(채소)', 70, 3, 10, 2);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'lunch', DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '고구마 외', 235, 3, 57, 0);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '고구마', 130, 2, 30, 0);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '바나나', 105, 1, 27, 0);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'dinner', DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '아보카도', 240, 3, 12, 22);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '아보카도', 240, 3, 12, 22);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'snack', DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '현미밥 외', 390, 14, 67, 7);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '현미밥', 310, 6, 65, 2);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '두부', 80, 8, 2, 5);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'breakfast', DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '바나나 외', 205, 11, 33, 4);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '바나나', 105, 1, 27, 0);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '그릭요거트', 100, 10, 6, 4);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'lunch', DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '그릭요거트', 100, 10, 6, 4);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '그릭요거트', 100, 10, 6, 4);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'dinner', DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '닭가슴살 외', 265, 41, 6, 8);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '닭가슴살', 165, 31, 0, 4);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '그릭요거트', 100, 10, 6, 4);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'snack', DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '그릭요거트 외', 170, 13, 16, 6);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '그릭요거트', 100, 10, 6, 4);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '샐러드(채소)', 70, 3, 10, 2);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'breakfast', DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '그릭요거트 외', 190, 14, 23, 5);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '그릭요거트', 100, 10, 6, 4);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '통밀빵', 90, 4, 17, 1);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'lunch', DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '샐러드(채소) 외', 220, 8, 37, 5);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '샐러드(채소)', 70, 3, 10, 2);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '오트밀', 150, 5, 27, 3);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'dinner', DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '통밀빵', 90, 4, 17, 1);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '통밀빵', 90, 4, 17, 1);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'breakfast', DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), '현미밥', 310, 6, 65, 2);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '현미밥', 310, 6, 65, 2);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'lunch', DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), '오트밀', 150, 5, 27, 3);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '오트밀', 150, 5, 27, 3);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'dinner', DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), '바나나 외', 205, 8, 28, 8);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '바나나', 105, 1, 27, 0);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '계란후라이', 100, 7, 1, 8);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'breakfast', DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), '닭가슴살', 165, 31, 0, 4);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '닭가슴살', 165, 31, 0, 4);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'lunch', DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), '아보카도 외', 345, 4, 39, 22);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '아보카도', 240, 3, 12, 22);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '바나나', 105, 1, 27, 0);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'dinner', DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), '샐러드(채소) 외', 170, 13, 16, 6);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '샐러드(채소)', 70, 3, 10, 2);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '그릭요거트', 100, 10, 6, 4);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'snack', DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), '샐러드(채소) 외', 310, 6, 22, 24);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '샐러드(채소)', 70, 3, 10, 2);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '아보카도', 240, 3, 12, 22);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'breakfast', DATE_SUB(CURRENT_DATE, INTERVAL 8 DAY), '그릭요거트 외', 250, 15, 33, 7);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '그릭요거트', 100, 10, 6, 4);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '오트밀', 150, 5, 27, 3);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'lunch', DATE_SUB(CURRENT_DATE, INTERVAL 8 DAY), '고구마', 130, 2, 30, 0);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '고구마', 130, 2, 30, 0);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'dinner', DATE_SUB(CURRENT_DATE, INTERVAL 8 DAY), '현미밥 외', 460, 11, 92, 5);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '현미밥', 310, 6, 65, 2);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '오트밀', 150, 5, 27, 3);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'breakfast', DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), '샐러드(채소) 외', 175, 4, 37, 2);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '샐러드(채소)', 70, 3, 10, 2);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '바나나', 105, 1, 27, 0);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'lunch', DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), '연어구이 외', 410, 27, 30, 18);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '연어구이', 280, 25, 0, 18);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '고구마', 130, 2, 30, 0);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'dinner', DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), '바나나 외', 175, 4, 37, 2);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '바나나', 105, 1, 27, 0);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '샐러드(채소)', 70, 3, 10, 2);
INSERT INTO diets (mno, meal, eaten_date, title, total_kcal, protein, carbs, fat) VALUES (@uid, 'snack', DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), '그릭요거트 외', 170, 13, 16, 6);
SET @dno = LAST_INSERT_ID();
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '그릭요거트', 100, 10, 6, 4);
INSERT INTO diet_foods (dno, name, kcal, protein, carbs, fat) VALUES (@dno, '샐러드(채소)', 70, 3, 10, 2);

-- 3) 일일 영양통계: 최근 10일
INSERT INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) VALUES (@uid, DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), 2279, 138, 154, 63, 3);
INSERT INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) VALUES (@uid, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 1924, 109, 253, 51, 3);
INSERT INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) VALUES (@uid, DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), 2048, 124, 241, 77, 3);
INSERT INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) VALUES (@uid, DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), 1865, 108, 173, 72, 3);
INSERT INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) VALUES (@uid, DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), 2090, 140, 158, 62, 3);
INSERT INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) VALUES (@uid, DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), 2110, 103, 162, 57, 3);
INSERT INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) VALUES (@uid, DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), 2021, 89, 207, 45, 3);
INSERT INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) VALUES (@uid, DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), 1694, 80, 207, 79, 3);
INSERT INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) VALUES (@uid, DATE_SUB(CURRENT_DATE, INTERVAL 8 DAY), 1685, 119, 251, 57, 3);
INSERT INTO daily_nutrition_stats (mno, stat_date, total_kcal, protein, carbs, fat, meal_count) VALUES (@uid, DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), 1853, 87, 229, 47, 3);

-- 4) 데모 챌린지 20개 생성
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '물 2L 마시기', '매일 물 2L 인증', '💧', '#77C0B3', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), NULL, NULL);
SET @c1 = LAST_INSERT_ID();
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '단백질 100g 챌린지', '매일 단백질 100g 이상', '🦖', '#D9C3A3', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'DAILY_PROTEIN_MIN', 100);
SET @c2 = LAST_INSERT_ID();
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '1500kcal 클린식단', '하루 1500kcal 이하', '🥗', '#D83D52', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 'DAILY_KCAL_MAX', 1500);
SET @c3 = LAST_INSERT_ID();
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '아침 거르지 않기', '매일 아침 식사 인증', '🍳', '#E8A87C', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 21 DAY), NULL, NULL);
SET @c4 = LAST_INSERT_ID();
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '야식 끊기', '밤 9시 이후 금식', '🌙', '#6C7A89', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), NULL, NULL);
SET @c5 = LAST_INSERT_ID();
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '매일 10000보 걷기', '하루 1만보 인증', '🚶', '#88C0A8', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '주 4회 근력운동', '주 4회 헬스 인증', '💪', '#C0676B', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 28 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '탄수화물 200g 이하', '하루 탄수 200g 이하', '🍚', '#D9B36A', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '저녁 7시 전 식사', '이른 저녁 챌린지', '🕖', '#7C9CB0', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 21 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '매일 채소 섭취', '끼니마다 채소 포함', '🥦', '#6FAE6F', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '간식 끊기 챌린지', '간식 금지', '🍪', '#B08968', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '단백질 셰이크 루틴', '운동 후 단백질 보충', '🥤', '#9B8BC4', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 21 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '2000kcal 유지', '하루 2000kcal 전후', '⚖️', '#77C0B3', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '계단 이용하기', '엘리베이터 대신 계단', '🪜', '#A8997C', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '물 대신 탄산수', '음료 줄이기', '🫧', '#7CC0D9', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 21 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '아침 스트레칭', '기상 후 스트레칭', '🧘', '#C4A88B', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 28 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '저당 식단', '당류 줄이기', '🍯', '#D9A86A', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '주말 등산', '주말 야외 활동', '⛰️', '#6F9E7C', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 60 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '금주 챌린지', '술 줄이기', '🚫', '#B0676B', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), NULL, NULL);
INSERT INTO challenges (mno, title, description, emoji, color, start_date, end_date, cond_type, cond_value) VALUES (@uid, '일찍 자기', '11시 전 취침', '😴', '#7C7CB0', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 21 DAY), NULL, NULL);

-- 5) 챌린지 참여 (앞 5개에 참여)
INSERT INTO challenge_participants (cno, mno, progress, done_days, total_days) VALUES (@c1, @uid, 35, 5, 14);
INSERT INTO challenge_participants (cno, mno, progress, done_days, total_days) VALUES (@c2, @uid, 57, 8, 14);
INSERT INTO challenge_participants (cno, mno, progress, done_days, total_days) VALUES (@c3, @uid, 40, 12, 30);
INSERT INTO challenge_participants (cno, mno, progress, done_days, total_days) VALUES (@c4, @uid, 20, 3, 21);
INSERT INTO challenge_participants (cno, mno, progress, done_days, total_days) VALUES (@c5, @uid, 70, 10, 14);

-- 6) 커뮤니티 게시글/댓글은 DataSeeder.seedCommunityUsersAndPosts() 에서
--    50명의 다양한 사용자가 시간에 걸쳐 작성한 형태로 생성한다(정유진/시드 회원 포함).

-- 7) 팔로우용 상대 회원 mno
SET @other1 = (SELECT mno FROM members WHERE email='sarah@ssafy.com');
SET @other2 = (SELECT mno FROM members WHERE email='rex@ssafy.com');

-- 8) 팔로우
INSERT IGNORE INTO follows (follower_mno, following_mno) SELECT @uid, @other1 WHERE @other1 IS NOT NULL;
INSERT IGNORE INTO follows (follower_mno, following_mno) SELECT @other1, @uid WHERE @other1 IS NOT NULL;
INSERT IGNORE INTO follows (follower_mno, following_mno) SELECT @uid, @other2 WHERE @other2 IS NOT NULL;