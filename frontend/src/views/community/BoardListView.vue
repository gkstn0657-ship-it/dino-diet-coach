<template>
  <div class="dino-page">
    <div class="page-top">
      <div>
        <h1 class="dino-page-title">커뮤니티 💬</h1>
        <p class="dino-page-subtitle">식단 리뷰·칼럼·자유 게시판에서 정보를 나눠요.</p>
      </div>
      <router-link :to="{ name: 'post-write' }" class="btn-primary">+ 글쓰기</router-link>
    </div>

    <!-- 오늘 인기글 TOP 3 -->
    <div class="cloud-card today-top">
      <div class="tt-head">
        <span class="tt-title">🔥 오늘 인기글</span>
        <span class="tt-sub">오늘 가장 반응이 좋은 글이에요</span>
      </div>
      <ol v-if="topPosts.length" class="tt-list">
        <li v-for="(p, i) in topPosts" :key="p.bno">
          <router-link :to="{ name: 'post-detail', params: { bno: p.bno } }" class="tt-item">
            <span class="tt-rank" :class="'r' + (i + 1)">{{ i + 1 }}</span>
            <span class="tt-name">{{ p.title }}</span>
            <span class="tt-meta">❤ {{ p.likes }} · 💬 {{ p.comments }}</span>
          </router-link>
        </li>
      </ol>
      <p v-else class="tt-empty">아직 오늘 작성된 글이 없어요. 첫 글을 남겨보세요! ✍️</p>
    </div>

    <!-- 탭 -->
    <div class="tabs">
      <button
        v-for="t in tabs"
        :key="t.value"
        class="tab"
        :class="{ active: board === t.value }"
        @click="changeBoard(t.value)"
      >
        {{ t.label }}
      </button>
    </div>

    <!-- 게시글 목록 -->
    <div class="cloud-card list">
      <router-link
        v-for="p in posts"
        :key="p.bno"
        :to="{ name: 'post-detail', params: { bno: p.bno } }"
        class="post-row"
      >
        <span class="tag-teal">{{ p.boardLabel }}</span>
        <span class="p-title">{{ p.title }}</span>
        <span class="p-meta">
          <span class="author-link" @click.stop.prevent="goUser(p.authorEmail)">{{ p.author }}</span>
          · 💬 {{ p.comments }} · {{ p.liked ? '❤' : '🤍' }} {{ p.likes }}
        </span>
      </router-link>
      <div v-if="!posts.length" class="dino-empty" style="padding: 24px">
        이 게시판에 글이 아직 없어요.
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="pageInfo && pageInfo.totalPages > 1" class="pager">
      <AppPagination :page="navPage" @changePage="loadPosts" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useFetchPosts, useTopTodayPosts } from '@/composables/useCommunityApis';
import AppPagination from '@/components/common/AppPagination.vue';

const tabs = [
  { value: 'review', label: '식단 리뷰' },
  { value: 'expert', label: '칼럼' },
  { value: 'free', label: '자유' },
];
const board = ref('review');

// 작성자 닉네임 클릭 → 사용자 프로필 (행 링크 전파 차단)
const router = useRouter();
const goUser = (email) => {
  if (email) router.push({ name: 'user-profile', params: { email } });
};

// 오늘 인기글 TOP 3 (탭/페이지와 무관, 진입 시 1회 조회)
const { data: topData, execute: fetchTop } = useTopTodayPosts();
const topPosts = computed(() => topData.value ?? []);
fetchTop();

// F114. 게시글 목록 (페이지 단위). payload = { posts, page }
const { data, execute, isLoading } = useFetchPosts();
const posts = computed(() => data.value?.posts ?? []);
const pageInfo = computed(() => data.value?.page ?? null);
// AppPagination 은 active 표시에 page.condition.currentPage 를 사용하므로 맞춰서 전달
const navPage = computed(() =>
  pageInfo.value ? { ...pageInfo.value, condition: { currentPage: pageInfo.value.currentPage } } : null
);

// 특정 페이지 로드 (중복 클릭 방어 + 상단 스크롤)
const loadPosts = (page = 1) => {
  if (isLoading.value) return;
  execute({ board: board.value, currentPage: page });
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// 탭 변경 시 항상 1페이지부터
const changeBoard = (value) => {
  if (board.value === value) return;
  board.value = value;
};
watch(board, () => loadPosts(1), { immediate: true });
</script>

<style scoped>
.page-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16px;
}
.btn-primary {
  text-decoration: none;
}
/* 오늘 인기글 TOP 3 */
.today-top {
  padding: 16px 20px;
  margin-bottom: 16px;
}
.tt-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 10px;
}
.tt-title {
  font-weight: 800;
  color: var(--color-text-dark);
}
.tt-sub {
  font-size: 12px;
  color: var(--color-tan);
}
.tt-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.tt-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 6px;
  border-radius: var(--radius-small);
  text-decoration: none;
  color: inherit;
}
.tt-item:hover {
  background: var(--color-beige);
}
.tt-rank {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  color: #fff;
  background: var(--color-tan);
}
.tt-rank.r1 {
  background: var(--color-red);
}
.tt-rank.r2 {
  background: var(--color-teal);
}
.tt-rank.r3 {
  background: var(--color-teal-dark);
}
.tt-name {
  flex: 1;
  font-weight: 600;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tt-meta {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--color-tan);
}
.tt-empty {
  font-size: 13px;
  color: var(--color-tan);
  margin: 4px 0;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.tab {
  border: none;
  background: var(--color-white);
  color: var(--color-tan);
  padding: 8px 20px;
  border-radius: var(--radius-pill);
  font-family: inherit;
  font-weight: 600;
  cursor: pointer;
}
.tab.active {
  background: var(--color-teal);
  color: #fff;
}
.list {
  padding: 8px 24px;
}
.post-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 2px dashed var(--color-beige);
  text-decoration: none;
  color: inherit;
}
.post-row:last-child {
  border: none;
}
.p-title {
  flex: 1;
  font-weight: 600;
}
.p-meta {
  font-size: 12px;
  color: var(--color-tan);
}
.author-link {
  cursor: pointer;
  font-weight: 700;
  color: var(--color-teal-dark);
}
.author-link:hover {
  text-decoration: underline;
}

/* 페이지네이션 */
.pager {
  margin-top: 20px;
}
.pager :deep(.pagination) {
  display: flex;
  justify-content: center;
  gap: 6px;
  list-style: none;
  padding: 0;
  flex-wrap: wrap;
}
.pager :deep(.page-link) {
  display: inline-block;
  padding: 8px 14px;
  border-radius: var(--radius-pill);
  background: var(--color-white);
  color: var(--color-teal-dark);
  font-weight: 700;
  font-size: 13px;
  text-decoration: none;
  border: 2px solid var(--color-beige);
}
.pager :deep(.page-item.active) .page-link {
  background: var(--color-teal);
  color: #fff;
  border-color: var(--color-teal);
}
</style>
