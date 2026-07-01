package com.ssafy.nyamnyam.domain.community;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private Integer bno;
    private Integer mno;
    private String board;        // review/expert/free
    private String title;
    private String content;
    private Integer likes;
    private Boolean hidden;       // 운영 숨김 여부
    private LocalDateTime createdAt;

    // 조인/가공용
    private String author;
    private String authorEmail;  // 작성자 프로필 이동용
    private Integer commentCount;
    private Boolean liked;       // 현재 로그인 사용자의 좋아요 여부 (EXISTS 조회)
}
