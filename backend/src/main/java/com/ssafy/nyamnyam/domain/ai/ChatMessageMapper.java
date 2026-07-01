package com.ssafy.nyamnyam.domain.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    int insert(ChatMessage message);

    /** 회원·코치별 최근 메시지 limit건 (오래된 순으로 반환) */
    List<ChatMessage> findRecent(@Param("mno") Integer mno,
                                 @Param("coach") String coach,
                                 @Param("limit") int limit);

    /** days일 이전 메시지 일괄 삭제 (90일 보존 정책 배치용) */
    int deleteOlderThan(@Param("days") int days);
}
