package com.flowiseai.getscheme.model;

import lombok.Data;

@Data
public class TableRelationship {
    private String sourceTable;
    private String sourceColumn;
    private String targetTable;
    private String targetColumn;
    private String constraintName;
    private String relationshipType; // ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY

    @Override
    public String toString() {
        return String.format("%s.%s → %s.%s (%s)", 
            sourceTable, sourceColumn, 
            targetTable, targetColumn, 
            constraintName);
    }
} 