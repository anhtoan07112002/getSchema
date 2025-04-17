package com.flowiseai.getscheme.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionResult {
    private boolean success;
    private String message;
    private List<TableInfo> tableInfos;
    private List<Map<String, Object>> databaseInfo;

    @Data
    public static class TableInfo {
        private String tableName;
        private boolean selected;
    }
}
