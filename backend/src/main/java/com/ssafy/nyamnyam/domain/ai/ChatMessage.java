package com.ssafy.nyamnyam.domain.ai;

import lombok.Data;

import java.time.LocalDateTime;

/** AI 코치 대화 메시지 (chat_messages 테이블) */
@Data
public class ChatMessage {
    private Integer cmno;
    private Integer mno;
    private String coach;      // powerrex | slimdino | balanceno
    private String role;       // user | assistant
    private String content;
    private LocalDateTime createdAt;
}
