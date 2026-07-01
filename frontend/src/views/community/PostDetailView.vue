<template>
  <div class="dino-page narrow">
    <button class="btn-ghost sm" style="margin-bottom: 12px" @click="goBack">← 뒤로가기</button>

    <div class="cloud-card">
      <div class="p-head">
        <span class="tag-teal">{{ post.boardLabel }}</span>
        <div v-if="post.canManage" class="p-actions">
          <router-link :to="{ name: 'post-modify', params: { bno } }" class="btn-ghost sm"
            >수정</router-link
          >
          <button class="btn-primary sm" @click="handleDelete">삭제</button>
        </div>
      </div>
      <h1 class="p-title">{{ post.title }}</h1>
      <div class="p-meta">
        <span class="author-link" @click="goUser(post.authorEmail)">{{ post.author }}</span>
        · {{ post.date }}
      </div>
      <p class="p-body">{{ post.content }}</p>

      <div class="like-row">
        <button class="like-btn" :class="{ on: post.liked }" @click="onLike">
          <span class="heart">{{ post.liked ? '❤' : '🤍' }}</span>
          좋아요 <b>{{ post.likes ?? 0 }}</b>
        </button>
      </div>
    </div>

    <!-- 댓글 (F115) -->
    <div class="cloud-card" style="margin-top: 20px">
      <div class="label-caps">댓글 {{ comments.length }}</div>

      <form class="comment-form" @submit.prevent="addComment">
        <input v-model="draft" class="dino-input" placeholder="댓글을 입력하세요" />
        <button type="submit" class="btn-primary">등록</button>
      </form>

      <ul class="comment-list">
        <li v-for="c in comments" :key="c.cno" class="comment-item">
          <!-- 수정 모드 -->
          <template v-if="editingCno === c.cno">
            <input v-model="editDraft" class="dino-input edit-input" @keyup.enter="saveEdit(c.cno)" />
            <div class="c-actions">
              <button class="mini save" @click="saveEdit(c.cno)">저장</button>
              <button class="mini" @click="editingCno = null">취소</button>
            </div>
          </template>
          <!-- 일반 모드 -->
          <template v-else>
            <div>
              <b class="author-link" @click="goUser(c.authorEmail)">{{ c.author }}</b>
              <span class="c-text">{{ c.content }}</span>
            </div>
            <div v-if="c.canManage" class="c-actions">
              <button class="mini" @click="startEdit(c)">수정</button>
              <button class="del" @click="removeComment(c.cno)">✕</button>
            </div>
          </template>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  useFetchPostDetail,
  useDeletePost,
  useToggleLike,
  useFetchComments,
  useWriteComment,
  useUpdateComment,
  useDeleteComment,
} from '@/composables/useCommunityApis';

const route = useRoute();
const router = useRouter();
const bno = computed(() => route.params.bno);

// 뒤로가기: 이전 페이지가 있으면 history back, 직접 진입(새 탭 등)이면 게시판 목록으로
const goBack = () => {
  if (window.history.length > 1) router.back();
  else router.push({ name: 'board-list' });
};

// 작성자 닉네임 → 사용자 프로필
const goUser = (email) => {
  if (email) router.push({ name: 'user-profile', params: { email } });
};

// F114. 게시글 상세 (실제 로드)
const { data, execute } = useFetchPostDetail();
const post = computed(() => data.value ?? {});

const { execute: deletePost } = useDeletePost();
const handleDelete = () => confirm('게시글을 삭제할까요?') && deletePost(bno.value);

// 좋아요 토글 — 서버가 변경 후 { liked, likes } 반환 → 즉시 반영
const { execute: toggleLike } = useToggleLike();
const onLike = async () => {
  const res = await toggleLike(bno.value);
  if (res && data.value) {
    data.value.liked = res.liked;
    data.value.likes = res.likes;
  }
};

// 댓글
const { data: commentData, execute: fetchComments } = useFetchComments();
const comments = computed(() => commentData.value ?? []);

const draft = ref('');
const { execute: writeComment } = useWriteComment();
const addComment = async () => {
  if (!draft.value.trim()) return;
  await writeComment(bno.value, draft.value);
  draft.value = '';
  fetchComments(bno.value);
};

// F115. 댓글 수정 (인라인)
const editingCno = ref(null);
const editDraft = ref('');
const { execute: updateComment } = useUpdateComment();
const startEdit = (c) => {
  editingCno.value = c.cno;
  editDraft.value = c.content;
};
const saveEdit = async (cno) => {
  if (!editDraft.value.trim()) return;
  await updateComment(bno.value, cno, editDraft.value);
  editingCno.value = null;
  fetchComments(bno.value);
};

const { execute: deleteComment } = useDeleteComment();
const removeComment = async (cno) => {
  await deleteComment(bno.value, cno);
  fetchComments(bno.value);
};

watch(
  bno,
  (v) => {
    if (!v) return;
    execute(v);
    fetchComments(v);
  },
  { immediate: true }
);
</script>

<style scoped>
.narrow {
  max-width: 760px;
}
.p-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.p-actions {
  display: flex;
  gap: 8px;
}
.sm {
  padding: 6px 14px;
  font-size: 12px;
  text-decoration: none;
}
.p-title {
  font-size: 24px;
  margin: 12px 0 4px;
}
.p-meta {
  color: var(--color-tan);
  font-size: 13px;
  margin-bottom: 16px;
}
.author-link {
  cursor: pointer;
  color: var(--color-teal-dark);
  font-weight: 700;
}
.author-link:hover {
  text-decoration: underline;
}
.p-body {
  font-size: 15px;
  line-height: 1.7;
}
.like-row {
  margin-top: 18px;
  display: flex;
  justify-content: center;
}
.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 2px solid var(--color-beige);
  background: var(--color-white);
  color: var(--color-text-dark);
  border-radius: var(--radius-pill);
  padding: 8px 18px;
  font-family: inherit;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.15s;
}
.like-btn .heart {
  font-size: 16px;
}
.like-btn.on {
  border-color: var(--color-teal);
  background: var(--color-teal);
  color: #fff;
}
.like-btn:active {
  transform: scale(0.96);
}
.comment-form {
  display: flex;
  gap: 10px;
  margin: 14px 0;
}
.comment-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.comment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 2px dashed var(--color-beige);
}
.c-text {
  margin-left: 10px;
  font-size: 14px;
}
.del {
  border: none;
  background: none;
  color: var(--color-tan);
  cursor: pointer;
  font-size: 14px;
}
.c-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}
.mini {
  border: none;
  background: var(--color-beige);
  color: var(--color-text-dark);
  border-radius: 8px;
  padding: 4px 10px;
  font-family: inherit;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.mini.save {
  background: var(--color-teal);
  color: #fff;
}
.edit-input {
  flex: 1;
  margin-right: 10px;
  padding: 8px 12px;
}
</style>
