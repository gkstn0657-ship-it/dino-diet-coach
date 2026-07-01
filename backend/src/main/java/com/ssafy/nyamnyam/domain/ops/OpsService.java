package com.ssafy.nyamnyam.domain.ops;

import com.ssafy.nyamnyam.domain.challenge.Challenge;
import com.ssafy.nyamnyam.domain.challenge.ChallengeMapper;
import com.ssafy.nyamnyam.domain.challenge.ChallengeService;
import com.ssafy.nyamnyam.domain.community.CommunityService;
import com.ssafy.nyamnyam.domain.diet.DietMapper;
import com.ssafy.nyamnyam.domain.food.FoodSyncService;
import com.ssafy.nyamnyam.domain.member.Member;
import com.ssafy.nyamnyam.domain.member.MemberMapper;
import com.ssafy.nyamnyam.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 운영 콘솔 전용 조회 서비스.
 * 일반 사용자 기능과 책임을 섞지 않도록 별도 서비스로 분리하되,
 * 실제 데이터 조회는 기존 Service/Mapper 를 재사용한다(중복 로직 최소화).
 * 권한 검사는 컨트롤러(OpsController)에서 수행한다.
 */
@Service
@RequiredArgsConstructor
public class OpsService {

    private final MemberService memberService;
    private final CommunityService communityService;
    private final MemberMapper memberMapper;
    private final ChallengeMapper challengeMapper;
    private final ChallengeService challengeService;
    private final DietMapper dietMapper;
    private final FoodSyncService foodSyncService;

    /** 운영 요약 통계: 회원/식단기록/진행중챌린지/게시글 수 */
    public Map<String, Object> summary() {
        List<Challenge> challenges = challengeMapper.findAll();
        long liveCount = challenges.stream().filter(c -> "진행중".equals(status(c))).count();

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("memberCount", memberMapper.findAll(null).size());
        m.put("dietCount", dietMapper.countAll());
        m.put("liveChallengeCount", (int) liveCount);
        m.put("postCount", communityService.countAllPosts());
        return m;
    }

    /** 회원 운영 목록 (정지 포함, active 상태 노출). Member.password 는 직렬화에서 제외됨 */
    public List<Member> members(String keyword) {
        return memberService.listForOps(keyword);
    }

    /** 게시글 숨김/해제 */
    public void setPostHidden(Integer bno, boolean hidden) {
        communityService.setPostHidden(bno, hidden);
    }

    /** 회원 정지/해제 */
    public void setMemberActive(Integer mno, boolean active) {
        memberService.setActive(mno, active);
    }

    /** 식품 DB 수동 갱신(식약처 API → 로컬 foods upsert) */
    public Map<String, Object> syncFoods() {
        return foodSyncService.sync();
    }

    /** 챌린지 운영 목록: 승인상태/노출/상태/조건/참여자/생성자 (전체) */
    public List<Map<String, Object>> challenges() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Challenge c : challengeMapper.findAll()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("cno", c.getCno());
            m.put("title", c.getTitle());
            m.put("status", status(c));
            m.put("approvalStatus", c.getApprovalStatus());  // PENDING | APPROVED | REJECTED
            m.put("visibility", c.getVisibility());          // VISIBLE | HIDDEN
            m.put("condType", c.getCondType());
            m.put("condValue", c.getCondValue());
            m.put("participants", nz(c.getParticipantCount()));
            m.put("creator", creatorName(c.getMno()));
            result.add(m);
        }
        return result;
    }

    /** 챌린지 승인/거부 (APPROVED/REJECTED/PENDING) */
    public void setChallengeApproval(Integer cno, String status) {
        challengeService.setApproval(cno, status);
    }

    /** 챌린지 숨김/노출 */
    public void setChallengeVisibility(Integer cno, boolean visible) {
        challengeService.setVisibility(cno, visible);
    }

    /** 챌린지 삭제(참가자 있으면 숨김 처리) */
    public String deleteChallenge(Integer cno) {
        return challengeService.opsDelete(cno);
    }

    /** 게시글 운영 목록 (기존 목록 가공 재사용: 게시판/제목/작성자/좋아요/댓글) */
    public List<Map<String, Object>> posts() {
        return communityService.listAllPosts();
    }

    private String creatorName(Integer mno) {
        if (mno == null) return "-";
        Member m = memberMapper.findByMno(mno);
        return m == null ? "-" : m.getName();
    }

    /** 운영 표시용 상태 라벨 (모집중 / 진행중 / 종료) */
    private String status(Challenge c) {
        LocalDate today = LocalDate.now();
        if (c.getStartDate() != null && today.isBefore(c.getStartDate())) return "모집중";
        if (c.getEndDate() != null && today.isAfter(c.getEndDate())) return "종료";
        return "진행중";
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
