package com.elster.jupiter.bootstrap.oracle.impl;

import oracle.jdbc.pool.OracleConnectionCacheManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;

/**
 * Created by bbl on 21/10/2015.
 */
@Component(name = "com.elster.jupiter.bootstrap.oracle.jmx",
        property = {"jmx.objectname=com.elster.jupiter:type=ConnectionPool"})
public class OracleConnectionManagerCache implements ConnectionPoolManagerMBean {

    private String jdbcUrl;
    private String userName;

    @Activate
    public void activate(BundleContext context) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        context.registerService(MBeanServer.class.getName(), mbs, null);

        jdbcUrl = context.getProperty(BootstrapServiceImpl.JDBC_DRIVER_URL);
        userName = context.getProperty(BootstrapServiceImpl.JDBC_USER);
    }

    @Override
    public int getActiveConnections() {
        try {
            return getOracleConnectionManager().getNumberOfActiveConnections(BootstrapServiceImpl.ORACLE_CONNECTION_POOL_NAME);
        } catch (SQLException e) {
            return -1;
        }
    }

    private OracleConnectionCacheManager getOracleConnectionManager() {
        try {
            return OracleConnectionCacheManager.getConnectionCacheManagerInstance();
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public int getAvailableConnections() {
        try {
            return getOracleConnectionManager().getNumberOfAvailableConnections(BootstrapServiceImpl.ORACLE_CONNECTION_POOL_NAME);
        } catch (SQLException e) {
            return -1;
        }
    }

    @Override
    public String[] getCacheNames() {
        try {
            return getOracleConnectionManager().getCacheNameList();
        } catch (SQLException e) {
            return new String[0];
        }
    }

    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String getSchemaName() {
        return userName;
    }
}
