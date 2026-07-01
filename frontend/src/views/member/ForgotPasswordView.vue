<template>
  <div class="dino-page narrow">
    <div class="cloud-card auth-card">
      <div class="auth-emoji">🔑</div>
      <h1 class="dino-page-title" style="text-align: center">비밀번호 재설정</h1>
      <p class="dino-page-subtitle" style="text-align: center">
        가입한 이메일을 입력하면 재설정 안내를 보내드려요.
      </p>

      <form v-if="!sent" @submit.prevent="submit">
        <div class="dino-field">
          <label for="email">이메일</label>
          <input
            id="email"
            v-model="email"
            type="email"
            class="dino-input"
            placeholder="email@ssafy.com"
            required
          />
        </div>
        <button type="submit" class="btn-primary block" :disabled="isLoading">
          {{ isLoading ? '전송 중...' : '재설정 링크 보내기' }}
        </button>
      </form>

      <div v-else class="done">
        <div class="done-emoji">📬</div>
        <p class="done-msg">
          입력하신 이메일이 가입되어 있다면 비밀번호 재설정 안내를 보냈어요.<br />
          메일함과 스팸함을 확인해 주세요. (재설정 링크는 30분간 유효합니다)
        </p>
      </div>

      <p class="to-login">
        <router-link :to="{ name: 'login-form' }">로그인으로 돌아가기</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRequestPasswordReset } from '@/composables/useMemberApis.js';

const email = ref('');
const sent = ref(false);
const { execute, isLoading } = useRequestPasswordReset();

const submit = async () => {
  const res = await execute(email.value.trim());
  // 계정 존재 여부와 무관하게 항상 동일한 안내 (정보 노출 방지)
  if (res !== undefined) sent.value = true;
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
.done {
  margin-top: 8px;
  text-align: center;
}
.done-emoji {
  font-size: 40px;
  margin-bottom: 8px;
}
.done-msg {
  font-size: 14px;
  color: var(--text);
  line-height: 1.6;
}
.to-login {
  text-align: center;
  font-size: 13px;
  margin-top: 14px;
}
</style>
