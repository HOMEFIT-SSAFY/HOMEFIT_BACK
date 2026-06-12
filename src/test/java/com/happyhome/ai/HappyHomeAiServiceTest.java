package com.happyhome.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.happyhome.house.dto.HouseDeal;
import com.happyhome.house.service.HouseDealService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.client.RestClient;

class HappyHomeAiServiceTest {

    @Test
    void returnsFriendlyFallbackWhenAiProviderFailsAndNoCompatibleApiKeyExists() {
        ChatClient chatClient = mock(ChatClient.class);
        VectorStore vectorStore = mock(VectorStore.class);
        HouseDealService houseDealService = mock(HouseDealService.class);
        HappyHomeRagDocumentFactory documentFactory = new HappyHomeRagDocumentFactory();
        HouseDeal deal = new HouseDeal();
        deal.setNo(1);
        deal.setAptName("테스트 아파트");
        when(houseDealService.findRecent(20)).thenReturn(List.of(deal));
        doThrow(new RuntimeException("invalid api key")).when(vectorStore).add(anyList());

        HappyHomeAiService service = new HappyHomeAiService(
                chatClient,
                vectorStore,
                houseDealService,
                documentFactory,
                RestClient.builder(),
                "",
                "https://api.openai.com",
                "/v1/chat/completions",
                "gpt-4o-mini",
                20
        );

        AiChatResponse response = service.chat("강남구 실거래가 알려줘");

        assertThat(response.answer()).contains("AI 설정");
        assertThat(response.indexedDocumentCount()).isZero();
    }
}
