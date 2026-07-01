<template>
  <header class="dino-header">
    <!-- 로고 -->
    <router-link :to="{ name: 'home' }" class="logo">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="var(--color-teal)">
        <circle cx="12" cy="12" r="10" />
        <path
          d="M8 12c0-2.21 1.79-4 4-4s4 1.79 4 4"
          fill="none"
          stroke="white"
          stroke-width="2"
          stroke-linecap="round"
        />
      </svg>
      DinoDiet
    </router-link>

    <!-- 주요 네비게이션 -->
    <nav class="dino-nav">
      <router-link :to="{ name: 'home' }">홈</router-link>
      <router-link :to="{ name: 'diet-list' }">식단</router-link>
      <router-link :to="{ name: 'ai-workout-coach' }">AI 코칭</router-link>
      <router-link :to="{ name: 'challenge-list' }">챌린지</router-link>
      <router-link :to="{ name: 'board-list' }">커뮤니티</router-link>
      <!-- 회원목록 대신 '팔로우'(내 프로필: 팔로워/팔로잉·작성글·챌린지). 회원 관리는 운영 콘솔. -->
      <router-link
        v-if="memberStore.isLoggedIn"
        :to="{ name: 'user-profile', params: { email: memberStore.loginUser?.email } }"
      >팔로우</router-link>
      <router-link v-if="isOperator" :to="{ name: 'ops-dashboard' }" class="ops-entry">운영 콘솔</router-link>
    </nav>

    <!-- 사용자 프로필 / 인증 -->
    <div class="user-profile">
      <template v-if="!memberStore.isLoggedIn">
        <router-link :to="{ name: 'login-form' }" class="auth-link">로그인</router-link>
        <router-link :to="{ name: 'regist-member-form' }" class="btn-dino sm">회원가입</router-link>
      </template>
      <template v-else>
        <router-link :to="{ name: 'mypage' }" class="profile-chip">
          <span>{{ memberStore.loginUser?.name || 'My Journey' }}</span>
          <span class="avatar" />
        </router-link>
        <a href="#" class="auth-link" @click.prevent="logoutApi.execute">로그아웃</a>
      </template>
    </div>
  </header>
</template>

<script setup>
import { watch, computed } from 'vue';
import { useMemberStore } from '@/stores/memberStore.js';
import { useRouter } from 'vue-router';
const router = useRouter();
const memberStore = useMemberStore();

import { useLogout } from '@/composables/useMemberApis.js';
const logoutApi = useLogout();

// 운영 권한(OPERATOR 신규 / ADMIN 호환)일 때만 운영 콘솔 진입점 노출
const isOperator = computed(() =>
  ['OPERATOR', 'ADMIN'].includes(memberStore.loginUser?.role)
);

// 로그아웃 성공 시 홈으로
watch(
  () => memberStore.isLoggedIn,
  (newData) => {
    if (!newData) {
      router.push({ name: 'home' });
    }
  }
);
</script>

<style scoped>
.dino-header {
  height: 64px;
  background-color: var(--color-teal);
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--color-white);
  padding: 6px 18px;
  border-radius: var(--radius-pill);
  color: var(--color-text-dark);
  font-weight: 700;
  font-size: 20px;
  text-decoration: none;
}

.dino-nav {
  display: flex;
  gap: 4px;
  background: rgba(0, 0, 0, 0.05);
  padding: 4px;
  border-radius: var(--radius-pill);
}
.dino-nav a {
  padding: 8px 18px;
  text-decoration: none;
  color: var(--color-text-light);
  font-size: 14px;
  font-weight: 500;
  border-radius: var(--radius-pill);
  transition: all 0.2s ease;
}
.dino-nav a.router-link-active {
  background: var(--color-white);
  color: var(--color-teal);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 14px;
}
.auth-link {
  color: var(--color-text-light);
  font-weight: 500;
  font-size: 14px;
  text-decoration: none;
}
.profile-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--color-text-light);
  font-weight: 500;
  font-size: 14px;
  text-decoration: none;
}
.btn-dino.sm {
  padding: 6px 16px;
  font-size: 13px;
  text-decoration: none;
}

@media (max-width: 820px) {
  .dino-nav {
    display: none;
  }
}
</style>
