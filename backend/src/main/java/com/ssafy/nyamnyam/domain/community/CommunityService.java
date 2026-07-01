package com.ssafy.nyamnyam.domain.community;

import com.ssafy.nyamnyam.common.CustomException;
import com.ssafy.nyamnyam.common.InputSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityMapper mapper;

    private static final int PAGE_SIZE = 10;    // 페이지당 게시글 수
    private static final int PAGE_BLOCK = 10;   // 화면에 보일 페이지 번호 개수
    /** 운영 콘솔 전체 목록 조회용 상한 (페이징 없이 한 번에) */
    private static final int OPS_MAX = 100000;

    private static final Map<String, String> BOARD = Map.of(
            "review", "리뷰", "expert", "칼럼", "free", "자유", "qna", "질문"
    );
    // 운영자(ADMIN/OPERATOR)만 작성할 수 있는 게시판 (칼럼=expert)
    private static final Set<String> OPERATOR_ONLY_BOARDS = Set.of("expert");

    private String boardLabel(String board) {
        return BOARD.getOrDefault(board, "자유");
    }

    /** 사용자 프로필: 특정 회원이 작성한 글 목록 */
    public List<Map<String, Object>> postsByMember(Integer mno) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Post p : mapper.findByAuthor(mno)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("bno", p.getBno());
            m.put("title", p.getTitle());
            m.put("boardLabel", boardLabel(p.getBoard()));
            m.put("likes", p.getLikes());
            m.put("comments", p.getCommentCount());
            result.add(m);
        }
        return result;
    }

    /** 게시글 목록 1건 → 프론트 카드용 가공 */
    private Map<String, Object> toItem(Post p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bno", p.getBno());
        m.put("boardLabel", boardLabel(p.getBoard()));
        m.put("title", p.getTitle());
        m.put("author", p.getAuthor());
        m.put("authorEmail", p.getAuthorEmail());
        m.put("comments", p.getCommentCount());
        m.put("likes", p.getLikes());
        m.put("liked", Boolean.TRUE.equals(p.getLiked()));
        m.put("hidden", Boolean.TRUE.equals(p.getHidden()));
        return m;
    }

    /**
     * 게시글 목록 페이지 단위 조회.
     * @param currentMno 로그인 사용자 mno (각 글의 liked 계산용)
     * @return { posts: [...], page: { currentPage, startPage, endPage, totalPages, totalCount, hasPre, hasNext } }
     */
    public Map<String, Object> listPosts(String board, int currentPage, Integer currentMno) {
        if (currentPage < 1) currentPage = 1;

        int totalCount = mapper.countPosts(board);
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages; // 범위 초과 보정(빈 목록 방지)

        int offset = (currentPage - 1) * PAGE_SIZE;
        List<Map<String, Object>> posts = new ArrayList<>();
        for (Post p : mapper.findPosts(board, offset, PAGE_SIZE, currentMno)) {
            posts.add(toItem(p));
        }

        int startPage = ((currentPage - 1) / PAGE_BLOCK) * PAGE_BLOCK + 1;
        int endPage = Math.min(startPage + PAGE_BLOCK - 1, totalPages);

        Map<String, Object> page = new LinkedHashMap<>();
        page.put("currentPage", currentPage);
        page.put("startPage", startPage);
        page.put("endPage", endPage);
        page.put("totalPages", totalPages);
        page.put("totalCount", totalCount);
        page.put("hasPre", startPage > 1);
        page.put("hasNext", endPage < totalPages);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("posts", posts);
        result.put("page", page);
        return result;
    }

    /** 운영 콘솔용: 전체 게시글 목록(페이징 없음) */
    public List<Map<String, Object>> listAllPosts() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Post p : mapper.findAllForOps()) {   // 숨김 포함 전체
            result.add(toItem(p));
        }
        return result;
    }

    /** 운영 콘솔: 게시글 숨김/해제 */
    @Transactional
    public void setPostHidden(Integer bno, boolean hidden) {
        if (mapper.findPost(bno, null) == null) {
            throw CustomException.notFound("게시글을 찾을 수 없습니다.");
        }
        mapper.setHidden(bno, hidden);
    }

    /** 운영 콘솔용: 전체 게시글 수 */
    public int countAllPosts() {
        return mapper.countPosts(null);
    }

    /** 오늘 작성 게시글 중 좋아요 상위 3개 (없으면 빈 배열) */
    public List<Map<String, Object>> topTodayPosts() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Post p : mapper.findTopToday(3)) {
            result.add(toItem(p));
        }
        return result;
    }

    /**
     * 게시글 좋아요 토글 (로그인 사용자 기준).
     * 이미 좋아요면 취소, 아니면 추가. posts.likes 동기화.
     * @return { liked, likes }
     */
    @Transactional
    public Map<String, Object> toggleLike(Integer bno, Integer mno) {
        if (mapper.findPost(bno, null) == null) {
            throw CustomException.notFound("게시글을 찾을 수 없습니다.");
        }
        boolean liked;
        if (mapper.existsLike(mno, bno) > 0) {
            mapper.deleteLike(mno, bno);
            mapper.updateLikes(bno, -1);
            liked = false;
        } else {
            mapper.insertLike(mno, bno);
            mapper.updateLikes(bno, 1);
            liked = true;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("liked", liked);
        m.put("likes", mapper.selectLikes(bno));
        return m;
    }

    /** 작성자 본인 또는 운영자(OPERATOR/ADMIN)면 수정·삭제 가능 — 공통 정의(Roles) 참조 */
    private static final Set<String> OPERATOR_ROLES = com.ssafy.nyamnyam.common.Roles.OPERATOR_ROLES;
    private boolean canManage(Post p, Integer currentMno, String role) {
        return (currentMno != null && currentMno.equals(p.getMno()))
                || (role != null && OPERATOR_ROLES.contains(role));
    }

    public Map<String, Object> postDetail(Integer bno, Integer currentMno, String role) {
        Post p = mapper.findPost(bno, currentMno);
        if (p == null) throw CustomException.notFound("게시글을 찾을 수 없습니다.");
        // 숨김 글은 일반 사용자에게 노출하지 않음 (작성자 본인/운영자만 열람 가능)
        if (Boolean.TRUE.equals(p.getHidden()) && !canManage(p, currentMno, role)) {
            throw CustomException.notFound("게시글을 찾을 수 없습니다.");
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bno", p.getBno());
        m.put("board", p.getBoard());
        m.put("boardLabel", boardLabel(p.getBoard()));
        m.put("title", p.getTitle());
        m.put("author", p.getAuthor());
        m.put("authorEmail", p.getAuthorEmail());
        m.put("content", p.getContent());
        m.put("likes", p.getLikes());
        m.put("liked", Boolean.TRUE.equals(p.getLiked()));
        m.put("date", p.getCreatedAt() == null ? "" :
                p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        // 프론트가 수정/삭제 버튼 노출 여부를 판단 (작성자 본인 또는 운영자)
        m.put("canManage", canManage(p, currentMno, role));
        return m;
    }

    /** 허용 게시판만 통과(그 외/누락은 자유게시판으로) */
    private static final Set<String> BOARDS = Set.of("free", "review", "expert", "qna");
    private String normalizeBoard(String board) {
        return (board != null && BOARDS.contains(board)) ? board : "free";
    }

    @Transactional
    public Integer createPost(Integer mno, String role, String board, String title, String content) {
        String b = normalizeBoard(board);
        // 칼럼(expert)은 운영자만 작성 가능
        if (OPERATOR_ONLY_BOARDS.contains(b) && !(role != null && OPERATOR_ROLES.contains(role))) {
            throw CustomException.forbidden("칼럼은 운영자만 작성할 수 있어요.");
        }
        Post p = new Post();
        p.setMno(mno);
        p.setBoard(b);
        p.setTitle(InputSanitizer.required(title, "제목", 200));
        p.setContent(InputSanitizer.required(content, "내용", 5000));
        mapper.insertPost(p);
        return p.getBno();
    }

    @Transactional
    public void updatePost(Integer bno, Integer currentMno, String role, String title, String content) {
        Post existing = mapper.findPost(bno, null);
        if (existing == null) throw CustomException.notFound("게시글을 찾을 수 없습니다.");
        if (!canManage(existing, currentMno, role)) {
            throw CustomException.forbidden("본인이 작성한 글만 수정할 수 있습니다.");
        }
        Post p = new Post();
        p.setBno(bno);
        p.setTitle(InputSanitizer.required(title, "제목", 200));
        p.setContent(InputSanitizer.required(content, "내용", 5000));
        mapper.updatePost(p);
    }

    @Transactional
    public void deletePost(Integer bno, Integer currentMno, String role) {
        Post existing = mapper.findPost(bno, null);
        if (existing == null) throw CustomException.notFound("게시글을 찾을 수 없습니다.");
        if (!canManage(existing, currentMno, role)) {
            throw CustomException.forbidden("본인이 작성한 글만 삭제할 수 있습니다.");
        }
        mapper.deletePost(bno);
    }

    public List<Map<String, Object>> comments(Integer bno, Integer currentMno, String role) {
        boolean operator = role != null && OPERATOR_ROLES.contains(role);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment c : mapper.findComments(bno)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("cno", c.getCno());
            m.put("author", c.getAuthor());
            m.put("authorEmail", c.getAuthorEmail());
            m.put("content", c.getContent());
            // 본인 댓글 또는 운영자면 수정/삭제 가능
            m.put("canManage", operator || (currentMno != null && currentMno.equals(c.getMno())));
            result.add(m);
        }
        return result;
    }

    @Transactional
    public Integer addComment(Integer bno, Integer mno, String content) {
        Comment c = new Comment();
        c.setBno(bno);
        c.setMno(mno);
        c.setContent(InputSanitizer.required(content, "댓글", 500));
        mapper.insertComment(c);
        return c.getCno();
    }

    @Transactional
    public void updateComment(Integer cno, Integer currentMno, String role, String content) {
        Comment existing = mapper.findComment(cno);
        if (existing == null) throw CustomException.notFound("댓글을 찾을 수 없습니다.");
        if (!canManageComment(existing, currentMno, role)) {
            throw CustomException.forbidden("본인이 작성한 댓글만 수정할 수 있습니다.");
        }
        mapper.updateComment(cno, InputSanitizer.required(content, "댓글", 500));
    }

    @Transactional
    public void deleteComment(Integer cno, Integer currentMno, String role) {
        Comment existing = mapper.findComment(cno);
        if (existing == null) throw CustomException.notFound("댓글을 찾을 수 없습니다.");
        if (!canManageComment(existing, currentMno, role)) {
            throw CustomException.forbidden("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }
        mapper.deleteComment(cno);
    }

    /** 댓글 작성자 본인 또는 운영자면 수정·삭제 가능 */
    private boolean canManageComment(Comment c, Integer currentMno, String role) {
        return (currentMno != null && currentMno.equals(c.getMno()))
                || (role != null && OPERATOR_ROLES.contains(role));
    }
}
