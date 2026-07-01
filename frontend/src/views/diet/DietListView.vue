<template>
  <div class="diet-page">
    <!-- ===== 헤더 ===== -->
    <header class="dp-header">
      <div>
        <h1 class="dino-page-title">내 식단 기록 🥗</h1>
        <p class="dino-page-subtitle">오늘의 식단과 최근 기록을 확인하세요.</p>
      </div>
      <router-link :to="{ name: 'diet-write' }" class="btn-primary cta">＋ 식단 기록</router-link>
    </header>

    <!-- ===== 오늘 요약 (3 카드) ===== -->
    <section class="summary-row">
      <!-- 오늘 총 섭취 -->
      <div class="cloud-card sum-card">
        <div class="sc-title">오늘 총 섭취</div>
        <div class="sc-stat">
          <span class="sc-num" :class="{ over: todayOver }">{{ todayKcal.toLocaleString() }}</span>
          <span class="sc-unit">/ {{ TARGET.toLocaleString() }} kcal</span>
        </div>
        <div class="bar">
          <div class="bar-fill" :class="{ over: todayOver }" :style="{ width: todayPct + '%' }" />
        </div>
        <div class="sc-foot" :class="{ over: todayOver }">
          {{ todayOver
            ? `목표보다 ${(todayKcal - TARGET).toLocaleString()}kcal 초과`
            : `${todayRemaining.toLocaleString()}kcal 남았어요` }}
        </div>
      </div>

      <!-- 끼니 기록 상태 -->
      <div class="cloud-card sum-card">
        <div class="sc-title">끼니 기록 상태</div>
        <ul class="meal-status">
          <li v-for="s in todayStatus" :key="s.key">
            <span class="ms-name">{{ s.emoji }} {{ s.label }}</span>
            <span class="ms-tag" :class="s.done ? 'done' : 'todo'">{{ s.done ? '완료' : '미기록' }}</span>
          </li>
        </ul>
      </div>

      <!-- 영양 균형 -->
      <div class="cloud-card sum-card">
        <div class="sc-title">영양 균형 · 오늘</div>
        <div v-for="m in todayMacros" :key="m.key" class="nb">
          <div class="nb-top">
            <span class="nb-label">{{ m.label }}</span>
            <span class="nb-g">{{ m.value }} / {{ m.target }}g</span>
          </div>
          <div class="bar sm">
            <div class="bar-fill" :style="{ width: m.pct + '%', background: m.color }" />
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 주간 캘린더 ===== -->
    <WeeklyCalendarBar
      :days="weekDays"
      @select="selectDate"
      @prev="shiftWeek(-7)"
      @next="shiftWeek(7)"
      @open-month="showMonth = true"
    />

    <!-- ===== 선택 날짜 기록 + AI 분석 ===== -->
    <section class="content-row">
      <div class="records">
        <div class="rec-head">
          <h2 class="section-title">{{ selectedTitle }}</h2>
          <span class="rec-sum">{{ selectedKcal.toLocaleString() }} kcal · {{ selectedFilled }}/4 끼니</span>
        </div>

        <div v-for="s in selectedSlots" :key="s.key" class="meal-group">
          <div class="mg-head">
            <span class="mg-title">{{ s.emoji }} {{ s.label }}</span>
            <span v-if="s.items.length" class="mg-kcal">{{ s.kcal }} kcal</span>
          </div>

          <template v-if="s.items.length">
            <div v-for="it in s.items" :key="it.dno" class="rec-item">
              <div class="ri-main">
                <div class="ri-title">{{ it.title }}</div>
                <div class="ri-meta">
                  {{ s.label }} · {{ it.date }} · 탄 {{ it.carbs }}g · 단 {{ it.protein }}g · 지 {{ it.fat }}g
                </div>
              </div>
              <div class="ri-kcal">{{ it.kcal }}<i>kcal</i></div>
              <div class="ri-actions">
                <router-link :to="{ name: 'diet-detail', params: { dno: it.dno } }" class="ria" title="상세 보기">🔍</router-link>
                <router-link :to="{ name: 'diet-modify', params: { dno: it.dno } }" class="ria" title="수정">✏️</router-link>
                <button type="button" class="ria danger" title="삭제" @click="onDelete(it.dno)">🗑</button>
              </div>
            </div>
          </template>

          <div v-else class="mg-empty">
            <div class="mg-empty-text">
              아직 {{ s.label }} 기록이 없어요.
              <small>기록하면 AI가 하루 영양 균형을 분석해드려요.</small>
            </div>
            <router-link :to="{ name: 'diet-write', query: { date: selected, meal: s.key } }" class="mg-cta">
              {{ s.label }} 기록하기
            </router-link>
          </div>
        </div>
      </div>

      <!-- AI 분석 -->
      <aside class="ai-card cloud-card">
        <div class="ai-head">
          <span class="ai-emoji">🦕</span>
          <span class="ai-title">AI 식단 분석</span>
        </div>
        <p class="ai-date">{{ selectedTitle }}</p>
        <ul class="ai-lines">
          <li v-for="(l, i) in insight" :key="i">{{ l }}</li>
        </ul>
        <div class="ai-actions">
          <router-link :to="{ name: 'ai-diet-analysis' }" class="ai-btn">AI 분석 자세히 보기</router-link>
          <router-link :to="{ name: 'ai-workout-coach' }" class="ai-link">AI 코치에게 물어보기 →</router-link>
        </div>
      </aside>
    </section>

    <!-- 월간 보기 모달 -->
    <MonthlyCalendarModal
      v-if="showMonth"
      :selected="selected"
      :record-set="recordSet"
      :today="today"
      @select="selectDate"
      @close="showMonth = false"
    />

    <!-- 모바일 FAB -->
    <router-link :to="{ name: 'diet-write' }" class="fab" aria-label="식단 기록">＋</router-link>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { ssafyapi } from '@/restapi/index';
