package com.flowiseai.getscheme.model;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class DatabaseConnectionInfo {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionInfo.class);
    
    private String databaseType;
    private String host;
    private String port;
    private String databaseName;
    private String username;
    private String password;
    private String schema;

    public DatabaseType getDatabaseTypeEnum() {
        logger.debug("Getting database type enum for: {}", databaseType);
        return DatabaseType.fromString(databaseType);
    }

    // Giá trị mặc định cho từng loại database
    public static class Defaults {
        public static final String MYSQL_HOST = "localhost";
        public static final String MYSQL_PORT = "3306";
        public static final String MYSQL_USER = "root";
        public static final String MYSQL_PASSWORD = "0";
        public static final String MYSQL_SCHEMA = "";

        public static final String POSTGRESQL_HOST = "localhost";
        public static final String POSTGRESQL_PORT = "5432";
        public static final String POSTGRESQL_USER = "postgres";
        public static final String POSTGRESQL_PASSWORD = "0";
        public static final String POSTGRESQL_SCHEMA = "public";

        public static final String SQLSERVER_HOST = "localhost";
        public static final String SQLSERVER_PORT = "1433";
        public static final String SQLSERVER_USER = "sa";
        public static final String SQLSERVER_PASSWORD = "Panda1@5Manda";
        public static final String SQLSERVER_SCHEMA = "dbo";

        // Oracle default values - not supported yet
        /*
        public static final String ORACLE_HOST = "localhost";
        public static final String ORACLE_PORT = "1521";
        public static final String ORACLE_USER = "SYSTEM";
        public static final String ORACLE_PASSWORD = "Panda1@5Manda";
        public static final String ORACLE_SERVICE_NAME = "FREEPDB1";
        public static final String ORACLE_SCHEMA = "";
        */
    }

    public String getUrl() {
        logger.debug("Generating database URL for type: {}", databaseType);
        String effectiveHost = host != null ? host : getDefaultHost();
        String effectiveDatabase = databaseName != null ? databaseName : "";
        DatabaseType dbType = getDatabaseTypeEnum();

        if (port == null || port.isEmpty()) {
            logger.debug("Using URL without port for database: {}", dbType);
            switch (dbType) {
                case MYSQL:
                    return String.format("jdbc:mysql://%s/%s", effectiveHost, effectiveDatabase);
                case POSTGRESQL:
                    return String.format("jdbc:postgresql://%s/%s?currentSchema=%s&ssl=false&prepareThreshold=0", 
                        effectiveHost, effectiveDatabase, getEffectiveSchema());
                case SQLSERVER:
                    return String.format("jdbc:sqlserver://%s;databaseName=%s;encrypt=true;trustServerCertificate=true", effectiveHost, effectiveDatabase);
                // case ORACLE:
                //     return String.format("jdbc:oracle:thin:@%s:%s/%s", effectiveHost, effectiveHost, Defaults.ORACLE_SERVICE_NAME);
                default:
                    logger.error("Unsupported database type: {}", databaseType);
                    throw new IllegalArgumentException("Unsupported database type: " + databaseType);
            }
        }

        String effectivePort = port;
        logger.debug("Using URL with port {} for database: {}", effectivePort, dbType);
        switch (dbType) {
            case MYSQL:
                return String.format("jdbc:mysql://%s:%s/%s", effectiveHost, effectivePort, effectiveDatabase);
            case POSTGRESQL:
                return String.format("jdbc:postgresql://%s:%s/%s?currentSchema=%s&ssl=false&prepareThreshold=0", 
                    effectiveHost, effectivePort, effectiveDatabase, getEffectiveSchema());
            case SQLSERVER:
                return String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true", 
                    effectiveHost, effectivePort, effectiveDatabase);
            // case ORACLE:
            //     return String.format("jdbc:oracle:thin:@%s:%s/%s", effectiveHost, effectivePort, Defaults.ORACLE_SERVICE_NAME);
            default:
                logger.error("Unsupported database type: {}", databaseType);
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }

    public String getDriverClassName() {
        logger.debug("Getting driver class name for database type: {}", databaseType);
        DatabaseType dbType = getDatabaseTypeEnum();
        switch (dbType) {
            case MYSQL:
                return "com.mysql.cj.jdbc.Driver";
            case POSTGRESQL:
                return "org.postgresql.Driver";
            case SQLSERVER:
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            // case ORACLE:
            //     return "oracle.jdbc.OracleDriver";
            default:
                logger.error("Unsupported database type: {}", databaseType);
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }

    private String getDefaultHost() {
        DatabaseType dbType = getDatabaseTypeEnum();
        switch (dbType) {
            case MYSQL: return Defaults.MYSQL_HOST;
            case POSTGRESQL: return Defaults.POSTGRESQL_HOST;
            case SQLSERVER: return Defaults.SQLSERVER_HOST;
            // case ORACLE: return Defaults.ORACLE_HOST;
            default: return "localhost";
        }
    }

    private String getDefaultPort() {
        DatabaseType dbType = getDatabaseTypeEnum();
        switch (dbType) {
            case MYSQL: return Defaults.MYSQL_PORT;
            case POSTGRESQL: return Defaults.POSTGRESQL_PORT;
            case SQLSERVER: return Defaults.SQLSERVER_PORT;
            // case ORACLE: return Defaults.ORACLE_PORT;
            default: return "3306";
        }
    }

    public String getEffectiveUsername() {
        if (username != null && !username.isEmpty()) {
            return username;
        }
        DatabaseType dbType = getDatabaseTypeEnum();
        switch (dbType) {
            case MYSQL: return Defaults.MYSQL_USER;
            case POSTGRESQL: return Defaults.POSTGRESQL_USER;
            case SQLSERVER: return Defaults.SQLSERVER_USER;
            // case ORACLE: return Defaults.ORACLE_USER;
            default: return "";
        }
    }

    public String getEffectivePassword() {
        if (password != null) {
            return password;
        }
        DatabaseType dbType = getDatabaseTypeEnum();
        switch (dbType) {
            case MYSQL: return Defaults.MYSQL_PASSWORD;
            case POSTGRESQL: return Defaults.POSTGRESQL_PASSWORD;
            case SQLSERVER: return Defaults.SQLSERVER_PASSWORD;
            // case ORACLE: return Defaults.ORACLE_PASSWORD;
            default: return "";
        }
    }

    public String getEffectiveSchema() {
        if (schema != null && !schema.isEmpty()) {
            return schema;
        }
        DatabaseType dbType = getDatabaseTypeEnum();
        switch (dbType) {
            case MYSQL: return Defaults.MYSQL_SCHEMA;
            case POSTGRESQL: return Defaults.POSTGRESQL_SCHEMA;
            case SQLSERVER: return Defaults.SQLSERVER_SCHEMA;
            // case ORACLE: return Defaults.ORACLE_SCHEMA;
            default: return "";
        }
    }
}
