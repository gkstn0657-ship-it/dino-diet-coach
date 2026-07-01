<template>
  <div class="dino-page">
    <h1 class="dino-page-title">📊 칼로리 추이</h1>
    <p class="dino-page-subtitle">기간을 설정해 일별 섭취 칼로리를 확인하세요.</p>

    <div class="trend-shell">
      <!-- 기간 빠른 선택 -->
      <div class="period-tabs">
        <button
          v-for="p in presets"
          :key="p.days"
          type="button"
          class="period-tab"
          :class="{ active: activePreset === p.days }"
          @click="applyPreset(p.days)"
        >
          {{ p.label }}
        </button>
      </div>

      <!-- 날짜 직접 지정 -->
      <div class="date-range">
        <label>시작 <input v-model="from" type="date" :max="to" @change="onManualChange" /></label>
        <span class="tilde">~</span>
        <label>종료 <input v-model="to" type="date" :min="from" :max="today" @change="onManualChange" /></label>
        <button type="button" class="btn-apply" @click="load">조회</button>
      </div>

      <!-- 요약 -->
      <div class="summary">
        <div class="summary-item">
          <span class="summary-label">하루 평균</span>
          <span class="summary-value">{{ avgKcal.toLocaleString() }} kcal</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">목표 초과 일수</span>
          <span class="summary-value over">{{ overDays }}일</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">기록일</span>
          <span class="summary-value">{{ trend.length }}일</span>
        </div>
      </div>

      <!-- 분석 -->
      <div v-if="trend.length" class="analysis">
        <div class="ana"><span>📈 최근 추세</span><b>{{ trendText }}</b></div>
        <div class="ana"><span>🔺 가장 많이 먹은 날</span><b>{{ maxDayText }}</b></div>
        <div class="ana"><span>🔻 가장 적게 먹은 날</span><b>{{ minDayText }}</b></div>
        <div class="ana"><span>🎯 하루 목표</span><b>{{ targetKcal.toLocaleString() }} kcal</b></div>
      </div>

      <!-- 범례 -->
      <div class="legend">
        <span><i class="sw ok"></i> 목표 이내</span>
        <span><i class="sw over"></i> 목표 초과</span>
        <span><i class="sw line"></i> 목표선</span>
      </div>

      <!-- 차트 -->
      <div v-if="isLoading" class="chart-empty">불러오는 중...</div>
      <div v-else-if="!trend.length" class="chart-empty">해당 기간에 기록된 식단이 없어요.</div>
      <div v-else class="chart-scroll">
        <div class="chart" :style="{ minWidth: Math.max(trend.length * 40, 320) + 'px' }">
          <div class="bars">
            <svg class="line-svg" viewBox="0 0 100 100" preserveAspectRatio="none">
              <defs>
                <linearGradient id="calGradDetail" x1="0" y1="0" x2="0" y2="1">
                  <stop class="g0" offset="0%" />
                  <stop class="g1" offset="100%" />
                </linearGradient>
              </defs>
              <path class="t-area" :d="areaPath" fill="url(#calGradDetail)" />
              <line class="t-line" x1="0" :y1="targetY" x2="100" :y2="targetY" />
              <path class="t-path" :d="linePath" />
            </svg>
            <span class="target-line-label" :style="{ top: targetY + '%' }"
              >목표 {{ targetKcal.toLocaleString() }}</span
            >
            <span
              v-for="(p, i) in points"
              :key="i"
              class="dot"
              :class="{ over: p.over }"
              :style="{ left: p.x + '%', top: p.y + '%' }"
              :title="`${shortDate(p.statDate)} · ${(p.totalKcal || 0).toLocaleString()}kcal`"
            ></span>
          </div>
          <div class="xrow">
            <div v-for="(d, i) in trend" :key="i" class="x-col">{{ shortDate(d.statDate) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ssafyapi } from '@/restapi/index';
import { useMyTarget } from '@/composables/useMemberApis';

const presets = [
  { label: '7일', days: 7 },
  { label: '30일', days: 30 },
  { label: '90일', days: 90 },
  { label: '전체', days: 365 },
];

const today = new Date().toISOString().slice(0, 10);
const from = ref('');
const to = ref(today);
const activePreset = ref(7);
const trend = ref([]);
const isLoading = ref(false);
const targetKcal = ref(2000);

const ymd = (d) => d.toISOString().slice(0, 10);

const applyPreset = (days) => {
  activePreset.value = days;
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - (days - 1));
  from.value = ymd(start);
  to.value = ymd(end);
  load();
};

