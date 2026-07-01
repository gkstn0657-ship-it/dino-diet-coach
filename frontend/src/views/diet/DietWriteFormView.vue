<template>
  <div class="dino-page narrow">
    <h1 class="dino-page-title">{{ mealLabelKo }} 기록하기 {{ mealEmoji }}</h1>
    <p class="dino-page-subtitle">{{ form.date }} · 음식을 추가하면 칼로리·영양이 자동 계산돼요</p>

    <form class="cloud-card" @submit.prevent="handleSubmit">
      <!-- 1) 사진 → AI 분석 (보조 진입점) -->
      <div class="photo-section-top">
        <!-- 업로드 전: 드롭존 / 업로드 후: 큰 미리보기 -->
        <div v-if="!preview" class="upload-drop" @click="fileInput?.click()">
          <span class="emoji">📷</span>
          <span class="up-text">사진 올리기 (선택)</span>
          <span class="up-hint">탭해서 음식 사진을 추가하세요</span>
        </div>
        <div v-else class="preview-wrap">
          <img :src="preview" alt="업로드한 사진 미리보기" class="preview-img" />
          <button type="button" class="preview-change" @click="fileInput?.click()">사진 변경</button>
        </div>

        <button
          v-if="preview"
          type="button"
          class="btn-teal photo-analyze"
          :disabled="analyzing"
          @click="analyze"
        >
          {{ analyzing ? 'AI 분석 중...' : '🦕 사진으로 음식 분석' }}
        </button>
        <input ref="fileInput" type="file" accept="image/*" hidden @change="onFile" />
      </div>
      <p v-if="analysisComment" class="analysis-line">💬 {{ analysisComment }}</p>

      <!-- 2) 음식 추가 (핵심) -->
      <div class="food-section">
        <label class="food-label">음식 추가 <span>(검색해서 선택)</span></label>
        <input
          v-model="foodKeyword"
          class="dino-input"
          placeholder="예: 닭가슴살, 현미밥"
          @input="onSearch"
        />

        <ul v-if="searchResults.length" class="food-results">
          <li v-for="f in searchResults" :key="f.fno ?? f.name" @click="addFood(f)">
            <span>{{ f.name }}</span><span class="kc">{{ f.kcal }} kcal</span>
          </li>
        </ul>

        <!-- 검색 결과 없음 → AI 영양 추정 -->
        <button
          v-if="foodKeyword.trim() && !searchResults.length && !estimating"
          type="button"
          class="ai-estimate-btn"
          @click="estimateFood"
        >
          🤖 ‘{{ foodKeyword.trim() }}’ 검색 결과가 없어요 — AI로 영양 추정하기
        </button>
        <p v-if="estimating" class="ai-estimating">AI가 1인분 영양을 추정하는 중...</p>

        <ul v-if="selectedFoods.length" class="selected-foods">
          <li v-for="(f, i) in selectedFoods" :key="i" :class="{ 'is-ai': f.source === 'AI' }">
            <div class="sf-head">
              <span class="sf-name">🍽 {{ f.name }}</span>
              <span class="src-badge" :class="srcClass(f.source)">{{ srcLabel(f.source) }}</span>
              <span v-if="f.source !== 'AI'" class="kc">{{ f.kcal }} kcal</span>
              <button type="button" class="rm" @click="removeFood(i)">✕</button>
            </div>
            <!-- AI 추정: 저장 전 직접 수정 가능 (추정치) -->
            <div v-if="f.source === 'AI'" class="ai-edit">
              <span class="ai-note">추정치 — 필요하면 수정하세요</span>
              <div class="ai-fields">
                <label>kcal<input v-model.number="f.kcal" type="number" min="0" /></label>
                <label>단백질<input v-model.number="f.protein" type="number" min="0" /></label>
                <label>탄수<input v-model.number="f.carbs" type="number" min="0" /></label>
                <label>지방<input v-model.number="f.fat" type="number" min="0" /></label>
              </div>
            </div>
          </li>
        </ul>

        <!-- 음식이 없을 때만 칼로리 직접 입력 -->
        <div v-if="!selectedFoods.length" class="kcal-direct">
          <label>음식을 못 찾았다면 칼로리만 입력</label>
          <input v-model.number="form.kcal" type="number" class="dino-input" placeholder="예: 520 (kcal)" />
        </div>

        <!-- 합계 요약 (음식 선택 시) -->
        <div v-if="selectedFoods.length" class="macro-totals">
          <span class="mt-kcal">{{ totalKcal }} kcal</span>
          <span class="mt">단백질 {{ totalP }}g</span>
          <span class="mt">탄수 {{ totalC }}g</span>
          <span class="mt">지방 {{ totalF }}g</span>
        </div>
      </div>

      <!-- 3) 보조 컨트롤: 끼니 / 날짜 / 메모 제목 (접힘) -->
      <details class="meta-controls" :open="metaOpen" @toggle="metaOpen = $event.target.open">
        <summary>
          현재 선택: <b>{{ mealLabelKo }} · {{ form.date }}</b>
          <span class="meta-hint">— 끼니·날짜·메모를 바꾸려면 펼치기</span>
        </summary>
        <div class="meta-grid">
          <div class="dino-field">
            <label>끼니</label>
            <select ref="mealSelect" v-model="form.meal" class="dino-select">
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
            <input v-model="form.title" class="dino-input" placeholder="비우면 자동으로 만들어드려요" />
          </div>
        </div>
      </details>

      <div class="form-actions">
        <router-link :to="{ name: 'diet-list' }" class="btn-ghost">취소</router-link>
        <button type="submit" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? '저장 중...' : `${mealLabelKo} 기록 저장` }}
        </button>
      </div>
    </form>

    <!-- 같은 끼니 중복 안내 -->
    <div v-if="dup" class="dup-overlay" @click.self="dup = null">
      <div class="dup-card">
        <div class="dup-emoji">🦕</div>
        <p class="dup-msg">{{ dup.message }}</p>
        <div class="dup-actions">
          <button class="btn-primary block" @click="goEdit">기존 기록 수정하기</button>
          <button class="btn-teal block" @click="openMealPicker">다른 끼니로 기록하기</button>
          <button class="btn-ghost block" @click="dup = null">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWriteDiet, useSearchFood } from '@/composables/useDietApis';
