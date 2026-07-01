package com.ssafy.nyamnyam.domain.community;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityMapper {
    // 게시글
    int insertPost(Post post);
    // 페이지 단위 조회 (board 필터 + LIMIT/OFFSET). mno: 로그인 사용자 좋아요 여부(liked) 계산용(없으면 null)
    List<Post> findPosts(@Param("board") String board,
                         @Param("offset") int offset,
                         @Param("size") int size,
                         @Param("mno") Integer mno);
    // 전체 개수 (총 페이지 계산용). board 필터는 findPosts 와 동일 결과 집합
    int countPosts(@Param("board") String board);
    Post findPost(@Param("bno") Integer bno, @Param("mno") Integer mno);
    int updatePost(Post post);
    int deletePost(@Param("bno") Integer bno);

    // 오늘 작성 게시글 중 좋아요 상위 N개 (좋아요 DESC, 최신 우선)
    List<Post> findTopToday(@Param("limit") int limit);

    // 운영 콘솔 전용: 숨김 포함 전체 게시글 (hidden 플래그 포함)
    List<Post> findAllForOps();
    // 사용자 프로필: 특정 회원이 작성한 글(숨김 제외)
    List<Post> findByAuthor(@Param("mno") Integer mno);
    // 운영 콘솔: 게시글 숨김/해제
    int setHidden(@Param("bno") Integer bno, @Param("hidden") boolean hidden);

    // 좋아요 (사용자별)
    int existsLike(@Param("mno") Integer mno, @Param("bno") Integer bno);
    int insertLike(@Param("mno") Integer mno, @Param("bno") Integer bno);
    int deleteLike(@Param("mno") Integer mno, @Param("bno") Integer bno);
    int updateLikes(@Param("bno") Integer bno, @Param("delta") int delta);
    Integer selectLikes(@Param("bno") Integer bno);

    // 댓글
    int insertComment(Comment comment);
    List<Comment> findComments(@Param("bno") Integer bno);
    Comment findComment(@Param("cno") Integer cno);   // 권한 확인용(작성자 mno)
    int updateComment(@Param("cno") Integer cno, @Param("content") String content);
    int deleteComment(@Param("cno") Integer cno);
}