// 날짜를 직접 바꾸면 프리셋 해제
const onManualChange = () => {
  activePreset.value = 0;
};

const load = async () => {
  if (!from.value || !to.value) return;
  isLoading.value = true;
  try {
    const res = await ssafyapi.get('/stats/me/range', {
      params: { from: from.value, to: to.value },
    });
    trend.value = res.data.payload ?? [];
  } catch {
    trend.value = [];
  } finally {
    isLoading.value = false;
  }
};

const { execute: fetchTarget } = useMyTarget();

// ===== 그래프 =====
const PAD_TOP = 12; // 라인 그래프 위/아래 여백(%)
const PAD_BOT = 10;
const totalKcal = computed(() => trend.value.reduce((s, d) => s + (d.totalKcal || 0), 0));
const avgKcal = computed(() =>
  trend.value.length ? Math.round(totalKcal.value / trend.value.length) : 0
);
const maxKcal = computed(() => Math.max(1, ...trend.value.map((d) => d.totalKcal || 0)));
// 목표선이 항상 그래프 안에 들어오도록 최댓값에 목표도 포함
const chartMax = computed(() => Math.max(maxKcal.value, targetKcal.value));
const yOf = (kcal) => PAD_TOP + (1 - (kcal || 0) / (chartMax.value || 1)) * (100 - PAD_TOP - PAD_BOT);
const points = computed(() =>
  trend.value.map((d, i) => ({
    ...d,
    x: ((i + 0.5) / (trend.value.length || 1)) * 100,
    y: yOf(d.totalKcal),
    over: (d.totalKcal || 0) > targetKcal.value,
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
const linePath = computed(() => smoothPath(points.value));
const areaPath = computed(() => {
  const pts = points.value;
  if (!pts.length) return '';
  return `${smoothPath(pts)} L ${pts[pts.length - 1].x.toFixed(2)} 100 L ${pts[0].x.toFixed(2)} 100 Z`;
});
const targetY = computed(() => yOf(targetKcal.value));
const shortDate = (s) => (s ? s.slice(5) : ''); // MM-DD

// ===== 분석 정보 =====
const overDays = computed(() => trend.value.filter((d) => (d.totalKcal || 0) > targetKcal.value).length);
const maxDay = computed(() =>
  trend.value.reduce((a, d) => ((d.totalKcal || 0) > (a ? a.totalKcal : -1) ? d : a), null)
);
const minDay = computed(() =>
  trend.value.reduce((a, d) => ((d.totalKcal || 0) < (a ? a.totalKcal : Infinity) ? d : a), null)
);
const maxDayText = computed(() =>
  maxDay.value ? `${shortDate(maxDay.value.statDate)} · ${maxDay.value.totalKcal.toLocaleString()}kcal` : '-'
);
const minDayText = computed(() =>
  minDay.value ? `${shortDate(minDay.value.statDate)} · ${minDay.value.totalKcal.toLocaleString()}kcal` : '-'
);
const trendText = computed(() => {
  const a = trend.value;
  if (a.length < 4) return '데이터가 더 쌓이면 분석할게요';
  const half = Math.floor(a.length / 2);
  const avg = (arr) => arr.reduce((s, d) => s + (d.totalKcal || 0), 0) / (arr.length || 1);
  const diff = Math.round(avg(a.slice(half)) - avg(a.slice(0, half)));
  if (Math.abs(diff) < 80) return '최근 섭취량이 비슷하게 유지되고 있어요';
  return diff > 0
    ? `최근 섭취량이 늘어나는 추세예요 (+${diff.toLocaleString()}kcal)`
    : `최근 섭취량이 줄어드는 추세예요 (${diff.toLocaleString()}kcal)`;
});

onMounted(async () => {
  const t = await fetchTarget();
  if (t?.targetCalories) targetKcal.value = t.targetCalories;
  applyPreset(7);
});
</script>

<style scoped>
.trend-shell {
  max-width: 760px;
  margin: 0 auto;
  background: #fff;
  border: 1px solid var(--color-tan);
  border-radius: var(--radius-large);
  padding: 22px;
}
.period-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.period-tab {
  flex: 1;
  padding: 9px 0;
  background: var(--color-tan);
  border: 2px solid transparent;
  border-radius: var(--radius-pill);
  font-family: inherit;
  font-weight: 700;
  font-size: 13px;
  color: var(--color-text-dark);
  cursor: pointer;
  transition: all 0.15s;
}
.period-tab.active {
  background: var(--color-teal);
  color: #fff;
  border-color: var(--color-teal-dark);
}
.date-range {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}
.date-range label {
  display: flex;
  flex-direction: column;
  font-size: 11px;
  color: var(--color-text-dark);
  gap: 4px;
}
.date-range input[type='date'] {
  border: 1px solid var(--color-tan);
  border-radius: 8px;
  padding: 7px 10px;
  font-family: inherit;
}
.tilde {
  padding-bottom: 8px;
  color: var(--color-text-dark);
  opacity: 0.6;
}
.btn-apply {
  background: var(--color-teal-dark);
  color: #fff;
  border: none;
  border-radius: var(--radius-pill);
  padding: 8px 18px;
  font-family: inherit;
  font-weight: 700;
  cursor: pointer;
}
.summary {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}
.summary-item {
  flex: 1;
  background: var(--color-tan);
  border-radius: 12px;
  padding: 12px;
  text-align: center;
}
.summary-label {
  display: block;
  font-size: 11px;
  color: var(--color-text-dark);
  opacity: 0.7;
  margin-bottom: 4px;
}
.summary-value {
  font-weight: 700;
  font-size: 15px;
  color: var(--color-teal-dark);
}
.summary-value.over {
  color: var(--color-red);
}
/* 분석 */
.analysis {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}
.ana {
  background: var(--color-tan);
  border-radius: 12px;
  padding: 10px 14px;
  font-size: 12px;
  color: var(--color-text-dark);
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.ana b {
  font-size: 13px;
  color: var(--color-teal-dark);
}
/* 범례 */
.legend {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  font-size: 11px;
  color: var(--color-text-dark);
  opacity: 0.8;
  margin-bottom: 8px;
}
.legend .sw {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 3px;
  vertical-align: -1px;
  margin-right: 3px;
}
.legend .sw.ok { background: var(--color-teal); border-radius: 50%; }
.legend .sw.over { background: var(--color-red); border-radius: 50%; }
.legend .sw.line { background: transparent; border-top: 2px dashed var(--color-teal-dark); border-radius: 0; height: 0; width: 14px; }
.chart-empty {
  text-align: center;
  padding: 70px 0;
  color: var(--color-text-dark);
  opacity: 0.6;
  font-size: 14px;
}
.chart-scroll {
  overflow-x: auto;
  padding-bottom: 6px;
}
.chart {
  display: flex;
  flex-direction: column;
}
/* 라인 그래프 영역(고정 높이) + 점/목표선 오버레이 */
.bars {
  position: relative;
  height: 280px;
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
  stop-color: var(--color-teal);
  stop-opacity: 0.3;
}
.line-svg .g1 {
  stop-color: var(--color-teal);
  stop-opacity: 0;
}
.t-path {
  fill: none;
  stroke: var(--color-teal);
  stroke-width: 2.5;
  stroke-linejoin: round;
  stroke-linecap: round;
  vector-effect: non-scaling-stroke;
}
.t-line {
  stroke: var(--color-teal-dark);
  stroke-width: 1.5;
  stroke-dasharray: 5 4;
  opacity: 0.7;
  vector-effect: non-scaling-stroke;
}
.dot {
  position: absolute;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--color-teal);
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px var(--color-teal);
  transform: translate(-50%, -50%);
  cursor: pointer;
}
.dot.over {
  background: var(--color-red);
  box-shadow: 0 0 0 1px var(--color-red);
}
.target-line-label {
  position: absolute;
  right: 0;
  transform: translateY(-50%);
  font-size: 10px;
  font-weight: 700;
  color: var(--color-teal-dark);
  background: #fff;
  padding: 0 4px;
  z-index: 1;
}
.xrow {
  display: flex;
  gap: 6px;
  margin-top: 6px;
}
.x-col {
  flex: 1;
  min-width: 30px;
  text-align: center;
  font-size: 10px;
  color: var(--color-text-dark);
  opacity: 0.7;
  white-space: nowrap;
}
@media (max-width: 560px) {
  .analysis {
    grid-template-columns: 1fr;
  }
}
</style>
