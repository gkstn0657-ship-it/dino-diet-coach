package com.ssafy.nyamnyam.domain.community;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Integer cno;
    private Integer bno;
    private Integer mno;
    private String content;
    private LocalDateTime createdAt;

    private String author;
    private String authorEmail;  // 작성자 프로필 이동용
}
