// 소셜 / 팔로우 (F111) API 컴포저블
import { ssafyapi } from '@/restapi/index';
import { useApi } from '@/composables/useApis';

// F111. 다른 사용자 공개 프로필 조회
export const useFetchUserProfile = () => {
  const callApi = async (email) => {
    const res = await ssafyapi.get(`/users/${email}`);
    return res.data.payload;
  };
  return useApi(callApi);
};

// F111. 팔로우 추가
export const useFollow = () => {
  const callApi = async (email) => {
    const res = await ssafyapi.post(`/users/${email}/follow`);
    return res.data.payload;
  };
  return useApi(callApi);
};

// F111. 팔로우 취소
export const useUnfollow = () => {
  const callApi = async (email) => {
    const res = await ssafyapi.delete(`/users/${email}/follow`);
    return res.data;
  };
  return useApi(callApi);
};

// F111. 팔로워/팔로잉 목록
export const useFetchFollows = () => {
  const callApi = async (email, type = 'followers') => {
    // type: 'followers' | 'followings'
    const res = await ssafyapi.get(`/users/${email}/${type}`);
    return res.data.payload;
  };
  return useApi(callApi);
};
