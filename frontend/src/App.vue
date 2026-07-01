<template>
  <div class="dot-pattern"></div> 
  <app-header />
  <main class="app-main">
    <router-view />
  </main>
  <app-footer />

  <!-- 전역 가이드 챗봇 위젯 (사이트 이용 가이드 에이전트) -->
  <guide-chatbot />
</template>

<script setup>
import AppHeader from '@/components/common/AppHeader.vue';
import AppFooter from '@/components/common/AppFooter.vue';
import GuideChatbot from '@/components/common/GuideChatbot.vue';

import { watch } from 'vue';
import { useMemberStore } from '@/stores/memberStore.js';
const memberStore = useMemberStore();

// 공통 기능: alertMsg 감시
watch(
  () => memberStore.alertMsg,
  (nv) => {
    if (nv) {
      alert(nv);
      memberStore.setAlertMsg('');
    }
  }
);
</script>

<style>
.app-main {
  min-height: calc(100vh - 64px - 80px);
}
</style>
