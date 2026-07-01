<template>
  <!-- 전역 가이드 챗봇: 사이트 이용 가이드 AI 에이전트 (위젯형) -->
  <div class="guide-chatbot">
    <!-- 토글 버튼 -->
    <button class="fab" :class="{ open }" @click="open = !open" aria-label="가이드 챗봇 열기">
      <span v-if="!open">🦕</span>
      <span v-else>✕</span>
    </button>

    <!-- 패널 -->
    <transition name="pop">
      <div v-if="open" class="panel">
        <div class="panel-head">
          <div class="coach-avatar">🦕</div>
          <div>
            <div class="title">가이드 디노</div>
            <div class="label-caps" style="color: rgba(255, 255, 255, 0.75); font-size: 9px">
              Site Guide AI
            </div>
          </div>
        </div>

        <div ref="bodyEl" class="panel-body">
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="bubble"
            :class="msg.role"
          >
            <span v-if="msg.role === 'user'">{{ msg.text }}</span>
            <div v-else class="md" v-html="renderMarkdown(msg.text)" />
          </div>
        </div>

        <form class="panel-input" @submit.prevent="handleSend">
          <input
            v-model="draft"
            type="text"
            placeholder="이용 방법을 물어보세요..."
            :disabled="isLoading"
          />
          <button type="submit" :disabled="isLoading">↑</button>
        </form>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue';
import { useGuideChat } from '@/composables/useAiApis';
import { renderMarkdown } from '@/utils/markdown';

const open = ref(false);
const draft = ref('');
const isLoading = ref(false);
const bodyEl = ref(null);

// 최신 메시지로 스크롤 (markdown v-html 렌더로 높이가 늦게 확정되는 경우까지 보정)
const scrollToBottom = () => {
  nextTick(() => {
    const el = bodyEl.value;
    if (el) el.scrollTop = el.scrollHeight;
    setTimeout(() => {
      if (bodyEl.value) bodyEl.value.scrollTop = bodyEl.value.scrollHeight;
    }, 80);
  });
};

// 패널을 열면 맨 아래(최신)로
watch(open, (v) => {
  if (v) scrollToBottom();
});
const messages = ref([
  {
    role: 'bot',
    text:
      '안녕하세요! 냠냠코치 사용법을 도와드리는 가이드 디노예요 🦕\n\n' +
      '이런 것들을 물어보실 수 있어요:\n' +
      '- 🍱 **식단** — 기록(사진 AI 분석)·수정·삭제 방법\n' +
      '- 📊 **AI 분석** — 식단 점수·피드백 받는 법\n' +
      '- 💪 **AI 코칭** — 코치 선택, 대화 저장·기억 범위\n' +
      '- 🏆 **챌린지** — 참여·일일 인증 방법\n' +
      '- 💬 **커뮤니티** — 글·댓글 작성, 팔로우\n' +
      '- 👤 **내 정보** — 프로필(키·몸무게·목표) 수정\n\n' +
      '무엇이 궁금하세요?',
  },
]);

const { execute } = useGuideChat();

const handleSend = async () => {
  const text = draft.value.trim();
  if (!text) return;
  messages.value.push({ role: 'user', text });
  draft.value = '';
  scrollToBottom();
  isLoading.value = true;
  try {
    const answer = await execute(text);
    messages.value.push({
      role: 'bot',
      text: answer ?? '(가이드 응답 자리 — API 연동 예정)',
    });
    scrollToBottom();
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.guide-chatbot {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 200;
}
.fab {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  border: 4px solid var(--color-tan);
  background: var(--color-teal);
  color: #fff;
  font-size: 40px;
  cursor: pointer;
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.22);
  transition: transform 0.15s, box-shadow 0.15s;
  animation: fab-pulse 2.4s ease-in-out infinite;
}
.fab:hover {
  transform: scale(1.06);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.28);
}
.fab:active {
  transform: scale(0.94);
}
/* 패널이 열렸을 땐 시선 분산을 막기 위해 펄스 정지 */
.fab.open {
  animation: none;
}
@keyframes fab-pulse {
  0%,
  100% {
    box-shadow: 0 8px 22px rgba(0, 0, 0, 0.22), 0 0 0 0 rgba(38, 166, 154, 0.45);
  }
  50% {
    box-shadow: 0 8px 22px rgba(0, 0, 0, 0.22), 0 0 0 14px rgba(38, 166, 154, 0);
  }
}
.panel {
  position: absolute;
  right: 0;
  bottom: 98px;
  width: 320px;
  height: 440px;
  background: var(--color-teal);
  border-radius: var(--radius-medium);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.22);
}
.panel-head {
  background: var(--color-teal-dark);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
}
.coach-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--color-white);
  border: 3px solid var(--color-tan);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.title {
  font-weight: 700;
}
.panel-body {
  flex: 1;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
}
.bubble {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 13px;
  line-height: 1.4;
  max-width: 85%;
}
.bubble.bot {
  background: var(--color-white);
  color: var(--color-text-dark);
  align-self: flex-start;
  border-bottom-left-radius: 4px;
}
.bubble.user {
  background: var(--color-teal-dark);
  color: #fff;
  align-self: flex-end;
  border-bottom-right-radius: 4px;
}
.panel-input {
  display: flex;
  gap: 8px;
  padding: 12px;
  background: var(--color-teal-dark);
}
.panel-input input {
  flex: 1;
  border: none;
  border-radius: var(--radius-pill);
  padding: 10px 16px;
  font-family: inherit;
  outline: none;
}
.panel-input button {
  width: 40px;
  border: none;
  border-radius: 50%;
  background: var(--color-red);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}
.pop-enter-active,
.pop-leave-active {
  transition: all 0.18s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}
</style>
