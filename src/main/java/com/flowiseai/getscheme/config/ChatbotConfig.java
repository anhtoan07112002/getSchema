package com.flowiseai.getscheme.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatbotConfig {


    @Value("${chatbot.url:http://localhost:3000/chatbot/default}")
    private String chatbotUrl;

    public String getChatbotUrl() {
        return chatbotUrl;
    }
} 