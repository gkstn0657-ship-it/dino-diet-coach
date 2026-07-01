<template>
  <div class="dino-page narrow">
    <h1 class="dino-page-title">글쓰기 ✍️</h1>
    <p class="dino-page-subtitle">커뮤니티에 새 글을 작성합니다.</p>

    <form class="cloud-card" @submit.prevent="handleSubmit">
      <div class="dino-field">
        <label>게시판</label>
        <select v-model="form.board" class="dino-select">
          <option value="review">식단 리뷰</option>
          <option v-if="isOperator" value="expert">칼럼 (운영자)</option>
          <option value="free">자유</option>
        </select>
        <p v-if="!isOperator" class="field-hint">📌 칼럼은 운영자만 작성할 수 있어요.</p>
      </div>
      <div class="dino-field">
        <label>제목</label>
        <input v-model="form.title" class="dino-input" placeholder="제목을 입력하세요" required />
      </div>
      <div class="dino-field">
        <label>내용</label>
        <textarea v-model="form.content" class="dino-textarea" style="min-height: 220px" required />
      </div>

      <div class="form-actions">
        <router-link :to="{ name: 'board-list' }" class="btn-ghost">취소</router-link>
        <button type="submit" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? '등록 중...' : '등록' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useWritePost } from '@/composables/useCommunityApis';
import { useMemberStore } from '@/stores/memberStore';

const memberStore = useMemberStore();
const isOperator = computed(() => ['OPERATOR', 'ADMIN'].includes(memberStore.loginUser?.role));

const form = ref({ board: 'review', title: '', content: '' });

// F114. 게시글 작성 API 연동
const { execute, isLoading } = useWritePost();
const handleSubmit = () => execute(form.value);
</script>

<style scoped>
.narrow {
  max-width: 760px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.btn-ghost {
  text-decoration: none;
}
.field-hint {
  font-size: 12px;
  color: var(--color-tan);
  margin: 6px 0 0;
}
</style>
