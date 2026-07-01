package com.ssafy.nyamnyam.domain.follow;

import com.ssafy.nyamnyam.domain.challenge.ChallengeService;
import com.ssafy.nyamnyam.domain.community.CommunityService;
import com.ssafy.nyamnyam.domain.member.Member;
import com.ssafy.nyamnyam.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;
    private final MemberService memberService;
    private final CommunityService communityService;
    private final ChallengeService challengeService;

    /** 공개 프로필 (F111) — 작성글 + 참여/신청 챌린지 중심(공개 식단 썸네일 제거) */
    public Map<String, Object> profile(String targetEmail, Integer currentMno) {
        Member target = memberService.getByEmail(targetEmail);
        Integer tmno = target.getMno();

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("mno", tmno);
        m.put("name", target.getName());
        m.put("email", target.getEmail());
        m.put("isMe", currentMno != null && currentMno.equals(tmno));
        m.put("followers", followMapper.countFollowers(tmno));
        m.put("followings", followMapper.countFollowings(tmno));
        m.put("isFollowing", followMapper.exists(currentMno, tmno) > 0);

        // 작성한 글
        m.put("posts", communityService.postsByMember(tmno));
        // 참여 중인 챌린지
        m.put("joinedChallenges", challengeService.myChallenges(tmno));
        // 신청 중인 챌린지(내가 등록 → 승인 대기 PENDING)
        List<Map<String, Object>> applied = new ArrayList<>();
        for (Map<String, Object> c : challengeService.createdChallenges(tmno)) {
            if ("PENDING".equals(c.get("approvalStatus"))) applied.add(c);
        }
        m.put("appliedChallenges", applied);
        return m;
    }

    @Transactional
    public void follow(Integer follower, String targetEmail) {
        Integer following = memberService.getByEmail(targetEmail).getMno();
        if (!follower.equals(following)) {
            followMapper.insert(follower, following);
        }
    }

    @Transactional
    public void unfollow(Integer follower, String targetEmail) {
        Integer following = memberService.getByEmail(targetEmail).getMno();
        followMapper.delete(follower, following);
    }

    /** type: followers | followings */
    public List<Map<String, Object>> follows(String email, String type) {
        Integer mno = memberService.getByEmail(email).getMno();
        List<Member> members = "followings".equals(type)
                ? followMapper.findFollowings(mno)
                : followMapper.findFollowers(mno);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Member u : members) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", u.getName());
            m.put("email", u.getEmail());
            result.add(m);
        }
        return result;
    }
}
