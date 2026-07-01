<template>
  <div class="dino-page narrow">
    <h1 class="dino-page-title">{{ mealLabelKo }} 기록 수정 ✏️</h1>
    <p class="dino-page-subtitle">{{ form.date }} · 음식을 추가하거나 빼서 끼니를 정리하세요.</p>

    <form class="cloud-card" @submit.prevent="handleSubmit">
      <!-- 음식 구성 (핵심) -->
      <div class="food-section">
        <label class="food-label">음식 구성 <span>(검색해서 추가, ✕로 제거)</span></label>
        <input
          v-model="foodKeyword"
          class="dino-input"
          placeholder="추가할 음식 검색 — 예: 닭가슴살, 현미밥"
          @input="onSearch"
        />

        <ul v-if="searchResults.length" class="food-results">
          <li v-for="f in searchResults" :key="f.fno ?? f.name" @click="addFood(f)">
            <span>{{ f.name }}</span><span class="kc">{{ f.kcal }} kcal</span>
          </li>
        </ul>

        <ul v-if="selectedFoods.length" class="selected-foods">
          <li v-for="(f, i) in selectedFoods" :key="i">
            <span>🍽 {{ f.name }}</span>
            <span class="kc">{{ f.kcal }} kcal</span>
            <button type="button" class="rm" @click="removeFood(i)">✕</button>
          </li>
        </ul>

        <div v-if="!selectedFoods.length" class="kcal-direct">
          <label>음식 없이 칼로리만 입력</label>
          <input v-model.number="form.kcal" type="number" class="dino-input" placeholder="예: 520 (kcal)" />
        </div>

        <div v-if="selectedFoods.length" class="macro-totals">
          <span class="mt-kcal">{{ totalKcal }} kcal</span>
          <span class="mt">단백질 {{ totalP }}g</span>
          <span class="mt">탄수 {{ totalC }}g</span>
          <span class="mt">지방 {{ totalF }}g</span>
        </div>
      </div>

      <!-- 보조 컨트롤 -->
      <details class="meta-controls" :open="metaOpen" @toggle="metaOpen = $event.target.open">
        <summary>
          현재 선택: <b>{{ mealLabelKo }} · {{ form.date }}</b>
          <span class="meta-hint">— 끼니·날짜·메모를 바꾸려면 펼치기</span>
        </summary>
        <div class="meta-grid">
          <div class="dino-field">
            <label>끼니</label>
            <select v-model="form.meal" class="dino-select">
              <option value="breakfast">아침</option>
              <option value="lunch">점심</option>
              <option value="dinner">저녁</option>
              <option value="snack">간식</option>
            </select>
          </div>
          <div class="dino-field">
            <label>날짜</label>
            <input v-model="form.date" type="date" class="dino-input" />
          </div>
          <div class="dino-field meta-full">
            <label>메모 제목 (선택)</label>
            <input v-model="form.title" class="dino-input" placeholder="비우면 음식 기준으로 자동 생성돼요" />
          </div>
        </div>
      </details>

      <div class="form-actions">
        <router-link :to="{ name: 'diet-detail', params: { dno } }" class="btn-ghost">취소</router-link>
        <button type="submit" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? '저장 중...' : `${mealLabelKo} 기록 수정` }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useFetchDietDetail, useModifyDiet, useSearchFood } from '@/composables/useDietApis';

const route = useRoute();
const dno = computed(() => route.params.dno);

const MEAL_KO = { breakfast: '아침', lunch: '점심', dinner: '저녁', snack: '간식' };
const mealLabelKo = computed(() => MEAL_KO[form.value.meal] ?? '식단');

const form = ref({ title: '', meal: 'lunch', date: '', kcal: null });
const selectedFoods = ref([]);
// 끼니/날짜/메모 변경 영역(접힘) 제어 — 등록 화면과 UX 일관성
const metaOpen = ref(false);

// 기존 식단 로드 — 기본 정보 + 음식 구성(영양 포함)
const { execute: fetchDetail } = useFetchDietDetail();
watch(
  dno,
  async (v) => {
    if (!v) return;
    const detail = await fetchDetail(v);
    if (detail) {
      form.value = {
        title: detail.title ?? '',
        meal: detail.meal ?? 'lunch',
        date: detail.date ?? '',
        kcal: detail.kcal ?? null,
      };
      selectedFoods.value = (detail.foods ?? []).map((f) => ({
        name: f.name,
        kcal: f.kcal || 0,
        protein: f.protein || 0,
        carbs: f.carbs || 0,
        fat: f.fat || 0,
      }));
    }
  },
  { immediate: true }
);

