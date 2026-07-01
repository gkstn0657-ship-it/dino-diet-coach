package com.ssafy.nyamnyam.domain.follow;

import com.ssafy.nyamnyam.domain.member.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {
    int countFollowers(@Param("mno") Integer mno);
    int countFollowings(@Param("mno") Integer mno);
    int exists(@Param("follower") Integer follower, @Param("following") Integer following);
    int insert(@Param("follower") Integer follower, @Param("following") Integer following);
    int delete(@Param("follower") Integer follower, @Param("following") Integer following);
    List<Member> findFollowers(@Param("mno") Integer mno);
    List<Member> findFollowings(@Param("mno") Integer mno);
}
