/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

class PreparedStatementWrapper implements PreparedStatement {
	  private final PreparedStatement statement;
	  private final Connection connection;
	  
	  PreparedStatementWrapper(PreparedStatement statement, Connection connection) {
		  this.statement = statement;
	      this.connection = connection;
	  }

	    @Override
	    public void addBatch(String arg0) throws SQLException {
	        statement.addBatch(arg0);
	    }

	    @Override
	    public void cancel() throws SQLException {
	        statement.cancel();
	    }

	    @Override
	    public void clearBatch() throws SQLException {
	        statement.clearBatch();
	    }

	    @Override
	    public void clearWarnings() throws SQLException {
	        statement.clearWarnings();
	    }

	    @Override
	    public void close() throws SQLException {
	        statement.close();
	   
	    }

	    @Override
	    public void closeOnCompletion() throws SQLException {
	        statement.closeOnCompletion();
	    }

	    @Override
	    public boolean execute(String arg0) throws SQLException {
	        return statement.execute(arg0);
	    }

	    @Override
	    public boolean execute(String arg0, int arg1) throws SQLException {
	        return statement.execute(arg0, arg1);
	    }

	    @Override
	    public boolean execute(String arg0, int[] arg1) throws SQLException {
	        return statement.execute(arg0, arg1);
	    }

	    @Override
	    public boolean execute(String arg0, String[] arg1) throws SQLException {
	        return statement.execute(arg0, arg1);
	    }

	    @Override
	    public int[] executeBatch() throws SQLException {
	        return statement.executeBatch();
	    }

	    @Override
	    public final ResultSet executeQuery(String arg0) throws SQLException {
	        return wrap(statement.executeQuery(arg0));
	    }

	    @Override
	    public int executeUpdate(String arg0) throws SQLException {
	        return statement.executeUpdate(arg0);
	    }

	    @Override
	    public int executeUpdate(String arg0, int arg1) throws SQLException {
	        return statement.executeUpdate(arg0, arg1);
	    }

	    @Override
	    public int executeUpdate(String arg0, int[] arg1) throws SQLException {
	        return statement.executeUpdate(arg0, arg1);
	    }

	    @Override
	    public int executeUpdate(String arg0, String[] arg1) throws SQLException {
	        return statement.executeUpdate(arg0, arg1);
	    }

	    @Override
	    public Connection getConnection() throws SQLException {
	        return connection;
	    }

	    @Override
	    public int getFetchDirection() throws SQLException {
	        return statement.getFetchDirection();
	    }

	    @Override
	    public int getFetchSize() throws SQLException {
	        return statement.getFetchSize();
	    }

	    @Override
	    public final ResultSet getGeneratedKeys() throws SQLException {
	        return wrap(statement.getGeneratedKeys());
	    }

	    @Override
	    public int getMaxFieldSize() throws SQLException {
	        return statement.getMaxFieldSize();
	    }

	    @Override
	    public int getMaxRows() throws SQLException {
	        return statement.getMaxRows();
	    }

	    @Override
	    public boolean getMoreResults() throws SQLException {
	        return statement.getMoreResults();
	    }

	    @Override
	    public boolean getMoreResults(int arg0) throws SQLException {
	        return statement.getMoreResults(arg0);
	    }

	    @Override
	    public int getQueryTimeout() throws SQLException {
	        return statement.getQueryTimeout();
	    }

	    @Override
	    public final ResultSet getResultSet() throws SQLException {
	        return wrap(statement.getResultSet());
	    }

	    @Override
	    public int getResultSetConcurrency() throws SQLException {
	        return statement.getResultSetConcurrency();
	    }

	    @Override
	    public int getResultSetHoldability() throws SQLException {
	        return statement.getResultSetHoldability();
	    }

	    @Override
	    public int getResultSetType() throws SQLException {
	        return statement.getResultSetType();
	    }

	    @Override
	    public int getUpdateCount() throws SQLException {
	        return statement.getUpdateCount();
	    }

	    @Override
	    public SQLWarning getWarnings() throws SQLException {
	        return statement.getWarnings();
	    }

	    @Override
	    public boolean isCloseOnCompletion() throws SQLException {
	        return statement.isCloseOnCompletion();
	    }

	    @Override
	    public boolean isClosed() throws SQLException {
	        return statement.isClosed();
	    }

	    @Override
	    public boolean isPoolable() throws SQLException {
	        return statement.isPoolable();
	    }

