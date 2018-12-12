/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.oracle.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.DataSourceSetupException;
import com.elster.jupiter.bootstrap.PropertyNotFoundException;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;

import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * This Component is responsible for creating a DataSource on demand. It does so by getting the needed properties from the BundleContext.
 * <br/>
 * Required properties :
 * <ul>
 * <li><code>com.elster.jupiter.datasource.jdbcurl</code> : the database url.</li>
 * <li><code>com.elster.jupiter.datasource.jdbcuser</code> : the database user.</li>
 * <li><code>com.elster.jupiter.datasource.jdbcpassword</code> : the database user's password.</li>
 * </ul>
 * Optional properties :
 * <ul>
 * <li><code>com.elster.jupiter.datasource.pool.maxlimit</code> : max limit, will default to 50.</li>
 * <li><code>com.elster.jupiter.datasource.pool.maxstatements</code> : max statements, will default to 50.</li>
 * <li><code>com.elster.jupiter.datasource.pool.oracle.ons.nodes</code> : ons nodes information. </li>
 * </ul>
 */
@Component(name = "com.elster.jupiter.bootstrap.oracle",
        property = {"osgi.command.scope=orm", "osgi.command.function=dbConnection"})
public final class BootstrapServiceImpl implements BootstrapService {

    static final String ORACLE_CONNECTION_POOL_NAME = "OracleConnectionPool";
    public static final String ORACLE_ONS_NODES = "com.elster.jupiter.datasource.pool.oracle.ons.nodes";

    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private int maxLimit;
    private int maxStatementsLimit;
    private String onsNodes;

    private Holder<DataSource> cache = HolderBuilder.lazyInitialize(this::doCreateDataSource);
    private boolean initialized = false;

    public BootstrapServiceImpl() {
    }

    @Inject
    public BootstrapServiceImpl(BundleContext bundleContext) {
        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext context) {
        jdbcUrl = getRequiredProperty(context, JDBC_DRIVER_URL);
        jdbcUser = getRequiredProperty(context, JDBC_USER);
        jdbcPassword = getRequiredProperty(context, JDBC_PASSWORD);
        maxLimit = getOptionalIntProperty(context, JDBC_POOLMAXLIMIT, Integer.parseInt(JDBC_POOLMAXLIMIT_DEFAULT));
        maxStatementsLimit = getOptionalIntProperty(context, JDBC_POOLMAXSTATEMENTS, Integer.parseInt(JDBC_POOLMAXSTATEMENTS_DEFAULT));
        onsNodes = getOptionalProperty(context, ORACLE_ONS_NODES, null);
    }

    @Deactivate
    public void deactivate() {
        if (initialized) {
            try {
                UniversalConnectionPoolManager manager = UniversalConnectionPoolManagerImpl. getUniversalConnectionPoolManager();
                manager.destroyConnectionPool(ORACLE_CONNECTION_POOL_NAME);
            } catch (UniversalConnectionPoolException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public DataSource createDataSource() {
        DataSource dataSource = cache.get();
        initialized = true;
        return dataSource;
    }

    private DataSource doCreateDataSource() {
        DataSource dataSource;
        try {
            dataSource = createDataSourceFromProperties();
        } catch (SQLException e) {
            // Basically this should never occur, since we're not accessing the DB in any way, just yet.
            throw new DataSourceSetupException(e);
        }
        return dataSource;
    }

    private DataSource createDataSourceFromProperties() throws SQLException {
        PoolDataSourceImpl source = (PoolDataSourceImpl) PoolDataSourceFactory.getPoolDataSource();
        source.setConnectionFactoryClassName("oracle.jdbc.replay.OracleDataSourceImpl");
        source.setURL(jdbcUrl);
        source.setUser(jdbcUser);
        source.setPassword(jdbcPassword);
        source.setConnectionPoolName(ORACLE_CONNECTION_POOL_NAME);
        source.setMinPoolSize(3);
        source.setMaxPoolSize(maxLimit);
        source.setInitialPoolSize(3);
        source.setMaxStatements(maxStatementsLimit);
        //source.setInactiveConnectionTimeout(PropertiesHelper.getInt(INACTIVITY_TIMEOUT, properties, 0));
        //source.setTimeToLiveConnectionTimeout(PropertiesHelper.getInt(TTL_TIMEOUT, properties, 0));
        //source.setAbandonedConnectionTimeout(PropertiesHelper.getInt(ABANDONED_CONNECTION_TIMEOUT, properties, 0));
        //source.setPropertyCycle(PropertiesHelper.getInt(PROPERTY_CHECK_INTERVAL, properties, 900));
        source.setConnectionWaitTimeout(10);
        source.setValidateConnectionOnBorrow(true);
        source.setFastConnectionFailoverEnabled(true);
        if (onsNodes != null) {
            source.setONSConfiguration("nodes=" + onsNodes);
        }
        // for now , no need to set connection properties , but possible interesting keys are
        // defaultRowPrefetch
        // oracle.jdbc.FreeMemoryOnEnterImplicitCache

        return new UcpWrappedDataSource(source);
    }

    private String getRequiredProperty(BundleContext context, String property) {
        String value = context.getProperty(property);
        if (value == null) {
            throw new PropertyNotFoundException(property);
        }
        return value.trim();
    }

    private String getOptionalProperty(BundleContext context, String property, String defaultValue) {
        String value = context.getProperty(property);
        return value == null ? defaultValue : value.trim();
    }

    private int getOptionalIntProperty(BundleContext context, String propertyName, int defaultValue) {
        String propertyValue = getOptionalProperty(context, propertyName, null);
        return propertyValue == null ? defaultValue : Integer.parseInt(propertyValue);
    }


    public void dbConnection() {
        StringBuilder sb = new StringBuilder("Connection settings :").append("\n");
        sb.append(" jdbcUrl = ").append(jdbcUrl).append("\n");
        sb.append(" dbUser = ").append(jdbcUser).append("\n");
        System.out.println(sb.toString());
    }
}