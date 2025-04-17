package com.flowiseai.getscheme.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TableSchema {
    private String tableName;
    private String createTableSql;
    private List<Map<String, Object>> sampleData;
}

@Data
class ColumnInfo {
    private String columnName;
    private String dataType;
    private Integer columnSize;
    private Boolean isNullable;
    private String defaultValue;
    private Boolean isPrimaryKey;
    private Boolean isForeignKey;
    private String referencedTable;
    private String referencedColumn;
} 