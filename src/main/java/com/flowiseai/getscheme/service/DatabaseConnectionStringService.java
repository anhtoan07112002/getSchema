package com.flowiseai.getscheme.service;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class DatabaseConnectionStringService {
    private Map<String, String> connectionInfo;

    public void setConnectionString(String databaseType, String host, String port, 
                                  String databaseName, String username, String password, 
                                  String schema) {
        connectionInfo = new HashMap<>();
        connectionInfo.put("databaseType", databaseType);
        connectionInfo.put("host", host);
        connectionInfo.put("port", port);
        connectionInfo.put("databaseName", databaseName);
        connectionInfo.put("username", username);
        connectionInfo.put("password", password);
        connectionInfo.put("schema", schema);
    }

    public Map<String, String> getConnectionInfo() {
        return connectionInfo;
    }

    public void clearConnectionString() {
        this.connectionInfo = null;
    }
} 