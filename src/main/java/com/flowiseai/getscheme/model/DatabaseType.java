package com.flowiseai.getscheme.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DatabaseType {
    MYSQL("MySQL", "mysql"),
    POSTGRESQL("PostgreSQL", "postgresql"),
    SQLSERVER("SQL Server", "sqlserver"),
    ORACLE("Oracle", "oracle");

    private static final Logger logger = LoggerFactory.getLogger(DatabaseType.class);
    private final String displayName;
    private final String code;

    DatabaseType(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public static DatabaseType fromString(String value) {
        logger.debug("Converting string to database type: {}", value);
        if (value == null) {
            logger.error("Database type cannot be null");
            throw new IllegalArgumentException("Database type cannot be null");
        }
        
        for (DatabaseType type : DatabaseType.values()) {
            if (type.code.equalsIgnoreCase(value) || type.displayName.equalsIgnoreCase(value)) {
                logger.debug("Found matching database type: {}", type);
                return type;
            }
        }
        
        logger.error("Unsupported database type: {}", value);
        throw new IllegalArgumentException("Unsupported database type: " + value);
    }
} 