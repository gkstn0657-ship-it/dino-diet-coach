package com.ssafy.nyamnyam.domain.challenge;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChallengeMapper {
    int insert(Challenge challenge);
    List<Challenge> findAll();                                  // 운영 콘솔: 전체(승인/노출 무관)
    List<Challenge> findPublic(@Param("sort") String sort);     // 일반 목록: APPROVED + VISIBLE
    Challenge findById(@Param("cno") Integer cno);
    int delete(@Param("cno") Integer cno);
    int setApproval(@Param("cno") Integer cno, @Param("status") String status);   // 승인/거부
    int setVisibility(@Param("cno") Integer cno, @Param("visibility") String visibility); // 숨김/노출

    ChallengeParticipant findParticipant(@Param("cno") Integer cno, @Param("mno") Integer mno);
    int insertParticipant(ChallengeParticipant p);
    int deleteParticipant(@Param("cno") Integer cno, @Param("mno") Integer mno); // 참여 취소
    int updateParticipant(@Param("cno") Integer cno, @Param("mno") Integer mno,
                          @Param("doneDays") Integer doneDays, @Param("progress") Integer progress,
                          @Param("checkDate") java.time.LocalDate checkDate);
    List<Challenge> findMyChallenges(@Param("mno") Integer mno);
    List<Challenge> findByCreator(@Param("mno") Integer mno); // 사용자 프로필: 내가 등록(신청)한 챌린지

    /** 리더보드: 달성률 상위 10명 (name, mno, progress, doneDays) */
    List<java.util.Map<String, Object>> leaderboard(@Param("cno") Integer cno);

    /** 회원이 참여 중인 챌린지 번호 목록 (목록의 '참여중' 배지용) */
    List<Integer> findJoinedCnos(@Param("mno") Integer mno);

    /** 챌린지의 전체 참여자 (자동 인증 배치용) */
    List<ChallengeParticipant> findParticipants(@Param("cno") Integer cno);
}
