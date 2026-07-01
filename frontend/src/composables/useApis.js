// api 호출을 위한 composable 함수 : options를 통해 즉시 호출 여부 등 부가적인 설정 가능
import { ref } from 'vue';
/**
 * API 호출을 위한 공통 Composable 함수
 * @param {Function} apiFunction - 실제 호출할 비동기 API 함수
 * @returns {Object} API 상태 및 실행 함수 객체
 * @property {Ref} data - API 응답 데이터
 * @property {Ref} error - API 호출 중 발생한 에러 객체
 * @property {Function} execute - API를 실행하는 함수. 전달받은 인자는 apiFunction으로 전달됨
 * @property {Ref} isLoading - 현재 API가 호출 중인지 여부
 */
export const useApi = (apiFunction) => {
  const data = ref(null);
  const error = ref(null);
  const isLoading = ref(false); // 상태 관리용

  /**
   * API 함수를 실행하고 상태를 업데이트합니다.
   * @param  {...any} args - apiFunction에 전달할 매개변수들
   * @returns {Promise<any>} API 호출 결과 (data.value와 동일)
   */
  const execute = async (...args) => {
    error.value = null;
    isLoading.value = true;
    try {
      return (data.value = await apiFunction(...args));
    } catch (err) {
      error.value = err;
    } finally {
      isLoading.value = false;
    }
  };
  // composable의 반환 값: 주로 component의 template에서 활용
  return { data, error, execute, isLoading };
};
