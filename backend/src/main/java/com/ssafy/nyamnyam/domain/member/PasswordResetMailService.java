package com.ssafy.nyamnyam.domain.member;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 비밀번호 재설정 메일 발송 (본문 생성 + 발송 책임 분리).
 *
 * - SMTP 가 설정되지 않은 개발 환경에서는 발송을 생략하고 서버 로그로만 링크를 남긴다(앱은 정상 기동).
 * - 발송 실패가 호출부(요청 응답)에 영향을 주지 않도록 내부에서 모두 처리한다(계정 존재 여부 비노출).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetMailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.mail.from:DinoDiet <no-reply@dinodiet.app>}")
    private String from;

    @Value("${spring.mail.host:}")
    private String mailHost;

    private static final long TTL_MINUTES = 30;

    /** 재설정 링크 메일 발송. rawToken 은 이메일에만 담기고 DB 에는 해시로 저장된다. */
    public void sendResetLink(String email, String rawToken) {
        String link = frontendBaseUrl.replaceAll("/+$", "") + "/member/reset-password?token=" + rawToken;

        JavaMailSender sender = (mailHost == null || mailHost.isBlank())
                ? null : mailSenderProvider.getIfAvailable();
        if (sender == null) {
            // 개발 환경(SMTP 미설정) — 사용자 화면엔 노출되지 않는 서버 로그 fallback
            log.info("[비밀번호 재설정][DEV] SMTP 미설정 — {} 재설정 링크: {}", email, link);
            return;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(email);
            helper.setFrom(from);
            helper.setSubject("[DinoDiet] 비밀번호 재설정 안내");
            helper.setText(buildHtml(link), true);
            sender.send(message);
            log.info("[비밀번호 재설정] {} 메일 발송 완료", email);
        } catch (Exception e) {
            // 발송 실패해도 응답은 동일하게 유지. 개발 추적용 로그만 남긴다.
            log.warn("[비밀번호 재설정] {} 메일 발송 실패({}) — 링크: {}", email, e.getMessage(), link);
        }
    }

    private String buildHtml(String link) {
        return """
                <div style="font-family:'Apple SD Gothic Neo',sans-serif;max-width:480px;margin:0 auto;color:#333;">
                  <h2 style="color:#2f9e8f;">🦕 DinoDiet 비밀번호 재설정</h2>
                  <p>아래 버튼을 눌러 새 비밀번호를 설정해 주세요.</p>
                  <p style="margin:24px 0;">
                    <a href="%s" style="background:#77C0B3;color:#fff;text-decoration:none;
                       padding:12px 22px;border-radius:8px;font-weight:bold;display:inline-block;">
                      비밀번호 재설정하기
                    </a>
                  </p>
                  <p style="font-size:13px;color:#666;">버튼이 동작하지 않으면 아래 주소를 복사해 접속하세요.<br>
                    <span style="word-break:break-all;color:#2f9e8f;">%s</span></p>
                  <p style="font-size:13px;color:#666;">이 링크는 <b>%d분간</b>만 유효하며, 한 번만 사용할 수 있어요.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin:20px 0;">
                  <p style="font-size:12px;color:#999;">본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.</p>
                </div>
                """.formatted(link, link, TTL_MINUTES);
    }
}
