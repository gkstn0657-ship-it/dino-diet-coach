<template>
  <div class="dash">
    <!-- ===== 게스트(미로그인): 랜딩 + 대시보드 프리뷰 ===== -->
    <section v-if="!isLoggedIn" class="landing">
      <!-- HERO -->
      <div class="hero">
        <div class="hero-left">
          <span class="hero-badge">🦕 AI 다이어트 파트너</span>
          <h1 class="hero-title">AI와 함께 기록하는<br /><em>나만의 식단 루틴</em></h1>
          <p class="hero-sub">
            DinoDiet은 식단 기록 · 칼로리 분석 · AI 코칭 · 챌린지를
            한 화면에서 관리하는 다이어트 파트너예요.
          </p>
          <div class="hero-cta">
            <router-link :to="{ name: 'regist-member-form' }" class="btn-primary cta-lg">
              식단 기록 시작하기
            </router-link>
            <router-link :to="{ name: 'login-form' }" class="cta-secondary">
              AI 코칭 보기 →
            </router-link>
          </div>
          <div class="hero-stats">
            <div class="hs"><b>사진 한 장</b><span>식단 자동 분석</span></div>
            <div class="hs"><b>3종 코치</b><span>맞춤 AI 코칭</span></div>
            <div class="hs"><b>자동 인증</b><span>식단·물 챌린지</span></div>
          </div>
        </div>

        <!-- 우측 앱 프리뷰 (랜딩용 mock UI) -->
        <div class="hero-right">
          <div class="preview" aria-hidden="true">
            <div class="pv-head">
              <span class="pv-emoji">🦕</span>
              <div class="pv-headtext">
                <div class="pv-name">오늘의 칼로리</div>
                <div class="pv-date">목표 1,830 kcal</div>
              </div>
              <span class="pv-pill">진행 중</span>
            </div>
            <div class="pv-cal"><b>930</b><span>kcal 남음</span></div>
            <div class="pv-bar"><div class="pv-bar-fill" style="width: 49%"></div></div>
            <div class="pv-macros">
              <div class="pv-macro"><span>탄수</span><b>120g</b></div>
              <div class="pv-macro"><span>단백질</span><b>68g</b></div>
              <div class="pv-macro"><span>지방</span><b>32g</b></div>
            </div>
            <div class="pv-coach">
              <span class="pv-coach-emoji">🦖</span>
              <p>단백질이 조금 부족해요! 저녁엔 닭가슴살 어때요?</p>
            </div>
            <div class="pv-chal">
              <span class="pv-chal-name">💧 물 8잔 챌린지</span>
              <div class="pv-chal-bar"><div style="width: 75%"></div></div>
              <span class="pv-chal-d">6/8잔</span>
            </div>
            <ul class="pv-diet">
              <li><span>🥣 아침 · 오트밀</span><span class="pv-kc">255</span></li>
              <li><span>🥗 점심 · 닭가슴살 샐러드</span><span class="pv-kc">410</span></li>
            </ul>
          </div>
          <div class="preview-glow"></div>
        </div>
      </div>

      <!-- 기능 프리뷰 카드 -->
      <div class="feat-grid">
        <div class="cloud-card feat">
          <div class="feat-top"><span class="feat-ic">🍽</span><span class="feat-t">식단 기록</span></div>
          <p class="feat-d">사진을 올리거나 검색해서 칼로리·탄단지를 자동으로 채워요.</p>
          <div class="feat-ui chips">
            <span class="chip">📷 사진 분석</span><span class="chip">🔍 검색</span><span class="chip ai">🤖 AI 추정</span>
          </div>
        </div>
        <div class="cloud-card feat">
          <div class="feat-top"><span class="feat-ic">🦖</span><span class="feat-t">AI 코칭</span></div>
          <p class="feat-d">내 식단·목표를 보고 3종 공룡 코치가 맞춤 피드백을 줘요.</p>
          <div class="feat-ui bubble">오늘 단백질 충분해요! 👍</div>
        </div>
        <div class="cloud-card feat">
          <div class="feat-top"><span class="feat-ic">🏆</span><span class="feat-t">챌린지 자동 인증</span></div>
          <p class="feat-d">식단·물 기록이 조건을 충족하면 하루 마감에 자동 인증돼요.</p>
          <div class="feat-ui">
            <div class="mini-bar"><div style="width: 70%"></div></div>
            <span class="mini-cap">7/10일 달성</span>
          </div>
        </div>
        <div class="cloud-card feat">
          <div class="feat-top"><span class="feat-ic">📊</span><span class="feat-t">칼로리·영양 추이</span></div>
          <p class="feat-d">기간별 섭취 칼로리와 탄단지 추세를 그래프로 확인해요.</p>
          <div class="feat-ui spark">
            <i style="height: 40%"></i><i style="height: 70%"></i><i style="height: 55%"></i>
            <i style="height: 85%"></i><i style="height: 60%"></i><i style="height: 75%"></i>
          </div>
        </div>
      </div>

      <!-- 서비스 흐름 -->
      <div class="cloud-card flow">
        <div class="flow-title">이렇게 동작해요</div>
        <div class="flow-steps">
          <div class="step"><span class="step-ic">📝</span><b>기록</b><span>사진·검색으로 식단 입력</span></div>
          <span class="step-arrow">→</span>
          <div class="step"><span class="step-ic">📊</span><b>분석</b><span>칼로리·탄단지 점수화</span></div>
          <span class="step-arrow">→</span>
          <div class="step"><span class="step-ic">🦕</span><b>코칭</b><span>AI 코치 맞춤 조언</span></div>
          <span class="step-arrow">→</span>
          <div class="step"><span class="step-ic">🏆</span><b>챌린지</b><span>자동 인증으로 동기부여</span></div>
        </div>
        <div class="flow-cta">
          <router-link :to="{ name: 'regist-member-form' }" class="btn-primary">지금 시작하기</router-link>
          <span class="flow-note">이미 계정이 있으신가요? <router-link :to="{ name: 'login-form' }">로그인</router-link></span>
        </div>
      </div>
    </section>

    <!-- 목표 칼로리 미설정 안내 CTA -->
    <router-link
      v-if="isLoggedIn && !profileSet"
      :to="{ name: 'member-modify-form' }"
      class="cloud-card target-cta"
    >
      <span class="cta-emoji">🎯</span>
      <span class="cta-text">
        <b>목표 칼로리를 설정해 보세요.</b>
        프로필(키·체중·활동량·목표)을 입력하면 더 정확한 칼로리 코칭을 받을 수 있어요.
      </span>
      <span class="cta-go">설정하기 →</span>
    </router-link>

    <!-- ===== HERO: 오늘 한눈에 (로그인 사용자) ===== -->
    <section v-if="isLoggedIn" class="dash-hero">
      <!-- 메인 요약: 오늘의 칼로리 + 매크로 + Primary CTA -->
      <div class="cloud-card hero-card">
        <div class="hero-top">
          <div>
            <div class="c-title">오늘의 칼로리</div>
            <div class="c-sub">{{ todayLabel }} · 목표 {{ targetKcal.toLocaleString() }} kcal</div>
          </div>
          <span class="pill" :class="overCalorie ? 'pill-danger' : 'pill-accent'">
            {{ overCalorie ? '목표 초과' : '진행 중' }}
          </span>
        </div>

        <div class="hero-body">
          <div class="cal-block">
            <div class="cal-remaining">
              <span class="cal-num" :class="{ over: overCalorie }">{{ remaining.toLocaleString() }}</span>
              <span class="cal-unit">kcal 남음</span>
            </div>

            <div class="cal-meta">
              <div class="meta-item">
                <span class="meta-k">오늘 섭취</span>
                <span class="meta-v">{{ consumed.toLocaleString() }}</span>
              </div>
              <div class="meta-sep"></div>
              <div class="meta-item">
                <span class="meta-k">목표</span>
                <span class="meta-v">{{ targetKcal.toLocaleString() }}</span>
              </div>
            </div>

            <div class="cal-bar">
              <div class="cal-bar-fill" :class="{ over: overCalorie }" :style="{ width: calPct + '%' }"></div>
            </div>

            <router-link :to="{ name: 'diet-write' }" class="btn-primary cta-block">
              ＋ 식단 기록하기
            </router-link>
          </div>

          <div class="macro-block">
            <div v-for="m in macros" :key="m.label" class="macro-ring-wrap">
              <div
                class="ring"
                :style="{ background: `conic-gradient(${m.color} ${m.pct}%, var(--surface-muted) ${m.pct}% 100%)` }"
              >
                <span class="ring-pct">{{ m.pct }}%</span>
              </div>
              <div class="macro-label">{{ m.label }}</div>
              <div class="macro-amt">{{ m.value }} / {{ m.target }}g</div>
            </div>
          </div>
        </div>
      </div>

      <!-- AI 코치 요약 (브랜드 패널 · secondary) -->
      <div class="coach-card">
        <div class="coach-top">
          <div class="coach-emoji">🦕</div>
          <div>
            <div class="coach-title">AI 코치</div>
            <div class="coach-sub">PowerRex · SlimDino · BalanceNo</div>
          </div>
        </div>
        <p class="coach-tip">{{ coachTip }}</p>
        <div class="coach-actions">
          <router-link :to="{ name: 'ai-workout-coach' }" class="btn-light">AI 코칭 받기</router-link>
          <router-link :to="{ name: 'ai-diet-analysis' }" class="coach-link">식단 종합 분석 보기 →</router-link>
        </div>
      </div>
    </section>

    <!-- ===== DASHBOARD GRID (로그인 사용자) ===== -->
    <section v-if="isLoggedIn" class="dash-grid">
      <!-- 주간 칼로리 추이 -->
      <div class="cloud-card grid-card">
        <div class="c-head">
          <div class="c-title">📊 주간 칼로리 추이</div>
          <router-link :to="{ name: 'calorie-trend' }" class="link-more">자세히 →</router-link>
        </div>
        <div class="chart">
          <div class="line-wrap">
            <svg class="line-svg" viewBox="0 0 100 100" preserveAspectRatio="none">
              <defs>
                <linearGradient id="calGradHome" x1="0" y1="0" x2="0" y2="1">
                  <stop class="g0" offset="0%" />
                  <stop class="g1" offset="100%" />
                </linearGradient>
              </defs>
              <path class="t-area" :d="areaPath" fill="url(#calGradHome)" />
              <line class="t-line" x1="0" :y1="targetY" x2="100" :y2="targetY" />
              <path class="t-path" :d="linePath" />
            </svg>
            <span
              v-for="(p, i) in linePoints"
              :key="i"
              class="dot"
              :class="{ over: p.over, today: i === 6 }"
              :style="{ left: p.x + '%', top: p.y + '%' }"
              :title="`${p.weekday} · ${p.kcal.toLocaleString()} kcal`"
            ></span>
          </div>
          <div class="x-axis">
            <span
              v-for="(p, i) in linePoints"
              :key="i"
              class="chart-x"
              :class="{ now: i === 6 }"
              :style="{ left: p.x + '%' }"
              >{{ p.weekday }}</span
            >
          </div>
        </div>
        <div class="chart-foot c-sub">
          <i class="lg-dot target"></i> 목표 {{ targetKcal.toLocaleString() }}
          <span class="foot-sep">·</span>
          하루 평균 {{ weeklyAvg.toLocaleString() }} kcal
        </div>
      </div>

      <!-- 최근 식단 기록 -->
      <div class="cloud-card grid-card">
        <div class="c-head">
          <div class="c-title">🍽 최근 식단 기록</div>
          <router-link :to="{ name: 'diet-list' }" class="link-more">전체 →</router-link>
        </div>
        <ul v-if="recentMeals.length" class="rows">
          <li v-for="d in recentMeals" :key="d.dno" class="row">
            <router-link :to="{ name: 'diet-detail', params: { dno: d.dno } }" class="row-link">
              <span class="row-thumb">{{ d.thumb }}</span>
              <span class="row-main">
                <span class="row-title">{{ d.title }}</span>
                <span class="row-meta">{{ d.mealLabel }} · {{ d.date }}</span>
              </span>
              <span class="row-kcal">{{ d.kcal }}<i>kcal</i></span>
            </router-link>
          </li>
        </ul>
        <div v-else class="empty">
          <span class="empty-emoji">🦕</span>
          아직 기록된 식단이 없어요.
          <router-link :to="{ name: 'diet-write' }" class="empty-cta">첫 식단 기록하기</router-link>
        </div>
      </div>

      <!-- 진행 중 챌린지 -->
      <div class="cloud-card grid-card">
        <div class="c-head">
          <div class="c-title">🏆 진행 중 챌린지</div>
          <router-link :to="{ name: 'challenge-list' }" class="link-more">전체 →</router-link>
        </div>
        <router-link
          v-if="liveChallenge"
          :to="{ name: 'challenge-detail', params: { cno: liveChallenge.cno } }"
          class="ch-panel"
        >
          <div class="ch-top">
            <span class="ch-emoji">{{ liveChallenge.emoji }}</span>
            <span class="ch-name">{{ liveChallenge.title }}</span>
            <span class="pill pill-danger sm">LIVE</span>
          </div>
          <p class="ch-desc">{{ liveChallenge.desc }}</p>
          <div class="ch-foot">
            <span>👥 {{ liveChallenge.participants }}명 참여</span>
            <span class="ch-go">참여하기 →</span>
          </div>
        </router-link>
        <div v-else class="empty">
          <span class="empty-emoji">🏆</span>
          진행 중인 챌린지가 없어요.
          <router-link :to="{ name: 'challenge-list' }" class="empty-cta">챌린지 보기</router-link>
        </div>
      </div>

      <!-- 오늘의 영양 체크 -->
      <div class="cloud-card grid-card">
        <div class="c-head">
          <div class="c-title">🩺 오늘의 영양 체크</div>
        </div>
        <ul v-if="warnings.length" class="alerts">
          <li v-for="(w, i) in warnings" :key="i" class="alert" :class="w.type">
            <span class="alert-dot"></span>{{ w.text }}
          </li>
        </ul>
        <div v-else class="empty ok">
          <span class="empty-emoji">✅</span>
          영양 밸런스가 좋아요!
        </div>
      </div>

      <!-- 물 섭취 -->
      <div class="cloud-card grid-card">
        <div class="c-head">
          <div class="c-title">💧 물 섭취</div>
          <span class="c-sub">{{ water }} / {{ WATER_GOAL }}잔</span>
        </div>
        <div class="cups">
          <button
            v-for="n in WATER_GOAL"
            :key="n"
            class="cup"
            :class="{ on: n <= water }"
            :aria-label="`${n}잔`"
            @click="setWater(n)"
          >
            💧
          </button>
        </div>
        <div class="cal-bar">
          <div class="cal-bar-fill" :style="{ width: (water / WATER_GOAL) * 100 + '%' }"></div>
        </div>
        <div class="c-sub water-foot">목표까지 {{ waterLeft }}잔 남았어요.</div>
      </div>

      <!-- 커뮤니티 최신 글 -->
      <div class="cloud-card grid-card">
        <div class="c-head">
          <div class="c-title">💬 커뮤니티</div>
          <router-link :to="{ name: 'board-list' }" class="link-more">전체 →</router-link>
        </div>
        <router-link
          v-if="latestPost"
          :to="{ name: 'post-detail', params: { bno: latestPost.bno } }"
          class="post-panel"
        >
          <div class="post-board c-sub">{{ latestPost.boardLabel }} · {{ latestPost.author }}</div>
          <p class="post-title">{{ latestPost.title }}</p>
          <div class="post-foot c-sub">❤ {{ latestPost.likes ?? 0 }} · 💬 {{ latestPost.comments ?? 0 }}</div>
        </router-link>
        <div v-else class="empty">
          <span class="empty-emoji">✍️</span>
          아직 게시글이 없어요.
          <router-link :to="{ name: 'board-list' }" class="empty-cta">커뮤니티 보기</router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useMemberStore } from '@/stores/memberStore';
