-- ============================================================
-- 커뮤니티 게시글 시드 (posts 가 적을 때 DataSeeder 가 1회 적재)
-- 게시판: review(리뷰) / expert(전문가) / free(자유) 각 10개
-- 작성자는 기존 시드 회원(admin/marvin/sarah/rex)을 사용
-- ============================================================

-- ===== 리뷰 (review) 10 =====
INSERT INTO posts (mno, board, title, content, likes) VALUES
((SELECT mno FROM members WHERE email='marvin@ssafy.com'), 'review', '닭가슴살 도시락 2주 식단 후기', '점심마다 닭가슴살 도시락으로 바꿔봤어요. 단백질 챙기기 편하고 포만감도 좋아서 군것질이 확 줄었습니다. 질리지 않게 소스만 바꿔주면 2주는 거뜬해요!', 28),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'),  'review', '그릭요거트 + 베리 아침 대용 추천', '바쁜 아침에 그릭요거트에 블루베리랑 견과류 조금 올려 먹는데 만족도 최고예요. 칼로리 대비 단백질이 높아서 다이어트에도 딱입니다.', 19),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'review', '편의점 단백질 음료 5종 비교', '맛·단백질량·가격 기준으로 5종 마셔봤어요. 운동 직후엔 당 적은 제품이, 식사 대용엔 단백질 20g 이상 제품이 좋더라고요. 표로 정리했습니다.', 42),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'),  'review', '고구마 vs 현미밥 포만감 실험', '같은 칼로리로 일주일씩 먹어봤는데, 저는 고구마가 포만감이 더 오래갔어요. 대신 현미밥이 끼니 구성은 더 편했습니다. 취향 차이인 듯!', 23),
((SELECT mno FROM members WHERE email='marvin@ssafy.com'), 'review', '두부면 비빔국수 후기', '밀가루 국수 대신 두부면으로 비빔국수 해먹었는데 칼로리가 반토막인데 맛은 괜찮아요. 단백질도 챙겨지고 야식 죄책감이 줄었습니다.', 17),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'),  'review', '제로 콜라 한 달 솔직 후기', '단 음료를 제로로 바꾼 지 한 달. 처음엔 어색했는데 이제 일반 콜라가 너무 달게 느껴져요. 식단 유지엔 확실히 도움이 됐습니다.', 12),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'),  'review', '오트밀 아침 루틴 일주일', '오버나이트 오트밀로 아침을 통일했더니 혈당이 천천히 올라서 그런지 오전에 덜 졸려요. 바나나랑 같이 먹으면 맛도 든든합니다.', 26),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'review', '닭가슴살 소시지 맛 순위', '시중 닭가슴살 소시지들 먹어보고 순위 매겨봤어요. 식감과 간 세기가 제품마다 차이가 커요. 데워 먹으면 대부분 맛이 살아납니다.', 33),
((SELECT mno FROM members WHERE email='marvin@ssafy.com'), 'review', '단백질 쉐이크 입문 3종 비교', '처음 보충제 고를 때 헷갈려서 3종 비교해봤어요. 유당 불내증이면 분리유청(WPI)이 속이 편하더라고요. 물에 타는 용해도도 체크하세요.', 21),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'),  'review', '냉동 도시락 다이어트 2주 변화', '요리할 시간이 없어서 냉동 다이어트 도시락으로 2주 버텼어요. 칼로리 계산이 자동이라 편하고, 군것질만 참으면 효과 확실합니다.', 30);

