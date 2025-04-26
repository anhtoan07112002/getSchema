package com.flowiseai.getscheme.model;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class TableRelationship {
    private static final Logger logger = LoggerFactory.getLogger(TableRelationship.class);
    
    private String sourceTable;
    private String sourceColumn;
    private String targetTable;
    private String targetColumn;
    private RelationshipType relationshipType;
    private String constraintName;

    public enum RelationshipType {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_MANY;

        private static final Logger logger = LoggerFactory.getLogger(RelationshipType.class);

        public static RelationshipType fromString(String value) {
            logger.debug("Converting string to relationship type: {}", value);
            if (value == null) {
                logger.error("Relationship type cannot be null");
                throw new IllegalArgumentException("Relationship type cannot be null");
            }
            
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.error("Invalid relationship type: {}", value);
                throw new IllegalArgumentException("Invalid relationship type: " + value);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s.%s → %s.%s (%s)", 
            sourceTable, sourceColumn, 
            targetTable, targetColumn, 
            constraintName);
    }
} 