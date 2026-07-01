<template>
  <div class="dino-page">
    <h1 class="dino-page-title">내 챌린지 현황 📊</h1>
    <p class="dino-page-subtitle">참여 중인 챌린지의 진행률을 확인하세요.</p>

    <div v-if="myChallenges.length" class="list">
      <router-link
        v-for="c in myChallenges"
        :key="c.cno"
        :to="{ name: 'challenge-detail', params: { cno: c.cno } }"
        class="cloud-card row"
      >
        <div class="emoji">{{ c.emoji }}</div>
        <div class="info">
          <div class="c-title">{{ c.title }}</div>
          <div class="bar"><div class="bar-fill" :style="{ width: c.progress + '%' }" /></div>
        </div>
        <div class="pct">{{ c.progress }}%</div>
      </router-link>
    </div>

    <div v-else class="dino-empty">
      <span class="emoji">🦕</span>
      참여 중인 챌린지가 없어요.
      <router-link :to="{ name: 'challenge-list' }">챌린지 둘러보기 →</router-link>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useFetchMyChallenges } from '@/composables/useChallengeApis';

// F113. 내 챌린지 (마운트 시 실제 로드)
const { data, execute } = useFetchMyChallenges();
const myChallenges = computed(() => data.value ?? []);
execute();
</script>

<style scoped>
.list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.row {
  display: flex;
  align-items: center;
  gap: 18px;
  text-decoration: none;
  color: inherit;
}
.emoji {
  font-size: 36px;
}
.info {
  flex: 1;
}
.c-title {
  font-weight: 700;
  margin-bottom: 8px;
}
.bar {
  height: 12px;
  background: var(--color-beige);
  border-radius: var(--radius-pill);
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: var(--color-teal);
}
.pct {
  font-weight: 700;
  color: var(--color-teal);
  font-size: 18px;
}
</style>
