// 커뮤니티 (F114 게시판, F115 댓글) API 컴포저블
import { ssafyapi } from '@/restapi/index';
import { useApi } from '@/composables/useApis';
import { useRouter } from 'vue-router';

// F114. 게시글 목록 조회 (board: review/expert/free)
export const useFetchPosts = () => {
  const callApi = async (params) => {
    const res = await ssafyapi.get('/posts', { params });
    return res.data.payload;
  };
  return useApi(callApi);
};

// F114. 게시글 상세 조회
export const useFetchPostDetail = () => {
  const callApi = async (bno) => {
    const res = await ssafyapi.get(`/posts/${bno}`);
    return res.data.payload;
  };
  return useApi(callApi);
};

// 오늘 인기글 TOP 3 (오늘 작성 + 좋아요 상위)
export const useTopTodayPosts = () => {
  const callApi = async () => {
    const res = await ssafyapi.get('/posts/top-today');
    return res.data.payload;
  };
  return useApi(callApi);
};

// 게시글 좋아요 토글 → { liked, likes }
export const useToggleLike = () => {
  const callApi = async (bno) => {
    const res = await ssafyapi.post(`/posts/${bno}/like`);
    return res.data.payload;
  };
  return useApi(callApi);
};

// F114. 게시글 작성
export const useWritePost = () => {
  const router = useRouter();
  const callApi = async (post) => {
    const res = await ssafyapi.post('/posts', post);
    router.push({ name: 'board-list' });
    return res.data.payload;
  };
  return useApi(callApi);
};

// F114. 게시글 수정
export const useModifyPost = () => {
  const router = useRouter();
  const callApi = async (bno, post) => {
    const res = await ssafyapi.put(`/posts/${bno}`, post);
    // 수정화면 진입 직전이 상세였다면(일반 흐름) 뒤로 돌아간다 → 상세가 재마운트되며 수정 내용으로 갱신,
    // 히스토리는 [목록, 상세]만 남아 상세에서 뒤로 1번이면 목록으로 간다.
    // (수정 URL 로 직접 들어온 예외 상황은 상세로 replace 하여 안전하게 처리)
    if (window.history.state && window.history.state.back) {
      router.back();
    } else {
      router.replace({ name: 'post-detail', params: { bno } });
    }
    return res.data.payload;
  };
  return useApi(callApi);
};

// F114. 게시글 삭제
export const useDeletePost = () => {
  const router = useRouter();
  const callApi = async (bno) => {
    const res = await ssafyapi.delete(`/posts/${bno}`);
    router.push({ name: 'board-list' });
    return res.data;
  };
  return useApi(callApi);
};

// F115. 댓글 목록 조회
export const useFetchComments = () => {
  const callApi = async (bno) => {
    const res = await ssafyapi.get(`/posts/${bno}/comments`);
    return res.data.payload;
  };
  return useApi(callApi);
};

// F115. 댓글 작성
export const useWriteComment = () => {
  const callApi = async (bno, content) => {
    const res = await ssafyapi.post(`/posts/${bno}/comments`, { content });
    return res.data.payload;
  };
  return useApi(callApi);
};

// F115. 댓글 수정
export const useUpdateComment = () => {
  const callApi = async (bno, cno, content) => {
    const res = await ssafyapi.put(`/posts/${bno}/comments/${cno}`, { content });
    return res.data;
  };
  return useApi(callApi);
};

// F115. 댓글 삭제
export const useDeleteComment = () => {
  const callApi = async (bno, cno) => {
    const res = await ssafyapi.delete(`/posts/${bno}/comments/${cno}`);
    return res.data;
  };
  return useApi(callApi);
};
