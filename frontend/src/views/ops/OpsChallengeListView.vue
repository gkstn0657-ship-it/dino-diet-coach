<template>
  <div>
    <header class="ops-head">
      <h1 class="ops-h1">챌린지 운영</h1>
      <p class="ops-desc">전체 {{ rows.length }}개 챌린지</p>
    </header>

    <div class="cloud-card table-card">
      <table class="ops-table">
        <thead>
          <tr>
            <th>챌린지명</th>
            <th>승인</th>
            <th>노출</th>
            <th>상태</th>
            <th>조건</th>
            <th class="num">참여자</th>
            <th>생성자</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in rows" :key="c.cno" :class="{ dim: c.visibility === 'HIDDEN' }">
            <td>{{ c.title }}</td>
            <td><span class="ops-badge" :class="approvalClass(c.approvalStatus)">{{ approvalLabel(c.approvalStatus) }}</span></td>
            <td><span class="ops-badge" :class="c.visibility === 'HIDDEN' ? 'muted' : 'ok'">{{ c.visibility === 'HIDDEN' ? '숨김' : '노출' }}</span></td>
            <td><span class="ops-badge" :class="statusClass(c.status)">{{ c.status }}</span></td>
            <td>{{ condTypeLabel(c.condType) }}{{ c.condValue != null ? ' ' + c.condValue : '' }}</td>
            <td class="num">{{ c.participants }}</td>
            <td>{{ c.creator }}</td>
            <td class="actions">
              <button v-if="c.approvalStatus !== 'APPROVED'" class="act-btn ok" :disabled="busy" @click="approve(c)">승인</button>
              <button v-if="c.approvalStatus !== 'REJECTED'" class="act-btn warn" :disabled="busy" @click="reject(c)">거부</button>
              <button class="act-btn" :disabled="busy" @click="toggleVisible(c)">{{ c.visibility === 'HIDDEN' ? '노출' : '숨김' }}</button>
              <button class="act-btn danger" :disabled="busy" @click="remove(c)">삭제</button>
            </td>
          </tr>
          <tr v-if="!rows.length">
            <td colspan="8" class="empty">등록된 챌린지가 없어요.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import {
  useOpsChallenges,
  useSetChallengeApproval,
  useSetChallengeVisibility,
  useDeleteChallengeOps,
} from '@/composables/useOpsApis';

const { data, execute } = useOpsChallenges();
execute();

const rows = computed(() => data.value ?? []);

const COND = {
  DAILY_KCAL_MAX: '일일 칼로리 이하',
  DAILY_PROTEIN_MIN: '일일 단백질 이상',
  WEEKLY_KCAL_MAX: '주간 칼로리 이하',
  DAILY_WATER_MIN: '일일 물 잔 이상',
};
const condTypeLabel = (t) => (t ? COND[t] ?? t : '조건 없음');

const statusClass = (status) => {
  if (status === '진행중') return 'ok';
  if (status === '모집중') return 'warn';
  return 'muted'; // 종료
};
const APPROVAL = { PENDING: '대기', APPROVED: '승인', REJECTED: '거부' };
const approvalLabel = (s) => APPROVAL[s] ?? s ?? '승인';
const approvalClass = (s) => (s === 'APPROVED' ? 'ok' : s === 'REJECTED' ? 'danger' : 'warn');

// 운영 액션
const { execute: setApproval } = useSetChallengeApproval();
const { execute: setVisibility } = useSetChallengeVisibility();
const { execute: deleteChallenge } = useDeleteChallengeOps();
const busy = ref(false);

const run = async (fn) => {
  if (busy.value) return;
  busy.value = true;
  const ok = await fn();
  if (ok) await execute(); // 목록 갱신
  busy.value = false;
};
const approve = (c) => run(() => setApproval(c.cno, 'APPROVED'));
const reject = (c) => run(() => setApproval(c.cno, 'REJECTED'));
const toggleVisible = (c) => run(() => setVisibility(c.cno, c.visibility === 'HIDDEN'));
const remove = (c) => {
  if (!confirm(`'${c.title}' 챌린지를 삭제할까요? (참가자가 있으면 숨김 처리됩니다)`)) return;
  run(() => deleteChallenge(c.cno));
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
.empty {
  text-align: center;
  color: var(--text-muted);
  padding: var(--space-6);
  font-weight: 600;
}
.actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
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
.act-btn.ok:hover { border-color: var(--success); color: #1f7a4d; }
.act-btn.warn:hover { border-color: var(--warning); color: #9a6b16; }
.act-btn.danger:hover { border-color: var(--danger); color: var(--primary-dark); }
tr.dim td { opacity: 0.55; }
</style>
