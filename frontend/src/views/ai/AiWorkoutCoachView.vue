<template>
  <div class="dino-page">
    <h1 class="dino-page-title">AI 운동 코칭 💪</h1>
    <p class="dino-page-subtitle">목표에 맞는 코치를 선택하고 맞춤 운동을 추천받으세요.</p>

    <!-- 코치 선택 -->
    <div class="coach-picker">
      <button
        v-for="c in coaches"
        :key="c.key"
        type="button"
        class="coach-card"
        :class="{ active: c.key === currentCoach.key }"
        :disabled="isLoading || isHistoryLoading"
        @click="selectCoach(c)"
      >
        <span class="coach-card-emoji">{{ c.emoji }}</span>
        <span class="coach-card-name">{{ c.name }}</span>
        <span class="coach-card-desc">{{ c.desc }}</span>
      </button>
    </div>

    <!-- 코치가 도와줄 수 있는 것 (Tool 에이전트 기반 기능 안내) -->
    <div class="coach-guide">
      <button
        type="button"
        class="coach-guide-toggle"
        :aria-expanded="showGuide"
        @click="showGuide = !showGuide"
      >
        <span>💡 이 코치가 도와줄 수 있는 것</span>
        <span class="coach-guide-chevron" :class="{ open: showGuide }">⌄</span>
      </button>

      <transition name="guide-fade">
        <div v-show="showGuide" class="coach-guide-body">
          <p class="coach-guide-lead">
            코치는 회원님의 <b>실제 식단·영양·건강 데이터</b>를 직접 조회해서 답해요.
            아래처럼 물어보면 더 정확하게 코칭해 드려요. 👇
          </p>

          <div class="guide-grid">
            <div v-for="cap in capabilities" :key="cap.title" class="guide-cap">
              <div class="guide-cap-icon">{{ cap.icon }}</div>
              <div class="guide-cap-text">
                <div class="guide-cap-title">{{ cap.title }}</div>
                <div class="guide-cap-desc">{{ cap.desc }}</div>
                <button type="button" class="guide-cap-example" @click="askExample(cap.example)">
                  “{{ cap.example }}”
                </button>
              </div>
            </div>
          </div>

          <p class="coach-guide-foot">
            예시 문구를 누르면 바로 코치에게 질문할 수 있어요.
          </p>
        </div>
      </transition>
    </div>

    <div class="coach-shell">
      <div class="coach-head">
        <div class="coach-avatar">{{ currentCoach.emoji }}</div>
        <div>
          <div class="coach-name">{{ currentCoach.name }}</div>
          <div class="label-caps" style="color: rgba(255, 255, 255, 0.7); font-size: 9px">
            {{ currentCoach.tagline }}
          </div>
        </div>
      </div>

      <div ref="scroll" class="chat-area">
        <div v-for="(m, i) in messages" :key="i" class="chat-bubble" :class="m.role">
          <span v-if="m.role === 'user'">{{ m.text }}</span>
          <div v-else class="md" v-html="renderMarkdown(m.text)" />
        </div>
        <div v-if="isLoading" class="chat-bubble coach typing">코치가 입력 중...</div>
      </div>

      <form class="chat-input" @submit.prevent="send">
        <input v-model="draft" type="text" placeholder="오늘 어떤 운동을 할까요?" :disabled="isLoading" />
        <button type="submit" class="btn-primary" :disabled="isLoading">전송</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue';
import { useAiWorkoutCoach, useAiChatHistory } from '@/composables/useAiApis';
import { renderMarkdown } from '@/utils/markdown';

// 3종 코치 에이전트 (백엔드 coach 키와 일치해야 함)
const coaches = [
  {
    key: 'powerrex',
    emoji: '🦖',
    name: 'PowerRex',
    tagline: 'Bulk-up Coach',
    desc: '근육 증가 · 벌크업',
    greeting: '왔구나! 나는 벌크업 코치 PowerRex야. 오늘 식단 기준으로 빡세게 코칭해줄게! 🦖',
  },
  {
    key: 'slimdino',
    emoji: '🦕',
    name: 'SlimDino',
    tagline: 'Diet Coach',
    desc: '체중 감량 · 다이어트',
    greeting: '안녕하세요, 다이어트 코치 SlimDino예요. 무리하지 않고 꾸준히, 함께 가볍게 시작해 봐요! 🦕',
  },
  {
    key: 'balanceno',
    emoji: '🐢',
    name: 'BalanceNo',
    tagline: 'Maintenance Coach',
    desc: '건강 유지 · 균형 식단',
    greeting: '안녕하세요, 건강 유지 코치 BalanceNo입니다. 강도보다 꾸준함이 중요합니다. 천천히 시작해 볼까요? 🐢',
  },
];

