package com.flowiseai.getscheme.model;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Data
public class DatabaseSchema {
    private List<TableSchema> tables;
    private List<TableRelationship> relationships;
    private Map<String, List<Map<String, Object>>> sampleData;
    private String databaseType;
    private String databaseName;
    private String host;
    private String port;
    private String schema;

    public String toMarkdown() {
        StringBuilder markdown = new StringBuilder();
        
        // Thông tin các bảng
        for (TableSchema table : tables) {
            // Tên bảng
            markdown.append("## Table: ").append(table.getTableName()).append("\n\n");
            
            // SQL tạo bảng
            String createTableSql = table.getCreateTableSql();
            if (createTableSql != null) {
                // Loại bỏ các thông tin không cần thiết
                createTableSql = createTableSql.replaceAll("ENGINE=.*", "");
                createTableSql = createTableSql.replaceAll("DEFAULT CHARSET=.*", "");
                createTableSql = createTableSql.replaceAll("COLLATE=.*", "");
                createTableSql = createTableSql.replaceAll("`", "");
                
                // Tách các phần của SQL
                String[] parts = createTableSql.split("\\(");
                String tableName = parts[0].replace("CREATE TABLE", "").trim();
                String columnsPart = parts[1].replaceAll("\\)\\s*;?$", "");
                
                // Tách các cột
                String[] columns = columnsPart.split(",");
                
                // Xây dựng lại SQL với định dạng đẹp
                markdown.append("```sql\n");
                markdown.append("CREATE TABLE ").append(tableName).append(" (\n");
                
                for (int i = 0; i < columns.length; i++) {
                    String column = columns[i].trim();
                    if (!column.isEmpty() && 
                        !column.matches("PRIMARY KEY.*") && 
                        !column.matches("KEY.*") && 
                        !column.matches("CONSTRAINT.*")) {
                        markdown.append(column);
                        if (i < columns.length - 1) {
                            markdown.append(",");
                        }
                        markdown.append("\n");
                    }
                }
                
                // Thêm các ràng buộc
                for (String column : columns) {
                    if (column.trim().matches("PRIMARY KEY.*")) {
                        markdown.append(column.trim()).append("\n");
                    }
                }
                
                for (String column : columns) {
                    if (column.trim().matches("CONSTRAINT.*")) {
                        markdown.append(column.trim()).append("\n");
                    }
                }
                
                markdown.append(");\n");
                markdown.append("```\n\n");
            } else {
                markdown.append("```sql\n-- No create table SQL available\n```\n\n");
            }
            
            // Dữ liệu mẫu
            markdown.append("### Sample data from ").append(table.getTableName()).append(":\n\n");
            List<Map<String, Object>> samples = sampleData.get(table.getTableName());
            if (samples != null && !samples.isEmpty()) {
                // Tạo header
                StringJoiner header = new StringJoiner(" | ");
                for (String column : samples.get(0).keySet()) {
                    header.add(column);
                }
                markdown.append("| ").append(header).append(" |\n");
                
                // Tạo separator
                StringJoiner separator = new StringJoiner(" | ");
                for (int i = 0; i < samples.get(0).size(); i++) {
                    separator.add("---");
                }
                markdown.append("| ").append(separator).append(" |\n");
                
                // Tạo dữ liệu
                for (Map<String, Object> row : samples) {
                    StringJoiner data = new StringJoiner(" | ");
                    for (Object value : row.values()) {
                        data.add(value != null ? value.toString() : "NULL");
                    }
                    markdown.append("| ").append(data).append(" |\n");
                }
            } else {
                markdown.append("No sample data available\n");
            }
            markdown.append("\n");
        }

        // Mối quan hệ giữa các bảng
        if (!relationships.isEmpty()) {
            markdown.append("## Relationships Between Tables\n\n");
            for (TableRelationship relationship : relationships) {
                markdown.append("- ")
                    .append(relationship.getSourceTable()).append(".")
                    .append(relationship.getSourceColumn()).append(" → ")
                    .append(relationship.getTargetTable()).append(".")
                    .append(relationship.getTargetColumn()).append(" (")
                    .append(relationship.getConstraintName()).append(")\n");
            }
            markdown.append("\n");

            // Tóm tắt mối quan hệ
            markdown.append("## Database Relationships Summary\n\n");
            for (TableRelationship relationship : relationships) {
                markdown.append("- ")
                    .append(relationship.getSourceTable()).append(".")
                    .append(relationship.getSourceColumn()).append(" = ")
                    .append(relationship.getTargetTable()).append(".")
                    .append(relationship.getTargetColumn());
                if (relationship.getSourceTable().equals(relationship.getTargetTable())) {
                    markdown.append(" (self-reference)");
                }
                markdown.append("\n");
            }
        }

        return markdown.toString();
    }
} 