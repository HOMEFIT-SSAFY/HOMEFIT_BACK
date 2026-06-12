package com.happyhome.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiRagConfig {

    @Bean
    VectorStore happyHomeVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    ChatClient happyHomeChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are HappyHome's real-estate assistant.
                        Answer in Korean.
                        Use the retrieved HappyHome context first.
                        If the context is not enough, say what information is missing instead of inventing facts.
                        Keep answers concise and practical for apartment or housing decisions.
                        """)
                .build();
    }
}
