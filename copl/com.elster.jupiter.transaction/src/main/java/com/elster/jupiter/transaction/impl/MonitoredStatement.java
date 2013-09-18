package com.elster.jupiter.transaction.impl;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.*;
import java.util.*;

import com.elster.jupiter.util.time.StopWatch;

class MonitoredStatement implements PreparedStatement {
	
	private final PreparedStatement statement;
	private final List<Object> parameters = new ArrayList<>();
	private final StopWatch stopWatch;
	private final String text;
	private MonitoredResultSet resultSet;
	
	MonitoredStatement(PreparedStatement statement, String text) {
		this.statement = statement;
		this.text = text;
		this.stopWatch = new StopWatch();
	}

	private void add(int offset, Object value) {
		if (parameters.size() == --offset) {
			parameters.add(value);
			return;
		}
		if (parameters.size() > offset) {
			parameters.set(offset,value);
			return;
		}
		for (int i = parameters.size() ; i < offset ; i++) {
			parameters.add(null);
		}
		parameters.add(value);
	}
	
	private ResultSet wrap(ResultSet resultSet) {
		this.resultSet = new MonitoredResultSet(resultSet);
		return this.resultSet;
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
		stopWatch.stop();
		System.out.println("SQL executed in " + (stopWatch.getElapsed() / 1000L) + " µs");
		System.out.println("\tText: " + text);
		System.out.println("\tBind variables: " + parameters);
		if (resultSet != null) {
			System.out.println("\tFetched " + resultSet.getFetchCount() + " tuples");
		}
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
		return statement.execute(arg0,arg1);
	}

	@Override
	public boolean execute(String arg0, int[] arg1) throws SQLException {
		return statement.execute(arg0,arg1);
	}

	@Override
	public boolean execute(String arg0, String[] arg1) throws SQLException {
		return statement.execute(arg0,arg1);
	}

	@Override
	public int[] executeBatch() throws SQLException {
		return statement.executeBatch();
	}

	@Override
	public ResultSet executeQuery(String arg0) throws SQLException {
		return wrap(statement.executeQuery(arg0));
	}

	@Override
	public int executeUpdate(String arg0) throws SQLException {
		return statement.executeUpdate(arg0);
	}

	@Override
	public int executeUpdate(String arg0, int arg1) throws SQLException {	
		return statement.executeUpdate(arg0,arg1);
	}

	@Override
	public int executeUpdate(String arg0, int[] arg1) throws SQLException {
		return statement.executeUpdate(arg0,arg1);
	}

	@Override
	public int executeUpdate(String arg0, String[] arg1) throws SQLException {		
		return statement.executeUpdate(arg0,arg1);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return statement.getConnection();
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
	public ResultSet getGeneratedKeys() throws SQLException {
		return statement.getGeneratedKeys();
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
	public ResultSet getResultSet() throws SQLException {
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
	public ResultSet executeQuery() throws SQLException {
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
		add(arg0,arg1);
		statement.setArray(arg0, arg1);
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
		add(arg0,arg1);
		statement.setAsciiStream(arg0,arg1);
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		add(arg0,arg1);
		statement.setAsciiStream(arg0,arg1,arg2);
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		add(arg0,arg1);
		statement.setAsciiStream(arg0,arg1,arg2);
	}

	@Override
	public void setBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
		add(arg0,arg1);
		statement.setBigDecimal(arg0, arg1);
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
		add(arg0,arg1);
		statement.setBinaryStream(arg0,arg1);
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		add(arg0,arg1);
		statement.setBinaryStream(arg0,arg1,arg2);
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		add(arg0,arg1);
		statement.setBinaryStream(arg0,arg1,arg2);
	}

	@Override
	public void setBlob(int arg0, Blob arg1) throws SQLException {
		add(arg0,arg1);
		statement.setBlob(arg0,arg1);
	}

	@Override
	public void setBlob(int arg0, InputStream arg1) throws SQLException {
		add(arg0,arg1);
		statement.setBlob(arg0,arg1);
	}

	@Override
	public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
		add(arg0,arg1);
		statement.setBlob(arg0,arg1,arg2);
	}

	@Override
	public void setBoolean(int arg0, boolean arg1) throws SQLException {
		add(arg0,arg1);
		statement.setBoolean(arg0,arg1);
	}

	@Override
	public void setByte(int arg0, byte arg1) throws SQLException {
		add(arg0,arg1);
		statement.setByte(arg0,arg1);
	}

	@Override
	public void setBytes(int arg0, byte[] arg1) throws SQLException {
		add(arg0,arg1);
		statement.setBytes(arg0,arg1);
	}

	public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
		add(arg0,arg1);
		statement.setCharacterStream(arg0,arg1);
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
		add(arg0,arg1);
		statement.setCharacterStream(arg0,arg1,arg2);
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
		add(arg0,arg1);
		statement.setCharacterStream(arg0,arg1,arg2);
	}