import { useFetchDiets } from '@/composables/useDietApis';
import { useFetchPosts } from '@/composables/useCommunityApis';
import { useFetchChallenges } from '@/composables/useChallengeApis';
import { useMyTarget } from '@/composables/useMemberApis';
import { useGetWater, useSetWater } from '@/composables/useWaterApis';

const memberStore = useMemberStore();
// 게스트(미로그인) vs 로그인 사용자 분기 — 빈 데이터 상태와 명확히 구분
const isLoggedIn = computed(() => memberStore.isLoggedIn);
const targetKcal = ref(2000); // 개인 목표 칼로리 (프로필 기반, 미설정 시 2000)
// 매크로 목표값(g) — 우선 고정값 유지
const MACRO_TARGETS = { protein: 60, carbs: 250, fat: 60 };

const pad = (n) => String(n).padStart(2, '0');
const todayStr = () => {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};
const todayLabel = (() => {
  const d = new Date();
  return `${d.getMonth() + 1}월 ${d.getDate()}일`;
})();

// 오늘 식단(요약·매크로) + 전체 식단(주간 추이·최근 기록) — 로그인 시에만 조회
const { data: todayDiets, execute: fetchToday } = useFetchDiets();
const { data: allDiets, execute: fetchAll } = useFetchDiets();
if (memberStore.isLoggedIn) {
  fetchToday({ date: todayStr() });
  fetchAll({});
}

