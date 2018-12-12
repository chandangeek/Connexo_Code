/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.oracle.impl;

import oracle.ucp.jdbc.PoolDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Created by bbl on 18/05/2016.
 */
public class UcpWrappedDataSource implements DataSource {

    private final PoolDataSource poolDataSource;

    public UcpWrappedDataSource(PoolDataSource poolDataSource) {
        this.poolDataSource = poolDataSource;
    }


    @Override
    public Connection getConnection() throws SQLException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(poolDataSource.getClass().getClassLoader());
            return poolDataSource.getConnection();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(poolDataSource.getClass().getClassLoader());
            return poolDataSource.getConnection(username, password);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return poolDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        poolDataSource.setLogWriter(out);

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        poolDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return poolDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return poolDataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return poolDataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return poolDataSource.isWrapperFor(iface);
    }
}
