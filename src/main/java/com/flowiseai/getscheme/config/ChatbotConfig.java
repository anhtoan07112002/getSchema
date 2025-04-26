package com.flowiseai.getscheme.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class ChatbotConfig {
    
    private final String chatbotUrl;

    @Autowired
    public ChatbotConfig(Environment env) {
        this.chatbotUrl = env.getProperty("chatbot.url", "http://localhost:3000/chatbot/default");
    }

    public String getChatbotUrl() {
        return chatbotUrl;
    }
} 