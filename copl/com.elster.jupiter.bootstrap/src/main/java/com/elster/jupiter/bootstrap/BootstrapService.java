package com.elster.jupiter.bootstrap;

import javax.sql.DataSource;

/**
 * This Service is responsible for creating a DataSource on demand.
 */
public interface BootstrapService {

    String JDBC_USER = "com.elster.jupiter.datasource.jdbcuser";
    String JDBC_PASSWORD = "com.elster.jupiter.datasource.jdbcpassword";
    String JDBC_POOLMAXLIMIT = "com.elster.jupiter.datasource.pool.maxlimit";
    String JDBC_POOLMAXLIMIT_DEFAULT = "50";
    String JDBC_POOLMAXSTATEMENTS = "com.elster.jupiter.datasource.pool.maxstatements";
    String JDBC_POOLMAXSTATEMENTS_DEFAULT = "50";
    String JDBC_DRIVER_URL = "com.elster.jupiter.datasource.jdbcurl";
    /**
     * @return a newly created DataSource instance.
     */
	DataSource createDataSource();
}
