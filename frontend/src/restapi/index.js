// /src/restapi/index.js
import axios from 'axios';
import { useMemberStore } from '@/stores/memberStore';
import { useTokenRefresh } from '@/composables/useMemberApis';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

// axios 인스턴스 생성
const ssafyapi = axios.create({
  baseURL: import.meta.env.VITE_SSAFY_API_URL,
  timeout: 1000 * 3,
});

// 요청 인터셉터: skipAuth가 아니면 access token을 Authorization 헤더에 부착
ssafyapi.interceptors.request.use(
  (config) => {
    NProgress.start();
    const memberStore = useMemberStore();
    const token = memberStore.tokens?.accessToken;
    if (!config.headers.skipAuth && token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    delete config.headers.skipAuth;
    return config;
  },
  (error) => {
    console.error('[요청]', error);
    return Promise.reject(error);
  }
);

// 응답 인터셉터: 401 시 TOKEN_ERROR면 refresh로 재발급, 아니면 로그아웃
ssafyapi.interceptors.response.use(
  (response) => {
    NProgress.done();
    return response;
  },
  async (error) => {
    NProgress.done();
    console.error('[응답 에러]', error);
    const memberStore = useMemberStore();
    const tokenRefreshApi = useTokenRefresh();
    if (error.status === 401) {
      const originalRequest = error.config;
      if (error.response.data.message === 'TOKEN_ERROR') {
        // refresh token으로 access token 재발급 후 원래 요청 재시도
        const result = await tokenRefreshApi.execute();
        if (result) {
          return ssafyapi(originalRequest);
        }
      } else {
        // refresh도 만료 → 로그아웃 처리
        memberStore.setAlertMsg('로그인 세션이 만료되었습니다. 다시 로그인하세요');
        memberStore.logout();
      }
    } else {
      // 401 외의 다른 에러는 그대로 전파
      return Promise.reject(error);
    }
  }
);

export { ssafyapi };
