package com.ssafy.nyamnyam.domain.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** AI 대화 90일 보존 배치 단위 테스트 */
@ExtendWith(MockitoExtension.class)
class ChatCleanupSchedulerTest {

    @Mock AiService aiService;

    @Test
    @DisplayName("정리 배치는 90일 보존 기준으로 삭제를 위임한다")
    void purgeOldChats_uses90DayRetention() {
        when(aiService.purgeOldChats(90)).thenReturn(3);

        new ChatCleanupScheduler(aiService).purgeOldChats();

        verify(aiService).purgeOldChats(90);
    }
}
