<template>
  <div class="dino-page narrow">
    <h1 class="dino-page-title">프로필 수정 ✏️</h1>
    <p class="dino-page-subtitle">건강 정보를 최신으로 유지하세요.</p>

    <form class="cloud-card" @submit.prevent="handleSubmit">
      <div class="dino-field">
        <label>이름</label>
        <input v-model="form.name" class="dino-input" required />
      </div>
      <div class="dino-grid dino-grid-2">
        <div class="dino-field">
          <label>키 (cm)</label>
          <input v-model.number="form.height" type="number" class="dino-input" />
        </div>
        <div class="dino-field">
          <label>몸무게 (kg)</label>
          <input v-model.number="form.weight" type="number" class="dino-input" />
        </div>
      </div>
      <div class="dino-field">
        <label>질환</label>
        <input v-model="form.disease" class="dino-input" placeholder="없으면 비워두세요" />
      </div>
      <div class="dino-grid dino-grid-2">
        <div class="dino-field">
          <label>성별</label>
          <select v-model="form.gender" class="dino-select">
            <option :value="null">선택 안 함</option>
            <option value="male">남성</option>
            <option value="female">여성</option>
          </select>
        </div>
        <div class="dino-field">
          <label>출생연도</label>
          <input v-model.number="form.birthYear" type="number" class="dino-input" placeholder="예: 1998" />
        </div>
      </div>
      <div class="dino-field">
        <label>체중 관리 목표</label>
        <select v-model="form.goalType" class="dino-select">
          <option value="DIET">감량 (TDEE의 85%)</option>
          <option value="MAINTAIN">유지 (TDEE)</option>
          <option value="BULK_UP">증량 (TDEE의 115%)</option>
        </select>
      </div>

      <!-- 활동량 설문 (목표 칼로리 계산용) -->
      <div class="survey">
        <div class="survey-head">🏃 활동량 설문</div>
        <p class="field-hint">정확한 목표 칼로리 계산을 위한 항목이에요. 평소에 가까운 쪽을 골라주세요.</p>
        <div class="dino-grid dino-grid-2">
          <div class="dino-field">
            <label>직업 활동량</label>
            <select v-model.number="form.jobActivity" class="dino-select">
              <option :value="1">대부분 앉아서 근무</option>
              <option :value="2">앉기와 서기를 반복</option>
              <option :value="3">대부분 서서 근무</option>
              <option :value="4">육체 노동 위주</option>
            </select>
          </div>
          <div class="dino-field">
            <label>운동 빈도</label>
            <select v-model.number="form.exerciseFrequency" class="dino-select">
              <option :value="0">운동 안 함</option>
              <option :value="1">주 1~2회</option>
              <option :value="2">주 3~4회</option>
              <option :value="3">주 5~6회</option>
              <option :value="4">거의 매일</option>
            </select>
          </div>
          <div class="dino-field">
            <label>운동 강도</label>
            <select v-model.number="form.exerciseIntensity" class="dino-select">
              <option :value="0">산책 수준</option>
              <option :value="1">가벼운 운동</option>
              <option :value="2">보통 운동</option>
              <option :value="3">고강도 운동</option>
              <option :value="4">선수급 훈련</option>
            </select>
          </div>
          <div class="dino-field">
            <label>하루 평균 걸음 수</label>
            <select v-model.number="form.dailySteps" class="dino-select">
              <option :value="0">3,000보 미만</option>
              <option :value="1">3,000~6,000보</option>
              <option :value="2">6,000~10,000보</option>
              <option :value="3">10,000~15,000보</option>
              <option :value="4">15,000보 이상</option>
            </select>
          </div>
          <div class="dino-field">
            <label>주당 운동 시간</label>
            <select v-model.number="form.weeklyExerciseHours" class="dino-select">
              <option :value="0">없음</option>
              <option :value="1">1~2시간</option>
              <option :value="2">3~5시간</option>
              <option :value="3">5~8시간</option>
              <option :value="4">8시간 이상</option>
            </select>
          </div>
        </div>
        <p class="field-hint">성별·출생연도·키·몸무게·목표·활동량이 모두 입력되면 목표 칼로리가 자동 계산돼요.</p>
      </div>

      <div class="form-actions">
        <router-link :to="{ name: 'mypage' }" class="btn-ghost">취소</router-link>
        <button type="submit" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? '저장 중...' : '수정 완료' }}
        </button>
      </div>
    </form>

    <!-- 비밀번호 변경 -->
    <form class="cloud-card pw-card" @submit.prevent="handleChangePassword">
      <h2 class="pw-title">🔒 비밀번호 변경</h2>
      <p class="pw-sub">안전을 위해 현재 비밀번호를 확인해요.</p>

      <div class="dino-field">
        <label>현재 비밀번호</label>
        <input v-model="pw.currentPassword" type="password" class="dino-input" autocomplete="current-password" />
      </div>
      <div class="dino-field">
        <label>새 비밀번호 <span class="hint">(8자 이상)</span></label>
        <input v-model="pw.newPassword" type="password" class="dino-input" autocomplete="new-password" />
      </div>
      <div class="dino-field">
        <label>새 비밀번호 확인</label>
        <input v-model="pw.newPasswordConfirm" type="password" class="dino-input" autocomplete="new-password" />
      </div>

      <p v-if="pwMismatch" class="pw-warn">새 비밀번호와 확인값이 일치하지 않아요.</p>

      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="pwLoading || !pwValid">
          {{ pwLoading ? '변경 중...' : '비밀번호 변경' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useMemberStore } from '@/stores/memberStore';
import {
  useFetchMemberDetail,
  useUpdateProfile,
  useChangePassword,
  useMyTarget,
} from '@/composables/useMemberApis';

const router = useRouter();
const memberStore = useMemberStore();
const email = memberStore.loginUser?.email;

// 활동량 설문 기본값(센더너리 기준) — 사용자가 바꾸기 전에도 계산 가능하도록
const form = ref({
  name: '', height: null, weight: null, disease: '',
  gender: null, birthYear: null, goalType: 'MAINTAIN',
  jobActivity: 1, exerciseFrequency: 0, exerciseIntensity: 0, dailySteps: 1, weeklyExerciseHours: 0,
});

// 현재 프로필 로드 (저장된 값이 있으면 덮어쓰기)
const { data: detail, execute: fetchDetail } = useFetchMemberDetail();
watch(detail, (d) => {
  if (!d) return;
  form.value = {
    name: d.name ?? '',
    height: d.height ?? null,
    weight: d.weight ?? null,
    disease: d.disease ?? '',
    gender: d.gender ?? null,
    birthYear: d.birthYear ?? null,
    goalType: d.goalType ?? 'MAINTAIN',
    jobActivity: d.jobActivity ?? 1,
    exerciseFrequency: d.exerciseFrequency ?? 0,
    exerciseIntensity: d.exerciseIntensity ?? 0,
    dailySteps: d.dailySteps ?? 1,
    weeklyExerciseHours: d.weeklyExerciseHours ?? 0,
  };
});
fetchDetail(email);

const { execute, isLoading } = useUpdateProfile();
const { execute: fetchTarget } = useMyTarget();
const handleSubmit = async () => {
  const res = await execute({ ...form.value, email }, email);
  if (!res) return;
  memberStore.updateLoginStatus({ ...memberStore.loginUser, name: form.value.name });
  // 저장 직후 서버가 계산한 목표 칼로리를 조회해 피드백
  const t = await fetchTarget();
  if (t?.hasProfile && t?.targetCalories) {
    memberStore.setAlertMsg(`프로필이 저장됐어요. 목표 칼로리: ${t.targetCalories.toLocaleString()} kcal 🎯`);
  } else {
    memberStore.setAlertMsg('프로필이 저장됐어요. (성별·출생연도·키·몸무게가 모두 있어야 목표 칼로리가 계산돼요)');
  }
  router.push({ name: 'mypage' });
};

// 비밀번호 변경
const pw = ref({ currentPassword: '', newPassword: '', newPasswordConfirm: '' });
const pwMismatch = computed(
  () => !!pw.value.newPasswordConfirm && pw.value.newPassword !== pw.value.newPasswordConfirm
);
const pwValid = computed(
  () =>
    pw.value.currentPassword &&
    pw.value.newPassword.length >= 8 &&
    pw.value.newPassword === pw.value.newPasswordConfirm
);
const { execute: changePassword, error: pwError, isLoading: pwLoading } = useChangePassword();
const handleChangePassword = async () => {
  if (!pwValid.value) {
    memberStore.setAlertMsg('입력값을 확인해주세요. (새 비밀번호 8자 이상, 확인 일치)');
    return;
  }
  const res = await changePassword({ ...pw.value });
  if (res) {
    pw.value = { currentPassword: '', newPassword: '', newPasswordConfirm: '' };
    memberStore.setAlertMsg('비밀번호가 변경되었습니다.');
  } else {
    // 서버 메시지(현재 비밀번호 불일치 등) 노출
    memberStore.setAlertMsg(pwError.value?.response?.data?.message || '비밀번호 변경에 실패했습니다.');
  }
};
</script>

<style scoped>
.narrow {
  max-width: 560px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid var(--border-soft);
}
.btn-ghost {
  text-decoration: none;
}
.pw-card {
  margin-top: 20px;
}
.pw-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-strong);
  margin: 0 0 4px;
}
.pw-sub {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0 0 16px;
}
.pw-card .hint {
  font-weight: 500;
  color: var(--text-muted);
  font-size: 12px;
}
.pw-warn {
  color: var(--danger);
  font-size: 13px;
  font-weight: 600;
  margin: 4px 0 0;
}
.field-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin: 6px 0 0;
}
.survey {
  margin-top: 18px;
  padding: 16px;
  border-radius: var(--radius-md, 12px);
  background: var(--surface-muted);
  border: 1px solid var(--border-soft);
}
.survey-head {
  font-weight: 800;
  color: var(--text-strong);
  margin-bottom: 2px;
}
.survey .field-hint {
  margin-bottom: 10px;
}
</style>
