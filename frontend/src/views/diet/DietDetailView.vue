<template>
  <div class="dino-page">
    <div class="page-top">
      <div>
        <h1 class="dino-page-title">{{ diet.title }}</h1>
        <p class="dino-page-subtitle">{{ diet.date }} · {{ diet.mealLabel }}</p>
      </div>
      <div class="actions">
        <router-link :to="{ name: 'diet-modify', params: { dno } }" class="btn-ghost"
          >수정</router-link
        >
        <button class="btn-primary" @click="handleDelete">삭제</button>
      </div>
    </div>

    <div class="dino-grid dino-grid-2">
      <!-- 음식 구성 -->
      <div class="cloud-card">
        <div class="diet-photo">📷 {{ diet.title }}</div>
        <div class="label-caps" style="margin: 16px 0 8px">음식 구성</div>
        <ul class="food-list">
          <li v-for="f in diet.foods" :key="f.name" class="food-item">
            <span>{{ f.name }}</span>
            <span class="food-kcal">{{ f.kcal }} kcal</span>
          </li>
        </ul>
      </div>

      <!-- 영양 정보 + 분석 (F105) -->
      <div class="cloud-card">
        <div class="label-caps">영양 분석</div>
        <div class="stat-large">
          <span>{{ diet.kcal }}</span>
          <span class="label-caps">kcal</span>
        </div>
        <div class="macro-rows">
          <div v-for="m in diet.macros" :key="m.label" class="macro-row">
            <span>{{ m.label }}</span>
            <div class="bar"><div class="bar-fill" :style="{ width: m.pct + '%' }" /></div>
            <span class="macro-val">{{ m.value }}g</span>
          </div>
        </div>
        <router-link :to="{ name: 'ai-diet-analysis' }" class="btn-teal block"
          >AI 종합 분석 보기</router-link
        >
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useFetchDietDetail, useDeleteDiet } from '@/composables/useDietApis';

const route = useRoute();
const dno = computed(() => route.params.dno);

// F102. 식단 상세 (실제 로드)
const { data, execute } = useFetchDietDetail();
const diet = computed(() => data.value ?? { foods: [], macros: [] });

const { execute: deleteDiet } = useDeleteDiet();
const handleDelete = () => {
  if (confirm('이 식단 기록을 삭제할까요?')) deleteDiet(dno.value);
};

watch(dno, (v) => v && execute(v), { immediate: true });
</script>

<style scoped>
.page-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
}
.actions {
  display: flex;
  gap: 10px;
}
.diet-photo {
  height: 200px;
  background: var(--color-teal);
  border-radius: var(--radius-small);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
}
.food-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.food-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 2px dashed var(--color-beige);
  font-size: 14px;
}
.food-item:last-child {
  border: none;
}
.food-kcal {
  color: var(--color-teal);
  font-weight: 600;
}
.macro-rows {
  margin: 16px 0;
}
.macro-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
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
.macro-val {
  width: 40px;
  text-align: right;
  font-weight: 600;
}
.block {
  display: block;
  text-align: center;
  text-decoration: none;
  margin-top: 8px;
}
</style>
