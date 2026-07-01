<template>
  <div class="dino-page">
    <div class="page-top">
      <div>
        <h1 class="dino-page-title">챌린지 🏆</h1>
        <p class="dino-page-subtitle">건강 목표를 위한 챌린지에 참여하세요.</p>
      </div>
      <div class="actions">
        <router-link :to="{ name: 'my-challenge' }" class="btn-ghost">내 챌린지</router-link>
        <router-link :to="{ name: 'challenge-create' }" class="btn-primary">+ 챌린지 등록 신청</router-link>
      </div>
    </div>

    <!-- 필터 · 검색 · 정렬 -->
    <div class="toolbar">
      <div class="tabs">
        <button
          v-for="t in statusTabs"
          :key="t.value"
          class="tab"
          :class="{ active: statusFilter === t.value }"
          @click="statusFilter = t.value"
        >
          {{ t.label }}
        </button>
      </div>
      <div class="tools">
        <input v-model="keyword" class="dino-input search" placeholder="챌린지 검색" />
        <select v-model="sortBy" class="dino-select sort">
          <option value="latest">최신순</option>
          <option value="popular">인기순</option>
        </select>
      </div>
    </div>

    <div class="dino-grid dino-grid-3">
      <router-link
        v-for="c in filtered"
        :key="c.cno"
        :to="{ name: 'challenge-detail', params: { cno: c.cno } }"
        class="cloud-card challenge-card"
      >
        <div class="thumb" :style="{ background: c.color }">
          {{ c.emoji }}
          <span v-if="c.joined" class="joined-badge">✓ 참여중</span>
        </div>
        <div class="c-head">
          <span class="tag-red" :class="{ done: c.status === '종료' }">{{ statusLabel(c.status) }}</span>
          <span v-if="c.dday" class="dday">{{ c.dday }}</span>
          <span class="label-caps">{{ c.period }}</span>
        </div>
        <div class="c-title">{{ c.title }}</div>
        <p class="c-desc">{{ c.desc }}</p>
        <div v-if="c.condLabel" class="c-cond">🎯 {{ c.condLabel }}</div>
        <div class="c-foot">👥 {{ c.participants }}명 참여</div>
      </router-link>
    </div>

    <div v-if="!filtered.length" class="dino-empty">
      <span class="emoji">🏆</span>
      {{ challenges.length ? '조건에 맞는 챌린지가 없어요.' : '아직 챌린지가 없어요. 첫 챌린지를 만들어보세요!' }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useFetchChallenges } from '@/composables/useChallengeApis';

// F112. 챌린지 목록 — 상태/정렬은 서버에서 처리(기본 모집중 + 인기순)
const { data, execute } = useFetchChallenges();
const challenges = computed(() => data.value ?? []);

const statusTabs = [
  { value: 'recruiting', label: '모집중' },
  { value: 'live', label: '진행중' },
  { value: 'ended', label: '종료' },
  { value: 'all', label: '전체' },
];
const statusFilter = ref('recruiting');
const sortBy = ref('popular');
const keyword = ref('');

const load = () => execute({ status: statusFilter.value, sort: sortBy.value });
watch([statusFilter, sortBy], load);
load();

// 백엔드 status: 모집중 | LIVE | 종료 → 화면엔 LIVE를 '진행중'으로
const statusLabel = (s) => (s === 'LIVE' ? '진행중' : s);

// 검색만 클라이언트(상태/정렬은 서버)
const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase();
  if (!k) return challenges.value;
  return challenges.value.filter(
    (c) => c.title?.toLowerCase().includes(k) || c.desc?.toLowerCase().includes(k)
  );
});
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
.btn-ghost,
.btn-primary {
  text-decoration: none;
}

/* 툴바 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.tabs {
  display: flex;
  gap: 6px;
}
.tab {
  border: none;
  background: #fff;
  border-radius: var(--radius-pill);
  padding: 8px 16px;
  font-family: inherit;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  color: var(--color-text-dark);
}
.tab.active {
  background: var(--color-teal);
  color: #fff;
}
.tools {
  display: flex;
  gap: 8px;
}
.search {
  width: 200px;
}
.sort {
  width: 110px;
}

.challenge-card {
  text-decoration: none;
  color: inherit;
  display: block;
}
.thumb {
  position: relative;
  height: 120px;
  border-radius: var(--radius-small);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  margin-bottom: 14px;
}
.joined-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--color-teal-dark);
  font-size: 11px;
  font-weight: 800;
  padding: 3px 9px;
  border-radius: var(--radius-pill);
}
.c-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.tag-red.done {
  background: #9aa0a6;
}
.dday {
  font-size: 11px;
  font-weight: 800;
  color: var(--color-red);
}
.c-head .label-caps {
  margin-left: auto;
}
.c-title {
  font-weight: 700;
  font-size: 18px;
}
.c-desc {
  font-size: 13px;
  color: var(--color-text-dark);
  margin: 6px 0 12px;
}
.c-cond {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-teal-dark);
  background: var(--color-beige);
  border-radius: var(--radius-pill);
  padding: 3px 10px;
  margin-bottom: 10px;
}
.c-foot {
  font-size: 12px;
  color: var(--color-tan);
  font-weight: 600;
}
</style>