// 개인 목표 칼로리 (서버 계산·저장값). 미설정 사용자는 안내 CTA 노출.
const { execute: fetchTarget } = useMyTarget();
const profileSet = ref(false);
if (memberStore.isLoggedIn) {
  fetchTarget().then((t) => {
    if (t?.targetCalories) targetKcal.value = t.targetCalories;
    profileSet.value = !!t?.hasProfile;
  });
}

// 커뮤니티 최신 글 1건
const { data: postData, execute: fetchPosts } = useFetchPosts();
if (memberStore.isLoggedIn) fetchPosts();
// 목록 API가 { posts, page } 구조로 바뀜 → posts 배열에서 최신 1건
const latestPost = computed(() => (postData.value?.posts ?? [])[0] ?? null);

// 진행 중(LIVE) 챌린지 1건 — 목록 기본이 모집중이라 명시적으로 진행중을 조회
const { data: chData, execute: fetchChallenges } = useFetchChallenges();
if (memberStore.isLoggedIn) fetchChallenges({ status: 'live', sort: 'popular' });
const liveChallenge = computed(() => (chData.value ?? []).find((c) => c.status === 'LIVE') ?? null);

// ===== 칼로리 요약 =====
const meals = computed(() => todayDiets.value ?? []);
const consumed = computed(() => meals.value.reduce((s, m) => s + (m.kcal || 0), 0));
const remaining = computed(() => Math.max(0, targetKcal.value - consumed.value));
const overCalorie = computed(() => consumed.value > targetKcal.value);
const calPct = computed(() => Math.min(100, Math.round((consumed.value / targetKcal.value) * 100)));