// 코치가 Tool 콜링으로 제공하는 기능 안내 (백엔드 @Tool 에이전트 기반)
const capabilities = [
  {
    icon: '🍱',
    title: '오늘 식단·영양 상태 분석',
    desc: '오늘 먹은 끼니와 총 섭취 칼로리·단백질, 목표 대비 남은 양을 직접 조회해 조언해요.',
    example: '오늘 단백질 충분히 먹었어?',
  },
  {
    icon: '📅',
    title: '특정 날짜 / 주간 패턴 점검',
    desc: '지정한 날짜나 최근 7일 식단 패턴을 분석해 부족·초과 영양소를 짚어줘요.',
    example: '이번 주 내 식단 패턴 봐줘',
  },
  {
    icon: '🔍',
    title: '음식 영양정보 조회',
    desc: '식약처 식품 DB에서 음식의 칼로리·단백질·탄수·지방을 찾아 알려줘요. (추측 X)',
    example: '닭가슴살 100g 영양정보 알려줘',
  },
  {
    icon: '🧬',
    title: '내 건강 프로필 기반 맞춤 추천',
    desc: '키·몸무게·목표·질환 정보를 반영해 나에게 맞는 운동·식단을 추천해요.',
    example: '내 목표에 맞는 오늘 운동 추천해줘',
  },
  {
    icon: '🏅',
    title: '챌린지 진행 상태 확인',
    desc: '참여 중인 챌린지의 진행률과 오늘 조건 충족·인증 여부를 확인해줘요.',
    example: '내 챌린지 오늘 조건 충족했어?',
  },
  {
    icon: '🕒',
    title: '오늘 기준 실시간 코칭',
    desc: '서버 기준 현재 날짜·시간을 확인해 "오늘", "이번 주" 같은 질문도 정확히 답해요.',
    example: '오늘 더 먹어도 되는 칼로리 남았어?',
  },
];

const showGuide = ref(true);
const currentCoach = ref(coaches[0]);
const draft = ref('');
const scroll = ref(null);
const messages = ref([{ role: 'coach', text: coaches[0].greeting }]);

// F117. AI 운동 코칭 API 연동 (히스토리는 서버 DB에 저장·복원)
const { execute, isLoading, error } = useAiWorkoutCoach();
const { execute: fetchHistory, isLoading: isHistoryLoading } = useAiChatHistory();

// 코치별 저장된 대화 복원 — 기록이 없으면 인사말 표시
const loadHistory = async (c) => {
  const saved = await fetchHistory(c.key);
  if (saved?.length) {
    messages.value = saved.map((m) => ({
      role: m.role === 'user' ? 'user' : 'coach',
      text: m.content,
    }));
  } else {
    messages.value = [{ role: 'coach', text: c.greeting }];
  }
  await scrollDown(true);
};

onMounted(() => loadHistory(currentCoach.value));

const selectCoach = async (c) => {
  if (c.key === currentCoach.value.key) return;
  currentCoach.value = c;
  await loadHistory(c); // 코치 전환 시 해당 코치의 저장된 대화 로드
};

// 가이드 예시 문구를 클릭하면 입력창에 채우고 바로 전송
const askExample = async (text) => {
  if (isLoading.value) return;
  draft.value = text;
  await send();
};

const send = async () => {
  const text = draft.value.trim();
  if (!text) return;
  messages.value.push({ role: 'user', text });
  draft.value = '';
  // 질문 전송 시점에는 항상 바닥으로 (사용자가 방금 보낸 메시지를 보여줘야 하므로 강제 이동)
  await scrollDown(true);

  // LLM 응답이 늦거나 실패해도 화면이 멈추지 않도록 예외 상황을 명확히 안내
  const reply = await execute(text, currentCoach.value.key);

  if (error.value) {
    // 네트워크/타임아웃 등으로 응답을 못 받은 경우 (서버는 살아있지만 응답이 없음)
    const isTimeout =
      error.value?.code === 'ECONNABORTED' ||
      /timeout/i.test(error.value?.message ?? '');
    messages.value.push({
      role: 'coach',
      text: isTimeout
        ? '⏳ 코치가 생각하는 데 시간이 너무 오래 걸려 응답을 받지 못했어요. 잠시 후 다시 질문해 주세요.'
        : '⚠️ 일시적인 문제로 응답을 받지 못했어요. 잠시 후 다시 시도해 주세요.',
    });
  } else {
    messages.value.push({
      role: 'coach',
      text: reply ?? '코치 응답을 받지 못했어요. 잠시 후 다시 시도해 주세요.',
    });
  }
  await scrollDown(true);
};

// 사용자가 위로 스크롤해 과거 대화를 읽는 중인지 판단 (바닥 근처면 자동 스크롤 허용)
const isNearBottom = () => {
  const el = scroll.value;
  if (!el) return true;
  return el.scrollHeight - el.scrollTop - el.clientHeight < 80;
};

