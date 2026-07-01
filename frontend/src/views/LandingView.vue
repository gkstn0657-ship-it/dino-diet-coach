<template>
  <div class="landing">
    <section class="hero">
      <div class="hero-text">
        <span class="tag-teal">AI Health Platform</span>
        <h1>사진 한 장으로<br />식단을 분석하는<br />건강 코치, 냠냠코치 🦕</h1>
        <p>식약처 음식 데이터와 생성형 AI로 내 식단을 분석하고, 운동까지 코칭받으세요.</p>
        <div class="cta">
          <router-link :to="{ name: 'regist-member-form' }" class="btn-primary">시작하기</router-link>
          <router-link :to="{ name: 'login-form' }" class="btn-ghost">로그인</router-link>
        </div>
      </div>
      <div class="hero-art">🥗🦖💪</div>
    </section>

    <section class="features">
      <div v-for="f in features" :key="f.title" class="cloud-card feature">
        <div class="f-emoji">{{ f.emoji }}</div>
        <h3>{{ f.title }}</h3>
        <p>{{ f.desc }}</p>
      </div>
    </section>
  </div>
</template>

<script setup>
const features = [
  { emoji: '📸', title: 'AI 식단 분석', desc: '사진을 올리면 영양 성분과 칼로리를 자동 분석해요.' },
  { emoji: '💪', title: 'AI 운동 코칭', desc: '프로필과 식단에 맞춘 운동을 추천받으세요.' },
  { emoji: '🏆', title: '챌린지 & 커뮤니티', desc: '함께 도전하고 후기를 나누며 동기부여를 얻어요.' },
];
</script>

<style scoped>
.landing {
  max-width: 1080px;
  margin: 0 auto;
  padding: 40px 24px 80px;
}
.hero {
  display: flex;
  align-items: center;
  gap: 40px;
  background: var(--color-teal);
  border-radius: var(--radius-large);
  padding: 48px;
  color: #fff;
}
.hero-text {
  flex: 1;
}
.hero-text h1 {
  font-size: 40px;
  line-height: 1.2;
  margin: 16px 0;
}
.hero-text p {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 24px;
}
.cta {
  display: flex;
  gap: 12px;
}
.cta a {
  text-decoration: none;
}
.hero-art {
  font-size: 96px;
  white-space: nowrap;
}
.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 24px;
}
@media (max-width: 768px) {
  .hero {
    flex-direction: column;
    text-align: center;
  }
  .features {
    grid-template-columns: 1fr;
  }
}
.feature {
  text-align: center;
}
.f-emoji {
  font-size: 44px;
  margin-bottom: 12px;
}
.feature h3 {
  margin: 0 0 8px;
}
.feature p {
  font-size: 14px;
  color: var(--color-text-dark);
}
</style>