// ===== 매크로 (오늘 식단 실데이터 합계) =====
const macros = computed(() => {
  const diets = todayDiets.value ?? [];
  const sum = (k) => diets.reduce((s, d) => s + (d[k] || 0), 0);
  const defs = [
    { label: '단백질', key: 'protein', color: 'var(--color-red)' },
    { label: '탄수화물', key: 'carbs', color: 'var(--color-teal)' },
    { label: '지방', key: 'fat', color: 'var(--color-tan)' },
  ];
  return defs.map((m) => {
    const value = sum(m.key);
    const target = MACRO_TARGETS[m.key];
    return { ...m, value, target, pct: Math.min(100, Math.round((value / target) * 100)) };
  });
});

// ===== 주간 칼로리 추이 (전체 식단 → 최근 7일 집계) =====
const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];
const weeklyTrend = computed(() => {
  const now = new Date();
  const days = [];
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(now.getDate() - i);
    days.push({ mmdd: `${pad(d.getMonth() + 1)}-${pad(d.getDate())}`, weekday: WEEKDAYS[d.getDay()], kcal: 0 });
  }
  const map = {};
  days.forEach((x) => (map[x.mmdd] = x));
  // 목록 API의 date 는 'MM-dd' 형식이라 그대로 매칭
  (allDiets.value ?? []).forEach((d) => {
    if (map[d.date]) map[d.date].kcal += d.kcal || 0;
  });
  return days;
});
const weeklyMax = computed(() => Math.max(targetKcal.value, ...weeklyTrend.value.map((d) => d.kcal)));
const weeklyAvg = computed(() =>
  Math.round(weeklyTrend.value.reduce((s, d) => s + d.kcal, 0) / 7)
);
// 라인 그래프 좌표(0~100 좌표계, 위/아래 여백 확보) — 점과 선이 동일 공식 → 항상 정렬
const PAD_TOP = 16;
const PAD_BOT = 12;
const yOf = (kcal) => PAD_TOP + (1 - (kcal || 0) / (weeklyMax.value || 1)) * (100 - PAD_TOP - PAD_BOT);
const linePoints = computed(() =>
  weeklyTrend.value.map((d, i) => ({
    ...d,
    x: (i / 6) * 100,
    y: yOf(d.kcal),
    over: d.kcal > targetKcal.value,
  }))
);
// 부드러운 곡선(Catmull-Rom 근사) 경로 생성
const smoothPath = (pts) => {
  if (!pts.length) return '';
  if (pts.length === 1) return `M ${pts[0].x} ${pts[0].y}`;
  const t = 0.18;
  const seg = (a, b) => ({ len: Math.hypot(b.x - a.x, b.y - a.y), ang: Math.atan2(b.y - a.y, b.x - a.x) });
  const cp = (cur, prev, next, rev) => {
    prev = prev || cur;
    next = next || cur;
    const o = seg(prev, next);
    const ang = o.ang + (rev ? Math.PI : 0);
    const len = o.len * t;
    return [cur.x + Math.cos(ang) * len, cur.y + Math.sin(ang) * len];
  };
  return pts.reduce((acc, p, i, a) => {
    if (i === 0) return `M ${p.x.toFixed(2)} ${p.y.toFixed(2)}`;
    const [c1x, c1y] = cp(a[i - 1], a[i - 2], p, false);
    const [c2x, c2y] = cp(p, a[i - 1], a[i + 1], true);
    return `${acc} C ${c1x.toFixed(2)} ${c1y.toFixed(2)}, ${c2x.toFixed(2)} ${c2y.toFixed(2)}, ${p.x.toFixed(2)} ${p.y.toFixed(2)}`;
  }, '');
};
const linePath = computed(() => smoothPath(linePoints.value));
const areaPath = computed(() => {
  const pts = linePoints.value;
  if (!pts.length) return '';
  return `${smoothPath(pts)} L ${pts[pts.length - 1].x.toFixed(2)} 100 L ${pts[0].x.toFixed(2)} 100 Z`;
});
const targetY = computed(() => yOf(targetKcal.value));

