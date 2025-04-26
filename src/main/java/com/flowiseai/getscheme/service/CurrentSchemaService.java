package com.flowiseai.getscheme.service;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class CurrentSchemaService {
    private Map<String, String> currentSchema;

    public void setCurrentSchema(String schema, String markdown) {
        currentSchema = new HashMap<>();
        currentSchema.put("schema", schema);
        currentSchema.put("markdown", markdown);
    }

    public Map<String, String> getCurrentSchema() {
        return currentSchema;
    }

    public void clearCurrentSchema() {
        this.currentSchema = null;
    }
} 