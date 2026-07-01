// 운영 콘솔 (Ops) API 컴포저블 — 일반 사용자 API와 분리
// 백엔드 /api/v1/ops/* 는 서버에서 운영 권한(OPERATOR/ADMIN)을 검사한다.
import { ssafyapi } from '@/restapi/index';
import { useApi } from '@/composables/useApis';

/** 운영 요약 통계 (회원/식단/진행중 챌린지/게시글 수) */
export const useOpsSummary = () => {
  const callApi = async () => {
    const res = await ssafyapi.get('/ops/summary');
    return res.data.payload;
  };
  return useApi(callApi);
};

/** 회원 운영 목록 (검색어 옵션) */
export const useOpsMembers = () => {
  const callApi = async (keyword) => {
    const res = await ssafyapi.get('/ops/members', { params: keyword ? { keyword } : {} });
    return res.data.payload;
  };
  return useApi(callApi);
};

/** 챌린지 운영 목록 */
export const useOpsChallenges = () => {
  const callApi = async () => {
    const res = await ssafyapi.get('/ops/challenges');
    return res.data.payload;
  };
  return useApi(callApi);
};

/** 게시글 운영 목록 (숨김 포함) */
export const useOpsPosts = () => {
  const callApi = async () => {
    const res = await ssafyapi.get('/ops/posts');
    return res.data.payload;
  };
  return useApi(callApi);
};

/** 게시글 숨김/해제 */
export const useSetPostHidden = () => {
  const callApi = async (bno, hidden) => {
    const res = await ssafyapi.patch(`/ops/posts/${bno}/hidden`, { hidden });
    return res.data;
  };
  return useApi(callApi);
};

/** 회원 정지/해제 */
export const useSetMemberActive = () => {
  const callApi = async (mno, active) => {
    const res = await ssafyapi.patch(`/ops/members/${mno}/active`, { active });
    return res.data;
  };
  return useApi(callApi);
};

/** 챌린지 승인/거부 (status: APPROVED | REJECTED | PENDING) */
export const useSetChallengeApproval = () => {
  const callApi = async (cno, status) => {
    const res = await ssafyapi.patch(`/ops/challenges/${cno}/approval`, { status });
    return res.data;
  };
  return useApi(callApi);
};

/** 챌린지 숨김/노출 */
export const useSetChallengeVisibility = () => {
  const callApi = async (cno, visible) => {
    const res = await ssafyapi.patch(`/ops/challenges/${cno}/visibility`, { visible });
    return res.data;
  };
  return useApi(callApi);
};

/** 챌린지 삭제(참가자 있으면 숨김 처리) */
export const useDeleteChallengeOps = () => {
  const callApi = async (cno) => {
    const res = await ssafyapi.delete(`/ops/challenges/${cno}`);
    return res.data;
  };
  return useApi(callApi);
};

/** 식품 DB 최신화 (식약처 API → 로컬 foods 갱신) */
export const useSyncFoods = () => {
  const callApi = async () => {
    // 외부 API 다건 호출이라 기본(3s)보다 넉넉한 타임아웃
    const res = await ssafyapi.post('/ops/foods/sync', {}, { timeout: 120000 });
    return res.data; // { status, message, payload: { configured, inserted, updated, failedKeywords } }
  };
  return useApi(callApi);
};
