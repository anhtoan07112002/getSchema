package com.flowiseai.getscheme.mysql;

import com.flowiseai.getscheme.model.DatabaseConnectionInfo;
import com.flowiseai.getscheme.model.TableSchema;
import com.flowiseai.getscheme.model.TableRelationship;
import com.flowiseai.getscheme.service.DatabaseSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MySQLSchemaService implements DatabaseSchemaService {
    private static final Logger logger = LoggerFactory.getLogger(MySQLSchemaService.class);

    @Override
    public List<TableSchema> getTableSchemas(DatabaseConnectionInfo connectionInfo, List<String> selectedTables) {
        logger.info("Retrieving table schemas for {} tables", selectedTables.size());
        List<TableSchema> schemas = new ArrayList<>();
        String url = String.format("jdbc:mysql://%s:%s/%s", 
            connectionInfo.getHost(), 
            connectionInfo.getPort(), 
            connectionInfo.getDatabaseName());

        try (Connection conn = DriverManager.getConnection(url, connectionInfo.getUsername(), connectionInfo.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            for (String tableName : selectedTables) {
                logger.debug("Processing table: {}", tableName);
                TableSchema schema = new TableSchema();
                schema.setTableName(tableName);
                schema.setCreateTableSql(getCreateTableSql(connectionInfo, tableName));
                
                // Lấy thông tin cột
                try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String dataType = columns.getString("TYPE_NAME");
                        int columnSize = columns.getInt("COLUMN_SIZE");
                        boolean isNullable = columns.getString("IS_NULLABLE").equals("YES");
                        String defaultValue = columns.getString("COLUMN_DEF");
                        
                        schema.addColumn(columnName, dataType, columnSize, isNullable, defaultValue);
                    }
                }
                
                // Lấy thông tin khóa chính
                try (ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName)) {
                    while (primaryKeys.next()) {
                        String columnName = primaryKeys.getString("COLUMN_NAME");
                        schema.addPrimaryKey(columnName);
                    }
                }
                
                schemas.add(schema);
                logger.debug("Successfully processed table: {}", tableName);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving table schemas: {}", e.getMessage());
        }
        
        return schemas;
    }

    @Override
    public List<TableRelationship> getTableRelationships(DatabaseConnectionInfo connectionInfo, List<String> selectedTables) {
        logger.info("Retrieving table relationships for {} tables", selectedTables.size());
        List<TableRelationship> relationships = new ArrayList<>();
        String url = String.format("jdbc:mysql://%s:%s/%s", 
            connectionInfo.getHost(), 
            connectionInfo.getPort(), 
            connectionInfo.getDatabaseName());

        try (Connection conn = DriverManager.getConnection(url, connectionInfo.getUsername(), connectionInfo.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Tạo set các bảng được chọn để kiểm tra nhanh
            Set<String> selectedTableSet = new HashSet<>(selectedTables);
            
            for (String tableName : selectedTables) {
                logger.debug("Processing relationships for table: {}", tableName);
                try (ResultSet foreignKeys = metaData.getImportedKeys(null, null, tableName)) {
                    while (foreignKeys.next()) {
                        String targetTable = foreignKeys.getString("PKTABLE_NAME");
                        
                        // Chỉ thêm mối quan hệ nếu bảng đích cũng được chọn
                        if (selectedTableSet.contains(targetTable)) {
                            TableRelationship relationship = new TableRelationship();
                            relationship.setSourceTable(tableName);
                            relationship.setSourceColumn(foreignKeys.getString("FKCOLUMN_NAME"));
                            relationship.setTargetTable(targetTable);
                            relationship.setTargetColumn(foreignKeys.getString("PKCOLUMN_NAME"));
                            relationship.setConstraintName(foreignKeys.getString("FK_NAME"));
                            
                            // Xác định loại quan hệ
                            if (isOneToOne(metaData, relationship, connectionInfo)) {
                                relationship.setRelationshipType(TableRelationship.RelationshipType.ONE_TO_ONE);
                            } else {
                                relationship.setRelationshipType(TableRelationship.RelationshipType.ONE_TO_MANY);
                            }
                            
                            relationships.add(relationship);
                            logger.debug("Found relationship: {} -> {}", tableName, targetTable);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving table relationships: {}", e.getMessage());
        }
        
        return relationships;
    }

    @Override
    public Map<String, List<Map<String, Object>>> getSampleData(DatabaseConnectionInfo connectionInfo, List<String> selectedTables) {
        logger.info("Retrieving sample data for {} tables", selectedTables.size());
        Map<String, List<Map<String, Object>>> sampleData = new HashMap<>();
        String url = String.format("jdbc:mysql://%s:%s/%s", 
            connectionInfo.getHost(), 
            connectionInfo.getPort(), 
            connectionInfo.getDatabaseName());

        try (Connection conn = DriverManager.getConnection(url, connectionInfo.getUsername(), connectionInfo.getPassword())) {
            for (String tableName : selectedTables) {
                logger.debug("Retrieving sample data for table: {}", tableName);
                String query = String.format("SELECT * FROM %s LIMIT 3", tableName);
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    
                    List<Map<String, Object>> rows = new ArrayList<>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            Object value = rs.getObject(i);
                            row.put(columnName, value);
                        }
                        rows.add(row);
                    }
                    sampleData.put(tableName, rows);
                    logger.debug("Retrieved {} rows for table: {}", rows.size(), tableName);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving sample data: {}", e.getMessage());
        }
        
        return sampleData;
    }

    @Override
    public String getCreateTableSql(DatabaseConnectionInfo connectionInfo, String tableName) {
        logger.debug("Retrieving create table SQL for table: {}", tableName);
        String url = String.format("jdbc:mysql://%s:%s/%s", 
            connectionInfo.getHost(), 
            connectionInfo.getPort(), 
            connectionInfo.getDatabaseName());

        try (Connection conn = DriverManager.getConnection(url, connectionInfo.getUsername(), connectionInfo.getPassword())) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(String.format("SHOW CREATE TABLE %s", tableName))) {
                
                if (rs.next()) {
                    String createTableSql = rs.getString("Create Table");
                    logger.debug("Successfully retrieved create table SQL for table: {}", tableName);
                    return createTableSql;
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving create table SQL: {}", e.getMessage());
        }
        return null;
    }

    private boolean isOneToOne(DatabaseMetaData metaData, TableRelationship relationship, DatabaseConnectionInfo connectionInfo) throws SQLException {
        logger.debug("Checking if relationship is one-to-one: {} -> {}", 
            relationship.getSourceTable(), relationship.getTargetTable());
        // Kiểm tra xem có phải là quan hệ 1-1 không
        try (ResultSet foreignKeys = metaData.getImportedKeys(null, null, relationship.getTargetTable())) {
            while (foreignKeys.next()) {
                if (foreignKeys.getString("PKTABLE_NAME").equals(relationship.getSourceTable())) {
                    logger.debug("Relationship is one-to-one");
                    return true;
                }
            }
        }
        logger.debug("Relationship is one-to-many");
        return false;
    }
} 