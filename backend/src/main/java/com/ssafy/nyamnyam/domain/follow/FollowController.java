package com.ssafy.nyamnyam.domain.follow;

import com.ssafy.nyamnyam.common.ApiResponse;
import com.ssafy.nyamnyam.domain.member.MemberService;
import com.ssafy.nyamnyam.security.LoginMember;
import com.ssafy.nyamnyam.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final MemberService memberService;

    private Integer mno(LoginUser user) {
        return memberService.getByEmail(user.email()).getMno();
    }

    /** 사용자 공개 프로필 (F111) */
    @GetMapping("/{email}")
    public ApiResponse<Map<String, Object>> profile(@PathVariable String email, @LoginMember LoginUser user) {
        return ApiResponse.ok(followService.profile(email, mno(user)));
    }

    /** 팔로우 (F111) */
    @PostMapping("/{email}/follow")
    public ApiResponse<Void> follow(@PathVariable String email, @LoginMember LoginUser user) {
        followService.follow(mno(user), email);
        return ApiResponse.ok("팔로우 완료", null);
    }

    /** 언팔로우 (F111) */
    @DeleteMapping("/{email}/follow")
    public ApiResponse<Void> unfollow(@PathVariable String email, @LoginMember LoginUser user) {
        followService.unfollow(mno(user), email);
        return ApiResponse.ok("언팔로우 완료", null);
    }

    /** 팔로워 목록 */
    @GetMapping("/{email}/followers")
    public ApiResponse<List<Map<String, Object>>> followers(@PathVariable String email) {
        return ApiResponse.ok(followService.follows(email, "followers"));
    }

    /** 팔로잉 목록 */
    @GetMapping("/{email}/followings")
    public ApiResponse<List<Map<String, Object>>> followings(@PathVariable String email) {
        return ApiResponse.ok(followService.follows(email, "followings"));
    }
}
