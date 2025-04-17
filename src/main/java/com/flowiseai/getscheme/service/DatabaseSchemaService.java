package com.flowiseai.getscheme.service;

import com.flowiseai.getscheme.model.DatabaseConnectionInfo;
import com.flowiseai.getscheme.model.TableSchema;
import com.flowiseai.getscheme.model.TableRelationship;

import java.util.List;
import java.util.Map;

public interface DatabaseSchemaService {
    List<TableSchema> getTableSchemas(DatabaseConnectionInfo connectionInfo, List<String> selectedTables);
    List<TableRelationship> getTableRelationships(DatabaseConnectionInfo connectionInfo, List<String> selectedTables);
    Map<String, List<Map<String, Object>>> getSampleData(DatabaseConnectionInfo connectionInfo, List<String> selectedTables);
} 