import { useAnalyzePhoto, useEstimateNutrition } from '@/composables/useAiApis';

const route = useRoute();
const router = useRouter();

const MEAL_KO = {
  breakfast: ['아침', '🥣'],
  lunch: ['점심', '🥗'],
  dinner: ['저녁', '🍗'],
  snack: ['간식', '🍎'],
};

const pad = (n) => String(n).padStart(2, '0');
const todayStr = () => {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};
// 현재 시간 기준 기본 끼니
const defaultMeal = () => {
  const h = new Date().getHours();
  if (h >= 5 && h < 11) return 'breakfast';
  if (h >= 11 && h < 16) return 'lunch';
  if (h >= 16 && h < 21) return 'dinner';
  return 'snack';
};

const fileInput = ref(null);
const preview = ref('');
const analyzing = ref(false);
const analysisComment = ref('');

// 날짜=오늘, 끼니=현재 시간 기준 자동 (슬롯 CTA 로 들어오면 query 값 우선)
const form = ref({
  title: '',
  meal: MEAL_KO[route.query.meal] ? route.query.meal : defaultMeal(),
  date: route.query.date || todayStr(),
  kcal: null,
  photo: null,
});

const mealLabelKo = computed(() => (MEAL_KO[form.value.meal] ?? ['식단', '🍽'])[0]);
const mealEmoji = computed(() => (MEAL_KO[form.value.meal] ?? ['식단', '🍽'])[1]);

// 끼니/날짜/메모 변경 영역(접힘) 제어 — 코드에서 펼칠 수 있게 상태 연결
const metaOpen = ref(false);
const mealSelect = ref(null);
/** 중복 모달의 '다른 끼니로 기록하기' → 모달 닫고 변경 영역을 펼친 뒤 끼니 select 로 포커스 이동 */
const openMealPicker = async () => {
  dup.value = null;
  metaOpen.value = true;
  await nextTick();
  mealSelect.value?.focus();
  mealSelect.value?.scrollIntoView({ behavior: 'smooth', block: 'center' });
};

const onFile = (e) => {
  const file = e.target.files[0];
  if (!file) return;
  form.value.photo = file;
  preview.value = URL.createObjectURL(file);
};

