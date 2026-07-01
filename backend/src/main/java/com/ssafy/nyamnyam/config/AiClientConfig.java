package com.ssafy.nyamnyam.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient 구성.
 * base-url / api-key / model 은 application.properties 의 spring.ai.openai.* 에서 주입된다.
 */
@Configuration
public class AiClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
