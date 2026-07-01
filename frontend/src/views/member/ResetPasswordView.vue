<template>
  <div class="dino-page narrow">
    <div class="cloud-card auth-card">
      <div class="auth-emoji">🔒</div>
      <h1 class="dino-page-title" style="text-align: center">새 비밀번호 설정</h1>

      <!-- 토큰이 없는(잘못된) 접근 -->
      <div v-if="!token" class="invalid">
        <p class="err-msg">재설정 링크가 올바르지 않아요. 다시 요청해 주세요.</p>
        <router-link :to="{ name: 'forgot-password' }" class="btn-primary block">
          비밀번호 찾기로 이동
        </router-link>
      </div>

      <!-- 정상 링크: 새 비밀번호 입력 -->
      <form v-else @submit.prevent="submit">
        <p class="dino-page-subtitle" style="text-align: center">새로 사용할 비밀번호를 입력하세요.</p>
        <div class="dino-field">
          <label for="pw">새 비밀번호 <span class="hint">(8자 이상)</span></label>
          <input
            id="pw"
            v-model="form.newPassword"
            type="password"
            class="dino-input"
            placeholder="8자 이상"
            autocomplete="new-password"
            required
          />
        </div>
        <div class="dino-field">
          <label for="pw2">새 비밀번호 확인</label>
          <input
            id="pw2"
            v-model="form.newPasswordConfirm"
            type="password"
            class="dino-input"
            autocomplete="new-password"
            required
          />
        </div>
        <p v-if="localError" class="err">{{ localError }}</p>
        <button type="submit" class="btn-primary block" :disabled="isLoading || !isValid">
          {{ isLoading ? '변경 중...' : '비밀번호 변경' }}
        </button>
      </form>

      <p class="to-login">
        <router-link :to="{ name: 'login-form' }">로그인으로 돌아가기</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMemberStore } from '@/stores/memberStore';
import { useResetPassword } from '@/composables/useMemberApis.js';

const route = useRoute();
const router = useRouter();
const memberStore = useMemberStore();

// 링크의 query token 을 자동으로 사용(사용자가 직접 입력/수정하지 않음)
const token = route.query.token || '';
const form = ref({ newPassword: '', newPasswordConfirm: '' });
const localError = ref('');
const { execute, isLoading, error } = useResetPassword();

const isValid = computed(
  () => form.value.newPassword.length >= 8 && form.value.newPassword === form.value.newPasswordConfirm
);

const submit = async () => {
  localError.value = '';
  if (form.value.newPassword.length < 8) {
    localError.value = '새 비밀번호는 8자 이상이어야 합니다.';
    return;
  }
  if (form.value.newPassword !== form.value.newPasswordConfirm) {
    localError.value = '새 비밀번호 확인이 일치하지 않습니다.';
    return;
  }
  const res = await execute({
    token,
    newPassword: form.value.newPassword,
    newPasswordConfirm: form.value.newPasswordConfirm,
  });
  if (res) {
    memberStore.setAlertMsg('비밀번호가 재설정되었어요. 새 비밀번호로 로그인하세요.');
    router.push({ name: 'login-form' });
  } else {
    // 서버 메시지(만료/사용됨/비밀번호 조건) 우선, 없으면 일반 안내
    localError.value =
      error.value?.response?.data?.message ||
      '링크가 만료되었거나 이미 사용되었어요. 다시 요청해 주세요.';
  }
};
</script>

<style scoped>
.narrow {
  max-width: 440px;
}
.auth-card {
  text-align: left;
}
.auth-emoji {
  font-size: 48px;
  text-align: center;
  margin-bottom: 8px;
}
.block {
  display: block;
  width: 100%;
  margin-top: 8px;
  text-align: center;
}
.invalid {
  text-align: center;
}
.err-msg {
  font-size: 14px;
  color: var(--text);
  line-height: 1.6;
  margin: 8px 0 14px;
}
.hint {
  font-weight: 500;
  color: var(--text-muted);
  font-size: 12px;
}
.err {
  color: var(--danger);
  font-size: 13px;
  margin: 4px 0 0;
}
.to-login {
  text-align: center;
  font-size: 13px;
  margin-top: 14px;
}
</style>
