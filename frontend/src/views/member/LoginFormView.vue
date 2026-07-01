<template>
  <div class="dino-page narrow">
    <div class="cloud-card auth-card">
      <div class="auth-emoji">🦕</div>
      <h1 class="dino-page-title" style="text-align: center">냠냠코치 로그인</h1>
      <p class="dino-page-subtitle" style="text-align: center">건강한 식단 여정을 시작하세요</p>

      <form @submit.prevent="handleLogin">
        <div class="dino-field">
          <label for="email">이메일</label>
          <input id="email" v-model="member.email" type="email" class="dino-input"
                 placeholder="email@ssafy.com" required />
        </div>
        <div class="dino-field">
          <label for="password">비밀번호</label>
          <input id="password" v-model="member.password" type="password" class="dino-input" required />
        </div>
        <button type="submit" class="btn-primary block" :disabled="isLoading">
          {{ isLoading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <p class="to-regist">
        아직 회원이 아니신가요?
        <router-link :to="{ name: 'regist-member-form' }">회원가입</router-link>
      </p>
      <p class="forgot">
        <!-- <a href="#" @click.prevent="forgotPassword">비밀번호를 잊으셨나요?</a> -->
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMemberStore } from '@/stores/memberStore';
import { useLogin } from '@/composables/useMemberApis.js';

const route = useRoute();
const router = useRouter();
const memberStore = useMemberStore();

const member = ref({ email: '', password: '' });
const { execute, isLoading } = useLogin();

watch(
  () => memberStore.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) router.push(route.query.to || { name: 'home' });
  }
);

const handleLogin = () => execute(member.value);

// 비밀번호 재설정 요청 화면으로 이동
const forgotPassword = () => router.push({ name: 'forgot-password' });
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
}
.hint {
  text-align: center;
  font-size: 12px;
  color: var(--color-tan);
  margin-top: 14px;
}
.to-regist {
  text-align: center;
  font-size: 13px;
  margin-top: 6px;
}
.forgot {
  text-align: center;
  font-size: 12px;
  margin-top: 4px;
}
.forgot a {
  color: var(--color-tan);
  text-decoration: none;
}
.forgot a:hover {
  color: var(--color-teal-dark);
}
</style>
