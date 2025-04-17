package com.flowiseai.getscheme.controller;

import com.flowiseai.getscheme.model.ConnectionResult;
import com.flowiseai.getscheme.model.DatabaseConnectionInfo;
import com.flowiseai.getscheme.model.TableSchema;
import com.flowiseai.getscheme.model.TableRelationship;
import com.flowiseai.getscheme.service.DatabaseConnectionService;
import com.flowiseai.getscheme.service.DatabaseSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class DatabaseConnectionController {

    private final DatabaseConnectionService connectionService;
    private final DatabaseSchemaService schemaService;

    @Autowired
    public DatabaseConnectionController(DatabaseConnectionService connectionService, 
                                      DatabaseSchemaService schemaService) {
        this.connectionService = connectionService;
        this.schemaService = schemaService;
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

        // Lấy thông tin schema và mối quan hệ
        List<TableSchema> tableSchemas = schemaService.getTableSchemas(connectionInfo, selectedTables);
        List<TableRelationship> relationships = schemaService.getTableRelationships(connectionInfo, selectedTables);
        Map<String, List<Map<String, Object>>> sampleData = schemaService.getSampleData(connectionInfo, selectedTables);

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
}