// ===== 최근 식단 기록 =====
const recentMeals = computed(() => (allDiets.value ?? []).slice(0, 4));

// ===== 오늘의 영양 체크 (계산) =====
const warnings = computed(() => {
  const out = [];
  if (!meals.value.length) {
    out.push({ type: 'info', text: '아직 오늘 식단 기록이 없어요.' });
    return out;
  }
  const m = {};
  macros.value.forEach((x) => (m[x.key] = x));
  if (overCalorie.value)
    out.push({ type: 'danger', text: `칼로리가 목표보다 ${(consumed.value - targetKcal.value).toLocaleString()}kcal 많아요.` });
  if (m.protein.value < m.protein.target)
    out.push({ type: 'warning', text: `단백질이 ${m.protein.target - m.protein.value}g 부족해요.` });
  if (m.fat.value > m.fat.target)
    out.push({ type: 'warning', text: `지방이 ${m.fat.value - m.fat.target}g 초과했어요.` });
  if (m.carbs.value < m.carbs.target * 0.5)
    out.push({ type: 'info', text: '탄수화물 섭취가 적어 에너지가 부족할 수 있어요.' });
  return out;
});

// ===== AI 코치 한 줄 요약 (계산) =====
const coachTip = computed(() => {
  if (!meals.value.length) return '오늘 첫 끼니를 기록하면 맞춤 코칭을 시작할게요! 🦕';
  const p = macros.value.find((x) => x.key === 'protein');
  if (overCalorie.value) return '오늘은 칼로리가 목표를 넘었어요. 가벼운 산책 어때요? 🐢';
  if (p.value < p.target) return `단백질이 ${p.target - p.value}g 부족해요. 닭가슴살·두부를 추천해요! 🦖`;
  return '좋은 페이스예요! 이대로 균형을 유지해봐요 🦕';
});

// ===== 물 섭취 (서버 저장 — 새로고침 후에도 유지) =====
const WATER_GOAL = 8;
const water = ref(0);
const waterLeft = computed(() => Math.max(0, WATER_GOAL - water.value));

const { execute: fetchWater } = useGetWater();
const { execute: saveWater } = useSetWater();
if (memberStore.isLoggedIn) {
  fetchWater().then((d) => {
    if (d && d.cups != null) water.value = d.cups;
  });
}
// 로그인 사용자만 기록. 클릭한 잔까지 채우고(같은 잔 다시 누르면 -1) 서버에 저장.
const setWater = async (n) => {
  if (!isLoggedIn.value) return;
  const next = water.value === n ? n - 1 : n;
  water.value = next; // 낙관적 반영
  const r = await saveWater(next);
  if (r && r.cups != null) water.value = r.cups; // 서버 정규화 값으로 확정
};
</script>

<style scoped>
.dash {
  max-width: 1320px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-6) var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  min-height: calc(100vh - 64px - 80px);
}

/* 카드 톤 통일 (대시보드용: 덜 둥글고 부드러운 그림자) */
.cloud-card {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-6);
}

/* 공통 타이틀/메타 */
.c-title {
  font-size: var(--fs-card-title);
  font-weight: 700;
  color: var(--text);
}
.c-sub {
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
  font-weight: 500;
}
.c-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

/* ===== HERO ===== */
.dash-hero {
  display: grid;
  grid-template-columns: 1.7fr 1fr;
  gap: var(--space-5);
  align-items: stretch;
}
.hero-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}
.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
}
.hero-body {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: var(--space-6);
  align-items: center;
  flex: 1;
}

.cal-block {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.cal-remaining {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
}
.cal-num {
  font-size: 60px;
  line-height: 1;
  font-weight: 700;
  color: var(--accent-dark);
}
.cal-num.over {
  color: var(--danger);
}
.cal-unit {
  font-size: var(--fs-body);
  font-weight: 600;
  color: var(--text-muted);
}
.cal-meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}
.meta-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.meta-k {
  font-size: var(--fs-label);
  color: var(--text-muted);
  font-weight: 600;
}
.meta-v {
  font-size: var(--fs-card-title);
  font-weight: 700;
  color: var(--text);
}
.meta-sep {
  width: 1px;
  height: 30px;
  background: var(--surface-muted);
}

.cal-bar {
  height: 10px;
  background: var(--surface-muted);
  border-radius: var(--radius-pill);
  overflow: hidden;
}
.cal-bar-fill {
  height: 100%;
  background: var(--accent);
  border-radius: var(--radius-pill);
  transition: width 0.4s ease;
}
.cal-bar-fill.over {
  background: var(--danger);
}

.cta-block {
  display: block;
  width: 100%;
  text-align: center;
  text-decoration: none;
  padding: 14px;
  font-size: var(--fs-card-title);
}

/* macro rings */
.macro-block {
  display: flex;
  justify-content: space-around;
  gap: var(--space-3);
}
.macro-ring-wrap {
  flex: 1;
  text-align: center;
}
.ring {
  position: relative;
  width: 78px;
  height: 78px;
  margin: 0 auto var(--space-2);
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.4s ease;
}
.ring::before {
  content: '';
  position: absolute;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--surface);
}
.ring-pct {
  position: relative;
  font-size: var(--fs-body);
  font-weight: 800;
  color: var(--text);
}
.macro-label {
  font-size: var(--fs-body-sm);
  font-weight: 700;
  color: var(--text);
}
.macro-amt {
  font-size: var(--fs-label);
  font-weight: 600;
  color: var(--text-muted);
  margin-top: 2px;
}

