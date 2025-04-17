package com.flowiseai.getscheme.sqlserver.service;

import com.flowiseai.getscheme.model.DatabaseConnectionInfo;
import com.flowiseai.getscheme.model.TableSchema;
import com.flowiseai.getscheme.model.TableRelationship;
import com.flowiseai.getscheme.service.DatabaseSchemaService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

@Service
public class SqlServerSchemaService implements DatabaseSchemaService {

    @Override
    public List<TableSchema> getTableSchemas(DatabaseConnectionInfo connectionInfo, List<String> selectedTables) {
        List<TableSchema> schemas = new ArrayList<>();
        try {
            DataSource dataSource = createDataSource(connectionInfo);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            for (String tableName : selectedTables) {
                TableSchema schema = new TableSchema();
                schema.setTableName(tableName);
                schema.setCreateTableSql(generateCreateTableSql(dataSource, tableName));
                schema.setSampleData(getSampleDataForTable(jdbcTemplate, tableName));
                schemas.add(schema);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy cấu trúc bảng: " + e.getMessage(), e);
        }
        return schemas;
    }

    @Override
    public List<TableRelationship> getTableRelationships(DatabaseConnectionInfo connectionInfo, List<String> selectedTables) {
        List<TableRelationship> relationships = new ArrayList<>();
        try {
            DataSource dataSource = createDataSource(connectionInfo);
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();

            for (String tableName : selectedTables) {
                try (ResultSet rs = metaData.getImportedKeys(null, null, tableName)) {
                    while (rs.next()) {
                        TableRelationship relationship = new TableRelationship();
                        relationship.setSourceTable(tableName);
                        relationship.setSourceColumn(rs.getString("FKCOLUMN_NAME"));
                        relationship.setTargetTable(rs.getString("PKTABLE_NAME"));
                        relationship.setTargetColumn(rs.getString("PKCOLUMN_NAME"));
                        relationship.setConstraintName(rs.getString("FK_NAME"));
                        relationship.setRelationshipType(determineRelationshipType(metaData, relationship));
                        relationships.add(relationship);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy mối quan hệ bảng: " + e.getMessage(), e);
        }
        return relationships;
    }

    @Override
    public Map<String, List<Map<String, Object>>> getSampleData(DatabaseConnectionInfo connectionInfo, List<String> selectedTables) {
        Map<String, List<Map<String, Object>>> sampleData = new HashMap<>();
        try {
            DataSource dataSource = createDataSource(connectionInfo);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            for (String tableName : selectedTables) {
                List<Map<String, Object>> data = jdbcTemplate.queryForList(
                    "SELECT TOP 3 * FROM " + tableName);
                sampleData.put(tableName, data);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy mẫu dữ liệu: " + e.getMessage(), e);
        }
        return sampleData;
    }

    private DataSource createDataSource(DatabaseConnectionInfo connectionInfo) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(connectionInfo.getDriverClassName());
        dataSource.setUrl(connectionInfo.getUrl());
        dataSource.setUsername(connectionInfo.getEffectiveUsername());
        dataSource.setPassword(connectionInfo.getEffectivePassword());
        return dataSource;
    }

    private String generateCreateTableSql(DataSource dataSource, String tableName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (\n");

        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                boolean isNullable = rs.getInt("NULLABLE") == 1;
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
                
                sql.append(",\n");
            }
        }

        // Thêm khóa chính
        try (ResultSet rs = metaData.getPrimaryKeys(null, null, tableName)) {
            List<String> primaryKeys = new ArrayList<>();
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
            if (!primaryKeys.isEmpty()) {
                sql.append("  PRIMARY KEY (")
                   .append(String.join(", ", primaryKeys))
                   .append("),\n");
            }
        }

        sql.deleteCharAt(sql.length() - 2); // Xóa dấu phẩy cuối cùng
        sql.append(");");
        
        return sql.toString();
    }

    private List<Map<String, Object>> getSampleDataForTable(JdbcTemplate jdbcTemplate, String tableName) {
        return jdbcTemplate.queryForList("SELECT TOP 3 * FROM " + tableName);
    }

    private String determineRelationshipType(DatabaseMetaData metaData, TableRelationship relationship) throws Exception {
        try (ResultSet rs = metaData.getImportedKeys(null, null, relationship.getTargetTable())) {
            boolean isOneToOne = false;
            while (rs.next()) {
                if (rs.getString("FKCOLUMN_NAME").equals(relationship.getSourceColumn())) {
                    isOneToOne = true;
                    break;
                }
            }
            return isOneToOne ? "ONE_TO_ONE" : "ONE_TO_MANY";
        }
    }
} 