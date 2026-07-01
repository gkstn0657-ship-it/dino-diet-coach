// 챌린지 (F112, F113) API 컴포저블
import { ssafyapi } from '@/restapi/index';
import { useApi } from '@/composables/useApis';
import { useRouter } from 'vue-router';

// F112. 챌린지 목록 조회
export const useFetchChallenges = () => {
  const callApi = async (params) => {
    const res = await ssafyapi.get('/challenges', { params });
    return res.data.payload;
  };
  return useApi(callApi);
};

// F112. 챌린지 상세 조회
export const useFetchChallengeDetail = () => {
  const callApi = async (cno) => {
    const res = await ssafyapi.get(`/challenges/${cno}`);
    return res.data.payload;
  };
  return useApi(callApi);
};

// F112. 챌린지 생성 — 이미지 업로드 대비 타임아웃 연장 (전역 3초로는 부족)
export const useCreateChallenge = () => {
  const router = useRouter();
  const callApi = async (formData) => {
    const res = await ssafyapi.post('/challenges', formData, { timeout: 30000 });
    router.push({ name: 'challenge-list' });
    return res.data.payload;
  };
  return useApi(callApi);
};

// F112. 챌린지 삭제
export const useDeleteChallenge = () => {
  const callApi = async (cno) => {
    const res = await ssafyapi.delete(`/challenges/${cno}`);
    return res.data;
  };
  return useApi(callApi);
};

// F113. 챌린지 참여 (조건 없이 자유 참여)
export const useJoinChallenge = () => {
  const callApi = async (cno) => {
    const res = await ssafyapi.post(`/challenges/${cno}/join`);
    return res.data.payload;
  };
  return useApi(callApi);
};

// F113. 챌린지 참여 취소 (모집중에만 가능)
export const useLeaveChallenge = () => {
  const callApi = async (cno) => {
    const res = await ssafyapi.delete(`/challenges/${cno}/join`);
    return res.data;
  };
  return useApi(callApi);
};

// F113. 챌린지 오늘 인증 (달성도 +1)
export const useCheckChallenge = () => {
  const callApi = async (cno) => {
    const res = await ssafyapi.post(`/challenges/${cno}/check`);
    return res.data.payload;
  };
  return useApi(callApi);
};

// F113. 내 챌린지 / 달성 현황
export const useFetchMyChallenges = () => {
  const callApi = async () => {
    const res = await ssafyapi.get('/challenges/my');
    return res.data.payload;
  };
  return useApi(callApi);
};