-- ===== 전문가 (expert) 10 =====
INSERT INTO posts (mno, board, title, content, likes) VALUES
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'expert', '탄수화물 섭취 타이밍 정리', '운동 전에는 소화가 빠른 탄수, 운동 후에는 단백질과 함께 탄수를 보충하는 것이 회복에 유리합니다. 하루 총량 안에서 배분이 핵심이에요.', 61),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'expert', '단백질 하루 권장량 계산법', '일반 성인은 체중 1kg당 0.8~1g, 근력 운동을 한다면 1.6~2.2g을 권장합니다. 한 끼에 몰아 먹기보다 끼니마다 20~40g씩 나누세요.', 58),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'expert', '감량 중 근손실 막는 법', '급격한 칼로리 제한은 근육부터 빠지게 만듭니다. 주당 체중의 0.5~1% 감량을 목표로 하고, 충분한 단백질과 근력 운동을 병행하세요.', 47),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'expert', '수면 부족이 식욕에 미치는 영향', '잠이 부족하면 식욕 호르몬(그렐린)이 늘고 포만 호르몬(렙틴)이 줄어 과식하기 쉬워요. 다이어트의 시작은 사실 충분한 수면입니다.', 39),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'expert', '유산소 vs 근력, 지방 연소 비교', '유산소는 운동 중 소비가, 근력은 운동 후 기초대사 상승이 강점입니다. 체지방 감량엔 둘을 병행하는 것이 가장 효율적이에요.', 52),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'expert', '식이섬유와 포만감의 관계', '채소·통곡물의 식이섬유는 위에서 천천히 비워져 포만감을 오래 유지시켜요. 같은 칼로리라도 가공식품보다 덜 먹게 됩니다.', 28),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'expert', '치팅데이 똑똑하게 활용하기', '치팅데이는 죄책감이 아니라 전략입니다. 주 1회, 한 끼 정도로 제한하고 다음 날 평소 식단으로 바로 복귀하면 대사와 의지력 모두에 도움돼요.', 44),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'expert', '물 섭취량과 신진대사', '수분이 부족하면 대사가 떨어지고 배고픔과 갈증을 혼동하기 쉬워요. 끼니 전 물 한 컵은 과식 예방에도 도움이 됩니다.', 31),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'expert', '근성장을 위한 단백질 분배', '한 번에 흡수되는 단백질에는 한계가 있어요. 하루 총량을 3~4끼로 나눠 끼니마다 일정량을 채우는 것이 근합성에 더 효과적입니다.', 49),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'expert', '나트륨 줄이는 식단 구성 팁', '국물·가공식품·소스에 나트륨이 숨어 있어요. 같은 메뉴라도 간을 줄이고 채소를 늘리면 붓기와 혈압 관리에 큰 도움이 됩니다.', 26);

-- ===== 자유 (free) 10 =====
INSERT INTO posts (mno, board, title, content, likes) VALUES
((SELECT mno FROM members WHERE email='marvin@ssafy.com'), 'free', '오늘 점심 다들 뭐 드셨어요?', '저는 현미밥에 닭가슴살, 샐러드 먹었어요. 다들 점심 메뉴 공유해요! 아이디어가 필요합니다 ㅎㅎ', 8),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'),  'free', '물 2L 챌린지 같이 하실 분!', '워터 랩터 챌린지 혼자 하니 자꾸 까먹네요. 같이 인증하면서 동기부여 받을 분 구해요 💧', 14),
((SELECT mno FROM members WHERE email='marvin@ssafy.com'), 'free', '저녁 늦게 먹으면 진짜 살쪄요?', '야근하면 어쩔 수 없이 늦게 먹게 되는데, 시간보다 총 칼로리가 중요하다는 말도 있고… 다들 어떻게 관리하세요?', 11),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'),  'free', '다이어트 중 가장 힘든 순간 ㅠㅠ', '저는 자기 전에 뭔가 먹고 싶을 때가 제일 힘들어요. 다들 어떻게 참으시나요? 꿀팁 공유 부탁드려요!', 22),
((SELECT mno FROM members WHERE email='marvin@ssafy.com'), 'free', '공룡 코치 진짜 귀엽지 않나요 🦕', 'AI 코치 캐릭터들 너무 귀여워서 자꾸 들어오게 돼요. 파워렉스 말투가 제 취향입니다 ㅋㅋ', 17),
((SELECT mno FROM members WHERE email='marvin@ssafy.com'), 'free', '아침 거르는 거 어떻게 생각하세요?', '간헐적 단식 하시는 분들 많던데, 아침 거르고 점심부터 드시는 분들 컨디션 어떠세요? 의견 궁금합니다.', 9),
((SELECT mno FROM members WHERE email='rex@ssafy.com'),    'free', '운동 끝나고 뭐 드세요?', '저는 보통 단백질 쉐이크에 바나나 하나인데 다들 운동 후 식사 루틴이 궁금하네요. 추천 받아요!', 13),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'),  'free', '식단 기록 14일째 성공 중입니다!', '작심삼일이 늘 문제였는데 냠냠코치로 2주째 매일 기록 중이에요. 기록하니까 확실히 덜 먹게 되네요. 다들 화이팅!', 25),
((SELECT mno FROM members WHERE email='marvin@ssafy.com'), 'free', '간식 추천 좀 해주세요 (단 거 끊는 중)', '단 거를 줄이는 중인데 입이 심심할 때 먹을 만한 건강 간식 있을까요? 견과류는 좀 질려서요 ㅠ', 16),
((SELECT mno FROM members WHERE email='sarah@ssafy.com'),  'free', '냠냠코치 덕분에 3kg 뺐어요!', '두 달 동안 식단 기록 + 챌린지 꾸준히 했더니 3kg 감량 성공했어요. 작은 습관이 진짜 중요하더라고요. 다들 포기하지 마세요!', 38);