import { useFetchDiets } from '@/composables/useDietApis';
import { useMyTarget } from '@/composables/useMemberApis';
import WeeklyCalendarBar from '@/components/diet/WeeklyCalendarBar.vue';
import MonthlyCalendarModal from '@/components/diet/MonthlyCalendarModal.vue';

const TARGET = ref(2000); // 개인 목표 칼로리 (프로필 기반, 미설정 시 2000)
const MACRO_TARGETS = { protein: 60, carbs: 250, fat: 60 };
const MEALS = [
  ['breakfast', '아침', '🥣'],
  ['lunch', '점심', '🥗'],
  ['dinner', '저녁', '🍗'],
  ['snack', '간식', '🍎'],
];
const WEEKDAYS_KO = ['일', '월', '화', '수', '목', '금', '토'];

const pad = (n) => String(n).padStart(2, '0');
const toStr = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const parse = (s) => {
  const [y, m, d] = s.split('-').map(Number);
  return new Date(y, m - 1, d);
};
const mmdd = (s) => s.slice(5);

const today = toStr(new Date());
const selected = ref(today);
const showMonth = ref(false);

// 전체 식단을 한 번에 받아 날짜(MM-dd) 기준으로 가공
const { data: allDiets, execute: fetchAll } = useFetchDiets();
fetchAll({});

// 개인 목표 칼로리 (서버 계산·저장값)
const { execute: fetchTarget } = useMyTarget();
fetchTarget().then((t) => {
  if (t?.targetCalories) TARGET.value = t.targetCalories;
});

const dayList = (dateStr) => (allDiets.value ?? []).filter((d) => d.date === mmdd(dateStr));
const recordSet = computed(() => new Set((allDiets.value ?? []).map((d) => d.date)));

// ===== 오늘 요약 =====
const todayList = computed(() => dayList(today));
const todayKcal = computed(() => todayList.value.reduce((s, d) => s + (d.kcal || 0), 0));
const todayOver = computed(() => todayKcal.value > TARGET.value);
const todayPct = computed(() => Math.min(100, Math.round((todayKcal.value / TARGET.value) * 100)));
const todayRemaining = computed(() => Math.max(0, TARGET.value - todayKcal.value));

const todayStatus = computed(() =>
  MEALS.map(([key, label, emoji]) => ({
    key,
    label,
    emoji,
    done: todayList.value.some((d) => d.meal === key),
  }))
);

const todayMacros = computed(() => {
  const sum = (k) => todayList.value.reduce((s, d) => s + (d[k] || 0), 0);
  return [
    { key: 'carbs', label: '탄수화물', color: 'var(--color-teal)' },
    { key: 'protein', label: '단백질', color: 'var(--color-red)' },
    { key: 'fat', label: '지방', color: 'var(--color-tan)' },
  ].map((m) => {
    const value = sum(m.key);
    const target = MACRO_TARGETS[m.key];
    return { ...m, value, target, pct: Math.min(100, Math.round((value / target) * 100)) };
  });
});

// ===== 주간 캘린더 =====
const weekDays = computed(() => {
  const sel = parse(selected.value);
  const lead = (sel.getDay() + 6) % 7; // 월요일 시작
  const monday = new Date(sel);
  monday.setDate(sel.getDate() - lead);
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    const s = toStr(d);
    const list = dayList(s);
    return {
      date: s,
      day: d.getDate(),
      weekday: WEEKDAYS_KO[d.getDay()],
      kcal: list.reduce((acc, x) => acc + (x.kcal || 0), 0),
      hasRecord: list.length > 0,
      isToday: s === today,
      isSelected: s === selected.value,
    };
  });
});

