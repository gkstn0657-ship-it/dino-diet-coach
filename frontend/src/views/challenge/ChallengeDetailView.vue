<template>
  <div class="dino-page">
    <div class="hero" :style="{ background: challenge.color }">
      <div class="hero-emoji">{{ challenge.emoji }}</div>
      <div class="hero-info">
        <span class="tag-red">{{ challenge.status }}</span>
        <span v-if="challenge.dday" class="hero-dday">{{ challenge.dday }}</span>
        <h1 class="hero-title">{{ challenge.title }}</h1>
        <p>{{ challenge.period }} · 👥 {{ challenge.participants }}명 참여 중</p>
      </div>
      <!-- 챌린지 삭제는 운영자(운영 콘솔)만 가능 — 일반 사용자에게 노출하지 않음 -->
    </div>

    <div class="dino-grid dino-grid-2" style="margin-top: 20px">
      <div class="cloud-card">
        <div class="label-caps">챌린지 설명</div>
        <p class="desc">{{ challenge.desc }}</p>

        <!-- 식단 연동 인증 조건 -->
        <div v-if="challenge.condLabel" class="cond-box" :class="{ ok: challenge.condMet }">
          <div class="cond-title">🎯 인증 조건: {{ challenge.condLabel }}</div>
          <div class="cond-status">
            {{
              challenge.condMet
                ? (challenge.phase === 'LIVE' ? '✅ 오늘 조건 달성! 자동으로 인증돼요' : '✅ 오늘 조건 충족 (시작일부터 인증)')
                : '⏳ ' + (challenge.condStatus || '조건 미달성')
            }}
          </div>
          <router-link
            v-if="!challenge.condMet"
            :to="{ name: 'diet-write' }"
            class="cond-link"
          >🍱 식단 기록하러 가기 →</router-link>
        </div>
        <template v-if="!challenge.isJoined">
          <button
            v-if="challenge.phase === '모집중'"
            class="btn-primary block"
            :disabled="joining"
            @click="join"
          >
            {{ joining ? '참여 중...' : '🏆 챌린지 참여하기' }}
          </button>
          <div v-else class="auto-note">
            {{ challenge.phase === '종료' ? '🏁 종료된 챌린지예요' : '🚩 이미 진행 중이에요 — 모집 기간에만 참여할 수 있어요' }}
          </div>
        </template>

        <!-- 조건 챌린지: 버튼 없이 자동 인증 (진행중에만 평가) -->
        <div v-else-if="challenge.condLabel" class="auto-note" :class="{ ok: challenge.checkedToday }">
          {{
            challenge.phase === '모집중'
              ? '🗓 아직 시작 전이에요 — 시작일부터 자동 인증돼요'
              : challenge.phase === '종료'
                ? '🏁 종료된 챌린지예요'
                : challenge.progress >= 100
                  ? '🎉 챌린지 완료!'
                  : challenge.checkedToday
                    ? '🤖 오늘 자동 인증 완료! 내일도 조건을 지켜봐요'
                    : '🤖 자동 인증 챌린지 — 오늘 식단이 조건을 충족하면 자동으로 기록돼요'
          }}
        </div>

        <!-- 일반 챌린지: 진행중에만 버튼 인증 -->
        <template v-else>
          <div v-if="challenge.phase === '모집중'" class="auto-note">
            🗓 아직 시작 전이에요 — 챌린지 시작일부터 인증할 수 있어요.
          </div>
          <div v-else-if="challenge.phase === '종료'" class="auto-note">
            🏁 종료된 챌린지예요.
          </div>
          <button
            v-else
            class="btn-teal block"
            :disabled="challenge.progress >= 100 || challenge.checkedToday || checking"
            @click="check"
          >
            {{
              challenge.progress >= 100
                ? '🎉 챌린지 완료!'
                : challenge.checkedToday
                  ? '✅ 오늘 인증 완료 (내일 또 만나요)'
                  : checking
                    ? '인증 중...'
                    : '✅ 오늘 인증하기'
            }}
          </button>
        </template>
        <button
          v-if="challenge.isJoined && challenge.phase === '모집중'"
          class="btn-ghost block leave-btn"
          :disabled="leaving"
          @click="leave"
        >
          {{ leaving ? '취소 중...' : '참여 취소' }}
        </button>
        <p v-if="challenge.isJoined" class="joined-note">참여중인 챌린지예요 · 진행 {{ challenge.progress }}%</p>
      </div>

      <!-- 달성도 (F113) -->
      <div class="cloud-card">
        <div class="label-caps">내 달성도</div>
        <div
          class="ring-big"
          :style="{
            background: `conic-gradient(var(--color-teal) ${challenge.progress}%, var(--color-beige) ${challenge.progress}% 100%)`,
          }"
        >
          <span>{{ challenge.progress }}%</span>
        </div>
        <p class="progress-text">{{ challenge.doneDays }} / {{ challenge.totalDays }}일 달성</p>
      </div>
    </div>

    <!-- 리더보드: 달성률 Top 10 -->
    <div class="cloud-card" style="margin-top: 20px">
      <div class="label-caps">🏅 리더보드 (달성률 Top 10)</div>
      <ul v-if="challenge.leaderboard?.length" class="board">
        <li
          v-for="(r, i) in challenge.leaderboard"
          :key="i"
          class="board-row"
          :class="{ me: r.isMe }"
        >
          <span class="rank">{{ medal(i) }}</span>
          <span class="name">{{ r.name }}<b v-if="r.isMe"> (나)</b></span>
          <div class="bar"><div class="bar-fill" :style="{ width: (r.progress ?? 0) + '%' }" /></div>
          <span class="pct">{{ r.progress ?? 0 }}%</span>
          <span class="days">{{ r.doneDays ?? 0 }}일</span>
        </li>
      </ul>
      <p v-else class="board-empty">아직 참여자가 없어요. 첫 주자가 되어보세요! 🏃</p>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  useFetchChallengeDetail,
  useJoinChallenge,
  useLeaveChallenge,
  useCheckChallenge,
} from '@/composables/useChallengeApis';

