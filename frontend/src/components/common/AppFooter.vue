<template>
  <footer class="dino-footer">
    <div class="logo-mini">🦕 DinoDiet · 냠냠코치</div>
    <div class="muted">식단 분석을 활용한 개인 맞춤형 건강 코칭 서비스 · SSAFY Java Track PJT</div>
  </footer>
</template>

<script setup></script>

<style scoped>
.dino-footer {
  min-height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 20px;
  color: var(--color-tan);
}
.logo-mini {
  font-weight: 700;
  color: var(--color-teal-dark);
}
.muted {
  font-size: 12px;
}
</style>
