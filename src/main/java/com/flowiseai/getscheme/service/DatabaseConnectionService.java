package com.flowiseai.getscheme.service;

import com.flowiseai.getscheme.model.ConnectionResult;
import com.flowiseai.getscheme.model.DatabaseConnectionInfo;
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
            List<ConnectionResult.TableInfo> tableInfos = getTableNames(dataSource);
            logger.debug("Retrieved {} tables", tableInfos.size());
            
            logger.debug("Retrieving database info");
            List<Map<String, Object>> databaseInfo = getDatabaseInfo(dataSource);
            logger.info("Database info retrieved successfully");

            return new ConnectionResult(
                    true,
                    "Kết nối thành công đến database " + connectionInfo.getDatabaseName(),
                    tableInfos,
                    databaseInfo
            );
        } catch (Exception e) {
            logger.error("Error connecting to database: {}", e.getMessage(), e);
            String errorMessage = "Không thể kết nối đến database: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " - Nguyên nhân: " + e.getCause().getMessage();
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

    private List<ConnectionResult.TableInfo> getTableNames(DataSource dataSource) throws SQLException {
        logger.debug("Retrieving table names from database");
        List<ConnectionResult.TableInfo> tableInfos = new ArrayList<>();
        try (ResultSet rs = dataSource.getConnection().getMetaData().getTables(
                null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                ConnectionResult.TableInfo tableInfo = new ConnectionResult.TableInfo();
                tableInfo.setTableName(rs.getString("TABLE_NAME"));
                tableInfo.setSelected(false); // Mặc định không chọn
                tableInfos.add(tableInfo);
                logger.trace("Found table: {}", tableInfo.getTableName());
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