// ===== 선택 날짜 기록 =====
const selectedList = computed(() => dayList(selected.value));
const selectedKcal = computed(() => selectedList.value.reduce((s, d) => s + (d.kcal || 0), 0));
const selectedFilled = computed(
  () => MEALS.filter(([key]) => selectedList.value.some((d) => d.meal === key)).length
);
const selectedTitle = computed(() => {
  const d = parse(selected.value);
  return `${pad(d.getMonth() + 1)}월 ${pad(d.getDate())}일 ${WEEKDAYS_KO[d.getDay()]}요일`;
});
const selectedSlots = computed(() =>
  MEALS.map(([key, label, emoji]) => {
    const items = selectedList.value.filter((d) => d.meal === key);
    return { key, label, emoji, items, kcal: items.reduce((s, d) => s + (d.kcal || 0), 0) };
  })
);

// ===== AI 분석 (선택 날짜 기준 계산) =====
const insight = computed(() => {
  const list = selectedList.value;
  if (!list.length) {
    return [
      '아직 이 날의 식단 기록이 없어요.',
      '한 끼라도 기록하면 AI가 하루 영양 균형을 분석해드려요.',
    ];
  }
  const kcal = selectedKcal.value;
  const protein = list.reduce((s, d) => s + (d.protein || 0), 0);
  const lines = [];
  lines.push(
    kcal > TARGET.value
      ? `총 ${kcal.toLocaleString()}kcal로 목표(${TARGET.value.toLocaleString()})를 넘었어요. 남은 끼니는 가볍게 가요.`
      : `총 ${kcal.toLocaleString()}kcal · 목표까지 ${(TARGET.value - kcal).toLocaleString()}kcal 남았어요.`
  );
  lines.push(
    protein < MACRO_TARGETS.protein
      ? `단백질이 ${MACRO_TARGETS.protein - protein}g 부족해요. 닭가슴살·두부·계란을 추천해요.`
      : `단백질 ${protein}g으로 충분히 챙겼어요. 좋아요! 🦖`
  );
  const missing = selectedSlots.value.filter((s) => !s.items.length).map((s) => s.label);
  if (missing.length && missing.length < 4) lines.push(`${missing.join('·')} 기록이 비어 있어요.`);
  return lines.slice(0, 3);
});

// ===== 액션 =====
const selectDate = (date) => (selected.value = date);
const shiftWeek = (delta) => {
  const d = parse(selected.value);
  d.setDate(d.getDate() + delta);
  selected.value = toStr(d);
};
const onDelete = async (dno) => {
  if (!confirm('이 식단 기록을 삭제할까요?')) return;
  await ssafyapi.delete(`/diets/${dno}`);
  fetchAll({});
};
</script>

<style scoped>
.diet-page {
  max-width: 1320px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-6) var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  min-height: calc(100vh - 64px - 80px);
}

/* 헤더 */
.dp-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: var(--space-4);
}
.dp-header .dino-page-subtitle {
  margin-bottom: 0;
}
.cta {
  text-decoration: none;
  white-space: nowrap;
  padding: 12px 24px;
  font-size: var(--fs-body);
}

/* 카드 톤 통일 */
.cloud-card {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-5);
}

/* 오늘 요약 */
.summary-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-5);
}
.sum-card {
  display: flex;
  flex-direction: column;
}
.sc-title {
  font-size: var(--fs-label);
  font-weight: 700;
  color: var(--text-muted);
  margin-bottom: var(--space-3);
}
.sc-stat {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
}
.sc-num {
  font-size: 34px;
  font-weight: 800;
  color: var(--accent-dark);
  line-height: 1;
}
.sc-num.over {
  color: var(--danger);
}
.sc-unit {
  font-size: var(--fs-body-sm);
  font-weight: 600;
  color: var(--text-muted);
}
.bar {
  height: 9px;
  background: var(--surface-muted);
  border-radius: var(--radius-pill);
  overflow: hidden;
  margin: var(--space-3) 0 var(--space-2);
}
.bar.sm {
  height: 7px;
  margin: 4px 0 0;
}
.bar-fill {
  height: 100%;
  background: var(--accent);
  border-radius: var(--radius-pill);
  transition: width 0.4s ease;
}
.bar-fill.over {
  background: var(--danger);
}
.sc-foot {
  font-size: var(--fs-body-sm);
  font-weight: 700;
  color: var(--accent-dark);
  margin-top: auto;
}
.sc-foot.over {
  color: var(--danger);
}