/* coach card */
.coach-card {
  background: var(--accent);
  color: #fff;
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  box-shadow: var(--shadow-soft);
}
.coach-top {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.coach-emoji {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: var(--color-white);
  border: 3px solid var(--color-tan);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}
.coach-title {
  font-size: var(--fs-card-title);
  font-weight: 700;
}
.coach-sub {
  font-size: var(--fs-label);
  color: rgba(255, 255, 255, 0.85);
  font-weight: 600;
}
.coach-tip {
  background: rgba(255, 255, 255, 0.16);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  font-size: var(--fs-body);
  line-height: 1.55;
  margin: 0;
  flex: 1;
}
.coach-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  align-items: center;
}
.btn-light {
  display: block;
  width: 100%;
  text-align: center;
  text-decoration: none;
  background: var(--color-white);
  color: var(--accent-dark);
  font-weight: 700;
  padding: 12px;
  border-radius: var(--radius-pill);
  font-size: var(--fs-body);
}
.coach-link {
  color: rgba(255, 255, 255, 0.92);
  font-size: var(--fs-body-sm);
  text-decoration: none;
  font-weight: 600;
}

/* ===== DASHBOARD GRID ===== */
.dash-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-5);
}
.grid-card {
  display: flex;
  flex-direction: column;
}
.link-more {
  font-size: var(--fs-body-sm);
  font-weight: 700;
  color: var(--accent-dark);
  text-decoration: none;
  white-space: nowrap;
}

/* weekly chart (라인 그래프) */
.chart {
  display: flex;
  flex-direction: column;
  flex: 1;            /* 카드 남은 높이를 채워 '하루 평균' 아래 공백 제거 */
  min-height: 140px;
}
.line-wrap {
  position: relative;
  flex: 1;
  overflow: visible;
}
.line-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: visible;
}
.t-area {
  stroke: none;
}
.line-svg .g0 {
  stop-color: var(--accent);
  stop-opacity: 0.32;
}
.line-svg .g1 {
  stop-color: var(--accent);
  stop-opacity: 0;
}
.t-path {
  fill: none;
  stroke: var(--accent);
  stroke-width: 2.5;
  stroke-linejoin: round;
  stroke-linecap: round;
  vector-effect: non-scaling-stroke;
}
.t-line {
  stroke: var(--accent-dark);
  stroke-width: 1.5;
  stroke-dasharray: 4 4;
  opacity: 0.6;
  vector-effect: non-scaling-stroke;
}
.dot {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  border: 2px solid var(--surface);
  box-shadow: 0 0 0 1px var(--accent);
  transform: translate(-50%, -50%);
}
.dot.over {
  background: var(--danger);
  box-shadow: 0 0 0 1px var(--danger);
}
.dot.today {
  width: 12px;
  height: 12px;
  background: var(--accent-dark);
  box-shadow: 0 0 0 1px var(--accent-dark);
}
.x-axis {
  position: relative;
  height: 16px;
  margin-top: var(--space-2);
}
.chart-x {
  position: absolute;
  transform: translateX(-50%);
  font-size: var(--fs-label);
  color: var(--text-muted);
  font-weight: 600;
  white-space: nowrap;
}
.chart-x.now {
  color: var(--accent-dark);
  font-weight: 800;
}
.chart-foot {
  margin-top: var(--space-3);
  text-align: right;
}
.lg-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  vertical-align: middle;
  margin-right: 2px;
}
.lg-dot.target {
  background: transparent;
  border-top: 2px dashed var(--accent-dark);
  border-radius: 0;
  width: 12px;
  height: 0;
}
.foot-sep {
  margin: 0 4px;
  opacity: 0.5;
}

