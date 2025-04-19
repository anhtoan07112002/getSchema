package com.flowiseai.getscheme.model;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class TableSchema {
    private static final Logger logger = LoggerFactory.getLogger(TableSchema.class);
    
    private String tableName;
    private List<ColumnInfo> columns = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();
    private List<String> foreignKeys = new ArrayList<>();
    private String createTableSql;
    private List<Map<String, Object>> sampleData;

    @Data
    public static class ColumnInfo {
        private static final Logger logger = LoggerFactory.getLogger(ColumnInfo.class);
        
        private String columnName;
        private String dataType;
        private Integer size;
        private boolean nullable;
        private String defaultValue;
        private boolean isPrimaryKey;
        private boolean isForeignKey;

        public ColumnInfo(String columnName, String dataType) {
            logger.debug("Creating new column info - Name: {}, Type: {}", columnName, dataType);
            this.columnName = columnName;
            this.dataType = dataType;
            this.nullable = true;
            this.isPrimaryKey = false;
            this.isForeignKey = false;
        }
    }

    public void addColumn(String columnName, String dataType, int columnSize, boolean isNullable, String defaultValue) {
        ColumnInfo column = new ColumnInfo(columnName, dataType);
        column.setSize(columnSize);
        column.setNullable(isNullable);
        column.setDefaultValue(defaultValue);
        column.setPrimaryKey(primaryKeys.contains(columnName));
        column.setForeignKey(foreignKeys.contains(columnName));
        columns.add(column);
    }

    public void addPrimaryKey(String columnName) {
        if (!primaryKeys.contains(columnName)) {
            primaryKeys.add(columnName);
            // Cập nhật trạng thái primary key cho cột tương ứng
            columns.stream()
                .filter(c -> c.getColumnName().equals(columnName))
                .forEach(c -> c.setPrimaryKey(true));
        }
    }

    public void addForeignKey(String columnName) {
        if (!foreignKeys.contains(columnName)) {
            foreignKeys.add(columnName);
            // Cập nhật trạng thái foreign key cho cột tương ứng
            columns.stream()
                .filter(c -> c.getColumnName().equals(columnName))
                .forEach(c -> c.setForeignKey(true));
        }
    }
} 