package com.ssafy.nyamnyam.domain.community;

import com.ssafy.nyamnyam.common.ApiResponse;
import com.ssafy.nyamnyam.domain.member.MemberService;
import com.ssafy.nyamnyam.security.LoginMember;
import com.ssafy.nyamnyam.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final MemberService memberService;

    private Integer mno(LoginUser user) {
        return memberService.getByEmail(user.email()).getMno();
    }

    public record PostRequest(String board, String title, String content) {}
    public record CommentRequest(String content) {}

    /** 게시글 목록 (F114) — 페이지 단위. 응답: { posts, page } (각 글에 liked 포함) */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @LoginMember LoginUser user,
            @RequestParam(required = false) String board,
            @RequestParam(defaultValue = "1") int currentPage) {
        return ApiResponse.ok(communityService.listPosts(board, currentPage, mno(user)));
    }

    /** 오늘 인기글 TOP 3 (오늘 작성 + 좋아요 상위) */
    @GetMapping("/top-today")
    public ApiResponse<List<Map<String, Object>>> topToday() {
        return ApiResponse.ok(communityService.topTodayPosts());
    }

    /** 게시글 좋아요 토글 (로그인 사용자). 응답: { liked, likes } */
    @PostMapping("/{bno}/like")
    public ApiResponse<Map<String, Object>> toggleLike(@PathVariable Integer bno, @LoginMember LoginUser user) {
        return ApiResponse.ok(communityService.toggleLike(bno, mno(user)));
    }

    /** 게시글 상세 (F114) — 본인/운영자 여부(canManage) 포함 */
    @GetMapping("/{bno}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Integer bno, @LoginMember LoginUser user) {
        return ApiResponse.ok(communityService.postDetail(bno, mno(user), user.role()));
    }

    /** 게시글 작성 (F114) */
    @PostMapping
    public ApiResponse<Integer> create(@LoginMember LoginUser user, @RequestBody PostRequest req) {
        return ApiResponse.ok("작성 완료",
                communityService.createPost(mno(user), user.role(), req.board(), req.title(), req.content()));
    }

    /** 게시글 수정 (F114) — 작성자 본인 또는 운영자만 */
    @PutMapping("/{bno}")
    public ApiResponse<Void> update(@PathVariable Integer bno, @LoginMember LoginUser user,
                                    @RequestBody PostRequest req) {
        communityService.updatePost(bno, mno(user), user.role(), req.title(), req.content());
        return ApiResponse.ok("수정 완료", null);
    }

    /** 게시글 삭제 (F114) — 작성자 본인 또는 운영자만 */
    @DeleteMapping("/{bno}")
    public ApiResponse<Void> delete(@PathVariable Integer bno, @LoginMember LoginUser user) {
        communityService.deletePost(bno, mno(user), user.role());
        return ApiResponse.ok("삭제 완료", null);
    }

    /** 댓글 목록 (F115) — 작성자 프로필 이동값 + canManage 포함 */
    @GetMapping("/{bno}/comments")
    public ApiResponse<List<Map<String, Object>>> comments(@PathVariable Integer bno, @LoginMember LoginUser user) {
        return ApiResponse.ok(communityService.comments(bno, mno(user), user.role()));
    }

    /** 댓글 작성 (F115) */
    @PostMapping("/{bno}/comments")
    public ApiResponse<Integer> addComment(@PathVariable Integer bno,
                                           @LoginMember LoginUser user,
                                           @RequestBody CommentRequest req) {
        return ApiResponse.ok("댓글 작성 완료",
                communityService.addComment(bno, mno(user), req.content()));
    }

    /** 댓글 수정 (F115) — 작성자 본인 또는 운영자만 */
    @PutMapping("/{bno}/comments/{cno}")
    public ApiResponse<Void> updateComment(@PathVariable Integer bno, @PathVariable Integer cno,
                                           @LoginMember LoginUser user,
                                           @RequestBody CommentRequest req) {
        communityService.updateComment(cno, mno(user), user.role(), req.content());
        return ApiResponse.ok("댓글 수정 완료", null);
    }

    /** 댓글 삭제 (F115) — 작성자 본인 또는 운영자만 */
    @DeleteMapping("/{bno}/comments/{cno}")
    public ApiResponse<Void> deleteComment(@PathVariable Integer bno, @PathVariable Integer cno,
                                           @LoginMember LoginUser user) {
        communityService.deleteComment(cno, mno(user), user.role());
        return ApiResponse.ok("댓글 삭제 완료", null);
    }
}