	    @Override
	    public void setCursorName(String arg0) throws SQLException {
	        statement.setCursorName(arg0);
	    }

	    @Override
	    public void setEscapeProcessing(boolean arg0) throws SQLException {
	        statement.setEscapeProcessing(arg0);
	    }

	    @Override
	    public void setFetchDirection(int arg0) throws SQLException {
	        statement.setFetchDirection(arg0);
	    }

	    @Override
	    public void setFetchSize(int arg0) throws SQLException {
	        statement.setFetchSize(arg0);
	    }

	    @Override
	    public void setMaxFieldSize(int arg0) throws SQLException {
	        statement.setMaxFieldSize(arg0);

	    }

	    @Override
	    public void setMaxRows(int arg0) throws SQLException {
	        statement.setMaxRows(arg0);
	    }

	    @Override
	    public void setPoolable(boolean arg0) throws SQLException {
	        statement.setPoolable(arg0);
	    }

	    @Override
	    public void setQueryTimeout(int arg0) throws SQLException {
	        statement.setQueryTimeout(arg0);

	    }

	    @Override
	    public boolean isWrapperFor(Class<?> iface) throws SQLException {
	        return iface.isInstance(this) || statement.isWrapperFor(iface);
	    }

	    @SuppressWarnings("unchecked")
	    @Override
	    public <T> T unwrap(Class<T> iface) throws SQLException {
	        return iface.isInstance(this) ? (T) this : statement.unwrap(iface);
	    }

	    @Override
	    public void addBatch() throws SQLException {
	        statement.addBatch();
	    }

	    @Override
	    public void clearParameters() throws SQLException {
	        statement.clearParameters();

	    }

	    @Override
	    public boolean execute() throws SQLException {
	        return statement.execute();
	    }

	    @Override
	    public final ResultSet executeQuery() throws SQLException {
	        return wrap(statement.executeQuery());
	    }

	    @Override
	    public int executeUpdate() throws SQLException {
	        return statement.executeUpdate();
	    }

	    @Override
	    public ResultSetMetaData getMetaData() throws SQLException {
	        return statement.getMetaData();
	    }

	    @Override
	    public ParameterMetaData getParameterMetaData() throws SQLException {
	        return statement.getParameterMetaData();
	    }

	    @Override
	    public void setArray(int arg0, Array arg1) throws SQLException {
	        statement.setArray(arg0, arg1);
	    }

	    @Override
	    public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {	        
	        statement.setAsciiStream(arg0, arg1);
	    }

