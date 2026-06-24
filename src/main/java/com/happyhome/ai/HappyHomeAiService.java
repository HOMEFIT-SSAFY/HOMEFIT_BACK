package com.happyhome.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyhome.analysis.dto.HousingAnalysis;
import com.happyhome.analysis.service.AnalysisService;
import com.happyhome.house.dto.HouseDeal;
import com.happyhome.house.dto.HouseSearchCondition;
import com.happyhome.house.service.HouseDealService;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.service.RentalService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class HappyHomeAiService {

    private static final Logger log = LoggerFactory.getLogger(HappyHomeAiService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final HouseDealService houseDealService;
    private final RentalService rentalService;
    private final AnalysisService analysisService;
    private final HappyHomeRagDocumentFactory documentFactory;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final int bootstrapLimit;
    private final String apiKey;
    private final String baseUrl;
    private final String completionsPath;
    private final String chatModel;
    private boolean indexed;
    private int indexedDocumentCount;

    public HappyHomeAiService(
            ChatClient chatClient,
            VectorStore vectorStore,
            HouseDealService houseDealService,
            RentalService rentalService,
            AnalysisService analysisService,
            HappyHomeRagDocumentFactory documentFactory,
            RestClient.Builder restClientBuilder,
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${spring.ai.openai.chat.base-url:${spring.ai.openai.base-url:https://api.openai.com}}") String baseUrl,
            @Value("${spring.ai.openai.chat.completions-path:/v1/chat/completions}") String completionsPath,
            @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}") String chatModel,
            @Value("${happyhome.ai.rag.bootstrap-limit:20}") int bootstrapLimit
    ) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.houseDealService = houseDealService;
        this.rentalService = rentalService;
        this.analysisService = analysisService;
        this.documentFactory = documentFactory;
        this.restClient = restClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.completionsPath = completionsPath;
        this.chatModel = chatModel;
        this.bootstrapLimit = bootstrapLimit;
    }

    public AiChatResponse chat(String question) {
        return chat(new AiChatRequest(question, null, null, null, null, null, null));
    }

    public AiChatResponse chat(AiChatRequest request) {
        String question = request.question();
        ChatContext context = buildContext(request);
        try {
            ensureIndexed();

            String answer = chatClient.prompt()
                    .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                            .searchRequest(SearchRequest.builder()
                                    .topK(5)
                                    .similarityThreshold(0.2)
                            .build())
                            .build())
                    .user(promptWithContext(question, context))
                    .call()
                    .content();

            return new AiChatResponse(answer, indexedDocumentCount);
        } catch (RuntimeException exception) {
            log.warn("HappyHome AI chat failed: {}", exception.getMessage());
            return chatWithGmsCompatibleApi(question, context);
        }
    }

    private AiChatResponse chatWithGmsCompatibleApi(String question, ChatContext context) {
        List<HouseDeal> recentDeals = context.deals().isEmpty()
                ? safeRecentDeals(Math.min(bootstrapLimit, 8))
                : context.deals();
        ChatContext usableContext = context.withDeals(recentDeals);
        if (!StringUtils.hasText(apiKey)) {
            return new AiChatResponse(localContextAnswer(question, usableContext), usableContext.documentCount());
        }

        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", """
                            You are HappyHome's real-estate assistant.
                            Answer in Korean.
                            Use the provided HappyHome context first.
                            If the context is not enough, say what information is missing instead of inventing facts.
                            """
            ));
            messages.add(Map.of(
                    "role", "user",
                    "content", promptWithContext(question, usableContext)
            ));

            Map<String, Object> requestBody = Map.of(
                    "model", chatModel,
                    "messages", messages,
                    "max_completion_tokens", 1024,
                    "temperature", 0.3
            );

            String responseBody = restClient.post()
                    .uri(gmsChatUrl())
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            String answer = objectMapper.readTree(responseBody)
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("");

            if (!StringUtils.hasText(answer)) {
                return new AiChatResponse(localContextAnswer(question, usableContext), usableContext.documentCount());
            }

            return new AiChatResponse(answer, usableContext.documentCount());
        } catch (IOException | RuntimeException exception) {
            log.warn("HappyHome GMS-compatible chat failed: {}", exception.getMessage());
            return new AiChatResponse(localContextAnswer(question, usableContext), usableContext.documentCount());
        }
    }

    private ChatContext buildContext(AiChatRequest request) {
        String question = request.question();
        List<HouseDeal> deals = asksAboutDeals(question)
                ? safeSearchDeals(question, Math.min(bootstrapLimit, 8))
                : List.of();
        List<RentalNotice> notices = asksAboutRentals(question)
                ? safeRentalNotices(question, 5)
                : List.of();
        HousingAnalysis analysis = asksAboutNeighborhood(question)
                ? safeAnalysis(request)
                : null;
        return new ChatContext(deals, notices, analysis);
    }

    private String promptWithContext(String question, ChatContext context) {
        return "HappyHome context:\n" + contextText(context) + "\n\nUser question:\n" + question;
    }

    private String contextText(ChatContext context) {
        StringBuilder builder = new StringBuilder();
        if (context.analysis() != null) {
            appendAnalysis(builder, context.analysis());
        }
        if (!context.deals().isEmpty()) {
            appendSection(builder, "Recent house deals");
            builder.append(contextFromDeals(context.deals()));
        }
        if (!context.notices().isEmpty()) {
            appendSection(builder, "Rental notices");
            for (RentalNotice notice : context.notices()) {
                builder.append("- ")
                        .append(value(notice.title()))
                        .append(" / ")
                        .append(value(notice.regionName()))
                        .append(" / ")
                        .append(value(notice.status()))
                        .append(" / noticeDate: ")
                        .append(value(notice.noticeDate()))
                        .append(" / closeDate: ")
                        .append(value(notice.closeDate()))
                        .append("\n");
            }
        }
        if (builder.length() == 0) {
            return "No page-specific HappyHome context is available. Ask for a location or keyword when needed.";
        }
        return builder.toString();
    }

    private void appendAnalysis(StringBuilder builder, HousingAnalysis analysis) {
        appendSection(builder, "Neighborhood analysis");
        builder.append("label: ").append(value(analysis.label())).append("\n")
                .append("lat/lng: ").append(analysis.latitude()).append(", ").append(analysis.longitude()).append("\n")
                .append("radiusMeters: ").append(analysis.radiusMeters()).append("\n")
                .append("totalScore: ").append(analysis.score().total()).append("\n")
                .append("level: ").append(value(analysis.score().level())).append("\n")
                .append("commercialScore: ").append(analysis.score().commercialScore()).append("\n")
                .append("transitScore: ").append(analysis.score().transitScore()).append("\n")
                .append("trafficSafetyScore: ").append(analysis.score().trafficSafetyScore()).append("\n")
                .append("commercialPlaces: ").append(analysis.commercialSummary().totalCount()).append("\n")
                .append("food: ").append(analysis.commercialSummary().foodCount()).append("\n")
                .append("cafes: ").append(analysis.commercialSummary().cafeCount()).append("\n")
                .append("medical: ").append(analysis.commercialSummary().medicalCount()).append("\n")
                .append("convenience: ").append(analysis.commercialSummary().convenienceCount()).append("\n")
                .append("busStopsWithin500m: ").append(analysis.transitSummary().busStopWithin500m()).append("\n")
                .append("subwayWithin1000m: ").append(analysis.transitSummary().subwayWithin1000m()).append("\n")
                .append("trafficEvents: ").append(analysis.trafficRiskSummary().eventCount()).append("\n");
    }

    private void appendSection(StringBuilder builder, String title) {
        if (builder.length() > 0) {
            builder.append("\n---\n");
        }
        builder.append(title).append("\n");
    }

    private List<HouseDeal> safeSearchDeals(String question, int limit) {
        try {
            HouseSearchCondition condition = new HouseSearchCondition();
            condition.setKeyword(extractKeyword(question));
            condition.setLimit(limit);
            List<HouseDeal> deals = houseDealService.search(condition);
            if (!deals.isEmpty()) {
                return deals;
            }
            return safeRecentDeals(limit);
        } catch (RuntimeException exception) {
            log.warn("HappyHome deal context lookup failed: {}", exception.getMessage());
            return safeRecentDeals(limit);
        }
    }

    private List<RentalNotice> safeRentalNotices(String question, int limit) {
        try {
            List<RentalNotice> notices = rentalService.notices(new RentalSearchCondition(extractKeyword(question), null, null, 1, limit));
            if (!notices.isEmpty()) {
                return notices;
            }
            return rentalService.notices(new RentalSearchCondition(null, null, null, 1, limit));
        } catch (RuntimeException exception) {
            log.warn("HappyHome rental context lookup failed: {}", exception.getMessage());
            return List.of();
        }
    }

    private HousingAnalysis safeAnalysis(AiChatRequest request) {
        double latitude = request.latitude() != null ? request.latitude() : 37.5665;
        double longitude = request.longitude() != null ? request.longitude() : 126.9780;
        int radiusMeters = request.radiusMeters() != null && request.radiusMeters() > 0 ? request.radiusMeters() : 1000;
        String label = StringUtils.hasText(request.label()) ? request.label() : "현재 지도 위치";
        try {
            return analysisService.analyze(label, longitude, latitude, radiusMeters);
        } catch (RuntimeException exception) {
            log.warn("HappyHome analysis context lookup failed: {}", exception.getMessage());
            return null;
        }
    }

    private boolean asksAboutDeals(String question) {
        return containsAny(question, "실거래", "거래가", "매매", "아파트", "가격", "시세");
    }

    private boolean asksAboutRentals(String question) {
        return containsAny(question, "임대", "청년", "공고", "lh", "LH", "분양");
    }

    private boolean asksAboutNeighborhood(String question) {
        return containsAny(question, "생활권", "편의", "상권", "교통", "주변", "입지", "동네");
    }

    private boolean containsAny(String question, String... keywords) {
        if (!StringUtils.hasText(question)) {
            return false;
        }
        for (String keyword : keywords) {
            if (question.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String extractKeyword(String question) {
        if (!StringUtils.hasText(question)) {
            return null;
        }
        String keyword = question
                .replace("최근", " ")
                .replace("실거래가", " ")
                .replace("실거래", " ")
                .replace("거래가", " ")
                .replace("알려줘", " ")
                .replace("찾아줘", " ")
                .replace("공고", " ")
                .replace("임대", " ")
                .replace("어때", " ")
                .replace("?", " ")
                .trim();
        return StringUtils.hasText(keyword) ? keyword : null;
    }

    private List<HouseDeal> safeRecentDeals(int limit) {
        try {
            return houseDealService.findRecent(limit);
        } catch (RuntimeException exception) {
            log.warn("HappyHome local deal fallback failed: {}", exception.getMessage());
            return List.of();
        }
    }

    private String contextFromDeals(List<HouseDeal> deals) {
        List<Document> documents = documentFactory.fromHouseDeals(deals);
        if (documents.isEmpty()) {
            return "No recent house deal context is available.";
        }

        StringBuilder builder = new StringBuilder();
        for (Document document : documents) {
            if (builder.length() > 0) {
                builder.append("\n---\n");
            }
            builder.append(document.getText());
        }
        return builder.toString();
    }

    private String gmsChatUrl() {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = completionsPath.startsWith("/") ? completionsPath : "/" + completionsPath;
        return normalizedBaseUrl + normalizedPath;
    }

    private String localDealAnswer(String question, List<HouseDeal> deals) {
        if (deals == null || deals.isEmpty()) {
            return """
                    지금은 AI 응답 서버 연결이 불안정해서 간단 답변으로 안내할게요.
                    현재 조회 가능한 최근 실거래 데이터가 없어서 지역명이나 아파트명을 바꿔 다시 검색해 주세요.
                    """;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("AI 응답 서버 연결이 불안정해서 최근 실거래 데이터 기준으로 먼저 안내할게요.\n");
        builder.append("질문: ").append(question).append("\n\n");
        int count = Math.min(deals.size(), 5);
        for (int index = 0; index < count; index++) {
            HouseDeal deal = deals.get(index);
            builder.append(index + 1)
                    .append(". ")
                    .append(value(deal.getAptName()))
                    .append(" - ")
                    .append(value(regionOf(deal)))
                    .append(", 거래금액 ")
                    .append(value(deal.getDealAmount()))
                    .append(", 거래일 ")
                    .append(value(deal.getDealDate()))
                    .append("\n");
        }
        builder.append("\n연결이 복구되면 더 자세한 추천 답변도 제공할 수 있어요.");
        return builder.toString();
    }

    private String localContextAnswer(String question, ChatContext context) {
        StringBuilder builder = new StringBuilder();
        builder.append("현재 조회 가능한 HappyHome 데이터 기준으로 답변드릴게요.\n");
        builder.append("질문: ").append(question).append("\n\n");

        if (context.analysis() != null) {
            HousingAnalysis analysis = context.analysis();
            builder.append("생활권 분석\n")
                    .append("- 종합 점수: ").append(analysis.score().total()).append("점 (").append(value(analysis.score().level())).append(")\n")
                    .append("- 상권: ").append(analysis.commercialSummary().totalCount()).append("곳, 상권 점수 ")
                    .append(analysis.score().commercialScore()).append("점\n")
                    .append("- 대중교통: 버스정류장 500m 이내 ")
                    .append(analysis.transitSummary().busStopWithin500m()).append("개, 지하철 1km 이내 ")
                    .append(analysis.transitSummary().subwayWithin1000m()).append("개, 교통 점수 ")
                    .append(analysis.score().transitScore()).append("점\n")
                    .append("- 교통 이벤트: ").append(analysis.trafficRiskSummary().eventCount()).append("건\n\n");
        }

        if (!context.deals().isEmpty()) {
            builder.append("최근 실거래\n");
            appendDeals(builder, context.deals(), 5);
            builder.append("\n");
        }

        if (!context.notices().isEmpty()) {
            builder.append("임대 공고\n");
            int count = Math.min(context.notices().size(), 5);
            for (int index = 0; index < count; index++) {
                RentalNotice notice = context.notices().get(index);
                builder.append(index + 1)
                        .append(". ")
                        .append(value(notice.title()))
                        .append(" - ")
                        .append(value(notice.regionName()))
                        .append(", ")
                        .append(value(notice.status()))
                        .append(", 마감 ")
                        .append(value(notice.closeDate()))
                        .append("\n");
            }
            builder.append("\n");
        }

        if (context.documentCount() == 0) {
            builder.append("아직 이 질문에 바로 붙일 DB/좌표 데이터가 부족합니다. 지역명, 단지명, 또는 지도 좌표가 전달되면 더 구체적으로 답할 수 있어요.");
        }
        return builder.toString();
    }

    private void appendDeals(StringBuilder builder, List<HouseDeal> deals, int limit) {
        int count = Math.min(deals.size(), limit);
        for (int index = 0; index < count; index++) {
            HouseDeal deal = deals.get(index);
            builder.append(index + 1)
                    .append(". ")
                    .append(value(deal.getAptName()))
                    .append(" - ")
                    .append(value(regionOf(deal)))
                    .append(", 거래금액 ")
                    .append(value(deal.getDealAmount()))
                    .append(", 거래일 ")
                    .append(value(deal.getDealDate()))
                    .append("\n");
        }
    }

    private String regionOf(HouseDeal deal) {
        List<String> parts = new ArrayList<>();
        addIfText(parts, deal.getSidoName());
        addIfText(parts, deal.getGugunName());
        addIfText(parts, deal.getDongName() != null ? deal.getDongName() : deal.getUmdName());
        return String.join(" ", parts);
    }

    private void addIfText(List<String> values, String value) {
        if (StringUtils.hasText(value)) {
            values.add(value.trim());
        }
    }

    private String value(Object value) {
        if (value == null) {
            return "-";
        }
        String text = value.toString();
        return text.isBlank() ? "-" : text;
    }

    private synchronized void ensureIndexed() {
        if (indexed) {
            return;
        }

        List<HouseDeal> recentDeals = houseDealService.findRecent(bootstrapLimit);
        List<Document> documents = documentFactory.fromHouseDeals(recentDeals);
        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
        indexedDocumentCount = documents.size();
        indexed = true;
    }

    private record ChatContext(List<HouseDeal> deals, List<RentalNotice> notices, HousingAnalysis analysis) {

        private int documentCount() {
            return deals.size() + notices.size() + (analysis == null ? 0 : 1);
        }

        private ChatContext withDeals(List<HouseDeal> replacementDeals) {
            return new ChatContext(replacementDeals == null ? List.of() : replacementDeals, notices, analysis);
        }
    }
}