const route = useRoute();
const router = useRouter();
const cno = computed(() => route.params.cno);

// 리더보드 순위 표시 (1~3위 메달)
const medal = (i) => (i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : `${i + 1}`);

// F112. 챌린지 상세 (실제 로드)
const { data, execute } = useFetchChallengeDetail();
const challenge = computed(
  () => data.value ?? { emoji: '🏆', color: '#77C0B3', status: '', title: '', period: '', participants: 0, desc: '', isJoined: false, checkedToday: false, progress: 0, doneDays: 0, totalDays: 0 }
);

// F113. 자유 참여 — 조건·서약 없음. 인증은 식단 조건 충족 시 자동 기록
const joining = ref(false);
const { execute: joinChallenge } = useJoinChallenge();
const join = async () => {
  joining.value = true;
  try {
    await joinChallenge(cno.value);
    await execute(cno.value);
  } finally {
    joining.value = false;
  }
};

// F113. 참여 취소 (모집중에만)
const leaving = ref(false);
const { execute: leaveChallenge } = useLeaveChallenge();
const leave = async () => {
  if (!confirm('이 챌린지 참여를 취소할까요?')) return;
  leaving.value = true;
  try {
    await leaveChallenge(cno.value);
    await execute(cno.value);
  } finally {
    leaving.value = false;
  }
};

// F113. 오늘 인증 → 달성도 증가
const { execute: checkChallenge } = useCheckChallenge();
const checking = ref(false);
const check = async () => {
  checking.value = true;
  try {
    await checkChallenge(cno.value);
    await execute(cno.value);
  } finally {
    checking.value = false;
  }
};

watch(cno, (v) => v && execute(v), { immediate: true });
</script>

<style scoped>
.leave-btn {
  margin-top: 8px;
  color: var(--color-red, #d83d52);
}
.hero {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 32px;
  border-radius: var(--radius-large);
  color: #fff;
}
.hero-emoji {
  font-size: 72px;
}
.hero-title {
  font-size: 32px;
  margin: 6px 0;
}
.desc {
  font-size: 14px;
  line-height: 1.6;
  margin: 12px 0 20px;
}
.block {
  display: block;
  width: 100%;
}
.joined-note {
  text-align: center;
  font-size: 12px;
  color: var(--color-tan);
  font-weight: 600;
  margin-top: 10px;
}
.ring-big {
  position: relative;
  width: 120px;
  height: 120px;
  border: none; /* 장식용 테두리 제거 — 진행률 색 채움(conic)만 보이게 */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 16px auto;
  transition: background 0.4s ease;
}
/* 색 채움이 그림 영역을 차지하도록 가운데만 흰 구멍(도넛) */
.ring-big::before {
  content: '';
  position: absolute;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: var(--color-white);
}
.ring-big span {
  position: relative;
  font-size: 28px;
  font-weight: 700;
  color: var(--color-teal);
}
.progress-text {
  text-align: center;
  font-weight: 600;
}

/* 식단 연동 조건 박스 */
.cond-box {
  background: var(--color-beige);
  border-left: 5px solid var(--color-red);
  border-radius: var(--radius-small);
  padding: 12px 16px;
  margin-bottom: 16px;
}
.cond-box.ok {
  border-left-color: var(--color-teal);
}
.cond-title {
  font-weight: 700;
  font-size: 13px;
}
.cond-status {
  font-size: 13px;
  margin-top: 4px;
}
.cond-link {
  display: inline-block;
  margin-top: 8px;
  font-size: 12px;
  font-weight: 700;
  color: var(--color-teal-dark);
  text-decoration: none;
}

/* hero 보강 */
.hero-info {
  flex: 1;
}
.hero-dday {
  margin-left: 8px;
  font-size: 13px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.25);
  padding: 3px 10px;
  border-radius: var(--radius-pill);
}
.hero-del {
  align-self: flex-start;
}

/* 리더보드 */
.board {
  list-style: none;
  margin: 12px 0 0;
  padding: 0;
}
.board-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px;
  border-bottom: 2px dashed var(--color-beige);
  font-size: 14px;
}
.board-row:last-child {
  border-bottom: none;
}
.board-row.me {
  background: var(--color-beige);
  border-radius: var(--radius-small);
}
.rank {
  width: 32px;
  text-align: center;
  font-weight: 800;
}
.name {
  width: 140px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.board-row .bar {
  flex: 1;
  height: 10px;
  background: var(--color-beige);
  border-radius: 5px;
  overflow: hidden;
}
.board-row .bar-fill {
  height: 100%;
  background: var(--color-teal);
  border-radius: 5px;
}
.pct {
  width: 48px;
  text-align: right;
  font-weight: 700;
  color: var(--color-teal-dark);
}
.days {
  width: 40px;
  text-align: right;
  font-size: 12px;
  color: var(--color-text-dark);
  opacity: 0.7;
}
.board-empty {
  margin: 14px 0 4px;
  font-size: 13px;
  color: var(--color-text-dark);
  opacity: 0.7;
}
/* 자동 인증 상태 패널 (조건 챌린지는 버튼 대신 표시) */
.auto-note {
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  padding: 12px 16px;
  border-radius: var(--radius-small);
  background: var(--color-beige);
  color: var(--color-text-dark);
}
.auto-note.ok {
  background: var(--color-teal);
  color: #fff;
}
</style>
