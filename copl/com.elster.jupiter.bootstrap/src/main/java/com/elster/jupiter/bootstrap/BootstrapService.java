/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap;

import aQute.bnd.annotation.ProviderType;

import javax.sql.DataSource;

/**
 * This Service is responsible for creating a DataSource on demand.
 */
@ProviderType
public interface BootstrapService {

    String JDBC_USER = "com.elster.jupiter.datasource.jdbcuser";
    String JDBC_PASSWORD = "com.elster.jupiter.datasource.jdbcpassword";
    String KEY_FILE = "com.elster.jupiter.datasource.keyfile";
    String JDBC_POOLMAXLIMIT = "com.elster.jupiter.datasource.pool.maxlimit";
    String JDBC_POOLMAXLIMIT_DEFAULT = "100";
    String JDBC_POOLMAXSTATEMENTS = "com.elster.jupiter.datasource.pool.maxstatements";
    String JDBC_POOLMAXSTATEMENTS_DEFAULT = "50";
    String JDBC_DRIVER_URL = "com.elster.jupiter.datasource.jdbcurl";
    String JDBC_CONNECTION_WAIT_TIMEOUT = "com.elster.jupiter.datasource.pool.connection.wait.timeout";
    String JDBC_INACTIVITY_TIMEOUT = "com.elster.jupiter.datasource.pool.inactivity.timeout";
    String JDBC_TTL_TIMEOUT = "com.elster.jupiter.datasource.pool.timetolive";
    String JDBC_ABANDONED_CONNECTION_TIMEOUT = "com.elster.jupiter.datasource.pool.abandoned.timeout";
    String JDBC_MAX_CONNECTION_REUSE_TIME = "com.elster.jupiter.datasource.pool.maxconnectionreusetime";
    String CONNECTION_POOL_NAME = "ConnexoConnectionPool";
    String CONNECTION_POOL_PROVIDER = "com.elster.jupiter.datasource.pool.provider";
    String HIKARI_CP = "hikari";
    String ORACLE_CP = "oracle";

    /**
     * @return a newly created DataSource instance.
     */
	DataSource createDataSource();
}
