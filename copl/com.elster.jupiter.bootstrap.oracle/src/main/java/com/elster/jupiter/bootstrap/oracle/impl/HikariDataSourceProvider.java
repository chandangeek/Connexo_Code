/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.oracle.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.datasource.DataSourceProvider;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.logging.Logger;

public class HikariDataSourceProvider implements DataSourceProvider {
    private static final Logger LOGGER = Logger.getLogger(HikariDataSourceProvider.class.getName());

    @Override
    public DataSource createDataSource(ConnectionProperties properties) throws SQLException {
        HikariDataSource hikariDataSource = new HikariDataSource();

        hikariDataSource.setJdbcUrl(properties.jdbcUrl);
        hikariDataSource.setUsername(properties.jdbcUser);
        hikariDataSource.setPassword(new PasswordDecryptServiceImpl().getDecryptPassword(properties.jdbcPassword, properties.keyFile));
        hikariDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        hikariDataSource.setMaximumPoolSize(properties.maxLimit);
        hikariDataSource.setMinimumIdle(3);
        hikariDataSource.setIdleTimeout(properties.inactivityTimeout * 1000L);
        hikariDataSource.setMaxLifetime(properties.timeToLive * 1000L);
        hikariDataSource.setConnectionTimeout(properties.connectionWaitTimeout * 1000L);
        hikariDataSource.setLeakDetectionThreshold(properties.abandonedConnectionsTimeout * 1000L);
        hikariDataSource.setPoolName(BootstrapService.CONNECTION_POOL_NAME);

        LOGGER.info("Using HikariCP database connection pool");

        return hikariDataSource;
    }
}
