package com.flowiseai.getscheme.service;

import com.flowiseai.getscheme.model.ConnectionResult;
import com.flowiseai.getscheme.model.DatabaseConnectionInfo;
import com.flowiseai.getscheme.model.DatabaseType;
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
public class DatabaseConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionService.class);

    public ConnectionResult connectToDatabase(DatabaseConnectionInfo connectionInfo) {
        logger.info("Attempting to connect to database with info: {}", connectionInfo);
        
        try {
            // Tạo datasource từ thông tin kết nối
            DataSource dataSource = createDataSource(connectionInfo);
            logger.debug("DataSource created successfully");

            // Kiểm tra kết nối
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            logger.debug("Testing connection with SELECT 1 query");
            jdbcTemplate.execute("SELECT 1");
            logger.info("Connection test successful");

            // Lấy danh sách bảng
            List<ConnectionResult.TableInfo> tableInfos = getTableNames(dataSource, connectionInfo.getDatabaseTypeEnum());
            logger.debug("Retrieved {} tables", tableInfos.size());
            
            logger.debug("Retrieving database info");
            List<Map<String, Object>> databaseInfo = getDatabaseInfo(dataSource);
            logger.info("Database info retrieved successfully");

            return new ConnectionResult(
                    true,
                    "Successfully connected to database " + connectionInfo.getDatabaseName(),
                    tableInfos,
                    databaseInfo
            );
        } catch (Exception e) {
            logger.error("Error connecting to database: {}", e.getMessage(), e);
            String errorMessage = "Cannot connect to database: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " - Cause: " + e.getCause().getMessage();
            }
            return new ConnectionResult(
                    false,
                    errorMessage,
                    null,
                    null
            );
        }
    }

    private DataSource createDataSource(DatabaseConnectionInfo connectionInfo) {
        logger.debug("Creating DataSource with URL: {}", connectionInfo.getUrl());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(connectionInfo.getDriverClassName());
        dataSource.setUrl(connectionInfo.getUrl());
        dataSource.setUsername(connectionInfo.getEffectiveUsername());
        dataSource.setPassword(connectionInfo.getEffectivePassword());
        return dataSource;
    }

    private List<ConnectionResult.TableInfo> getTableNames(DataSource dataSource, DatabaseType dbType) throws SQLException {
        List<ConnectionResult.TableInfo> tableInfos = new ArrayList<>();
        try (ResultSet rs = dataSource.getConnection().getMetaData().getTables(
                null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                ConnectionResult.TableInfo tableInfo = new ConnectionResult.TableInfo();
                String schema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                
                // Xử lý tên bảng theo loại database
                switch (dbType) {
                    case SQLSERVER:
                        // SQL Server: lưu dạng schema.tableName
                        tableInfo.setTableName(schema != null ? schema + "." + tableName : "dbo." + tableName);
                        break;
                    case POSTGRESQL:
                        // PostgreSQL: chỉ lưu tên bảng, schema được xử lý trong service
                        tableInfo.setTableName(tableName);
                        break;
                    case MYSQL:
                    // case ORACLE:
                    default:
                        // MySQL và Oracle: chỉ lưu tên bảng
                        tableInfo.setTableName(tableName);
                        break;
                }
                
                tableInfo.setSelected(false);
                tableInfos.add(tableInfo);
            }
        }
        return tableInfos;
    }

    private List<Map<String, Object>> getDatabaseInfo(DataSource dataSource) throws SQLException {
        logger.debug("Retrieving database metadata");
        List<Map<String, Object>> info = new ArrayList<>();
        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();

        Map<String, Object> dbInfo = new HashMap<>();
        dbInfo.put("Database Product Name", metaData.getDatabaseProductName());
        dbInfo.put("Database Product Version", metaData.getDatabaseProductVersion());
        dbInfo.put("Driver Name", metaData.getDriverName());
        dbInfo.put("Driver Version", metaData.getDriverVersion());
        dbInfo.put("JDBC Major Version", metaData.getJDBCMajorVersion());
        dbInfo.put("JDBC Minor Version", metaData.getJDBCMinorVersion());

        info.add(dbInfo);
        logger.debug("Database info: {}", dbInfo);
        return info;
    }
}