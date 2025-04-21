package com.flowiseai.getscheme.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatbotConfig {
    
    @Value("${chatbot.url}")
    private String chatbotUrl;

    public String getChatbotUrl() {
        return chatbotUrl;
    }
} 