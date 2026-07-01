// AI 에이전트 (F116, F117 + 가이드) API 컴포저블
import { ssafyapi } from '@/restapi/index';
import { useApi } from '@/composables/useApis';

// 생성형 AI(추론 모델)는 응답이 느려 호출별 타임아웃을 60초로 연장
const AI_TIMEOUT = 120000;

// F116. 생성형 AI 식단 종합 분석
export const useAiDietAnalysis = () => {
  const callApi = async (payload) => {
    // payload 예: { dno } 또는 { period: 'week' }
    const res = await ssafyapi.post('/ai/diet-analysis', payload, { timeout: AI_TIMEOUT });
    return res.data.payload;
  };
  return useApi(callApi);
};

// F116(심화). 사진 → 음식 인식·영양 분석 (비전)
export const useAnalyzePhoto = () => {
  const callApi = async (file) => {
    const fd = new FormData();
    fd.append('photo', file);
    const res = await ssafyapi.post('/ai/analyze-photo', fd, { timeout: AI_TIMEOUT });
    return res.data.payload; // { title, foods:[{name,kcal,...}], totalKcal, comment }
  };
  return useApi(callApi);
};

// 음식명 → 1인분 영양 추정 (검색 결과 없을 때 보조 등록용). 결과에 estimated=true 포함
export const useEstimateNutrition = () => {
  const callApi = async (name) => {
    const res = await ssafyapi.post('/ai/estimate-nutrition', { name }, { timeout: AI_TIMEOUT });
    return res.data.payload; // { name, kcal, protein, carbs, fat, estimated }
  };
  return useApi(callApi);
};

// F117. 생성형 AI 운동 코칭 (프로필 + 식단 기반, 3종 코치 에이전트)
// coach: 'powerrex'(벌크업) | 'slimdino'(다이어트) | 'balanceno'(유지)
// 대화 히스토리는 서버 DB에 저장·관리됨 (멀티턴 컨텍스트는 서버가 구성)
export const useAiWorkoutCoach = () => {
  const callApi = async (message, coach) => {
    const res = await ssafyapi.post('/ai/workout-coach', { message, coach }, { timeout: AI_TIMEOUT });
    return res.data.payload;
  };
  return useApi(callApi);
};

// 코치별 저장된 대화 조회 (채팅방 입장/코치 전환 시 복원)
export const useAiChatHistory = () => {
  const callApi = async (coach) => {
    const res = await ssafyapi.get('/ai/chat-history', { params: { coach } });
    return res.data.payload; // [{ role: 'user'|'assistant', content, createdAt }]
  };
  return useApi(callApi);
};

// 사이트 이용 가이드 챗봇 (전역 위젯)
export const useGuideChat = () => {
  const callApi = async (message) => {
    const res = await ssafyapi.post('/ai/guide', { message }, { timeout: AI_TIMEOUT });
    return res.data.payload?.answer ?? res.data.payload;
  };
  return useApi(callApi);
};
