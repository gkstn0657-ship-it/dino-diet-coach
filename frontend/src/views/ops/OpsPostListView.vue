<template>
  <div>
    <header class="ops-head">
      <h1 class="ops-h1">게시글 운영</h1>
      <p class="ops-desc">전체 {{ rows.length }}개 게시글</p>
    </header>

    <div class="cloud-card table-card">
      <table class="ops-table">
        <thead>
          <tr>
            <th>게시판</th>
            <th>제목</th>
            <th>작성자</th>
            <th class="num">좋아요</th>
            <th class="num">댓글</th>
            <th>상태</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in rows" :key="p.bno" :class="{ dim: p.hidden }">
            <td><span class="ops-badge muted">{{ p.boardLabel }}</span></td>
            <td class="title-cell">{{ p.title }}</td>
            <td>{{ p.author }}</td>
            <td class="num">{{ p.likes ?? 0 }}</td>
            <td class="num">{{ p.comments ?? 0 }}</td>
            <td>
              <span class="ops-badge" :class="p.hidden ? 'warn' : 'ok'">
                {{ p.hidden ? '숨김' : '노출' }}
              </span>
            </td>
            <td class="num actions">
              <router-link :to="{ name: 'post-detail', params: { bno: p.bno } }" class="row-link">
                상세
              </router-link>
              <button class="act-btn" :disabled="busy" @click="toggleHidden(p)">
                {{ p.hidden ? '숨김 해제' : '숨김' }}
              </button>
            </td>
          </tr>
          <tr v-if="!rows.length">
            <td colspan="7" class="empty">등록된 게시글이 없어요.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useOpsPosts, useSetPostHidden } from '@/composables/useOpsApis';

const { data, execute } = useOpsPosts();
execute();

const rows = computed(() => data.value ?? []);

// 게시글 숨김/해제 (하드 삭제 대신) — 즉시 로컬 반영
const { execute: setHidden } = useSetPostHidden();
const busy = ref(false);
const toggleHidden = async (p) => {
  if (busy.value) return;
  busy.value = true;
  const res = await setHidden(p.bno, !p.hidden);
  if (res) p.hidden = !p.hidden;
  busy.value = false;
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
.table-card {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-4) var(--space-5);
  overflow-x: auto;
}
.title-cell {
  font-weight: 600;
  color: var(--text-strong);
  max-width: 360px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.empty {
  text-align: center;
  color: var(--text-muted);
  padding: var(--space-6);
  font-weight: 600;
}
.actions {
  display: flex;
  gap: var(--space-2);
  align-items: center;
  justify-content: flex-end;
}
.act-btn {
  border: 1px solid var(--border-soft);
  background: var(--surface);
  color: var(--text);
  border-radius: var(--radius-pill);
  padding: 5px 12px;
  font-family: inherit;
  font-size: var(--fs-label);
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}
.act-btn:hover {
  border-color: var(--warning);
  color: #9a6b16;
}
tr.dim td {
  opacity: 0.55;
}
</style>
