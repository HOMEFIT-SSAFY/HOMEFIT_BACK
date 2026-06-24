package com.happyhome.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.happyhome.analysis.dto.AnalysisScore;
import com.happyhome.analysis.dto.HousingAnalysis;
import com.happyhome.analysis.dto.TransitSummary;
import com.happyhome.analysis.service.AnalysisService;
import com.happyhome.commercial.dto.CommercialSummary;
import com.happyhome.house.dto.HouseDeal;
import com.happyhome.house.service.HouseDealService;
import com.happyhome.rental.service.RentalService;
import com.happyhome.traffic.dto.TrafficRiskSummary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HappyHomeAiServiceTest {

    @Test
    void returnsLocalDealAnswerWhenAiProviderFailsAndNoCompatibleApiKeyExists() {
        ChatClient chatClient = mock(ChatClient.class);
        VectorStore vectorStore = mock(VectorStore.class);
        HouseDealService houseDealService = mock(HouseDealService.class);
        RentalService rentalService = mock(RentalService.class);
        AnalysisService analysisService = mock(AnalysisService.class);
        HappyHomeRagDocumentFactory documentFactory = new HappyHomeRagDocumentFactory();
        HouseDeal deal = houseDeal();
        when(houseDealService.search(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(deal));
        when(houseDealService.findRecent(20)).thenReturn(List.of(deal));
        doThrow(new RuntimeException("invalid api key")).when(vectorStore).add(anyList());

        HappyHomeAiService service = newService(
                chatClient,
                vectorStore,
                houseDealService,
                rentalService,
                analysisService,
                documentFactory,
                RestClient.builder(),
                ""
        );

        AiChatResponse response = service.chat("강남구 최근 실거래가 알려줘");

        assertThat(response.answer())
                .contains("테스트아파트", "강남구", "120,000")
                .doesNotContain("AI 설정", "GMS API");
        assertThat(response.indexedDocumentCount()).isEqualTo(1);
    }

    @Test
    void sendsMaxCompletionTokensWhenUsingGmsCompatibleApi() {
        ChatClient chatClient = mock(ChatClient.class);
        VectorStore vectorStore = mock(VectorStore.class);
        HouseDealService houseDealService = mock(HouseDealService.class);
        RentalService rentalService = mock(RentalService.class);
        AnalysisService analysisService = mock(AnalysisService.class);
        HappyHomeRagDocumentFactory documentFactory = new HappyHomeRagDocumentFactory();
        when(houseDealService.findRecent(20)).thenReturn(List.of());
        when(houseDealService.findRecent(8)).thenReturn(List.of());

        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        server.expect(requestTo("https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer gms-key"))
                .andExpect(content().string(allOf(
                        containsString("\"max_completion_tokens\""),
                        not(containsString("\"max_tokens\""))
                )))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"GMS 응답 성공"}}]}
                        """, MediaType.APPLICATION_JSON));

        HappyHomeAiService service = newService(
                chatClient,
                vectorStore,
                houseDealService,
                rentalService,
                analysisService,
                documentFactory,
                restClientBuilder,
                "gms-key"
        );

        AiChatResponse response = service.chat("ping");

        assertThat(response.answer()).isEqualTo("GMS 응답 성공");
        server.verify();
    }

    @Test
    void answersNeighborhoodQuestionFromAnalysisContextWhenAiProviderFails() {
        ChatClient chatClient = mock(ChatClient.class);
        VectorStore vectorStore = mock(VectorStore.class);
        HouseDealService houseDealService = mock(HouseDealService.class);
        RentalService rentalService = mock(RentalService.class);
        AnalysisService analysisService = mock(AnalysisService.class);
        HappyHomeRagDocumentFactory documentFactory = new HappyHomeRagDocumentFactory();
        when(houseDealService.findRecent(20)).thenReturn(List.of(houseDeal()));
        doThrow(new RuntimeException("provider down")).when(vectorStore).add(anyList());
        when(analysisService.analyze("강남역", 127.0276, 37.4979, 1000))
                .thenReturn(new HousingAnalysis(
                        "강남역",
                        37.4979,
                        127.0276,
                        1000,
                        new CommercialSummary(80, 30, 20, 5, 10, 15),
                        new TrafficRiskSummary(2, 1, "보통"),
                        new TransitSummary(3, 7, 8, 12, 20),
                        new AnalysisScore(90, 25, 30, 5, "좋음", "상권과 교통이 풍부합니다."),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        "api"
                ));

        HappyHomeAiService service = newService(
                chatClient,
                vectorStore,
                houseDealService,
                rentalService,
                analysisService,
                documentFactory,
                RestClient.builder(),
                ""
        );

        AiChatResponse response = service.chat(new AiChatRequest(
                "이 지역 생활 편의성 어때?",
                "analysis",
                "/analysis",
                "강남역",
                37.4979,
                127.0276,
                1000
        ));

        assertThat(response.answer())
                .contains("생활권 분석", "90점", "상권", "80", "버스정류장", "12", "지하철", "7")
                .doesNotContain("AI 설정", "GMS API");
        assertThat(response.indexedDocumentCount()).isEqualTo(1);
    }

    private HappyHomeAiService newService(
            ChatClient chatClient,
            VectorStore vectorStore,
            HouseDealService houseDealService,
            RentalService rentalService,
            AnalysisService analysisService,
            HappyHomeRagDocumentFactory documentFactory,
            RestClient.Builder restClientBuilder,
            String apiKey
    ) {
        return new HappyHomeAiService(
                chatClient,
                vectorStore,
                houseDealService,
                rentalService,
                analysisService,
                documentFactory,
                restClientBuilder,
                apiKey,
                apiKey.isBlank() ? "https://api.openai.com" : "https://gms.ssafy.io/gmsapi/api.openai.com",
                "/v1/chat/completions",
                apiKey.isBlank() ? "gpt-4o-mini" : "gpt-5.4-mini",
                20
        );
    }

    private HouseDeal houseDeal() {
        HouseDeal deal = new HouseDeal();
        deal.setNo(1);
        deal.setAptName("테스트아파트");
        deal.setSidoName("서울특별시");
        deal.setGugunName("강남구");
        deal.setDongName("역삼동");
        deal.setDealAmount("120,000");
        deal.setDealYear(2026);
        deal.setDealMonth(6);
        deal.setDealDay(20);
        return deal;
    }
}
