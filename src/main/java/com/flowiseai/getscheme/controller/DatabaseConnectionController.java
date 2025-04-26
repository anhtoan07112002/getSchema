package com.flowiseai.getscheme.controller;

import com.flowiseai.getscheme.model.ConnectionResult;
import com.flowiseai.getscheme.model.DatabaseConnectionInfo;
import com.flowiseai.getscheme.model.TableSchema;
import com.flowiseai.getscheme.model.TableRelationship;
import com.flowiseai.getscheme.model.DatabaseSchema;
import com.flowiseai.getscheme.model.MarkdownSchema;
import com.flowiseai.getscheme.service.CurrentSchemaService;
import com.flowiseai.getscheme.service.DatabaseConnectionService;
import com.flowiseai.getscheme.service.DatabaseSchemaService;
import com.flowiseai.getscheme.service.DatabaseSchemaServiceFactory;
import com.flowiseai.getscheme.service.DatabaseConnectionStringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class DatabaseConnectionController {

    private final DatabaseConnectionService connectionService;
    private final DatabaseSchemaServiceFactory schemaServiceFactory;
    private final CurrentSchemaService currentSchemaService;
    private final DatabaseConnectionStringService connectionStringService;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionController.class);

    @Autowired
    public DatabaseConnectionController(DatabaseConnectionService connectionService, 
                                      DatabaseSchemaServiceFactory schemaServiceFactory,
                                      CurrentSchemaService currentSchemaService,
                                      DatabaseConnectionStringService connectionStringService) {
        this.connectionService = connectionService;
        this.schemaServiceFactory = schemaServiceFactory;
        this.currentSchemaService = currentSchemaService;
        this.connectionStringService = connectionStringService;
    }

    @GetMapping("/api")
    public String showConnectionForm(Model model) {
        model.addAttribute("connectionInfo", new DatabaseConnectionInfo());
        return "connection-form";
    }

    @PostMapping("/connect")
    public String connectToDatabase(@ModelAttribute DatabaseConnectionInfo connectionInfo, Model model) {
        ConnectionResult result = connectionService.connectToDatabase(connectionInfo);
        model.addAttribute("result", result);
        model.addAttribute("connectionInfo", connectionInfo);
        return "connection-result";
    }

    @PostMapping("/select-tables")
    public String selectTables(@ModelAttribute DatabaseConnectionInfo connectionInfo,
                             @RequestParam(value = "selectedTables", required = false) List<String> selectedTables,
                             Model model) {
        if (selectedTables == null || selectedTables.isEmpty()) {
            ConnectionResult result = connectionService.connectToDatabase(connectionInfo);
            model.addAttribute("result", result);
            model.addAttribute("connectionInfo", connectionInfo);
            model.addAttribute("error", "Vui lòng chọn ít nhất một bảng");
            return "connection-result";
        }

        // Lấy service phù hợp từ factory
        DatabaseSchemaService schemaService = schemaServiceFactory.getService(connectionInfo);

        // Lấy thông tin schema và mối quan hệ
        List<TableSchema> tableSchemas = schemaService.getTableSchemas(connectionInfo, selectedTables);
        List<TableRelationship> relationships = schemaService.getTableRelationships(connectionInfo, selectedTables);
        Map<String, List<Map<String, Object>>> sampleData = schemaService.getSampleData(connectionInfo, selectedTables);

        // Tạo đối tượng DatabaseSchema
        DatabaseSchema schema = new DatabaseSchema();
        schema.setTables(tableSchemas);
        schema.setRelationships(relationships);
        schema.setSampleData(sampleData);
        schema.setDatabaseType(connectionInfo.getDatabaseType());
        schema.setDatabaseName(connectionInfo.getDatabaseName());
        schema.setHost(connectionInfo.getHost());
        schema.setPort(connectionInfo.getPort());
        schema.setSchema(connectionInfo.getSchema());

        // Chuyển đổi sang markdown
        String markdownContent = schema.toMarkdown();
        
        // Log chuỗi markdown thực tế
        logger.debug("Generated markdown content:\n{}", markdownContent);

        // Lưu schema hiện tại
        currentSchemaService.setCurrentSchema(connectionInfo.getSchema(), markdownContent);

        // Lưu thông tin kết nối
        connectionStringService.setConnectionString(
            connectionInfo.getDatabaseType(),
            connectionInfo.getHost(),
            connectionInfo.getPort(),
            connectionInfo.getDatabaseName(),
            connectionInfo.getUsername(),
            connectionInfo.getPassword(),
            connectionInfo.getSchema()
        );

        model.addAttribute("connectionInfo", connectionInfo);
        model.addAttribute("tableSchemas", tableSchemas);
        model.addAttribute("relationships", relationships);
        model.addAttribute("sampleData", sampleData);
        return "schema-result";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @PostMapping("/api/schema")
    @ResponseBody
    public Map<String, String> getDatabaseSchema(@ModelAttribute DatabaseConnectionInfo connectionInfo,
                                          @RequestParam(value = "selectedTables", required = false) List<String> selectedTables) {
        if (selectedTables == null || selectedTables.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một bảng");
        }

        // Lấy service phù hợp từ factory
        DatabaseSchemaService schemaService = schemaServiceFactory.getService(connectionInfo);

        // Lấy thông tin schema và mối quan hệ
        List<TableSchema> tableSchemas = schemaService.getTableSchemas(connectionInfo, selectedTables);
        List<TableRelationship> relationships = schemaService.getTableRelationships(connectionInfo, selectedTables);
        Map<String, List<Map<String, Object>>> sampleData = schemaService.getSampleData(connectionInfo, selectedTables);

        // Tạo đối tượng DatabaseSchema
        DatabaseSchema schema = new DatabaseSchema();
        schema.setTables(tableSchemas);
        schema.setRelationships(relationships);
        schema.setSampleData(sampleData);
        schema.setDatabaseType(connectionInfo.getDatabaseType());
        schema.setDatabaseName(connectionInfo.getDatabaseName());
        schema.setHost(connectionInfo.getHost());
        schema.setPort(connectionInfo.getPort());
        schema.setSchema(connectionInfo.getSchema());

        // Chuyển đổi sang markdown
        String markdownContent = schema.toMarkdown();
        
        // Log chuỗi markdown thực tế
        logger.debug("Generated markdown content:\n{}", markdownContent);

        // Lưu schema hiện tại
        currentSchemaService.setCurrentSchema(connectionInfo.getSchema(), markdownContent);

        // Lưu thông tin kết nối
        connectionStringService.setConnectionString(
            connectionInfo.getDatabaseType(),
            connectionInfo.getHost(),
            connectionInfo.getPort(),
            connectionInfo.getDatabaseName(),
            connectionInfo.getUsername(),
            connectionInfo.getPassword(),
            connectionInfo.getSchema()
        );

        Map<String, String> result = new HashMap<>();
        result.put("schema", connectionInfo.getSchema());
        result.put("markdown", markdownContent);
        return result;
    }

    @GetMapping("/api/schema/current")
    @ResponseBody
    public Map<String, String> getCurrentSchema() {
        Map<String, String> currentSchema = currentSchemaService.getCurrentSchema();
        if (currentSchema == null) {
            throw new IllegalArgumentException("Chưa có schema nào được chọn");
        }
        return currentSchema;
    }

    @GetMapping("/api/connection/string")
    @ResponseBody
    public Map<String, String> getConnectionString() {
        Map<String, String> connectionInfo = connectionStringService.getConnectionInfo();
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Chưa có thông tin kết nối nào được lưu");
        }
        return connectionInfo;
    }
}