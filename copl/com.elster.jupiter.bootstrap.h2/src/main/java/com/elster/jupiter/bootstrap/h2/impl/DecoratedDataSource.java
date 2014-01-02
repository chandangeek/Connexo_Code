package com.elster.jupiter.bootstrap.h2.impl;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class DecoratedDataSource implements DataSource {

    private final DataSource decorated;

    public DecoratedDataSource(DataSource decorated) {
        this.decorated = decorated;
    }

    public Connection getConnection() throws SQLException {
        return new DecoratedConnection(decorated.getConnection());
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
        return new DecoratedConnection(decorated.getConnection(username, password));
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
}
