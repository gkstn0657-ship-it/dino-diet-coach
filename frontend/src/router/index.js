// 냠냠코치 · 전체 페이지 라우팅
// - 공개(/member): 로그인/회원가입/랜딩
// - 인증 필요(/auth): 식단/AI/챌린지/커뮤니티/소셜/마이페이지
import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ===== 공통 / 진입 =====
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
    },
    {
      path: '/landing',
      name: 'landing',
      component: () => import('@/views/LandingView.vue'),
    },

    // ===== 회원 / 인증 (F106~F110) - 공개 =====
    {
      path: '/member', // 대표 endpoint - 실제 component는 children에서 지정
      children: [
        {
          path: 'login-form',
          name: 'login-form',
          component: () => import('@/views/member/LoginFormView.vue'),
        },
        {
          path: 'regist-member-form',
          name: 'regist-member-form',
          component: () => import('@/views/member/MemberRegistFormView.vue'),
        },
        {
          path: 'forgot-password',
          name: 'forgot-password',
          component: () => import('@/views/member/ForgotPasswordView.vue'),
        },
        {
          path: 'reset-password',
          name: 'reset-password',
          component: () => import('@/views/member/ResetPasswordView.vue'),
        },
      ],
    },

    // ===== 인증 필요 영역 =====
    {
      path: '/auth',
      meta: { requiresAuth: true },
      children: [
        // --- 마이페이지 / 회원 (F107~F109) ---
        {
          path: 'mypage',
          name: 'mypage',
          component: () => import('@/views/member/MemberDetailView.vue'),
        },
        {
          // 다른 회원 상세도 같은 컴포넌트로 표시 (?email= 쿼리)
          path: 'member-detail',
          name: 'member-detail',
          component: () => import('@/views/member/MemberDetailView.vue'),
        },
        {
          path: 'member-modify-form',
          name: 'member-modify-form',
          component: () => import('@/views/member/MemberModifyFormView.vue'),
        },
        {
          path: 'member-list',
          name: 'member-list',
          meta: { requiresOperator: true }, // 회원목록은 운영자 전용
          component: () => import('@/views/member/MemberListView.vue'),
        },

        // --- 식단 (F101~F105) ---
        {
          path: 'diet',
          name: 'diet-list',
          component: () => import('@/views/diet/DietListView.vue'),
        },
        {
          path: 'diet/write',
          name: 'diet-write',
          component: () => import('@/views/diet/DietWriteFormView.vue'),
        },
        {
          path: 'diet/:dno',
          name: 'diet-detail',
          component: () => import('@/views/diet/DietDetailView.vue'),
        },
        {
          path: 'diet/:dno/modify',
          name: 'diet-modify',
          component: () => import('@/views/diet/DietModifyFormView.vue'),
        },

        // --- 통계 (칼로리 추이) ---
        {
          path: 'stats/calorie-trend',
          name: 'calorie-trend',
          component: () => import('@/views/stats/CalorieTrendView.vue'),
        },

        // --- AI 에이전트 (F116, F117) ---
        {
          path: 'ai/diet-analysis',
          name: 'ai-diet-analysis',
          component: () => import('@/views/ai/AiDietAnalysisView.vue'),
        },
        {
          path: 'ai/workout-coach',
          name: 'ai-workout-coach',
          component: () => import('@/views/ai/AiWorkoutCoachView.vue'),
        },
        {
          path: 'ai/chat',
          name: 'ai-chat',
          component: () => import('@/views/ai/AiChatView.vue'),
        },

        // --- 챌린지 (F112, F113) ---
        {
          path: 'challenge',
          name: 'challenge-list',
          component: () => import('@/views/challenge/ChallengeListView.vue'),
        },
        {
          path: 'challenge/create',
          name: 'challenge-create',
          component: () => import('@/views/challenge/ChallengeCreateFormView.vue'),
        },
        {
          path: 'challenge/my',
          name: 'my-challenge',
          component: () => import('@/views/challenge/MyChallengeView.vue'),
        },
        {
          path: 'challenge/:cno',
          name: 'challenge-detail',
          component: () => import('@/views/challenge/ChallengeDetailView.vue'),
        },

        // --- 커뮤니티 (F114, F115) ---
        {
          path: 'community',
          name: 'board-list',
          component: () => import('@/views/community/BoardListView.vue'),
        },
        {
          path: 'community/write',
          name: 'post-write',
          component: () => import('@/views/community/PostWriteFormView.vue'),
        },
        {
          path: 'community/:bno',
          name: 'post-detail',
          component: () => import('@/views/community/PostDetailView.vue'),
        },
        {
          path: 'community/:bno/modify',
          name: 'post-modify',
          component: () => import('@/views/community/PostModifyFormView.vue'),
        },

        // --- 소셜 / 팔로우 (F111) ---
        {
          path: 'user/:email',
          name: 'user-profile',
          component: () => import('@/views/social/UserProfileView.vue'),
        },
      ],
    },

    // ===== 운영 콘솔 (로그인 + 운영 권한 필요) =====
    {
      path: '/ops',
      meta: { requiresAuth: true, requiresOperator: true },
      component: () => import('@/views/ops/OpsLayout.vue'),
      children: [
        {
          path: '',
          name: 'ops-dashboard',
          component: () => import('@/views/ops/OpsDashboardView.vue'),
        },
        {
          path: 'members',
          name: 'ops-members',
          component: () => import('@/views/ops/OpsMemberListView.vue'),
        },
        {
          path: 'challenges',
          name: 'ops-challenges',
          component: () => import('@/views/ops/OpsChallengeListView.vue'),
        },
        {
          path: 'posts',
          name: 'ops-posts',
          component: () => import('@/views/ops/OpsPostListView.vue'),
        },
      ],
    },

    // ===== 에러 (catch-all) =====
    {
      path: '/:custom(.*)',
      name: 'custom-error',
      component: () => import('@/views/error/CommonErrorView.vue'),
    },
  ],
});

// 라우터 가드: meta.requiresAuth가 있고 미로그인 시 로그인 폼으로
import { useMemberStore } from '@/stores/memberStore';

// 운영 권한으로 인정하는 role (OPERATOR 신규 + ADMIN 기존 호환)
const OPERATOR_ROLES = ['OPERATOR', 'ADMIN'];

router.beforeEach((to) => {
  const memberStore = useMemberStore();
  if (to.meta.requiresAuth && !memberStore.isLoggedIn) {
    return { name: 'login-form', query: { to: to.path } };
  }
  // 운영 콘솔: 운영 권한이 없으면 홈으로 차단 (백엔드에서도 403으로 재검사)
  if (to.meta.requiresOperator && !OPERATOR_ROLES.includes(memberStore.loginUser?.role)) {
    return { name: 'home' };
  }
});

export default router;
