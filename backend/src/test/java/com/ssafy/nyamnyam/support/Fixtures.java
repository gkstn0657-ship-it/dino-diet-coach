package com.ssafy.nyamnyam.support;

import com.ssafy.nyamnyam.domain.community.Post;
import com.ssafy.nyamnyam.domain.diet.Diet;
import com.ssafy.nyamnyam.domain.member.Member;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 테스트 픽스처(Object Mother).
 * <p>
 * 테스트가 필요한 데이터를 코드로 명시적으로 생성한다. 기본값을 채워두고
 * 필요한 필드만 바꿔 쓰는 방식이라 "이 테스트가 어떤 데이터를 쓰는지"가 코드에 드러난다.
 */
public final class Fixtures {

    private Fixtures() {}

    /** "1234" 의 BCrypt 해시 (로그인 테스트용) */
    public static final String PW_1234 =
            "$2b$10$syscggn3gTr5.rFl06AFyOxef7drZgkEbyZ6/MFUaYN6I1B6O3JLq";

    // 이메일 충돌 방지용 시퀀스
    private static final AtomicInteger SEQ = new AtomicInteger(0);

    /** 기본 회원 (USER / 목표 근육 증가). email 은 자동 유니크. */
    public static Member member() {
        int n = SEQ.incrementAndGet();
        Member m = new Member();
        m.setEmail("test" + n + "@ssafy.com");
        m.setPassword(PW_1234);
        m.setName("테스트유저" + n);
        m.setRole("USER");
        m.setHeight(175);
        m.setWeight(70);
        m.setGoal("근육 증가");
        return m;
    }

    /** 이메일을 직접 지정한 회원 (로그인 테스트용) */
    public static Member member(String email, String goal) {
        Member m = member();
        m.setEmail(email);
        m.setGoal(goal);
        return m;
    }

    /** 특정 회원의 게시글 (board 지정) */
    public static Post post(int mno, String board, String title) {
        Post p = new Post();
        p.setMno(mno);
        p.setBoard(board);
        p.setTitle(title);
        p.setContent("테스트 게시글 본문");
        return p;
    }

    /** 특정 회원의 식단 1끼 (영양 합계 직접 지정) */
    public static Diet diet(int mno, LocalDate date, String meal,
                            int kcal, int protein, int carbs, int fat) {
        Diet d = new Diet();
        d.setMno(mno);
        d.setMeal(meal);
        d.setEatenDate(date);
        d.setTitle(meal + " 식단");
        d.setTotalKcal(kcal);
        d.setProtein(protein);
        d.setCarbs(carbs);
        d.setFat(fat);
        return d;
    }
}
