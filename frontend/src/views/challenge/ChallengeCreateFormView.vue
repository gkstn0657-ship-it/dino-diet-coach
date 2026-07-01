<template>
  <div class="dino-page narrow">
    <h1 class="dino-page-title">챌린지 등록 신청 ✨</h1>
    <p class="dino-page-subtitle">기간·설명·이미지를 등록해 새 챌린지를 신청해요.</p>

    <p class="approval-notice">📝 등록 신청한 챌린지는 <b>운영자 승인 후 공개</b>됩니다.</p>

    <form class="cloud-card" @submit.prevent="handleSubmit">
      <div class="dino-field">
        <label>챌린지 이름</label>
        <input v-model="form.title" class="dino-input" placeholder="예: Water Raptor" required />
      </div>
      <div class="dino-field">
        <label>설명</label>
        <textarea v-model="form.desc" class="dino-textarea" placeholder="챌린지 내용을 설명하세요" />
      </div>
      <div class="dino-grid dino-grid-2">
        <div class="dino-field">
          <label>시작일 <span class="hint">(내일부터)</span></label>
          <input v-model="form.startDate" type="date" class="dino-input" :min="minStart" required />
        </div>
        <div class="dino-field">
          <label>종료일 <span class="hint">(시작일 이후)</span></label>
          <input v-model="form.endDate" type="date" class="dino-input" :min="minEnd" required />
        </div>
      </div>
      <!-- 식단 연동 인증 조건 -->
      <div class="dino-grid dino-grid-2">
        <div class="dino-field">
          <label>인증 조건 <span class="hint">(식단 기록 연동)</span></label>
          <select v-model="form.condType" class="dino-select">
            <option value="">조건 없음 — 버튼으로 인증</option>
            <option value="DAILY_KCAL_MAX">🥗 일일 칼로리 ○○ 이하</option>
            <option value="DAILY_PROTEIN_MIN">💪 일일 단백질 ○○ 이상</option>
            <option value="WEEKLY_KCAL_MAX">📅 최근 7일 칼로리 합 ○○ 이하</option>
            <option value="DAILY_WATER_MIN">💧 일일 물 ○○잔 이상</option>
          </select>
        </div>
        <div v-if="form.condType" class="dino-field">
          <label>기준값 ({{ condUnit }})</label>
          <input
            v-model.number="form.condValue"
            type="number"
            min="1"
            class="dino-input"
            :placeholder="condPlaceholder"
            required
          />
        </div>
      </div>
      <p v-if="form.condType" class="cond-help">
        💡 참여자는 해당 조건을 만족하는 식단을 기록해야만 일일 인증을 할 수 있어요.
      </p>

      <div class="dino-field">
        <label>대표 이미지</label>
        <input type="file" accept="image/*" class="dino-input" @change="onFile" />
      </div>

      <!-- 실패 사유 표시 (이전엔 에러가 조용히 무시되어 '무반응'처럼 보였음) -->
      <p v-if="errorMsg" class="error-msg">⚠ {{ errorMsg }}</p>

      <div class="form-actions">
        <router-link :to="{ name: 'challenge-list' }" class="btn-ghost">취소</router-link>
        <button type="submit" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? '신청 중...' : '등록 신청' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useCreateChallenge } from '@/composables/useChallengeApis';
import { useMemberStore } from '@/stores/memberStore';

const memberStore = useMemberStore();

// 날짜 제한: 시작일은 내일부터, 종료일은 시작일 다음날부터
const pad = (n) => String(n).padStart(2, '0');
const ymd = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const addDays = (base, n) => {
  const d = new Date(base);
  d.setDate(d.getDate() + n);
  return ymd(d);
};
const minStart = computed(() => addDays(new Date(), 1));
const minEnd = computed(() => (form.value.startDate ? addDays(new Date(form.value.startDate), 1) : minStart.value));

const form = ref({
  title: '',
  desc: '',
  startDate: '',
  endDate: '',
  condType: '',
  condValue: null,
  image: null,
});
const onFile = (e) => (form.value.image = e.target.files[0]);

// 조건 타입별 기준값 단위/플레이스홀더
const condUnit = computed(() => {
  if (form.value.condType === 'DAILY_PROTEIN_MIN') return 'g';
  if (form.value.condType === 'DAILY_WATER_MIN') return '잔';
  return 'kcal';
});
const condPlaceholder = computed(() => {
  if (form.value.condType === 'DAILY_PROTEIN_MIN') return '예: 100';
  if (form.value.condType === 'DAILY_WATER_MIN') return '예: 8';
  return '예: 1500';
});

// 시작일을 바꾸면 종료일이 더 이상 유효하지 않을 때 비움
watch(
  () => form.value.startDate,
  () => {
    if (form.value.endDate && form.value.endDate < minEnd.value) form.value.endDate = '';
  }
);

// F112. 챌린지 생성 API 연동 (빈 값은 전송 제외, 실패 시 사유 표시)
const { execute, isLoading, error } = useCreateChallenge();
const errorMsg = ref('');
const handleSubmit = async () => {
  errorMsg.value = '';
  if (!form.value.startDate || form.value.startDate < minStart.value) {
    errorMsg.value = '시작일은 내일 이후로 선택해 주세요.';
    return;
  }
  if (!form.value.endDate || form.value.endDate < minEnd.value) {
    errorMsg.value = '종료일은 시작일 이후로 선택해 주세요.';
    return;
  }
  if (form.value.condType && (!form.value.condValue || form.value.condValue <= 0)) {
    errorMsg.value = '인증 조건의 기준값을 1 이상으로 입력해주세요.';
    return;
  }
  const fd = new FormData();
  Object.entries(form.value).forEach(([k, v]) => {
    if (v != null && v !== '') fd.append(k, v);
  });
  await execute(fd);
  if (error.value) {
    errorMsg.value =
      error.value?.response?.data?.message ??
      (error.value?.code === 'ECONNABORTED'
        ? '요청 시간이 초과됐어요. 백엔드 서버가 실행 중인지 확인해주세요.'
        : '신청에 실패했어요. 백엔드 서버 상태를 확인해주세요.');
  } else {
    // 성공 시(목록으로 이동됨) 전역 안내
    memberStore.setAlertMsg('챌린지 등록을 신청했어요. 운영자 승인 후 공개됩니다. 🦕');
  }
};
</script>

<style scoped>
.narrow {
  max-width: 720px;
}
.approval-notice {
  background: var(--color-beige);
  color: var(--color-teal-dark);
  border-radius: var(--radius-small);
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 14px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}
.btn-ghost {
  text-decoration: none;
}
.hint {
  font-weight: 400;
  font-size: 11px;
  opacity: 0.6;
}
.cond-help {
  font-size: 12px;
  color: var(--color-teal-dark);
  background: var(--color-beige);
  border-radius: var(--radius-small);
  padding: 10px 14px;
  margin: -6px 0 14px;
}
.error-msg {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-red);
  background: #fdecee;
  border-radius: var(--radius-small);
  padding: 10px 14px;
  margin-bottom: 4px;
}
</style>
