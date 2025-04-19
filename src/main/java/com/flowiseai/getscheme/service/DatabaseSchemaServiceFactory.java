package com.flowiseai.getscheme.service;

import com.flowiseai.getscheme.model.DatabaseConnectionInfo;
import com.flowiseai.getscheme.model.DatabaseType;
import com.flowiseai.getscheme.mysql.MySQLSchemaService;
import com.flowiseai.getscheme.postgresql.PostgreSQLSchemaService;
import com.flowiseai.getscheme.sqlserver.SqlServerSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaServiceFactory {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaServiceFactory.class);

    @Autowired
    private MySQLSchemaService mySQLSchemaService;

    @Autowired
    private PostgreSQLSchemaService postgreSQLSchemaService;

    @Autowired
    private SqlServerSchemaService sqlServerSchemaService;

    public DatabaseSchemaService getService(DatabaseConnectionInfo connectionInfo) {
        DatabaseType dbType = connectionInfo.getDatabaseTypeEnum();
        if (dbType == null) {
            logger.error("Unsupported database type: {}", connectionInfo.getDatabaseType());
            throw new IllegalArgumentException("Unsupported database type: " + connectionInfo.getDatabaseType());
        }

        switch (dbType) {
            case MYSQL:
                logger.debug("Returning MySQL schema service");
                return mySQLSchemaService;
            case POSTGRESQL:
                logger.debug("Returning PostgreSQL schema service");
                return postgreSQLSchemaService;
            case SQLSERVER:
                logger.debug("Returning SQL Server schema service");
                return sqlServerSchemaService;
            case ORACLE:
                logger.error("Oracle is not supported yet");
                throw new UnsupportedOperationException("Oracle is not supported yet");
            default:
                logger.error("Unsupported database type: {}", dbType);
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
} 