/* 끼니 상태 */
.meal-status {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.meal-status li {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.ms-name {
  font-size: var(--fs-body-sm);
  font-weight: 600;
  color: var(--text);
}
.ms-tag {
  font-size: var(--fs-label);
  font-weight: 700;
  padding: 3px 10px;
  border-radius: var(--radius-pill);
}
.ms-tag.done {
  background: rgba(63, 157, 122, 0.16);
  color: var(--success);
}
.ms-tag.todo {
  background: var(--surface-muted);
  color: var(--text-muted);
}

/* 영양 균형 */
.nb {
  margin-bottom: var(--space-3);
}
.nb:last-child {
  margin-bottom: 0;
}
.nb-top {
  display: flex;
  justify-content: space-between;
  font-size: var(--fs-body-sm);
  font-weight: 600;
}
.nb-label {
  color: var(--text);
}
.nb-g {
  color: var(--text-muted);
}

/* 선택 날짜 기록 + AI */
.content-row {
  display: grid;
  grid-template-columns: 1.8fr 1fr;
  gap: var(--space-5);
  align-items: start;
}
.rec-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.section-title {
  font-size: var(--fs-section-title);
  font-weight: 800;
  color: var(--text-strong);
  margin: 0;
}
.rec-sum {
  font-size: var(--fs-body-sm);
  font-weight: 700;
  color: var(--text-muted);
}

.meal-group {
  margin-bottom: var(--space-4);
}
.mg-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}
.mg-title {
  font-size: var(--fs-card-title);
  font-weight: 700;
  color: var(--text-strong);
}
.mg-kcal {
  font-size: var(--fs-body-sm);
  font-weight: 700;
  color: var(--accent-dark);
}

/* 기록 아이템 (일지형 row) */
.rec-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  background: var(--surface);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-2);
  box-shadow: var(--shadow-soft);
}
.ri-main {
  flex: 1;
  min-width: 0;
}
.ri-title {
  font-size: var(--fs-body);
  font-weight: 700;
  color: var(--text-strong);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ri-meta {
  font-size: var(--fs-label);
  color: var(--text-muted);
  margin-top: 2px;
}
.ri-kcal {
  font-size: var(--fs-card-title);
  font-weight: 800;
  color: var(--accent-dark);
  white-space: nowrap;
}
.ri-kcal i {
  font-style: normal;
  font-size: var(--fs-label);
  color: var(--text-muted);
  margin-left: 2px;
}
.ri-actions {
  display: flex;
  gap: 2px;
}
.ria {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 15px;
  text-decoration: none;
}
.ria:hover {
  background: var(--surface-muted);
}

/* 끼니 미기록 empty state */
.mg-empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
}
.mg-empty-text {
  font-size: var(--fs-body-sm);
  font-weight: 600;
  color: var(--text-muted);
}
.mg-empty-text small {
  display: block;
  font-size: var(--fs-label);
  font-weight: 500;
  opacity: 0.85;
  margin-top: 2px;
}
.mg-cta {
  flex-shrink: 0;
  text-decoration: none;
  background: var(--primary);
  color: #fff;
  font-weight: 700;
  font-size: var(--fs-body-sm);
  padding: 9px 16px;
  border-radius: var(--radius-pill);
  white-space: nowrap;
}

/* AI 카드 */
.ai-card {
  background: var(--accent);
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  position: sticky;
  top: 84px;
}
.ai-head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.ai-emoji {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-white);
  border: 3px solid var(--color-tan);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.ai-title {
  font-size: var(--fs-card-title);
  font-weight: 800;
}
.ai-date {
  margin: 0;
  font-size: var(--fs-label);
  font-weight: 700;
  color: rgba(255, 255, 255, 0.85);
}
.ai-lines {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.ai-lines li {
  background: rgba(255, 255, 255, 0.16);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  font-size: var(--fs-body-sm);
  line-height: 1.5;
  font-weight: 600;
}
.ai-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  align-items: center;
  margin-top: var(--space-2);
}
.ai-btn {
  display: block;
  width: 100%;
  text-align: center;
  text-decoration: none;
  background: var(--color-white);
  color: var(--accent-dark);
  font-weight: 700;
  padding: 11px;
  border-radius: var(--radius-pill);
  font-size: var(--fs-body-sm);
}
.ai-link {
  color: rgba(255, 255, 255, 0.92);
  font-size: var(--fs-body-sm);
  text-decoration: none;
  font-weight: 600;
}

/* FAB (모바일 전용) */
.fab {
  display: none;
  position: fixed;
  right: 18px;
  bottom: 18px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-size: 28px;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  box-shadow: var(--shadow-elevated);
  z-index: 90;
}

/* ===== 반응형 ===== */
@media (max-width: 1024px) {
  .summary-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .content-row {
    grid-template-columns: 1fr;
  }
  .ai-card {
    position: static;
  }
}
@media (max-width: 768px) {
  .diet-page {
    padding: var(--space-4) var(--space-4) 96px;
    gap: var(--space-4);
  }
  .summary-row {
    grid-template-columns: 1fr;
  }
  .cta {
    display: none; /* 모바일은 FAB 로 대체 */
  }
  .fab {
    display: flex;
  }
}
</style>