// 바닥으로 스크롤. force=true면 무조건, 아니면 사용자가 바닥 근처일 때만 이동
// 마크다운(v-html) 렌더로 높이가 늦게 확정되는 경우를 대비해 rAF로 한 번 더 보정
const scrollDown = async (force = false) => {
  if (!force && !isNearBottom()) return;
  await nextTick();
  const el = scroll.value;
  if (!el) return;
  el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
  requestAnimationFrame(() => {
    el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
  });
};
</script>

<style scoped>
.coach-picker {
  max-width: 720px;
  margin: 0 auto 16px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.coach-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px 10px;
  background: #fff;
  border: 2px solid transparent;
  border-radius: var(--radius-large);
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.15s, transform 0.15s;
}
.coach-card:hover:not(:disabled) {
  transform: translateY(-2px);
}
.coach-card.active {
  border-color: var(--color-teal);
  background: var(--color-tan);
}
.coach-card:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.coach-card-emoji {
  font-size: 28px;
}
.coach-card-name {
  font-weight: 700;
  font-size: 14px;
  color: var(--color-text-dark);
}
.coach-card-desc {
  font-size: 11px;
  color: var(--color-text-dark);
  opacity: 0.7;
}
/* ===== 코치 기능 가이드 ===== */
.coach-guide {
  max-width: 720px;
  margin: 0 auto 16px;
}
.coach-guide-toggle {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px;
  background: var(--color-tan);
  border: 2px solid var(--color-teal);
  border-radius: var(--radius-large);
  cursor: pointer;
  font-family: inherit;
  font-weight: 700;
  font-size: 14px;
  color: var(--color-text-dark);
  transition: background 0.15s;
}
.coach-guide-toggle:hover {
  background: #f3e9d6;
}
.coach-guide-chevron {
  font-size: 18px;
  transition: transform 0.2s;
}
.coach-guide-chevron.open {
  transform: rotate(180deg);
}
.coach-guide-body {
  margin-top: 10px;
  padding: 18px;
  background: #fff;
  border: 1px solid var(--color-tan);
  border-radius: var(--radius-large);
}
.coach-guide-lead {
  margin: 0 0 16px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--color-text-dark);
}
.guide-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.guide-cap {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: var(--color-tan);
  border-radius: var(--radius-large);
}
.guide-cap-icon {
  font-size: 22px;
  line-height: 1;
  flex-shrink: 0;
}
.guide-cap-title {
  font-weight: 700;
  font-size: 13px;
  color: var(--color-text-dark);
  margin-bottom: 3px;
}
.guide-cap-desc {
  font-size: 12px;
  line-height: 1.45;
  color: var(--color-text-dark);
  opacity: 0.8;
  margin-bottom: 8px;
}
.guide-cap-example {
  display: inline-block;
  max-width: 100%;
  padding: 5px 12px;
  background: #fff;
  border: 1px solid var(--color-teal);
  border-radius: var(--radius-pill);
  color: var(--color-teal-dark);
  font-family: inherit;
  font-size: 11.5px;
  font-weight: 600;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s, color 0.15s;
}
.guide-cap-example:hover {
  background: var(--color-teal);
  color: #fff;
}
.coach-guide-foot {
  margin: 14px 0 0;
  font-size: 11.5px;
  color: var(--color-text-dark);
  opacity: 0.6;
  text-align: center;
}
.guide-fade-enter-active,
.guide-fade-leave-active {
  transition: opacity 0.2s ease;
}
.guide-fade-enter-from,
.guide-fade-leave-to {
  opacity: 0;
}
@media (max-width: 560px) {
  .guide-grid {
    grid-template-columns: 1fr;
  }
}

.coach-shell {
  max-width: 720px;
  margin: 0 auto;
  background: var(--color-teal);
  border-radius: var(--radius-large);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 70vh;
}
.coach-head {
  background: var(--color-teal-dark);
  padding: 18px 24px;
  display: flex;
  align-items: center;
  gap: 14px;
  color: #fff;
}
.coach-avatar {
  width: 52px;
  height: 52px;
  background: #fff;
  border-radius: 50%;
  border: 4px solid var(--color-tan);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.coach-name {
  font-weight: 700;
  font-size: 18px;
}
.chat-area {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
}
.chat-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.4;
  max-width: 80%;
}
.chat-bubble.coach {
  background: #fff;
  color: var(--color-text-dark);
  align-self: flex-start;
  border-bottom-left-radius: 4px;
}
.chat-bubble.user {
  background: var(--color-teal-dark);
  color: #fff;
  align-self: flex-end;
  border-bottom-right-radius: 4px;
}
.typing {
  opacity: 0.7;
  font-style: italic;
}
.chat-input {
  display: flex;
  gap: 10px;
  padding: 16px;
  background: var(--color-teal-dark);
}
.chat-input input {
  flex: 1;
  border: none;
  border-radius: var(--radius-pill);
  padding: 12px 20px;
  font-family: inherit;
  outline: none;
}
</style>