/* compact rows (최근 식단) */
.rows {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.row + .row {
  border-top: 1px solid var(--surface-muted);
}
.row-link {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  text-decoration: none;
  color: inherit;
}
.row-thumb {
  width: 38px;
  height: 38px;
  border-radius: var(--radius-md);
  background: var(--surface-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.row-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
  gap: 2px;
}
.row-title {
  font-size: var(--fs-body);
  font-weight: 600;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.row-meta {
  font-size: var(--fs-label);
  color: var(--text-muted);
}
.row-kcal {
  font-weight: 700;
  color: var(--accent-dark);
  font-size: var(--fs-body);
  white-space: nowrap;
}
.row-kcal i {
  font-style: normal;
  font-size: var(--fs-label);
  color: var(--text-muted);
  margin-left: 2px;
}

/* challenge panel */
.ch-panel {
  display: block;
  text-decoration: none;
  color: inherit;
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  border-left: 5px solid var(--primary);
}
.ch-top {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}
.ch-emoji {
  font-size: 18px;
}
.ch-name {
  font-weight: 700;
  color: var(--text);
  font-size: var(--fs-body);
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ch-desc {
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
  margin: 0 0 var(--space-3);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.ch-foot {
  display: flex;
  justify-content: space-between;
  font-size: var(--fs-body-sm);
  font-weight: 700;
  color: var(--text-muted);
}
.ch-go {
  color: var(--primary);
}

/* nutrition alerts */
.alerts {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.alert {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--fs-body-sm);
  font-weight: 600;
  color: var(--text);
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
}
.alert-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  background: var(--text-muted);
}
.alert.danger .alert-dot {
  background: var(--danger);
}
.alert.warning .alert-dot {
  background: var(--warning);
}
.alert.info .alert-dot {
  background: var(--accent);
}

/* water cups */
.cups {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  margin-bottom: var(--space-4);
}
.cup {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--surface-muted);
  font-size: 18px;
  cursor: pointer;
  filter: grayscale(1) opacity(0.45);
  transition: all 0.15s ease;
}
.cup.on {
  filter: none;
  background: #e3f1ee;
}
.cup:active {
  transform: scale(0.92);
}
.water-foot {
  margin-top: var(--space-3);
}

/* community post panel */
.post-panel {
  display: block;
  text-decoration: none;
  color: inherit;
  flex: 1;
}
.post-title {
  font-size: var(--fs-body);
  font-weight: 600;
  color: var(--text);
  margin: var(--space-2) 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* empty states */
.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: var(--space-3);
  padding: var(--space-6) var(--space-4);
  color: var(--text-muted);
  font-size: var(--fs-body-sm);
  font-weight: 600;
}
.empty-emoji {
  font-size: 34px;
}
.empty-cta {
  text-decoration: none;
  color: #fff;
  background: var(--primary);
  padding: 8px 18px;
  border-radius: var(--radius-pill);
  font-weight: 700;
  font-size: var(--fs-body-sm);
}
.empty.ok {
  color: var(--accent-dark);
}

/* pills */
.pill {
  font-size: var(--fs-label);
  font-weight: 700;
  padding: 5px 12px;
  border-radius: var(--radius-pill);
  white-space: nowrap;
}
.pill-accent {
  background: rgba(119, 192, 179, 0.18);
  color: var(--accent-dark);
}
.pill-danger {
  background: rgba(216, 61, 82, 0.14);
  color: var(--primary-dark);
}
.pill.sm {
  padding: 2px 8px;
}

/* ===== 게스트(미로그인) ===== */
/* ===== 게스트 랜딩 ===== */
.landing {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}
/* HERO */
.hero {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: var(--space-6);
  align-items: center;
  min-height: 460px;
  padding: var(--space-8) var(--space-6);
  border-radius: var(--radius-lg);
  background:
    radial-gradient(120% 120% at 85% 10%, rgba(119, 192, 179, 0.18), transparent 55%),
    radial-gradient(90% 90% at 10% 90%, rgba(217, 195, 163, 0.22), transparent 60%),
    var(--surface);
  border: 1px solid var(--border-soft);
  box-shadow: var(--shadow-soft);
}
.hero-badge {
  display: inline-block;
  background: var(--surface-muted);
  border: 1px solid var(--border-soft);
  color: var(--accent-dark);
  font-weight: 800;
  font-size: var(--fs-label);
  padding: 6px 14px;
  border-radius: var(--radius-pill);
}
.hero-title {
  font-size: clamp(28px, 3.4vw, 44px);
  line-height: 1.2;
  font-weight: 800;
  color: var(--text-strong);
  margin: var(--space-3) 0 var(--space-3);
}
.hero-title em {
  font-style: normal;
  color: var(--accent-dark);
}
.hero-sub {
  font-size: var(--fs-body);
  color: var(--text-muted);
  line-height: 1.7;
  margin: 0 0 var(--space-5);
  max-width: 460px;
}
.hero-cta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}
.hero-cta .cta-lg {
  text-decoration: none;
  padding: 14px 30px;
  font-size: var(--fs-body);
  font-weight: 800;
  border-radius: var(--radius-pill);
}
.cta-secondary {
  text-decoration: none;
  font-weight: 700;
  color: var(--accent-dark);
  font-size: var(--fs-body-sm);
  padding: 12px 14px;
}
.cta-secondary:hover {
  text-decoration: underline;
}
.hero-stats {
  display: flex;
  gap: var(--space-5);
  margin-top: var(--space-6);
  flex-wrap: wrap;
}
.hs {
  display: flex;
  flex-direction: column;
}
.hs b {
  font-size: var(--fs-card-title);
  font-weight: 800;
  color: var(--text-strong);
}
.hs span {
  font-size: var(--fs-label);
  color: var(--text-muted);
}

/* HERO 우측 앱 프리뷰 */
.hero-right {
  position: relative;
  display: flex;
  justify-content: center;
}
.preview-glow {
  position: absolute;
  inset: 12% 6% 0 6%;
  background: radial-gradient(60% 50% at 50% 40%, rgba(119, 192, 179, 0.25), transparent 70%);
  filter: blur(6px);
  z-index: 0;
}
.preview {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 360px;
  background: var(--surface);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-elevated);
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: 10px;
  transform: rotate(-1deg);
}
.pv-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pv-emoji {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--surface-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.pv-headtext {
  flex: 1;
}
.pv-name {
  font-weight: 800;
  color: var(--text-strong);
  font-size: var(--fs-body-sm);
}
.pv-date {
  font-size: var(--fs-label);
  color: var(--text-muted);
}
.pv-pill {
  background: rgba(119, 192, 179, 0.18);
  color: var(--accent-dark);
  font-size: var(--fs-label);
  font-weight: 800;
  padding: 4px 10px;
  border-radius: var(--radius-pill);
}
.pv-cal {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.pv-cal b {
  font-size: 34px;
  font-weight: 800;
  color: var(--accent-dark);
}
.pv-cal span {
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
}
.pv-bar {
  height: 8px;
  background: var(--surface-muted);
  border-radius: var(--radius-pill);
  overflow: hidden;
}
.pv-bar-fill {
  height: 100%;
  background: var(--accent);
  border-radius: var(--radius-pill);
}
.pv-macros {
  display: flex;
  gap: 8px;
}
.pv-macro {
  flex: 1;
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  padding: 8px;
  text-align: center;
}
.pv-macro span {
  display: block;
  font-size: var(--fs-label);
  color: var(--text-muted);
}
.pv-macro b {
  font-size: var(--fs-body-sm);
  color: var(--text-strong);
}
.pv-coach {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  padding: 10px;
}
.pv-coach-emoji {
  font-size: 18px;
}
.pv-coach p {
  margin: 0;
  font-size: var(--fs-body-sm);
  color: var(--text);
  line-height: 1.4;
}
.pv-chal {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--fs-label);
  color: var(--text-muted);
}
.pv-chal-name {
  font-weight: 700;
  color: var(--text-strong);
}
.pv-chal-bar {
  flex: 1;
  height: 6px;
  background: var(--surface-muted);
  border-radius: var(--radius-pill);
  overflow: hidden;
}
.pv-chal-bar > div {
  height: 100%;
  background: var(--accent);
}
.pv-diet {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.pv-diet li {
  display: flex;
  justify-content: space-between;
  font-size: var(--fs-body-sm);
  color: var(--text);
  padding: 6px 0;
  border-top: 1px solid var(--border-soft);
}
.pv-kc {
  color: var(--accent-dark);
  font-weight: 700;
}

/* 기능 카드 */
.feat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}
.feat {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.feat-top {
  display: flex;
  align-items: center;
  gap: 8px;
}
.feat-ic {
  font-size: 24px;
}
.feat-t {
  font-weight: 800;
  color: var(--text-strong);
  font-size: var(--fs-card-title);
}
.feat-d {
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
  line-height: 1.5;
  margin: 0;
  flex: 1;
}
.feat-ui {
  margin-top: var(--space-2);
}
.feat-ui.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.chip {
  font-size: var(--fs-label);
  font-weight: 700;
  background: var(--surface-muted);
  color: var(--text-muted);
  border-radius: var(--radius-pill);
  padding: 4px 10px;
}
.chip.ai {
  background: #efe7fb;
  color: #6b46c1;
}
.feat-ui.bubble {
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  padding: 8px 12px;
  font-size: var(--fs-body-sm);
  color: var(--text);
}
.mini-bar {
  height: 8px;
  background: var(--surface-muted);
  border-radius: var(--radius-pill);
  overflow: hidden;
}
.mini-bar > div {
  height: 100%;
  background: var(--accent);
}
.mini-cap {
  display: block;
  margin-top: 4px;
  font-size: var(--fs-label);
  color: var(--text-muted);
  font-weight: 700;
}
.feat-ui.spark {
  display: flex;
  align-items: flex-end;
  gap: 5px;
  height: 44px;
}
.feat-ui.spark i {
  flex: 1;
  background: var(--accent);
  opacity: 0.7;
  border-radius: 3px 3px 0 0;
}

/* 서비스 흐름 */
.flow {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-6);
}
.flow-title {
  font-weight: 800;
  color: var(--text-strong);
  font-size: var(--fs-card-title);
  margin-bottom: var(--space-4);
  text-align: center;
}
.flow-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}
.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  min-width: 130px;
}
.step-ic {
  font-size: 24px;
}
.step b {
  color: var(--text-strong);
  font-size: var(--fs-body);
}
.step span {
  font-size: var(--fs-label);
  color: var(--text-muted);
}
.step-arrow {
  color: var(--accent-dark);
  font-weight: 800;
  font-size: 20px;
}
.flow-cta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  margin-top: var(--space-5);
  flex-wrap: wrap;
}
.flow-cta .btn-primary {
  text-decoration: none;
  padding: 12px 28px;
  border-radius: var(--radius-pill);
  font-weight: 800;
}
.flow-note {
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
}
.flow-note a {
  color: var(--accent-dark);
  font-weight: 700;
}

