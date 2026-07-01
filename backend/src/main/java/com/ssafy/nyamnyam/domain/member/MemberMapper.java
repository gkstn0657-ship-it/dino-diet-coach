package com.ssafy.nyamnyam.domain.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberMapper {
    int insert(Member member);
    Member findByEmail(@Param("email") String email);                 // active=1만 (일반 조회)
    Member findByEmailAny(@Param("email") String email);              // 정지 포함 (로그인 검증용)
    Member findByMno(@Param("mno") Integer mno);
    int countByEmail(@Param("email") String email);
    List<Member> findAll(@Param("keyword") String keyword);
    List<Member> findAllForOps(@Param("keyword") String keyword);     // 정지 포함 (운영 콘솔)
    int updateActive(@Param("mno") Integer mno, @Param("active") boolean active); // 정지/해제
    int updateInfo(Member member);              // 이름/권한/목표 등
    int updateProfile(Member member);            // 키/몸무게/질환
    int updatePassword(@Param("mno") Integer mno, @Param("password") String password); // 비밀번호 변경
    int delete(@Param("mno") Integer mno);

    // 비밀번호 재설정 토큰 (token 컬럼에는 원본이 아닌 해시를 저장)
    int insertResetToken(@Param("mno") Integer mno, @Param("token") String token,
                         @Param("expiresAt") java.time.LocalDateTime expiresAt);
    PasswordResetToken findResetToken(@Param("token") String token);
    int markResetTokenUsed(@Param("id") Integer id);
    int invalidateActiveTokens(@Param("mno") Integer mno); // 새 발급 전 기존 미사용 토큰 무효화
}