	@Override
	public void setClob(int arg0, Clob arg1) throws SQLException {
		add(arg0,arg1);
		statement.setClob(arg0,arg1);
	}

	@Override
	public void setClob(int arg0, Reader arg1) throws SQLException {
		add(arg0,arg1);
		statement.setClob(arg0,arg1);
	}

	@Override
	public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
		add(arg0,arg1);
		statement.setClob(arg0,arg1,arg2);
	}

	@Override
	public void setDate(int arg0, Date arg1) throws SQLException {
		add(arg0,arg1);
		statement.setDate(arg0, arg1);
	}

	@Override
	public void setDate(int arg0, Date arg1, Calendar arg2) throws SQLException {
		add(arg0,arg1);
		statement.setDate(arg0, arg1, arg2);
	}

	@Override
	public void setDouble(int arg0, double arg1) throws SQLException {
		add(arg0,arg1);
		statement.setDouble(arg0, arg1);
	}

	@Override
	public void setFloat(int arg0, float arg1) throws SQLException {
		add(arg0,arg1);
		statement.setFloat(arg0, arg1);
	}

	@Override
	public void setInt(int arg0, int arg1) throws SQLException {
		add(arg0,arg1);
		statement.setInt(arg0, arg1);
	}

	@Override
	public void setLong(int arg0, long arg1) throws SQLException {
		add(arg0,arg1);
		statement.setLong(arg0, arg1);
	}

	@Override
	public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
		add(arg0,arg1);
		statement.setNCharacterStream(arg0, arg1);
	}

	@Override
	public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
		add(arg0,arg1);
		statement.setNCharacterStream(arg0, arg1,arg2);
	}

	@Override
	public void setNClob(int arg0, NClob arg1) throws SQLException {
		add(arg0,arg1);
		statement.setNClob(arg0, arg1);
	}

	@Override
	public void setNClob(int arg0, Reader arg1) throws SQLException {
		add(arg0,arg1);
		statement.setNClob(arg0, arg1);
	}

	@Override
	public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
		add(arg0,arg1);
		statement.setNClob(arg0, arg1,arg2);
	}

	@Override
	public void setNString(int arg0, String arg1) throws SQLException {
		add(arg0,arg1);
		statement.setNString(arg0, arg1);
	}

	@Override
	public void setNull(int arg0, int arg1) throws SQLException {
		add(arg0,null);
		statement.setNull(arg0, arg1);
	}

	@Override
	public void setNull(int arg0, int arg1, String arg2) throws SQLException {
		add(arg0,null);
		statement.setNull(arg0, arg1,arg2);
	}

	@Override
	public void setObject(int arg0, Object arg1) throws SQLException {
		add(arg0,arg1);
		statement.setObject(arg0, arg1);
	}

	@Override
	public void setObject(int arg0, Object arg1, int arg2) throws SQLException {
		add(arg0,arg1);
		statement.setObject(arg0, arg1,arg2);
	}

	@Override
	public void setObject(int arg0, Object arg1, int arg2, int arg3) throws SQLException {
		add(arg0,arg1);
		statement.setObject(arg0, arg1,arg2,arg3);
	}

	@Override
	public void setRef(int arg0, Ref arg1) throws SQLException {
		add(arg0,arg1);
		statement.setRef(arg0, arg1);
	}

	@Override
	public void setRowId(int arg0, RowId arg1) throws SQLException {
		add(arg0,arg1);
		statement.setRowId(arg0, arg1);
	}

	@Override
	public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
		add(arg0,arg1);
		statement.setSQLXML(arg0, arg1);
	}

	@Override
	public void setShort(int arg0, short arg1) throws SQLException {
		add(arg0,arg1);
		statement.setShort(arg0, arg1);
	}

	@Override
	public void setString(int arg0, String arg1) throws SQLException {
		add(arg0,arg1);
		statement.setString(arg0, arg1);		// TODO Auto-generated method stub
	}

	@Override
	public void setTime(int arg0, Time arg1) throws SQLException {
		add(arg0,arg1);
		statement.setTime(arg0, arg1);
	}

	@Override
	public void setTime(int arg0, Time arg1, Calendar arg2) throws SQLException {
		add(arg0,arg1);
		statement.setTime(arg0, arg1,arg2);
	}

	@Override
	public void setTimestamp(int arg0, Timestamp arg1) throws SQLException {
		add(arg0,arg1);
		statement.setTimestamp(arg0, arg1);
	}

	@Override
	public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2) throws SQLException {
		add(arg0,arg1);
		statement.setTimestamp(arg0, arg1);
	}

	@Override
	public void setURL(int arg0, URL arg1) throws SQLException {
		add(arg0,arg1);
		statement.setURL(arg0, arg1);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setUnicodeStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		add(arg0,arg1);
		statement.setUnicodeStream(arg0, arg1,arg2);
	}

}
