/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.offline.services.bootstrap.impl;

import org.h2.jdbc.JdbcConnection;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class H2DecoratedConnection implements Connection {

    private final Connection decorated;

    private final int identifier;
    private final StackTraceElement[] stackTrace;

    public H2DecoratedConnection(Connection decorated, int identifier) {
        this.decorated = decorated;
        this.identifier = identifier;
        stackTrace = Thread.currentThread().getStackTrace();
    }

    public void abort(Executor executor) throws SQLException {
        decorated.abort(executor);
    }

    public boolean isReadOnly() throws SQLException {
        return decorated.isReadOnly();
    }

    public Savepoint setSavepoint() throws SQLException {
        return decorated.setSavepoint();
    }

    public Properties getClientInfo() throws SQLException {
        return decorated.getClientInfo();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return decorated.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public String getSchema() throws SQLException {
        return decorated.getSchema();
    }

    public void close() throws SQLException {
        decorated.close();
    }

    public void setHoldability(int holdability) throws SQLException {
        decorated.setHoldability(holdability);
    }

    public void setTransactionIsolation(int level) throws SQLException {
        decorated.setTransactionIsolation(level);
    }

    public String getClientInfo(String name) throws SQLException {
        return decorated.getClientInfo(name);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        decorated.setClientInfo(name, value);
    }

    public int getTransactionIsolation() throws SQLException {
        return decorated.getTransactionIsolation();
    }

    public Statement createStatement() throws SQLException {
        return decorated.createStatement();
    }

    public boolean getAutoCommit() throws SQLException {
        return decorated.getAutoCommit();
    }

    public int getNetworkTimeout() throws SQLException {
        return decorated.getNetworkTimeout();
    }

    public Clob createClob() throws SQLException {
        return decorated.createClob();
    }

    public void setSchema(String schema) throws SQLException {
        decorated.setSchema(schema);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return decorated.prepareStatement(sql, columnIndexes);
    }

    public NClob createNClob() throws SQLException {
        return decorated.createNClob();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return decorated.prepareStatement(sql);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        decorated.releaseSavepoint(savepoint);
    }

    public SQLWarning getWarnings() throws SQLException {
        return decorated.getWarnings();
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return decorated.prepareCall(sql);
    }

    public SQLXML createSQLXML() throws SQLException {
        return decorated.createSQLXML();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return decorated.getMetaData();
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return decorated.getTypeMap();
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return decorated.prepareStatement(sql, autoGeneratedKeys);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        decorated.rollback(savepoint);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        decorated.setClientInfo(properties);
    }

    public boolean isClosed() throws SQLException {
        return decorated.isClosed();
    }

    public boolean isValid(int timeout) throws SQLException {
        return decorated.isValid(timeout);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return decorated.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public String nativeSQL(String sql) throws SQLException {
        return decorated.nativeSQL(sql);
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        decorated.setTypeMap(map);
    }

    public void commit() throws SQLException {
        decorated.commit();
    }

    public Blob createBlob() throws SQLException {
        return decorated.createBlob();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return decorated.setSavepoint(name);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return decorated.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return decorated.createStatement(resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return decorated.prepareStatement(sql, columnNames);
    }

    public void setCatalog(String catalog) throws SQLException {
        decorated.setCatalog(catalog);
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        decorated.setReadOnly(readOnly);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return decorated.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
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

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        decorated.setAutoCommit(autoCommit);
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return decorated.createArrayOf(typeName, elements);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return decorated.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public int getHoldability() throws SQLException {
        return decorated.getHoldability();
    }

    public void clearWarnings() throws SQLException {
        decorated.clearWarnings();
    }

    public void rollback() throws SQLException {
        decorated.rollback();
    }

    public String getCatalog() throws SQLException {
        return decorated.getCatalog();
    }

    @SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        if (iface.isInstance(decorated)) {
            return (T) decorated;
        }
        if (decorated instanceof JdbcConnection) {
            throw new SQLException();
        }
        return decorated.unwrap(iface);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return decorated.createStruct(typeName, attributes);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        decorated.setNetworkTimeout(executor, milliseconds);
    }

    public StackTraceElement[] getOriginatingStackTrace() {
        return stackTrace;
    }

    public int getIdentifier() {
        return identifier;
    }
}
