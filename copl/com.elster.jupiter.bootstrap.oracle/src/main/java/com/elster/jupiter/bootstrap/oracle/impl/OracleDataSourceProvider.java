/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.oracle.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.datasource.DataSourceProvider;

import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSourceImpl;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.logging.Logger;

public class OracleDataSourceProvider implements DataSourceProvider {
    private static final Logger LOGGER = Logger.getLogger(OracleDataSourceProvider.class.getName());

    @Override
    public DataSource createDataSource(ConnectionProperties properties) throws SQLException {
        PoolDataSourceImpl source = (PoolDataSourceImpl) PoolDataSourceFactory.getPoolDataSource();
        source.setConnectionFactoryClassName("oracle.jdbc.replay.OracleDataSourceImpl");
        source.setURL(properties.jdbcUrl);
        source.setUser(properties.jdbcUser);
        source.setPassword(new PasswordDecryptServiceImpl().getDecryptPassword(properties.jdbcPassword, properties.keyFile));
        source.setConnectionPoolName(BootstrapService.CONNECTION_POOL_NAME);
        source.setMinPoolSize(3);
        source.setMaxPoolSize(properties.maxLimit);
        source.setInitialPoolSize(3);
        source.setMaxStatements(properties.maxStatementsLimit);
        source.setInactiveConnectionTimeout(properties.inactivityTimeout);
        source.setTimeToLiveConnectionTimeout(properties.timeToLive);
        source.setAbandonedConnectionTimeout(properties.abandonedConnectionsTimeout);
        source.setMaxConnectionReuseTime(properties.maxConnectionReuseTime);
        //source.setPropertyCycle(PropertiesHelper.getInt(PROPERTY_CHECK_INTERVAL, properties, 900));
        source.setConnectionWaitTimeout(properties.connectionWaitTimeout);
        source.setValidateConnectionOnBorrow(true);
        source.setFastConnectionFailoverEnabled(true);
        if (properties.onsNodes != null) {
            source.setONSConfiguration("nodes=" + properties.onsNodes);
        }
        // possible interesting keys are
        // defaultRowPrefetch
        // oracle.jdbc.FreeMemoryOnEnterImplicitCache

        LOGGER.info("Using Oracle UCP database connection pool");

        return new UcpWrappedDataSource(source);
    }
}
