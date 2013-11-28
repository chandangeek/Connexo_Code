package com.elster.jupiter.bootstrap.impl;

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
        return decorated.isWrapperFor(iface);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        decorated.setLoginTimeout(seconds);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return decorated.unwrap(iface);
    }
}
