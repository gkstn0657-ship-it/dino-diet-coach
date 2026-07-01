<template>
  <div>
    <header class="ops-head">
      <div>
        <h1 class="ops-h1">회원 운영</h1>
        <p class="ops-desc">전체 {{ members.length }}명 · 이름/이메일로 검색</p>
      </div>
      <input
        v-model="keyword"
        class="dino-input search"
        placeholder="이름 또는 이메일 검색"
        @input="onSearch"
      />
    </header>

    <div class="cloud-card table-card">
      <table class="ops-table">
        <thead>
          <tr>
            <th>이름</th>
            <th>이메일</th>
            <th>권한</th>
            <th>목표</th>
            <th class="num">키 / 몸무게</th>
            <th>상태</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="m in members" :key="m.email" :class="{ dim: m.active === false }">
            <td>{{ m.name }}</td>
            <td>{{ m.email }}</td>
            <td>
              <span class="ops-badge" :class="isOperator(m.role) ? 'ok' : 'muted'">
                {{ m.role || 'USER' }}
              </span>
            </td>
            <td>{{ m.goal || '-' }}</td>
            <td class="num">{{ m.height ? m.height + 'cm' : '-' }} / {{ m.weight ? m.weight + 'kg' : '-' }}</td>
            <td>
              <span class="ops-badge" :class="m.active === false ? 'danger' : 'ok'">
                {{ m.active === false ? '정지' : '정상' }}
              </span>
            </td>
            <td class="num actions">
              <router-link :to="{ name: 'member-detail', query: { email: m.email } }" class="row-link">
                상세
              </router-link>
              <button
                v-if="!isOperator(m.role)"
                class="act-btn"
                :disabled="busy"
                @click="toggleActive(m)"
              >
                {{ m.active === false ? '정지 해제' : '정지' }}
              </button>
            </td>
          </tr>
          <tr v-if="!members.length">
            <td colspan="7" class="empty">검색 결과가 없어요.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useOpsMembers, useSetMemberActive } from '@/composables/useOpsApis';

const { data, execute } = useOpsMembers();
execute();

const members = computed(() => data.value ?? []);

const keyword = ref('');
let timer;
const onSearch = () => {
  clearTimeout(timer);
  timer = setTimeout(() => execute(keyword.value.trim()), 300);
};

const isOperator = (role) => role === 'OPERATOR' || role === 'ADMIN';

// 회원 정지/해제 (하드 삭제 대신) — 즉시 로컬 반영
const { execute: setActive } = useSetMemberActive();
const busy = ref(false);
const toggleActive = async (m) => {
  const nextActive = m.active === false; // 정지(false)면 해제(true)로
  if (busy.value) return;
  if (!nextActive && !confirm(`${m.name} 님을 정지할까요?`)) return;
  busy.value = true;
  const res = await setActive(m.mno, nextActive);
  if (res) m.active = nextActive;
  busy.value = false;
};
</script>

<style scoped>
.ops-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-4);
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
.search {
  max-width: 280px;
}
.table-card {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-4) var(--space-5);
  overflow-x: auto;
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
  border-color: var(--danger);
  color: var(--primary-dark);
}
tr.dim td {
  opacity: 0.55;
}
</style>
