package com.flowiseai.getscheme.controller;

import com.flowiseai.getscheme.service.DatabaseConnectionStringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/query")
public class SqlQueryController {

    @Autowired
    private DatabaseConnectionStringService connectionStringService;

    /*
    @PostMapping("/execute")
    public ResponseEntity<?> executeQuery(@RequestBody Map<String, String> request) {
        try {
            // Lấy thông tin kết nối
            Map<String, String> connectionInfo = connectionStringService.getConnectionInfo();
            if (connectionInfo == null) {
                return ResponseEntity.badRequest().body("Chưa thiết lập thông tin kết nối database");
            }

            // Lấy câu truy vấn từ request
            String query = request.get("query");
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Câu truy vấn không được để trống");
            }

            // Tạo DataSource từ thông tin kết nối
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(getDriverClassName(connectionInfo.get("databaseType")));
            dataSource.setUrl(getConnectionUrl(connectionInfo));
            dataSource.setUsername(connectionInfo.get("username"));
            dataSource.setPassword(connectionInfo.get("password"));

            // Tạo JdbcTemplate
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // Thực hiện truy vấn
            if (query.trim().toUpperCase().startsWith("SELECT")) {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
                return ResponseEntity.ok(results);
            } else {
                int affectedRows = jdbcTemplate.update(query);
                return ResponseEntity.ok(Map.of("affectedRows", affectedRows));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi thực hiện truy vấn: " + e.getMessage());
        }
    }

    private String getDriverClassName(String databaseType) {
        switch (databaseType.toLowerCase()) {
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "sqlserver":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            // case "oracle":
            //     return "oracle.jdbc.driver.OracleDriver";
            default:
                throw new IllegalArgumentException("Loại database không được hỗ trợ: " + databaseType);
        }
    }

    private String getConnectionUrl(Map<String, String> connectionInfo) {
        String databaseType = connectionInfo.get("databaseType").toLowerCase();
        String host = connectionInfo.get("host");
        String port = connectionInfo.get("port");
        String databaseName = connectionInfo.get("databaseName");
        String schema = connectionInfo.get("schema");

        switch (databaseType) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%s/%s", host, port, databaseName);
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%s/%s?currentSchema=%s", 
                    host, port, databaseName, schema);
            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%s;databaseName=%s", 
                    host, port, databaseName);
            // case "oracle":
            //     return String.format("jdbc:oracle:thin:@%s:%s:%s", 
            //         host, port, databaseName);
            default:
                throw new IllegalArgumentException("Loại database không được hỗ trợ: " + databaseType);
        }
    }
    */
} 