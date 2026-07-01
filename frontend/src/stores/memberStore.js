import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import { jwtDecode } from 'jwt-decode';
export const useMemberStore = defineStore(
  'member',
  () => {
    // 관례상 _를 붙여서 내부 상태임을 표시 - getter를 통해 공개
    const _alertMsg = ref('');
    // alert 메시지
    const alertMsg = computed(() => _alertMsg.value);
    // alert 메시지 설정
    const setAlertMsg = (msg) => {
      _alertMsg.value = msg;
    };

    const _loggedIn = ref(false);
    // login 상태
    const isLoggedIn = computed(() => _loggedIn.value);

    const _loginUser = ref(null);
    // 로그인한 사용자 정보
    const loginUser = computed(() => _loginUser.value);

    // 로그인 상태 업데이트
    const updateLoginStatus = (member) => {
      if (member) {
        _loggedIn.value = true;
        _loginUser.value = member;
      } else {
        _loggedIn.value = false;
        _loginUser.value = {};
      }
    };

    const _tokens = ref({});
    const tokens = computed(() => _tokens.value);

    // login / logout / refresh
    const login = (payload) => {
      // payload: { accessToken, refreshToken, user }
      _tokens.value = {
        accessToken: payload.accessToken,
        refreshToken: payload.refreshToken,
      };
      updateLoginStatus(payload.user);
    };

    const logout = () => {
      _tokens.value = {};
      updateLoginStatus(false);
    };

    const refresh = (accessToken) => {
      _tokens.value = { ..._tokens.value, accessToken };
    };
    // END

    return {
      _alertMsg,
      _loggedIn,
      _loginUser,
      _tokens,
      isLoggedIn,
      alertMsg,
      loginUser,
      tokens,
      setAlertMsg,
      updateLoginStatus,
      login,
      logout,
      refresh,
    };
  },
  {
    // pinia-plugin-persistedstate에 의한 영속화 설정
    persist: { storage: sessionStorage },
  }
);