// 음식 DB 검색·추가
const foodKeyword = ref('');
const { data: searchData, execute: searchFood } = useSearchFood();
const searchResults = computed(() => (foodKeyword.value.trim() ? (searchData.value ?? []) : []));
let timer;
const onSearch = () => {
  clearTimeout(timer);
  timer = setTimeout(() => {
    if (foodKeyword.value.trim()) searchFood(foodKeyword.value);
  }, 300);
};

const addFood = (f) => {
  selectedFoods.value.push({
    fno: f.fno,
    name: f.name,
    kcal: f.kcal || 0,
    protein: f.protein || 0,
    carbs: f.carbs || 0,
    fat: f.fat || 0,
  });
  foodKeyword.value = '';
  searchData.value = null;
};
const removeFood = (i) => selectedFoods.value.splice(i, 1);

const totalKcal = computed(() => selectedFoods.value.reduce((s, f) => s + (f.kcal || 0), 0));
const totalP = computed(() => selectedFoods.value.reduce((s, f) => s + (f.protein || 0), 0));
const totalC = computed(() => selectedFoods.value.reduce((s, f) => s + (f.carbs || 0), 0));
const totalF = computed(() => selectedFoods.value.reduce((s, f) => s + (f.fat || 0), 0));

// 수정 저장 — 음식 목록 포함 (백엔드가 구성 교체 + 영양 합산 + 제목 자동 생성)
const { execute, isLoading } = useModifyDiet();
const handleSubmit = () => execute(dno.value, { ...form.value, foods: selectedFoods.value });
</script>

<style scoped>
.narrow {
  max-width: 720px;
}

.food-section {
  margin-top: var(--space-1);
}
.food-label {
  display: block;
  font-weight: 700;
  font-size: var(--fs-body);
  margin-bottom: var(--space-2);
  color: var(--text);
}
.food-label span {
  font-weight: 500;
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
}
.food-results {
  list-style: none;
  margin: var(--space-1) 0 0;
  padding: 0;
  border: 2px solid var(--surface-muted);
  border-radius: var(--radius-md);
  max-height: 200px;
  overflow-y: auto;
  background: #fff;
}
.food-results li {
  display: flex;
  justify-content: space-between;
  padding: 10px 14px;
  font-size: var(--fs-body);
  cursor: pointer;
  border-bottom: 1px dashed var(--surface-muted);
}
.food-results li:hover {
  background: var(--surface-muted);
}
.food-results .kc {
  color: var(--accent-dark);
  font-weight: 600;
}
.selected-foods {
  list-style: none;
  margin: var(--space-3) 0 0;
  padding: 0;
}
.selected-foods li {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 10px 14px;
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-2);
  font-size: var(--fs-body);
}
.selected-foods .kc {
  margin-left: auto;
  font-weight: 700;
  color: var(--accent-dark);
}
.selected-foods .rm {
  border: none;
  background: none;
  color: var(--danger);
  font-weight: 800;
  cursor: pointer;
}
.kcal-direct {
  margin-top: var(--space-3);
}
.kcal-direct label {
  display: block;
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
  margin-bottom: var(--space-2);
  font-weight: 600;
}
.macro-totals {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  font-size: var(--fs-body-sm);
  font-weight: 600;
  color: var(--text-muted);
}
.macro-totals .mt-kcal {
  font-size: var(--fs-card-title);
  font-weight: 800;
  color: var(--accent-dark);
  margin-right: auto;
}

.meta-controls {
  margin-top: var(--space-5);
  border-top: 1px solid var(--surface-muted);
  padding-top: var(--space-4);
}
.meta-controls summary {
  cursor: pointer;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  background: var(--surface-muted);
  border: 1px solid var(--border-soft);
  border-left: 4px solid var(--accent);
  border-radius: var(--radius-md);
  padding: 10px 14px;
  font-size: var(--fs-body-sm);
  color: var(--text);
  font-weight: 700;
}
.meta-controls summary b {
  color: var(--accent-dark);
}
.meta-hint {
  color: var(--text-muted);
  font-weight: 500;
}
.meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
  margin-top: var(--space-4);
}
.meta-full {
  grid-column: 1 / -1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-5);
}
.btn-ghost {
  text-decoration: none;
}

@media (max-width: 560px) {
  .meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