// 사진 → 비전 AI 분석 → 음식 목록 자동 채움
const { execute: analyzePhotoApi } = useAnalyzePhoto();
const analyze = async () => {
  if (!form.value.photo) return;
  analyzing.value = true;
  try {
    const result = await analyzePhotoApi(form.value.photo);
    if (result) {
      if (result.title && !form.value.title) form.value.title = result.title;
      if (Array.isArray(result.foods) && result.foods.length) {
        // 사진 분석 결과는 DB에 없어도 선택 목록에 추가(저장 가능)
        result.foods.forEach((f) => {
          selectedFoods.value.push({
            name: f.name,
            kcal: f.kcal || 0,
            protein: f.protein || 0,
            carbs: f.carbs || 0,
            fat: f.fat || 0,
            source: 'PHOTO',
          });
        });
      }
      analysisComment.value = result.comment || '';
    } else {
      analysisComment.value = '분석에 실패했어요. 다시 시도해 주세요.';
    }
  } finally {
    analyzing.value = false;
  }
};

// 음식 DB 검색·선택
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

const selectedFoods = ref([]);
const addFood = (f) => {
  selectedFoods.value.push({
    fno: f.fno,
    name: f.name,
    kcal: f.kcal || 0,
    protein: f.protein || 0,
    carbs: f.carbs || 0,
    fat: f.fat || 0,
    source: 'DB',
  });
  foodKeyword.value = '';
  searchData.value = null;
};
const removeFood = (i) => selectedFoods.value.splice(i, 1);

// 검색 결과가 없을 때: 입력한 음식명으로 AI 영양 추정 → 선택 목록에 추가(저장 전 수정 가능)
const { execute: estimateApi, isLoading: estimating } = useEstimateNutrition();
const estimateFood = async () => {
  const name = foodKeyword.value.trim();
  if (!name || estimating.value) return;
  const r = await estimateApi(name);
  if (r) {
    selectedFoods.value.push({
      name: r.name || name,
      kcal: r.kcal || 0,
      protein: r.protein || 0,
      carbs: r.carbs || 0,
      fat: r.fat || 0,
      source: 'AI',
    });
    foodKeyword.value = '';
    searchData.value = null;
  }
};

// 음식 출처 배지
const SRC = { DB: '식품DB', AI: 'AI추정', PHOTO: '사진분석' };
const srcLabel = (s) => SRC[s] || '식품DB';
const srcClass = (s) => (s === 'AI' ? 'src-ai' : s === 'PHOTO' ? 'src-photo' : 'src-db');

const totalKcal = computed(() => selectedFoods.value.reduce((s, f) => s + (f.kcal || 0), 0));
const totalP = computed(() => selectedFoods.value.reduce((s, f) => s + (f.protein || 0), 0));
const totalC = computed(() => selectedFoods.value.reduce((s, f) => s + (f.carbs || 0), 0));
const totalF = computed(() => selectedFoods.value.reduce((s, f) => s + (f.fat || 0), 0));

// 저장 — 선택 음식이 있으면 foods, 없으면 kcal. 제목은 비우면 백엔드가 자동 생성
const { execute, isLoading, error: writeError } = useWriteDiet();
const dup = ref(null);
const handleSubmit = async () => {
  dup.value = null;
  const fd = new FormData();
  if (form.value.title?.trim()) fd.append('title', form.value.title.trim());
  fd.append('meal', form.value.meal);
  if (form.value.date) fd.append('date', form.value.date);
  if (selectedFoods.value.length) {
    fd.append('foods', JSON.stringify(selectedFoods.value));
  } else if (form.value.kcal != null) {
    fd.append('kcal', form.value.kcal);
  }
  if (form.value.photo) fd.append('photo', form.value.photo);

  await execute(fd); // 성공 시 composable 이 목록으로 이동
  const resp = writeError.value?.response;
  if (resp?.status === 409) {
    dup.value = { message: resp.data.message, dno: resp.data.payload };
  }
};

const goEdit = () => {
  if (dup.value?.dno) router.push({ name: 'diet-modify', params: { dno: dup.value.dno } });
};
</script>

<style scoped>
.narrow {
  max-width: 720px;
}

