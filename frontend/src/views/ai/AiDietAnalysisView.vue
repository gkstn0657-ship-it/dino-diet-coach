<template>
  <div class="dino-page">
    <h1 class="dino-page-title">AI 식단 종합 분석 🦕</h1>
    <p class="dino-page-subtitle">생성형 AI가 내 식단을 분석해 점수와 피드백을 제공합니다.</p>

    <!-- 분석 범위 선택 -->
    <div class="period-tabs">
      <button :class="{ active: period === 'today' }" @click="run('today')">오늘</button>
      <button :class="{ active: period === 'week' }" @click="run('week')">최근 7일</button>
      <span v-if="analysis" class="basis">
        분석 근거: {{ analysis.period }} 기록 {{ analysis.mealCount }}끼 · 하루 평균 {{ analysis.avgKcal }} kcal
      </span>
    </div>

    <div v-if="isLoading" class="dino-empty"><span class="emoji">🦕</span>분석 중...</div>

    <template v-else-if="analysis && analysis.mealCount > 0">
      <div class="dino-grid dino-grid-3">
        <div class="cloud-card score-card">
          <div class="label-caps">{{ analysis.period }} 식단 점수</div>
          <div class="score" :class="{ low: analysis.score < 50 }">{{ analysis.score }}<span>/100</span></div>
          <div class="tag-teal" :class="{ low: analysis.grade === 'LOW' }">{{ analysis.grade }}</div>
          <div v-if="analysis.lowData" class="low-note">⚠️ 기록된 끼니가 적어 평가 신뢰도가 낮아요</div>
        </div>
        <div class="cloud-card">
          <div class="label-caps">영양 밸런스 (하루 평균 기준)</div>
          <div v-for="m in analysis.macros" :key="m.label" class="macro-row">
            <span>{{ m.label }}</span>
            <div class="bar"><div class="bar-fill" :style="{ width: m.pct + '%' }" /></div>
            <span>{{ m.pct }}%</span>
          </div>
        </div>
        <div class="cloud-card coach-summary">
          <div class="coach-avatar">🦕</div>
          <p>{{ analysis.summary }}</p>
        </div>
      </div>

      <div class="cloud-card" style="margin-top: 20px">
        <div class="label-caps">AI 상세 피드백</div>
        <ul class="feedback-list">
          <li v-for="(f, i) in analysis.feedback" :key="i" :class="{ warn: !f.ok }">
            {{ f.ok ? '✅' : '⚠️' }} {{ f.text }}
          </li>
        </ul>
      </div>
    </template>

    <div v-else class="dino-empty">
      <span class="emoji">🥗</span>
      {{ analysis ? analysis.summary : '범위를 선택하면 분석을 시작합니다.' }}
      <router-link :to="{ name: 'diet-write' }" style="display: block; margin-top: 8px"
        >식단 기록하러 가기 →</router-link
      >
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAiDietAnalysis } from '@/composables/useAiApis';

// F116. AI 식단 분석 (실제 연동, 범위 선택)
const { execute, isLoading } = useAiDietAnalysis();
const analysis = ref(null);
const period = ref('today');

const run = async (p) => {
  period.value = p;
  const result = await execute({ period: p });
  if (result) analysis.value = result;
};

// 진입 시 오늘 기준 자동 분석
run('today');
</script>

<style scoped>
.period-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
}
.period-tabs button {
  border: none;
  background: var(--color-white);
  color: var(--color-tan);
  padding: 8px 18px;
  border-radius: var(--radius-pill);
  font-family: inherit;
  font-weight: 600;
  cursor: pointer;
}
.period-tabs button.active {
  background: var(--color-teal);
  color: #fff;
}
.period-tabs .basis {
  margin-left: 8px;
  font-size: 12px;
  color: var(--color-tan);
}
.score-card {
  text-align: center;
}
.score {
  font-size: 56px;
  font-weight: 700;
  color: var(--color-teal);
  margin: 8px 0;
}
.score span {
  font-size: 20px;
  color: var(--color-tan);
}
.score.low {
  color: var(--color-red);
}
.tag-teal.low {
  background: var(--color-red);
}
.low-note {
  margin-top: 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-red);
}
.macro-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px 0;
  font-size: 13px;
}
.macro-row > span:first-child {
  width: 64px;
  font-weight: 600;
}
.bar {
  flex: 1;
  height: 10px;
  background: var(--color-beige);
  border-radius: var(--radius-pill);
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: var(--color-teal);
}
.coach-summary {
  background: var(--color-teal);
  color: #fff;
  text-align: center;
}
.coach-summary .coach-avatar {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  background: #fff;
  border-radius: 50%;
  border: 4px solid var(--color-tan);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}
.coach-summary p {
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}
.feedback-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.feedback-list li {
  padding: 10px 0;
  border-bottom: 2px dashed var(--color-beige);
  font-size: 14px;
}
.feedback-list li:last-child {
  border: none;
}
.feedback-list li.warn {
  color: #b3261e;
}
</style>
