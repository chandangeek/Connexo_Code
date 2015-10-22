package com.elster.jupiter.bootstrap.oracle.impl;

/**
 * Created by bbl on 21/10/2015.
 */
public interface ConnectionPoolManagerMBean {
    int getActiveConnections();

    int getAvailableConnections();

    String[] getCacheNames();

    String getJdbcUrl();

    String getSchemaName();
}
