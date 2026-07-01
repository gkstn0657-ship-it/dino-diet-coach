// 물 섭취 기록 API 컴포저블 (메인 물섭취 위젯 서버 저장/복원)
import { ssafyapi } from '@/restapi/index';
import { useApi } from '@/composables/useApis';

/** 오늘 물 섭취 잔 수 조회 → { cups } */
export const useGetWater = () => {
  const callApi = async () => {
    const res = await ssafyapi.get('/water/today');
    return res.data.payload;
  };
  return useApi(callApi);
};

/** 오늘 물 섭취 잔 수 저장 → { cups } (서버에서 0~30 정규화) */
export const useSetWater = () => {
  const callApi = async (cups) => {
    const res = await ssafyapi.put('/water/today', { cups });
    return res.data.payload;
  };
  return useApi(callApi);
};
