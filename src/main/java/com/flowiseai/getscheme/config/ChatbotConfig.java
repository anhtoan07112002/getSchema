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
    private final boolean chatbotEnabled;

    @Autowired
    public ChatbotConfig(@Value("${chatbot.url:http://localhost:3000/chatbot/default}") String chatbotUrl,
                        @Value("${chatbot.enabled:false}") boolean chatbotEnabled) {
        this.chatbotUrl = chatbotUrl;
        this.chatbotEnabled = chatbotEnabled;
        logger.info("Initializing ChatbotConfig with URL: {}, enabled: {}", this.chatbotUrl, this.chatbotEnabled);
    }

    public String getChatbotUrl() {
        return chatbotUrl;
    }

    public boolean isEnabled() {
        return chatbotEnabled;
    }
} 