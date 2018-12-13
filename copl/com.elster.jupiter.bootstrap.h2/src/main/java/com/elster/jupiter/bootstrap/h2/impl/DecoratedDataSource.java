/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.h2.impl;

import com.google.common.collect.ImmutableList;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class DecoratedDataSource implements DataSource {

    private final DataSource decorated;

    private final List<DecoratedConnection> openConnections = new ArrayList<>();
    private final AtomicInteger identifiers = new AtomicInteger();

    public DecoratedDataSource(DataSource decorated) {
        this.decorated = decorated;
    }

    public Connection getConnection() throws SQLException {
        purgeClosed();
        DecoratedConnection decoratedConnection = new DecoratedConnection(decorated.getConnection(), identifiers.incrementAndGet());
        openConnections.add(decoratedConnection);
        return decoratedConnection;
    }

    private void purgeClosed() throws SQLException {
        for (Iterator<DecoratedConnection> iterator = openConnections.iterator(); iterator.hasNext(); ) {
            if (iterator.next().isClosed()) {
                iterator.remove();
            }
        }
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        decorated.setLogWriter(out);
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return decorated.getParentLogger();
    }

    public int getLoginTimeout() throws SQLException {
        return decorated.getLoginTimeout();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        purgeClosed();
        DecoratedConnection decoratedConnection = new DecoratedConnection(decorated.getConnection(username, password), identifiers.incrementAndGet());
        openConnections.add(decoratedConnection);
        return decoratedConnection;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return decorated.getLogWriter();
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return true;
        }
        if (iface.isInstance(decorated)) {
            return true;
        }
        return !(decorated instanceof JdbcDataSource) && decorated.isWrapperFor(iface);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        decorated.setLoginTimeout(seconds);
    }

    @SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        if (iface.isInstance(decorated)) {
            return (T) decorated;
        }
        if (decorated instanceof JdbcDataSource) {
            throw new SQLException();
        }
        return decorated.unwrap(iface);
    }

    public List<DecoratedConnection> getOpenConnections() {
        try {
            purgeClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ImmutableList.copyOf(openConnections);
    }
}