/* 사진 + 분석 (위쪽 큰 미리보기) */
.photo-section-top {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
/* 업로드 전 드롭존 */
.upload-drop {
  width: 100%;
  min-height: 130px;
  border: 2px dashed var(--color-tan);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--text-muted);
  cursor: pointer;
  font-size: var(--fs-body-sm);
}
.upload-drop .emoji {
  font-size: 40px;
}
.upload-drop .up-text {
  font-weight: 700;
  color: var(--text);
}
.upload-drop .up-hint {
  font-size: var(--fs-label);
}
/* 업로드 후 큰 미리보기 (잘림 없이 전체가 보이도록 contain) */
.preview-wrap {
  position: relative;
  width: 100%;
}
.preview-img {
  width: 100%;
  max-height: 340px;
  object-fit: contain;
  background: var(--surface-muted);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-md);
  display: block;
}
.preview-change {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border: none;
  border-radius: var(--radius-pill);
  padding: 6px 12px;
  font-family: inherit;
  font-size: var(--fs-label);
  font-weight: 700;
  cursor: pointer;
}
.photo-analyze {
  width: 100%;
}
.analysis-line {
  margin: var(--space-3) 0 0;
  font-size: var(--fs-body-sm);
  color: var(--accent-dark);
  font-weight: 600;
}

/* 음식 추가 (핵심) */
.food-section {
  margin-top: var(--space-5);
}
.food-label {
  display: block;
  font-size: var(--fs-body);
  font-weight: 700;
  margin-bottom: var(--space-2);
  color: var(--text);
}
.food-label span {
  color: var(--text-muted);
  font-weight: 500;
  font-size: var(--fs-body-sm);
}
.food-results {
  list-style: none;
  margin: var(--space-1) 0 0;
  padding: 0;
  border: 2px solid var(--surface-muted);
  border-radius: var(--radius-md);
  max-height: 200px;
  overflow-y: auto;
}
.food-results li {
  display: flex;
  justify-content: space-between;
  padding: 10px 14px;
  cursor: pointer;
  font-size: var(--fs-body);
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
  padding: 10px 14px;
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-2);
  font-size: var(--fs-body);
}
.selected-foods li.is-ai {
  border: 1px solid #d8c7f5;
  background: #f7f2ff;
}
.sf-head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.sf-name {
  font-weight: 600;
}
.selected-foods .kc {
  color: var(--accent-dark);
  font-weight: 700;
}
.selected-foods .rm {
  border: none;
  background: none;
  color: var(--danger);
  cursor: pointer;
  font-weight: 800;
}
.src-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  margin-right: auto;
}
.src-db {
  background: var(--surface);
  color: var(--text-muted);
  border: 1px solid var(--border-soft);
}
.src-ai {
  background: #efe7fb;
  color: #6b46c1;
}
.src-photo {
  background: #e7f3fb;
  color: #1d6fa5;
}
.ai-edit {
  margin-top: 8px;
}
.ai-note {
  font-size: 11px;
  color: #6b46c1;
  font-weight: 600;
}
.ai-fields {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  flex-wrap: wrap;
}
.ai-fields label {
  display: flex;
  flex-direction: column;
  font-size: 10px;
  color: var(--text-muted);
  gap: 2px;
}
.ai-fields input {
  width: 64px;
  padding: 5px 8px;
  border: 1px solid var(--border-soft);
  border-radius: 8px;
  font-family: inherit;
}
.ai-estimate-btn {
  margin-top: var(--space-2);
  width: 100%;
  padding: 10px 14px;
  background: #f7f2ff;
  border: 1px solid #d8c7f5;
  color: #6b46c1;
  border-radius: var(--radius-md);
  font-family: inherit;
  font-weight: 700;
  font-size: var(--fs-body-sm);
  cursor: pointer;
}
.ai-estimate-btn:hover {
  background: #efe7fb;
}
.ai-estimating {
  margin-top: var(--space-2);
  font-size: var(--fs-body-sm);
  color: #6b46c1;
  font-weight: 600;
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

/* 보조 컨트롤 (접힘) */
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

/* 중복 안내 모달 */
.dup-overlay {
  position: fixed;
  inset: 0;
  background: rgba(80, 61, 80, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: var(--space-4);
}
.dup-card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  max-width: 360px;
  width: 100%;
  text-align: center;
  box-shadow: var(--shadow-elevated);
}
.dup-emoji {
  font-size: 40px;
}
.dup-msg {
  margin: var(--space-3) 0 var(--space-5);
  font-size: var(--fs-body);
  line-height: 1.5;
  color: var(--text);
  font-weight: 600;
}
.dup-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.dup-actions .block {
  display: block;
  width: 100%;
  text-align: center;
}

@media (max-width: 560px) {
  .meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
