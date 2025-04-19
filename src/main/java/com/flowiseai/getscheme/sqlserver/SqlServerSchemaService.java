package com.flowiseai.getscheme.sqlserver;

import com.flowiseai.getscheme.model.DatabaseConnectionInfo;
import com.flowiseai.getscheme.model.TableSchema;
import com.flowiseai.getscheme.model.TableRelationship;
import com.flowiseai.getscheme.service.DatabaseSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class SqlServerSchemaService implements DatabaseSchemaService {
    private static final Logger logger = LoggerFactory.getLogger(SqlServerSchemaService.class);

    @Override
    public List<TableSchema> getTableSchemas(DatabaseConnectionInfo connectionInfo, List<String> selectedTables) {
        logger.info("Retrieving table schemas for {} tables", selectedTables.size());
        List<TableSchema> schemas = new ArrayList<>();
        try {
            DataSource dataSource = createDataSource(connectionInfo);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String defaultSchema = connectionInfo.getEffectiveSchema();

            for (String tableName : selectedTables) {
                logger.debug("Processing table: {}", tableName);
                // Tách schema và tên bảng nếu có
                String[] parts = tableName.split("\\.");
                String tableSchema = parts.length > 1 ? parts[0] : defaultSchema;
                String actualTableName = parts.length > 1 ? parts[1] : parts[0];
                
                TableSchema tableSchemaObj = new TableSchema();
                tableSchemaObj.setTableName(tableName);
                tableSchemaObj.setCreateTableSql(getCreateTableSql(connectionInfo, actualTableName, tableSchema));
                tableSchemaObj.setSampleData(getSampleDataForTable(jdbcTemplate, actualTableName, tableSchema));
                schemas.add(tableSchemaObj);
                logger.debug("Successfully processed table: {}", tableName);
            }
        } catch (Exception e) {
            logger.error("Error retrieving table schemas: {}", e.getMessage());
            throw new RuntimeException("Error retrieving table schemas: " + e.getMessage(), e);
        }
        return schemas;
    }

    @Override
    public List<TableRelationship> getTableRelationships(DatabaseConnectionInfo connectionInfo, List<String> selectedTables) {
        logger.info("Retrieving table relationships for {} tables", selectedTables.size());
        List<TableRelationship> relationships = new ArrayList<>();
        try {
            DataSource dataSource = createDataSource(connectionInfo);
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            String defaultSchema = connectionInfo.getEffectiveSchema();
            
            // Tạo set các bảng được chọn để kiểm tra nhanh
            Set<String> selectedTableSet = new HashSet<>(selectedTables);

            for (String tableName : selectedTables) {
                logger.debug("Processing relationships for table: {}", tableName);
                // Tách schema và tên bảng nếu có
                String[] parts = tableName.split("\\.");
                String tableSchema = parts.length > 1 ? parts[0] : defaultSchema;
                String actualTableName = parts.length > 1 ? parts[1] : parts[0];
                
                try (ResultSet rs = metaData.getImportedKeys(null, tableSchema, actualTableName)) {
                    while (rs.next()) {
                        String targetTable = rs.getString("PKTABLE_NAME");
                        String targetSchema = rs.getString("PKTABLE_SCHEM");
                        String fullTargetTable = targetSchema + "." + targetTable;
                        
                        // Chỉ thêm mối quan hệ nếu bảng đích cũng được chọn
                        if (selectedTableSet.contains(fullTargetTable)) {
                            TableRelationship relationship = new TableRelationship();
                            relationship.setSourceTable(tableName);
                            relationship.setSourceColumn(rs.getString("FKCOLUMN_NAME"));
                            relationship.setTargetTable(fullTargetTable);
                            relationship.setTargetColumn(rs.getString("PKCOLUMN_NAME"));
                            relationship.setConstraintName(rs.getString("FK_NAME"));
                            
                            // Xác định loại quan hệ
                            if (isOneToOne(metaData, relationship)) {
                                relationship.setRelationshipType(TableRelationship.RelationshipType.ONE_TO_ONE);
                            } else {
                                relationship.setRelationshipType(TableRelationship.RelationshipType.ONE_TO_MANY);
                            }
                            
                            relationships.add(relationship);
                            logger.debug("Found relationship: {} -> {}", tableName, fullTargetTable);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error retrieving table relationships: {}", e.getMessage());
            throw new RuntimeException("Error retrieving table relationships: " + e.getMessage(), e);
        }
        return relationships;
    }

    @Override
    public Map<String, List<Map<String, Object>>> getSampleData(DatabaseConnectionInfo connectionInfo, List<String> selectedTables) {
        logger.info("Retrieving sample data for {} tables", selectedTables.size());
        Map<String, List<Map<String, Object>>> sampleData = new HashMap<>();
        try {
            DataSource dataSource = createDataSource(connectionInfo);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String defaultSchema = connectionInfo.getEffectiveSchema();

            for (String tableName : selectedTables) {
                logger.debug("Retrieving sample data for table: {}", tableName);
                // Tách schema và tên bảng nếu có
                String[] parts = tableName.split("\\.");
                String tableSchema = parts.length > 1 ? parts[0] : defaultSchema;
                String actualTableName = parts.length > 1 ? parts[1] : parts[0];
                
                List<Map<String, Object>> data = jdbcTemplate.queryForList(
                    "SELECT TOP 3 * FROM [" + tableSchema + "].[" + actualTableName + "]");
                sampleData.put(tableName, data);
                logger.debug("Retrieved {} rows for table: {}", data.size(), tableName);
            }
        } catch (Exception e) {
            logger.error("Error retrieving sample data: {}", e.getMessage());
            throw new RuntimeException("Error retrieving sample data: " + e.getMessage(), e);
        }
        return sampleData;
    }

    @Override
    public String getCreateTableSql(DatabaseConnectionInfo connectionInfo, String tableName) {
        logger.debug("Retrieving create table SQL for table: {}", tableName);
        // Tách schema và tên bảng nếu có
        String[] parts = tableName.split("\\.");
        String tableSchema = parts.length > 1 ? parts[0] : connectionInfo.getEffectiveSchema();
        String actualTableName = parts.length > 1 ? parts[1] : parts[0];
        
        return getCreateTableSql(connectionInfo, actualTableName, tableSchema);
    }

    private String getCreateTableSql(DatabaseConnectionInfo connectionInfo, String tableName, String schema) {
        logger.debug("Retrieving create table SQL for table {}.{}", schema, tableName);
        try {
            DataSource dataSource = createDataSource(connectionInfo);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // Kiểm tra sự tồn tại của bảng
            String checkTableQuery = String.format(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'",
                schema,
                tableName
            );
            
            int count = jdbcTemplate.queryForObject(checkTableQuery, Integer.class);
            if (count == 0) {
                logger.error("Table {}.{} does not exist", schema, tableName);
                throw new SQLException("Table " + tableName + " does not exist in schema " + schema);
            }

            // Thử lấy từ sp_helptext
            String fullTableName = String.format("[%s].[%s]", schema, tableName);
            String helptextQuery = String.format("EXEC sp_helptext '%s'", fullTableName);
            
            try {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(helptextQuery);
                if (!results.isEmpty()) {
                    StringBuilder createTableSql = new StringBuilder();
                    for (Map<String, Object> row : results) {
                        createTableSql.append(row.get("Text"));
                    }
                    String sql = createTableSql.toString();
                    if (sql != null && !sql.isEmpty()) {
                        logger.debug("Successfully retrieved create table SQL from sp_helptext for table {}.{}", schema, tableName);
                        return sql;
                    }
                }
            } catch (Exception e) {
                logger.debug("Failed to get create table SQL from sp_helptext: {}", e.getMessage());
            }

            // Nếu không lấy được từ sp_helptext, thử lấy từ sys.sql_modules
            String moduleQuery = String.format(
                "SELECT m.definition FROM sys.sql_modules m " +
                "INNER JOIN sys.objects o ON m.object_id = o.object_id " +
                "INNER JOIN sys.schemas s ON o.schema_id = s.schema_id " +
                "WHERE o.type = 'U' AND s.name = '%s' AND o.name = '%s'",
                schema,
                tableName
            );
            
            try {
                String createTableSql = jdbcTemplate.queryForObject(moduleQuery, String.class);
                if (createTableSql != null && !createTableSql.isEmpty()) {
                    logger.debug("Successfully retrieved create table SQL from sys.sql_modules for table {}.{}", schema, tableName);
                    return createTableSql;
                }
            } catch (Exception e) {
                logger.debug("Failed to get create table SQL from sys.sql_modules: {}", e.getMessage());
            }

            // Nếu không lấy được từ cả hai cách trên, tạo từ metadata
            logger.debug("Generating create table SQL from metadata for table {}.{}", schema, tableName);
            return generateCreateTableSql(dataSource, fullTableName);
        } catch (Exception e) {
            logger.error("Error retrieving create table SQL: {}", e.getMessage());
            throw new RuntimeException("Error retrieving create table SQL: " + e.getMessage(), e);
        }
    }

    private DataSource createDataSource(DatabaseConnectionInfo connectionInfo) {
        logger.debug("Creating data source for database: {}", connectionInfo.getDatabaseName());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(connectionInfo.getDriverClassName());
        dataSource.setUrl(connectionInfo.getUrl());
        dataSource.setUsername(connectionInfo.getEffectiveUsername());
        dataSource.setPassword(connectionInfo.getEffectivePassword());
        return dataSource;
    }

    private String generateCreateTableSql(DataSource dataSource, String fullTableName) throws Exception {
        logger.debug("Generating create table SQL from metadata for table: {}", fullTableName);
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(fullTableName).append(" (\n");

        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        String[] parts = fullTableName.split("\\.");
        String schema = parts[0].replace("[", "").replace("]", "");
        String tableName = parts[1].replace("[", "").replace("]", "");

        // Lấy thông tin cột
        try (ResultSet rs = metaData.getColumns(null, schema, tableName, null)) {
            boolean firstColumn = true;
            while (rs.next()) {
                if (!firstColumn) {
                    sql.append(",\n");
                }
                firstColumn = false;

                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                boolean isNullable = rs.getString("IS_NULLABLE").equals("YES");
                String defaultValue = rs.getString("COLUMN_DEF");

                sql.append("  ").append(columnName).append(" ").append(dataType);
                
                if (columnSize > 0) {
                    sql.append("(").append(columnSize).append(")");
                }
                
                if (!isNullable) {
                    sql.append(" NOT NULL");
                }
                
                if (defaultValue != null) {
                    sql.append(" DEFAULT ").append(defaultValue);
                }
            }
        }

        sql.append("\n);");
        
        logger.debug("Successfully generated create table SQL from metadata for table: {}", fullTableName);
        return sql.toString();
    }

    private List<Map<String, Object>> getSampleDataForTable(JdbcTemplate jdbcTemplate, String tableName, String schema) {
        logger.debug("Retrieving sample data for table {}.{}", schema, tableName);
        return jdbcTemplate.queryForList("SELECT TOP 3 * FROM [" + schema + "].[" + tableName + "]");
    }

    private boolean isOneToOne(DatabaseMetaData metaData, TableRelationship relationship) throws Exception {
        logger.debug("Checking if relationship is one-to-one: {} -> {}", 
            relationship.getSourceTable(), relationship.getTargetTable());
        try (ResultSet rs = metaData.getImportedKeys(null, null, relationship.getTargetTable())) {
            while (rs.next()) {
                if (rs.getString("PKTABLE_NAME").equals(relationship.getSourceTable())) {
                    logger.debug("Relationship is one-to-one");
                    return true;
                }
            }
        }
        logger.debug("Relationship is one-to-many");
        return false;
    }
} 