import { useMemberStore } from '@/stores/memberStore';
import { ssafyapi } from '@/restapi/index';
import { useApi } from '@/composables/useApis';
import { useRouter } from 'vue-router';

// 로그인 처리 Composable
export const useLogin = () => {
  const memberStore = useMemberStore();

  /**
   * 로그인 로직 실행 함수
   * @param {Object} member - 로그인 정보 (email, password)
   * @returns {Promise<any>} 로그인 성공 데이터
   */
  const callApi = async (member) => {
    // 실제 로그인 API 호출 (토큰 발급 전이므로 skipAuth)
    try {
      const res = await ssafyapi.post('/auth/login', member, {
        headers: { skipAuth: true },
      });
      memberStore.login(res.data.payload); // { accessToken, refreshToken, user }
      return res.data.payload;
    } catch (err) {
      const msg = err.response?.data?.message || '이메일 또는 비밀번호가 올바르지 않습니다.';
      memberStore.setAlertMsg(msg);
      throw err;
    }
  };
  return useApi(callApi);
};

export const useLogout = () => {
  const memberStore = useMemberStore();

  const callApi = async () => {
    // 로그아웃 API 호출 후 클라이언트 상태 정리
    try {
      await ssafyapi.post('/auth/logout', {}, { headers: { skipAuth: true } });
    } catch {
      // 서버 오류와 무관하게 클라이언트는 로그아웃 처리
    }
    memberStore.logout();
  };

  return useApi(callApi);
};

/**
 * 이메일 중복 체크를 위한 Composable
 * @returns {Object} { data, error, execute, isLoading }
 */
export const useCheckEmail = () => {
  const callApi = async (email) => {
    if (!email) {
      return;
    } else {
      const res = await ssafyapi.get('/members/checkEmail', {
        params: { email },
        headers: { skipAuth: true },
      });
      return res.data.payload; // { canUse }
    }
  };

  return useApi(callApi);
};

// END

/**
 * 회원 가입 처리를 위한 Composable
 * @returns {Object} { data, error, execute, isLoading }
 */
export const useRegistMember = () => {
  const callApi = async (member) => {
    const res = await ssafyapi.post('/members', member, {
      headers: { skipAuth: true },
    });
    return res.data;
  }; // callApi

  return useApi(callApi);
};

// END

/**
 * 회원 목록 조회를 위한 Composable
 * @returns {Object} { data, error, execute, isLoading }
 */
export const useFetchMembers = () => {
  const callApi = async (params) => {
    const res = await ssafyapi.get('/members', { params });
    return res.data;
  };
  return useApi(callApi);
};

// END

/**
 * 회원 상세 조회를 위한 Composable
 * @returns {Object} { data, error, execute, isLoading }
 */
export const useFetchMemberDetail = () => {
  const callApi = async (email) => {
    const res = await ssafyapi.get(`/members/${email}`);
    return res.data.payload;
  };

  return useApi(callApi);
};

// END

/**
 * 회원 삭제를 위한 Composable
 * @returns {Object} { data, error, execute, isLoading }
 */
export const useDeleteMember = () => {
  const memberStore = useMemberStore();
  const router = useRouter();

  const callApi = async (member) => {
    const res = await ssafyapi.delete(`/members/${member.mno}`);
    if (member.email === memberStore.loginUser.email) {
      memberStore.updateLoginStatus(false);
      memberStore.setAlertMsg('로그아웃 되었습니다.');
      router.push({ name: 'home' });
    } else {
      router.push({ name: 'member-list' });
    }
    return res.data;
  };

  return useApi(callApi);
};

// END

/**
 * 회원 프로필 수정을 위한 Composable
 * @returns {Object} { data, error, execute, isLoading }
 */
export const useUpdateProfile = () => {
  const callApi = async (formData, email) => {
    const res = await ssafyapi.patch(`/members/${email}/profile`, formData);
    return res.data;
  };

  return useApi(callApi);
};

// END

/**
 * 로그인 사용자 비밀번호 변경을 위한 Composable
 * payload: { currentPassword, newPassword, newPasswordConfirm }
 * @returns {Object} { data, error, execute, isLoading }
 */
export const useChangePassword = () => {
  const callApi = async (payload) => {
    const res = await ssafyapi.patch('/members/me/password', payload);
    return res.data;
  };
  return useApi(callApi);
};

/**
 * 내 개인 목표 칼로리 조회 — { targetCalories, consumedCalories, remainingCalories, hasProfile }
 */
export const useMyTarget = () => {
  const callApi = async () => {
    const res = await ssafyapi.get('/members/me/target');
    return res.data.payload;
  };
  return useApi(callApi);
};

/**
 * 비밀번호 재설정 요청 (이메일) — 계정 존재 여부를 노출하지 않음
 */
export const useRequestPasswordReset = () => {
  const callApi = async (email) => {
    const res = await ssafyapi.post(
      '/auth/password/reset-request',
      { email },
      { headers: { skipAuth: true } }
    );
    return res.data;
  };
  return useApi(callApi);
};

/**
 * 비밀번호 재설정 실행 — { token, newPassword, newPasswordConfirm }
 */
export const useResetPassword = () => {
  const callApi = async (payload) => {
    const res = await ssafyapi.post('/auth/password/reset', payload, {
      headers: { skipAuth: true },
    });
    return res.data;
  };
  return useApi(callApi);
};

/**
 * 토큰 재발급 처리를 위한 Composable
 * @returns {Object} { data, error, execute, isLoading }
 */
export const useTokenRefresh = () => {
  const memberStore = useMemberStore();
  // 24-1. refresh token 으로 access token 재발급
  const callApi = async () => {
    const refreshToken = memberStore.tokens?.refreshToken;
    if (!refreshToken) return false;
    try {
      const res = await ssafyapi.post(
        '/auth/refresh',
        { refreshToken },
        { headers: { skipAuth: true } }
      );
      memberStore.refresh(res.data.payload.accessToken);
      return true;
    } catch {
      return false;
    }
  };
  return useApi(callApi);
};
