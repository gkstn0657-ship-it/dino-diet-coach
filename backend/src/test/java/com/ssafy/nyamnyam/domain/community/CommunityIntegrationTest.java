package com.ssafy.nyamnyam.domain.community;

import com.ssafy.nyamnyam.domain.member.Member;
import com.ssafy.nyamnyam.domain.member.MemberMapper;
import com.ssafy.nyamnyam.support.Fixtures;
import com.ssafy.nyamnyam.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 커뮤니티 게시글 통합테스트 (실제 MySQL + MyBatis end-to-end).
 * 작성 → 조회 라운드트립과 board 필터, 작성자 JOIN 을 실제 DB 로 검증한다.
 */
class CommunityIntegrationTest extends IntegrationTest {

    @Autowired CommunityService communityService;
    @Autowired MemberMapper memberMapper;

    /** 게시글을 쓸 회원 1명 생성 후 mno 반환 */
    private int givenMember() {
        Member m = Fixtures.member();
        memberMapper.insert(m);
        return m.getMno();
    }

    /** 페이징 응답({ posts, page })에서 posts 목록만 추출 */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> postsOf(Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get("posts");
    }

    @Test
    @DisplayName("게시글 작성 후 목록(1페이지) 조회 시 방금 글이 포함된다")
    void createAndList() {
        int mno = givenMember();

        communityService.createPost(mno, "ADMIN", "free", "통합테스트 글", "본문");

        // 최신 글이 bno DESC 로 1페이지 맨 앞에 온다
        List<Map<String, Object>> posts = postsOf(communityService.listPosts(null, 1, null));
        assertThat(posts).isNotEmpty();
        assertThat(posts).anyMatch(p -> "통합테스트 글".equals(p.get("title")));
    }

    @Test
    @DisplayName("board 필터로 조회하면 해당 게시판 글만 반환된다")
    void listFiltersByBoard() {
        int mno = givenMember();
        communityService.createPost(mno, "ADMIN", "free", "자유글", "본문");
        communityService.createPost(mno, "ADMIN", "review", "후기글", "본문");

        List<Map<String, Object>> reviews = postsOf(communityService.listPosts("review", 1, null));

        assertThat(reviews).isNotEmpty();
        // review 게시판은 boardLabel '리뷰' 로 매핑된다 (CommunityService 참조)
        assertThat(reviews).allMatch(p -> "리뷰".equals(p.get("boardLabel")));
        assertThat(reviews).anyMatch(p -> "후기글".equals(p.get("title")));
    }

    @Test
    @DisplayName("게시글 상세 조회 시 작성자 이름이 JOIN 되어 함께 온다")
    void detailIncludesAuthor() {
        int mno = givenMember();
        Member writer = memberMapper.findByMno(mno);
        Integer bno = communityService.createPost(mno, "ADMIN", "expert", "전문가 글", "본문");

        Map<String, Object> detail = communityService.postDetail(bno, mno, "USER");

        assertThat(detail.get("title")).isEqualTo("전문가 글");
        assertThat(detail.get("author")).isEqualTo(writer.getName());
    }
}
