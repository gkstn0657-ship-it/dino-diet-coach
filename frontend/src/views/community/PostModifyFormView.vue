<template>
  <div class="dino-page narrow">
    <h1 class="dino-page-title">글 수정 ✏️</h1>
    <p class="dino-page-subtitle">작성한 게시글을 수정합니다.</p>

    <form class="cloud-card" @submit.prevent="handleSubmit">
      <div class="dino-field">
        <label>제목</label>
        <input v-model="form.title" class="dino-input" required />
      </div>
      <div class="dino-field">
        <label>내용</label>
        <textarea v-model="form.content" class="dino-textarea" style="min-height: 220px" required />
      </div>

      <div class="form-actions">
        <router-link :to="{ name: 'post-detail', params: { bno } }" class="btn-ghost"
          >취소</router-link
        >
        <button type="submit" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? '저장 중...' : '수정 완료' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useFetchPostDetail, useModifyPost } from '@/composables/useCommunityApis';

const route = useRoute();
const bno = computed(() => route.params.bno);
const form = ref({ title: '', content: '' });

// F114. 기존 게시글 로드
const { execute: fetchDetail } = useFetchPostDetail();
watch(
  bno,
  async (v) => {
    if (!v) return;
    const detail = await fetchDetail(v);
    if (detail) form.value = { title: detail.title, content: detail.content };
  },
  { immediate: true }
);

// F114. 게시글 수정 저장
const { execute, isLoading } = useModifyPost();
const handleSubmit = () => execute(bno.value, form.value);
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
</style>
