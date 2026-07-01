<template>
  <div>
    <header class="ops-head">
      <h1 class="ops-h1">대시보드</h1>
      <p class="ops-desc">서비스 주요 지표를 한눈에 확인합니다.</p>
    </header>

    <div class="stat-grid">
      <div v-for="s in stats" :key="s.label" class="cloud-card stat">
        <div class="stat-ic" :style="{ background: s.bg }">{{ s.icon }}</div>
        <div class="stat-body">
          <div class="stat-num">{{ s.value.toLocaleString() }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <p v-if="error" class="ops-error">지표를 불러오지 못했습니다. 권한 또는 네트워크를 확인해주세요.</p>

    <!-- 운영 액션: 식품 DB 최신화 -->
    <div class="cloud-card action-card">
      <div class="action-info">
        <div class="action-title">🥗 식품 DB 최신화</div>
        <p class="action-desc">
          식약처 식품영양성분 API에서 대표 음식 데이터를 받아 로컬 음식 DB를 갱신합니다.
          (정기 갱신은 매월 1일 0시에 자동 실행돼요.)
        </p>
        <p v-if="syncResult" class="action-result" :class="{ warn: syncResult.configured === false }">
          {{ syncResultText }}
        </p>
      </div>
      <button class="btn-primary sync-btn" :disabled="syncing" @click="onSync">
        {{ syncing ? '갱신 중…' : '지금 최신화' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useOpsSummary, useSyncFoods } from '@/composables/useOpsApis';

const { data, error, execute } = useOpsSummary();
execute();

const summary = computed(() => data.value ?? {});
const stats = computed(() => [
  { label: '전체 회원', value: summary.value.memberCount ?? 0, icon: '👥', bg: 'rgba(119,192,179,0.18)' },
  { label: '전체 식단 기록', value: summary.value.dietCount ?? 0, icon: '🍽', bg: 'rgba(217,195,163,0.30)' },
  { label: '진행 중 챌린지', value: summary.value.liveChallengeCount ?? 0, icon: '🏆', bg: 'rgba(216,61,82,0.12)' },
  { label: '전체 게시글', value: summary.value.postCount ?? 0, icon: '💬', bg: 'rgba(119,192,179,0.18)' },
]);

// 식품 DB 최신화
const { execute: syncFoods, isLoading: syncing } = useSyncFoods();
const syncResult = ref(null);
const syncResultText = computed(() => {
  const r = syncResult.value;
  if (!r) return '';
  if (r.configured === false) {
    return '식약처 API 키가 설정되지 않아 갱신을 건너뛰었어요. (로컬 데이터는 유지됩니다)';
  }
  return `갱신 완료 · 추가 ${r.inserted ?? 0} · 수정 ${r.updated ?? 0}` +
    (r.failedKeywords ? ` · 실패 키워드 ${r.failedKeywords}` : '');
});
const onSync = async () => {
  if (syncing.value) return;
  syncResult.value = null;
  const res = await syncFoods();
  if (res) syncResult.value = res.payload ?? {};
};
</script>

<style scoped>
.ops-head {
  margin-bottom: var(--space-5);
}
.ops-h1 {
  font-size: var(--fs-section-title);
  font-weight: 800;
  color: var(--text-strong);
  margin: 0 0 var(--space-1);
}
.ops-desc {
  margin: 0;
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}
.stat {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-5);
  display: flex;
  align-items: center;
  gap: var(--space-4);
}
.stat-ic {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.stat-num {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-strong);
  line-height: 1.1;
}
.stat-label {
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
  font-weight: 600;
}
.ops-error {
  margin-top: var(--space-4);
  color: var(--danger);
  font-size: var(--fs-body-sm);
  font-weight: 600;
}
.action-card {
  margin-top: var(--space-5);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-5);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
}
.action-title {
  font-size: var(--fs-card-title);
  font-weight: 800;
  color: var(--text-strong);
  margin-bottom: var(--space-1);
}
.action-desc {
  margin: 0;
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
  line-height: 1.5;
}
.action-result {
  margin: var(--space-2) 0 0;
  font-size: var(--fs-body-sm);
  font-weight: 700;
  color: var(--accent-dark);
}
.action-result.warn {
  color: #9a6b16;
}
.sync-btn {
  flex-shrink: 0;
  white-space: nowrap;
}

@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 560px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