	    @Override
	    public void setAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
	        statement.setAsciiStream(arg0, arg1, arg2);
	    }

	    @Override
	    public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
	        statement.setAsciiStream(arg0, arg1, arg2);
	    }

	    @Override
	    public void setBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
	        statement.setBigDecimal(arg0, arg1);
	    }

	    @Override
	    public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
	        statement.setBinaryStream(arg0, arg1);
	    }

	    @Override
	    public void setBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
	        statement.setBinaryStream(arg0, arg1, arg2);
	    }

	    @Override
	    public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
	        statement.setBinaryStream(arg0, arg1, arg2);
	    }

	    @Override
	    public void setBlob(int arg0, Blob arg1) throws SQLException {
	        statement.setBlob(arg0, arg1);
	    }

	    @Override
	    public void setBlob(int arg0, InputStream arg1) throws SQLException {
	        statement.setBlob(arg0, arg1);
	    }

	    @Override
	    public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
	        statement.setBlob(arg0, arg1, arg2);
	    }

	    @Override
	    public void setBoolean(int arg0, boolean arg1) throws SQLException {
	        statement.setBoolean(arg0, arg1);
	    }

	    @Override
	    public void setByte(int arg0, byte arg1) throws SQLException {
	        statement.setByte(arg0, arg1);
	    }

	    @Override
	    public void setBytes(int arg0, byte[] arg1) throws SQLException {
	        statement.setBytes(arg0, arg1);
	    }

	    public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
	        statement.setCharacterStream(arg0, arg1);
	    }

	    @Override
	    public void setCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
	        statement.setCharacterStream(arg0, arg1, arg2);
	    }

	    @Override
	    public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
	        statement.setCharacterStream(arg0, arg1, arg2);
	    }

	    @Override
	    public void setClob(int arg0, Clob arg1) throws SQLException {
	        statement.setClob(arg0, arg1);
	    }

	    @Override
	    public void setClob(int arg0, Reader arg1) throws SQLException {
	        statement.setClob(arg0, arg1);
	    }

	    @Override
	    public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
	        statement.setClob(arg0, arg1, arg2);
	    }

	    @Override
	    public void setDate(int arg0, Date arg1) throws SQLException {
	        statement.setDate(arg0, arg1);
	    }

	    @Override
	    public void setDate(int arg0, Date arg1, Calendar arg2) throws SQLException {
	        statement.setDate(arg0, arg1, arg2);
	    }

	    @Override
	    public void setDouble(int arg0, double arg1) throws SQLException {
	        statement.setDouble(arg0, arg1);
	    }

	    @Override
	    public void setFloat(int arg0, float arg1) throws SQLException {
	        statement.setFloat(arg0, arg1);
	    }

	    @Override
	    public void setInt(int arg0, int arg1) throws SQLException {
	        statement.setInt(arg0, arg1);
	    }

	    @Override
	    public void setLong(int arg0, long arg1) throws SQLException {
	        statement.setLong(arg0, arg1);
	    }

	    @Override
	    public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
	        statement.setNCharacterStream(arg0, arg1);
	    }

	    @Override
	    public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
	        statement.setNCharacterStream(arg0, arg1, arg2);
	    }

	    @Override
	    public void setNClob(int arg0, NClob arg1) throws SQLException {
	        statement.setNClob(arg0, arg1);
	    }

	    @Override
	    public void setNClob(int arg0, Reader arg1) throws SQLException {
	        statement.setNClob(arg0, arg1);
	    }

	    @Override
	    public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
	        statement.setNClob(arg0, arg1, arg2);
	    }

	    @Override
	    public void setNString(int arg0, String arg1) throws SQLException {
	        statement.setNString(arg0, arg1);
	    }

	    @Override
	    public void setNull(int arg0, int arg1) throws SQLException {
	        statement.setNull(arg0, arg1);
	    }

	    @Override
	    public void setNull(int arg0, int arg1, String arg2) throws SQLException {
	        statement.setNull(arg0, arg1, arg2);
	    }

	    @Override
	    public void setObject(int arg0, Object arg1) throws SQLException {
	        statement.setObject(arg0, arg1);
	    }

	    @Override
	    public void setObject(int arg0, Object arg1, int arg2) throws SQLException {
	        statement.setObject(arg0, arg1, arg2);
	    }

	    @Override
	    public void setObject(int arg0, Object arg1, int arg2, int arg3) throws SQLException {
	        statement.setObject(arg0, arg1, arg2, arg3);
	    }

	    @Override
	    public void setRef(int arg0, Ref arg1) throws SQLException {
	        statement.setRef(arg0, arg1);
	    }

	    @Override
	    public void setRowId(int arg0, RowId arg1) throws SQLException {
	        statement.setRowId(arg0, arg1);
	    }

	    @Override
	    public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
	        statement.setSQLXML(arg0, arg1);
	    }

	    @Override
	    public void setShort(int arg0, short arg1) throws SQLException {
	        statement.setShort(arg0, arg1);
	    }

	    @Override
	    public void setString(int arg0, String arg1) throws SQLException {
	        statement.setString(arg0, arg1);      
	    }

	    @Override
	    public void setTime(int arg0, Time arg1) throws SQLException {
	        statement.setTime(arg0, arg1);
	    }

	    @Override
	    public void setTime(int arg0, Time arg1, Calendar arg2) throws SQLException {
	        statement.setTime(arg0, arg1, arg2);
	    }

	    @Override
	    public void setTimestamp(int arg0, Timestamp arg1) throws SQLException {
	        statement.setTimestamp(arg0, arg1);
	    }

	    @Override
	    public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2) throws SQLException {
	        statement.setTimestamp(arg0, arg1);
	    }

	    @Override
	    public void setURL(int arg0, URL arg1) throws SQLException {
	        statement.setURL(arg0, arg1);
	    }

	    @SuppressWarnings("deprecation")
	    @Override
	    public void setUnicodeStream(int arg0, InputStream arg1, int arg2) throws SQLException {
	        statement.setUnicodeStream(arg0, arg1, arg2);
	    }
	    
	    ResultSet wrap(ResultSet resultSet) {
	    	return new ResultSetWrapper(resultSet, this);
	    }

}
