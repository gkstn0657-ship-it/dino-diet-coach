package com.ssafy.nyamnyam.domain.member;

import com.ssafy.nyamnyam.common.CustomException;
import com.ssafy.nyamnyam.support.Fixtures;
import com.ssafy.nyamnyam.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

/**
 * 회원 가입·인증 통합테스트 (실제 MySQL + BCrypt + MyBatis end-to-end).
 * 단위테스트(Mockito)와 달리 실제 DB 라운드트립과 BCrypt 검증을 함께 확인한다.
 */
class MemberAuthIntegrationTest extends IntegrationTest {

    @Autowired MemberService memberService;
    @Autowired MemberMapper memberMapper;

    @Test
    @DisplayName("회원가입 시 비밀번호는 BCrypt로 해싱되어 저장된다")
    void signup_hashesPasswordWithBcrypt() {
        Member m = Fixtures.member();
        m.setPassword("1234"); // 평문으로 가입

        memberService.signup(m);

        Member saved = memberMapper.findByEmail(m.getEmail());
        assertThat(saved).isNotNull();
        assertThat(saved.getPassword()).startsWith("$2");          // BCrypt 해시
        assertThat(saved.getPassword()).isNotEqualTo("1234");      // 평문 저장 안 됨
    }

    @Test
    @DisplayName("올바른 비밀번호로 로그인하면 회원이 반환된다")
    void authenticate_succeedsWithCorrectPassword() {
        Member m = Fixtures.member();
        m.setPassword("1234");
        memberService.signup(m);

        Member auth = memberService.authenticate(m.getEmail(), "1234");

        assertThat(auth.getEmail()).isEqualTo(m.getEmail());
    }

    @Test
    @DisplayName("틀린 비밀번호로 로그인하면 예외가 발생한다")
    void authenticate_failsWithWrongPassword() {
        Member m = Fixtures.member();
        m.setPassword("1234");
        memberService.signup(m);

        assertThatThrownBy(() -> memberService.authenticate(m.getEmail(), "wrong"))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("중복 이메일로 가입하면 예외가 발생한다")
    void signup_failsOnDuplicateEmail() {
        Member m1 = Fixtures.member("dup@ssafy.com", "근육 증가");
        m1.setPassword("1234");
        memberService.signup(m1);

        Member m2 = Fixtures.member("dup@ssafy.com", "체중 감량");
        m2.setPassword("1234");

        assertThatThrownBy(() -> memberService.signup(m2))
                .isInstanceOf(CustomException.class);
    }
}
