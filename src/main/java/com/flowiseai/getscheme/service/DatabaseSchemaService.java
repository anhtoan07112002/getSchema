package com.flowiseai.getscheme.service;

import com.flowiseai.getscheme.model.DatabaseConnectionInfo;
import com.flowiseai.getscheme.model.TableSchema;
import com.flowiseai.getscheme.model.TableRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public interface DatabaseSchemaService {
    Logger logger = LoggerFactory.getLogger(DatabaseSchemaService.class);

    List<TableSchema> getTableSchemas(DatabaseConnectionInfo connectionInfo, List<String> selectedTables);
    List<TableRelationship> getTableRelationships(DatabaseConnectionInfo connectionInfo, List<String> selectedTables);
    Map<String, List<Map<String, Object>>> getSampleData(DatabaseConnectionInfo connectionInfo, List<String> selectedTables);
    String getCreateTableSql(DatabaseConnectionInfo connectionInfo, String tableName);

    default void logSchemaRetrieval(String tableName) {
        logger.debug("Retrieving schema for table: {}", tableName);
    }

    default void logRelationshipRetrieval(String tableName) {
        logger.debug("Retrieving relationships for table: {}", tableName);
    }

    default void logSampleDataRetrieval(String tableName) {
        logger.debug("Retrieving sample data for table: {}", tableName);
    }

    default void logError(String operation, String tableName, Exception e) {
        logger.error("Error during {} for table {}: {}", operation, tableName, e.getMessage());
    }
} 