/* ===== responsive ===== */
@media (max-width: 1180px) {
  .dash-hero {
    grid-template-columns: 1fr;
  }
  .dash-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  /* 랜딩: hero 상하 배치, 기능 카드 2열 */
  .hero {
    grid-template-columns: 1fr;
    min-height: auto;
    text-align: center;
  }
  .hero-sub {
    margin-left: auto;
    margin-right: auto;
  }
  .hero-cta,
  .hero-stats {
    justify-content: center;
  }
  .hero-right {
    margin-top: var(--space-3);
  }
  .feat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 768px) {
  .dash {
    padding: var(--space-4) var(--space-4) var(--space-6);
    gap: var(--space-4);
  }
  .hero-body {
    grid-template-columns: 1fr;
    gap: var(--space-5);
  }
  .dash-grid {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }
  .cal-num {
    font-size: 50px;
  }
  /* 랜딩 모바일 */
  .hero {
    padding: var(--space-5) var(--space-4);
  }
  .feat-grid {
    grid-template-columns: 1fr;
  }
  .hero-cta {
    flex-direction: column;
    align-items: stretch;
  }
  .hero-cta .cta-lg {
    text-align: center;
  }
  .cta-secondary {
    text-align: center;
  }
  .preview {
    transform: none;
  }
  .step-arrow {
    transform: rotate(90deg);
  }
}
.target-cta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  margin-bottom: var(--space-4);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  border: 1px solid var(--border-soft);
  border-left: 4px solid var(--accent);
  text-decoration: none;
  color: var(--text);
}
.target-cta .cta-emoji {
  font-size: 26px;
  flex-shrink: 0;
}
.target-cta .cta-text {
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
  line-height: 1.5;
}
.target-cta .cta-text b {
  color: var(--text-strong);
  display: block;
}
.target-cta .cta-go {
  margin-left: auto;
  flex-shrink: 0;
  font-weight: 700;
  color: var(--accent-dark);
  font-size: var(--fs-body-sm);
  white-space: nowrap;
}
</style>
