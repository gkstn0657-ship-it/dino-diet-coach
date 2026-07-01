<template>
  <div class="ops">
    <!-- 사이드바 -->
    <aside class="ops-side">
      <div class="ops-brand">
        <span class="ops-logo">🦕</span>
        <div>
          <div class="ops-title">운영 콘솔</div>
          <div class="ops-sub">DinoDiet Operations</div>
        </div>
      </div>
      <nav class="ops-nav">
        <router-link :to="{ name: 'ops-dashboard' }" class="ops-link" exact-active-class="on">
          <span class="ic">📊</span> 대시보드
        </router-link>
        <router-link :to="{ name: 'ops-members' }" class="ops-link" active-class="on">
          <span class="ic">👥</span> 회원 운영
        </router-link>
        <router-link :to="{ name: 'ops-challenges' }" class="ops-link" active-class="on">
          <span class="ic">🏆</span> 챌린지 운영
        </router-link>
        <router-link :to="{ name: 'ops-posts' }" class="ops-link" active-class="on">
          <span class="ic">💬</span> 게시글 운영
        </router-link>
      </nav>
      <router-link :to="{ name: 'home' }" class="ops-exit">← 서비스로 돌아가기</router-link>
    </aside>

    <!-- 본문 -->
    <main class="ops-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
// 레이아웃 전용 — 데이터 없음. 권한은 라우터 가드 + 백엔드에서 검사.
</script>

<style scoped>
.ops {
  display: grid;
  grid-template-columns: 232px 1fr;
  gap: var(--space-5);
  max-width: 1320px;
  margin: 0 auto;
  padding: var(--space-6);
  min-height: calc(100vh - 64px - 80px);
  align-items: start;
}

/* 사이드바 */
.ops-side {
  position: sticky;
  top: 84px;
  background: var(--surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.ops-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-soft);
}
.ops-logo {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--surface-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.ops-title {
  font-size: var(--fs-card-title);
  font-weight: 800;
  color: var(--text-strong);
}
.ops-sub {
  font-size: var(--fs-label);
  color: var(--text-muted);
  font-weight: 600;
}
.ops-nav {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}
.ops-link {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 10px var(--space-3);
  border-radius: var(--radius-md);
  text-decoration: none;
  color: var(--text);
  font-weight: 600;
  font-size: var(--fs-body-sm);
}
.ops-link .ic {
  font-size: 15px;
}
.ops-link:hover {
  background: var(--surface-muted);
}
.ops-link.on {
  background: rgba(119, 192, 179, 0.18);
  color: var(--accent-dark);
  font-weight: 800;
}
.ops-exit {
  margin-top: auto;
  font-size: var(--fs-body-sm);
  color: var(--text-muted);
  text-decoration: none;
  font-weight: 600;
}
.ops-exit:hover {
  color: var(--text);
}

@media (max-width: 900px) {
  .ops {
    grid-template-columns: 1fr;
  }
  .ops-side {
    position: static;
  }
  .ops-nav {
    flex-direction: row;
    flex-wrap: wrap;
  }
  .ops-exit {
    margin-top: 0;
  }
}
</style>
