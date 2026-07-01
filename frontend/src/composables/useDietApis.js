// 식단 (F101~F105) API 컴포저블
// 패턴: useApi(callApi) → { data, error, execute, isLoading }
import { ssafyapi } from '@/restapi/index';
import { useApi } from '@/composables/useApis';
import { useRouter } from 'vue-router';

// F102. 식단 목록 조회 (날짜/끼니 필터)
export const useFetchDiets = () => {
  const callApi = async (params) => {
    const res = await ssafyapi.get('/diets', { params });
    return res.data.payload;
  };
  return useApi(callApi);
};

// F102. 식단 상세 조회 (식단 ID)
export const useFetchDietDetail = () => {
  const callApi = async (dno) => {
    const res = await ssafyapi.get(`/diets/${dno}`);
    return res.data.payload;
  };
  return useApi(callApi);
};

// F101. 식단 작성 (사진 업로드 + 음식 구성 → 저장)
export const useWriteDiet = () => {
  const router = useRouter();
  const callApi = async (formData) => {
    // 사진 포함 시 multipart/form-data
    const res = await ssafyapi.post('/diets', formData);
    router.push({ name: 'diet-list' });
    return res.data.payload;
  };
  return useApi(callApi);
};

// F103. 식단 수정
export const useModifyDiet = () => {
  const router = useRouter();
  const callApi = async (dno, diet) => {
    const res = await ssafyapi.put(`/diets/${dno}`, diet);
    router.push({ name: 'diet-detail', params: { dno } });
    return res.data.payload;
  };
  return useApi(callApi);
};

// F104. 식단 삭제
export const useDeleteDiet = () => {
  const router = useRouter();
  const callApi = async (dno) => {
    const res = await ssafyapi.delete(`/diets/${dno}`);
    router.push({ name: 'diet-list' });
    return res.data;
  };
  return useApi(callApi);
};

// 음식 검색 (식약처 음식 DB → 실패 시 백엔드가 로컬 foods 로 폴백)
// 외부 API 대기 시간을 감안해 전역 타임아웃(3초)보다 길게 설정
export const useSearchFood = () => {
  const callApi = async (keyword) => {
    const res = await ssafyapi.get('/foods', { params: { keyword }, timeout: 15000 });
    return res.data.payload;
  };
  return useApi(callApi);
};
