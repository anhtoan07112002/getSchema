package com.flowiseai.getscheme.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class ChatbotConfig {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotConfig.class);
    private final String chatbotUrl;

    public ChatbotConfig() {
        this.chatbotUrl = "http://localhost:3000/chatbot/default";
        logger.info("Initializing ChatbotConfig with default URL: {}", this.chatbotUrl);
    }

    public String getChatbotUrl() {
        return chatbotUrl;
    }
} 