package com.ssafy.nyamnyam.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 통합테스트 베이스 클래스.
 * <p>
 * 현업 방식: 운영과 동일한 MySQL 을 Testcontainers 로 일회용 띄우고,
 * 각 테스트는 {@link Transactional} 로 끝나면 롤백되어 서로 간섭하지 않는다.
 *
 * <ul>
 *   <li><b>Testcontainers</b> — 운영과 같은 MySQL 8 컨테이너 (H2 문법 차이 문제 없음)</li>
 *   <li><b>@DynamicPropertySource</b> — 컨테이너 접속정보를 스프링에 주입</li>
 *   <li><b>@Transactional</b> — 테스트 메서드 종료 시 자동 롤백 (격리)</li>
 *   <li>스키마는 운영 schema.sql 을 그대로 사용 → 운영 스키마와 일치 검증</li>
 * </ul>
 *
 * <p>주의: 로컬/CI 에 <b>Docker 가 실행 중</b>이어야 한다.
 */
@SpringBootTest
@Testcontainers
@Transactional
public abstract class IntegrationTest {

    // 컨테이너는 모든 통합테스트가 공유 (static → 클래스당 1회 재사용, 테스트마다 새로 안 띄움)
    @SuppressWarnings("resource")
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("nyamnyam_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        // 컨테이너 접속정보를 datasource 로 주입
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        // 운영 schema.sql 로 스키마 생성 (운영 스키마와 동일하게)
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:schema.sql");
        // 통합테스트에서는 DataSeeder 등 외부 시드/AI 비활성
        registry.add("ai.gms.enabled", () -> "false");

        // 컨텍스트 기동에 필요한 시크릿은 테스트용 더미로 주입 (운영 키 불필요)
        registry.add("jwt.secret", () -> "test-secret-key-for-integration-tests-1234567890");
        registry.add("ai.gms.api-key", () -> "test-key");
        registry.add("spring.ai.openai.api-key", () -> "test-key");
        registry.add("food.api.key", () -> "test-key");
    }
}
